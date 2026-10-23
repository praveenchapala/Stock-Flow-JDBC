
package com.stockflow;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

import com.stockflow.dao.ProductDAO;
import com.stockflow.model.Product;
import com.stockflow.service.ProductService;

public class Main {

    private static final Scanner SCANNER = new Scanner(System.in);

    // Standalone CLI initialization.
    // The Spring Boot REST API uses Spring-managed dependency injection.
    private static final ProductService PRODUCT_SERVICE =
            new ProductService(new ProductDAO());

    public static void main(String[] args) {

        boolean running = true;

        System.out.println("========================================");
        System.out.println("       Welcome to StockFlow");
        System.out.println("   Inventory Management System");
        System.out.println("========================================");

        while (running) {
            displayMenu();

            String choice = SCANNER.nextLine().trim();

            try {
                switch (choice) {

                    case "1" -> addProduct();

                    case "2" -> displayProducts();

                    case "3" -> updateProduct();

                    case "4" -> deleteProduct();

                    case "5" -> {
                        running = false;
                        System.out.println(
                                "Thank you for using StockFlow.");
                    }

                    default -> System.out.println(
                            "Invalid option. Please choose 1-5.");
                }

            } catch (NumberFormatException e) {
                System.out.println(
                        "Invalid input. Please enter a valid number.");

            } catch (IllegalArgumentException e) {
                System.out.println(
                        "Validation error: " + e.getMessage());

            } catch (SQLException e) {
                System.err.println(
                        "Unable to complete the database operation. "
                                + "Please try again.");

                // Detailed diagnostics for local development.
                e.printStackTrace(System.err);
            }

            if (running) {
                System.out.println();
            }
        }

        SCANNER.close();
    }

    private static void displayMenu() {
        System.out.println("\n========== STOCKFLOW MENU ==========");
        System.out.println("1. Add Product");
        System.out.println("2. View All Products");
        System.out.println("3. Update Product");
        System.out.println("4. Delete Product");
        System.out.println("5. Exit");
        System.out.print("Choose an option: ");
    }

    private static void addProduct() throws SQLException {

        System.out.println("\n--- Add Product ---");

        System.out.print("Enter product name: ");
        String name = SCANNER.nextLine();

        System.out.print("Enter product price: ");
        BigDecimal price = new BigDecimal(
                SCANNER.nextLine().trim());

        System.out.print("Enter stock quantity: ");
        int quantity = Integer.parseInt(
                SCANNER.nextLine().trim());

        long id = PRODUCT_SERVICE.addProduct(
                name, price, quantity);

        System.out.println(
                "Product added successfully. Product ID: " + id);
    }

    private static void displayProducts() throws SQLException {

        List<Product> products =
                PRODUCT_SERVICE.getAllProducts();

        if (products.isEmpty()) {
            System.out.println("\nNo products found.");
            return;
        }

        System.out.println("\n--- Product Inventory ---");

        System.out.printf(
                "%-8s %-25s %-12s %-12s %-10s%n",
                "ID", "NAME", "PRICE", "STOCK", "ACTIVE");

        System.out.println(
                "-------------------------------------------------------------------");

        for (Product product : products) {
            System.out.printf(
                    "%-8d %-25s %-12s %-12d %-10s%n",
                    product.getId(),
                    truncate(product.getName(), 25),
                    product.getPrice().toPlainString(),
                    product.getStockQuantity(),
                    product.isActive() ? "Yes" : "No");
        }

        System.out.println(
                "-------------------------------------------------------------------");

        System.out.println(
                "Total products: " + products.size());
    }

    private static void updateProduct() throws SQLException {

        System.out.println("\n--- Update Product ---");

        System.out.print("Enter product ID to update: ");
        long id = Long.parseLong(
                SCANNER.nextLine().trim());

        System.out.print("Enter updated product name: ");
        String name = SCANNER.nextLine();

        System.out.print("Enter updated price: ");
        BigDecimal price = new BigDecimal(
                SCANNER.nextLine().trim());

        System.out.print("Enter updated stock quantity: ");
        int quantity = Integer.parseInt(
                SCANNER.nextLine().trim());

        boolean updated = PRODUCT_SERVICE.updateProduct(
                id, name, price, quantity);

        if (updated) {
            System.out.println(
                    "Product updated successfully.");
        } else {
            System.out.println(
                    "No product found with ID: " + id);
        }
    }

    private static void deleteProduct() throws SQLException {

        System.out.println("\n--- Delete Product ---");

        System.out.print("Enter product ID to delete: ");
        long id = Long.parseLong(
                SCANNER.nextLine().trim());

        System.out.print(
                "Confirm deletion? Type yes to continue: ");

        String confirmation = SCANNER.nextLine().trim();

        if (!confirmation.equalsIgnoreCase("yes")) {
            System.out.println("Deletion cancelled.");
            return;
        }

        boolean deleted = PRODUCT_SERVICE.deleteProduct(id);

        if (deleted) {
            System.out.println(
                    "Product deleted successfully.");
        } else {
            System.out.println(
                    "No product found with ID: " + id);
        }
    }

    private static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength - 3) + "...";
    }
}
