package repository;

import model.Product;

import java.util.List;

// This interface defines the contract for persisting and retrieving products.
// It abstracts away the storage mechanism so the rest of the application doesn't
// need to know if the data is saved in a database, a text file, or memory.
public interface IProductRepository {

    // Reads the storage medium and reconstructs all previously saved products.
    // If the storage is empty or missing, it should gracefully return an empty list.
    List<Product> loadAll();

    // Serializes and overwrites the entire product list in the storage.
    // This is useful for bulk updates or when the structure of the file changes.
    void saveAll(List<Product> products);

    // Appends a single new product to the existing inventory list.
    // The implementation must ensure the change is immediately saved to disk.
    void addProduct(Product product);

    // Locates a product by its unique identifier and replaces its stored data.
    // Useful when prices change or when stock quantities are updated after a sale.
    void updateProduct(Product product);
}
