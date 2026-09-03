package com.sourcegraph.demo.bigbadmonolith.app.web;

import com.sourcegraph.demo.bigbadmonolith.app.web.testsupport.FakeHttpServletRequest;
import com.sourcegraph.demo.bigbadmonolith.app.web.testsupport.FakeHttpServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Directly exercises the {@code ViewSupport} helpers, including the error paths (forward/redirect
 * failures) and the flash-message round-trip that the servlet tests do not otherwise reach.
 */
class ViewSupportTest {

    @Test
    void renderForwardsToView() {
        FakeHttpServletRequest request = new FakeHttpServletRequest();
        FakeHttpServletResponse response = new FakeHttpServletResponse();

        ViewSupport.render(request, response, "/WEB-INF/views/index.jsp");

        assertThat(request.getLastDispatcher().wasForwarded()).isTrue();
    }

    @Test
    void renderSendsServerErrorWhenForwardFails() {
        FakeHttpServletRequest request = new FakeHttpServletRequest().withFailingForward();
        FakeHttpServletResponse response = new FakeHttpServletResponse();

        ViewSupport.render(request, response, "/WEB-INF/views/index.jsp");

        assertThat(response.getErrorCode())
            .isEqualTo(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    void redirectPrependsContextPath() {
        FakeHttpServletRequest request = new FakeHttpServletRequest();
        FakeHttpServletResponse response = new FakeHttpServletResponse();

        ViewSupport.redirect(request, response, "/customers");

        assertThat(response.getRedirectLocation()).isEqualTo("/customers");
    }

    @Test
    void redirectSwallowsIoException() {
        FakeHttpServletRequest request = new FakeHttpServletRequest();
        FakeHttpServletResponse response = new FakeHttpServletResponse().withFailingRedirect();

        ViewSupport.redirect(request, response, "/customers");

        assertThat(response.getRedirectLocation()).isNull();
    }

    @Test
    void consumeFlashMovesMessageOntoRequestAndClearsSession() {
        FakeHttpServletRequest request = new FakeHttpServletRequest();
        ViewSupport.setFlash(request, "Saved!", false);

        ViewSupport.consumeFlash(request);

        assertThat(request.getAttribute("message")).isEqualTo("Saved!");
        assertThat((Boolean) request.getAttribute("messageError")).isFalse();
        assertThat(request.getSession().getAttribute(ViewSupport.FLASH_MESSAGE)).isNull();
        assertThat(request.getSession().getAttribute(ViewSupport.FLASH_ERROR)).isNull();
    }

    @Test
    void consumeFlashDoesNothingWhenNoMessage() {
        FakeHttpServletRequest request = new FakeHttpServletRequest();

        ViewSupport.consumeFlash(request);

        assertThat(request.getAttribute("message")).isNull();
    }
}
