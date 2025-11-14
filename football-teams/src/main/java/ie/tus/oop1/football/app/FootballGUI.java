package ie.tus.oop1.football.app;

import ie.tus.oop1.football.dao.CoachDAO;
import ie.tus.oop1.football.dao.DatabaseConnection;
import ie.tus.oop1.football.dao.PlayerDAO;
import ie.tus.oop1.football.dao.TeamDAO;
import ie.tus.oop1.football.model.Coach;
import ie.tus.oop1.football.model.Player;
import ie.tus.oop1.football.model.Team;
import e.tus.oop1.football.service.DatabaseMigrator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.util.*;
import java.util.List;

public class FootballGUI extends JFrame {

    private final TeamDAO teamDAO = new TeamDAO();
    private final PlayerDAO playerDAO = new PlayerDAO();
    private final CoachDAO coachDAO = new CoachDAO();

    private JLabel lblStatus;
    private JTextField txtSearchTeams;

    // Shared data
    private List<Team> teams = new ArrayList<>();
    private Map<Integer, List<Player>> playersByTeam = new HashMap<>();
    private List<Coach> coaches = new ArrayList<>();

    // Child panels
    private TeamsPlayersPanel teamsPlayersPanel;
    private CoachesPanel coachesPanel;

    private static final Color WINDOW_BG = new Color(245, 247, 250);

    public FootballGUI() {
        setTitle("⚽ Football Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1250, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildTabs(), BorderLayout.CENTER);

        // Light background for the main content
        getContentPane().setBackground(WINDOW_BG);

        updateDbStatus();
        loadAllDataAsync();
    }

    // ------------------ TOP BAR ------------------

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout(10, 0));
        top.setBorder(new EmptyBorder(8, 8, 8, 8));
        top.setBackground(WINDOW_BG);

        lblStatus = new JLabel("DB: Checking…");
        lblStatus.setFont(lblStatus.getFont().deriveFont(Font.BOLD, 13f));
        lblStatus.setForeground(new Color(0, 128, 0));

        // 🔄 Refresh button
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> loadAllDataAsync());

        // 🔍 Search field
        txtSearchTeams = new JTextField(22);
        txtSearchTeams.putClientProperty("JTextField.placeholderText", "Search teams…");
        txtSearchTeams.addActionListener(e -> {
            String q = txtSearchTeams.getText().trim().toLowerCase();
            if (teamsPlayersPanel != null) {
                teamsPlayersPanel.filterTeams(q);
            }
        });

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.add(txtSearchTeams);
        right.add(btnRefresh);

        top.add(lblStatus, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);
        return top;
    }

    // ------------------ TABS ------------------

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();

        // pass DAOs + callback
        teamsPlayersPanel = new TeamsPlayersPanel(teamDAO, playerDAO, this::loadAllDataAsync);
        coachesPanel      = new CoachesPanel(teamDAO, coachDAO, this::loadAllDataAsync);

        tabs.addTab("Teams / Players", teamsPlayersPanel);
        tabs.addTab("Coaches", coachesPanel);

        tabs.setBackground(WINDOW_BG);
        tabs.setForeground(Color.BLACK);

        return tabs;
    }

    // ------------------ DB STATUS ------------------

    private void updateDbStatus() {
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                try (Connection c = DatabaseConnection.get()) {
                    return c != null && !c.isClosed();
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    boolean ok = get();
                    lblStatus.setText(ok ? "DB: Connected ✅" : "DB: Not connected ❌");
                    lblStatus.setForeground(ok ? new Color(0, 128, 0) : Color.RED);
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    // ------------------ DATA LOADING ------------------

    private void loadAllDataAsync() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                teams = teamDAO.getAllTeams();

                playersByTeam.clear();
                for (Team t : teamDAO.getAllTeamsWithPlayers()) {
                    playersByTeam.put(t.id(), t.getPlayers());
                }

                coaches = coachDAO.getAllCoaches();
                return null;
            }

            @Override
            protected void done() {
                if (teamsPlayersPanel != null) {
                    teamsPlayersPanel.refreshData(teams, playersByTeam);
                }
                if (coachesPanel != null) {
                    coachesPanel.refreshData(coaches, teams);
                }
            }
        }.execute();
    }

    // ------------------ MAIN ------------------

    public static void main(String[] args) {
        DatabaseMigrator.run();
        SwingUtilities.invokeLater(() -> new FootballGUI().setVisible(true));
    }
}







//package ie.tus.oop1.football.app;
//
//import ie.tus.oop1.football.dao.CoachDAO;
//import ie.tus.oop1.football.dao.DatabaseConnection;
//import ie.tus.oop1.football.dao.PlayerDAO;
//import ie.tus.oop1.football.dao.TeamDAO;
//import ie.tus.oop1.football.model.Coach;
//import ie.tus.oop1.football.model.Player;
//import ie.tus.oop1.football.model.Team;
//import e.tus.oop1.football.service.DatabaseMigrator; // (same as your code)
//import ie.tus.oop1.football.util.ValidationUtil;
//
//import javax.swing.*;
//import javax.swing.border.EmptyBorder;
//import java.awt.*;
//import java.sql.Connection;
//import java.util.List;
//import java.util.*;
//import java.util.ArrayList;
//import java.util.HashMap;
//
//public class FootballGUI extends JFrame {
//
//  private final TeamDAO teamDAO = new TeamDAO();
//  private final PlayerDAO playerDAO = new PlayerDAO();
//  private final CoachDAO coachDAO = new CoachDAO();
//
//  private JLabel lblStatus;
//  private JTextField txtSearchTeams;
//
//  private boolean darkMode = false; // 🌙 Track theme state
//
//  // Shared data
//  private List<Team> teams = new ArrayList<>();
//  private Map<Integer, List<Player>> playersByTeam = new HashMap<>();
//  private List<Coach> coaches = new ArrayList<>();
//
//  // Child panels
//  private TeamsPlayersPanel teamsPlayersPanel;
//  private CoachesPanel coachesPanel;
//
//  public FootballGUI() {
//      setTitle("⚽ Football Management System");
//      setDefaultCloseOperation(EXIT_ON_CLOSE);
//      setSize(1250, 720);
//      setLocationRelativeTo(null);
//      setLayout(new BorderLayout(8, 8));
//
//      JPanel topBar = buildTopBar();
//      add(topBar, BorderLayout.NORTH);
//
//      JTabbedPane tabs = buildTabs();
//      add(tabs, BorderLayout.CENTER);
//
//      updateDbStatus();
//      loadAllDataAsync();
//  }
//
//  // ------------------ UI BUILDERS ------------------
//
//  private JPanel buildTopBar() {
//      JPanel top = new JPanel(new BorderLayout(10, 0));
//      top.setBorder(new EmptyBorder(8, 8, 8, 8));
//
//      lblStatus = new JLabel("DB: Checking…");
//      lblStatus.setFont(lblStatus.getFont().deriveFont(Font.BOLD, 13f));
//
//      // 🔄 Refresh button
//      JButton btnRefresh = new JButton("Refresh");
//      btnRefresh.addActionListener(e -> loadAllDataAsync());
//
//      // 🌙 Dark mode toggle button
//      JButton btnTheme = new JButton("🌙 Dark");
//      btnTheme.setFocusPainted(false);
//      btnTheme.addActionListener(e -> {
//          darkMode = !darkMode;
//          applyTheme(darkMode);
//          btnTheme.setText(darkMode ? "☀️ Light" : "🌙 Dark");
//      });
//
//      txtSearchTeams = new JTextField(22);
//      txtSearchTeams.putClientProperty("JTextField.placeholderText", "Search teams…");
//      txtSearchTeams.addActionListener(e -> {
//          String q = txtSearchTeams.getText().trim().toLowerCase();
//          if (teamsPlayersPanel != null) {
//              teamsPlayersPanel.filterTeams(q);
//          }
//      });
//
//      JPanel right = new JPanel();
//      right.add(txtSearchTeams);
//      right.add(btnRefresh);
//      right.add(btnTheme);
//
//      top.add(lblStatus, BorderLayout.WEST);
//      top.add(right, BorderLayout.EAST);
//      return top;
//  }
//
//  private JTabbedPane buildTabs() {
//      JTabbedPane tabs = new JTabbedPane();
//
//      // Pass DAOs + callback so panels can trigger reloads after CRUD
//      teamsPlayersPanel = new TeamsPlayersPanel(teamDAO, playerDAO, this::loadAllDataAsync);
//      coachesPanel = new CoachesPanel(teamDAO, coachDAO, this::loadAllDataAsync);
//
//      tabs.addTab("Teams / Players", teamsPlayersPanel);
//      tabs.addTab("Coaches", coachesPanel);
//      return tabs;
//  }
//
//  // ------------------ THEME ------------------
//
//  private void applyTheme(boolean dark) {
//      // 🎨 Define theme colours
//      Color bg, fg, tableBg, tableFg, headerBg, headerFg, btnBg, btnFg, fieldBg, fieldFg, scrollBg, tabBg;
//
//      if (dark) {
//          bg = new Color(40, 44, 52);
//          fg = new Color(230, 230, 230);
//          tableBg = new Color(50, 54, 62);
//          tableFg = Color.WHITE;
//          headerBg = new Color(65, 69, 78);
//          headerFg = Color.WHITE;
//          btnBg = new Color(75, 110, 175);
//          btnFg = Color.WHITE;
//          fieldBg = new Color(60, 63, 70);
//          fieldFg = Color.WHITE;
//          scrollBg = new Color(55, 58, 65);
//          tabBg = new Color(45, 48, 55);
//      } else {
//          bg = new Color(245, 247, 250);
//          fg = Color.BLACK;
//          tableBg = Color.WHITE;
//          tableFg = Color.BLACK;
//          headerBg = new Color(235, 237, 240);
//          headerFg = Color.BLACK;
//          btnBg = new Color(0, 120, 215);
//          btnFg = Color.WHITE;
//          fieldBg = Color.WHITE;
//          fieldFg = Color.BLACK;
//          scrollBg = new Color(245, 247, 250);
//          tabBg = new Color(245, 247, 250);
//      }
//
//      // 🌍 Apply to frame background and panels
//      getContentPane().setBackground(bg);
////      updateContainerColors(getContentPane(), bg, fg);
//      updateContainerBackgrounds(getContentPane(), bg);
//
//      // 🧾 Tables
//      List<JTable> allTables = findComponentsOfType(this, JTable.class);
//      for (JTable tbl : allTables) {
//          tbl.setBackground(tableBg);
//          tbl.setForeground(tableFg);
//          tbl.setSelectionBackground(dark ? new Color(75, 110, 175) : new Color(184, 207, 229));
//          tbl.setSelectionForeground(Color.WHITE);
//          tbl.setGridColor(dark ? new Color(90, 90, 90) : new Color(220, 220, 220));
//          if (tbl.getTableHeader() != null) {
//              tbl.getTableHeader().setBackground(headerBg);
//              tbl.getTableHeader().setForeground(headerFg);
//          }
//      }
//
//      // 🧮 Text fields
//      List<JTextField> allTextFields = findComponentsOfType(this, JTextField.class);
//      for (JTextField f : allTextFields) {
//          f.setBackground(fieldBg);
//          f.setForeground(fieldFg);
//          f.setCaretColor(fieldFg);
//          f.setBorder(BorderFactory.createLineBorder(dark ? new Color(90, 90, 90) : new Color(200, 200, 200)));
//      }
//
//      // 🧩 Combo boxes
//      @SuppressWarnings({ "rawtypes", "unchecked" })
//      List<JComboBox> allCombos = findComponentsOfType(this, JComboBox.class);
//      for (JComboBox combo : allCombos) {
//          combo.setBackground(fieldBg);
//          combo.setForeground(fieldFg);
//          combo.setRenderer(new DefaultListCellRenderer() {
//              @Override
//              public Component getListCellRendererComponent(JList<?> list, Object value, int index,
//                                                            boolean isSelected, boolean cellHasFocus) {
//                  JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
//                  label.setBackground(isSelected ? btnBg.darker() : fieldBg);
//                  label.setForeground(fieldFg);
//                  return label;
//              }
//          });
//      }
//
//      // 🖱 Scroll panes
//      List<JScrollPane> allScrolls = findComponentsOfType(this, JScrollPane.class);
//      for (JScrollPane sp : allScrolls) {
//          sp.getViewport().setBackground(tableBg);
//          sp.setBackground(scrollBg);
//          sp.setBorder(BorderFactory.createLineBorder(dark ? new Color(80, 80, 80) : new Color(210, 210, 210)));
//      }
//
//      // 🗂 Tabbed panes
//      List<JTabbedPane> allTabs = findComponentsOfType(this, JTabbedPane.class);
//      for (JTabbedPane tab : allTabs) {
//          tab.setBackground(tabBg);
//          tab.setForeground(fg);
//          tab.setOpaque(true);
//      }
//
//      // 🔘 Buttons
//      List<JButton> allButtons = findComponentsOfType(this, JButton.class);
//      for (JButton b : allButtons) {
//          applyButtonTheme(b, dark);
//      }
//
//      // 🔁 Update UI defaults for future components
////      UIManager.put("Panel.background", bg);
////      UIManager.put("Label.foreground", fg);
////      UIManager.put("Button.background", btnBg);
////      UIManager.put("Button.foreground", btnFg);
////      UIManager.put("TextField.background", fieldBg);
////      UIManager.put("TextField.foreground", fieldFg);
////      UIManager.put("ComboBox.background", fieldBg);
////      UIManager.put("ComboBox.foreground", fieldFg);
////      UIManager.put("Table.background", tableBg);
////      UIManager.put("Table.foreground", tableFg);
////      UIManager.put("TableHeader.background", headerBg);
////      UIManager.put("TableHeader.foreground", headerFg);
////      UIManager.put("TabbedPane.background", tabBg);
////      UIManager.put("TabbedPane.foreground", fg);
//
//      // 💡 Full refresh
////      SwingUtilities.updateComponentTreeUI(this);
//
//      lblStatus.setForeground(dark ? new Color(120, 200, 120) : new Color(0, 128, 0));
//  }
//
////  private void updateContainerColors(Container container, Color bg, Color fg) {
////      for (Component c : container.getComponents()) {
////          c.setBackground(bg);
////          c.setForeground(fg);
////          if (c instanceof Container child) {
////              updateContainerColors(child, bg, fg);
////          }
////      }
////  }
//
//  private void updateContainerBackgrounds(Container container, Color bg) {
//      for (Component c : container.getComponents()) {
//          c.setBackground(bg);
//          if (c instanceof Container child) {
//              updateContainerBackgrounds(child, bg);
//          }
//      }
//  }
//
//  private <T extends JComponent> List<T> findComponentsOfType(Container parent, Class<T> type) {
//      List<T> list = new ArrayList<>();
//      for (Component c : parent.getComponents()) {
//          if (type.isInstance(c)) {
//              list.add(type.cast(c));
//          } else if (c instanceof Container cont) {
//              list.addAll(findComponentsOfType(cont, type));
//          }
//      }
//      return list;
//  }
//  
//
//  private void applyButtonTheme(JButton b, boolean dark) {
//      if (dark) {
//          b.setBackground(new Color(90, 130, 200));
//          b.setForeground(Color.WHITE);
//          b.setBorder(BorderFactory.createLineBorder(new Color(140, 170, 220), 1));
//      } else {
//          b.setBackground(new Color(235, 235, 235));
//          b.setForeground(Color.BLACK);
//          b.setBorder(BorderFactory.createLineBorder(new Color(155, 155, 155), 1));
//      }
//
//      b.setOpaque(true);
//      b.setContentAreaFilled(true);
//      b.setFocusPainted(false);
//
//      // Medium size (smaller general default)
//      b.setMargin(new Insets(3, 10, 3, 10));
//      b.setPreferredSize(new Dimension(60, 24));
//  }
//
//  // ------------------ DB STATUS & DATA LOADING ------------------
//
//  private void updateDbStatus() {
//      new SwingWorker<Boolean, Void>() {
//          @Override
//          protected Boolean doInBackground() {
//              try (Connection c = DatabaseConnection.get()) {
//                  return c != null && !c.isClosed();
//              } catch (Exception e) {
//                  return false;
//              }
//          }
//
//          @Override
//          protected void done() {
//              try {
//                  boolean ok = get();
//                  lblStatus.setText(ok ? "DB: Connected ✅" : "DB: Not connected ❌");
//                  lblStatus.setForeground(ok ? new Color(0, 128, 0) : Color.RED);
//              } catch (Exception ignored) {
//              }
//          }
//      }.execute();
//  }
//
//  private void loadAllDataAsync() {
//      new SwingWorker<Void, Void>() {
//          @Override
//          protected Void doInBackground() {
//              teams = teamDAO.getAllTeams();
//              playersByTeam.clear();
//              for (Team t : teamDAO.getAllTeamsWithPlayers()) {
//                  playersByTeam.put(t.id(), t.getPlayers());
//              }
//              coaches = coachDAO.getAllCoaches();
//              return null;
//          }
//
//          @Override
//          protected void done() {
//              if (teamsPlayersPanel != null) {
//                  teamsPlayersPanel.refreshData(teams, playersByTeam);
//              }
//              if (coachesPanel != null) {
//                  coachesPanel.refreshData(coaches, teams);
//              }
//          }
//      }.execute();
//  }
//
//  public static void main(String[] args) {
//      DatabaseMigrator.run();
//      SwingUtilities.invokeLater(() -> new FootballGUI().setVisible(true));
//  }
//}




