package com.sourcegraph.demo.bigbadmonolith.app.web;

import com.sourcegraph.demo.bigbadmonolith.app.web.testsupport.FakeHttpServletRequest;
import com.sourcegraph.demo.bigbadmonolith.app.web.testsupport.FakeHttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class DashboardServletTest extends ServletTestBase {

    private DashboardServlet servlet;

    @BeforeEach
    void createServlet() {
        servlet = new DashboardServlet(customerService, userService, reportService);
    }

    @Test
    void doGetForwardsToIndexViewWithCounts() {
        Long customerId = seedCustomer("Acme");
        Long userId = seedUser("John");
        Long categoryId = seedCategory("Dev", "100.00");
        seedHour(customerId, userId, categoryId, "8.00", LocalDate.of(2024, 6, 15));
        seedCustomer("Beta");

        FakeHttpServletRequest request = newRequest();
        FakeHttpServletResponse response = new FakeHttpServletResponse();

        servlet.doGet(request, response);

        assertThat(request.getForwardedPath()).isEqualTo("/WEB-INF/views/index.jsp");
        assertThat(request.getAttribute("customerCount")).isEqualTo(2);
        assertThat(request.getAttribute("userCount")).isEqualTo(1);
        assertThat((BigDecimal) request.getAttribute("totalRevenue"))
            .isEqualByComparingTo(new BigDecimal("800.00"));
    }

    @Test
    void doGetReportsZerosWhenNoData() {
        FakeHttpServletRequest request = newRequest();

        servlet.doGet(request, new FakeHttpServletResponse());

        assertThat((Integer) request.getAttribute("customerCount")).isZero();
        assertThat((Integer) request.getAttribute("userCount")).isZero();
        assertThat((BigDecimal) request.getAttribute("totalRevenue")).isZero();
    }
}
