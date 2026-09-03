package com.sourcegraph.demo.bigbadmonolith.app.web;

import com.sourcegraph.demo.bigbadmonolith.billing.api.ReportService;
import com.sourcegraph.demo.bigbadmonolith.billing.api.RevenueByCategoryRow;
import com.sourcegraph.demo.bigbadmonolith.catalog.api.BillingCategory;
import com.sourcegraph.demo.bigbadmonolith.catalog.api.BillingCategoryService;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Billing-category controller. GET lists categories and the per-category revenue rollup (name,
 * rate, hours, revenue) computed by {@link ReportService#revenueByCategory()} rather than in the
 * view. POST adds a category or updates its hourly rate, then redirects back (Post/Redirect/Get).
 */
@WebServlet("/categories")
@Dependent
public class CategoryServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final transient BillingCategoryService categoryService;
    private final transient ReportService reportService;

    @Inject
    public CategoryServlet(BillingCategoryService categoryService, ReportService reportService) {
        this.categoryService = categoryService;
        this.reportService = reportService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        ViewSupport.consumeFlash(request);
        try {
            request.setAttribute("categoryRows", buildCategoryRows());
        } catch (RuntimeException e) {
            request.setAttribute("loadError", "Error loading categories: " + e.getMessage());
        }
        ViewSupport.render(request, response, "/WEB-INF/views/categories.jsp");
    }

    /**
     * Merges each catalog category (for id/description, needed by the update-rate form) with its
     * revenue rollup from {@link ReportService#revenueByCategory()} (joined by category name), sorted
     * by name to match the legacy page.
     */
    private List<CategoryRow> buildCategoryRows() {
        List<RevenueByCategoryRow> revenue = reportService.revenueByCategory();

        List<BillingCategory> categories = new ArrayList<>(categoryService.listCategories());
        categories.sort(Comparator.comparing(BillingCategory::getName));

        List<CategoryRow> rows = new ArrayList<>();
        for (BillingCategory category : categories) {
            RevenueByCategoryRow rollup = revenue.stream()
                .filter(r -> r.categoryName().equals(category.getName()))
                .findFirst()
                .orElse(null);
            BigDecimal totalHours = rollup == null ? BigDecimal.ZERO : rollup.totalHours();
            BigDecimal totalRevenue = rollup == null ? BigDecimal.ZERO : rollup.totalRevenue();
            rows.add(new CategoryRow(category.getId(), category.getName(),
                category.getDescription(), category.getHourlyRate(), totalHours, totalRevenue));
        }
        return rows;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        String action = request.getParameter("action");
        if ("update".equals(action)) {
            handleUpdateRate(request);
        } else if ("add".equals(action)) {
            handleAdd(request);
        }
        ViewSupport.redirect(request, response, "/categories");
    }

    private void handleAdd(HttpServletRequest request) {
        try {
            BigDecimal hourlyRate = new BigDecimal(request.getParameter("hourlyRate"));
            BillingCategory category = new BillingCategory(
                request.getParameter("name"), request.getParameter("description"), hourlyRate);
            categoryService.createCategory(category);
            ViewSupport.setFlash(request, "Billing category added successfully!", false);
        } catch (NumberFormatException e) {
            ViewSupport.setFlash(request, "Error: Invalid hourly rate format", true);
        } catch (RuntimeException e) {
            ViewSupport.setFlash(request, "Error adding category: " + e.getMessage(), true);
        }
    }

    private void handleUpdateRate(HttpServletRequest request) {
        try {
            Long categoryId = Long.valueOf(request.getParameter("id"));
            BigDecimal newRate = new BigDecimal(request.getParameter("newRate"));
            BillingCategory category = categoryService.getCategory(categoryId);
            if (category != null) {
                category.setHourlyRate(newRate);
                categoryService.updateCategory(category);
                ViewSupport.setFlash(request, "Hourly rate updated successfully!", false);
            } else {
                ViewSupport.setFlash(request, "Error: Category not found", true);
            }
        } catch (NumberFormatException e) {
            ViewSupport.setFlash(request, "Error: Invalid ID or rate format", true);
        } catch (RuntimeException e) {
            ViewSupport.setFlash(request, "Error updating rate: " + e.getMessage(), true);
        }
    }
}
