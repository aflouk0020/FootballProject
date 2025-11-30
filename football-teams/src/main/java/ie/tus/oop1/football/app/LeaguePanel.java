package ie.tus.oop1.football.app;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import e.tus.oop1.football.service.LeagueService;

import java.awt.*;
import java.util.List;

public class LeaguePanel extends JPanel {

    private final LeagueService service = new LeagueService();
    private final DefaultTableModel tableModel = new DefaultTableModel();
    private final JTable table;

    
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(210, 210, 210);
    private static final Font BASE_FONT = new Font("SansSerif", Font.PLAIN, 13);

    // last simulated data
    private List<Main.Match> lastFixtures = List.of();
    private List<Main.LeagueRow> lastTable = List.of();

    public LeaguePanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        // -------- TABLE SETUP --------
        table = new JTable(tableModel);
        tableModel.setColumnIdentifiers(new Object[]{
                "Pos", "Team", "P", "W", "D", "L", "GF", "GA", "Pts"
        });

        table.setFont(BASE_FONT);
        table.setRowHeight(26);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230));
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.putClientProperty("terminateEditOnFocusLost", true);

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setFont(new Font("SansSerif", Font.BOLD, 13));

        // custom renderer for highlighting rows
        table.setDefaultRenderer(Object.class, new LeagueTableCellRenderer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(12, 12, 12, 12)
        ));
        card.add(buildButtonBar(), BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);
    }

    // --------------------------------------------------------------------
    // BUTTON BAR
    // --------------------------------------------------------------------
    private JComponent buildButtonBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(0, 0, 8, 0));

        JButton simulateBtn = new JButton("▶ Simulate Season");
        JButton summaryBtn = new JButton("📄 Season Summary");
        JButton chartBtn = new JButton("📊 Points Chart");
        JButton exportJsonBtn = new JButton("💾 Export JSON");
        JButton exportXmlBtn = new JButton("💾 Export XML");

        stylePrimaryButton(simulateBtn);
        styleSecondaryButton(summaryBtn);
        styleSecondaryButton(chartBtn);
        styleSecondaryButton(exportJsonBtn);
        styleSecondaryButton(exportXmlBtn);

        // TOOLTIP TEXT
        simulateBtn.setToolTipText("Run a full season simulation with animated commentary");
        summaryBtn.setToolTipText("Show champion, top 3, best attack/defence, etc.");
        chartBtn.setToolTipText("Show a bar chart of points per team");
        exportJsonBtn.setToolTipText("Export league table to a JSON file");
        exportXmlBtn.setToolTipText("Export league table to an XML file");

        // ACTIONS
        simulateBtn.addActionListener(e -> simulateWithAnimation());
        summaryBtn.addActionListener(e -> {
            ensureTableExistsWithoutAnimation();
            if (!lastTable.isEmpty()) {
                showSummaryDialog(lastTable);
            }
        });

        chartBtn.addActionListener(e -> {
            ensureTableExistsWithoutAnimation();
            if (!lastTable.isEmpty()) {
                showChartDialog(lastTable);
            }
        });

        exportJsonBtn.addActionListener(e -> {
            ensureTableExistsWithoutAnimation();
            if (lastTable.isEmpty()) return;
            String json = service.exportLeagueJson(lastTable);
            showLargeText("League Table (JSON)", json);
            saveToFile("league_table.json", json);
        });

        exportXmlBtn.addActionListener(e -> {
            ensureTableExistsWithoutAnimation();
            if (lastTable.isEmpty()) return;
            String xml = service.exportLeagueXml(lastTable);
            showLargeText("League Table (XML)", xml);
            saveToFile("league_table.xml", xml);
        });

        bar.add(simulateBtn);
        bar.add(summaryBtn);
        bar.add(chartBtn);
        bar.add(Box.createHorizontalStrut(16));
        bar.add(exportJsonBtn);
        bar.add(exportXmlBtn);

        return bar;
    }

    private void stylePrimaryButton(JButton btn) {
        btn.setBackground(new Color(0, 120, 215));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
    }

    private void styleSecondaryButton(JButton btn) {
        btn.setBackground(new Color(238, 240, 244));
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
    }

    // --------------------------------------------------------------------
    // SIMULATION + TABLE FILL
    // --------------------------------------------------------------------
    private void simulateWithAnimation() {
        // Run simulation logic
        lastFixtures = service.simulateLeague();
        lastTable = service.generateLeagueTable(lastFixtures);

        // Show animated dialog; when finished, update table
        SimulationDialog dialog = new SimulationDialog(SwingUtilities.getWindowAncestor(this), lastFixtures, () -> {
            fillTable(lastTable);
            showSummaryDialog(lastTable);
        });
        dialog.start();
    }

    private void ensureTableExistsWithoutAnimation() {
        if (lastTable != null && !lastTable.isEmpty()) return;
        lastFixtures = service.simulateLeague();
        lastTable = service.generateLeagueTable(lastFixtures);
        fillTable(lastTable);
    }

    private void fillTable(List<Main.LeagueRow> rows) {
        tableModel.setRowCount(0);
        int pos = 1;
        for (var r : rows) {
            tableModel.addRow(new Object[]{
                    pos++,
                    r.teamName(), r.played(), r.won(), r.drawn(),
                    r.lost(), r.goalsFor(), r.goalsAgainst(), r.points()
            });
        }
    }

    // --------------------------------------------------------------------
    // SUMMARY DIALOG
    // --------------------------------------------------------------------
    private void showSummaryDialog(List<Main.LeagueRow> table) {
        if (table == null || table.isEmpty()) return;

        Main.LeagueRow champion = table.get(0);
        Main.LeagueRow runnerUp = table.size() > 1 ? table.get(1) : null;
        Main.LeagueRow third = table.size() > 2 ? table.get(2) : null;
        Main.LeagueRow bottom = table.get(table.size() - 1);

        int totalGoals = 0;
        Main.LeagueRow bestAttack = champion;
        Main.LeagueRow bestDefense = champion;

        for (Main.LeagueRow row : table) {
            totalGoals += row.goalsFor();
            if (row.goalsFor() > bestAttack.goalsFor()) bestAttack = row;
            if (row.goalsAgainst() < bestDefense.goalsAgainst()) bestDefense = row;
        }

        double avgGoals = totalGoals / (double) table.size();

        StringBuilder sb = new StringBuilder();
        sb.append("🏆 Champion: ").append(champion.teamName())
                .append(" (").append(champion.points()).append(" pts)\n\n");

        if (runnerUp != null) {
            sb.append("🥈 2nd: ").append(runnerUp.teamName())
                    .append(" (").append(runnerUp.points()).append(" pts)\n");
        }
        if (third != null) {
            sb.append("🥉 3rd: ").append(third.teamName())
                    .append(" (").append(third.points()).append(" pts)\n");
        }
        sb.append("\n");

        sb.append("🔥 Best Attack: ").append(bestAttack.teamName())
                .append(" (GF = ").append(bestAttack.goalsFor()).append(")\n");
        sb.append("🛡 Best Defence: ").append(bestDefense.teamName())
                .append(" (GA = ").append(bestDefense.goalsAgainst()).append(")\n");
        sb.append("⚠ Relegated (bottom): ").append(bottom.teamName())
                .append(" (").append(bottom.points()).append(" pts)\n\n");

        sb.append("⚽ Total goals scored: ").append(totalGoals).append("\n");
        sb.append("📊 Average goals per team: ").append(String.format("%.2f", avgGoals)).append("\n");

        showLargeText("Season Summary", sb.toString());
    }

    // --------------------------------------------------------------------
    // CHART DIALOG (POINTS BAR CHART)
    // --------------------------------------------------------------------
    private void showChartDialog(List<Main.LeagueRow> table) {
        if (table == null || table.isEmpty()) return;

        JDialog dialog = new JDialog((Frame) null, "Points Chart", true);
        dialog.setSize(800, 500);
        dialog.setLocationRelativeTo(null);

        dialog.setLayout(new BorderLayout());
        dialog.add(new PointsChartPanel(table), BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    // --------------------------------------------------------------------
    // TEXT & FILE HELPERS
    // --------------------------------------------------------------------
    private void showLargeText(String title, String text) {
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane pane = new JScrollPane(area);

        JDialog dialog = new JDialog((Frame) null, title, true);
        dialog.add(pane);
        dialog.setSize(600, 600);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    private void saveToFile(String defaultFileName, String content) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File(defaultFileName));

        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try (java.io.FileWriter writer = new java.io.FileWriter(chooser.getSelectedFile())) {
                writer.write(content);
                JOptionPane.showMessageDialog(this,
                        "Saved to:\n" + chooser.getSelectedFile(),
                        "Export Successful",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error saving file:\n" + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // --------------------------------------------------------------------
    // RENDERERS & INNER CLASSES
    // --------------------------------------------------------------------

    /**
     * Custom renderer to highlight the top 3 teams and bottom 2,
     * and add zebra striping.
     */
    private class LeagueTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (isSelected) {
                c.setBackground(new Color(0, 120, 215));
                c.setForeground(Color.WHITE);
                return c;
            }

            // zebra striping
            if (row % 2 == 0) {
                c.setBackground(Color.WHITE);
            } else {
                c.setBackground(new Color(245, 247, 250));
            }
            c.setForeground(Color.BLACK);

            int lastRow = table.getRowCount() - 1;

            // Top 3
            if (row == 0) {                 // 1st place
                c.setBackground(new Color(255, 238, 170)); // gold-ish
            } else if (row == 1) {          // 2nd
                c.setBackground(new Color(220, 230, 240)); // silver-ish
            } else if (row == 2) {          // 3rd
                c.setBackground(new Color(240, 220, 210)); // bronze-ish
            }
            // Bottom 2 (relegation)
            else if (row >= lastRow - 1) {
                c.setBackground(new Color(255, 220, 220));
            }

            return c;
        }
    }

    /**
     * Modal dialog that animates through fixtures using a Swing Timer.
     */
    private static class SimulationDialog extends JDialog {
        private final JTextArea area = new JTextArea();
        private final JProgressBar progressBar = new JProgressBar();
        private final List<Main.Match> fixtures;
        private final Runnable onComplete;
        private int index = 0;
        private Timer timer;

        SimulationDialog(Window owner, List<Main.Match> fixtures, Runnable onComplete) {
            super(owner, "Season Simulation", ModalityType.APPLICATION_MODAL);
            this.fixtures = fixtures;
            this.onComplete = onComplete;

            setSize(700, 500);
            setLocationRelativeTo(owner);
            setLayout(new BorderLayout(8, 8));

            area.setEditable(false);
            area.setFont(new Font("Monospaced", Font.PLAIN, 13));
            JScrollPane scroll = new JScrollPane(area);

            progressBar.setMinimum(0);
            progressBar.setMaximum(fixtures.isEmpty() ? 1 : fixtures.size());
            progressBar.setStringPainted(true);

            add(new JLabel("Simulating fixtures..."), BorderLayout.NORTH);
            add(scroll, BorderLayout.CENTER);
            add(progressBar, BorderLayout.SOUTH);
        }

        void start() {
            if (fixtures.isEmpty()) {
                area.setText("No teams available to simulate.\nAdd teams first.");
                progressBar.setValue(1);
                setVisible(true);
                return;
            }

            timer = new Timer(120, e -> step());
            timer.start();
            setVisible(true);
        }

        private void step() {
            if (index >= fixtures.size()) {
                timer.stop();
                area.append("\nSimulation complete ✅\n");
                progressBar.setValue(progressBar.getMaximum());

                if (onComplete != null) onComplete.run();

                // small delay before closing
                Timer closeTimer = new Timer(700, e -> dispose());
                closeTimer.setRepeats(false);
                closeTimer.start();
                return;
            }

            Main.Match m = fixtures.get(index++);
            String line = String.format(
                    "Round %2d: %-18s %2d - %2d %-18s%n",
                    m.round(),
                    m.home().getName(),
                    m.homeGoals(),
                    m.awayGoals(),

                    m.away().getName()
            );
            area.append(line);
            area.setCaretPosition(area.getDocument().getLength());
            progressBar.setValue(index);
        }
    }

    /**
     * Simple custom panel that draws a bar chart of points per team.
     */
    private static class PointsChartPanel extends JPanel {
        private final List<Main.LeagueRow> table;

        PointsChartPanel(List<Main.LeagueRow> table) {
            this.table = table;
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(20, 40, 40, 40));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (table == null || table.isEmpty()) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            int axisMargin = 40;
            int bottom = height - axisMargin;
            int left = axisMargin;
            int right = width - axisMargin;

            int maxPoints = table.stream().mapToInt(Main.LeagueRow::points).max().orElse(1);

            // draw axes
            g2.setColor(Color.DARK_GRAY);
            g2.drawLine(left, bottom, right, bottom);   // X
            g2.drawLine(left, bottom, left, axisMargin); // Y

            int n = table.size();
            int barSpace = (right - left - 20) / n;
            int barWidth = (int) (barSpace * 0.6);

            FontMetrics fm = g2.getFontMetrics();

            for (int i = 0; i < n; i++) {
                Main.LeagueRow row = table.get(i);
                int points = row.points();

                double ratio = points / (double) maxPoints;
                int barHeight = (int) ((bottom - axisMargin - 20) * ratio);

                int x = left + 10 + i * barSpace;
                int y = bottom - barHeight;

                // bar colour (top 3 highlighted)
                if (i == 0) g2.setColor(new Color(255, 205, 96));
                else if (i == 1) g2.setColor(new Color(189, 195, 199));
                else if (i == 2) g2.setColor(new Color(205, 127, 50));
                else g2.setColor(new Color(100, 149, 237));

                g2.fillRect(x, y, barWidth, barHeight);

                // outline
                g2.setColor(new Color(80, 80, 80, 160));
                g2.drawRect(x, y, barWidth, barHeight);

                // team name (shortened)
                String name = row.teamName();
                if (name.length() > 10) name = name.substring(0, 9) + "…";
                int textWidth = fm.stringWidth(name);
                int tx = x + (barWidth - textWidth) / 2;
                int ty = bottom + fm.getAscent() + 2;
                g2.setColor(Color.DARK_GRAY);
                g2.drawString(name, tx, ty);

                // points label
                String ptsLabel = String.valueOf(points);
                int ptsWidth = fm.stringWidth(ptsLabel);
                int px = x + (barWidth - ptsWidth) / 2;
                int py = y - 4;
                g2.drawString(ptsLabel, px, py);
            }

            g2.dispose();
        }
    }
}


