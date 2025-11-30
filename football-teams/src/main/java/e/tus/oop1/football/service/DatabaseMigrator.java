package e.tus.oop1.football.service;



import ie.tus.oop1.football.dao.DatabaseConnection;

import java.sql.Connection;
import java.sql.Statement;

public final class DatabaseMigrator {
    private DatabaseMigrator() {}

    
    public static void run() {
        try (Connection c = DatabaseConnection.get(); Statement s = c.createStatement()) {
            // Teams
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS teams (
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  name VARCHAR(100) NOT NULL UNIQUE,
                  city VARCHAR(100) NOT NULL,
                  founded_year INT NOT NULL
                )
            """);

            // Players
            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS players (
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  name VARCHAR(100) NOT NULL,
                  position VARCHAR(30) NOT NULL,
                  age INT NOT NULL,
                  team_id INT NOT NULL,
                  CONSTRAINT fk_players_team
                    FOREIGN KEY (team_id) REFERENCES teams(id)
                    ON DELETE CASCADE
                )
            """);

            // Coaches

            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS coaches (
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  name VARCHAR(100) NOT NULL,
                  age INT NOT NULL,
                  team_id INT NOT NULL UNIQUE,
                  CONSTRAINT fk_coaches_team
                    FOREIGN KEY (team_id) REFERENCES teams(id)
                    ON DELETE CASCADE
                )
            """);

        } catch (Exception e) {
            System.err.println("Schema migration failed: " + e.getMessage());
        }
    }
}

