package com.sourcegraph.demo.bigbadmonolith.app.web;

import com.sourcegraph.demo.bigbadmonolith.app.web.testsupport.FakeHttpServletRequest;
import com.sourcegraph.demo.bigbadmonolith.app.web.testsupport.FakeHttpServletResponse;
import com.sourcegraph.demo.bigbadmonolith.billing.api.UserRevenueRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserServletTest extends ServletTestBase {

    private UserServlet servlet;

    @BeforeEach
    void createServlet() {
        servlet = new UserServlet(userService, reportService);
    }

    @Test
    @SuppressWarnings("unchecked")
    void doGetForwardsToUsersViewWithUserRevenue() {
        seedUser("John");

        FakeHttpServletRequest request = newRequest();

        servlet.doGet(request, new FakeHttpServletResponse());

        assertThat(request.getForwardedPath()).isEqualTo("/WEB-INF/views/users.jsp");
        List<UserRevenueRow> userRevenue =
            (List<UserRevenueRow>) request.getAttribute("userRevenue");
        assertThat(userRevenue).hasSize(1);
    }

    @Test
    void doPostAddCreatesUserAndRedirects() {
        FakeHttpServletRequest request = newRequest()
            .withParameter("action", "add")
            .withParameter("email", "jane@example.com")
            .withParameter("name", "Jane");
        FakeHttpServletResponse response = new FakeHttpServletResponse();

        servlet.doPost(request, response);

        assertThat(response.getRedirectLocation()).isEqualTo("/users");
        assertThat(userService.listUsers()).hasSize(1);
        assertThat(request.getSession().getAttribute(ViewSupport.FLASH_MESSAGE))
            .isEqualTo("User added successfully!");
    }

    @Test
    void doPostWithoutAddActionCreatesNothing() {
        FakeHttpServletRequest request = newRequest().withParameter("action", "noop");
        FakeHttpServletResponse response = new FakeHttpServletResponse();

        servlet.doPost(request, response);

        assertThat(response.getRedirectLocation()).isEqualTo("/users");
        assertThat(userService.listUsers()).isEmpty();
    }
}
