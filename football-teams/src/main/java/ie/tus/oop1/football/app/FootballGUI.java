package ie.tus.oop1.football.app;

import ie.tus.oop1.football.dao.*;
import ie.tus.oop1.football.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class FootballGUI extends JFrame {

    // DAOs
    private final TeamDAO teamDAO = new TeamDAO();
    private final PlayerDAO playerDAO = new PlayerDAO();
    private final CoachDAO coachDAO = new CoachDAO();

    // UI Components
    private JLabel lblStatus, lblStats;
    private JTable tblTeams, tblPlayers, tblCoaches;
    private DefaultTableModel teamModel, playerModel, coachModel;
    private JTextField txtSearch;
    private JTextArea txtLog;

    private List<Team> teams = new ArrayList<>();
    private Map<Integer, List<Player>> playersByTeam = new HashMap<>();
    private List<Coach> coaches = new ArrayList<>();

    public FootballGUI() {
        setTitle("⚽ Football Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildTabs(), BorderLayout.CENTER);
        add(buildLogArea(), BorderLayout.SOUTH);

        updateDbStatus();
        loadAllDataAsync();
    }

    // ---------------- TOP BAR ----------------
    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout(10, 0));
        top.setBorder(new EmptyBorder(8, 8, 8, 8));

        lblStatus = new JLabel("DB: Checking...");
        lblStatus.setFont(lblStatus.getFont().deriveFont(Font.BOLD, 13f));
        lblStats = new JLabel("");

        JButton btnRefresh = new JButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> loadAllDataAsync());

        txtSearch = new JTextField(20);
        txtSearch.putClientProperty("JTextField.placeholderText", "Search by name...");
        txtSearch.addActionListener(e -> filterSearch());

        JPanel right = new JPanel();
        right.add(txtSearch);
        right.add(btnRefresh);

        top.add(lblStatus, BorderLayout.WEST);
        top.add(lblStats, BorderLayout.CENTER);
        top.add(right, BorderLayout.EAST);

        return top;
    }

    // ---------------- TABS ----------------
    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Teams / Players", buildTeamsPlayersPanel());
        tabs.addTab("Coaches", buildCoachesPanel());
        return tabs;
    }

    private JPanel buildTeamsPlayersPanel() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.45);

        // === Teams ===
        teamModel = new DefaultTableModel(new Object[]{"ID", "Name", "City", "Founded"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblTeams = new JTable(teamModel);
        tblTeams.getSelectionModel().addListSelectionListener(this::onTeamSelected);

        JPanel left = new JPanel(new BorderLayout(5, 5));
        left.setBorder(new EmptyBorder(6, 8, 6, 8));
        left.add(new JLabel("Teams", SwingConstants.LEFT), BorderLayout.NORTH);
        left.add(new JScrollPane(tblTeams), BorderLayout.CENTER);
        left.add(buildAddTeamPanel(), BorderLayout.SOUTH);

        // === Players ===
        playerModel = new DefaultTableModel(new Object[]{"ID", "Name", "Position", "Age", "TeamId"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblPlayers = new JTable(playerModel);

        JPanel right = new JPanel(new BorderLayout(5, 5));
        right.setBorder(new EmptyBorder(6, 8, 6, 8));
        right.add(new JLabel("Players (by team)"), BorderLayout.NORTH);
        right.add(new JScrollPane(tblPlayers), BorderLayout.CENTER);
        right.add(buildAddPlayerPanel(), BorderLayout.SOUTH);

        split.setLeftComponent(left);
        split.setRightComponent(right);
        JPanel p = new JPanel(new BorderLayout());
        p.add(split, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildCoachesPanel() {
        coachModel = new DefaultTableModel(new Object[]{"ID", "Name", "Age", "TeamId"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblCoaches = new JTable(coachModel);

        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(new EmptyBorder(6, 8, 6, 8));
        p.add(new JScrollPane(tblCoaches), BorderLayout.CENTER);
        JButton btnAddCoach = new JButton("Add Coach");
        btnAddCoach.addActionListener(e -> addCoachDialog());
        p.add(btnAddCoach, BorderLayout.SOUTH);
        return p;
    }

    // ---------------- ADD PANELS ----------------
    private JPanel buildAddTeamPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField txtName = new JTextField(10);
        JTextField txtCity = new JTextField(8);
        JTextField txtYear = new JTextField(5);
        JButton btnAdd = new JButton("Add");
        btnAdd.addActionListener(e -> {
            try {
                int year = Integer.parseInt(txtYear.getText());
                int id = teamDAO.addTeamAndReturnId(new Team(txtName.getText(), txtCity.getText(), year));
                toast(id > 0 ? "✅ Team added" : "ℹ️ Team exists");
                loadAllDataAsync();
            } catch (Exception ex) { toast("Error: " + ex.getMessage()); }
        });
        p.add(new JLabel("Name:"));
        p.add(txtName);
        p.add(new JLabel("City:"));
        p.add(txtCity);
        p.add(new JLabel("Year:"));
        p.add(txtYear);
        p.add(btnAdd);
        return p;
    }

    private JPanel buildAddPlayerPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField txtName = new JTextField(10);
        JTextField txtAge = new JTextField(5);
        JComboBox<Position> cmbPos = new JComboBox<>(Position.values());
        JButton btnAdd = new JButton("Add");
        btnAdd.addActionListener(e -> {
            int row = tblTeams.getSelectedRow();
            if (row < 0) { toast("Select a team first."); return; }
            int teamId = (int) teamModel.getValueAt(row, 0);
            try {
                boolean ok = playerDAO.addPlayer(new Player(txtName.getText(), (Position)cmbPos.getSelectedItem(), Integer.parseInt(txtAge.getText()), teamId));
                toast(ok ? "✅ Player added" : "ℹ️ Player exists");
                refreshPlayersTable(teamId);
                loadAllDataAsync();
            } catch (Exception ex) { toast("Error: " + ex.getMessage()); }
        });
        p.add(new JLabel("Name:"));
        p.add(txtName);
        p.add(new JLabel("Pos:"));
        p.add(cmbPos);
        p.add(new JLabel("Age:"));
        p.add(txtAge);
        p.add(btnAdd);
        return p;
    }

    private void addCoachDialog() {
        JTextField txtName = new JTextField();
        JTextField txtAge = new JTextField();
        JComboBox<Team> cmbTeam = new JComboBox<>(teams.toArray(new Team[0]));
        Object[] fields = {
                "Name:", txtName,
                "Age:", txtAge,
                "Team:", cmbTeam
        };
        int res = JOptionPane.showConfirmDialog(this, fields, "Add Coach", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                Team team = (Team)cmbTeam.getSelectedItem();
                boolean ok = coachDAO.addCoach(new Coach(txtName.getText(), Integer.parseInt(txtAge.getText()), team.id()));
                toast(ok ? "✅ Coach added" : "ℹ️ Coach exists");
                loadAllDataAsync();
            } catch (Exception ex) { toast("Error: " + ex.getMessage()); }
        }
    }

    // ---------------- LOG AREA ----------------
    private JScrollPane buildLogArea() {
        txtLog = new JTextArea(4, 20);
        txtLog.setEditable(false);
        txtLog.setLineWrap(true);
        JScrollPane sp = new JScrollPane(txtLog);
        sp.setBorder(new EmptyBorder(0, 8, 8, 8));
        return sp;
    }

    // ---------------- EVENTS ----------------
    private void onTeamSelected(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        int row = tblTeams.getSelectedRow();
        if (row < 0) return;
        int teamId = (int) teamModel.getValueAt(row, 0);
        refreshPlayersTable(teamId);
    }

    // ---------------- DATA ----------------
    private void loadAllDataAsync() {
        new SwingWorker<Void, Void>() {
            protected Void doInBackground() {
                teams = teamDAO.getAllTeams();
                playersByTeam.clear();
                for (Team t : teamDAO.getAllTeamsWithPlayers())
                    playersByTeam.put(t.id(), t.getPlayers());
                coaches = coachDAO.getAllCoaches();
                return null;
            }
            protected void done() {
                populateTables();
                lblStats.setText(makeStatsLine());
            }
        }.execute();
    }

    private void populateTables() {
        teamModel.setRowCount(0);
        for (Team t : teams)
            teamModel.addRow(new Object[]{t.id(), t.getName(), t.getCity(), t.getFoundedYear()});

        coachModel.setRowCount(0);
        for (Coach c : coaches)
            coachModel.addRow(new Object[]{c.id(), c.getName(), c.getAge(), c.id()});
    }

    private void refreshPlayersTable(int teamId) {
        playerModel.setRowCount(0);
        List<Player> players = playersByTeam.getOrDefault(teamId, List.of());
        for (Player p : players)
            playerModel.addRow(new Object[]{p.getIdBoxed(), p.getName(), p.getPosition(), p.getAge(), p.getTeamId()});
    }

    private String makeStatsLine() {
        int t = teams.size();
        int p = playersByTeam.values().stream().mapToInt(List::size).sum();
        int c = coaches.size();
        return String.format("Teams: %d | Players: %d | Coaches: %d", t, p, c);
    }

    private void updateDbStatus() {
        new SwingWorker<Boolean, Void>() {
            protected Boolean doInBackground() {
                try (Connection c = DatabaseConnection.get()) { return c != null && !c.isClosed(); }
                catch (Exception e) { return false; }
            }
            protected void done() {
                try {
                    boolean ok = get();
                    lblStatus.setText(ok ? "DB: Connected ✅" : "DB: Not connected ❌");
                    lblStatus.setForeground(ok ? new Color(0,128,0) : Color.RED);
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void filterSearch() {
        String q = txtSearch.getText().trim().toLowerCase();
        teamModel.setRowCount(0);
        for (Team t : teams)
            if (t.getName().toLowerCase().contains(q) || t.getCity().toLowerCase().contains(q))
                teamModel.addRow(new Object[]{t.id(), t.getName(), t.getCity(), t.getFoundedYear()});
    }

    private void toast(String msg) {
        txtLog.append(msg + "\n");
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FootballGUI().setVisible(true));
    }
}


