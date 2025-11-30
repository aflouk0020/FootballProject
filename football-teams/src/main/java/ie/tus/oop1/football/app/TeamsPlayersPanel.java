package ie.tus.oop1.football.app;

import ie.tus.oop1.football.dao.TeamDAO;
import ie.tus.oop1.football.dao.PlayerDAO;
import ie.tus.oop1.football.model.Team;
import ie.tus.oop1.football.model.Player;
import ie.tus.oop1.football.model.Position;
import ie.tus.oop1.football.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntConsumer;

public class TeamsPlayersPanel extends JPanel {

	
    private final TeamDAO teamDAO;
    private final PlayerDAO playerDAO;
    private final Runnable dataReloadCallback;

    private JTable tblTeams, tblPlayers;
    private DefaultTableModel teamModel, playerModel;

    private List<Team> teams = new ArrayList<>();
    private Map<Integer, List<Player>> playersByTeam = new HashMap<>();

    public TeamsPlayersPanel(TeamDAO teamDAO, PlayerDAO playerDAO, Runnable dataReloadCallback) {
        this.teamDAO = teamDAO;
        this.playerDAO = playerDAO;
        this.dataReloadCallback = dataReloadCallback;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        buildUI();
    }

    private void buildUI() {
        // ===== TEAM TABLE =====
        teamModel = new DefaultTableModel(new Object[]{"ID", "Name", "City", "Founded", "Delete"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return (c >= 1 && c <= 3) || c == 4;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                return (c == 0 || c == 3) ? Integer.class : Object.class;
            }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                if (column >= 1 && column <= 3) {
                    Object old = getValueAt(row, column);
                    if (!Objects.equals(old, aValue)) {
                        int id = (int) getValueAt(row, 0);
                        String name = String.valueOf(column == 1 ? aValue : getValueAt(row, 1)).trim();
                        String city = String.valueOf(column == 2 ? aValue : getValueAt(row, 2)).trim();
                        String yearStr = String.valueOf(column == 3 ? aValue : getValueAt(row, 3)).trim();

                        if (!confirm("Are you sure you want to update this team?\n\n" +
                                "Team ID: " + id + "\nName: " + name + "\nCity: " + city))
                            return;

                        try {
                            int year = ValidationUtil.parseYear(yearStr);
                            teamDAO.updateTeam(id, name, city, year);
                            super.setValueAt(aValue, row, column);
                            showInfo("✅ Team updated successfully.");
                            dataReloadCallback.run();
                        } catch (Exception ex) {
                            showError("Error updating team: " + ex.getMessage());
                            dataReloadCallback.run();
                        }
                    }
                } else {
                    super.setValueAt(aValue, row, column);
                }
            }
        };

        tblTeams = new JTable(teamModel);
        tblTeams.setFillsViewportHeight(true);
        tblTeams.putClientProperty("terminateEditOnFocusLost", true);
        tblTeams.setRowHeight(26);
        tblTeams.setFont(new Font("SansSerif", Font.PLAIN, 13));

        tblTeams.getColumnModel().getColumn(0).setPreferredWidth(50);
        tblTeams.getColumnModel().getColumn(1).setPreferredWidth(160);
        tblTeams.getColumnModel().getColumn(2).setPreferredWidth(130);
        tblTeams.getColumnModel().getColumn(3).setPreferredWidth(80);
        tblTeams.getColumnModel().getColumn(4).setPreferredWidth(80);
        tblTeams.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // 🔹 Enable selection -> refresh players dynamically
        tblTeams.getSelectionModel().addListSelectionListener(this::onTeamSelected);

        // Same styling as other tables
        addDeleteButton(tblTeams, 4, Color.RED.darker(), this::deleteTeamAtRow);
        leftAlignNumberColumns(tblTeams, new int[]{0, 3});

        JScrollPane teamScroll = new JScrollPane(tblTeams);
        teamScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        JPanel teamCard = makeCardPanel(teamScroll, buildAddTeamPanel(), "Teams");

        // ===== PLAYER TABLE =====
        playerModel = new DefaultTableModel(new Object[]{"ID", "Name", "Position", "Age", "Team ID", "Delete"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return (c >= 1 && c <= 3) || c == 5;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                return switch (c) {
                    case 0, 3, 4 -> Integer.class;
                    case 2 -> Position.class;
                    default -> Object.class;
                };
            }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                if (column >= 1 && column <= 3) {
                    Object old = getValueAt(row, column);
                    if (!Objects.equals(old, aValue)) {
                        int id = (int) getValueAt(row, 0);
                        String name = String.valueOf(getValueAt(row, 1)).trim();
                        Position pos = (Position) getValueAt(row, 2);
                        String ageStr = String.valueOf(getValueAt(row, 3)).trim();
                        int teamId = (int) getValueAt(row, 4);

                        if (!confirm("Are you sure you want to update this player?\n\n" +
                                "Player ID: " + id + "\nName: " + name))
                            return;

                        try {
                            int age = ValidationUtil.parseAge(ageStr);
                            playerDAO.updatePlayer(new Player(id, name, pos, age, teamId));
                            super.setValueAt(aValue, row, column);
                            showInfo("✅ Player updated successfully.");
                            dataReloadCallback.run();
                        } catch (Exception ex) {
                            showError("Error updating player: " + ex.getMessage());
                            dataReloadCallback.run();
                        }
                    }
                } else {
                    super.setValueAt(aValue, row, column);
                }
            }
        };

        tblPlayers = new JTable(playerModel);
        tblPlayers.setFillsViewportHeight(true);
        tblPlayers.putClientProperty("terminateEditOnFocusLost", true);
        tblPlayers.setRowHeight(26);
        tblPlayers.setFont(new Font("SansSerif", Font.PLAIN, 13));

        tblPlayers.getColumnModel().getColumn(0).setPreferredWidth(50);
        tblPlayers.getColumnModel().getColumn(1).setPreferredWidth(160);
        tblPlayers.getColumnModel().getColumn(2).setPreferredWidth(110);
        tblPlayers.getColumnModel().getColumn(3).setPreferredWidth(70);
        tblPlayers.getColumnModel().getColumn(4).setPreferredWidth(80);
        tblPlayers.getColumnModel().getColumn(5).setPreferredWidth(80);
        tblPlayers.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JComboBox<Position> posCombo = new JComboBox<>(Position.values());
        tblPlayers.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(posCombo));
        addDeleteButton(tblPlayers, 5, Color.RED.darker(), this::deletePlayerAtRow);
        leftAlignNumberColumns(tblPlayers, new int[]{0, 3, 4});

        JScrollPane playerScroll = new JScrollPane(tblPlayers);
        playerScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        JPanel playerCard = makeCardPanel(playerScroll, buildAddPlayerPanel(), "Players");

        // ===== SPLIT VIEW =====
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, teamCard, playerCard);
        split.setResizeWeight(0.5);
        split.setBorder(null);

        add(split, BorderLayout.CENTER);
    }

    private JPanel makeCardPanel(JScrollPane table, JPanel addPanel, String title) {
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 14));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(new Color(245, 247, 250));
        header.add(lblTitle);

        JPanel innerBox = new JPanel(new BorderLayout());
        innerBox.setBackground(Color.WHITE);
        innerBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));
        innerBox.add(table, BorderLayout.CENTER);
        innerBox.add(addPanel, BorderLayout.SOUTH);

        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(new Color(245, 247, 250));
        container.add(header, BorderLayout.NORTH);
        container.add(innerBox, BorderLayout.CENTER);

        return container;
    }

    // ------------------ ADD PANELS ------------------

    private JPanel buildAddTeamPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField txtName = new JTextField(10);
        JTextField txtCity = new JTextField(8);
        JTextField txtYear = new JTextField(5);

        JButton btnAdd = new JButton("+ Add");
        btnAdd.setFont(btnAdd.getFont().deriveFont(Font.BOLD, 13f));
        btnAdd.setBackground(new Color(0, 120, 215));
        btnAdd.setForeground(Color.BLACK);
        btnAdd.setFocusPainted(false);
        btnAdd.setPreferredSize(new Dimension(80, 35));

        btnAdd.addActionListener(e -> {
            try {
            	String name = txtName.getText().trim();
            	String city = txtCity.getText().trim();
            	String yearText = txtYear.getText().trim();

            	if (name.isEmpty() || city.isEmpty() || yearText.isEmpty()) {
            	    showError("Name, City and Year cannot be empty.");
            	    return;
            	}

            	int year = ValidationUtil.parseYear(yearText);

            	if (name.isEmpty() || city.isEmpty()) {
                    showError("Team name and city are required.");
                    return;
                }
                teamDAO.addTeamAndReturnId(new Team(name, city, year));
                showInfo("✅ Team added successfully.");
                dataReloadCallback.run();
            } catch (Exception ex) {
                showError("Error adding team: " + ex.getMessage());
            }
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

        JButton btnAdd = new JButton("+ Add");
        btnAdd.setFont(btnAdd.getFont().deriveFont(Font.BOLD, 13f));
        btnAdd.setBackground(new Color(0, 120, 215));
        btnAdd.setForeground(Color.BLACK);
        btnAdd.setFocusPainted(false);
        btnAdd.setPreferredSize(new Dimension(80, 35));

        btnAdd.addActionListener(e -> {
            int row = tblTeams.getSelectedRow();
            if (row < 0) {
                showError("Select a team first.");
                return;
            }
            int teamId = (int) teamModel.getValueAt(row, 0);
            try {
                String name = txtName.getText().trim();
                int age = ValidationUtil.parseAge(txtAge.getText());
                Position pos = (Position) cmbPos.getSelectedItem();
                playerDAO.addPlayer(new Player(name, pos, age, teamId));
                showInfo("✅ Player added successfully.");
                dataReloadCallback.run();
            } catch (Exception ex) {
                showError("Error adding player: " + ex.getMessage());
            }
        });

        p.add(new JLabel("Name:"));
        p.add(txtName);
        p.add(new JLabel("Position:"));
        p.add(cmbPos);
        p.add(new JLabel("Age:"));
        p.add(txtAge);
        p.add(btnAdd);
        return p;
    }

    // ------------------ DELETE LOGIC ------------------

    private void deleteTeamAtRow(int row) {
        int id = (int) teamModel.getValueAt(row, 0);
        if (!confirm("Delete team and all related data?")) return;
        teamDAO.deleteTeamByIdCascade(id);
        dataReloadCallback.run();
    }

    private void deletePlayerAtRow(int row) {
        String name = String.valueOf(playerModel.getValueAt(row, 1));
        if (!confirm("Delete player '" + name + "'?")) return;
        playerDAO.deletePlayerByName(name);
        dataReloadCallback.run();
    }

    // ------------------ HELPERS ------------------

    private void addDeleteButton(JTable table, int col, Color color, IntConsumer action) {
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
                button.addActionListener(e -> {
                    fireEditingStopped();
                    if (editingRow >= 0) action.accept(editingRow);
                });
            }

            @Override
            public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
                editingRow = r;
                return button;
            }

            @Override
            public Object getCellEditorValue() {
                return null;
            }
        });
        table.getColumnModel().getColumn(col).setMaxWidth(85);
    }

    private void leftAlignNumberColumns(JTable table, int[] cols) {
        DefaultTableCellRenderer left = new DefaultTableCellRenderer();
        left.setHorizontalAlignment(SwingConstants.LEFT);
        for (int col : cols) {
            table.getColumnModel().getColumn(col).setCellRenderer(left);
        }
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

    private void onTeamSelected(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        int row = tblTeams.getSelectedRow();
        if (row < 0) {
            playerModel.setRowCount(0);
            return;
        }
        int teamId = (int) teamModel.getValueAt(row, 0);
        refreshPlayersTable(teamId);
    }

    private void refreshPlayersTable(int teamId) {
        playerModel.setRowCount(0);
        List<Player> players = playersByTeam.getOrDefault(teamId, List.of());
        for (Player p : players) {
            playerModel.addRow(new Object[]{
                    p.getIdBoxed(),
                    p.getName(),
                    p.getPosition(),
                    p.getAge(),
                    p.getTeamId(),
                    "Delete"
            });
        }
    }

    // ------------------ DATA FROM PARENT ------------------

    public void refreshData(List<Team> teams, Map<Integer, List<Player>> playersByTeam) {
        this.teams.clear();
        this.teams.addAll(teams);

        this.playersByTeam.clear();
        this.playersByTeam.putAll(playersByTeam);

        populateTables();
    }

    private void populateTables() {
        teamModel.setRowCount(0);
        for (Team t : teams) {
            teamModel.addRow(new Object[]{
                    t.id(),
                    t.getName(),
                    t.getCity(),
                    t.getFoundedYear(),
                    "Delete"
            });
        }
        playerModel.setRowCount(0); // Player table will fill when a team is selected
    }

    public void filterTeams(String query) {
        teamModel.setRowCount(0);
        for (Team t : teams) {
            if (t.getName().toLowerCase().contains(query) ||
                    t.getCity().toLowerCase().contains(query)) {
                teamModel.addRow(new Object[]{
                        t.id(),
                        t.getName(),
                        t.getCity(),
                        t.getFoundedYear(),
                        "Delete"
                });
            }
        }
    }
}
