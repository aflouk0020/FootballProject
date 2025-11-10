
package ie.tus.oop1.football.app;

import ie.tus.oop1.football.dao.*;
import ie.tus.oop1.football.model.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import e.tus.oop1.football.service.DatabaseMigrator;

import java.awt.*;
import java.sql.Connection;
import java.util.*;
import java.util.List;

public class FootballGUI extends JFrame {

    // === DAOs ===
    private final TeamDAO teamDAO = new TeamDAO();
    private final PlayerDAO playerDAO = new PlayerDAO();
    private final CoachDAO coachDAO = new CoachDAO();

    // === UI Components ===
    private JLabel lblStatus, lblStats;
    private JTable tblTeams, tblPlayers, tblCoaches;
    private DefaultTableModel teamModel, playerModel, coachModel;
    private JTextField txtSearchTeams;      // global team search
    private JTextField txtSearchPlayers;    // right-panel player search (per team)
    private JTextArea txtLog;

    // === Data Caches ===
    private List<Team> teams = new ArrayList<>();
    private Map<Integer, List<Player>> playersByTeam = new HashMap<>();
    private List<Coach> coaches = new ArrayList<>();

    // === Constructor ===
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

    // ---------------- TOP BAR ----------------
    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout(10, 0));
        top.setBorder(new EmptyBorder(8, 8, 8, 8));

        lblStatus = new JLabel("DB: Checking…");
        lblStatus.setFont(lblStatus.getFont().deriveFont(Font.BOLD, 13f));
        lblStats = new JLabel("");

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setFont(btnRefresh.getFont().deriveFont(Font.BOLD, 13f));
        btnRefresh.setForeground(new Color(30, 90, 180));
        btnRefresh.addActionListener(e -> loadAllDataAsync());

        txtSearchTeams = new JTextField(22);
        txtSearchTeams.putClientProperty("JTextField.placeholderText", "Search teams by name/city…");
        txtSearchTeams.addActionListener(e -> filterTeams());

        JPanel right = new JPanel();
        right.add(txtSearchTeams);
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

    // ---------------- TEAMS / PLAYERS PANEL ----------------
    private JPanel buildTeamsPlayersPanel() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.45);

        // === Teams ===
        teamModel = new DefaultTableModel(new Object[]{"ID", "Name", "City", "Founded", "Edit", "Delete"}, 0) {
            public boolean isCellEditable(int r, int c) { return c >= 4; }
        };
        tblTeams = new JTable(teamModel);
        tblTeams.getSelectionModel().addListSelectionListener(this::onTeamSelected);
        addButtonColumns(tblTeams, new int[]{4, 5}, new String[]{"Edit", "Delete"}, new Color[]{new Color(0,128,0), Color.RED.darker()},
                (row, col) -> { if (col == 4) editTeamAtRow(row); else deleteTeamAtRow(row); });

        JPanel leftNorth = new JPanel(new BorderLayout(5, 5));
        leftNorth.add(new JLabel("Teams", SwingConstants.LEFT), BorderLayout.WEST);

        JPanel left = new JPanel(new BorderLayout(5, 5));
        left.setBorder(new EmptyBorder(6, 8, 6, 8));
        left.add(leftNorth, BorderLayout.NORTH);
        left.add(new JScrollPane(tblTeams), BorderLayout.CENTER);
        left.add(buildAddTeamPanel(), BorderLayout.SOUTH);

        // === Players ===
        playerModel = new DefaultTableModel(new Object[]{"ID", "Name", "Position", "Age", "TeamId", "Edit", "Delete"}, 0) {
            public boolean isCellEditable(int r, int c) { return c >= 5; }
        };
        tblPlayers = new JTable(playerModel);
        addButtonColumns(tblPlayers, new int[]{5, 6}, new String[]{"Edit", "Delete"}, new Color[]{new Color(0,128,0), Color.RED.darker()},
                (row, col) -> { if (col == 5) editPlayerAtRow(row); else deletePlayerAtRow(row); });

        JPanel rightNorth = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rightNorth.add(new JLabel("Players (by team)"));
        txtSearchPlayers = new JTextField(18);
        txtSearchPlayers.putClientProperty("JTextField.placeholderText", "Search players in this team…");
        txtSearchPlayers.addActionListener(e -> filterPlayersForSelectedTeam());
        rightNorth.add(Box.createHorizontalStrut(15));
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

    // ---------------- COACHES PANEL ----------------
    private JPanel buildCoachesPanel() {
        coachModel = new DefaultTableModel(new Object[]{"ID", "Name", "Age", "TeamId", "Edit", "Delete"}, 0) {
            public boolean isCellEditable(int r, int c) { return c >= 4; }
        };
        tblCoaches = new JTable(coachModel);
        addButtonColumns(tblCoaches, new int[]{4, 5}, new String[]{"Edit", "Delete"}, new Color[]{new Color(0,128,0), Color.RED.darker()},
                (row, col) -> { if (col == 4) editCoachAtRow(row); else deleteCoachAtRow(row); });

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
                // validation
                String name = txtName.getText().trim();
                String city = txtCity.getText().trim();
                int year = parseYear(txtYear.getText());

                if (name.isEmpty() || city.isEmpty()) {
                    showError("Team name and city are required.");
                    return;
                }
                int teamId = teamDAO.addTeamAndReturnId(new Team(name, city, year));
                if (teamId <= 0) { showInfo("ℹ️ Team already exists."); loadAllDataAsync(); return; }

                // Immediately ask for a coach (mandatory)
                boolean coachAdded = promptCoachForTeam(teamId, name);
                if (!coachAdded) {
                    showInfo("⚠️ Coach creation cancelled. The team will be removed.");
                    teamDAO.deleteTeamByIdCascade(teamId);
                } else {
                    showInfo("✅ Team and coach added.");
                }
                loadAllDataAsync();

            } catch (NumberFormatException nfe) {
                showError("Founded year must be between 1850 and " + java.time.Year.now().getValue() + ".");
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

    private JPanel buildAddPlayerPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField txtName = new JTextField(10);
        JTextField txtAge = new JTextField(5);
        JComboBox<Position> cmbPos = new JComboBox<>(Position.values());
        JButton btnAdd = new JButton("Add");

        btnAdd.addActionListener(e -> {
            int row = tblTeams.getSelectedRow();
            if (row < 0) { showInfo("Select a team first."); return; }
            int teamId = (int) teamModel.getValueAt(row, 0);

            try {
                String name = txtName.getText().trim();
                int age = parseAge(txtAge.getText());
                Position pos = (Position) cmbPos.getSelectedItem();

                if (name.isEmpty()) { showError("Player name is required."); return; }

                boolean ok = playerDAO.addPlayer(new Player(name, pos, age, teamId));
                showInfo(ok ? "✅ Player added." : "ℹ️ Player already exists.");
                refreshPlayersTable(teamId);
                loadAllDataAsync();
            } catch (NumberFormatException nfe) {
                showError("Age must be between 15 and 60.");
            } catch (Exception ex) {
                showError("Error: " + ex.getMessage());
            }
        });

        p.add(new JLabel("Name:")); p.add(txtName);
        p.add(new JLabel("Pos:"));  p.add(cmbPos);
        p.add(new JLabel("Age:"));  p.add(txtAge);
        p.add(btnAdd);
        return p;
    }

    private boolean promptCoachForTeam(int teamId, String teamName) {
        JTextField txtCoach = new JTextField();
        JTextField txtAge = new JTextField();
        Object[] fields = {"New coach for " + teamName + ":", "Name:", txtCoach, "Age:", txtAge};

        int res = JOptionPane.showConfirmDialog(this, fields, "Add Coach (Required)", JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) return false;

        try {
            String name = txtCoach.getText().trim();
            int age = parseAge(txtAge.getText());
            if (name.isEmpty()) { showError("Coach name is required."); return false; }
            boolean ok = coachDAO.addCoach(new Coach(name, age, teamId));
            if (!ok) showInfo("ℹ️ Coach already exists.");
            return true;
        } catch (NumberFormatException nfe) {
            showError("Age must be between 18 and 75.");
            return false;
        } catch (Exception ex) {
            showError("Error creating coach: " + ex.getMessage());
            return false;
        }
    }

    private void addCoachDialog() {
        if (teams.isEmpty()) { showInfo("Add a team first."); return; }
        JTextField txtName = new JTextField();
        JTextField txtAge = new JTextField();
        JComboBox<Team> cmbTeam = new JComboBox<>(teams.toArray(new Team[0]));
        Object[] fields = {"Name:", txtName, "Age:", txtAge, "Team:", cmbTeam};

        int res = JOptionPane.showConfirmDialog(this, fields, "Add Coach", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                Team team = (Team) cmbTeam.getSelectedItem();
                String name = txtName.getText().trim();
                int age = parseAge(txtAge.getText());
                if (name.isEmpty()) { showError("Coach name is required."); return; }
                boolean ok = coachDAO.addCoach(new Coach(name, age, team.id()));
                showInfo(ok ? "✅ Coach added." : "ℹ️ Coach exists.");
                loadAllDataAsync();
            } catch (NumberFormatException nfe) {
                showError("Age must be between 18 and 75.");
            } catch (Exception ex) {
                showError("Error: " + ex.getMessage());
            }
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
        if (row < 0) { playerModel.setRowCount(0); return; }
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
            teamModel.addRow(new Object[]{t.id(), t.getName(), t.getCity(), t.getFoundedYear(), "Edit", "Delete"});

        coachModel.setRowCount(0);
        for (Coach c : coaches)
            coachModel.addRow(new Object[]{c.id(), c.getName(), c.getAge(), c.id(), "Edit", "Delete"});

        // refresh right table if a team is selected
        int row = tblTeams.getSelectedRow();
        if (row >= 0) refreshPlayersTable((int) teamModel.getValueAt(row, 0));
    }

    private void refreshPlayersTable(int teamId) {
        playerModel.setRowCount(0);
        String q = txtSearchPlayers == null ? "" : txtSearchPlayers.getText().trim().toLowerCase();
        List<Player> players = playersByTeam.getOrDefault(teamId, List.of());
        for (Player p : players) {
            if (q.isEmpty() || p.getName().toLowerCase().contains(q) || p.getPosition().name().toLowerCase().contains(q)) {
                playerModel.addRow(new Object[]{p.getIdBoxed(), p.getName(), p.getPosition(), p.getAge(), p.getTeamId(), "Edit", "Delete"});
            }
        }
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
                    lblStatus.setForeground(ok ? new Color(0, 128, 0) : Color.RED);
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    // ---------------- SEARCH ----------------
    private void filterTeams() {
        String q = txtSearchTeams.getText().trim().toLowerCase();
        teamModel.setRowCount(0);
        for (Team t : teams)
            if (t.getName().toLowerCase().contains(q) || t.getCity().toLowerCase().contains(q))
                teamModel.addRow(new Object[]{t.id(), t.getName(), t.getCity(), t.getFoundedYear(), "Edit", "Delete"});
    }

    private void filterPlayersForSelectedTeam() {
        int row = tblTeams.getSelectedRow();
        if (row < 0) return;
        int teamId = (int) teamModel.getValueAt(row, 0);
        refreshPlayersTable(teamId);
    }

    // ---------------- VALIDATION HELPERS ----------------
    private int parseAge(String s) {
        int age = Integer.parseInt(s.trim());
        if (age < 15 || age > 75) throw new NumberFormatException("bad age");
        return age;
    }
    private int parseYear(String s) {
        int y = Integer.parseInt(s.trim());
        int max = java.time.Year.now().getValue();
        if (y < 1850 || y > max) throw new NumberFormatException("bad year");
        return y;
    }
    private void showInfo(String msg)  { JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);  toast(msg); }
    private void showError(String msg) { JOptionPane.showMessageDialog(this, msg, "Problem", JOptionPane.ERROR_MESSAGE);       toast("❌ " + msg); }
    private boolean confirm(String msg) {
        return JOptionPane.showConfirmDialog(this, msg, "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
    private void toast(String msg) {
        if (txtLog == null) return;
        txtLog.append(msg + "\n");
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
    }

    // ---------------- DELETE & EDIT ----------------
    private void deleteTeamAtRow(int row) {
        if (row < 0 || row >= teamModel.getRowCount()) return;
        int id = (int) teamModel.getValueAt(row, 0);
        String name = String.valueOf(teamModel.getValueAt(row, 1));
        if (!confirm("Delete team '" + name + "'?\n(Players & coaches will also be deleted)")) return;

        teamDAO.deleteTeamByIdCascade(id);
        teamModel.removeRow(row);
        tblTeams.clearSelection();
        showInfo("Deleted team: " + name);
        new Timer(200, e -> loadAllDataAsync()).start();
    }

    private void deletePlayerAtRow(int row) {
        if (row < 0 || row >= playerModel.getRowCount()) return;
        String name = String.valueOf(playerModel.getValueAt(row, 1));
        if (!confirm("Delete player '" + name + "'?")) return;

        playerDAO.deletePlayerByName(name);
        playerModel.removeRow(row);
        tblPlayers.clearSelection();
        showInfo("Deleted player: " + name);
        new Timer(200, e -> loadAllDataAsync()).start();
    }

    private void deleteCoachAtRow(int row) {
        if (row < 0 || row >= coachModel.getRowCount()) return;
        String name = String.valueOf(coachModel.getValueAt(row, 1));
        if (!confirm("Delete coach '" + name + "'?")) return;

        coachDAO.deleteCoachByName(name);
        coachModel.removeRow(row);
        tblCoaches.clearSelection();
        showInfo("Deleted coach: " + name);
        new Timer(200, e -> loadAllDataAsync()).start();
    }

    private void editTeamAtRow(int row) {
        int id = (int) teamModel.getValueAt(row, 0);
        String name = String.valueOf(teamModel.getValueAt(row, 1));
        String city = String.valueOf(teamModel.getValueAt(row, 2));
        String yearStr = String.valueOf(teamModel.getValueAt(row, 3));

        JTextField txtName = new JTextField(name);
        JTextField txtCity = new JTextField(city);
        JTextField txtYear = new JTextField(yearStr);
        Object[] fields = {"Name:", txtName, "City:", txtCity, "Founded:", txtYear};

        if (JOptionPane.showConfirmDialog(this, fields, "Edit Team", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                String newName = txtName.getText().trim();
                String newCity = txtCity.getText().trim();
                int newYear   = parseYear(txtYear.getText());
                if (newName.isEmpty() || newCity.isEmpty()) { showError("Name and city are required."); return; }
                teamDAO.updateTeam(id, newName, newCity, newYear);
                loadAllDataAsync();
                showInfo("Team updated.");
            } catch (NumberFormatException nfe) {
                showError("Founded year is invalid.");
            } catch (Exception ex) {
                showError("Update failed: " + ex.getMessage());
            }
        }
    }

    private void editPlayerAtRow(int row) {
        Integer id = (Integer) playerModel.getValueAt(row, 0);
        String name = String.valueOf(playerModel.getValueAt(row, 1));
        Position pos = (Position) playerModel.getValueAt(row, 2);
        String ageStr = String.valueOf(playerModel.getValueAt(row, 3));
        Integer teamId = (Integer) playerModel.getValueAt(row, 4);

        JTextField txtName = new JTextField(name);
        JComboBox<Position> cmbPos = new JComboBox<>(Position.values());
        cmbPos.setSelectedItem(pos);
        JTextField txtAge = new JTextField(ageStr);
        Object[] fields = {"Name:", txtName, "Position:", cmbPos, "Age:", txtAge};

        if (JOptionPane.showConfirmDialog(this, fields, "Edit Player", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                String newName = txtName.getText().trim();
                int newAge     = parseAge(txtAge.getText());
                Position newPos = (Position) cmbPos.getSelectedItem();
                if (newName.isEmpty()) { showError("Player name is required."); return; }
                playerDAO.updatePlayer(new Player(id, newName, newPos, newAge, teamId));
                loadAllDataAsync();
                showInfo("Player updated.");
            } catch (NumberFormatException nfe) {
                showError("Age is invalid.");
            } catch (Exception ex) {
                showError("Update failed: " + ex.getMessage());
            }
        }
    }

    private void editCoachAtRow(int row) {
        Integer id = (Integer) coachModel.getValueAt(row, 0);
        String name = String.valueOf(coachModel.getValueAt(row, 1));
        String ageStr = String.valueOf(coachModel.getValueAt(row, 2));
        Integer teamId = (Integer) coachModel.getValueAt(row, 3);

        JTextField txtName = new JTextField(name);
        JTextField txtAge  = new JTextField(ageStr);
        Object[] fields = {"Name:", txtName, "Age:", txtAge};

        if (JOptionPane.showConfirmDialog(this, fields, "Edit Coach", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                String newName = txtName.getText().trim();
                int newAge     = parseAge(txtAge.getText());
                if (newName.isEmpty()) { showError("Coach name is required."); return; }
                coachDAO.updateCoach(new Coach(id, newName, newAge, teamId));
                loadAllDataAsync();
                showInfo("Coach updated.");
            } catch (NumberFormatException nfe) {
                showError("Age is invalid.");
            } catch (Exception ex) {
                showError("Update failed: " + ex.getMessage());
            }
        }
    }

    // ---------------- BUTTON COLUMNS ----------------
    @FunctionalInterface private interface RowColAction { void run(int row, int col); }

    private void addButtonColumns(JTable table, int[] cols, String[] labels, Color[] colors, RowColAction action) {
        for (int i = 0; i < cols.length; i++) {
            int colIndex = cols[i];
            String label = labels[i];
            Color color  = colors[i];

            table.getColumnModel().getColumn(colIndex).setCellRenderer(new ButtonCellRenderer(label, color));
            table.getColumnModel().getColumn(colIndex).setCellEditor(new ButtonCellEditor(new JCheckBox(), label, color, action));
            table.getColumnModel().getColumn(colIndex).setMaxWidth(85);
        }
    }

    private static class ButtonCellRenderer extends JButton implements TableCellRenderer {
        ButtonCellRenderer(String label, Color color) {
            setOpaque(true); setText(label); setForeground(color); setFont(getFont().deriveFont(Font.BOLD, 12f));
        }
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) { return this; }
    }

    private class ButtonCellEditor extends DefaultCellEditor {
        private final JButton button;
        private int currentRow = -1, currentCol = -1;
        private final RowColAction action;

        ButtonCellEditor(JCheckBox dummy, String label, Color color, RowColAction action) {
            super(dummy);
            this.action = action;
            button = new JButton(label);
            button.setForeground(color);
            button.setFont(button.getFont().deriveFont(Font.BOLD, 12f));
            button.addActionListener(e -> { fireEditingStopped(); SwingUtilities.invokeLater(() -> action.run(currentRow, currentCol)); });
        }
        public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            currentRow = r; currentCol = c; return button;
        }
        public Object getCellEditorValue() { return null; }
    }

    // ---------------- MAIN ----------------
    public static void main(String[] args) {
        // ensure DB schema exists (auto-migration)
        DatabaseMigrator.run();
        SwingUtilities.invokeLater(() -> new FootballGUI().setVisible(true));
    }
}
