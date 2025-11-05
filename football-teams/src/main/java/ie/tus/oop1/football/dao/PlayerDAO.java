package ie.tus.oop1.football.dao;

import ie.tus.oop1.football.model.Player;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlayerDAO {

    // helper method check if player already exists in the same team
    private boolean playerExists(String name, int teamId) {
        String sql = "SELECT COUNT(*) FROM players WHERE LOWER(name) = LOWER(?) AND team_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setInt(2, teamId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error checking player existence: " + e.getMessage());
        }
        return false;
    }

    // createa  new player
    public void addPlayer(Player player) {
        if (playerExists(player.getName(), player.getTeamId())) {
            System.out.printf("Player '%s' already exists in team ID %d. Skipping insert.%n",
                    player.getName(), player.getTeamId());
            return;
        }

        String sql = "INSERT INTO players (name, position, age, team_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, player.getName());
            stmt.setString(2, player.getPosition());
            stmt.setInt(3, player.getAge());
            stmt.setInt(4, player.getTeamId());
            stmt.executeUpdate();
            System.out.println("✅ Player added successfully!");

        } catch (SQLException e) {
            System.err.println("Error adding player: " + e.getMessage());
        }
    }

    // read  all players for one team
    public List<Player> getPlayersByTeamId(int teamId) {
        List<Player> players = new ArrayList<>();
        String sql = "SELECT * FROM players WHERE team_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, teamId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                players.add(new Player(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("position"),
                        rs.getInt("age"),
                        rs.getInt("team_id")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Error reading players: " + e.getMessage());
        }
        return players;
    }

    // read a  single player by ID
    public Player getPlayerById(int id) {
        String sql = "SELECT * FROM players WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Player(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("position"),
                        rs.getInt("age"),
                        rs.getInt("team_id")
                );
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving player: " + e.getMessage());
        }
        return null;
    }

    // updagte to  modify existing player details
    public void updatePlayer(Player player) {
        String sql = "UPDATE players SET name = ?, position = ?, age = ?, team_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, player.getName());
            stmt.setString(2, player.getPosition());
            stmt.setInt(3, player.getAge());
            stmt.setInt(4, player.getTeamId());
            stmt.setInt(5, player.getId());

            int rows = stmt.executeUpdate();

            if (rows > 0)
                System.out.println("Player updated successfully!");
            else
                System.out.println(" No player found with ID " + player.getId());

        } catch (SQLException e) {
            System.err.println("Error updating player: " + e.getMessage());
        }
    }

    // delete to  remove player by ID
    public void deletePlayer(int id) {
        String sql = "DELETE FROM players WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();

            if (rows > 0)
                System.out.println("Player deleted successfully!");
            else
                System.out.println("No player found with ID " + id);

        } catch (SQLException e) {
            System.err.println("Error deleting player: " + e.getMessage());
        }
    }

    // read all players (optional, across all teams)
    public List<Player> getAllPlayers() {
        List<Player> players = new ArrayList<>();
        String sql = "SELECT * FROM players";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                players.add(new Player(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("position"),
                        rs.getInt("age"),
                        rs.getInt("team_id")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Error reading all players: " + e.getMessage());
        }
        return players;
    }
}
