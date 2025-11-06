package ie.tus.oop1.football.dao;

import ie.tus.oop1.football.model.Player;
import ie.tus.oop1.football.model.Position;
import ie.tus.oop1.football.util.DataAccessRuntimeException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlayerDAO {

    private boolean playerExists(Connection conn, String name, int teamId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM players WHERE name = ? AND team_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public boolean addPlayer(Player p) {
        final String sqlInsert = "INSERT INTO players(name, position, age, team_id) VALUES(?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.get()) {
            if (playerExists(conn, p.getName(), p.getTeamId())) {
                System.out.println("ℹ️ Player already exists: " + p.getName());
                return false;
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, p.getName());
                ps.setString(2, p.getPosition().name().toUpperCase());
                ps.setInt(3, p.getAge());
                ps.setInt(4, p.getTeamId());
                ps.executeUpdate();
                System.out.println("✅ Player added: " + p.getName());
            }
            return true;
        } catch (SQLException e) {
            throw new DataAccessRuntimeException("Failed to insert or check player", e);
        }
    }

    public List<Player> getAllPlayers() {
        final String sql = "SELECT id, name, position, age, team_id FROM players ORDER BY name";
        List<Player> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new Player(
                        rs.getInt("id"),
                        rs.getString("name"),
                        Position.valueOf(rs.getString("position").toUpperCase()),
                        rs.getInt("age"),
                        rs.getInt("team_id")));
            }
            return result;
        } catch (SQLException e) {
            throw new DataAccessRuntimeException("Failed to fetch players", e);
        }
    }
}
