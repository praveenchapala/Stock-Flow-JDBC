
package com.stockflow.service;

import java.math.BigDecimal;
import java.sql.SQLException;

import com.stockflow.dao.ProductDAO;
import com.stockflow.model.Product;

public class ProductService {

    private final ProductDAO productDAO;

    public ProductService() {
        this.productDAO = new ProductDAO();
    }

    public long addProduct(String name, BigDecimal price,
                           int stockQuantity) throws SQLException {

        // Validate product name
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Product name cannot be empty.");
        }

        // Validate price
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Product price cannot be negative.");
        }

        // Validate stock quantity
        if (stockQuantity < 0) {
            throw new IllegalArgumentException(
                    "Stock quantity cannot be negative.");
        }

        Product product = new Product(
                name.trim(), price, stockQuantity);

        return productDAO.addProduct(product);
    }
}
