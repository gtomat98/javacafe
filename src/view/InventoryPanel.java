package view;

import controller.InventoryController;
import model.Product;
import util.CurrencyFormatter;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

// Panel for managing product inventory.
public class InventoryPanel extends JPanel implements ActionListener {

    private final InventoryController inventoryController;
    private final JTable productsTable;
    private final DefaultTableModel tableModel;
    private final JButton addBtn;
    private final JButton editBtn;

    public InventoryPanel(InventoryController inventoryController) {
        this.inventoryController = inventoryController;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titleLabel = new JLabel("📦 ESTOQUE");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        topPanel.add(titleLabel);
        add(topPanel, BorderLayout.NORTH);

        String[] columns = {"Foto", "ID", "Nome", "Preco", "Estoque", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? ImageIcon.class : Object.class;
            }
        };
        productsTable = new JTable(tableModel);
        productsTable.setRowHeight(50);
        productsTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        productsTable.setSelectionBackground(new Color(173, 216, 230)); // Light blue
        applyHighlights();
        
        productsTable.addMouseListener(new TableDoubleClickListener());

        JScrollPane tableScroll = new JScrollPane(productsTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder(null, "PRODUTOS", 
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
                javax.swing.border.TitledBorder.DEFAULT_POSITION, 
                new Font("Segoe UI", Font.BOLD, 12)));
        add(tableScroll, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        addBtn = new JButton("➕ Adicionar Produto");
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addBtn.setBackground(new Color(144, 238, 144)); // Light green
        addBtn.addActionListener(this);
        bottomPanel.add(addBtn);
        
        editBtn = new JButton("✏️ Editar Produto Selecionado");
        editBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        editBtn.setBackground(new Color(144, 238, 144)); // Light green
        editBtn.addActionListener(this);
        bottomPanel.add(editBtn);
        
        add(bottomPanel, BorderLayout.SOUTH);
        refreshInventoryView();
    }

    public void refreshInventoryView() {
        tableModel.setRowCount(0);
        inventoryController.reloadProducts();
        List<Product> products = inventoryController.getAllProducts();
        for (Product p : products) {
            String status = p.isLowStock() ? "⚠️ Baixo" : "✅ OK";
            ImageIcon icon = getScaledIcon(p.getImagePath(), 40, 40);
            tableModel.addRow(new Object[]{
                icon, p.getId(), p.getName(), CurrencyFormatter.format(p.getPrice()), p.getStockQuantity(), status
            });
        }
    }

    private ImageIcon getScaledIcon(String path, int w, int h) {
        if (path == null || path.isEmpty()) return null;
        File f = new File(path);
        if (!f.exists()) return null;
        ImageIcon icon = new ImageIcon(path);
        Image img = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    private void onAddProduct() {
        JTextField nameField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField stockField = new JTextField();
        JLabel imgLabel = new JLabel("No image");
        JButton imgBtn = new JButton("Select Photo...");
        
        final String[] selectedImagePath = {null};

        imgBtn.addActionListener(new ImageSelectAction(selectedImagePath, imgLabel));

        Object[] message = {
            "Name:", nameField,
            "Price:", priceField,
            "Initial Stock:", stockField,
            "Photo:", imgBtn, imgLabel
        };
        
        int option = JOptionPane.showConfirmDialog(this, message, "Add Product", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                double price = Double.parseDouble(priceField.getText().trim().replace(',', '.'));
                int stock = Integer.parseInt(stockField.getText().trim());
                
                if (name.isEmpty() || price <= 0 || stock < 0) {
                    throw new IllegalArgumentException("Invalid values.");
                }
                
                inventoryController.addProduct(name, price, stock, selectedImagePath[0]);
                refreshInventoryView();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onEditProduct() {
        int row = productsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product from the table first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String productId = (String) tableModel.getValueAt(row, 1);
        String currentName = (String) tableModel.getValueAt(row, 2);
        String currentPriceStr = (String) tableModel.getValueAt(row, 3);
        String currentPrice = currentPriceStr.replace("R$", "").replace("\u00A0", "").trim().replace(',', '.');
        int currentStock = (int) tableModel.getValueAt(row, 4);

        JTextField nameField = new JTextField(currentName);
        JTextField priceField = new JTextField(currentPrice);
        JTextField stockField = new JTextField(String.valueOf(currentStock));
        JLabel imgLabel = new JLabel("Keep current image");
        JButton imgBtn = new JButton("Select New Photo...");

        final String[] selectedImagePath = {null};

        imgBtn.addActionListener(new ImageSelectAction(selectedImagePath, imgLabel));

        Object[] message = {
            "Product ID: " + productId,
            "Name:", nameField,
            "Price:", priceField,
            "Stock:", stockField,
            "New Photo (Optional):", imgBtn, imgLabel
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Update Product", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                double price = Double.parseDouble(priceField.getText().trim().replace(',', '.'));
                int stock = Integer.parseInt(stockField.getText().trim());

                if (name.isEmpty() || price <= 0 || stock < 0) {
                    throw new IllegalArgumentException("Invalid values.");
                }

                inventoryController.updateProduct(productId, name, price, stock, selectedImagePath[0]);
                refreshInventoryView();
                JOptionPane.showMessageDialog(this, "Product updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void applyHighlights() {
        productsTable.setDefaultRenderer(Object.class, new StockTableCellRenderer());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addBtn) {
            onAddProduct();
        } else if (e.getSource() == editBtn) {
            onEditProduct();
        }
    }

    // Inner classes replacing anonymous classes

    private class TableDoubleClickListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                onEditProduct();
            }
        }
    }

    private class ImageSelectAction implements ActionListener {
        private final String[] pathRef;
        private final JLabel labelRef;

        public ImageSelectAction(String[] pathRef, JLabel labelRef) {
            this.pathRef = pathRef;
            this.labelRef = labelRef;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(InventoryPanel.this) == JFileChooser.APPROVE_OPTION) {
                pathRef[0] = chooser.getSelectedFile().getAbsolutePath();
                labelRef.setText(chooser.getSelectedFile().getName());
            }
        }
    }

    private class StockTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                String status = (String) table.getModel().getValueAt(row, 5);
                if ("⚠️ Baixo".equals(status)) {
                    c.setBackground(new Color(255, 230, 204));
                } else {
                    c.setBackground(Color.WHITE);
                }
            }
            return c;
        }
    }
}
