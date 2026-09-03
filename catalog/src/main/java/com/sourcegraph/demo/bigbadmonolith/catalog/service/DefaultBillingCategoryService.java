package com.sourcegraph.demo.bigbadmonolith.catalog.service;

import com.sourcegraph.demo.bigbadmonolith.catalog.api.BillingCategory;
import com.sourcegraph.demo.bigbadmonolith.catalog.api.BillingCategoryRepository;
import com.sourcegraph.demo.bigbadmonolith.catalog.api.BillingCategoryService;

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
