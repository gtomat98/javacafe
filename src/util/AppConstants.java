package util;

// Global configuration constants for the Java Cafe POS application.
// This class centralizes all the raw values, file paths, and formatting strings
// used across the system. Centralizing them here prevents "magic numbers" from scattering
// through the codebase and makes it incredibly easy to tweak the application later.
// Because it contains only static constants, it is explicitly marked as final
// and has a private constructor so nobody accidentally instantiates it.
public final class AppConstants {

    // Prevents other classes from accidentally creating an object of AppConstants.
    private AppConstants() { }

    // The fixed tax rate applied globally to the subtotal of every order.
    // Set to 0.10, representing a 10% tax charge.
    public static final double TAX_RATE = 0.10;

    // The default baseline inventory threshold used to trigger low-stock alerts.
    // If a product doesn't specify its own threshold, it uses this fallback value.
    public static final int DEFAULT_LOW_STOCK = 5;

    // The standardized relative path leading directly to the CSV file 
    // where the product catalog is saved and loaded from.
    public static final String PRODUCTS_FILE =
        "data/products.csv";

    // The standard directory path where user-uploaded product images are safely copied and stored.
    public static final String IMAGES_DIR =
        "data/images/";

    // The standardized relative path leading directly to the CSV file 
    // where the historical sales ledgers are accumulated.
    public static final String SALES_FILE =
        "data/sales.csv";

    // The core string title displayed right at the top bar of the main graphical application window.
    public static final String APP_TITLE =
        "Java Cafe \u2014 POS";

    // The standardized date format (ISO style) used strictly when parsing or displaying dates without times.
    public static final String DATE_FORMAT =
        "yyyy-MM-dd";

    // The comprehensive date and time format (ISO style) heavily used to accurately log the exact second a sale happened.
    public static final String DATETIME_FORMAT =
        "yyyy-MM-dd'T'HH:mm:ss";

    // The specific character chosen to divide data columns inside the CSV files.
    public static final String CSV_SEPARATOR = ",";

    // The exact column header structure injected at the top of the products CSV file when it is first generated.
    public static final String PRODUCTS_CSV_HEADER =
        "id,name,price,stockQuantity,lowStockThreshold,imagePath";

    // The exact column header structure injected at the top of the sales CSV file when it is first generated.
    public static final String SALES_CSV_HEADER =
        "orderId,timestamp,subtotal,tax,total,items";

    // The secondary separator used exclusively inside the "items" column of the sales CSV.
    // Since a single order can contain multiple items, this separates them internally without breaking the main CSV comma structure.
    public static final String ITEMS_SEPARATOR = ";";

    // The standardized text label representing the current day period, used inside the graphical report dashboards.
    public static final String PERIOD_TODAY = "Hoje";

    // The standardized text label representing the current week period, used inside the graphical report dashboards.
    public static final String PERIOD_WEEK = "Semana";

    // The standardized text label representing the current month period, used inside the graphical report dashboards.
    public static final String PERIOD_MONTH = "M\u00eas";
}
