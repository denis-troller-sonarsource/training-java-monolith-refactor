package com.sourcegraph.demo.bigbadmonolith.app.web.testsupport;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

/**
 * {@link RequestDispatcher} test double that records the path it was created for and whether
 * {@link #forward} (or {@link #include}) was invoked, so tests can assert the servlet's forward
 * target without a servlet container.
 */
public class FakeRequestDispatcher implements RequestDispatcher {

    private final String path;
    private final boolean throwOnForward;
    private boolean forwarded;
    private boolean included;

    public FakeRequestDispatcher(String path) {
        this(path, false);
    }

    public FakeRequestDispatcher(String path, boolean throwOnForward) {
        this.path = path;
        this.throwOnForward = throwOnForward;
    }

    public String getPath() {
        return path;
    }

    public boolean wasForwarded() {
        return forwarded;
    }

    public boolean wasIncluded() {
        return included;
    }

    @Override
    public void forward(ServletRequest request, ServletResponse response) throws ServletException {
        if (throwOnForward) {
            throw new ServletException("simulated forward failure");
        }
        this.forwarded = true;
    }

    @Override
    public void include(ServletRequest request, ServletResponse response) {
        this.included = true;
    }
}
