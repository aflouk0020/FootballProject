package e.tus.oop1.football.service;

import ie.tus.oop1.football.app.Main;
import ie.tus.oop1.football.dao.CoachDAO;
import ie.tus.oop1.football.dao.PlayerDAO;
import ie.tus.oop1.football.dao.TeamDAO;
import ie.tus.oop1.football.model.Team;

import java.util.List;

public class LeagueService {

    private final TeamDAO teamDAO = new TeamDAO();
    private final PlayerDAO playerDAO = new PlayerDAO();
    private final CoachDAO coachDAO = new CoachDAO();


    public void preloadDemoData() {
  
    }
    

    
    public List<Main.Match> simulateLeague() {
        List<Team> teams = teamDAO.getAllTeams();
        return Main.runSimulationAndReturnFixtures(teams);
    }

    public List<Main.LeagueRow> generateLeagueTable(List<Main.Match> fixtures) {
        List<Team> teams = teamDAO.getAllTeams();
        return Main.computeLeagueTable(teams, fixtures);
    }

    public String exportLeagueJson(List<Main.LeagueRow> table) {
        return Main.exportLeagueTableToJson(table);
    }

    public String exportLeagueXml(List<Main.LeagueRow> table) {
        return Main.exportLeagueTableToXml(table);
    }

    public void insertLoadTestPlayers(int count) {
        List<Team> teams = teamDAO.getAllTeams();
        Main.insertRandomExtraPlayers(playerDAO, teams, count);
    }
}
