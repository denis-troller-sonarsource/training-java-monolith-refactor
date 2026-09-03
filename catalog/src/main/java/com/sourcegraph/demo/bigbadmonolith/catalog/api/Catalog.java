package com.sourcegraph.demo.bigbadmonolith.catalog.api;

import java.util.ServiceLoader;

/**
 * Entry point to the catalog context for callers that are not yet CDI-managed (e.g. the legacy
 * bootstrap and JSP layers). Resolves the {@link BillingCategoryService} implementation via {@link
 * ServiceLoader} so callers depend only on this {@code api} package, never on the internal
 * {@code service}/{@code repository} packages. Once the web layer is fully CDI-wired (Phase 5),
 * callers should {@code @Inject BillingCategoryService} directly and this factory can be retired.
 */
public final class Catalog {

    private Catalog() {
        // Static factory holder.
    }

    public static BillingCategoryService service() {
        return ServiceLoader.load(BillingCategoryService.class)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No BillingCategoryService implementation registered"));
    }
}
