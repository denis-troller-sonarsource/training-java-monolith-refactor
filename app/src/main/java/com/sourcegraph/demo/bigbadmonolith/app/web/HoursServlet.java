package com.sourcegraph.demo.bigbadmonolith.app.web;

import com.sourcegraph.demo.bigbadmonolith.billing.api.BillingService;
import com.sourcegraph.demo.bigbadmonolith.catalog.api.BillingCategory;
import com.sourcegraph.demo.bigbadmonolith.catalog.api.BillingCategoryService;
import com.sourcegraph.demo.bigbadmonolith.customers.api.Customer;
import com.sourcegraph.demo.bigbadmonolith.customers.api.CustomerService;
import com.sourcegraph.demo.bigbadmonolith.timesheet.api.BillableHour;
import com.sourcegraph.demo.bigbadmonolith.timesheet.api.BillableHourService;
import com.sourcegraph.demo.bigbadmonolith.users.api.User;
import com.sourcegraph.demo.bigbadmonolith.users.api.UserService;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Log-hours controller. GET populates the customer/user/category dropdowns and the 20 most recent
 * billable hours (joined to display names into {@link RecentHourView}). POST validates the submitted
 * hour via {@link BillingService#validateBillableHour}, logs it on success, and redirects back
 * (Post/Redirect/Get) with a one-shot flash message.
 */
@WebServlet("/hours")
@Dependent
public class HoursServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final int RECENT_LIMIT = 20;

    private final transient BillableHourService billableHourService;
    private final transient CustomerService customerService;
    private final transient UserService userService;
    private final transient BillingCategoryService categoryService;
    private final transient BillingService billingService;

    @Inject
    public HoursServlet(BillableHourService billableHourService, CustomerService customerService,
                        UserService userService, BillingCategoryService categoryService,
                        BillingService billingService) {
        this.billableHourService = billableHourService;
        this.customerService = customerService;
        this.userService = userService;
        this.categoryService = categoryService;
        this.billingService = billingService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        ViewSupport.consumeFlash(request);
        try {
            List<Customer> customers = customerService.listCustomers();
            List<User> users = userService.listUsers();
            List<BillingCategory> categories = categoryService.listCategories();

            request.setAttribute("customers", customers);
            request.setAttribute("users", users);
            request.setAttribute("categories", categories);
            request.setAttribute("today", LocalDate.now(ZoneId.systemDefault()).toString());
            request.setAttribute("recentHours", buildRecentHours(customers, users, categories));
        } catch (RuntimeException e) {
            request.setAttribute("loadError", "Error loading recent hours: " + e.getMessage());
        }
        ViewSupport.render(request, response, "/WEB-INF/views/hours.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        if ("log".equals(request.getParameter("action"))) {
            handleLog(request);
        }
        ViewSupport.redirect(request, response, "/hours");
    }

    private void handleLog(HttpServletRequest request) {
        try {
            BillableHour hour = parseBillableHour(request);
            String errors = billingService.validateBillableHour(hour);
            if (errors != null && !errors.isEmpty()) {
                ViewSupport.setFlash(request, "Validation errors: " + errors, true);
                return;
            }
            billableHourService.logHour(hour);
            ViewSupport.setFlash(request, "Hours logged successfully!", false);
        } catch (NumberFormatException e) {
            ViewSupport.setFlash(request, "Error logging hours: invalid number format", true);
        } catch (RuntimeException e) {
            ViewSupport.setFlash(request, "Error logging hours: " + e.getMessage(), true);
        }
    }

    private static BillableHour parseBillableHour(HttpServletRequest request) {
        Long customerId = parseId(request.getParameter("customerId"));
        Long userId = parseId(request.getParameter("userId"));
        Long categoryId = parseId(request.getParameter("categoryId"));

        String hoursStr = request.getParameter("hours");
        BigDecimal hours = (hoursStr == null || hoursStr.trim().isEmpty())
            ? null : new BigDecimal(hoursStr.trim());

        String dateStr = request.getParameter("date");
        LocalDate logDate = (dateStr == null || dateStr.trim().isEmpty())
            ? LocalDate.now(ZoneId.systemDefault()) : LocalDate.parse(dateStr.trim());

        return new BillableHour(customerId, userId, categoryId, hours,
            request.getParameter("note"), logDate);
    }

    private static Long parseId(String value) {
        return (value == null || value.trim().isEmpty()) ? null : Long.valueOf(value.trim());
    }

    private List<RecentHourView> buildRecentHours(List<Customer> customers, List<User> users,
                                                  List<BillingCategory> categories) {
        Map<Long, Customer> customerMap = new HashMap<>();
        for (Customer customer : customers) {
            customerMap.put(customer.getId(), customer);
        }
        Map<Long, User> userMap = new HashMap<>();
        for (User user : users) {
            userMap.put(user.getId(), user);
        }
        Map<Long, BillingCategory> categoryMap = new HashMap<>();
        for (BillingCategory category : categories) {
            categoryMap.put(category.getId(), category);
        }

        List<BillableHour> hours = new ArrayList<>(billableHourService.listHours());
        hours.sort(Comparator.comparing(BillableHour::getDateLogged)
            .thenComparing(BillableHour::getCreatedAt).reversed());

        List<RecentHourView> recent = new ArrayList<>();
        for (BillableHour hour : hours) {
            if (recent.size() >= RECENT_LIMIT) {
                break;
            }
            Customer customer = customerMap.get(hour.getCustomerId());
            User user = userMap.get(hour.getUserId());
            BillingCategory category = categoryMap.get(hour.getCategoryId());
            if (customer != null && user != null && category != null) {
                BigDecimal lineTotal = hour.getHours().multiply(category.getHourlyRate());
                recent.add(new RecentHourView(hour.getDateLogged(), customer.getName(),
                    user.getName(), category.getName(), hour.getHours(),
                    category.getHourlyRate(), lineTotal, hour.getNote()));
            }
        }
        return recent;
    }
}
