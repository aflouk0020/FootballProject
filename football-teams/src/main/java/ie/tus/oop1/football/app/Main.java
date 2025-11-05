package ie.tus.oop1.football.app;

import ie.tus.oop1.football.dao.TeamDAO;
import ie.tus.oop1.football.dao.PlayerDAO;
import ie.tus.oop1.football.model.Team;
import ie.tus.oop1.football.model.Player;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        TeamDAO teamDAO = new TeamDAO();
        PlayerDAO playerDAO = new PlayerDAO();

        //  Create multiple teams and get their IDs
        int madridId = teamDAO.addTeamAndReturnId(new Team("Real Madrid", "Madrid", 1902));
        int liverpoolId = teamDAO.addTeamAndReturnId(new Team("Liverpool FC", "Liverpool", 1892));
        int barcelonaId = teamDAO.addTeamAndReturnId(new Team("FC Barcelona", "Barcelona", 1899));
        int manCityId = teamDAO.addTeamAndReturnId(new Team("Manchester City", "Manchester", 1880));

        // Add players for each team
        // Real Madrid
        playerDAO.addPlayer(new Player("Vinícius Jr.", "Forward", 23, madridId));
        playerDAO.addPlayer(new Player("Jude Bellingham", "Midfielder", 21, madridId));
        playerDAO.addPlayer(new Player("Thibaut Courtois", "Goalkeeper", 32, madridId));

        // Liverpool FC
        playerDAO.addPlayer(new Player("Mohamed Salah", "Forward", 31, liverpoolId));
        playerDAO.addPlayer(new Player("Virgil van Dijk", "Defender", 33, liverpoolId));
        playerDAO.addPlayer(new Player("Trent Alexander-Arnold", "Defender", 26, liverpoolId));

        // FC Barcelona
        playerDAO.addPlayer(new Player("Robert Lewandowski", "Forward", 35, barcelonaId));
        playerDAO.addPlayer(new Player("Pedri", "Midfielder", 22, barcelonaId));
        playerDAO.addPlayer(new Player("Marc-André ter Stegen", "Goalkeeper", 33, barcelonaId));

        // Manchester City
        playerDAO.addPlayer(new Player("Erling Haaland", "Forward", 24, manCityId));
        playerDAO.addPlayer(new Player("Kevin De Bruyne", "Midfielder", 33, manCityId));
        playerDAO.addPlayer(new Player("Ederson", "Goalkeeper", 31, manCityId));

        // Display all teams with their players
        System.out.println("\n ALL TEAMS AND THEIR PLAYERS:");
        for (Team t : teamDAO.getAllTeamsWithPlayers()) {
            System.out.println("\n" + t.getName() + " (" + t.getCity() + ", Founded " + t.getFoundedYear() + ")");
            System.out.println("Players:");
            if (t.getPlayers().isEmpty()) {
                System.out.println("    No players found for this team.");
            } else {
                for (Player p : t.getPlayers()) {
                    System.out.println("   ↳ " + p.getName() + " (" + p.getPosition() + ", Age " + p.getAge() + ")");
                }
            }
        }

        // Optional: Count total players
        List<Player> allPlayers = playerDAO.getAllPlayers();
        System.out.println("\nTotal players in database: " + allPlayers.size());
    }
}
