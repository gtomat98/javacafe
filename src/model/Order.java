package model;

import util.AppConstants;
import util.CurrencyFormatter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

// Represents a successfully completed and immutable business order.
// This class is the core historical record of a sale in the system.
// Once an order is created, absolutely none of its fields can be altered.
// The list of items is wrapped in an unmodifiable collection so that no external
// class can maliciously or accidentally add or remove items from a finalized receipt.
public class Order {

    private final String orderId;
    private final LocalDateTime timestamp;
    private final List<OrderItem> items;
    private final double subtotal;
    private final double tax;
    private final double total;
    private final double amountPaid;
    private final double change;

    // Constructs a finalized order. 
    // It receives all the financial data already calculated and validated by the service layer.
    // The change given to the customer is safely computed inside the constructor as the amount paid minus the total.
    public Order(String orderId,
                 LocalDateTime timestamp,
                 List<OrderItem> items,
                 double subtotal,
                 double tax,
                 double total,
                 double amountPaid) {
        this.orderId = orderId;
        this.timestamp = timestamp;
        this.items = Collections.unmodifiableList(items);
        this.subtotal = subtotal;
        this.tax = tax;
        this.total = total;
        this.amountPaid = amountPaid;
        this.change = amountPaid - total;
    }

    // Builds a comprehensive formatted receipt text that can be displayed on screen or saved to a physical file.
    // It formats dates, prices, and aligns columns to look like a real printed receipt from a cashier machine.
    public String toReceiptText() {
        DateTimeFormatter fmt =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        StringBuilder sb = new StringBuilder();
        sb.append("=============================")
            .append("===========\n");
        sb.append("          JAVA CAFE")
            .append("                     \n");
        sb.append("=============================")
            .append("===========\n");
        sb.append(String.format("Pedido: %s%n", orderId));
        sb.append(String.format("Data:   %s%n",
            timestamp.format(fmt)));
        sb.append("-----------------------------")
            .append("-----------\n");
        
        // Appends each individual item sold, its quantity, and the line subtotal.
        for (OrderItem item : items) {
            sb.append(String.format(
                "%-20s x%d  %s%n",
                item.getProduct().getName(),
                item.getQuantity(),
                CurrencyFormatter.format(
                    item.getLineTotal())));
        }
        
        sb.append("-----------------------------")
            .append("-----------\n");
        
        // Appends the final financial breakdown.
        sb.append(String.format("Subtotal:       %s%n",
            CurrencyFormatter.format(subtotal)));
        sb.append(String.format(
            "Imposto (%d%%):  %s%n",
            (int) (AppConstants.TAX_RATE * 100),
            CurrencyFormatter.format(tax)));
        sb.append(String.format("TOTAL:          %s%n",
            CurrencyFormatter.format(total)));
        sb.append("-----------------------------")
            .append("-----------\n");
        sb.append(String.format("Pago:           %s%n",
            CurrencyFormatter.format(amountPaid)));
        sb.append(String.format("Troco:          %s%n",
            CurrencyFormatter.format(change)));
        sb.append("=============================")
            .append("===========\n");
        sb.append("       Obrigado pela visita!")
            .append("            \n");
        sb.append("=============================")
            .append("===========\n");
        return sb.toString();
    }

    // Returns the system-generated unique order identifier.
    public String getOrderId() {
        return orderId;
    }

    // Returns the exact date and time the sale was finalized.
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // Returns the list of items. It is wrapped as unmodifiable, so elements cannot be added or removed.
    public List<OrderItem> getItems() {
        return items;
    }

    // Returns the subtotal sum of the items, not including taxes.
    public double getSubtotal() {
        return subtotal;
    }

    // Returns the calculated tax amount charged on this order.
    public double getTax() {
        return tax;
    }

    // Returns the final total amount the customer had to pay.
    public double getTotal() {
        return total;
    }

    // Returns the cash amount the customer handed to the cashier.
    public double getAmountPaid() {
        return amountPaid;
    }

    // Returns the change that was given back to the customer.
    public double getChange() {
        return change;
    }
}
