package model;

// Represents an aggregated count of sales for a specific product.
// Used internally by the ReportService to compute the top-selling items
public class AggregatedSale {

    private final String productName;
    private int quantity;

    // Constructs a new aggregated record for a product.
    public AggregatedSale(String productName, int quantity) {
        this.productName = productName;
        this.quantity = quantity;
    }

    // Returns the name of the product being tracked.
    public String getProductName() {
        return productName;
    }

    // Returns the total quantity accumulated so far.
    public int getQuantity() {
        return quantity;
    }

    // Increments the quantity sold.
    public void addQuantity(int qty) {
        this.quantity += qty;
    }
}
