
package com.stockflow.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.stockflow.model.Product;

public class ProductDAOIntegrationTest {

    private static final String REQUIRED_DATABASE = "stockflow_test";
    private static final String TEST_PREFIX = "DAO_IT_";

    private DataSource dataSource;
    private ProductDAO productDAO;

    private final List<Long> createdProductIds = new ArrayList<>();

    @Before
    public void setUp() throws SQLException {
        String username = getRequiredEnvironmentVariable("STOCKFLOW_DB_USER");
        String password = getRequiredEnvironmentVariable("STOCKFLOW_DB_PASSWORD");

        DriverManagerDataSource testDataSource =
                new DriverManagerDataSource();

        testDataSource.setUrl(
                "jdbc:mysql://localhost:3306/" + REQUIRED_DATABASE);
        testDataSource.setUsername(username);
        testDataSource.setPassword(password);
        testDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");

        dataSource = testDataSource;

        // Verify the database before performing test operations.
        try (Connection connection = dataSource.getConnection()) {
            assertEquals(
                    "Integration tests must run against "
                            + REQUIRED_DATABASE,
                    REQUIRED_DATABASE,
                    connection.getCatalog());
        }

        productDAO = new ProductDAO(dataSource);
    }

    @After
    public void cleanUp() throws SQLException {
        if (dataSource == null || createdProductIds.isEmpty()) {
            return;
        }

        try (Connection connection = dataSource.getConnection()) {
            String actualDatabase = connection.getCatalog();

            assertEquals(
                    "Refusing to clean up: connected to an unexpected database.",
                    REQUIRED_DATABASE,
                    actualDatabase);

            String sql = "DELETE FROM products WHERE id = ?";

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {
                for (Long productId : createdProductIds) {
                    statement.setLong(1, productId);
                    statement.executeUpdate();
                }
            }
        }
    }

    private static String getRequiredEnvironmentVariable(String variable) {
        String value = System.getenv(variable);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Missing required environment variable: " + variable);
        }

        return value.trim();
    }

    private long insertTestProduct(
            String name, BigDecimal price, int quantity) throws SQLException {

        Product product = new Product(name, price, quantity);
        long generatedId = productDAO.addProduct(product);

        assertTrue("Generated product ID must be positive.", generatedId > 0);
        createdProductIds.add(generatedId);

        return generatedId;
    }

    @Test
    public void addProductShouldReturnGeneratedId() throws SQLException {
        String name = TEST_PREFIX + UUID.randomUUID();

        long id = insertTestProduct(
                name, new BigDecimal("499.99"), 10);

        List<Product> products = productDAO.getAllProducts();

        Product savedProduct = products.stream()
                .filter(product -> product.getId() == id)
                .findFirst()
                .orElse(null);

        assertNotNull("Inserted product should be retrievable.", savedProduct);
        assertEquals(name, savedProduct.getName());
        assertEquals(
                0,
                new BigDecimal("499.99").compareTo(savedProduct.getPrice()));
        assertEquals(10, savedProduct.getStockQuantity());
        assertTrue(savedProduct.isActive());
    }

    @Test
    public void getAllProductsShouldIncludeInsertedProduct()
            throws SQLException {
        String name = TEST_PREFIX + UUID.randomUUID();

        long id = insertTestProduct(
                name, new BigDecimal("199.00"), 5);

        List<Product> products = productDAO.getAllProducts();

        assertTrue(
                "Results should include the inserted product.",
                products.stream().anyMatch(product ->
                        product.getId() == id
                                && product.getName().equals(name)));
    }

    @Test
    public void updateProductShouldPersistChanges() throws SQLException {
        String originalName = TEST_PREFIX + UUID.randomUUID();

        long id = insertTestProduct(
                originalName, new BigDecimal("100.00"), 5);

        Product updatedProduct = new Product(
                Math.toIntExact(id),
                originalName + "_UPDATED",
                new BigDecimal("150.00"),
                20,
                true);

        assertTrue(
                "An existing product should be updated.",
                productDAO.updateProduct(updatedProduct));

        Product savedProduct = productDAO.getAllProducts().stream()
                .filter(product -> product.getId() == id)
                .findFirst()
                .orElse(null);

        assertNotNull(savedProduct);
        assertEquals(originalName + "_UPDATED", savedProduct.getName());
        assertEquals(
                0,
                new BigDecimal("150.00").compareTo(savedProduct.getPrice()));
        assertEquals(20, savedProduct.getStockQuantity());
    }

    @Test
    public void deleteProductShouldRemoveProduct() throws SQLException {
        long id = insertTestProduct(
                TEST_PREFIX + UUID.randomUUID(),
                new BigDecimal("75.00"),
                3);

        assertTrue(
                "An existing product should be deleted.",
                productDAO.deleteProduct(id));

        assertFalse(
                "Deleted product should not appear in results.",
                productDAO.getAllProducts().stream()
                        .anyMatch(product -> product.getId() == id));

        createdProductIds.remove(id);
    }

    @Test
    public void updateProductShouldReturnFalseForMissingId()
            throws SQLException {
        Product missingProduct = new Product(
                Integer.MAX_VALUE,
                TEST_PREFIX + "MISSING",
                new BigDecimal("50.00"),
                1,
                true);

        assertFalse(productDAO.updateProduct(missingProduct));
    }

    @Test
    public void deleteProductShouldReturnFalseForMissingId()
            throws SQLException {
        assertFalse(productDAO.deleteProduct(Integer.MAX_VALUE));
    }
}
