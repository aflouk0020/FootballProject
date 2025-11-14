package ie.tus.oop1.football.app;

import ie.tus.oop1.football.dao.CoachDAO;
import ie.tus.oop1.football.dao.TeamDAO;
import ie.tus.oop1.football.dao.DatabaseConnection;
import ie.tus.oop1.football.model.Coach;
import ie.tus.oop1.football.model.Team;
import ie.tus.oop1.football.util.DataAccessRuntimeException;
import ie.tus.oop1.football.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.IntConsumer;

public class CoachesPanel extends JPanel {

    private final TeamDAO teamDAO;
    private final CoachDAO coachDAO;
    private final Runnable dataReloadCallback;

    private JTable tblCoaches;
    private DefaultTableModel coachModel;

    private List<Coach> coaches = new ArrayList<>();
    private List<Team> teams = new ArrayList<>();

    public CoachesPanel(TeamDAO teamDAO, CoachDAO coachDAO, Runnable dataReloadCallback) {
        this.teamDAO = teamDAO;
        this.coachDAO = coachDAO;
        this.dataReloadCallback = dataReloadCallback;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(30, 0, 30, 0));

        buildUI();
    }

    private void buildUI() {
        coachModel = new DefaultTableModel(new Object[]{"ID", "Name", "Age", "Team ID", "Delete"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return (c >= 1 && c <= 3) || c == 4;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                return (c == 0 || c == 2 || c == 3) ? Integer.class : Object.class;
            }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                if (column >= 1 && column <= 3) {
                    Object oldValue = getValueAt(row, column);
                    if (Objects.equals(oldValue, aValue)) return;

                    int id = (int) getValueAt(row, 0);
                    String name = String.valueOf(column == 1 ? aValue : getValueAt(row, 1)).trim();
                    String ageStr = String.valueOf(column == 2 ? aValue : getValueAt(row, 2)).trim();
                    int teamId = (int) (column == 3 ? aValue : getValueAt(row, 3));

                    if (!confirm("Are you sure you want to update this coach?\n\n" +
                            "Coach ID: " + id + "\nName: " + name + "\nTeam ID: " + teamId))
                        return;

                    try {
                        int age = ValidationUtil.parseAge(ageStr);
                        coachDAO.updateCoach(new Coach(id, name, age, teamId));

                        // Verify DB consistency
                        try (var conn = DatabaseConnection.get();
                             var ps = conn.prepareStatement("SELECT team_id FROM coaches WHERE id = ?")) {
                            ps.setInt(1, id);
                            try (var rs = ps.executeQuery()) {
                                if (!rs.next() || rs.getInt(1) != teamId)
                                    throw new DataAccessRuntimeException("This team already has a coach.", null);
                            }
                        }

                        super.setValueAt(aValue, row, column);
                        showInfo("✅ Coach updated successfully.");
                        dataReloadCallback.run();

                    } catch (Exception ex) {
                        showError("❌ " + ex.getMessage());
                        SwingUtilities.invokeLater(() -> super.setValueAt(oldValue, row, column));
                        dataReloadCallback.run();
                    }
                } else {
                    super.setValueAt(aValue, row, column);
                }
            }
        };

        // --- Table setup ---
        tblCoaches = new JTable(coachModel);
        tblCoaches.setFillsViewportHeight(true);
        tblCoaches.putClientProperty("terminateEditOnFocusLost", true);
        tblCoaches.setRowHeight(26);
        tblCoaches.setFont(new Font("SansSerif", Font.PLAIN, 13));


        tblCoaches.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Align numeric columns
        leftAlignNumberColumns(tblCoaches, new int[]{0, 2, 3});

        // Add delete button column
        addDeleteButton(tblCoaches, 4, Color.RED.darker(), this::deleteCoachAtRow);

        // --- Scroll and container styling ---
        JScrollPane scroll = new JScrollPane(tblCoaches);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));

        JPanel innerBox = new JPanel(new BorderLayout());
        innerBox.setPreferredSize(null); // allow expansion
        innerBox.setMaximumSize(new Dimension(900, Integer.MAX_VALUE));
        innerBox.setBackground(Color.WHITE);
        innerBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));
        innerBox.add(scroll, BorderLayout.CENTER);
        
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(false);
        tablePanel.add(innerBox, BorderLayout.CENTER);

        // --- Add Coach button ---
        JButton btnAddCoach = new JButton("Add Coach");
        btnAddCoach.setFont(btnAddCoach.getFont().deriveFont(Font.BOLD, 13f));
        btnAddCoach.setBackground(new Color(0, 120, 215));
        btnAddCoach.setForeground(Color.BLACK);
        btnAddCoach.setFocusPainted(false);
        btnAddCoach.setPreferredSize(new Dimension(150, 35));
        btnAddCoach.addActionListener(e -> addCoachDialog());

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(245, 247, 250));
        bottomPanel.add(btnAddCoach);

        add(tablePanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // ------------------ ADD COACH ------------------

    private void addCoachDialog() {
        if (teams.isEmpty()) {
            showError("Add a team first.");
            return;
        }

        JTextField txtName = new JTextField();
        JTextField txtAge = new JTextField();
        JComboBox<Team> cmbTeam = new JComboBox<>(teams.toArray(new Team[0]));
        Object[] fields = {"Name:", txtName, "Age:", txtAge, "Team:", cmbTeam};

        if (JOptionPane.showConfirmDialog(this, fields, "Add Coach",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
//                String name = txtName.getText().trim();
//                int age = ValidationUtil.parseAge(txtAge.getText());
//                Team t = (Team) cmbTeam.getSelectedItem();
            	String name = txtName.getText().trim();
            	String ageText = txtAge.getText().trim();

            	if (name.isEmpty() || ageText.isEmpty()) {
            	    showError("Name and Age cannot be empty.");
            	    return;
            	}

            	int age = ValidationUtil.parseAge(ageText);
            	Team t = (Team) cmbTeam.getSelectedItem();

                // 🔹 1-to-1 relationship validation
                for (Coach c : coaches) {
                    if (c.getTeamId() == t.id()) {
                        showError("❌ This team already has a coach assigned.");
                        return;
                    }
                }

                coachDAO.addCoach(new Coach(name, age, t.id()));
                showInfo("✅ Coach added successfully.");
                dataReloadCallback.run();

            } catch (Exception ex) {
                showError("Error adding coach: " + ex.getMessage());
            }
        }
    }

    // ------------------ DELETE ------------------

    private void deleteCoachAtRow(int row) {
        String name = String.valueOf(coachModel.getValueAt(row, 1));
        if (!confirm("Delete coach '" + name + "'?")) return;
        coachDAO.deleteCoachByName(name);
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

    // ------------------ DATA FROM PARENT ------------------

    public void refreshData(List<Coach> coaches, List<Team> teams) {
        this.coaches.clear();
        this.coaches.addAll(coaches);

        this.teams.clear();
        this.teams.addAll(teams);

        populateTable();
    }

    private void populateTable() {
        coachModel.setRowCount(0);
        for (Coach c : coaches) {
            coachModel.addRow(new Object[]{
                    c.id(),
                    c.getName(),
                    c.getAge(),
                    c.getTeamId(),
                    "Delete"
            });
        }
    }
}
