package controller;

import exception.InvalidPaymentException;
import exception.OutOfStockException;
import model.Order;
import model.OrderItem;
import model.Product;
import service.OrderService;

import java.util.List;

// This controller bridges the gap specifically between the Order/Checkout user interface
// and the underlying transaction services. It doesn't perform heavy calculations itself;
// rather, it safely delegates the UI's actions (clicks, inputs) down to the OrderService logic.
public class OrderController {

    private final OrderService orderService;

    // Initializes the controller by creating a fresh instance of the order transaction service.
    public OrderController() {
        this.orderService = new OrderService();
    }

    // Tells the service to add a product to the ongoing basket.
    // If the service detects that there isn't enough stock in the warehouse, it will throw
    // an OutOfStockException which this controller allows to bubble up to the graphical interface.
    public void addItem(Product product, int quantity) throws OutOfStockException {
        OrderItem item = new OrderItem(product, quantity);
        orderService.addItem(item);
    }

    // Tells the service to completely remove a specific product line from the basket.
    // Passing a quantity of 0 is just a trick so the service knows exactly which product ID to target and drop.
    public void removeItem(Product product) {
        orderService.removeItem(new OrderItem(product, 0));
    }

    // Attempts to conclude the sale by passing the cash amount handed by the customer down to the service.
    // If the payment is insufficient, the service will reject it by throwing an InvalidPaymentException,
    // which alerts the UI to display an error pop-up to the cashier.
    public Order finalizeOrder(double amountPaid) throws InvalidPaymentException {
        return orderService.finalizeOrder(amountPaid);
    }

    // Discards all current selections and empties the cart. Used when a customer abandons the purchase.
    public void clearOrder() {
        orderService.clearOrder();
    }

    // --- Data retrieval methods specifically designed to feed the graphical tables and labels ---

    // Retrieves the current list of items in the basket so the UI table can draw them.
    public List<OrderItem> getCurrentItems() {
        return orderService.getCurrentItems();
    }

    // Retrieves the raw sum of items, without tax.
    public double getSubtotal() { return orderService.getSubtotal(); }
    
    // Retrieves the calculated tax amount.
    public double getTax()      { return orderService.getTax(); }
    
    // Retrieves the absolute total the customer must pay.
    public double getTotal()    { return orderService.getTotal(); }

    // After a sale concludes, this fetches the fully formatted receipt text.
    // It's used by the UI to show the pop-up containing the receipt details and offering the "Save .txt" option.
    // Returns an empty string if there isn't a recently completed order.
    public String getLastReceiptText() {
        Order last = orderService.getLastOrder();
        return last != null ? last.toReceiptText() : "";
    }
}
