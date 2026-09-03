package com.sourcegraph.demo.bigbadmonolith.app.web.testsupport;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;

/**
 * {@link HttpServletResponse} test double that records the values the servlets set:
 * {@link #getRedirectLocation()} (from {@code sendRedirect}), {@link #getStatus()} and the
 * {@link #getErrorCode()} / {@link #getErrorMessage()} from {@code sendError}. The rest of the
 * interface is a no-op or throws {@link UnsupportedOperationException}, since the servlets never
 * call it.
 */
public class FakeHttpServletResponse implements HttpServletResponse {

    private String redirectLocation;
    private int status = SC_OK;
    private int errorCode = -1;
    private String errorMessage;
    private boolean committed;
    private boolean throwOnRedirect;

    /**
     * Makes {@code sendRedirect} throw {@link IOException}, so tests can exercise the redirect error
     * path in {@code ViewSupport}. Returns {@code this} for fluent setup.
     */
    public FakeHttpServletResponse withFailingRedirect() {
        this.throwOnRedirect = true;
        return this;
    }

    public String getRedirectLocation() {
        return redirectLocation;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        if (throwOnRedirect) {
            throw new IOException("simulated redirect failure");
        }
        this.redirectLocation = location;
        this.committed = true;
    }

    @Override
    public void sendRedirect(String location, int sc, boolean clearBuffer) {
        this.redirectLocation = location;
        this.status = sc;
        this.committed = true;
    }

    @Override
    public void sendError(int sc, String msg) {
        this.errorCode = sc;
        this.errorMessage = msg;
        this.status = sc;
        this.committed = true;
    }

    @Override
    public void sendError(int sc) {
        sendError(sc, null);
    }

    @Override
    public void setStatus(int sc) {
        this.status = sc;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public boolean isCommitted() {
        return committed;
    }

    // --- Everything below is unused by the servlets under test. ---

    @Override
    public void addCookie(jakarta.servlet.http.Cookie cookie) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsHeader(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String encodeURL(String url) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String encodeRedirectURL(String url) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDateHeader(String name, long date) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addDateHeader(String name, long date) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHeader(String name, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addHeader(String name, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setIntHeader(String name, int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addIntHeader(String name, int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getHeader(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return Collections.emptyList();
    }

    @Override
    public Collection<String> getHeaderNames() {
        return Collections.emptyList();
    }

    @Override
    public String getCharacterEncoding() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getContentType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServletOutputStream getOutputStream() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PrintWriter getWriter() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCharacterEncoding(String charset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setContentLength(int len) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setContentLengthLong(long len) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setContentType(String type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBufferSize(int size) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getBufferSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void flushBuffer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resetBuffer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLocale(java.util.Locale loc) {
        throw new UnsupportedOperationException();
    }

    @Override
    public java.util.Locale getLocale() {
        throw new UnsupportedOperationException();
    }
}
