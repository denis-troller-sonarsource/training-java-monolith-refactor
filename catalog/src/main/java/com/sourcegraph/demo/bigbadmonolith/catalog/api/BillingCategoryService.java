package com.sourcegraph.demo.bigbadmonolith.catalog.api;

import java.util.List;

/**
 * Application service for the catalog context. The public entry point other modules and the web
 * layer use instead of touching the repository directly.
 */
public interface BillingCategoryService {

    BillingCategory createCategory(BillingCategory category);

    BillingCategory getCategory(Long id);

    List<BillingCategory> listCategories();

    boolean updateCategory(BillingCategory category);

    boolean deleteCategory(Long id);
}
