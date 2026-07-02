package repository;

import model.Order;
import model.OrderItem;
import model.Product;
import util.AppConstants;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// Responsible for persisting and retrieving finalized customer orders using a CSV file mechanism.
// Unlike the product catalog which is constantly overwritten, the sales ledger is cumulative:
// each new sale is simply appended as a brand new row at the very end of the file.
public class SalesRepository {

    private static final File FILE_PATH =
        new File(AppConstants.SALES_FILE);

    private static final DateTimeFormatter DT_FMT =
        DateTimeFormatter.ofPattern(
            AppConstants.DATETIME_FORMAT);

    // Safely appends a freshly completed order to the historic sales CSV file.
    // If this is the very first sale and the file doesn't exist yet, it automatically
    // generates the directory structure and writes the column headers before logging the data.
    public void saveSale(Order order) {
        try {
            File parent = FILE_PATH.getParentFile();
            if (parent != null
                    && !parent.exists()) {
                parent.mkdirs();
            }
        } catch (Exception e) {
            System.err.println(
                "Error creating data directory: "
                    + e.getMessage());
            return;
        }

        boolean fileExists = FILE_PATH.exists();
        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter(FILE_PATH, true));
            if (!fileExists) {
                writer.write(
                    AppConstants.SALES_CSV_HEADER);
                writer.newLine();
            }
            writer.write(toCSVLine(order));
            writer.newLine();
        } catch (IOException e) {
            System.err.println(
                "Error saving sale: "
                    + e.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    System.err.println("Error closing writer: " + e.getMessage());
                }
            }
        }
    }

    // Scans the sales file from top to bottom and reconstructs every single order
    // that has ever been recorded. This is highly useful for generating long-term reports.
    // If the file hasn't been created yet, it will quietly return an empty list without crashing.
    public List<Order> loadAll() {
        List<Order> orders = new ArrayList<>();
        if (!FILE_PATH.exists()) {
            return orders;
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(FILE_PATH));
            // skip header row describing the columns
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                Order order = parseLine(line);
                if (order != null) {
                    orders.add(order);
                }
            }
        } catch (IOException e) {
            System.err.println(
                "Error loading sales: "
                    + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.err.println("Error closing reader: " + e.getMessage());
                }
            }
        }
        return orders;
    }

    // A convenience wrapper function that filters the historical sales and returns
    // strictly the orders that happened exactly on the provided date.
    public List<Order> loadByDate(LocalDate date) {
        return loadByDateRange(date, date);
    }

    // Fetches all historical sales and filters them down to a specific time window.
    // Both the start date and the end date are considered inclusive in the filter logic.
    public List<Order> loadByDateRange(LocalDate from,
                                       LocalDate to) {
        List<Order> all = loadAll();
        List<Order> filtered = new ArrayList<>();
        for (Order order : all) {
            LocalDate orderDate =
                order.getTimestamp().toLocalDate();
            if (!orderDate.isBefore(from)
                    && !orderDate.isAfter(to)) {
                filtered.add(order);
            }
        }
        return filtered;
    }

    // Dissects a single raw CSV row and rebuilds an entire Order instance from it.
    // Because the "items" column aggregates multiple products, it relies on being wrapped
    // in double-quotes to protect against internal commas interfering with the main structure.
    // The method carefully isolates this quoted block before splitting the normal columns.
    private Order parseLine(String line) {
        try {
            int quotesStart = line.indexOf('"');
            int quotesEnd = line.lastIndexOf('"');
            String itemsRaw;
            String prefix;
            if (quotesStart >= 0
                    && quotesEnd > quotesStart) {
                itemsRaw = line.substring(
                    quotesStart + 1, quotesEnd);
                prefix = line.substring(
                    0, quotesStart - 1);
            } else {
                // When there are no quotes, it assumes the items string is just the last column.
                String[] allParts = line.split(
                    AppConstants.CSV_SEPARATOR, -1);
                if (allParts.length < 6) {
                    System.err.println(
                        "Invalid sale line: "
                            + line);
                    return null;
                }
                itemsRaw = allParts[5];
                prefix = allParts[0]
                    + AppConstants.CSV_SEPARATOR
                    + allParts[1]
                    + AppConstants.CSV_SEPARATOR
                    + allParts[2]
                    + AppConstants.CSV_SEPARATOR
                    + allParts[3]
                    + AppConstants.CSV_SEPARATOR
                    + allParts[4];
            }

            String[] parts = prefix.split(
                AppConstants.CSV_SEPARATOR, -1);
            if (parts.length < 5) {
                System.err.println(
                    "Invalid sale line: " + line);
                return null;
            }

            String orderId = parts[0].trim();
            LocalDateTime timestamp =
                LocalDateTime.parse(
                    parts[1].trim(), DT_FMT);
            double subtotal =
                Double.parseDouble(parts[2].trim());
            double tax =
                Double.parseDouble(parts[3].trim());
            double total =
                Double.parseDouble(parts[4].trim());

            List<OrderItem> items =
                parseItems(itemsRaw);

            // Due to limitations in the initial CSV schema design, the amount physically paid
            // by the customer is not stored. We bypass this by injecting the total cost as the amount paid,
            // which effectively results in a zero-change output when viewing old receipts.
            return new Order(orderId, timestamp, items,
                subtotal, tax, total, total);

        } catch (Exception e) {
            System.err.println(
                "Error converting sale: "
                    + e.getMessage());
            return null;
        }
    }

    // Breaks down the nested string of ordered items into a list of OrderItem objects.
    // It relies on semicolons to distinguish between different products, and the " x" marker
    // to separate the product name from the requested quantity. Since the historical record
    // doesn't store individual prices or stock levels, it instantiates "dummy" Product objects
    // that just carry the name for reporting purposes.
    private List<OrderItem> parseItems(String itemsRaw) {
        List<OrderItem> items = new ArrayList<>();
        if (itemsRaw == null || itemsRaw.trim().isEmpty()) {
            return items;
        }
        String[] entries = itemsRaw.split(
            AppConstants.ITEMS_SEPARATOR);
        for (String entry : entries) {
            entry = entry.trim();
            if (entry.isEmpty()) {
                continue;
            }
            int lastX = entry.lastIndexOf(" x");
            if (lastX < 0) {
                // If the quantity marker is entirely missing, we fallback to assuming a single unit was sold.
                Product p = new Product(
                    "", entry.trim(), 0, 0);
                items.add(new OrderItem(p, 1));
                continue;
            }
            String name =
                entry.substring(0, lastX).trim();
            int qty;
            try {
                qty = Integer.parseInt(
                    entry.substring(lastX + 2).trim());
            } catch (NumberFormatException e) {
                qty = 1;
            }
            Product p = new Product("", name, 0, 0);
            items.add(new OrderItem(p, qty));
        }
        return items;
    }

    // Translates a fully realized Order object down into a single flat CSV text line.
    // The items list is iteratively stitched together and then enclosed in double-quotes
    // so it doesn't break the column structure when written to the file.
    private String toCSVLine(Order order) {
        StringBuilder itemsStr = new StringBuilder();
        List<OrderItem> items = order.getItems();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                itemsStr.append(
                    AppConstants.ITEMS_SEPARATOR);
            }
            OrderItem item = items.get(i);
            itemsStr.append(
                item.getProduct().getName())
                .append(" x")
                .append(item.getQuantity());
        }
        return String.format(java.util.Locale.US,
            "%s%s%s%s%.2f%s%.2f%s%.2f%s\"%s\"",
            order.getOrderId(),
            AppConstants.CSV_SEPARATOR,
            order.getTimestamp().format(DT_FMT),
            AppConstants.CSV_SEPARATOR,
            order.getSubtotal(),
            AppConstants.CSV_SEPARATOR,
            order.getTax(),
            AppConstants.CSV_SEPARATOR,
            order.getTotal(),
            AppConstants.CSV_SEPARATOR,
            itemsStr.toString());
    }
}
