
package com.stockflow;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import com.stockflow.dao.ProductDAO;
import com.stockflow.model.Product;
import com.stockflow.service.ProductService;

public class Main {

    private static final Scanner SCANNER = new Scanner(System.in);

    public static void main(String[] args) {
        try (HikariDataSource dataSource = createDataSource()) {
            ProductService productService =
                    new ProductService(new ProductDAO(dataSource));

            runMenu(productService);
        } catch (IllegalStateException e) {
            System.err.println("Configuration error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("StockFlow encountered an error.");
            e.printStackTrace(System.err);
        } finally {
            SCANNER.close();
        }
    }

    private static HikariDataSource createDataSource() {
        String url = getEnvironmentValue(
                "STOCKFLOW_DB_URL",
                "jdbc:mysql://localhost:3306/stockflow");

        String username =
                getRequiredEnvironmentValue("STOCKFLOW_DB_USER");
        String password =
                getRequiredEnvironmentValue("STOCKFLOW_DB_PASSWORD");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setPoolName("StockFlowCliPool");
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000);

        return new HikariDataSource(config);
    }

    private static String getEnvironmentValue(
            String variable, String defaultValue) {
        String value = System.getenv(variable);

        return value == null || value.isBlank()
                ? defaultValue
                : value.trim();
    }

    private static String getRequiredEnvironmentValue(String variable) {
        String value = System.getenv(variable);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Missing required environment variable: " + variable);
        }

        return value.trim();
    }

    private static void runMenu(ProductService productService) {
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
                    case "1" -> addProduct(productService);
                    case "2" -> displayProducts(productService);
                    case "3" -> updateProduct(productService);
                    case "4" -> deleteProduct(productService);
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
                e.printStackTrace(System.err);
            }

            if (running) {
                System.out.println();
            }
        }
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

    private static void addProduct(ProductService service)
            throws SQLException {
        System.out.println("\n--- Add Product ---");

        System.out.print("Enter product name: ");
        String name = SCANNER.nextLine();

        System.out.print("Enter product price: ");
        BigDecimal price = new BigDecimal(SCANNER.nextLine().trim());

        System.out.print("Enter stock quantity: ");
        int quantity = Integer.parseInt(SCANNER.nextLine().trim());

        long id = service.addProduct(name, price, quantity);

        System.out.println(
                "Product added successfully. Product ID: " + id);
    }

    private static void displayProducts(ProductService service)
            throws SQLException {
        List<Product> products = service.getAllProducts();

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
        System.out.println("Total products: " + products.size());
    }

    private static void updateProduct(ProductService service)
            throws SQLException {
        System.out.println("\n--- Update Product ---");

        System.out.print("Enter product ID to update: ");
        long id = Long.parseLong(SCANNER.nextLine().trim());

        System.out.print("Enter updated product name: ");
        String name = SCANNER.nextLine();

        System.out.print("Enter updated price: ");
        BigDecimal price = new BigDecimal(SCANNER.nextLine().trim());

        System.out.print("Enter updated stock quantity: ");
        int quantity = Integer.parseInt(SCANNER.nextLine().trim());

        boolean updated =
                service.updateProduct(id, name, price, quantity);

        System.out.println(updated
                ? "Product updated successfully."
                : "No product found with ID: " + id);
    }

    private static void deleteProduct(ProductService service)
            throws SQLException {
        System.out.println("\n--- Delete Product ---");

        System.out.print("Enter product ID to delete: ");
        long id = Long.parseLong(SCANNER.nextLine().trim());

        System.out.print("Confirm deletion? Type yes to continue: ");
        String confirmation = SCANNER.nextLine().trim();

        if (!confirmation.equalsIgnoreCase("yes")) {
            System.out.println("Deletion cancelled.");
            return;
        }

        boolean deleted = service.deleteProduct(id);

        System.out.println(deleted
                ? "Product deleted successfully."
                : "No product found with ID: " + id);
    }

    private static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength - 3) + "...";
    }
}
