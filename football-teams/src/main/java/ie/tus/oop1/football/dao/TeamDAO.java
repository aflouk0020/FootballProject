package ie.tus.oop1.football.dao;

import ie.tus.oop1.football.model.Team;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeamDAO {

    // helper method: check if team already exists (by name + city)
    private boolean teamExists(String name, String city) {
        String sql = "SELECT COUNT(*) FROM teams WHERE LOWER(name) = LOWER(?) AND LOWER(city) = LOWER(?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, name);
            stmt.setString(2, city);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;  // true if any row matches
            }

        } catch (SQLException e) {
            System.err.println("Error checking for duplicate team: " + e.getMessage());
        }
        return false;
    }
 // find team by name and city
    public Team getTeamByNameAndCity(String name, String city) {
        String sql = "SELECT * FROM teams WHERE LOWER(name) = LOWER(?) AND LOWER(city) = LOWER(?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, city);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Team(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("city"),
                    rs.getInt("founded_year")
                );
            }
        } catch (SQLException e) {
            System.err.println(" Error fetching team by name and city: " + e.getMessage());
        }
        return null;
    }

 // return the inserted team’s ID (or existing one)
    public int addTeamAndReturnId(Team team) {
        if (teamExists(team.getName(), team.getCity())) {
            Team existing = getTeamByNameAndCity(team.getName(), team.getCity());
            if (existing != null) return existing.getId();
            return -1;
        }

        String sql = "INSERT INTO teams (name, city, founded_year) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, team.getName());
            stmt.setString(2, team.getCity());
            stmt.setInt(3, team.getFoundedYear());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1); // newly inserted team ID
            }
        } catch (SQLException e) {
            System.err.println(" Error adding team: " + e.getMessage());
        }
        return -1;
    }


    // read all teams
    public List<Team> getAllTeams() {
        List<Team> teams = new ArrayList<>();
        String sql = "SELECT * FROM teams";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Team team = new Team(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("city"),
                    rs.getInt("founded_year")
                );
                teams.add(team);
            }

        } catch (SQLException e) {
            System.err.println(" Error reading teams: " + e.getMessage());
        }
        return teams;
    }

 //  read all teams with their players (joined data)
    public List<Team> getAllTeamsWithPlayers() {
        PlayerDAO playerDAO = new PlayerDAO();  
        List<Team> teams = getAllTeams();      

        // for each team, load its player list
        for (Team team : teams) {
            team.setPlayers(playerDAO.getPlayersByTeamId(team.getId()));
        }

        return teams;
    }

    
    // read single team by ID
    public Team getTeamById(int id) {
        String sql = "SELECT * FROM teams WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Team(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("city"),
                    rs.getInt("founded_year")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving team: " + e.getMessage());
        }
        return null;
    }

    // update existing team
    public void updateTeam(Team team) {
        String sql = "UPDATE teams SET name = ?, city = ?, founded_year = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, team.getName());
            stmt.setString(2, team.getCity());
            stmt.setInt(3, team.getFoundedYear());
            stmt.setInt(4, team.getId());
            int rows = stmt.executeUpdate();

            if (rows > 0)
                System.out.println(" Team updated successfully!");
            else
                System.out.println("No team found with ID " + team.getId());

        } catch (SQLException e) {
            System.err.println("Error updating team: " + e.getMessage());
        }
    }

    // delete team
    public void deleteTeam(int id) {
        String sql = "DELETE FROM teams WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();

            if (rows > 0)
                System.out.println("Team deleted successfully!");
            else
                System.out.println("No team found with ID " + id);

        } catch (SQLException e) {
            System.err.println("Error deleting team: " + e.getMessage());
        }
    }
}
