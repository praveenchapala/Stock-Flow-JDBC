
package com.stockflow.service;

import com.stockflow.dao.ProductDAO;
import com.stockflow.model.Product;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProductServiceMockitoTest {

    private ProductDAO productDAO;
    private ProductService productService;

    @Before
    public void setUp() {
        productDAO = mock(ProductDAO.class);
        productService = new ProductService(productDAO);
    }

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

    @Test
    public void deleteProductShouldReturnTrueWhenSuccessful()
            throws Exception {

        when(productDAO.deleteProduct(1L))
                .thenReturn(true);

        boolean deleted = productService.deleteProduct(1L);

        assertTrue(deleted);

        verify(productDAO).deleteProduct(1L);
    }
}
