package ie.tus.oop1.football.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Adjust JDBC URL/creds as per your environment. */
public final class DatabaseConnection {
    private static final String URL  = "jdbc:mysql://localhost:3306/football_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "";

    private DatabaseConnection() {}

    
    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
        
    }
}
