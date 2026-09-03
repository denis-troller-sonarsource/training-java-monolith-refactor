package com.sourcegraph.demo.bigbadmonolith.catalog.api;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Basic accessor coverage for the {@link BillingCategory} model, including the no-arg constructor
 * used by frameworks that populate via setters.
 */
class BillingCategoryTest {

    @Test
    void noArgConstructorWithSettersPopulatesFields() {
        BillingCategory category = new BillingCategory();
        category.setId(7L);
        category.setName("Development");
        category.setDescription("Dev work");
        category.setHourlyRate(new BigDecimal("150.00"));

        assertThat(category.getId()).isEqualTo(7L);
        assertThat(category.getName()).isEqualTo("Development");
        assertThat(category.getDescription()).isEqualTo("Dev work");
        assertThat(category.getHourlyRate()).isEqualByComparingTo(new BigDecimal("150.00"));
    }
}
