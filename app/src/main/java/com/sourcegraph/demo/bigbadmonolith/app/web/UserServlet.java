package com.sourcegraph.demo.bigbadmonolith.app.web;

import com.sourcegraph.demo.bigbadmonolith.billing.api.ReportService;
import com.sourcegraph.demo.bigbadmonolith.users.api.User;
import com.sourcegraph.demo.bigbadmonolith.users.api.UserService;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * User controller. GET lists users and the per-user revenue rollup (hours + revenue), computed by
 * {@link ReportService#revenueByUser()} in the billing module rather than in the view. POST adds a
 * user, then redirects back (Post/Redirect/Get) with a one-shot flash message.
 */
@WebServlet("/users")
@Dependent
public class UserServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final transient UserService userService;
    private final transient ReportService reportService;

    @Inject
    public UserServlet(UserService userService, ReportService reportService) {
        this.userService = userService;
        this.reportService = reportService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        ViewSupport.consumeFlash(request);
        try {
            request.setAttribute("userRevenue", reportService.revenueByUser());
        } catch (RuntimeException e) {
            request.setAttribute("loadError", "Error loading users: " + e.getMessage());
        }
        ViewSupport.render(request, response, "/WEB-INF/views/users.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        if ("add".equals(request.getParameter("action"))) {
            try {
                User user = new User(request.getParameter("email"), request.getParameter("name"));
                userService.createUser(user);
                ViewSupport.setFlash(request, "User added successfully!", false);
            } catch (RuntimeException e) {
                ViewSupport.setFlash(request, "Error adding user: " + e.getMessage(), true);
            }
        }
        ViewSupport.redirect(request, response, "/users");
    }
}
