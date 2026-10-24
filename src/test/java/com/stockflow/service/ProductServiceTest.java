
package com.stockflow.service;

import java.math.BigDecimal;

import static org.junit.Assert.assertThrows;
import org.junit.Test;
import static org.mockito.Mockito.mock;

import com.stockflow.dao.ProductDAO;

public class ProductServiceTest {

    private final ProductDAO productDAO = mock(ProductDAO.class);

    private final ProductService productService =
            new ProductService(productDAO);

    @Test
    public void addProductShouldRejectEmptyName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> productService.addProduct(
                        "   ", new BigDecimal("100.00"), 10));
    }

    @Test
    public void addProductShouldRejectNegativePrice() {
        assertThrows(
                IllegalArgumentException.class,
                () -> productService.addProduct(
                        "Laptop", new BigDecimal("-100.00"), 10));
    }

    @Test
    public void addProductShouldRejectNegativeStock() {
        assertThrows(
                IllegalArgumentException.class,
                () -> productService.addProduct(
                        "Laptop", new BigDecimal("100.00"), -5));
    }

    @Test
    public void addProductShouldRejectNullName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> productService.addProduct(
                        null, new BigDecimal("100.00"), 10));
    }

    @Test
    public void addProductShouldRejectNullPrice() {
        assertThrows(
                IllegalArgumentException.class,
                () -> productService.addProduct(
                        "Laptop", null, 10));
    }

    @Test
    public void updateProductShouldRejectInvalidId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> productService.updateProduct(
                        0, "Laptop", new BigDecimal("100.00"), 10));
    }

    @Test
    public void deleteProductShouldRejectInvalidId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> productService.deleteProduct(0));
    }

    @Test
    public void deleteProductShouldRejectNegativeId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> productService.deleteProduct(-1));
    }
}
