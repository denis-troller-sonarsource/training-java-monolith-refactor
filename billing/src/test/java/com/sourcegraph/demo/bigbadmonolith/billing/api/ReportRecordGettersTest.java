package com.sourcegraph.demo.bigbadmonolith.billing.api;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers the JavaBean-style {@code getX()} accessors on the billing.api report records. These getters
 * exist only for Jakarta Pages EL and are otherwise called only from the JSP views, so they need
 * direct assertions to be exercised.
 */
class ReportRecordGettersTest {

    @Nested
    class CustomerBillLineGetters {

        @Test
        void gettersReturnComponentValues() {
            LocalDate date = LocalDate.of(2024, 6, 15);
            CustomerBillLine line = new CustomerBillLine(date, "John", "Dev",
                new BigDecimal("8.00"), new BigDecimal("100.00"), new BigDecimal("800.00"), "note");

            assertThat(line.getDateLogged()).isEqualTo(date);
            assertThat(line.getUserName()).isEqualTo("John");
            assertThat(line.getCategoryName()).isEqualTo("Dev");
            assertThat(line.getHours()).isEqualByComparingTo("8.00");
            assertThat(line.getHourlyRate()).isEqualByComparingTo("100.00");
            assertThat(line.getLineTotal()).isEqualByComparingTo("800.00");
            assertThat(line.getNote()).isEqualTo("note");
        }
    }

    @Nested
    class CustomerBillReportGetters {

        @Test
        void gettersReturnComponentValues() {
            List<CustomerBillLine> lines = List.of();
            CustomerBillReport report = new CustomerBillReport("Acme", "acme@example.com",
                lines, new BigDecimal("8.00"), new BigDecimal("800.00"));

            assertThat(report.getCustomerName()).isEqualTo("Acme");
            assertThat(report.getCustomerEmail()).isEqualTo("acme@example.com");
            assertThat(report.getLines()).isSameAs(lines);
            assertThat(report.getTotalHours()).isEqualByComparingTo("8.00");
            assertThat(report.getTotalAmount()).isEqualByComparingTo("800.00");
        }
    }

    @Nested
    class MonthlySummaryRowGetters {

        @Test
        void gettersReturnComponentValues() {
            MonthlySummaryRow row =
                new MonthlySummaryRow("Acme", new BigDecimal("8.00"), new BigDecimal("800.00"));

            assertThat(row.getCustomerName()).isEqualTo("Acme");
            assertThat(row.getTotalHours()).isEqualByComparingTo("8.00");
            assertThat(row.getTotalAmount()).isEqualByComparingTo("800.00");
        }
    }

    @Nested
    class RevenueByCustomerRowGetters {

        @Test
        void gettersReturnComponentValues() {
            RevenueByCustomerRow row = new RevenueByCustomerRow("Acme",
                new BigDecimal("8.00"), new BigDecimal("800.00"), new BigDecimal("100.00"));

            assertThat(row.getCustomerName()).isEqualTo("Acme");
            assertThat(row.getTotalHours()).isEqualByComparingTo("8.00");
            assertThat(row.getTotalRevenue()).isEqualByComparingTo("800.00");
            assertThat(row.getAverageRate()).isEqualByComparingTo("100.00");
        }
    }

    @Nested
    class RevenueByCategoryRowGetters {

        @Test
        void gettersReturnComponentValues() {
            RevenueByCategoryRow row = new RevenueByCategoryRow("Dev",
                new BigDecimal("100.00"), new BigDecimal("8.00"), new BigDecimal("800.00"));

            assertThat(row.getCategoryName()).isEqualTo("Dev");
            assertThat(row.getHourlyRate()).isEqualByComparingTo("100.00");
            assertThat(row.getTotalHours()).isEqualByComparingTo("8.00");
            assertThat(row.getTotalRevenue()).isEqualByComparingTo("800.00");
        }
    }

    @Nested
    class UserRevenueRowGetters {

        @Test
        void gettersReturnComponentValues() {
            UserRevenueRow row = new UserRevenueRow(1L, "John", "john@example.com",
                new BigDecimal("8.00"), new BigDecimal("800.00"));

            assertThat(row.getUserId()).isEqualTo(1L);
            assertThat(row.getUserName()).isEqualTo("John");
            assertThat(row.getUserEmail()).isEqualTo("john@example.com");
            assertThat(row.getTotalHours()).isEqualByComparingTo("8.00");
            assertThat(row.getTotalRevenue()).isEqualByComparingTo("800.00");
        }
    }
}
