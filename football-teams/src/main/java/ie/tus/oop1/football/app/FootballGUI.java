





package ie.tus.oop1.football.app;

import ie.tus.oop1.football.dao.CoachDAO;
import ie.tus.oop1.football.dao.DatabaseConnection;
import ie.tus.oop1.football.dao.PlayerDAO;
import ie.tus.oop1.football.dao.TeamDAO;
import ie.tus.oop1.football.model.Coach;
import ie.tus.oop1.football.model.Player;
import ie.tus.oop1.football.model.Position;
import ie.tus.oop1.football.model.Team;
import e.tus.oop1.football.service.DatabaseMigrator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.util.*;
import java.util.List;

public class FootballGUI extends JFrame {

    private final TeamDAO teamDAO = new TeamDAO();
    private final PlayerDAO playerDAO = new PlayerDAO();
    private final CoachDAO coachDAO = new CoachDAO();

    private JLabel lblStatus, lblStats;
    private JTable tblTeams, tblPlayers, tblCoaches;
    private DefaultTableModel teamModel, playerModel, coachModel;
    private JTextField txtSearchTeams, txtSearchPlayers;
    private JTextArea txtLog;

    private List<Team> teams = new ArrayList<>();
    private Map<Integer, List<Player>> playersByTeam = new HashMap<>();
    private List<Coach> coaches = new ArrayList<>();

    public FootballGUI() {
        setTitle("⚽ Football Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1250, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildTabs(), BorderLayout.CENTER);
        add(buildLogArea(), BorderLayout.SOUTH);

        updateDbStatus();
        loadAllDataAsync();
    }

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout(10, 0));
        top.setBorder(new EmptyBorder(8, 8, 8, 8));

        lblStatus = new JLabel("DB: Checking…");
        lblStatus.setFont(lblStatus.getFont().deriveFont(Font.BOLD, 13f));
        lblStats = new JLabel("");

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> loadAllDataAsync());

        txtSearchTeams = new JTextField(22);
        txtSearchTeams.putClientProperty("JTextField.placeholderText", "Search teams…");
        txtSearchTeams.addActionListener(e -> filterTeams());

        JPanel right = new JPanel();
        right.add(txtSearchTeams);
        right.add(btnRefresh);

        top.add(lblStatus, BorderLayout.WEST);
        top.add(lblStats, BorderLayout.CENTER);
        top.add(right, BorderLayout.EAST);
        return top;
    }

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
        teamModel = new DefaultTableModel(new Object[]{"ID", "Name", "City", "Founded", "Delete"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return (c >= 1 && c <= 3) || c == 4; }
            @Override public Class<?> getColumnClass(int c) { return (c == 0 || c == 3) ? Integer.class : Object.class; }
        };
        tblTeams = new JTable(teamModel);
        tblTeams.setFillsViewportHeight(true);
        tblTeams.putClientProperty("terminateEditOnFocusLost", true);
        tblTeams.getSelectionModel().addListSelectionListener(this::onTeamSelected);
        addDeleteButton(tblTeams, 4, Color.RED.darker(), this::deleteTeamAtRow);
        leftAlignNumberColumns(tblTeams, new int[]{0, 3});
        teamModel.addTableModelListener(this::inlineTeamEdit);

        JPanel left = new JPanel(new BorderLayout(5, 5));
        left.setBorder(new EmptyBorder(6, 8, 6, 8));
        left.add(new JLabel("Teams"), BorderLayout.NORTH);
        left.add(new JScrollPane(tblTeams), BorderLayout.CENTER);
        left.add(buildAddTeamPanel(), BorderLayout.SOUTH);

        // === Players ===
        playerModel = new DefaultTableModel(new Object[]{"ID", "Name", "Position", "Age", "TeamId", "Delete"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return (c >= 1 && c <= 3) || c == 5; }
            @Override public Class<?> getColumnClass(int c) {
                return switch (c) { case 0, 3, 4 -> Integer.class; case 2 -> Position.class; default -> Object.class; };
            }
        };
        tblPlayers = new JTable(playerModel);
        tblPlayers.setFillsViewportHeight(true);
        tblPlayers.putClientProperty("terminateEditOnFocusLost", true);
        addDeleteButton(tblPlayers, 5, Color.RED.darker(), this::deletePlayerAtRow);
        leftAlignNumberColumns(tblPlayers, new int[]{0, 3, 4});
        installPlayerPositionEditor();
        playerModel.addTableModelListener(this::inlinePlayerEdit);

        JPanel rightNorth = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rightNorth.add(new JLabel("Players"));
        txtSearchPlayers = new JTextField(18);
        txtSearchPlayers.putClientProperty("JTextField.placeholderText", "Search players…");
        txtSearchPlayers.addActionListener(e -> filterPlayersForSelectedTeam());
        rightNorth.add(txtSearchPlayers);

        JPanel right = new JPanel(new BorderLayout(5, 5));
        right.setBorder(new EmptyBorder(6, 8, 6, 8));
        right.add(rightNorth, BorderLayout.NORTH);
        right.add(new JScrollPane(tblPlayers), BorderLayout.CENTER);
        right.add(buildAddPlayerPanel(), BorderLayout.SOUTH);

        split.setLeftComponent(left);
        split.setRightComponent(right);
        JPanel p = new JPanel(new BorderLayout());
        p.add(split, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildCoachesPanel() {
        coachModel = new DefaultTableModel(new Object[]{"ID", "Name", "Age", "TeamId", "Delete"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return (c == 1 || c == 2) || c == 4; }
            @Override public Class<?> getColumnClass(int c) { return (c == 0 || c == 2 || c == 3) ? Integer.class : Object.class; }
        };
        tblCoaches = new JTable(coachModel);
        tblCoaches.setFillsViewportHeight(true);
        tblCoaches.putClientProperty("terminateEditOnFocusLost", true);
        addDeleteButton(tblCoaches, 4, Color.RED.darker(), this::deleteCoachAtRow);
        leftAlignNumberColumns(tblCoaches, new int[]{0, 2, 3});
        coachModel.addTableModelListener(this::inlineCoachEdit);

        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(new EmptyBorder(6, 8, 6, 8));
        p.add(new JScrollPane(tblCoaches), BorderLayout.CENTER);
        JButton btnAddCoach = new JButton("Add Coach");
        btnAddCoach.addActionListener(e -> addCoachDialog());
        p.add(btnAddCoach, BorderLayout.SOUTH);
        return p;
    }

    // ---------------- INLINE EDITS ----------------
    private void inlineTeamEdit(TableModelEvent e) {
        if (e.getType() != TableModelEvent.UPDATE) return;
        int row = e.getFirstRow();
        try {
            int id = (int) teamModel.getValueAt(row, 0);
            String name = (String) teamModel.getValueAt(row, 1);
            String city = (String) teamModel.getValueAt(row, 2);
            int year = parseYear(String.valueOf(teamModel.getValueAt(row, 3)));
            teamDAO.updateTeam(id, name, city, year);
        } catch (Exception ex) { showError("Error updating team."); }
    }

    private void inlinePlayerEdit(TableModelEvent e) {
        if (e.getType() != TableModelEvent.UPDATE) return;
        int row = e.getFirstRow();
        try {
            Integer id = (Integer) playerModel.getValueAt(row, 0);
            String name = String.valueOf(playerModel.getValueAt(row, 1));
            Position pos = (Position) playerModel.getValueAt(row, 2);
            int age = parseAge(String.valueOf(playerModel.getValueAt(row, 3)));
            int teamId = (Integer) playerModel.getValueAt(row, 4);
            playerDAO.updatePlayer(new Player(id, name, pos, age, teamId));
        } catch (Exception ex) { showError("Error updating player."); }
    }
 // ---------------- ADD TEAM PANEL ----------------
    private JPanel buildAddTeamPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField txtName = new JTextField(10);
        JTextField txtCity = new JTextField(8);
        JTextField txtYear = new JTextField(5);
        JButton btnAdd = new JButton("Add");

        btnAdd.addActionListener(e -> {
            try {
                String name = txtName.getText().trim();
                String city = txtCity.getText().trim();
                int year = parseYear(txtYear.getText());
                if (name.isEmpty() || city.isEmpty()) {
                    showError("Team name and city are required.");
                    return;
                }
                int teamId = teamDAO.addTeamAndReturnId(new Team(name, city, year));
                if (teamId <= 0) {
                    showError("Team already exists.");
                    loadAllDataAsync();
                    return;
                }
                showInfo("✅ Team added.");
                loadAllDataAsync();
            } catch (Exception ex) {
                showError("Error adding team: " + ex.getMessage());
            }
        });

        
        p.add(new JLabel("Name:")); p.add(txtName);
        p.add(new JLabel("City:")); p.add(txtCity);
        p.add(new JLabel("Year:")); p.add(txtYear);
        p.add(btnAdd);
        return p;
    }
 // ---------------- MESSAGE HELPERS ----------------
    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }



    // ---------------- ADD PLAYER PANEL ----------------
    private JPanel buildAddPlayerPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField txtName = new JTextField(10);
        JTextField txtAge = new JTextField(5);
        JComboBox<Position> cmbPos = new JComboBox<>(Position.values());
        JButton btnAdd = new JButton("Add");

        btnAdd.addActionListener(e -> {
            int row = tblTeams.getSelectedRow();
            if (row < 0) { showError("Select a team first."); return; }
            int teamId = (int) teamModel.getValueAt(row, 0);
            try {
                String name = txtName.getText().trim();
                int age = parseAge(txtAge.getText());
                Position pos = (Position) cmbPos.getSelectedItem();
                if (name.isEmpty()) { showError("Player name required."); return; }
                playerDAO.addPlayer(new Player(name, pos, age, teamId));
                showInfo("✅ Player added.");
                refreshPlayersTable(teamId);
                loadAllDataAsync();
            } catch (Exception ex) {
                showError("Error adding player: " + ex.getMessage());
            }
        });

        p.add(new JLabel("Name:")); p.add(txtName);
        p.add(new JLabel("Position:")); p.add(cmbPos);
        p.add(new JLabel("Age:")); p.add(txtAge);
        p.add(btnAdd);
        return p;
    }

    // ---------------- ADD COACH DIALOG ----------------
    private void addCoachDialog() {
        if (teams.isEmpty()) { showError("Add a team first."); return; }

        JTextField txtName = new JTextField();
        JTextField txtAge = new JTextField();
        JComboBox<Team> cmbTeam = new JComboBox<>(teams.toArray(new Team[0]));
        Object[] fields = {"Name:", txtName, "Age:", txtAge, "Team:", cmbTeam};

        if (JOptionPane.showConfirmDialog(this, fields, "Add Coach", JOptionPane.OK_CANCEL_OPTION)
                == JOptionPane.OK_OPTION) {
            try {
                String name = txtName.getText().trim();
                int age = parseAge(txtAge.getText());
                Team t = (Team) cmbTeam.getSelectedItem();
                coachDAO.addCoach(new Coach(name, age, t.id()));
                showInfo("✅ Coach added.");
                loadAllDataAsync();
            } catch (Exception ex) {
                showError("Error adding coach: " + ex.getMessage());
            }
        }
    }


    private void inlineCoachEdit(TableModelEvent e) {
        if (e.getType() != TableModelEvent.UPDATE) return;
        int row = e.getFirstRow();
        try {
            Integer id = (Integer) coachModel.getValueAt(row, 0);
            String name = String.valueOf(coachModel.getValueAt(row, 1));
            int age = parseAge(String.valueOf(coachModel.getValueAt(row, 2)));
            int teamId = (Integer) coachModel.getValueAt(row, 3);
            coachDAO.updateCoach(new Coach(id, name, age, teamId));
        } catch (Exception ex) { showError("Error updating coach."); }
    }

    // ---------------- DELETE BUTTONS ----------------
    private void addDeleteButton(JTable table, int col, Color color, java.util.function.IntConsumer action) {
        table.getColumnModel().getColumn(col).setCellRenderer((t, v, s, f, r, c) -> {
            JButton btn = new JButton("Delete");
            btn.setForeground(color);
            btn.setFont(btn.getFont().deriveFont(Font.BOLD, 12f));
            return btn;
        });
        table.getColumnModel().getColumn(col).setCellEditor(new DefaultCellEditor(new JCheckBox()) {
            private final JButton button = new JButton("Delete");
            private int editingRow = -1;
            {
                button.setForeground(color);
                button.setFont(button.getFont().deriveFont(Font.BOLD, 12f));
                button.addActionListener(e -> {
                    fireEditingStopped();
                    if (editingRow >= 0) action.accept(editingRow);
                });
            }
            @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
                editingRow = r; return button;
            }
            @Override public Object getCellEditorValue() { return null; }
        });
        table.getColumnModel().getColumn(col).setMaxWidth(85);
    }

    private void installPlayerPositionEditor() {
        JComboBox<Position> combo = new JComboBox<>(Position.values());
        tblPlayers.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(combo));
    }

    private void leftAlignNumberColumns(JTable table, int[] cols) {
        DefaultTableCellRenderer left = new DefaultTableCellRenderer();
        left.setHorizontalAlignment(SwingConstants.LEFT);
        for (int col : cols)
            table.getColumnModel().getColumn(col).setCellRenderer(left);
    }

    // ---------------- DELETE LOGIC ----------------
    private void deleteTeamAtRow(int row) {
        if (row < 0 || row >= teamModel.getRowCount()) return;
        int id = (int) teamModel.getValueAt(row, 0);
        if (!confirm("Delete team and all related data?")) return;
        teamDAO.deleteTeamByIdCascade(id);
        loadAllDataAsync();
    }

    private void deletePlayerAtRow(int row) {
        if (row < 0 || row >= playerModel.getRowCount()) return;
        String name = String.valueOf(playerModel.getValueAt(row, 1));
        if (!confirm("Delete player '" + name + "'?")) return;
        playerDAO.deletePlayerByName(name);
        loadAllDataAsync();
    }

    private void deleteCoachAtRow(int row) {
        if (row < 0 || row >= coachModel.getRowCount()) return;
        String name = String.valueOf(coachModel.getValueAt(row, 1));
        if (!confirm("Delete coach '" + name + "'?")) return;
        coachDAO.deleteCoachByName(name);
        loadAllDataAsync();
    }

    // ---------------- OTHER UTILITY ----------------
    private void updateDbStatus() {
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() {
                try (Connection c = DatabaseConnection.get()) {
                    return c != null && !c.isClosed();
                } catch (Exception e) { return false; }
            }
            @Override protected void done() {
                try {
                    boolean ok = get();
                    lblStatus.setText(ok ? "DB: Connected ✅" : "DB: Not connected ❌");
                    lblStatus.setForeground(ok ? new Color(0,128,0) : Color.RED);
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private JScrollPane buildLogArea() {
        txtLog = new JTextArea(4, 20);
        txtLog.setEditable(false);
        txtLog.setLineWrap(true);
        JScrollPane sp = new JScrollPane(txtLog);
        sp.setBorder(new EmptyBorder(0,8,8,8));
        return sp;
    }

    private boolean confirm(String msg) {
        return JOptionPane.showConfirmDialog(this, msg, "Confirm", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION;
    }

    private int parseAge(String s) {
        int age = Integer.parseInt(s.trim());
        if (age < 15 || age > 75) throw new NumberFormatException();
        return age;
    }

    private int parseYear(String s) {
        int y = Integer.parseInt(s.trim());
        int max = java.time.Year.now().getValue();
        if (y < 1850 || y > max) throw new NumberFormatException();
        return y;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void filterTeams() {
        String q = txtSearchTeams.getText().trim().toLowerCase();
        teamModel.setRowCount(0);
        for (Team t : teams)
            if (t.getName().toLowerCase().contains(q) || t.getCity().toLowerCase().contains(q))
                teamModel.addRow(new Object[]{t.id(), t.getName(), t.getCity(), t.getFoundedYear(), "Delete"});
    }

    private void filterPlayersForSelectedTeam() {
        int row = tblTeams.getSelectedRow();
        if (row < 0) return;
        int teamId = (int) teamModel.getValueAt(row, 0);
        refreshPlayersTable(teamId);
    }

    private void onTeamSelected(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        int row = tblTeams.getSelectedRow();
        if (row < 0) { playerModel.setRowCount(0); return; }
        int teamId = (int) teamModel.getValueAt(row, 0);
        refreshPlayersTable(teamId);
    }

    private void refreshPlayersTable(int teamId) {
        playerModel.setRowCount(0);
        List<Player> players = playersByTeam.getOrDefault(teamId, List.of());
        for (Player p : players)
            playerModel.addRow(new Object[]{p.getIdBoxed(), p.getName(), p.getPosition(), p.getAge(), p.getTeamId(), "Delete"});
    }

    private void loadAllDataAsync() {
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() {
                teams = teamDAO.getAllTeams();
                playersByTeam.clear();
                for (Team t : teamDAO.getAllTeamsWithPlayers())
                    playersByTeam.put(t.id(), t.getPlayers());
                coaches = coachDAO.getAllCoaches();
                return null;
            }
            @Override protected void done() { populateTables(); }
        }.execute();
    }

    private void populateTables() {
        teamModel.setRowCount(0);
        for (Team t : teams)
            teamModel.addRow(new Object[]{t.id(), t.getName(), t.getCity(), t.getFoundedYear(), "Delete"});
        coachModel.setRowCount(0);
        for (Coach c : coaches)
            coachModel.addRow(new Object[]{c.id(), c.getName(), c.getAge(), c.getTeamId(), "Delete"});
    }

    public static void main(String[] args) {
        DatabaseMigrator.run();
        SwingUtilities.invokeLater(() -> new FootballGUI().setVisible(true));
    }
}















