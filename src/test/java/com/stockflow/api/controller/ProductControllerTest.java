
package com.stockflow.api.controller;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.stockflow.model.Product;
import com.stockflow.service.ProductService;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void getAllProductsShouldReturnProducts() throws Exception {
        Product product = new Product(
                1L,
                "Wireless Mouse",
                new BigDecimal("799.00"),
                25,
                true
        );

        when(productService.getAllProducts())
                .thenReturn(List.of(product));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Wireless Mouse"))
                .andExpect(jsonPath("$[0].stockQuantity").value(25));
    }

    @Test
    void addProductShouldReturnCreated() throws Exception {
        when(productService.addProduct(
                eq("Wireless Mouse"),
                eq(new BigDecimal("799.00")),
                eq(25)
        )).thenReturn(101L);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "Wireless Mouse",
                          "price": 799.00,
                          "stockQuantity": 25
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(content().string("101"));
    }

    @Test
    void addProductShouldRejectBlankName() throws Exception {
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "   ",
                          "price": 799.00,
                          "stockQuantity": 25
                        }
                        """))
                .andExpect(status().isBadRequest());

        verify(productService, never())
                .addProduct(any(), any(), eq(25));
    }

    @Test
    void updateProductShouldReturnNoContent() throws Exception {
        when(productService.updateProduct(
                eq(1L),
                eq("Wireless Mouse"),
                eq(new BigDecimal("799.00")),
                eq(25)
        )).thenReturn(true);

        mockMvc.perform(put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "Wireless Mouse",
                          "price": 799.00,
                          "stockQuantity": 25
                        }
                        """))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateProductShouldReturnNotFound() throws Exception {
        when(productService.updateProduct(
                eq(999L),
                eq("Wireless Mouse"),
                eq(new BigDecimal("799.00")),
                eq(25)
        )).thenReturn(false);

        mockMvc.perform(put("/api/products/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "Wireless Mouse",
                          "price": 799.00,
                          "stockQuantity": 25
                        }
                        """))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProductShouldReturnNoContent() throws Exception {
        when(productService.deleteProduct(1L))
                .thenReturn(true);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());

        verify(productService).deleteProduct(1L);
    }

    @Test
    void deleteProductShouldReturnNotFound() throws Exception {
        when(productService.deleteProduct(999L))
                .thenReturn(false);

        mockMvc.perform(delete("/api/products/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProductShouldRejectZeroId() throws Exception {
        mockMvc.perform(delete("/api/products/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Product ID must be greater than zero."));
    }

    @Test
    void deleteProductShouldRejectNonNumericId() throws Exception {
        mockMvc.perform(delete("/api/products/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Invalid value for parameter: id"));
    }
}
