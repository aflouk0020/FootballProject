package ie.tus.oop1.football.app;

import ie.tus.oop1.football.dao.*;
import ie.tus.oop1.football.model.*;
import ie.tus.oop1.football.util.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) {
        var teamDAO = new TeamDAO();
        var playerDAO = new PlayerDAO();
        var coachDAO = new CoachDAO();

        System.out.println("\n⚽ Initialising Football Database...\n");

        // === TEAMS ===
        int madridId    = teamDAO.addTeamAndReturnId(new Team("Real Madrid", "Madrid", 1902));
        int liverpoolId = teamDAO.addTeamAndReturnId("Liverpool FC", "Liverpool", 1892);
        int barcaId     = teamDAO.addTeamAndReturnId(new Team("FC Barcelona", "Barcelona", 1899));
        int cityId      = teamDAO.addTeamAndReturnId("Manchester City", "Manchester", 1880);

        // === PLAYERS ===
        long newPlayers = Stream.of(
                new Player("Vinícius Jr.", Position.FORWARD, 23, madridId),
                new Player("Jude Bellingham", Position.MIDFIELDER, 21, madridId),
                new Player("Thibaut Courtois", Position.GOALKEEPER, 32, madridId),

                new Player("Mohamed Salah", Position.FORWARD, 31, liverpoolId),
                new Player("Virgil van Dijk", Position.DEFENDER, 33, liverpoolId),
                new Player("Trent Alexander-Arnold", Position.DEFENDER, 26, liverpoolId),

                new Player("Robert Lewandowski", Position.FORWARD, 35, barcaId),
                new Player("Pedri", Position.MIDFIELDER, 22, barcaId),
                new Player("Marc-André ter Stegen", Position.GOALKEEPER, 33, barcaId),

                new Player("Erling Haaland", Position.FORWARD, 24, cityId),
                new Player("Kevin De Bruyne", Position.MIDFIELDER, 33, cityId),
                new Player("Ederson", Position.GOALKEEPER, 31, cityId)
        ).filter(playerDAO::addPlayer).count();

        // === COACHES ===
        long newCoaches = Stream.of(
                new Coach("Carlo Ancelotti", 65, madridId),
                new Coach("Jürgen Klopp", 57, liverpoolId),
                new Coach("Xavi Hernández", 45, barcaId),
                new Coach("Pep Guardiola", 54, cityId)
        ).filter(coachDAO::addCoach).count();

        System.out.println("\n✅ Summary of new inserts:");
        System.out.println("   ➤ Teams inserted: (if any new ones were missing)");
        System.out.println("   ➤ Players inserted: " + newPlayers);
        System.out.println("   ➤ Coaches inserted: " + newCoaches);

        // === DISPLAY COACHES ===
        System.out.println("\n🏟️  ALL COACHES:");
        coachDAO.getAllCoaches().forEach(c -> System.out.println("  ↳ " + c.name()));

        // === SEALED TYPE DEMO ===
        System.out.println("\n🔒 Sealed Class Demo:");
        FootballEntity[] entities = {
                new Coach("Carlo Ancelotti", 65, madridId),
                new Team("Demo United", "Dublin", 1999),
                new Player("Demo Forward", Position.FORWARD, 19, madridId)
        };
        for (var e : entities)
            System.out.println("  " + EntityPrinter.describe(e));

        // === DISPLAY TEAMS & PLAYERS ===
        System.out.println("\n📋 ALL TEAMS AND THEIR PLAYERS:");
        for (Team t : teamDAO.getAllTeamsWithPlayers()) {
            System.out.println("\n" + t + "\nPlayers:");
            t.getPlayers().stream()
                    .sorted(Comparator.comparingInt(Player::getAge).reversed())
                    .forEach(p -> System.out.println("   ↳ " + p));
        }

        // === LAMBDAS / PREDICATES ===
        List<Player> allPlayers = playerDAO.getAllPlayers();
        var adultForwards = ValidationUtil.filter(allPlayers,
                p -> p.getPosition().isAttacking() && ValidationUtil.isAdult(p));
        System.out.println("\n⚡ Adult attacking players: " + adultForwards.size());

        // === RECORD + SWITCH EXPRESSION ===
        var contract = new ContractRecord("Jude Bellingham", 18_000_000, 6);
        var label = switch (contract.years()) {
            case 1, 2, 3 -> "Short-term";
            case 4, 5 -> "Mid-term";
            default -> "Long-term";
        };
        System.out.printf("💰 Contract for %s is %s worth €%,.0f%n",
                contract.playerName(), label, contract.totalValue());
    }
}
