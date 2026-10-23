
package com.stockflow.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import com.stockflow.dao.ProductDAO;
import com.stockflow.model.Product;

public class ProductService {

    private final ProductDAO productDAO;

    public ProductService() {
        this.productDAO = new ProductDAO();
    }

    // CREATE: Validate and add a product
    public long addProduct(
            String name,
            BigDecimal price,
            int stockQuantity) throws SQLException {

        validateProduct(name, price, stockQuantity);

        Product product = new Product(
                name.trim(),
                price,
                stockQuantity
        );

        return productDAO.addProduct(product);
    }

    // READ: Retrieve all products
    public List<Product> getAllProducts() throws SQLException {
        return productDAO.getAllProducts();
    }

    // UPDATE: Validate and update a product
    public boolean updateProduct(
            int id,
            String name,
            BigDecimal price,
            int stockQuantity) throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Product ID must be greater than zero.");
        }

        validateProduct(name, price, stockQuantity);

        Product product = new Product(
                id,
                name.trim(),
                price,
                stockQuantity,
                true
        );

        return productDAO.updateProduct(product);
    }

    // Shared validation for product creation and updates
    private void validateProduct(
            String name,
            BigDecimal price,
            int stockQuantity) {

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Product name cannot be empty.");
        }

        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Product price cannot be negative.");
        }

        if (stockQuantity < 0) {
            throw new IllegalArgumentException(
                    "Stock quantity cannot be negative.");
        }
    }
}
