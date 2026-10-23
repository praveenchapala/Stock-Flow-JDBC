
package com.stockflow.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.stockflow.dao.ProductDAO;
import com.stockflow.model.Product;

@Service
public class ProductService {

    private final ProductDAO productDAO;

    // Constructor injection
    public ProductService(ProductDAO productDAO) {
        this.productDAO = Objects.requireNonNull(
                productDAO,
                "ProductDAO cannot be null."
        );
    }

    // CREATE
    public long addProduct(
            String name,
            BigDecimal price,
            int stockQuantity
    ) throws SQLException {

        validateProduct(name, price, stockQuantity);

        Product product = new Product(
                name.trim(),
                price,
                stockQuantity
        );

        return productDAO.addProduct(product);
    }

    // READ
    public List<Product> getAllProducts() throws SQLException {
        return productDAO.getAllProducts();
    }

    // UPDATE
    public boolean updateProduct(
            long id,
            String name,
            BigDecimal price,
            int stockQuantity
    ) throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Product ID must be greater than zero."
            );
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

    // DELETE
    public boolean deleteProduct(long id) throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Product ID must be greater than zero."
            );
        }

        return productDAO.deleteProduct(id);
    }

    // Shared business validation
    private void validateProduct(
            String name,
            BigDecimal price,
            int stockQuantity
    ) {

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Product name cannot be empty."
            );
        }

        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Product price cannot be null or negative."
            );
        }

        if (stockQuantity < 0) {
            throw new IllegalArgumentException(
                    "Stock quantity cannot be negative."
            );
        }
    }
}
