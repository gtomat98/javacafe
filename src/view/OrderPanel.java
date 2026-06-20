package view;

import controller.OrderController;
import exception.InvalidPaymentException;
import exception.OutOfStockException;
import model.OrderItem;
import model.Product;
import util.CurrencyFormatter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

// Panel for registering and managing orders.
public class OrderPanel extends JPanel implements ActionListener {

    private final OrderController orderController;
    private final JPanel menuButtonsPanel;
    private final JTable orderItemsTable;
    private final DefaultTableModel tableModel;
    private final JLabel subtotalLabel;
    private final JLabel taxLabel;
    private final JLabel totalLabel;
    private final JTextField paidField;
    private List<Product> currentMenuProducts;
    
    private final JButton btnRemove;
    private final JButton btnClear;
    private final JButton btnFinalize;

    public OrderPanel(OrderController orderController) {
        this.orderController = orderController;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titleLabel = new JLabel("☕ PEDIDO ATUAL");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        topPanel.add(titleLabel);
        add(topPanel, BorderLayout.NORTH);

        // Center Split
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        
        // Menu Panel
        menuButtonsPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        JScrollPane menuScroll = new JScrollPane(menuButtonsPanel);
        menuScroll.setBorder(BorderFactory.createTitledBorder("CARDÁPIO"));
        centerPanel.add(menuScroll);

        // Table Panel
        String[] columns = {"Produto", "Qtd", "Subtotal"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        orderItemsTable = new JTable(tableModel);
        orderItemsTable.setRowHeight(25);
        orderItemsTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        orderItemsTable.setSelectionBackground(new Color(173, 216, 230));
        
        JScrollPane tableScroll = new JScrollPane(orderItemsTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder(null, "ITENS DO PEDIDO", 
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
                javax.swing.border.TitledBorder.DEFAULT_POSITION, 
                new Font("Segoe UI", Font.BOLD, 12)));
        
        JPanel tableContainer = new JPanel(new BorderLayout(0, 5));
        tableContainer.add(tableScroll, BorderLayout.CENTER);
        btnRemove = new JButton("➖ Remover da Lista");
        btnRemove.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRemove.setBackground(new Color(255, 230, 230));
        btnRemove.addActionListener(this);
        tableContainer.add(btnRemove, BorderLayout.SOUTH);
        centerPanel.add(tableContainer);

        add(centerPanel, BorderLayout.CENTER);

        // Bottom Totals and Actions
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createTitledBorder(null, "FINALIZAÇÃO", 
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
                javax.swing.border.TitledBorder.DEFAULT_POSITION, 
                new Font("Segoe UI", Font.BOLD, 12)));

        JPanel totalsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        subtotalLabel = new JLabel("Subtotal: R$ 0,00");
        subtotalLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        taxLabel = new JLabel("Imposto: R$ 0,00");
        taxLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        totalLabel = new JLabel("Total: R$ 0,00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        totalLabel.setForeground(new Color(34, 139, 34)); // Verde
        
        totalsPanel.add(subtotalLabel);
        totalsPanel.add(taxLabel);
        totalsPanel.add(totalLabel);
        bottomPanel.add(totalsPanel, BorderLayout.WEST);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JLabel paidTextLabel = new JLabel("Valor Pago:");
        paidTextLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        actionsPanel.add(paidTextLabel);
        
        paidField = new JTextField(8);
        paidField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        actionsPanel.add(paidField);

        btnClear = new JButton("🗑️ Limpar");
        btnClear.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClear.setBackground(new Color(255, 230, 230)); // Fundo vermelho clarinho
        btnClear.addActionListener(this);
        actionsPanel.add(btnClear);

        btnFinalize = new JButton("✅ Finalizar Venda");
        btnFinalize.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnFinalize.setBackground(new Color(144, 238, 144)); // Verde claro
        btnFinalize.setForeground(new Color(0, 50, 0));
        btnFinalize.setPreferredSize(new Dimension(200, 40));
        btnFinalize.addActionListener(this);
        actionsPanel.add(btnFinalize);
        
        bottomPanel.add(actionsPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
     }

     private ImageIcon getScaledIcon(String path, int w, int h) {
         if (path == null || path.isEmpty()) return null;
         File f = new File(path);
         if (!f.exists()) return null;
         ImageIcon icon = new ImageIcon(path);
         Image img = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
         return new ImageIcon(img);
     }

     public void setMenuProducts(List<Product> products) {
         this.currentMenuProducts = products;
         menuButtonsPanel.removeAll();
         menuButtonsPanel.setLayout(new GridLayout(0, 2, 15, 15));
         
         for (final Product p : products) {
             JPanel productPanel = new JPanel(new BorderLayout(5, 5));
             productPanel.setBackground(new Color(255, 255, 255));
             productPanel.setBorder(BorderFactory.createCompoundBorder(
                 BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                 BorderFactory.createEmptyBorder(10, 10, 10, 10)
             ));
             
             // Image Container
             int imgWidth = 100;
             int imgHeight = 100;
             JLabel imgLabel = new JLabel();
             imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
             
             ImageIcon icon = getScaledIcon(p.getImagePath(), imgWidth, imgHeight);
             if (icon != null) {
                 imgLabel.setIcon(icon);
                 productPanel.add(imgLabel, BorderLayout.NORTH);
             } else {
                 // Visual placeholder (no image)
                 JPanel skeleton = new JPanel();
                 skeleton.setBackground(new Color(245, 245, 245));
                 skeleton.setPreferredSize(new Dimension(imgWidth, imgHeight));
                 productPanel.add(skeleton, BorderLayout.NORTH);
             }
             
             JLabel nameLabel = new JLabel("<html><div style='text-align: center; font-family: Segoe UI, sans-serif;'>"
                 + "<b style='font-size: 14px; color: #333333;'>" + p.getName() + "</b><br/>" 
                 + "<span style='font-size: 13px; color: #2E8B57;'>" + CurrencyFormatter.format(p.getPrice()) + "</span>"
                 + "</div></html>", SwingConstants.CENTER);
             productPanel.add(nameLabel, BorderLayout.CENTER);
             
             JPanel buttonsPanel = new JPanel(new GridLayout(1, 2, 8, 0));
             buttonsPanel.setOpaque(false);
             
             JButton subBtn = new JButton("-");
             subBtn.setBackground(new Color(231, 76, 60)); // Prettier red
             subBtn.setForeground(Color.WHITE);
             subBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
             subBtn.setFocusPainted(false);
             subBtn.setActionCommand("SUB_" + p.getId());
             subBtn.addActionListener(this);
             
             JButton addBtn = new JButton("+");
             addBtn.setBackground(new Color(46, 204, 113)); // Prettier green
             addBtn.setForeground(Color.WHITE);
             addBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
             addBtn.setFocusPainted(false);
             addBtn.setActionCommand("ADD_" + p.getId());
             addBtn.addActionListener(this);
             
             buttonsPanel.add(subBtn);
             buttonsPanel.add(addBtn);
             
             productPanel.add(buttonsPanel, BorderLayout.SOUTH);
             
             menuButtonsPanel.add(productPanel);
         }
         menuButtonsPanel.revalidate();
         menuButtonsPanel.repaint();
     }

    public void refreshOrderView() {
        tableModel.setRowCount(0);
        for (OrderItem item : orderController.getCurrentItems()) {
            tableModel.addRow(new Object[]{
                item.getProduct().getName(),
                item.getQuantity(),
                CurrencyFormatter.format(item.getLineTotal())
            });
        }
        subtotalLabel.setText("Subtotal: " + CurrencyFormatter.format(orderController.getSubtotal()));
        taxLabel.setText("Imposto: " + CurrencyFormatter.format(orderController.getTax()));
        totalLabel.setText("Total: " + CurrencyFormatter.format(orderController.getTotal()));
    }

    private void onAddItem(Product product) {
        try {
            orderController.addItem(product, 1);
            refreshOrderView();
        } catch (OutOfStockException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Out of Stock", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void onRemoveItem() {
        int row = orderItemsTable.getSelectedRow();
        if (row >= 0) {
            List<OrderItem> items = orderController.getCurrentItems();
            Product p = items.get(row).getProduct();
            orderController.removeItem(p);
            refreshOrderView();
        } else {
            JOptionPane.showMessageDialog(this, "Please select an item to remove.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void onFinalizeOrder() {
        String paidText = paidField.getText().trim();
        if (paidText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the amount paid.", "Required field", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        double amountPaid;
        try {
            amountPaid = Double.parseDouble(paidText.replace(',', '.'));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount. Use numbers only.", "Input error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (orderController.getCurrentItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "The order is empty.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            orderController.finalizeOrder(amountPaid);
            showReceipt(orderController.getLastReceiptText());
            paidField.setText("");
            refreshOrderView();
        } catch (InvalidPaymentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Invalid payment", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Internal error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onClearOrder() {
        orderController.clearOrder();
        paidField.setText("");
        refreshOrderView();
    }

    private void showReceipt(final String receiptText) {
        JTextArea area = new JTextArea(receiptText);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JButton saveBtn = new JButton("💾 Salvar Recibo (.txt)");
        saveBtn.addActionListener(new SaveReceiptAction(receiptText));
        
        Object[] message = {
            new JScrollPane(area),
            saveBtn
        };

        JOptionPane.showMessageDialog(this, message, "Receipt", JOptionPane.INFORMATION_MESSAGE);
    }

    // Inner class to explicitly handle saving the receipt instead of an anonymous class
    private class SaveReceiptAction implements ActionListener {
        private final String receiptText;

        public SaveReceiptAction(String receiptText) {
            this.receiptText = receiptText;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Save Receipt");
            if (chooser.showSaveDialog(OrderPanel.this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                if (!file.getName().endsWith(".txt")) {
                    file = new File(file.getAbsolutePath() + ".txt");
                }
                try {
                    java.io.FileWriter writer = new java.io.FileWriter(file);
                    writer.write(receiptText);
                    writer.close();
                    JOptionPane.showMessageDialog(OrderPanel.this, "Receipt saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(OrderPanel.this, "Error saving receipt: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnRemove) {
            onRemoveItem();
        } else if (e.getSource() == btnClear) {
            onClearOrder();
        } else if (e.getSource() == btnFinalize) {
            onFinalizeOrder();
        } else if (e.getActionCommand() != null) {
            String cmd = e.getActionCommand();
            if (cmd.startsWith("SUB_") || cmd.startsWith("ADD_")) {
                String pId = cmd.substring(4);
                Product p = null;
                if (currentMenuProducts != null) {
                    for (Product product : currentMenuProducts) {
                        if (product.getId().equals(pId)) {
                            p = product;
                            break;
                        }
                    }
                }
                if (p != null) {
                    if (cmd.startsWith("SUB_")) {
                        orderController.removeItem(p);
                        refreshOrderView();
                    } else {
                        onAddItem(p);
                    }
                }
            }
        }
    }
}
