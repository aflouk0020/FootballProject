package ie.tus.oop1.football.dao;

import ie.tus.oop1.football.model.Coach;
import ie.tus.oop1.football.util.DataAccessRuntimeException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoachDAO {

    private boolean coachExists(Connection conn, String name, int teamId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM coaches WHERE name=? AND team_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name); ps.setInt(2, teamId);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1) > 0; }
        }
    }

    public boolean addCoach(Coach coach) {
        final String sql = "INSERT INTO coaches(name, age, team_id) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.get()) {
            if (coachExists(conn, coach.getName(), coach.id())) return false;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, coach.getName()); ps.setInt(2, coach.getAge()); ps.setInt(3, coach.id());
                ps.executeUpdate();
            }
            return true;
        } catch (SQLException e) { throw new DataAccessRuntimeException("Insert coach failed", e); }
    }

    public void updateCoach(Coach coach) {
        final String sql = "UPDATE coaches SET name=?, age=? WHERE id=?";
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, coach.getName()); ps.setInt(2, coach.getAge()); ps.setInt(3, coach.id());
            ps.executeUpdate();
        } catch (SQLException e) { throw new DataAccessRuntimeException("Update coach failed", e); }
    }

    public List<Coach> getAllCoaches() {
        final String sql = "SELECT id, name, age, team_id FROM coaches ORDER BY team_id";
        List<Coach> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.get(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(new Coach(rs.getInt("id"), rs.getString("name"), rs.getInt("age"), rs.getInt("team_id")));
            return list;
        } catch (SQLException e) { throw new DataAccessRuntimeException("Fetch coaches failed", e); }
    }

    public void deleteCoachByName(String name) {
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement("DELETE FROM coaches WHERE name=?")) {
            ps.setString(1, name); ps.executeUpdate();
        } catch (SQLException e) { throw new DataAccessRuntimeException("Delete coach failed", e); }
    }
}
