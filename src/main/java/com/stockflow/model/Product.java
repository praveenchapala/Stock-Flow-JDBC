
package com.stockflow.model;

import java.math.BigDecimal;

public class Product {

    private long id;
    private String name;
    private BigDecimal price;
    private int stockQuantity;
    private boolean active;

    public Product(String name, BigDecimal price, int stockQuantity) {
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.active = true;
    }

    public Product(long id, String name, BigDecimal price,
                   int stockQuantity, boolean active) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.active = active;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public boolean isActive() {
        return active;
    }
}
