package com.sourcegraph.demo.bigbadmonolith.catalog.api;

import java.util.List;

/**
 * Persistence contract for {@link BillingCategory}s. The public API of the catalog context;
 * implemented internally (JDBC) and consumed by {@link BillingCategoryService} and other modules via
 * this interface.
 */
public interface BillingCategoryRepository {

    BillingCategory save(BillingCategory category);

    BillingCategory findById(Long id);

    List<BillingCategory> findAll();

    boolean update(BillingCategory category);

    boolean delete(Long id);
}
