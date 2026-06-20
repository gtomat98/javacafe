package service;

import model.SalesReport;

// Defines the contract for generating business intelligence and sales reports.
// It exposes querying methods that slice the historical sales data into different time frames.
public interface IReportService {

    // Analyzes the database to compile and aggregate all sales that occurred exactly today.
    SalesReport getToday();

    // Analyzes the database to compile and aggregate all sales from Monday to Sunday of the current week.
    SalesReport getCurrentWeek();

    // Analyzes the database to compile and aggregate all sales from the very first day 
    // to the final day of the current month.
    SalesReport getCurrentMonth();
}
