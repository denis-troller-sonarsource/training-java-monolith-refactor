package com.sourcegraph.demo.bigbadmonolith.service;

import com.sourcegraph.demo.bigbadmonolith.timesheet.api.BillableHour;
import com.sourcegraph.demo.bigbadmonolith.timesheet.api.BillableHourService;
import com.sourcegraph.demo.bigbadmonolith.timesheet.api.Timesheet;
import com.sourcegraph.demo.bigbadmonolith.catalog.api.BillingCategory;
import com.sourcegraph.demo.bigbadmonolith.catalog.api.BillingCategoryService;
import com.sourcegraph.demo.bigbadmonolith.catalog.api.Catalog;
import com.sourcegraph.demo.bigbadmonolith.customers.api.Customer;
import com.sourcegraph.demo.bigbadmonolith.customers.api.CustomerService;
import com.sourcegraph.demo.bigbadmonolith.customers.api.Customers;
import com.sourcegraph.demo.bigbadmonolith.common.DateTimeUtils;
import org.joda.time.LocalDate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BillingService {

    private BillableHourService billableHourService = Timesheet.service();
    private BillingCategoryService categoryService = Catalog.service();
    private CustomerService customerService = Customers.service();

    public Map<String, Object> generateCustomerBill(Long customerId) {
        Customer customer = customerService.getCustomer(customerId);
        if (customer == null) {
            throw new RuntimeException("Customer not found");
        }

        List<BillableHour> hours = billableHourService.listHoursForCustomer(customerId);
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalHours = BigDecimal.ZERO;
        
        for (BillableHour hour : hours) {
            BillingCategory category = categoryService.getCategory(hour.getCategoryId());
            if (category != null) {
                BigDecimal lineAmount = hour.getHours().multiply(category.getHourlyRate());
                totalAmount = totalAmount.add(lineAmount);
                totalHours = totalHours.add(hour.getHours());
            }
        }
        
        Map<String, Object> bill = new HashMap<>();
        bill.put("customer", customer);
        bill.put("billableHours", hours);
        bill.put("totalHours", totalHours);
        bill.put("totalAmount", totalAmount);
        bill.put("generatedDate", DateTimeUtils.getCurrentDateAndLog());
        
        return bill;
    }
    
    public Map<String, Object> generateMonthlyReport(int year, int month) {
        List<BillableHour> allHours = billableHourService.listHours();
        
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalHours = BigDecimal.ZERO;
        Map<String, BigDecimal> revenueByCategory = new HashMap<>();
        
        for (BillableHour hour : allHours) {
            LocalDate dateLogged = hour.getDateLogged();
            if (dateLogged.getYear() == year && dateLogged.getMonthOfYear() == month) {
                BillingCategory category = categoryService.getCategory(hour.getCategoryId());
                if (category != null) {
                    BigDecimal lineAmount = hour.getHours().multiply(category.getHourlyRate());
                    totalRevenue = totalRevenue.add(lineAmount);
                    totalHours = totalHours.add(hour.getHours());
                    
                    String categoryName = category.getName();
                    BigDecimal categoryRevenue = revenueByCategory.getOrDefault(categoryName, BigDecimal.ZERO);
                    revenueByCategory.put(categoryName, categoryRevenue.add(lineAmount));
                }
            }
        }
        
        Map<String, Object> report = new HashMap<>();
        report.put("year", year);
        report.put("month", month);
        report.put("totalRevenue", totalRevenue);
        report.put("totalHours", totalHours);
        report.put("revenueByCategory", revenueByCategory);
        report.put("generatedDate", LocalDate.now());
        
        return report;
    }
    

    public String validateBillableHour(BillableHour hour) {
        String validationErrors = "";
        
        Customer customer = customerService.getCustomer(hour.getCustomerId());
        if (customer == null) {
            validationErrors += "Invalid customer ID. ";
        }

        BillingCategory category = categoryService.getCategory(hour.getCategoryId());
        if (category == null) {
            validationErrors += "Invalid category ID. ";
        }

        if (hour.getHours() == null || hour.getHours().compareTo(BigDecimal.ZERO) <= 0) {
            validationErrors += "Hours must be greater than zero. ";
        }
        
        if (hour.getDateLogged() == null) {
            validationErrors += "Date logged is required. ";
        } else if (hour.getDateLogged().isAfter(LocalDate.now())) {
            validationErrors += "Date logged cannot be in the future. ";
        } else if (!DateTimeUtils.isWorkingDay(hour.getDateLogged())) {
            validationErrors += "Warning: Hours logged on weekend. ";
        }
        
        return validationErrors.trim();
    }
}
