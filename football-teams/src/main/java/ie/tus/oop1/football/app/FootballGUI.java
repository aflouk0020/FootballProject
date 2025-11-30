

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
    private LeaguePanel leaguePanel;

    // Center area with cards
    private JPanel cardContainer;
    private CardLayout cardLayout;

    private static final Color WINDOW_BG = new Color(245, 247, 250);
    private static final Color SIDEBAR_BG = new Color(232, 235, 240);
    private static final Color SIDEBAR_BUTTON_BG = new Color(220, 224, 232);
    private static final Color SIDEBAR_BUTTON_SELECTED = new Color(200, 210, 230);
    private static final Font  BASE_FONT = new Font("SansSerif", Font.PLAIN, 13);

    private static final String CARD_TEAMS  = "CARD_TEAMS";
    private static final String CARD_COACHES = "CARD_COACHES";
    private static final String CARD_LEAGUE = "CARD_LEAGUE";

    public FootballGUI() {
        setTitle("⚽ Football Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1300, 760);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        UIManager.put("Label.font", BASE_FONT);
        UIManager.put("Button.font", BASE_FONT);

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        getContentPane().setBackground(WINDOW_BG);

        updateDbStatus();
        loadAllDataAsync();
    }

    // ------------------ TOP BAR ------------------

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout(10, 0));
        top.setBorder(new EmptyBorder(8, 12, 8, 12));
        top.setBackground(Color.WHITE);

        JLabel title = new JLabel("Football Management System");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));

        lblStatus = new JLabel("DB: Checking…");
        lblStatus.setFont(lblStatus.getFont().deriveFont(Font.BOLD, 13f));
        lblStatus.setForeground(new Color(0, 128, 0));

        JButton btnRefresh = new JButton("Refresh");
        stylePrimaryButton(btnRefresh);
        btnRefresh.addActionListener(e -> loadAllDataAsync());

        txtSearchTeams = new JTextField(22);
        txtSearchTeams.putClientProperty("JTextField.placeholderText", "Search teams…");
        txtSearchTeams.addActionListener(e -> {
            String q = txtSearchTeams.getText().trim().toLowerCase();
            if (teamsPlayersPanel != null) teamsPlayersPanel.filterTeams(q);
        });

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(title);
        left.add(new JLabel("│"));
        left.add(lblStatus);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(txtSearchTeams);
        right.add(btnRefresh);

        top.add(left, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);
        return top;
    }

    // ------------------ BODY (SIDEBAR + CARDS) ------------------

    private JComponent buildBody() {
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(WINDOW_BG);

        // ✨ FIRST: Create card layout + container
        cardLayout = new CardLayout();
        cardContainer = new JPanel(cardLayout);
        cardContainer.setBackground(WINDOW_BG);

        // Create panels
        teamsPlayersPanel = new TeamsPlayersPanel(teamDAO, playerDAO, this::loadAllDataAsync);
        coachesPanel      = new CoachesPanel(teamDAO, coachDAO, this::loadAllDataAsync);
        leaguePanel       = new LeaguePanel();

        // Add them to card container
        cardContainer.add(new JScrollPane(teamsPlayersPanel), CARD_TEAMS);

        cardContainer.add(coachesPanel, CARD_COACHES);
        cardContainer.add(leaguePanel, CARD_LEAGUE);

        // THEN: build sidebar (it can now switch cards safely)
        JPanel sidebar = buildSidebar();

        body.add(sidebar, BorderLayout.WEST);
        body.add(cardContainer, BorderLayout.CENTER);
        return body;
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setBorder(new EmptyBorder(16, 12, 16, 12));
        sidebar.setPreferredSize(new Dimension(190, 0));

        JLabel navTitle = new JLabel("Navigation");
        navTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        navTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(navTitle);
        sidebar.add(Box.createVerticalStrut(12));

        ButtonGroup group = new ButtonGroup();

        JToggleButton btnTeams = createSidebarButton("Teams & Players", group);
        btnTeams.addActionListener(e -> cardLayout.show(cardContainer, CARD_TEAMS));

        JToggleButton btnCoaches = createSidebarButton("Coaches", group);
        btnCoaches.addActionListener(e -> cardLayout.show(cardContainer, CARD_COACHES));

        JToggleButton btnLeague = createSidebarButton("League Simulator", group);
        btnLeague.addActionListener(e -> cardLayout.show(cardContainer, CARD_LEAGUE));

        // Default selection
        btnTeams.setSelected(true);
        cardLayout.show(cardContainer, CARD_TEAMS);

        sidebar.add(btnTeams);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(btnCoaches);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(btnLeague);
        sidebar.add(Box.createVerticalGlue());

        return sidebar;
    }


    private JToggleButton createSidebarButton(String text, ButtonGroup group) {
        JToggleButton btn = new JToggleButton(text);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btn.setFocusPainted(false);
        btn.setBackground(SIDEBAR_BUTTON_BG);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        btn.setHorizontalAlignment(SwingConstants.LEFT);

        btn.addChangeListener(e -> {
            if (btn.isSelected()) {
                btn.setBackground(SIDEBAR_BUTTON_SELECTED);
            } else {
                btn.setBackground(SIDEBAR_BUTTON_BG);
            }
        });

        group.add(btn);
        return btn;
    }

    private void stylePrimaryButton(JButton btn) {
        btn.setBackground(new Color(0, 120, 215));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
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

    // ------------------ LOAD DATA ------------------

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
                if (teamsPlayersPanel != null)
                    teamsPlayersPanel.refreshData(teams, playersByTeam);

                if (coachesPanel != null)
                    coachesPanel.refreshData(coaches, teams);
            }
        }.execute();
    }

    // ------------------ MAIN ------------------

    public static void main(String[] args) {
        DatabaseMigrator.run();
        SwingUtilities.invokeLater(() -> new FootballGUI().setVisible(true));
    }
}

