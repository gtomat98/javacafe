package view;

import controller.InventoryController;
import controller.OrderController;
import util.AppConstants;

import javax.swing.*;

// Main window of the Java Cafe POS.
// Contains tab-based navigation between the system sections.
// Implements ChangeListener directly to avoid anonymous inner classes.
public class MainFrame extends JFrame implements javax.swing.event.ChangeListener {

    private final OrderPanel orderPanel;
    private final InventoryPanel inventoryPanel;
    private final ReportsPanel reportsPanel;
    private final InventoryController inventoryController;

    public MainFrame() {
        OrderController orderController = new OrderController();
        this.inventoryController = new InventoryController();
        setTitle(AppConstants.APP_TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 768);
        setMinimumSize(new java.awt.Dimension(800, 600));
        setLocationRelativeTo(null);  // center on screen

        orderPanel = new OrderPanel(orderController);
        // Load buttons with products
        orderPanel.setMenuProducts(inventoryController.getAllProducts());
        
        inventoryPanel = new InventoryPanel(inventoryController);
        reportsPanel = new ReportsPanel(inventoryController);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        tabs.addTab("☕ Pedidos", orderPanel);
        tabs.addTab("📦 Estoque", inventoryPanel);
        tabs.addTab("📊 Relatorios", reportsPanel);

        // Link the listener strictly using "this"
        tabs.addChangeListener(this);

        add(tabs);
    }

    // Overridden method required by ChangeListener.
    // Refresh the view when switching tabs.
    @Override
    public void stateChanged(javax.swing.event.ChangeEvent e) {
        JTabbedPane tabs = (JTabbedPane) e.getSource();
        int index = tabs.getSelectedIndex();
        if (index == 0) {
            // Refresh the menu in case something changed in the inventory
            orderPanel.setMenuProducts(inventoryController.getAllProducts());
            orderPanel.refreshOrderView();
        } else if (index == 1) {
            inventoryPanel.refreshInventoryView();
        } else if (index == 2) {
            // When opening the reports tab, go back to today's status
            reportsPanel.refreshToday();
        }
    }
}
