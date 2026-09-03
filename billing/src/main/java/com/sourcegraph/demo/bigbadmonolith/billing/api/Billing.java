package com.sourcegraph.demo.bigbadmonolith.billing.api;

import java.util.ServiceLoader;

/**
 * Entry point to the billing context for callers that are not yet CDI-managed (the JSP layer).
 * Resolves the {@link BillingService} implementation via {@link ServiceLoader} so callers depend
 * only on this {@code api} package. Retired once the web layer is fully CDI-wired (Phase 5).
 */
public final class Billing {

    private Billing() {
        // Static factory holder.
    }

    public static BillingService service() {
        return ServiceLoader.load(BillingService.class)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No BillingService implementation registered"));
    }

    public static ReportService reportService() {
        return ServiceLoader.load(ReportService.class)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No ReportService implementation registered"));
    }
}
