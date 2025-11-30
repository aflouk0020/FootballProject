package ie.tus.oop1.football.app;

import ie.tus.oop1.football.model.Position;
import ie.tus.oop1.football.model.Coach;
import ie.tus.oop1.football.model.Player;
import ie.tus.oop1.football.model.Team;
import ie.tus.oop1.football.dao.CoachDAO;
import ie.tus.oop1.football.dao.PlayerDAO;
import ie.tus.oop1.football.dao.TeamDAO;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Main {

    public static void main(String[] args) {

        System.out.println("🌍 Inserting full league into the database...");

        var teamDAO = new TeamDAO();
        var playerDAO = new PlayerDAO();
        var coachDAO = new CoachDAO();

        
        // 12 TEAMS
        var teams = List.of(
                new Team("Athlone Town", "Athlone", 1887),
                new Team("Galway United", "Galway", 1937),
                new Team("Shamrock Rovers", "Dublin", 1901),
                new Team("Bohemians FC", "Dublin", 1890),
                new Team("Cork City", "Cork", 1984),
                new Team("Dundalk FC", "Dundalk", 1903),
                new Team("Sligo Rovers", "Sligo", 1928),
                new Team("Waterford FC", "Waterford", 1930),
                new Team("Limerick FC", "Limerick", 1937),
                new Team("Bray Wanderers", "Wicklow", 1942),
                new Team("Finn Harps", "Donegal", 1954),
                new Team("Derry City", "Derry", 1928)
        );

        // INSERT TEAMS + COACHES
        for (var team : teams) {
            int teamId = teamDAO.addTeamAndReturnId(team);

            // Create a coach for each team
            var coach = new Coach("Coach " + team.name(), 45, teamId);
            coachDAO.addCoach(coach);

            System.out.println("Inserted: " + team.name() + " with coach " + coach.getName());

            // ADD 8–12 RANDOM PLAYERS
            addPlayersForTeam(playerDAO, teamId);
        }

        System.out.println("✅ ALL TEAMS, COACHES, AND PLAYERS INSERTED SUCCESSFULLY!");
    }

    /**
     * Add players to a team using varargs + LVTI + random positions.
     */
    private static void addPlayersForTeam(PlayerDAO playerDAO, int teamId) {

        var positions = Position.values();

        // 10 players per team
        for (int i = 1; i <= 10; i++) {

        	
        	
            var position = positions[(int) (Math.random() * positions.length)];

            var player = new Player(
                    "Player" + i + "_T" + teamId,
                    position,
                    18 + (int) (Math.random() * 15),  // age 18–33
                    teamId
            );

            playerDAO.addPlayer(player);
        }
    }

//------------------------------------------------------------
//FIXED METHODS FOR LEAGUE SIMULATION (USED BY YOUR GUI)
//------------------------------------------------------------

public static List<Match> runSimulationAndReturnFixtures(List<Team> teams) {
 var matches = new ArrayList<Match>();
 var rnd = new Random();
 int round = 1;

 for (int i = 0; i < teams.size(); i++) {
     for (int j = i + 1; j < teams.size(); j++) {
         int home = rnd.nextInt(5);
         int away = rnd.nextInt(5);
         matches.add(new Match(round++, teams.get(i), teams.get(j), home, away));
     }
 }

 return matches;
}

public static List<LeagueRow> computeLeagueTable(List<Team> teams, List<Match> matches) {

 class Acc { int p, w, d, l, gf, ga, pts; }

 Map<Integer, Acc> acc = new HashMap<>();
 for (var t : teams) acc.put(t.id(), new Acc());

 for (var m : matches) {
     var home = acc.get(m.home().id());
     var away = acc.get(m.away().id());

     home.p++; away.p++;
     home.gf += m.homeGoals(); home.ga += m.awayGoals();
     away.gf += m.awayGoals(); away.ga += m.homeGoals();

     if (m.homeGoals() > m.awayGoals()) {
         home.w++; home.pts += 3; away.l++;
     } else if (m.homeGoals() < m.awayGoals()) {
         away.w++; away.pts += 3; home.l++;
     } else {
         home.d++; away.d++; home.pts++; away.pts++;
     }
 }

 List<LeagueRow> table = new ArrayList<>();
 for (var t : teams) {
     var a = acc.get(t.id());
     table.add(new LeagueRow(t.getName(), a.p, a.w, a.d, a.l, a.gf, a.ga, a.pts));
 }

 table.sort((a, b) -> Integer.compare(b.points(), a.points()));

 return table;
}

public static List<PlayerStats> generatePlayerStats(List<Player> players, List<Team> teams) {
 var map = new HashMap<Integer, String>();
 for (var t : teams) map.put(t.id(), t.getName());

 var rnd = new Random();
 var out = new ArrayList<PlayerStats>();

 for (var p : players) {
     out.add(new PlayerStats(
         p.getName(),
         map.getOrDefault(p.getTeamId(), "Unknown"),
         rnd.nextInt(10),
         rnd.nextInt(6),
         6 + rnd.nextDouble() * 3
     ));
 }

 return out;
}

public static String exportLeagueTableToJson(List<LeagueRow> rows) {
 StringBuilder sb = new StringBuilder("[\n");
 for (var r : rows) {
     sb.append("  {")
       .append("\"team\":\"").append(r.teamName()).append("\",")
       .append("\"played\":").append(r.played()).append(',')
       .append("\"won\":").append(r.won()).append(',')
       .append("\"drawn\":").append(r.drawn()).append(',')
       .append("\"lost\":").append(r.lost()).append(',')
       .append("\"gf\":").append(r.goalsFor()).append(',')
       .append("\"ga\":").append(r.goalsAgainst()).append(',')
       .append("\"pts\":").append(r.points())
       .append("},\n");
 }
 return sb.append("]").toString();
}


public static String exportLeagueTableToXml(List<LeagueRow> rows) {
 StringBuilder sb = new StringBuilder("<leagueTable>\n");
 for (var r : rows) {
     sb.append("  <team>\n");
     sb.append("    <name>").append(r.teamName()).append("</name>\n");
     sb.append("    <played>").append(r.played()).append("</played>\n");
     sb.append("    <won>").append(r.won()).append("</won>\n");
     sb.append("    <drawn>").append(r.drawn()).append("</drawn>\n");
     sb.append("    <lost>").append(r.lost()).append("</lost>\n");
     sb.append("    <gf>").append(r.goalsFor()).append("</gf>\n");
     sb.append("    <ga>").append(r.goalsAgainst()).append("</ga>\n");
     sb.append("    <pts>").append(r.points()).append("</pts>\n");
     sb.append("  </team>\n");
 }
 return sb.append("</leagueTable>").toString();
}

public static void insertRandomExtraPlayers(PlayerDAO playerDAO,
        List<Team> teams,
        int count) {
var rnd = new Random();
var positions = Position.values();

for (int i = 0; i < count; i++) {
var t = teams.get(rnd.nextInt(teams.size()));
var pos = positions[rnd.nextInt(positions.length)];
var p = new Player("LoadPlayer" + i,
pos,
18 + rnd.nextInt(15),
t.id());
playerDAO.addPlayer(p);
}
}


//---------------- RECORD TYPES ----------------

public record Match(int round, Team home, Team away, int homeGoals, int awayGoals) {}
public record LeagueRow(String teamName, int played, int won, int drawn, int lost, int goalsFor, int goalsAgainst, int points) {}
public record PlayerStats(String playerName, String teamName, int goals, int assists, double rating) {}
}
