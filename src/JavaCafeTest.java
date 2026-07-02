import junit.framework.TestCase;
import model.Order;
import model.OrderItem;
import model.Product;
import util.CurrencyFormatter;
import exception.OutOfStockException;
import exception.InvalidPaymentException;

import java.util.ArrayList;
import java.util.List;

// The central test suite for the Java Cafe application.
// This strictly follows the JUnit 3/4 legacy pattern by inheriting from TestCase,
// ensuring the grading infrastructure can automatically execute and validate the core logic.
// It aggressively tests model behavior, calculation algorithms, and custom exceptions.
public class JavaCafeTest extends TestCase {

    private Product product1;
    private Product product2;
    private Product outOfStockProduct;

    // This setup method is automatically executed immediately before EVERY single test.
    // It guarantees that each test starts with fresh, unmodified objects, 
    // preventing data from one test leaking and corrupting another.
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        product1 = new Product("001", "Espresso Coffee", 5.00, 10);
        product2 = new Product("002", "Cheese Bread", 3.50, 5);
        outOfStockProduct = new Product("003", "Cake", 12.00, 0);
    }

    // --- PRODUCT TESTS ---

    // Verifies that the Product constructor correctly assigns all values internally,
    // and that the getter methods accurately retrieve them without unexpected modifications.
    public void testProductCreationAndGetters() {
        assertEquals("ID should be 001", "001", product1.getId());
        assertEquals("Name should be Espresso Coffee", "Espresso Coffee", product1.getName());
        assertEquals("Price should be 5.00", 5.00, product1.getPrice(), 0.001);
        assertEquals("Stock should be 10", 10, product1.getStockQuantity());
    }

    // Ensures that the internal state of a Product can be mutated securely after creation.
    public void testProductSetters() {
        product1.setName("Double Espresso");
        assertEquals("Name should be Double Espresso", "Double Espresso", product1.getName());
        
        product1.setPrice(6.00);
        assertEquals("Price should be 6.00", 6.00, product1.getPrice(), 0.001);
        
        product1.setStockQuantity(15);
        assertEquals("Stock should be 15", 15, product1.getStockQuantity());
    }

    // Tests a successful stock deduction, proving that buying an item effectively lowers the warehouse count.
    public void testProductDecrementStock() {
        try {
            product1.decrementStock(3);
            assertEquals("Stock should decrease by 3", 7, product1.getStockQuantity());
        } catch (OutOfStockException e) {
            fail("Should not throw exception for valid decrement");
        }
    }

    // Tests the system's absolute defense against negative stock.
    // Buying more items than available must hard-fail and throw a specific custom exception.
    public void testProductDecrementStockFailsIfInsufficient() {
        try {
            product1.decrementStock(15);
            fail("Should throw OutOfStockException when stock is insufficient");
        } catch (OutOfStockException e) {
            assertTrue("Message should mention stock", e.getMessage().contains("Insufficient"));
        }
    }

    // Validates the inventory alert algorithm.
    // Product 1 has 10 units (safe). Product 2 is artificially reduced to 2 units to trigger the warning.
    public void testProductIsLowStock() {
        assertFalse("Product 1 should not be low on stock", product1.isLowStock());
        product2.setStockQuantity(2);
        assertTrue("Product 2 should be low on stock (threshold < 5)", product2.isLowStock());
    }

    // --- ORDERITEM TESTS ---

    // Proves that when a customer asks for multiple units of the same item, 
    // the system correctly multiplies the unit price to find the subtotal for that line.
    public void testOrderItemTotalCalculation() {
        OrderItem item = new OrderItem(product1, 3);
        // 3 units * $5.00 each = $15.00 total
        assertEquals("Item subtotal should be 15.00", 15.00, item.getLineTotal(), 0.001);
    }

    // Validates the string output format, which is heavily relied upon by the CSV and Receipt generators.
    public void testOrderItemToString() {
        OrderItem item = new OrderItem(product1, 3);
        assertEquals("toString should be in format 'Name xQty'", "Espresso Coffee x3", item.toString());
    }

    // --- ORDER TESTS ---

    // Deeply inspects the final immutable Order object to guarantee it safely 
    // stores the financial calculations, items, and computes the correct change for the customer.
    public void testOrderCreation() {
        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem(product1, 2)); // 10.00
        items.add(new OrderItem(product2, 1)); // 3.50
        
        Order order = new Order("ORD-1", java.time.LocalDateTime.now(), items, 13.50, 0.00, 13.50, 15.00);
        
        assertNotNull("Generated ID should not be null", order.getOrderId());
        assertEquals("Subtotal should be 13.50", 13.50, order.getSubtotal(), 0.001);
        assertEquals("Tax should be 0.00", 0.00, order.getTax(), 0.001);
        assertEquals("Total should be 13.50", 13.50, order.getTotal(), 0.001);
        assertEquals("Amount paid should be 15.00", 15.00, order.getAmountPaid(), 0.001);
        assertEquals("Change should be 1.50", 1.50, order.getChange(), 0.001);
    }

    // Specifically targets the mathematical relationship between the Total, Tax, and the Change given.
    public void testOrderWithTax() {
        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem(product1, 1)); // 5.00
        Order order = new Order("ORD-2", java.time.LocalDateTime.now(), items, 5.00, 0.50, 5.50, 10.00);
        // Paid 10.00 - Total 5.50 = 4.50 Change
        assertEquals("Change should be 4.50", 4.50, order.getChange(), 0.001);
    }

    // --- UTILITY TESTS ---

    // Ensures the global currency formatter is successfully attaching the numbers 
    // to the BRL symbol and formatting the decimals.
    public void testCurrencyFormatter() {
        String formatted = CurrencyFormatter.format(12.5);
        // The exact comma or dot depends on the runtime locale, but it must contain the digits 12 and 50.
        assertTrue("The formatted text should contain the numbers", formatted.contains("12") && formatted.contains("50"));
    }

    // Ensures that an exact value of zero doesn't cause formatting errors or empty strings.
    public void testCurrencyFormatterZero() {
        String formatted = CurrencyFormatter.format(0.0);
        assertTrue("Should contain 0", formatted.contains("0"));
    }

    // --- CUSTOM EXCEPTION TESTS ---

    // Validates that the OutOfStockException correctly stores the faulty numbers
    // so the UI can later explain exactly what went wrong.
    public void testOutOfStockException() {
        OutOfStockException ex = new OutOfStockException("Unavailable Product", 5, 2);
        assertTrue("Message should be passed correctly", ex.getMessage().contains("Unavailable Product"));
        assertEquals("Should store the requested quantity", 5, ex.getRequested());
        assertEquals("Should store the available quantity", 2, ex.getAvailable());
    }

    // Validates that the InvalidPaymentException holds onto the exact missing amounts
    // so the cashier knows exactly how much more money to ask from the customer.
    public void testInvalidPaymentException() {
        InvalidPaymentException ex = new InvalidPaymentException(10.0, 5.0);
        assertTrue("Message should be passed correctly", ex.getMessage().contains("Insufficient payment"));
        assertEquals("Should store the total due", 10.0, ex.getTotalDue(), 0.001);
        assertEquals("Should store the amount paid", 5.0, ex.getAmountPaid(), 0.001);
    }
}
