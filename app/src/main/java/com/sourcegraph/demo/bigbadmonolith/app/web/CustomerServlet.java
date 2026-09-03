package com.sourcegraph.demo.bigbadmonolith.app.web;

import com.sourcegraph.demo.bigbadmonolith.customers.api.Customer;
import com.sourcegraph.demo.bigbadmonolith.customers.api.CustomerService;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Customer controller. GET lists customers (newest first) for the view; POST performs mutations
 * (add / delete) then redirects back to the GET URL (Post/Redirect/Get) with a one-shot flash
 * message. Delete is POST-only, which fixes the destructive-GET vulnerability of the legacy JSP.
 */
@WebServlet("/customers")
@Dependent
public class CustomerServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final transient CustomerService customerService;

    @Inject
    public CustomerServlet(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        ViewSupport.consumeFlash(request);
        try {
            List<Customer> customers = new ArrayList<>(customerService.listCustomers());
            customers.sort(Comparator.comparing(Customer::getCreatedAt).reversed());
            request.setAttribute("customers", customers);
        } catch (RuntimeException e) {
            request.setAttribute("loadError", "Error loading customers: " + e.getMessage());
        }
        ViewSupport.render(request, response, "/WEB-INF/views/customers.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        String action = request.getParameter("action");
        if ("delete".equals(action)) {
            handleDelete(request);
        } else if ("add".equals(action)) {
            handleAdd(request);
        }
        ViewSupport.redirect(request, response, "/customers");
    }

    private void handleAdd(HttpServletRequest request) {
        try {
            Customer customer = new Customer(
                request.getParameter("name"),
                request.getParameter("email"),
                request.getParameter("address"));
            customerService.createCustomer(customer);
            ViewSupport.setFlash(request, "Customer added successfully!", false);
        } catch (RuntimeException e) {
            ViewSupport.setFlash(request, "Error adding customer: " + e.getMessage(), true);
        }
    }

    private void handleDelete(HttpServletRequest request) {
        try {
            Long customerId = Long.valueOf(request.getParameter("id"));
            customerService.deleteCustomer(customerId);
            ViewSupport.setFlash(request, "Customer deleted!", false);
        } catch (NumberFormatException e) {
            ViewSupport.setFlash(request, "Error: Invalid customer ID format", true);
        } catch (RuntimeException e) {
            ViewSupport.setFlash(request, "Error deleting customer: " + e.getMessage(), true);
        }
    }
}
