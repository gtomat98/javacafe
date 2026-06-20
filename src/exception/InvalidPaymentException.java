package exception;

// This custom exception is thrown during the checkout process when the cashier tries to finalize an order,
// but the cash amount handed by the customer is not enough to cover the order's total price.
// It carries the financial details so the user interface can display exactly how much is missing.
public class InvalidPaymentException extends Exception {

    private final double totalDue;
    private final double amountPaid;

    // Constructs the exception with the required financial context.
    // Receives the total amount that should have been paid and what was actually handed over.
    // Automatically builds a descriptive error message with both values formatted.
    public InvalidPaymentException(double totalDue,
                                   double amountPaid) {
        super(String.format(
            "Insufficient payment. Total: R$ %.2f, "
                + "Paid: R$ %.2f.",
            totalDue, amountPaid));
        this.totalDue = totalDue;
        this.amountPaid = amountPaid;
    }

    // Returns the total amount of money the customer was supposed to pay.
    public double getTotalDue() {
        return totalDue;
    }

    // Returns the actual amount of money the customer handed over to the cashier.
    public double getAmountPaid() {
        return amountPaid;
    }
}
