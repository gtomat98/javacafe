package service;

import exception.InvalidPaymentException;
import exception.OutOfStockException;
import model.Order;
import model.OrderItem;

// Outlines the core business rules and behaviors required to assemble and complete a sale.
// It acts as the bridge between the user interface actions (like clicking "Add Item")
// and the underlying storage and data models.
public interface IOrderService {

    // Registers a product and its requested quantity into the ongoing order session.
    // The implementation is strictly required to verify if the warehouse can fulfill the request,
    // and must throw an exception if the stock levels are critically insufficient.
    void addItem(OrderItem item)
        throws OutOfStockException;

    // Discards a specific product line from the current order session.
    // Usually triggered when the cashier realizes they made a mistake scanning an item.
    void removeItem(OrderItem item);

    // Concludes the current assembly session and turns it into a permanent historical order.
    // This critical method encompasses the actual transaction logic: it verifies the financial payment,
    // permanently deducts the items from the stock, logs the sale into the CSV files, and finally resets the cart.
    // It will aggressively reject the operation if the cash provided isn't enough to cover the bill.
    Order finalizeOrder(double amountPaid)
        throws InvalidPaymentException;

    // Computes and returns the raw sum of all the items currently placed in the basket, without taxes applied.
    double getSubtotal();

    // Computes and returns the specific tax value that will be added on top of the subtotal.
    double getTax();

    // Computes and returns the final mathematical total that the customer is expected to pay.
    double getTotal();

    // Empties the entire current basket completely. This is a destructive operation that drops
    // all ongoing selections without saving any history. It's essentially the "Cancel Order" button.
    void clearOrder();
}
