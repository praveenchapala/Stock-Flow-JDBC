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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.stockflow.DatabaseConnection;
import com.stockflow.model.Product;

public class ProductDAOIntegrationTest {

    private static final String REQUIRED_DATABASE =
            "stockflow_test";

    private static final String TEST_PREFIX =
            "DAO_IT_";

    private ProductDAO productDAO;

    private final List<Long> createdProductIds =
            new ArrayList<>();

    @Before
    public void setUp() throws SQLException {

        // Verify the database before performing any test operations.
        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            String actualDatabase = connection.getCatalog();

            assertEquals(
                    "Safety check failed! Integration tests must "
                    + "run against stockflow_test. Actual database: "
                    + actualDatabase,
                    REQUIRED_DATABASE,
                    actualDatabase
            );
        }

        productDAO = new ProductDAO();
    }

    @After
    public void cleanUp() throws SQLException {

        // Recheck the database before deleting any test data.
        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            String actualDatabase = connection.getCatalog();

            assertEquals(
                    "Refusing to clean up: connected to an "
                    + "unexpected database.",
                    REQUIRED_DATABASE,
                    actualDatabase
            );

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

    // Helper: create a uniquely named test product.
    private long insertTestProduct(
            String name,
            BigDecimal price,
            int quantity) throws SQLException {

        Product product = new Product(
                name,
                price,
                quantity
        );

        long generatedId = productDAO.addProduct(product);

        assertTrue(
                "The generated product ID must be positive.",
                generatedId > 0
        );

        createdProductIds.add(generatedId);

        return generatedId;
    }

    // CREATE: insert a product and verify its generated ID.
    @Test
    public void addProductShouldReturnGeneratedId()
            throws SQLException {

        String name = TEST_PREFIX + UUID.randomUUID();

        long id = insertTestProduct(
                name,
                new BigDecimal("499.99"),
                10
        );

        assertTrue(id > 0);

        List<Product> products = productDAO.getAllProducts();

        Product savedProduct = products.stream()
                .filter(product -> product.getId() == id)
                .findFirst()
                .orElse(null);

        assertNotNull(
                "The inserted product should be retrievable.",
                savedProduct
        );

        assertEquals(name, savedProduct.getName());
        assertEquals(
                0,
                new BigDecimal("499.99")
                        .compareTo(savedProduct.getPrice())
        );
        assertEquals(10, savedProduct.getStockQuantity());
        assertTrue(savedProduct.isActive());
    }

    // READ: retrieve products and verify the inserted product exists.
    @Test
    public void getAllProductsShouldIncludeInsertedProduct()
            throws SQLException {

        String name = TEST_PREFIX + UUID.randomUUID();

        long id = insertTestProduct(
                name,
                new BigDecimal("199.00"),
                5
        );

        List<Product> products = productDAO.getAllProducts();

        assertTrue(
                "The returned products should include the new product.",
                products.stream()
                        .anyMatch(product ->
                                product.getId() == id
                                && product.getName().equals(name))
        );
    }

    // UPDATE: update a product and verify the changed values.
    @Test
    public void updateProductShouldPersistChanges()
            throws SQLException {

        String originalName = TEST_PREFIX + UUID.randomUUID();

        long id = insertTestProduct(
                originalName,
                new BigDecimal("100.00"),
                5
        );

        Product updatedProduct = new Product(
                (int) id,
                originalName + "_UPDATED",
                new BigDecimal("150.00"),
                20,
                true
        );

        boolean updated = productDAO.updateProduct(updatedProduct);

        assertTrue(
                "An existing product should be updated.",
                updated
        );

        Product savedProduct = productDAO.getAllProducts()
                .stream()
                .filter(product -> product.getId() == id)
                .findFirst()
                .orElse(null);

        assertNotNull(savedProduct);
        assertEquals(originalName + "_UPDATED", savedProduct.getName());
        assertEquals(
                0,
                new BigDecimal("150.00")
                        .compareTo(savedProduct.getPrice())
        );
        assertEquals(20, savedProduct.getStockQuantity());
    }

    // DELETE: delete an existing product and verify it is absent.
    @Test
    public void deleteProductShouldRemoveProduct()
            throws SQLException {

        long id = insertTestProduct(
                TEST_PREFIX + UUID.randomUUID(),
                new BigDecimal("75.00"),
                3
        );

        boolean deleted = productDAO.deleteProduct(id);

        assertTrue(
                "An existing product should be deleted.",
                deleted
        );

        assertFalse(
                "The deleted product should not appear in results.",
                productDAO.getAllProducts()
                        .stream()
                        .anyMatch(product -> product.getId() == id)
        );

        // It has already been deleted; no cleanup is needed for this ID.
        createdProductIds.remove(id);
    }

    // UPDATE: return false when the product does not exist.
    @Test
    public void updateProductShouldReturnFalseForMissingId()
            throws SQLException {

        Product missingProduct = new Product(
                Integer.MAX_VALUE,
                TEST_PREFIX + "MISSING",
                new BigDecimal("50.00"),
                1,
                true
        );

        assertFalse(
                productDAO.updateProduct(missingProduct)
        );
    }

    // DELETE: return false when the product does not exist.
    @Test
    public void deleteProductShouldReturnFalseForMissingId()
            throws SQLException {

        assertFalse(
                productDAO.deleteProduct(Integer.MAX_VALUE)
        );
    }
}
