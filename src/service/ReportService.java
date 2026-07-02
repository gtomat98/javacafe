package service;

import model.Order;
import model.OrderItem;
import model.SalesReport;
import model.AggregatedSale;
import repository.SalesRepository;
import util.AppConstants;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

// The concrete implementation of the reporting module.
// It interfaces with the SalesRepository to fetch raw historical order data, filters it
// down into the correct time windows, and applies aggregation algorithms to calculate totals,
// transaction counts, and product rankings.
public class ReportService implements IReportService {

    private final SalesRepository salesRepository;

    // Initializes the service and automatically links it to the sales repository so it can read the CSV files.
    public ReportService() {
        this.salesRepository = new SalesRepository();
    }

    // Connects to the database and pulls exclusively the orders that have the exact current day's timestamp.
    // It then funnels these orders into the calculation engine to generate the "Today" report.
    @Override
    public SalesReport getToday() {
        LocalDate today = LocalDate.now();
        List<Order> orders =
            salesRepository.loadByDate(today);
        return buildReport(
            AppConstants.PERIOD_TODAY,
            today, today, orders);
    }

    // Connects to the database and pulls all orders starting from the most recent Monday
    // up until the upcoming (or current) Sunday. It properly handles the boundaries of the week
    // using Java's temporal adjusters, and generates the "Current Week" report.
    @Override
    public SalesReport getCurrentWeek() {
        LocalDate today = LocalDate.now();
        // The business week is defined as strictly starting on Monday.
        LocalDate monday = today.with(
            TemporalAdjusters.previousOrSame(
                DayOfWeek.MONDAY));
        LocalDate sunday = today.with(
            TemporalAdjusters.nextOrSame(
                DayOfWeek.SUNDAY));
        List<Order> orders =
            salesRepository.loadByDateRange(
                monday, sunday);
        return buildReport(
            AppConstants.PERIOD_WEEK,
            monday, sunday, orders);
    }

    // Connects to the database and pulls all orders starting from the 1st day of the current month,
    // stretching all the way to the automatically calculated last day (28, 30, 31).
    // It processes this block of data to generate the "Current Month" report.
    @Override
    public SalesReport getCurrentMonth() {
        LocalDate today = LocalDate.now();
        LocalDate firstDay = today.withDayOfMonth(1);
        LocalDate lastDay = today.with(
            TemporalAdjusters.lastDayOfMonth());
        List<Order> orders =
            salesRepository.loadByDateRange(
                firstDay, lastDay);
        return buildReport(
            AppConstants.PERIOD_MONTH,
            firstDay, lastDay, orders);
    }

    // The core calculation engine for reports. It iterates through the raw, filtered list of orders
    // and calculates the overarching financial metrics: total monetary revenue gathered across the period,
    // the absolute volume of transactions processed, and the mathematical ranking of the best-selling products.
    // It packages all this data into an immutable SalesReport object ready for UI display.
    private SalesReport buildReport(
            String periodLabel,
            LocalDate from,
            LocalDate to,
            List<Order> orders) {
        double totalRevenue = 0;
        for (Order order : orders) {
            totalRevenue += order.getTotal();
        }
        int transactionCount = orders.size();
        List<String> topThree =
            computeTopThree(orders);
        return new SalesReport(periodLabel, from, to,
            totalRevenue, transactionCount, topThree);
    }

    // Aggregation algorithm that scans every single item inside every single order passed to it.
    // It aggregates the total sales by product name using an ArrayList of AggregatedSale.
    // Afterward, it sorts the list descendingly based on volume using a basic Bubble Sort,
    // and snips off everything except the absolute top 3 performers.
    private List<String> computeTopThree(
            List<Order> orders) {
        
        List<AggregatedSale> salesList = new ArrayList<>();

        for (Order order : orders) {
            for (OrderItem item : order.getItems()) {
                String name = item.getProduct().getName();
                int qty = item.getQuantity();

                boolean found = false;
                for (AggregatedSale agg : salesList) {
                    if (agg.getProductName().equals(name)) {
                        agg.addQuantity(qty);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    salesList.add(new AggregatedSale(name, qty));
                }
            }
        }

        // Basic Bubble Sort to arrange items in descending order of quantity sold.
        int n = salesList.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (salesList.get(j).getQuantity() < salesList.get(j + 1).getQuantity()) {
                    AggregatedSale temp = salesList.get(j);
                    salesList.set(j, salesList.get(j + 1));
                    salesList.set(j + 1, temp);
                }
            }
        }

        // Safely extracts up to 3 elements, handling cases where fewer than 3 items were actually sold.
        List<String> topThree = new ArrayList<>();
        int limit = Math.min(3, salesList.size());
        for (int i = 0; i < limit; i++) {
            topThree.add(salesList.get(i).getProductName());
        }
        
        return topThree;
    }
}
