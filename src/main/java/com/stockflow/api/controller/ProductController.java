package com.stockflow.api.controller;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@CrossOrigin(origins = "http://localhost:5173")
@Tag(
    name = "Products",
    description = "APIs for managing inventory products"
)
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // READ: Retrieve all products.
    @Operation(
        summary = "Get all products",
        description = "Retrieves all products from the inventory."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Products retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(
                    schema = @Schema(implementation = Product.class)
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class),
                examples = @ExampleObject(
                    name = "UnexpectedServerError",
                    value = "{\"error\":\"An unexpected server error occurred.\"}"
                )
            )
        )
    })
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts()
            throws SQLException {

        return ResponseEntity.ok(
            productService.getAllProducts()
        );
    }

    // CREATE: Add a product.
    @Operation(
        summary = "Create a product",
        description = "Creates a product and returns its generated ID."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Product created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    type = "integer",
                    format = "int64",
                    example = "101"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid product data",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class),
                examples = {
                    @ExampleObject(
                        name = "EmptyProductName",
                        value = "{\"error\":\"Product name cannot be empty.\"}"
                    ),
                    @ExampleObject(
                        name = "NegativePrice",
                        value = "{\"error\":\"Product price cannot be negative.\"}"
                    ),
                    @ExampleObject(
                        name = "NegativeStock",
                        value = "{\"error\":\"Stock quantity cannot be negative.\"}"
                    ),
                    @ExampleObject(
                        name = "InvalidJson",
                        value = "{\"error\":\"Request body is missing or contains invalid JSON.\"}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server or database error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class),
                examples = {
                    @ExampleObject(
                        name = "UnexpectedServerError",
                        value = "{\"error\":\"An unexpected server error occurred.\"}"
                    ),
                    @ExampleObject(
                        name = "DatabaseError",
                        value = "{\"error\":\"A database error occurred. Please try again later.\"}"
                    )
                }
            )
        )
    })
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

    // UPDATE: Update an existing product.
    @Operation(
        summary = "Update a product",
        description = "Updates an existing product using its ID."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Product updated successfully"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid product ID or request data",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class),
                examples = {
                    @ExampleObject(
                        name = "InvalidProductId",
                        value = "{\"error\":\"Product ID must be greater than zero.\"}"
                    ),
                    @ExampleObject(
                        name = "InvalidProductData",
                        value = "{\"error\":\"Product price cannot be negative.\"}"
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    description = "The controller returns an empty body when the product does not exist."
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server or database error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class),
                examples = {
                    @ExampleObject(
                        name = "UnexpectedServerError",
                        value = "{\"error\":\"An unexpected server error occurred.\"}"
                    ),
                    @ExampleObject(
                        name = "DatabaseError",
                        value = "{\"error\":\"A database error occurred. Please try again later.\"}"
                    )
                }
            )
        )
    })
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateProduct(
            @PathVariable
            @Positive(message = "Product ID must be greater than zero.")
            long id,
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

    // DELETE: Delete an existing product.
    @Operation(
        summary = "Delete a product",
        description = "Deletes an existing product using its ID."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Product deleted successfully"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid product ID",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class),
                examples = @ExampleObject(
                    name = "InvalidProductId",
                    value = "{\"error\":\"Product ID must be greater than zero.\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    description = "The controller returns an empty body when the product does not exist."
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server or database error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiError.class),
                examples = {
                    @ExampleObject(
                        name = "UnexpectedServerError",
                        value = "{\"error\":\"An unexpected server error occurred.\"}"
                    ),
                    @ExampleObject(
                        name = "DatabaseError",
                        value = "{\"error\":\"A database error occurred. Please try again later.\"}"
                    )
                }
            )
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable
            @Positive(message = "Product ID must be greater than zero.")
            long id)
            throws SQLException {

        boolean deleted = productService.deleteProduct(id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    // Request DTO with validation rules.
    @Schema(
        name = "CreateProductRequest",
        description = "Request data required to create or update a product."
    )
    public record CreateProductRequest(

        @Schema(
            description = "Product name; cannot be blank",
            example = "Wireless Mouse",
            minLength = 1,
            maxLength = 150,
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Product name cannot be empty.")
        @Size(
            max = 150,
            message = "Product name cannot exceed 150 characters."
        )
        String name,

        @Schema(
            description = "Product price; must be zero or greater",
            example = "599.00",
            minimum = "0",
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Product price is required.")
        @DecimalMin(
            value = "0.00",
            message = "Product price cannot be negative."
        )
        BigDecimal price,

        @Schema(
            description = "Available stock quantity; must be zero or greater",
            example = "25",
            minimum = "0",
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Stock quantity is required.")
        @PositiveOrZero(
            message = "Stock quantity cannot be negative."
        )
        Integer stockQuantity

    ) {
    }

    // Standard error response schema for Swagger documentation.
    @Schema(
        name = "ApiError",
        description = "Error response returned by the API."
    )
    public record ApiError(

        @Schema(
            description = "Human-readable error message",
            example = "An unexpected server error occurred."
        )
        String error

    ) {
    }
}
