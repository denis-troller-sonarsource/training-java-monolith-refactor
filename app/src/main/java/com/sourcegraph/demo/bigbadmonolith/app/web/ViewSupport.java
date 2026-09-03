package com.sourcegraph.demo.bigbadmonolith.app.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Small helpers shared by the MVC controllers: forwarding to a view and reading a one-shot flash
 * message from the session (Post/Redirect/Get). Keeps the servlets free of duplicated
 * try/catch-around-forward boilerplate and lets them avoid letting {@link ServletException} or
 * {@link IOException} escape their service methods.
 */
final class ViewSupport {

    /** Session attribute used to carry a flash message across a POST-redirect-GET. */
    static final String FLASH_MESSAGE = "flashMessage";
    /** Session attribute marking whether the flash message is an error (vs. success). */
    static final String FLASH_ERROR = "flashError";

    private static final Logger LOGGER = Logger.getLogger(ViewSupport.class.getName());

    private ViewSupport() {
        // Utility class.
    }

    /**
     * Forwards to a view under {@code /WEB-INF/views/}, translating the checked forward failures into
     * a 500 so they never escape the servlet's service method (java:S1989).
     */
    static void render(HttpServletRequest request, HttpServletResponse response, String view) {
        try {
            request.getRequestDispatcher(view).forward(request, response);
        } catch (ServletException | IOException e) {
            LOGGER.log(Level.SEVERE, e, () -> "Failed to render view " + view);
            sendServerError(response);
        }
    }

    private static void sendServerError(HttpServletResponse response) {
        try {
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to render page");
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to send error response", e);
        }
    }

    /**
     * Redirects to {@code contextRelativePath} (a path starting with "/", resolved against the
     * context root), handling the checked {@link IOException} so it never escapes the servlet's
     * service method (java:S1989).
     */
    static void redirect(HttpServletRequest request, HttpServletResponse response,
                         String contextRelativePath) {
        try {
            response.sendRedirect(request.getContextPath() + contextRelativePath);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e, () -> "Failed to redirect to " + contextRelativePath);
        }
    }

    /**
     * Moves any pending flash message from the session onto the request (so the view can display it)
     * and clears it from the session so it shows only once.
     */
    static void consumeFlash(HttpServletRequest request) {
        Object message = request.getSession().getAttribute(FLASH_MESSAGE);
        if (message != null) {
            request.setAttribute("message", message);
            request.setAttribute("messageError", request.getSession().getAttribute(FLASH_ERROR));
            request.getSession().removeAttribute(FLASH_MESSAGE);
            request.getSession().removeAttribute(FLASH_ERROR);
        }
    }

    /** Stores a one-shot flash message in the session for the subsequent redirected GET to show. */
    static void setFlash(HttpServletRequest request, String message, boolean error) {
        request.getSession().setAttribute(FLASH_MESSAGE, message);
        request.getSession().setAttribute(FLASH_ERROR, error);
    }
}
