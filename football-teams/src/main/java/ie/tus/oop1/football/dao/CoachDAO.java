package ie.tus.oop1.football.dao;

import ie.tus.oop1.football.model.Coach;
import ie.tus.oop1.football.util.DataAccessRuntimeException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class CoachDAO {

    private static final Logger LOGGER = Logger.getLogger(CoachDAO.class.getName());

    // ------------------ ADD ------------------
    
    
    public void addCoach(Coach coach) {
        try (Connection conn = DatabaseConnection.get()) {

            // Ensure one coach per team
            String checkSql = "SELECT COUNT(*) FROM coaches WHERE team_id = ?";
            try (PreparedStatement check = conn.prepareStatement(checkSql)) {
                check.setInt(1, coach.getTeamId());
                ResultSet rs = check.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new DataAccessRuntimeException("This team already has a coach assigned.", null);
                }
            }

            String insertSql = "INSERT INTO coaches (name, age, team_id) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setString(1, coach.getName());
                stmt.setInt(2, coach.getAge());
                stmt.setInt(3, coach.getTeamId());
                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            handleSQLException(e, "adding coach");
        }
    }

    // ------------------ UPDATE ------------------
    public void updateCoach(Coach coach) {
        try (Connection conn = DatabaseConnection.get()) {

            // Check if another coach already manages that team
            String checkSql = """
                SELECT c.id, c.name AS existing_coach, t.name AS team_name
                FROM coaches c
                JOIN teams t ON c.team_id = t.id
                WHERE c.team_id = ? AND c.id <> ?
            """;

            try (PreparedStatement check = conn.prepareStatement(checkSql)) {
                check.setInt(1, coach.getTeamId());
                check.setInt(2, coach.id());
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) {
                        String existingCoach = rs.getString("existing_coach");
                        String teamName = rs.getString("team_name");
                        throw new DataAccessRuntimeException(
                                "Cannot move coach '" + coach.getName() +
                                        "' to team '" + teamName +
                                        "' — that team already has coach '" + existingCoach + "'.", null);
                    }
                }
            }

            String updateSql = "UPDATE coaches SET name = ?, age = ?, team_id = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setString(1, coach.getName());
                stmt.setInt(2, coach.getAge());
                stmt.setInt(3, coach.getTeamId());
                stmt.setInt(4, coach.id());

                int updated = stmt.executeUpdate();
                if (updated == 0) {
                    throw new DataAccessRuntimeException("No coach found with that ID.", null);
                }
            }

        } catch (SQLException e) {
            handleSQLException(e, "updating coach");
        }
    }

    // ------------------ DELETE ------------------
    public void deleteCoachByName(String name) {
        try (Connection conn = DatabaseConnection.get();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM coaches WHERE name = ?")) {
            stmt.setString(1, name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            handleSQLException(e, "deleting coach");
        }
    }

    // ------------------ FETCH ------------------
    public List<Coach> getAllCoaches() {
        List<Coach> list = new ArrayList<>();
        String sql = "SELECT id, name, age, team_id FROM coaches ORDER BY team_id";

        try (Connection conn = DatabaseConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Coach(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getInt("team_id")));
            }

        } catch (SQLException e) {
            handleSQLException(e, "fetching coaches");
        }
        return list;
    }

    // ------------------ UTILS ------------------
    private void handleSQLException(SQLException e, String action) {
        if (e instanceof SQLIntegrityConstraintViolationException ||
                e.getErrorCode() == 1062 ||
                "23000".equals(e.getSQLState())) {
            throw new DataAccessRuntimeException(
                    "Cannot perform " + action + " this team already has a coach.", e);
        }
        throw new DataAccessRuntimeException("Database error while " + action + ": " + e.getMessage(), e);
    }
}
