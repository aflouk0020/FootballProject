package ie.tus.oop1.football.dao;

import ie.tus.oop1.football.model.*;
import ie.tus.oop1.football.util.DataAccessRuntimeException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TeamDAO {

    private boolean teamExists(Connection conn, String name) throws SQLException {
        String sql = "SELECT COUNT(*) FROM teams WHERE name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1) > 0; }
        }
    }

    public int addTeamAndReturnId(Team t) { return addTeamAndReturnId(t.getName(), t.getCity(), t.getFoundedYear()); }

    public int addTeamAndReturnId(String name, String city, int foundedYear) {
        final String sqlInsert = "INSERT INTO teams(name, city, founded_year) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.get()) {
            if (teamExists(conn, name)) {
                try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM teams WHERE name=?")) {
                    ps.setString(1, name); try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
                }
                return -1;
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name); ps.setString(2, city); ps.setInt(3, foundedYear); ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) return rs.getInt(1); }
                throw new SQLException("No generated key");
            }
        } catch (SQLException e) { throw new DataAccessRuntimeException("Failed to insert team", e); }
    }

    public void updateTeam(int id, String name, String city, int foundedYear) {
        final String sql = "UPDATE teams SET name=?, city=?, founded_year=? WHERE id=?";
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name); ps.setString(2, city); ps.setInt(3, foundedYear); ps.setInt(4, id); ps.executeUpdate();
        } catch (SQLException e) { throw new DataAccessRuntimeException("Failed to update team", e); }
    }

    public List<Team> getAllTeamsWithPlayers() {
        List<Team> teams = getAllTeams();
        final String sql = "SELECT p.id, p.name, p.position, p.age, p.team_id FROM players p WHERE p.team_id=? ORDER BY p.name";
        try (Connection conn = DatabaseConnection.get(); PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Team t : teams) {
                List<Player> players = new ArrayList<>();
                ps.setInt(1, t.id());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        players.add(new Player(rs.getInt("id"), rs.getString("name"),
                                Position.valueOf(rs.getString("position").toUpperCase()),
                                rs.getInt("age"), rs.getInt("team_id")));
                    }
                }
                t.addPlayers(players.toArray(new Player[0]));
            }
            return teams;
        } catch (SQLException e) { throw new DataAccessRuntimeException("Failed to fetch teams with players", e); }
    }

    public List<Team> getAllTeams() {
        final String sql = "SELECT id, name, city, founded_year FROM teams ORDER BY name";
        List<Team> result = new ArrayList<>();
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(new Team(rs.getInt("id"), rs.getString("name"), rs.getString("city"), rs.getInt("founded_year")));
            return result;
        } catch (SQLException e) { throw new DataAccessRuntimeException("Failed to fetch teams", e); }
    }

    /** Uses FK constraints with ON DELETE CASCADE (see DatabaseMigrator). */
    public void deleteTeamByIdCascade(int id) {
        final String sql = "DELETE FROM teams WHERE id=?";
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id); ps.executeUpdate();
        } catch (SQLException e) { throw new DataAccessRuntimeException("Failed to delete team", e); }
    }

    @Deprecated public void deleteTeamByName(String name) {
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement("DELETE FROM teams WHERE name=?")) {
            ps.setString(1, name); ps.executeUpdate();
        } catch (SQLException e) { throw new DataAccessRuntimeException("Failed to delete team by name", e); }
    }
}

