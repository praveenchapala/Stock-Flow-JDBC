
package com.stockflow.dao;

import com.stockflow.DatabaseConnection;
import com.stockflow.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ProductDAO {

    public long addProduct(Product product) throws SQLException {

        String sql = """
                INSERT INTO products (name, price, stock_quantity, active)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, product.getName());
            statement.setBigDecimal(2, product.getPrice());
            statement.setInt(3, product.getStockQuantity());
            statement.setBoolean(4, product.isActive());

            int rowsInserted = statement.executeUpdate();

            if (rowsInserted == 0) {
                throw new SQLException("Product insertion failed.");
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }

            throw new SQLException("Product inserted, but no ID was returned.");
        }
    }
}
