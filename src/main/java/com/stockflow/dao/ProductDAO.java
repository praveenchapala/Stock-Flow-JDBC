
package com.stockflow.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.stockflow.DatabaseConnection;
import com.stockflow.model.Product;

public class ProductDAO {

    // CREATE: Add a product and return its generated ID
    public long addProduct(Product product) throws SQLException {

        String sql = """
                INSERT INTO products
                    (name, price, stock_quantity, active)
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

            if (rowsInserted != 1) {
                throw new SQLException(
                        "Expected one inserted product, but got "
                                + rowsInserted + "."
                );
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }

            throw new SQLException(
                    "Product was inserted, but no generated ID was returned."
            );
        }
    }

    // READ: Retrieve all products
    public List<Product> getAllProducts() throws SQLException {

        String sql = """
                SELECT id, name, price, stock_quantity, active
                FROM products
                ORDER BY id
                """;

        List<Product> products = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Product product = new Product(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getBigDecimal("price"),
                        resultSet.getInt("stock_quantity"),
                        resultSet.getBoolean("active")
                );

                products.add(product);
            }
        }

        return products;
    }

    // UPDATE: Update an existing product
    public boolean updateProduct(Product product) throws SQLException {

        String sql = """
                UPDATE products
                SET name = ?, price = ?, stock_quantity = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, product.getName());
            statement.setBigDecimal(2, product.getPrice());
            statement.setInt(3, product.getStockQuantity());
            statement.setLong(4, product.getId());

            return statement.executeUpdate() > 0;
        }
    }

    // DELETE: Delete a product by ID
    public boolean deleteProduct(long id) throws SQLException {

        String sql = "DELETE FROM products WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            return statement.executeUpdate() > 0;
        }
    }
}
