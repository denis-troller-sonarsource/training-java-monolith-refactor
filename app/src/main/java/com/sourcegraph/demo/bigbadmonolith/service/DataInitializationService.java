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
import com.sourcegraph.demo.bigbadmonolith.users.api.User;
import com.sourcegraph.demo.bigbadmonolith.users.api.UserService;
import com.sourcegraph.demo.bigbadmonolith.users.api.Users;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@ApplicationScoped
public class DataInitializationService {

    private final UserService userService;
    private final CustomerService customerService;
    private final BillingCategoryService categoryService;
    private final BillableHourService billableHourService;

    @Inject
    public DataInitializationService(UserService userService,
                                     CustomerService customerService,
                                     BillingCategoryService categoryService,
                                     BillableHourService billableHourService) {
        this.userService = userService;
        this.customerService = customerService;
        this.categoryService = categoryService;
        this.billableHourService = billableHourService;
    }

    /**
     * No-arg constructor for non-CDI callers (the {@link
     * com.sourcegraph.demo.bigbadmonolith.StartupListener} still {@code new}s this). Self-wires
     * each context service via its {@link java.util.ServiceLoader} factory. Retired once the
     * bootstrap is fully CDI-managed.
     */
    public DataInitializationService() {
        this(Users.service(), Customers.service(), Catalog.service(), Timesheet.service());
    }

    public void initializeSampleData() {
        List<User> existingUsers = userService.listUsers();
        if (!existingUsers.isEmpty()) {
            return;
        }

        // Create sample users
        User user1 = userService.createUser(new User("john.doe@example.com", "John Doe"));
        User user2 = userService.createUser(new User("jane.smith@example.com", "Jane Smith"));
        
        // Create sample customers
        Customer customer1 = customerService.createCustomer(new Customer("Acme Corp", "billing@acme.com", "123 Business St"));
        Customer customer2 = customerService.createCustomer(new Customer("TechStart Inc", "finance@techstart.com", "456 Innovation Ave"));
        Customer customer3 = customerService.createCustomer(new Customer("MegaCorp Ltd", "accounts@megacorp.com", "789 Enterprise Blvd"));
        
        // Create billing categories
        BillingCategory devCategory = categoryService.createCategory(new BillingCategory("Development", "Software development work", new BigDecimal("150.00")));
        BillingCategory consultingCategory = categoryService.createCategory(new BillingCategory("Consulting", "Business consulting services", new BigDecimal("200.00")));
        BillingCategory supportCategory = categoryService.createCategory(new BillingCategory("Support", "Technical support and maintenance", new BigDecimal("100.00")));
        
        // Create sample billable hours
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        billableHourService.logHour(new BillableHour(customer1.getId(), user1.getId(), devCategory.getId(),
                new BigDecimal("8.50"), "Implemented user authentication module", today.minusDays(5)));

        billableHourService.logHour(new BillableHour(customer1.getId(), user2.getId(), consultingCategory.getId(),
                new BigDecimal("4.00"), "Requirements gathering session", today.minusDays(3)));

        billableHourService.logHour(new BillableHour(customer2.getId(), user1.getId(), devCategory.getId(),
                new BigDecimal("6.75"), "Database schema design and implementation", today.minusDays(2)));

        billableHourService.logHour(new BillableHour(customer2.getId(), user2.getId(), supportCategory.getId(),
                new BigDecimal("2.25"), "Payment processing support", today.minusDays(1)));

        billableHourService.logHour(new BillableHour(customer3.getId(), user1.getId(), consultingCategory.getId(),
                new BigDecimal("3.50"), "Architecture review and recommendations", today));

        billableHourService.logHour(new BillableHour(customer3.getId(), user2.getId(), devCategory.getId(),
                new BigDecimal("7.25"), "API endpoint development", today));
    }
}
