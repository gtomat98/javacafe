package model;

import exception.OutOfStockException;
import util.AppConstants;

import java.util.Objects;

// Represents a product available on the Java Cafe menu.
// This is a central data structure used to build orders and manage the inventory.
// Each product has a unique identifier, a descriptive name, a selling price, the current amount in stock,
// a low-stock alert threshold, and an optional image path to be displayed in the user interface.
public class Product {

    private String id;
    private String name;
    private double price;
    private int stockQuantity;
    private int lowStockThreshold;
    private String imagePath;

    // Constructor without threshold or image. It defaults to the system's standard low-stock threshold 
    // defined in AppConstants and leaves the image path as null, meaning no picture is associated.
    public Product(String id, String name, double price, int stockQuantity) {
        this(id, name, price, stockQuantity, AppConstants.DEFAULT_LOW_STOCK, null);
    }

    // Constructor allowing a custom low-stock threshold, but still without an image path.
    public Product(String id, String name, double price, int stockQuantity, int lowStockThreshold) {
        this(id, name, price, stockQuantity, lowStockThreshold, null);
    }

    // Full constructor. This is usually called when loading the data back from the persistence layer (CSV),
    // where all fields, including the saved image path and custom thresholds, are fully known.
    public Product(String id, String name, double price, int stockQuantity, int lowStockThreshold, String imagePath) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.lowStockThreshold = lowStockThreshold;
        this.imagePath = imagePath;
    }

    // Reduces the stock by a given amount. 
    // It validates if the requested quantity is actually available before deducting.
    // Throws an OutOfStockException if there isn't enough, protecting the integrity of the inventory.
    public void decrementStock(int quantity) throws OutOfStockException {
        if (quantity > stockQuantity) {
            throw new OutOfStockException(name, quantity, stockQuantity);
        }
        stockQuantity -= quantity;
    }

    // Checks whether this product needs to be restocked.
    // Returns true if the current stock falls below the configured alert threshold.
    public boolean isLowStock() {
        return stockQuantity < lowStockThreshold;
    }

    // Returns the unique identifier of the product, typically a short UUID.
    public String getId() { return id; }

    // Returns the descriptive name of the product.
    public String getName() { return name; }

    // Returns the selling price of the product.
    public double getPrice() { return price; }

    // Returns the current amount of units available in the inventory.
    public int getStockQuantity() { return stockQuantity; }

    // Returns the minimum number of items considered safe. Below this, alerts are triggered.
    public int getLowStockThreshold() { return lowStockThreshold; }

    // Returns the absolute or relative path to the product's image file. Can be null if none exists.
    public String getImagePath() { return imagePath; }

    // Updates the descriptive name of the product.
    public void setName(String name) { this.name = name; }

    // Updates the selling price of the product.
    public void setPrice(double price) { this.price = price; }

    // Updates the current stock quantity. Can be used during manual inventory adjustments.
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    // Updates the low-stock alert threshold for this specific product.
    public void setLowStockThreshold(int lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; }

    // Updates the path to the product's associated image.
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    // Custom equality logic. Two products are considered mathematically equal if they share 
    // the exact same ID, regardless of whether their names, prices, or stock quantities differ.
    // This allows robust finding and updating mechanisms inside lists and maps.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        Product other = (Product) o;
        return Objects.equals(id, other.id);
    }

    // Computes the hash code based purely on the unique identifier.
    // Important for ensuring this object works correctly inside HashMaps and HashSets.
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Provides a readable string representation of the product.
    // Highly useful for debugging purposes and logging state in the console.
    @Override
    public String toString() {
        return String.format(java.util.Locale.US,
            "Product{id='%s', name='%s', price=%.2f, stock=%d, imagePath='%s'}",
            id, name, price, stockQuantity, imagePath);
    }
}
