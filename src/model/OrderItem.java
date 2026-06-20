package model;

// Represents a single line item within a customer's order.
// It bundles a Product reference together with the quantity the customer wants to buy.
// Instances of this class are designed to be immutable after creation.
public class OrderItem {

    private final Product product;
    private final int quantity;

    // Creates an order item mapping a specific product to the desired quantity.
    public OrderItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    // Calculates the total financial cost for this specific line item.
    // It multiplies the unit price of the underlying product by the requested quantity.
    public double getLineTotal() {
        return product.getPrice() * quantity;
    }

    // Returns the product associated with this item line.
    public Product getProduct() {
        return product;
    }

    // Returns how many units of the product were requested.
    public int getQuantity() {
        return quantity;
    }

    // Provides a formatted string representation of the item.
    // This is primarily used when displaying the item in generated text receipts or debug outputs.
    // Example format: "Espresso Coffee x3".
    @Override
    public String toString() {
        return String.format("%s x%d",
            product.getName(), quantity);
    }
}
