
package com.stockflow.service;

import com.stockflow.dao.ProductDAO;
import com.stockflow.model.Product;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class ProductServiceMockitoTest {

    private ProductDAO productDAO;
    private ProductService productService;

    @Before
    public void setUp() {
        productDAO = mock(ProductDAO.class);
        productService = new ProductService(productDAO);
    }

    // CREATE: Verify generated ID
    @Test
    public void addProductShouldReturnGeneratedId() throws Exception {
        when(productDAO.addProduct(any(Product.class)))
                .thenReturn(101L);

        long productId = productService.addProduct(
                "Laptop",
                new BigDecimal("55000.00"),
                10
        );

        assertEquals(101L, productId);
        verify(productDAO).addProduct(any(Product.class));
    }

    // READ: Verify products returned by DAO
    @Test
    public void getAllProductsShouldReturnProductsFromDAO()
            throws Exception {

        List<Product> expectedProducts = Collections.emptyList();

        when(productDAO.getAllProducts())
                .thenReturn(expectedProducts);

        List<Product> actualProducts =
                productService.getAllProducts();

        assertSame(expectedProducts, actualProducts);
        verify(productDAO).getAllProducts();
    }

    // UPDATE: Verify successful update
    @Test
    public void updateProductShouldReturnTrueWhenSuccessful()
            throws Exception {

        when(productDAO.updateProduct(any(Product.class)))
                .thenReturn(true);

        boolean updated = productService.updateProduct(
                1,
                "Updated Laptop",
                new BigDecimal("60000.00"),
                15
        );

        assertTrue(updated);
        verify(productDAO).updateProduct(any(Product.class));
    }

    // DELETE: Verify successful deletion
    @Test
    public void deleteProductShouldReturnTrueWhenSuccessful()
            throws Exception {

        when(productDAO.deleteProduct(1L))
                .thenReturn(true);

        boolean deleted = productService.deleteProduct(1L);

        assertTrue(deleted);
        verify(productDAO).deleteProduct(1L);
    }

    // VALIDATION: Verify product name is trimmed
    @Test
    public void addProductShouldTrimProductName() throws Exception {
        when(productDAO.addProduct(any(Product.class)))
                .thenReturn(102L);

        productService.addProduct(
                "  Keyboard  ",
                new BigDecimal("1500.00"),
                5
        );

        ArgumentCaptor<Product> captor =
                ArgumentCaptor.forClass(Product.class);

        verify(productDAO).addProduct(captor.capture());

        assertEquals("Keyboard", captor.getValue().getName());
    }

    // BOUNDARY: Zero price and zero stock are allowed
    @Test
    public void addProductShouldAllowZeroPriceAndStock()
            throws Exception {

        when(productDAO.addProduct(any(Product.class)))
                .thenReturn(103L);

        productService.addProduct(
                "Free Sample",
                BigDecimal.ZERO,
                0
        );

        verify(productDAO).addProduct(any(Product.class));
    }

    // VALIDATION: Negative price must not reach DAO
    @Test
    public void addProductShouldRejectNegativePriceWithoutCallingDAO() {
        assertThrows(
                IllegalArgumentException.class,
                () -> productService.addProduct(
                        "Laptop",
                        new BigDecimal("-1.00"),
                        10
                )
        );

        verifyNoInteractions(productDAO);
    }

    // VALIDATION: Negative stock must not reach DAO
    @Test
    public void updateProductShouldRejectNegativeStockWithoutCallingDAO() {
        assertThrows(
                IllegalArgumentException.class,
                () -> productService.updateProduct(
                        1,
                        "Laptop",
                        new BigDecimal("100.00"),
                        -1
                )
        );

        verifyNoInteractions(productDAO);
    }

    // VALIDATION: Negative ID must not reach DAO
    @Test
    public void updateProductShouldRejectNegativeIdWithoutCallingDAO() {
        assertThrows(
                IllegalArgumentException.class,
                () -> productService.updateProduct(
                        -1,
                        "Laptop",
                        new BigDecimal("100.00"),
                        10
                )
        );

        verifyNoInteractions(productDAO);
    }

    // VALIDATION: Zero ID must not reach DAO
    @Test
    public void deleteProductShouldRejectZeroIdWithoutCallingDAO() {
        assertThrows(
                IllegalArgumentException.class,
                () -> productService.deleteProduct(0L)
        );

        verifyNoInteractions(productDAO);
    }
}
