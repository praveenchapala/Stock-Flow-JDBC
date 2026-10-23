
package com.stockflow;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

import com.stockflow.model.Product;
import com.stockflow.service.ProductService;

public class Main {

    public static void main(String[] args) {

        ProductService productService = new ProductService();

        try (Scanner scanner = new Scanner(System.in)) {

            boolean running = true;

            while (running) {
                System.out.println(
                        "\n===== StockFlow Inventory Management =====");
                System.out.println("1. Add Product");
                System.out.println("2. View All Products");
                System.out.println("3. Update Product");
                System.out.println("4. Exit");
                System.out.print("Choose an option: ");

                String choice = scanner.nextLine().trim();

                try {
                    switch (choice) {

                        case "1" -> {
                            System.out.print("Enter product name: ");
                            String name = scanner.nextLine();

                            System.out.print("Enter product price: ");
                            BigDecimal price = new BigDecimal(
                                    scanner.nextLine().trim());

                            System.out.print("Enter stock quantity: ");
                            int quantity = Integer.parseInt(
                                    scanner.nextLine().trim());

                            long productId = productService.addProduct(
                                    name, price, quantity);

                            System.out.println(
                                    "Product added successfully. ID: "
                                            + productId);
                        }

                        case "2" -> displayProducts(productService);

                        case "3" -> {
                            System.out.print("Enter product ID to update: ");
                            int id = Integer.parseInt(
                                    scanner.nextLine().trim());

                            System.out.print("Enter updated product name: ");
                            String name = scanner.nextLine();

                            System.out.print("Enter updated price: ");
                            BigDecimal price = new BigDecimal(
                                    scanner.nextLine().trim());

                            System.out.print("Enter updated stock quantity: ");
                            int quantity = Integer.parseInt(
                                    scanner.nextLine().trim());

                            boolean updated = productService.updateProduct(
                                    id, name, price, quantity);

                            if (updated) {
                                System.out.println(
                                        "Product updated successfully.");
                            } else {
                                System.out.println(
                                        "No product found with ID: " + id);
                            }
                        }

                        case "4" -> {
                            running = false;
                            System.out.println("Exiting StockFlow.");
                        }

                        default -> System.out.println(
                                "Invalid option. Choose 1, 2, 3, or 4.");
                    }

                } catch (NumberFormatException e) {
                    System.out.println(
                            "Invalid number. Enter a valid numeric value.");
                } catch (IllegalArgumentException e) {
                    System.out.println(
                            "Validation error: " + e.getMessage());
                } catch (SQLException e) {
                    System.out.println(
                            "Database error: " + e.getMessage());
                }
            }
        }
    }

    private static void displayProducts(
            ProductService productService) throws SQLException {

        List<Product> products = productService.getAllProducts();

        if (products.isEmpty()) {
            System.out.println("No products found.");
            return;
        }

        System.out.println("\n--- Product List ---");

        for (Product product : products) {
            System.out.println(
                    "ID: " + product.getId()
                    + " | Name: " + product.getName()
                    + " | Price: " + product.getPrice()
                    + " | Stock: " + product.getStockQuantity()
                    + " | Active: " + product.isActive());
        }
    }
}
