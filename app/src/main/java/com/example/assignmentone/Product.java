package com.example.assignmentone;

public class Product {
    private final int id;
    private final String name;
    private final double price;
    private final String description;
    private int availableQuantity;
    private final int originalQuantity;
    private final String category;
    private final boolean isLocal;

    public Product(int id, String name, double price, String description, int quantity,
                   String category, boolean isLocal) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.availableQuantity = quantity;
        this.originalQuantity = quantity;
        this.category = category;
        this.isLocal = isLocal;
    }

    // Getters and Setters
    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getDescription() { return description; }
    public int getAvailableQuantity() { return availableQuantity; }
    public int getOriginalQuantity() { return originalQuantity; }
    public String getCategory() { return category; }
    public boolean isLocal() { return isLocal; }

    public void setAvailableQuantity(int quantity) {
        this.availableQuantity = quantity;
    }

    public void resetQuantity() {
        this.availableQuantity = this.originalQuantity;
    }
}