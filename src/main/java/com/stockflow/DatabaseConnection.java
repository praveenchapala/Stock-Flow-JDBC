
package com.stockflow;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseConnection {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/stockflow";

    private static final String URL =
            getEnvironmentValue("STOCKFLOW_DB_URL", DEFAULT_URL);

    private static final String USER =
            System.getenv("STOCKFLOW_DB_USER");

    private static final String PASSWORD =
            System.getenv("STOCKFLOW_DB_PASSWORD");

    private DatabaseConnection() {
        // Utility class: prevent object creation.
    }

    private static String getEnvironmentValue(
            String variable,
            String defaultValue) {

        String value = System.getenv(variable);

        return value == null || value.isBlank()
                ? defaultValue
                : value.trim();
    }

    /**
     * Creates a JDBC connection to the configured MySQL database.
     */
    public static Connection getConnection() throws SQLException {

        if (USER == null || USER.isBlank()
                || PASSWORD == null || PASSWORD.isBlank()) {

            throw new IllegalStateException(
                    "Database credentials are missing. Set "
                    + "STOCKFLOW_DB_USER and STOCKFLOW_DB_PASSWORD."
            );
        }

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Checks connectivity and displays database identification details.
     * Never prints database credentials.
     */
    public static void main(String[] args) {

        try (Connection connection = getConnection()) {

            System.out.println("MySQL connection successful.");
            System.out.println("Configured JDBC URL: " + URL);
            System.out.println("Connected database: "
                    + connection.getCatalog());

            String sql = """
                    SELECT @@hostname AS hostname,
                           @@port AS port,
                           DATABASE() AS database_name
                    """;

            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {

                if (resultSet.next()) {
                    System.out.println("MySQL hostname: "
                            + resultSet.getString("hostname"));

                    System.out.println("MySQL port: "
                            + resultSet.getInt("port"));

                    System.out.println("Current database: "
                            + resultSet.getString("database_name"));
                }
            }

            // Verify the products table and show its record count.
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(
                         "SELECT COUNT(*) FROM products")) {

                if (resultSet.next()) {
                    System.out.println("Total products: "
                            + resultSet.getLong(1));
                }
            }

        } catch (SQLException | IllegalStateException e) {
            System.err.println(
                    "Database connection failed: " + e.getMessage());
        }
    }
}
