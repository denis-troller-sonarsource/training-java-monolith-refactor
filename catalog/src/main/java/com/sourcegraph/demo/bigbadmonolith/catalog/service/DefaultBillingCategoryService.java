package com.sourcegraph.demo.bigbadmonolith.catalog.service;

import com.sourcegraph.demo.bigbadmonolith.catalog.api.BillingCategory;
import com.sourcegraph.demo.bigbadmonolith.catalog.api.BillingCategoryRepository;
import com.sourcegraph.demo.bigbadmonolith.catalog.api.BillingCategoryService;
import com.sourcegraph.demo.bigbadmonolith.catalog.repository.JdbcBillingCategoryRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

/**
 * Default {@link BillingCategoryService}, delegating persistence to a
 * {@link BillingCategoryRepository}.
 */
@ApplicationScoped
public class DefaultBillingCategoryService implements BillingCategoryService {

    private final BillingCategoryRepository billingCategoryRepository;

    @Inject
    public DefaultBillingCategoryService(BillingCategoryRepository billingCategoryRepository) {
        this.billingCategoryRepository = billingCategoryRepository;
    }

    /**
     * No-arg constructor for non-CDI callers reached via {@link java.util.ServiceLoader}
     * (see {@link com.sourcegraph.demo.bigbadmonolith.catalog.api.Catalog}). Classpath-mode
     * ServiceLoader requires a public no-arg constructor, so this self-wires the default JDBC
     * repository. Retired once the web layer is fully CDI-managed (Phase 5).
     */
    public DefaultBillingCategoryService() {
        this(new JdbcBillingCategoryRepository());
    }

    @Override
    public BillingCategory createCategory(BillingCategory category) {
        return billingCategoryRepository.save(category);
    }

    @Override
    public BillingCategory getCategory(Long id) {
        return billingCategoryRepository.findById(id);
    }

    @Override
    public List<BillingCategory> listCategories() {
        return billingCategoryRepository.findAll();
    }

    @Override
    public boolean updateCategory(BillingCategory category) {
        return billingCategoryRepository.update(category);
    }

    @Override
    public boolean deleteCategory(Long id) {
        return billingCategoryRepository.delete(id);
    }
}
