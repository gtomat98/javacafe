package exception;

// This custom exception is thrown when there is an attempt to sell or deduct more units of a specific product
// than what is currently available in the system's inventory.
// It acts as a safeguard to prevent negative stock quantities.
public class OutOfStockException extends Exception {

    private final String productName;
    private final int requested;
    private final int available;

    // Constructs the exception with context about the inventory failure.
    // Stores the product name, how many units were requested by the user, and how many are actually left in stock.
    // Automatically builds a descriptive error message detailing the discrepancy.
    public OutOfStockException(String productName,
                               int requested,
                               int available) {
        super(String.format(
            "Insufficient stock for '%s': "
                + "requested %d, available %d.",
            productName, requested, available));
        this.productName = productName;
        this.requested = requested;
        this.available = available;
    }

    // Returns the name of the product that triggered the stock error.
    public String getProductName() {
        return productName;
    }

    // Returns the number of units that the system attempted to deduct.
    public int getRequested() {
        return requested;
    }

    // Returns the actual number of units that were available in the inventory at the time of the error.
    public int getAvailable() {
        return available;
    }
}
