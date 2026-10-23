
package com.stockflow.api.controller;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stockflow.model.Product;
import com.stockflow.service.ProductService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/products")
@Validated
public class ProductController {

    private final ProductService productService;

    // Spring injects the ProductService bean
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // READ: Retrieve all products
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts()
            throws SQLException {

        return ResponseEntity.ok(
                productService.getAllProducts()
        );
    }

    // CREATE: Add a product
    @PostMapping
    public ResponseEntity<Long> addProduct(
            @Valid @RequestBody CreateProductRequest request)
            throws SQLException {

        long id = productService.addProduct(
                request.name(),
                request.price(),
                request.stockQuantity()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(id);
    }

    // UPDATE: Update an existing product
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateProduct(
            @PathVariable @Positive(message =
                    "Product ID must be greater than zero.") long id,
            @Valid @RequestBody CreateProductRequest request)
            throws SQLException {

        boolean updated = productService.updateProduct(
                id,
                request.name(),
                request.price(),
                request.stockQuantity()
        );

        if (!updated) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    // DELETE: Delete an existing product
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable @Positive(message =
                    "Product ID must be greater than zero.") long id)
            throws SQLException {

        boolean deleted = productService.deleteProduct(id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    // Request DTO with validation rules
    public record CreateProductRequest(

            @NotBlank(message = "Product name cannot be empty.")
            @Size(
                    max = 150,
                    message = "Product name cannot exceed 150 characters."
            )
            String name,

            @NotNull(message = "Product price is required.")
            @DecimalMin(
                    value = "0.00",
                    message = "Product price cannot be negative."
            )
            BigDecimal price,

            @NotNull(message = "Stock quantity is required.")
            @PositiveOrZero(
                    message = "Stock quantity cannot be negative."
            )
            Integer stockQuantity

    ) {
    }
}
