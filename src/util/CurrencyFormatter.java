package util;

// A dedicated helper class meant strictly for formatting raw numeric data 
// into highly readable, localized currency strings.
// Specifically, it enforces the Brazilian standard (BRL), ensuring all prices 
// shown on the user interface and printed receipts look like "R$ 15,50".
// As a pure utility class holding only static methods, it is marked as final
// to explicitly prevent inheritance and instantiation.
public final class CurrencyFormatter {

    // A private constructor that actively blocks any attempt by other classes
    // to create an object of CurrencyFormatter using the "new" keyword.
    private CurrencyFormatter() { }

    // Transforms a raw floating-point number into a formatted text string.
    // It rounds the number strictly to 2 decimal places and replaces the standard
    // programming decimal dot with the culturally correct comma.
    // Example input: 15.5
    // Example output: "R$ 15,50"
    public static String format(double value) {
        return String.format("R$ %.2f", value)
            .replace('.', ',');
    }
}
