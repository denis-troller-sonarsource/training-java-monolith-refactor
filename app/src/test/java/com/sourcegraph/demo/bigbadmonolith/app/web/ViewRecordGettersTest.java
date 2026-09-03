package com.sourcegraph.demo.bigbadmonolith.app.web;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers the JavaBean-style {@code getX()} accessors on the app.web view records. These getters exist
 * only for Jakarta Pages EL and are otherwise called only from the JSP views, so they need direct
 * assertions to be exercised.
 */
class ViewRecordGettersTest {

    @Nested
    class CategoryRowGetters {

        @Test
        void gettersReturnComponentValues() {
            CategoryRow row = new CategoryRow(1L, "Dev", "desc",
                new BigDecimal("100.00"), new BigDecimal("8.00"), new BigDecimal("800.00"));

            assertThat(row.getId()).isEqualTo(1L);
            assertThat(row.getName()).isEqualTo("Dev");
            assertThat(row.getDescription()).isEqualTo("desc");
            assertThat(row.getHourlyRate()).isEqualByComparingTo("100.00");
            assertThat(row.getTotalHours()).isEqualByComparingTo("8.00");
            assertThat(row.getTotalRevenue()).isEqualByComparingTo("800.00");
        }
    }

    @Nested
    class RecentHourViewGetters {

        @Test
        void gettersReturnComponentValues() {
            LocalDate date = LocalDate.of(2024, 6, 15);
            RecentHourView view = new RecentHourView(date, "Acme", "John", "Dev",
                new BigDecimal("8.00"), new BigDecimal("100.00"), new BigDecimal("800.00"), "note");

            assertThat(view.getDateLogged()).isEqualTo(date);
            assertThat(view.getCustomerName()).isEqualTo("Acme");
            assertThat(view.getUserName()).isEqualTo("John");
            assertThat(view.getCategoryName()).isEqualTo("Dev");
            assertThat(view.getHours()).isEqualByComparingTo("8.00");
            assertThat(view.getHourlyRate()).isEqualByComparingTo("100.00");
            assertThat(view.getLineTotal()).isEqualByComparingTo("800.00");
            assertThat(view.getNote()).isEqualTo("note");
        }
    }
}
