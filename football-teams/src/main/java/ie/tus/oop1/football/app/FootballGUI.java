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
import ie.tus.oop1.football.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
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

    private JLabel lblStatus;
    private JTable tblTeams, tblPlayers, tblCoaches;
    private DefaultTableModel teamModel, playerModel, coachModel;
    private JTextField txtSearchTeams, txtSearchPlayers;

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

        updateDbStatus();
        loadAllDataAsync();
    }

    // ------------------ UI BUILDERS ------------------
    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout(10, 0));
        top.setBorder(new EmptyBorder(8, 8, 8, 8));

        lblStatus = new JLabel("DB: Checking…");
        lblStatus.setFont(lblStatus.getFont().deriveFont(Font.BOLD, 13f));

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> loadAllDataAsync());

        txtSearchTeams = new JTextField(22);
        txtSearchTeams.putClientProperty("JTextField.placeholderText", "Search teams…");
        // Inline filter (no extra method)
        txtSearchTeams.addActionListener(e -> {
            String q = txtSearchTeams.getText().trim().toLowerCase();
            teamModel.setRowCount(0);
            for (Team t : teams) {
                if (t.getName().toLowerCase().contains(q) || t.getCity().toLowerCase().contains(q)) {
                    teamModel.addRow(new Object[]{t.id(), t.getName(), t.getCity(), t.getFoundedYear(), "Delete"});
                }
            }
        });

        JPanel right = new JPanel();
        right.add(txtSearchTeams);
        right.add(btnRefresh);

        top.add(lblStatus, BorderLayout.WEST);
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

        // ===== TEAMS TABLE =====
        teamModel = new DefaultTableModel(new Object[]{"ID", "Name", "City", "Founded", "Delete"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return (c >= 1 && c <= 3) || c == 4; }
            @Override public Class<?> getColumnClass(int c) { return (c == 0 || c == 3) ? Integer.class : Object.class; }

            @Override public void setValueAt(Object aValue, int row, int column) {
                if (column >= 1 && column <= 3) {
                    Object old = getValueAt(row, column);
                    if (!Objects.equals(old, aValue)) {
                        int id = (int) getValueAt(row, 0);
                        String name = String.valueOf(column == 1 ? aValue : getValueAt(row, 1)).trim();
                        String city = String.valueOf(column == 2 ? aValue : getValueAt(row, 2)).trim();
                        String yearStr = String.valueOf(column == 3 ? aValue : getValueAt(row, 3)).trim();

                        int choice = JOptionPane.showConfirmDialog(
                                FootballGUI.this,
                                "Are you sure you want to update this team?\n\n" +
                                        "Team ID: " + id + "\nName: " + name + "\nCity: " + city,
                                "Confirm Update",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE
                        );
                        if (choice != JOptionPane.YES_OPTION) return; // cancel -> auto revert

                        try {
                        	int year = ValidationUtil.parseYear(yearStr);
                            super.setValueAt(aValue, row, column); // commit to model
                            teamDAO.updateTeam(id,
                                    String.valueOf(getValueAt(row, 1)).trim(),
                                    String.valueOf(getValueAt(row, 2)).trim(),
                                    Integer.parseInt(String.valueOf(getValueAt(row, 3)).trim()));
                            showInfo("✅ Team updated successfully.");
                        } catch (Exception ex) {
                            showError("Error updating team: " + ex.getMessage());
                            loadAllDataAsync(); // reload from DB on failure
                        }
                        return;
                    }
                }
                super.setValueAt(aValue, row, column);
            }
        };

        tblTeams = new JTable(teamModel);
        tblTeams.setFillsViewportHeight(true);
        tblTeams.putClientProperty("terminateEditOnFocusLost", true);
        tblTeams.getSelectionModel().addListSelectionListener(this::onTeamSelected);
        addDeleteButton(tblTeams, 4, Color.RED.darker(), this::deleteTeamAtRow);
        centerNumberColumns(tblTeams, new int[]{0, 3});

        JPanel left = new JPanel(new BorderLayout(5, 5));
        left.setBorder(new EmptyBorder(6, 8, 6, 8));
        left.add(new JLabel("Teams"), BorderLayout.NORTH);
        left.add(new JScrollPane(tblTeams), BorderLayout.CENTER);
        left.add(buildAddTeamPanel(), BorderLayout.SOUTH);

        // ===== PLAYERS TABLE =====
        playerModel = new DefaultTableModel(new Object[]{"ID", "Name", "Position", "Age", "TeamId", "Delete"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return (c >= 1 && c <= 3) || c == 5; }
            @Override public Class<?> getColumnClass(int c) {
                return switch (c) { case 0, 3, 4 -> Integer.class; case 2 -> Position.class; default -> Object.class; };
            }

            @Override public void setValueAt(Object aValue, int row, int column) {
                if (column >= 1 && column <= 3) {
                    Object old = getValueAt(row, column);
                    if (!Objects.equals(old, aValue)) {
                        int id = (int) getValueAt(row, 0);
                        String name = String.valueOf(getValueAt(row, 1)).trim();
                        Position pos = (Position) (column == 2 ? aValue : getValueAt(row, 2));
                        String ageStr = String.valueOf(column == 3 ? aValue : getValueAt(row, 3)).trim();
                        int teamId = (int) getValueAt(row, 4);

                        int choice = JOptionPane.showConfirmDialog(
                                FootballGUI.this,
                                "Are you sure you want to update this player?\n\n" +
                                        "Player ID: " + id + "\nName: " + name + "\nPosition: " + pos,
                                "Confirm Update",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE
                        );
                        if (choice != JOptionPane.YES_OPTION) return;

                        try {
                        	int age = ValidationUtil.parseAge(ageStr);
                            super.setValueAt(aValue, row, column);
                            playerDAO.updatePlayer(new Player(id,
                                    String.valueOf(getValueAt(row, 1)).trim(),
                                    (Position) getValueAt(row, 2),
                                    Integer.parseInt(String.valueOf(getValueAt(row, 3)).trim()),
                                    teamId));
                            showInfo("✅ Player updated successfully.");
                        } catch (Exception ex) {
                            showError("Error updating player: " + ex.getMessage());
                            loadAllDataAsync();
                        }
                        return;
                    }
                }
                super.setValueAt(aValue, row, column);
            }
        };

        tblPlayers = new JTable(playerModel);
        tblPlayers.setFillsViewportHeight(true);
        tblPlayers.putClientProperty("terminateEditOnFocusLost", true);
        addDeleteButton(tblPlayers, 5, Color.RED.darker(), this::deletePlayerAtRow);
        centerNumberColumns(tblPlayers, new int[]{0, 3, 4});
        // Inline: install position combo editor (no extra method)
        JComboBox<Position> posCombo = new JComboBox<>(Position.values());
        tblPlayers.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(posCombo));

        JPanel right = new JPanel(new BorderLayout(5, 5));
        right.setBorder(new EmptyBorder(6, 8, 6, 8));
        right.add(new JScrollPane(tblPlayers), BorderLayout.CENTER);
        right.add(buildAddPlayerPanel(), BorderLayout.SOUTH);

        split.setLeftComponent(left);
        split.setRightComponent(right);
        return new JPanel(new BorderLayout()) {{ add(split, BorderLayout.CENTER); }};
    }

    private JPanel buildCoachesPanel() {
        coachModel = new DefaultTableModel(new Object[]{"ID", "Name", "Age", "TeamId", "Delete"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return (c == 1 || c == 2 || c == 3) || c == 4; }
            @Override public Class<?> getColumnClass(int c) { return (c == 0 || c == 2 || c == 3) ? Integer.class : Object.class; }

            @Override public void setValueAt(Object aValue, int row, int column) {
                if (column == 1 || column == 2 || column == 3) {
                    Object old = getValueAt(row, column);
                    if (!Objects.equals(old, aValue)) {
                        int id = (int) getValueAt(row, 0);
                        String name = String.valueOf(column == 1 ? aValue : getValueAt(row, 1)).trim();
                        String ageStr = String.valueOf(column == 2 ? aValue : getValueAt(row, 2)).trim();
                        int teamId = (int) (column == 3 ? aValue : getValueAt(row, 3));

                        int choice = JOptionPane.showConfirmDialog(
                                FootballGUI.this,
                                "Are you sure you want to update this coach?\n\n" +
                                        "Coach ID: " + id + "\nName: " + name,
                                "Confirm Update",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE
                        );
                        if (choice != JOptionPane.YES_OPTION) return;

                        try {
                            int age = ValidationUtil.parseAge(ageStr);
                            super.setValueAt(aValue, row, column);
                            coachDAO.updateCoach(new Coach(id,
                                    String.valueOf(getValueAt(row, 1)).trim(),
                                    Integer.parseInt(String.valueOf(getValueAt(row, 2)).trim()),
                                    (Integer) getValueAt(row, 3)));
                            showInfo("✅ Coach updated successfully.");
                        } catch (Exception ex) {
                            showError("Error updating coach: " + ex.getMessage());
                            loadAllDataAsync();
                        }
                        return;
                    }
                }
                super.setValueAt(aValue, row, column);
            }
        };

        tblCoaches = new JTable(coachModel);
        tblCoaches.setFillsViewportHeight(true);
        tblCoaches.putClientProperty("terminateEditOnFocusLost", true);
        addDeleteButton(tblCoaches, 4, Color.RED.darker(), this::deleteCoachAtRow);
        centerNumberColumns(tblCoaches, new int[]{0, 2, 3});

        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(new EmptyBorder(6, 8, 6, 8));
        p.add(new JScrollPane(tblCoaches), BorderLayout.CENTER);
        JButton btnAddCoach = new JButton("Add Coach");
        btnAddCoach.addActionListener(e -> addCoachDialog());
        p.add(btnAddCoach, BorderLayout.SOUTH);
        return p;
    }

    // ------------------ ADD PANELS ------------------
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
                int year = ValidationUtil.parseYear(txtYear.getText());
                if (name.isEmpty() || city.isEmpty()) { showError("Team name and city are required."); return; }
                teamDAO.addTeamAndReturnId(new Team(name, city, year));
                showInfo("✅ Team added successfully.");
                loadAllDataAsync();
            } catch (Exception ex) { showError("Error adding team: " + ex.getMessage()); }
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
            if (row < 0) { showError("Select a team first."); return; }
            int teamId = (int) teamModel.getValueAt(row, 0);
            try {
                String name = txtName.getText().trim();
                int age = ValidationUtil.parseAge(txtAge.getText());
                Position pos = (Position) cmbPos.getSelectedItem();
                playerDAO.addPlayer(new Player(name, pos, age, teamId));
                showInfo("✅ Player added successfully.");
                loadAllDataAsync();
            } catch (Exception ex) { showError("Error adding player: " + ex.getMessage()); }
        });
        p.add(new JLabel("Name:")); p.add(txtName);
        p.add(new JLabel("Position:")); p.add(cmbPos);
        p.add(new JLabel("Age:")); p.add(txtAge);
        p.add(btnAdd);
        return p;
    }

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
                int age = ValidationUtil.parseAge(txtAge.getText());
                Team t = (Team) cmbTeam.getSelectedItem();
                coachDAO.addCoach(new Coach(name, age, t.id()));
                showInfo("✅ Coach added successfully.");
                loadAllDataAsync();
            } catch (Exception ex) { showError("Error adding coach: " + ex.getMessage()); }
        }
    }

    // ------------------ DELETE LOGIC ------------------
    private void deleteTeamAtRow(int row) {
        int id = (int) teamModel.getValueAt(row, 0);
        if (!confirm("Delete team and all related data?")) return;
        teamDAO.deleteTeamByIdCascade(id);
        loadAllDataAsync();
    }

    private void deletePlayerAtRow(int row) {
        String name = String.valueOf(playerModel.getValueAt(row, 1));
        if (!confirm("Delete player '" + name + "'?")) return;
        playerDAO.deletePlayerByName(name);
        loadAllDataAsync();
    }

    private void deleteCoachAtRow(int row) {
        String name = String.valueOf(coachModel.getValueAt(row, 1));
        if (!confirm("Delete coach '" + name + "'?")) return;
        coachDAO.deleteCoachByName(name);
        loadAllDataAsync();
    }

    // ------------------ HELPERS ------------------
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
            { button.setForeground(color); button.addActionListener(e -> { fireEditingStopped(); if (editingRow >= 0) action.accept(editingRow); }); }
            @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) { editingRow = r; return button; }
            @Override public Object getCellEditorValue() { return null; }
        });
        table.getColumnModel().getColumn(col).setMaxWidth(85);
    }

    private void centerNumberColumns(JTable table, int[] cols) {
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int col : cols) table.getColumnModel().getColumn(col).setCellRenderer(center);
    }

    private boolean confirm(String msg) {
        return JOptionPane.showConfirmDialog(this, msg, "Confirm", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION;
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }





    private void updateDbStatus() {
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() {
                try (Connection c = DatabaseConnection.get()) { return c != null && !c.isClosed(); }
                catch (Exception e) { return false; }
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

    private void loadAllDataAsync() {
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() {
                teams = teamDAO.getAllTeams();
                playersByTeam.clear();
                for (Team t : teamDAO.getAllTeamsWithPlayers()) playersByTeam.put(t.id(), t.getPlayers());
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

    public static void main(String[] args) {
        DatabaseMigrator.run();
        SwingUtilities.invokeLater(() -> new FootballGUI().setVisible(true));
    }
}