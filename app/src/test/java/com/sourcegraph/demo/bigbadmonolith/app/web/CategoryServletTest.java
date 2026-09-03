package com.sourcegraph.demo.bigbadmonolith.app.web;

import com.sourcegraph.demo.bigbadmonolith.app.web.testsupport.FakeHttpServletRequest;
import com.sourcegraph.demo.bigbadmonolith.app.web.testsupport.FakeHttpServletResponse;
import com.sourcegraph.demo.bigbadmonolith.catalog.api.BillingCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryServletTest extends ServletTestBase {

    private CategoryServlet servlet;

    @BeforeEach
    void createServlet() {
        servlet = new CategoryServlet(categoryService, reportService);
    }

    @Test
    @SuppressWarnings("unchecked")
    void doGetForwardsToCategoriesViewWithRows() {
        seedCategory("Dev", "100.00");

        FakeHttpServletRequest request = newRequest();

        servlet.doGet(request, new FakeHttpServletResponse());

        assertThat(request.getForwardedPath()).isEqualTo("/WEB-INF/views/categories.jsp");
        List<CategoryRow> rows = (List<CategoryRow>) request.getAttribute("categoryRows");
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).name()).isEqualTo("Dev");
        assertThat(rows.get(0).totalHours()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void doPostAddCreatesCategoryAndRedirects() {
        FakeHttpServletRequest request = newRequest()
            .withParameter("action", "add")
            .withParameter("name", "QA")
            .withParameter("description", "testing")
            .withParameter("hourlyRate", "80.00");
        FakeHttpServletResponse response = new FakeHttpServletResponse();

        servlet.doPost(request, response);

        assertThat(response.getRedirectLocation()).isEqualTo("/categories");
        assertThat(categoryService.listCategories()).hasSize(1);
    }

    @Test
    void doPostAddWithInvalidRateSetsErrorFlash() {
        FakeHttpServletRequest request = newRequest()
            .withParameter("action", "add")
            .withParameter("name", "QA")
            .withParameter("description", "testing")
            .withParameter("hourlyRate", "abc");

        servlet.doPost(request, new FakeHttpServletResponse());

        assertThat(categoryService.listCategories()).isEmpty();
        assertThat(request.getSession().getAttribute(ViewSupport.FLASH_MESSAGE))
            .isEqualTo("Error: Invalid hourly rate format");
    }

    @Test
    void doPostUpdateChangesRateAndRedirects() {
        Long categoryId = seedCategory("Dev", "100.00");
        FakeHttpServletRequest request = newRequest()
            .withParameter("action", "update")
            .withParameter("id", categoryId.toString())
            .withParameter("newRate", "125.00");
        FakeHttpServletResponse response = new FakeHttpServletResponse();

        servlet.doPost(request, response);

        assertThat(response.getRedirectLocation()).isEqualTo("/categories");
        BillingCategory updated = categoryService.getCategory(categoryId);
        assertThat(updated.getHourlyRate()).isEqualByComparingTo(new BigDecimal("125.00"));
    }
}
