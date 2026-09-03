package com.sourcegraph.demo.bigbadmonolith.timesheet.api;

import java.util.List;

/**
 * Persistence contract for {@link BillableHour}s. The public API of the timesheet context;
 * implemented internally (JDBC) and consumed by {@link BillableHourService} and other modules via
 * this interface.
 */
public interface BillableHourRepository {

    BillableHour save(BillableHour billableHour);

    BillableHour findById(Long id);

    List<BillableHour> findByCustomerId(Long customerId);

    List<BillableHour> findByUserId(Long userId);

    List<BillableHour> findAll();

    boolean update(BillableHour billableHour);

    boolean delete(Long id);
}
