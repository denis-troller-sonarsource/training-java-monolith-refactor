package com.sourcegraph.demo.bigbadmonolith.app.web;

import com.sourcegraph.demo.bigbadmonolith.billing.api.ReportService;
import com.sourcegraph.demo.bigbadmonolith.customers.api.CustomerService;
import com.sourcegraph.demo.bigbadmonolith.users.api.UserService;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.math.BigDecimal;

/**
 * Dashboard controller. Reads the customer/user counts and the total revenue across all billable
 * hours (computed by {@link ReportService#totalRevenue()} in the billing module, not in the view),
 * sets them as request attributes and forwards to the pure-JSTL dashboard view.
 */
@WebServlet("/dashboard")
@Dependent
public class DashboardServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final transient CustomerService customerService;
    private final transient UserService userService;
    private final transient ReportService reportService;

    @Inject
    public DashboardServlet(CustomerService customerService, UserService userService,
                            ReportService reportService) {
        this.customerService = customerService;
        this.userService = userService;
        this.reportService = reportService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            request.setAttribute("customerCount", customerService.listCustomers().size());
            request.setAttribute("userCount", userService.listUsers().size());
            request.setAttribute("totalRevenue", reportService.totalRevenue());
        } catch (RuntimeException e) {
            request.setAttribute("customerCount", 0);
            request.setAttribute("userCount", 0);
            request.setAttribute("totalRevenue", BigDecimal.ZERO);
            request.setAttribute("errorMessage", "Database error: " + e.getMessage());
        }
        ViewSupport.render(request, response, "/WEB-INF/views/index.jsp");
    }
}
