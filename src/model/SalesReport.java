package model;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

// Holds the aggregated and processed sales data for a specific time period.
// This is created by the ReportService by reading historical data, and it is then
// consumed by the ReportsPanel to display dashboards and metrics on the graphical interface.
public class SalesReport {

    private final String periodLabel;
    private final LocalDate from;
    private final LocalDate to;
    private final double totalRevenue;
    private final int transactionCount;
    private final List<String> topThreeItems;

    // Constructs the sales report.
    // All values are injected from the service layer after iterating and processing the sales CSV file.
    // The top items list is made unmodifiable to prevent unintended alterations to the report data.
    public SalesReport(String periodLabel,
                       LocalDate from,
                       LocalDate to,
                       double totalRevenue,
                       int transactionCount,
                       List<String> topThreeItems) {
        this.periodLabel = periodLabel;
        this.from = from;
        this.to = to;
        this.totalRevenue = totalRevenue;
        this.transactionCount = transactionCount;
        this.topThreeItems =
            Collections.unmodifiableList(topThreeItems);
    }

    // Returns the human-readable display name for this time period, such as "Today", "Week", or "Month".
    public String getPeriodLabel() {
        return periodLabel;
    }

    // Returns the start date of the period this report covers.
    public LocalDate getFrom() {
        return from;
    }

    // Returns the end date of the period this report covers.
    public LocalDate getTo() {
        return to;
    }

    // Returns the total sum of money collected across all orders within the period.
    public double getTotalRevenue() {
        return totalRevenue;
    }

    // Returns the total number of finalized orders that occurred in this period.
    public int getTransactionCount() {
        return transactionCount;
    }

    // Returns a list containing up to 3 product names.
    // They are ordered descendingly by the total quantity sold during the period.
    // The returned list is immutable.
    public List<String> getTopThreeItems() {
        return topThreeItems;
    }
}
