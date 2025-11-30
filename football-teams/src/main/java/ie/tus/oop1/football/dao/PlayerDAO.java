package ie.tus.oop1.football.dao;

import ie.tus.oop1.football.model.Player;
import ie.tus.oop1.football.model.Position;
import ie.tus.oop1.football.util.DataAccessRuntimeException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlayerDAO {

    private boolean playerExists(Connection conn, String name, int teamId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM players WHERE name=? AND team_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name); ps.setInt(2, teamId);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1) > 0; }
        }
    }

    
    public boolean addPlayer(Player p) {
        final String sql = "INSERT INTO players(name, position, age, team_id) VALUES(?, ?, ?, ?)";
        try (Connection c = DatabaseConnection.get()) {
            if (playerExists(c, p.getName(), p.getTeamId())) return false;
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, p.getName()); ps.setString(2, p.getPosition().name());
                ps.setInt(3, p.getAge()); ps.setInt(4, p.getTeamId()); ps.executeUpdate();
            }
            return true;
            
        } catch (SQLException e) { throw new DataAccessRuntimeException("Insert player failed", e); }
    }

    public void updatePlayer(Player p) {
        final String sql = "UPDATE players SET name=?, position=?, age=? WHERE id=?";
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getName()); ps.setString(2, p.getPosition().name());
            ps.setInt(3, p.getAge()); ps.setInt(4, p.getIdBoxed()); ps.executeUpdate();
        } catch (SQLException e) { throw new DataAccessRuntimeException("Update player failed", e); }
    }

    public List<Player> getAllPlayers() {
        final String sql = "SELECT id, name, position, age, team_id FROM players ORDER BY name";
        List<Player> out = new ArrayList<>();
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                out.add(new Player(rs.getInt("id"), rs.getString("name"),
                        Position.valueOf(rs.getString("position").toUpperCase()),
                        rs.getInt("age"), rs.getInt("team_id")));
            return out;
        } catch (SQLException e) { throw new DataAccessRuntimeException("Fetch players failed", e); }
    }

    public void deletePlayerByName(String name) {
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement("DELETE FROM players WHERE name=?")) {
            ps.setString(1, name); ps.executeUpdate();
        } catch (SQLException e) { throw new DataAccessRuntimeException("Delete player failed", e); }
    }
}

