package com.stockflow;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/stockflow";

    private static final String USER =
            System.getenv("STOCKFLOW_DB_USER");

    private static final String PASSWORD =
            System.getenv("STOCKFLOW_DB_PASSWORD");

    public static Connection getConnection() throws SQLException {

        if (USER == null || USER.isBlank()
                || PASSWORD == null || PASSWORD.isBlank()) {
            throw new IllegalStateException(
                    "Database credentials are missing. "
                    + "Set STOCKFLOW_DB_USER and STOCKFLOW_DB_PASSWORD.");
        }

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void main(String[] args) {

        try (Connection connection = getConnection()) {
            System.out.println("Connected to MySQL successfully!");
            System.out.println("Database: " + connection.getCatalog());

        } catch (SQLException | IllegalStateException e) {
            System.err.println("Database connection failed: "
                    + e.getMessage());
        }
    }
}
