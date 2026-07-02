package controller;

import model.Product;
import model.SalesReport;
import repository.IProductRepository;
import service.IReportService;
import util.AppConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// This controller bridges the gap between the user interface panels (Inventory and Reports)
// and the underlying business services. It handles user requests to view the product catalog,
// add or edit items, copy uploaded images to the correct folder, and pull business intelligence reports.
public class InventoryController {

    private final IProductRepository productRepository;
    private final IReportService reportService;
    private final OrderController orderController;
    private List<Product> cachedProducts;

    // Constructs the controller and eagerly loads all products into memory.
    // Receives the shared OrderController so that cart checks use the same
    // instance that the OrderPanel operates on, preventing stale state.
    public InventoryController(OrderController orderController) {
        this.productRepository = new repository.ProductRepository();
        this.reportService = new service.ReportService();
        this.orderController = orderController;
        this.cachedProducts = productRepository.loadAll();
    }

    // Returns a defensive copy of the product catalog.
    // Providing a copy prevents the graphical table models from accidentally modifying the real list.
    public List<Product> getAllProducts() {
        return new ArrayList<>(cachedProducts);
    }

    // Handles the creation of a brand new product from the UI form.
    // It automatically generates a unique 8-character ID, copies the user-selected image
    // into the internal data folder, creates the domain object, and forces the repository to save it.
    public void addProduct(String name, double price, int stock, String imagePath) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        String savedImagePath = copyImage(id, imagePath);
        Product product = new Product(id, name, price, stock, AppConstants.DEFAULT_LOW_STOCK, savedImagePath);
        productRepository.addProduct(product);
        reloadProducts(); // Refreshes the local cache to reflect the new addition.
    }

    // Handles the modification of an existing product in the catalog.
    // Crucially, it actively blocks any edits if the cashier is currently midway through assembling an order,
    // to prevent the cart's pricing or stock calculation from becoming corrupted.
    public void updateProduct(String productId, String newName, double newPrice, int newStock, String imagePath) {
        if (!orderController.getCurrentItems().isEmpty()) {
            throw new IllegalStateException("Não é possível editar produtos enquanto há itens no carrinho do cliente.");
        }
        for (Product p : cachedProducts) {
            if (p.getId().equals(productId)) {
                if (newName != null && !newName.trim().isEmpty()) p.setName(newName);
                if (newPrice > 0) p.setPrice(newPrice);
                if (newStock >= 0) p.setStockQuantity(newStock);
                
                if (imagePath != null && !imagePath.equals(p.getImagePath())) {
                    String savedPath = copyImage(productId, imagePath);
                    if (savedPath != null) {
                        p.setImagePath(savedPath);
                    }
                }
                
                productRepository.updateProduct(p);
                break;
            }
        }
        reloadProducts(); // Refreshes the local cache so the UI shows the new data instantly.
    }

    // Internal helper that copies a photo chosen by the user from their computer
    // into the application's dedicated "data/images/" folder.
    // It cleverly renames the file to match the product's unique ID to avoid naming collisions.
    private String copyImage(String productId, String sourcePath) {
        if (sourcePath == null || sourcePath.trim().isEmpty()) return null;
        try {
            File src = new File(sourcePath);
            String ext = "";
            int extIdx = sourcePath.lastIndexOf('.');
            if (extIdx > 0) {
                ext = sourcePath.substring(extIdx);
            }
            File destDir = new File(AppConstants.IMAGES_DIR);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            File dest = new File(destDir, productId + ext);
            
            FileInputStream in = new FileInputStream(src);
            FileOutputStream out = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            in.close();
            out.close();
            
            return dest.getPath().replace("\\", "/");
        } catch (Exception e) {
            System.err.println("Error copying image: " + e.getMessage());
            return null;
        }
    }

    // Routes requests from the Reports panel to the underlying reporting engine.
    // By passing a simple string like "today", "week", or "month", it determines
    // which specialized query method to execute.
    public SalesReport getReport(String period) {
        if ("today".equals(period)) {
            return reportService.getToday();
        } else if ("week".equals(period)) {
            return reportService.getCurrentWeek();
        } else if ("month".equals(period)) {
            return reportService.getCurrentMonth();
        } else {
            return reportService.getToday();
        }
    }

    // Forces the controller to drop its current memory cache and reread the entire CSV from disk.
    // This is vital after a product is added, updated, or when a sale concludes (which changes stock levels).
    public void reloadProducts() {
        cachedProducts = productRepository.loadAll();
    }
}
