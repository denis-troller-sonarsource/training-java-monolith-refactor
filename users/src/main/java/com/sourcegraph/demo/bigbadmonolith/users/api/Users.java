package com.sourcegraph.demo.bigbadmonolith.users.api;

import java.util.ServiceLoader;

/**
 * Entry point to the users context for callers that are not yet CDI-managed (e.g. the legacy
 * bootstrap and JSP layers). Resolves the {@link UserService} implementation via {@link
 * ServiceLoader} so callers depend only on this {@code api} package, never on the internal
 * {@code service}/{@code repository} packages. Once the web layer is fully CDI-wired (Phase 5),
 * callers should {@code @Inject UserService} directly and this factory can be retired.
 */
public final class Users {

    private Users() {
        // Static factory holder.
    }

    public static UserService service() {
        return ServiceLoader.load(UserService.class)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No UserService implementation registered"));
    }
}
