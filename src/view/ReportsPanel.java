package view;

import controller.InventoryController;
import model.SalesReport;
import util.CurrencyFormatter;

import javax.swing.*;
import java.awt.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// Panel for viewing sales reports.
public class ReportsPanel extends JPanel implements ActionListener {

    private final InventoryController inventoryController;
    private final JLabel periodLabel;
    private final JLabel revenueLabel;
    private final JLabel transactionsLabel;
    private final JLabel periodValueLabel;
    private final JLabel revenueValueLabel;
    private final JLabel transactionsValueLabel;
    private final DefaultListModel<String> topItemsModel;
    
    private final JButton btnToday;
    private final JButton btnWeek;
    private final JButton btnMonth;

    public ReportsPanel(InventoryController inventoryController) {
        this.inventoryController = inventoryController;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("📊 RELATÓRIOS DE VENDAS");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        topPanel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        btnToday = new JButton("📅 Hoje");
        btnToday.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnWeek = new JButton("📆 Semana Atual");
        btnWeek.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnMonth = new JButton("🗓️ Mês Atual");
        btnMonth.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        btnToday.addActionListener(this);
        btnWeek.addActionListener(this);
        btnMonth.addActionListener(this);
        
        buttonsPanel.add(btnToday);
        buttonsPanel.add(btnWeek);
        buttonsPanel.add(btnMonth);
        topPanel.add(buttonsPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(4, 1, 15, 15));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        
        periodLabel = new JLabel("Período:");
        periodLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        revenueLabel = new JLabel("Receita Total: R$ 0,00");
        revenueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        revenueLabel.setForeground(new Color(0, 102, 51));
        
        transactionsLabel = new JLabel("Nº de Transações: 0");
        transactionsLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        // Style as "Cards" to highlight
        periodLabel.setOpaque(true);
        periodLabel.setBackground(new Color(240, 240, 245));
        periodLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        revenueLabel.setOpaque(true);
        revenueLabel.setBackground(new Color(230, 255, 230));
        revenueLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(40, 167, 69)),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        transactionsLabel.setOpaque(true);
        transactionsLabel.setBackground(new Color(230, 240, 255));
        transactionsLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 123, 255)),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        centerPanel.add(periodLabel);
        centerPanel.add(revenueLabel);
        centerPanel.add(transactionsLabel);
        
        JPanel listPanel = new JPanel(new BorderLayout(5, 5));
        JLabel topLabel = new JLabel("⭐ Top 3 Mais Vendidos:");
        topLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        listPanel.add(topLabel, BorderLayout.NORTH);
        
        topItemsModel = new DefaultListModel<>();
        JList<String> topItemsList = new JList<>(topItemsModel);
        topItemsList.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        topItemsList.setBackground(new Color(250, 250, 250));
        topItemsList.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        listPanel.add(topItemsList, BorderLayout.CENTER);
        
        centerPanel.add(listPanel);
        
        add(centerPanel, BorderLayout.CENTER);
        
        periodValueLabel = null;
        revenueValueLabel = null;
        transactionsValueLabel = null;
    }
    
    public void refreshToday() {
        displayReport(inventoryController.getReport("today"));
    }

    public void displayReport(SalesReport report) {
        if (report == null) return;
        periodLabel.setText("Período: " + report.getPeriodLabel() + " (" + report.getFrom() + " a " + report.getTo() + ")");
        revenueLabel.setText("Receita Total: " + CurrencyFormatter.format(report.getTotalRevenue()));
        transactionsLabel.setText("Nº de Transações: " + report.getTransactionCount());
        
        topItemsModel.clear();
        int i = 1;
        for (String item : report.getTopThreeItems()) {
            topItemsModel.addElement(i + ". " + item);
            i++;
        }
        if (report.getTopThreeItems().isEmpty()) {
            topItemsModel.addElement("  Nenhuma venda registrada neste período.");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnToday) {
            displayReport(inventoryController.getReport("today"));
        } else if (e.getSource() == btnWeek) {
            displayReport(inventoryController.getReport("week"));
        } else if (e.getSource() == btnMonth) {
            displayReport(inventoryController.getReport("month"));
        }
    }
}

