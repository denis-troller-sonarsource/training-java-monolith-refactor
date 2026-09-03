package com.sourcegraph.demo.bigbadmonolith.app.web;

import com.sourcegraph.demo.bigbadmonolith.app.web.testsupport.FakeHttpServletRequest;
import com.sourcegraph.demo.bigbadmonolith.app.web.testsupport.FakeHttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HoursServletTest extends ServletTestBase {

    private HoursServlet servlet;
    private Long customerId;
    private Long userId;
    private Long categoryId;

    @BeforeEach
    void createServletAndFixtures() {
        servlet = new HoursServlet(billableHourService, customerService, userService,
            categoryService, billingService);
        customerId = seedCustomer("Acme");
        userId = seedUser("John");
        categoryId = seedCategory("Dev", "100.00");
    }

    @Test
    @SuppressWarnings("unchecked")
    void doGetForwardsToHoursViewWithDropdownsAndRecentHours() {
        seedHour(customerId, userId, categoryId, "8.00", LocalDate.of(2024, 6, 15));

        FakeHttpServletRequest request = newRequest();

        servlet.doGet(request, new FakeHttpServletResponse());

        assertThat(request.getForwardedPath()).isEqualTo("/WEB-INF/views/hours.jsp");
        assertThat((List<Object>) request.getAttribute("customers")).hasSize(1);
        assertThat((List<Object>) request.getAttribute("users")).hasSize(1);
        assertThat((List<Object>) request.getAttribute("categories")).hasSize(1);
        assertThat(request.getAttribute("today")).isNotNull();
        List<RecentHourView> recent =
            (List<RecentHourView>) request.getAttribute("recentHours");
        assertThat(recent).hasSize(1);
        assertThat(recent.get(0).customerName()).isEqualTo("Acme");
    }

    @Test
    void doPostLogCreatesHourAndRedirects() {
        FakeHttpServletRequest request = newRequest()
            .withParameter("action", "log")
            .withParameter("customerId", customerId.toString())
            .withParameter("userId", userId.toString())
            .withParameter("categoryId", categoryId.toString())
            .withParameter("hours", "5.00")
            .withParameter("note", "worked")
            .withParameter("date", "2024-06-20");
        FakeHttpServletResponse response = new FakeHttpServletResponse();

        servlet.doPost(request, response);

        assertThat(response.getRedirectLocation()).isEqualTo("/hours");
        assertThat(billableHourService.listHours()).hasSize(1);
        assertThat(request.getSession().getAttribute(ViewSupport.FLASH_MESSAGE))
            .isEqualTo("Hours logged successfully!");
    }

    @Test
    void doPostLogWithValidationErrorLogsNothing() {
        FakeHttpServletRequest request = newRequest()
            .withParameter("action", "log")
            .withParameter("customerId", customerId.toString())
            .withParameter("userId", userId.toString())
            .withParameter("categoryId", categoryId.toString())
            .withParameter("hours", "-5.00")
            .withParameter("date", "2024-06-20");

        servlet.doPost(request, new FakeHttpServletResponse());

        assertThat(billableHourService.listHours()).isEmpty();
        assertThat((Boolean) request.getSession().getAttribute(ViewSupport.FLASH_ERROR)).isTrue();
    }

    @Test
    void doPostLogWithInvalidNumberSetsErrorFlash() {
        FakeHttpServletRequest request = newRequest()
            .withParameter("action", "log")
            .withParameter("customerId", customerId.toString())
            .withParameter("userId", userId.toString())
            .withParameter("categoryId", categoryId.toString())
            .withParameter("hours", "not-a-number")
            .withParameter("date", "2024-06-20");

        servlet.doPost(request, new FakeHttpServletResponse());

        assertThat(billableHourService.listHours()).isEmpty();
        assertThat(request.getSession().getAttribute(ViewSupport.FLASH_MESSAGE))
            .isEqualTo("Error logging hours: invalid number format");
    }
}
