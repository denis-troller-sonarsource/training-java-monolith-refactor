package com.sourcegraph.demo.bigbadmonolith.timesheet.api;

import java.util.ServiceLoader;

/**
 * Entry point to the timesheet context for callers that are not yet CDI-managed (e.g. the legacy
 * bootstrap and JSP layers). Resolves the {@link BillableHourService} implementation via {@link
 * ServiceLoader} so callers depend only on this {@code api} package, never on the internal
 * {@code service}/{@code repository} packages. Once the web layer is fully CDI-wired (Phase 5),
 * callers should {@code @Inject BillableHourService} directly and this factory can be retired.
 */
public final class Timesheet {

    private Timesheet() {
        // Static factory holder.
    }

    public static BillableHourService service() {
        return ServiceLoader.load(BillableHourService.class)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No BillableHourService implementation registered"));
    }
}
