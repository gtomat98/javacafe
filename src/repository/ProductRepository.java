package repository;

import model.Product;
import util.AppConstants;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// This concrete implementation reads and writes product data specifically to a CSV file.
// The exact path to the file is retrieved from the centralized AppConstants class.
// It implements the IProductRepository contract to guarantee consistency.
public class ProductRepository implements IProductRepository {

    private static final File FILE_PATH =
        new File(AppConstants.PRODUCTS_FILE);

    // Reads the underlying CSV file row by row, automatically skipping the header.
    // It creates Product objects out of each valid line and returns the full list.
    // If the file hasn't been created yet, it returns a blank list without causing an error.
    @Override
    public List<Product> loadAll() {
        List<Product> products = new ArrayList<>();
        if (!FILE_PATH.exists()) {
            return products;
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(FILE_PATH));
            // skip header row which just describes the columns
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                Product product = parseLine(line);
                if (product != null) {
                    products.add(product);
                }
            }
        } catch (IOException e) {
            System.err.println(
                "Error loading products: "
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
        return products;
    }

    // Completely replaces the contents of the CSV file with the provided list.
    // It proactively checks if the necessary folders (like data/ and data/images/)
    // exist, creating them automatically to avoid "File Not Found" errors during saving.
    @Override
    public void saveAll(List<Product> products) {
        try {
            File parent = FILE_PATH.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            File imagesDir = new File(AppConstants.IMAGES_DIR);
            if (!imagesDir.exists()) {
                imagesDir.mkdirs();
            }
        } catch (Exception e) {
            System.err.println(
                "Error creating data directory: "
                    + e.getMessage());
            return;
        }
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(FILE_PATH));
            writer.write(
                AppConstants.PRODUCTS_CSV_HEADER);
            writer.newLine();
            for (Product p : products) {
                writer.write(toCSVLine(p));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println(
                "Error saving products: "
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

    // Retrieves all existing products, adds the newly created one to the end of the list,
    // and instantly overrides the file to guarantee the change is saved permanently.
    @Override
    public void addProduct(Product product) {
        List<Product> products = loadAll();
        products.add(product);
        saveAll(products);
    }

    // Sweeps the current catalog searching for a product whose ID matches the incoming one.
    // When found, it updates the record with the fresh data (price, name, new stock, etc.)
    // and flushes the entire list back to the disk.
    @Override
    public void updateProduct(Product product) {
        List<Product> products = loadAll();
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId()
                    .equals(product.getId())) {
                products.set(i, product);
                break;
            }
        }
        saveAll(products);
    }

    // Helper method that takes a single text line from the CSV and converts it back
    // into a functional Product object. It handles formatting errors and missing
    // trailing columns gracefully using split limits.
    private Product parseLine(String line) {
        String[] parts = line.split(
            AppConstants.CSV_SEPARATOR, -1);
        if (parts.length < 5) {
            System.err.println(
                "Invalid product line: " + line);
            return null;
        }
        try {
            String id = parts[0].trim();
            String name = parts[1].trim();
            double price =
                Double.parseDouble(parts[2].trim());
            int stock =
                Integer.parseInt(parts[3].trim());
            int threshold =
                Integer.parseInt(parts[4].trim());
            String imagePath = parts.length > 5 && !parts[5].trim().isEmpty() ? parts[5].trim() : null;
            return new Product(
                id, name, price, stock, threshold, imagePath);
        } catch (NumberFormatException e) {
            System.err.println(
                "Error converting product data: "
                    + line);
            return null;
        }
    }

    // Helper method that translates a Product object into a comma-separated text string,
    // ensuring the sequence perfectly aligns with the predefined CSV header format.
    private String toCSVLine(Product p) {
        return String.format(java.util.Locale.US, "%s%s%s%s%.2f%s%d%s%d%s%s",
            p.getId(), AppConstants.CSV_SEPARATOR,
            p.getName(), AppConstants.CSV_SEPARATOR,
            p.getPrice(), AppConstants.CSV_SEPARATOR,
            p.getStockQuantity(),
            AppConstants.CSV_SEPARATOR,
            p.getLowStockThreshold(),
            AppConstants.CSV_SEPARATOR,
            p.getImagePath() == null ? "" : p.getImagePath());
    }
}
