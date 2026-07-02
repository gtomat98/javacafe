package service;

import exception.InvalidPaymentException;
import exception.OutOfStockException;
import model.Order;
import model.OrderItem;
import model.Product;
import repository.IProductRepository;
import repository.SalesRepository;
import util.AppConstants;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// This service is the operational heart of the checkout system.
// It manages the entire lifecycle of an active order: from adding the very first item
// into the basket, calculating subtotals and taxes on the fly, to finally processing
// the payment and concluding the sale.
public class OrderService implements IOrderService {

    private final IProductRepository productRepository;
    private final SalesRepository salesRepository;
    private final List<OrderItem> currentItems = new ArrayList<>();
    private Order lastOrder;

    // Initializes the service and automatically links it to the necessary data repositories.
    public OrderService() {
        this.productRepository = new repository.ProductRepository();
        this.salesRepository = new repository.SalesRepository();
    }

    // Attempts to add a product to the ongoing order.
    // It is highly robust: if the product is already in the basket, it merges the quantities
    // instead of creating a duplicate line. Before confirming the addition, it strictly
    // cross-references the requested amount with the available inventory in the warehouse.
    @Override
    public void addItem(OrderItem item) throws OutOfStockException {
        int currentQty = 0;
        OrderItem existingItem = null;
        
        for (OrderItem i : currentItems) {
            if (i.getProduct().getId().equals(item.getProduct().getId())) {
                existingItem = i;
                currentQty = i.getQuantity();
                break;
            }
        }
        
        int totalQty = currentQty + item.getQuantity();
        if (totalQty > item.getProduct().getStockQuantity()) {
            throw new OutOfStockException(item.getProduct().getName(), totalQty, item.getProduct().getStockQuantity());
        }
        
        if (existingItem != null) {
            currentItems.remove(existingItem);
            currentItems.add(new OrderItem(item.getProduct(), totalQty));
        } else {
            currentItems.add(item);
        }
    }

    // Completely removes a specific product line from the current active order basket.
    // This is utilized when a customer changes their mind or a cashier scans an item by mistake.
    @Override
    public void removeItem(OrderItem item) {
        java.util.Iterator<OrderItem> iterator = currentItems.iterator();
        while (iterator.hasNext()) {
            OrderItem i = iterator.next();
            if (i.getProduct().getId().equals(item.getProduct().getId())) {
                iterator.remove();
            }
        }
    }

    // The most critical transaction method in the application.
    // When the user clicks "Finalize", this method executes a chain of unbreakable rules:
    // 1. It blocks the sale if the provided cash is less than the total bill.
    // 2. It permanently deducts the sold items from the active inventory.
    // 3. It generates a unique Order ID and bundles the history into an immutable Order object.
    // 4. It saves the sale to the permanent historical CSV ledger.
    // 5. It clears the basket so the system is ready for the next customer.
    @Override
    public Order finalizeOrder(double amountPaid) throws InvalidPaymentException {
        if (amountPaid < getTotal()) {
            throw new InvalidPaymentException(getTotal(), amountPaid);
        }
        
        for (OrderItem item : currentItems) {
            try {
                item.getProduct().decrementStock(item.getQuantity());
            } catch (OutOfStockException e) {
                throw new RuntimeException("Unexpected situation - inconsistent data: " + e.getMessage(), e);
            }
            productRepository.updateProduct(item.getProduct());
        }
        
        String orderId = UUID.randomUUID().toString().substring(0, 8);
        LocalDateTime timestamp = LocalDateTime.now();
        
        Order order = new Order(orderId, timestamp, new ArrayList<>(currentItems), getSubtotal(), getTax(), getTotal(), amountPaid);
        
        salesRepository.saveSale(order);
        
        lastOrder = order;
        currentItems.clear();
        
        return order;
    }

    // Iterates over everything currently in the basket to calculate the sum, excluding taxes.
    @Override
    public double getSubtotal() {
        double subtotal = 0;
        for (OrderItem item : currentItems) {
            subtotal += item.getLineTotal();
        }
        return subtotal;
    }

    // Applies the global tax rate (defined in AppConstants) over the calculated subtotal.
    @Override
    public double getTax() {
        return getSubtotal() * AppConstants.TAX_RATE;
    }

    // Adds the subtotal and the calculated taxes to find the final price the customer must pay.
    @Override
    public double getTotal() {
        return getSubtotal() + getTax();
    }

    // Wipes the current order session completely clean. 
    // Usually triggered manually by a "Clear/Cancel" button on the interface.
    @Override
    public void clearOrder() {
        currentItems.clear();
        lastOrder = null;
    }

    // Safely exposes the current basket contents to the graphical interface so it can be drawn on screen.
    // It returns a fresh ArrayList copy to prevent external classes from secretly modifying the basket.
    public List<OrderItem> getCurrentItems() {
        return new ArrayList<>(currentItems);
    }

    // Exposes the very last finalized order.
    // This is primarily used immediately after a sale to display or print the generated text receipt.
    public Order getLastOrder() {
        return lastOrder;
    }
}
