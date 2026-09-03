package com.sourcegraph.demo.bigbadmonolith.timesheet.api;

import java.util.List;

/**
 * Application service for the timesheet context. The public entry point other modules and the web
 * layer use instead of touching the repository directly.
 */
public interface BillableHourService {

    BillableHour logHour(BillableHour billableHour);

    BillableHour getHour(Long id);

    List<BillableHour> listHoursForCustomer(Long customerId);

    List<BillableHour> listHoursForUser(Long userId);

    List<BillableHour> listHours();

    boolean updateHour(BillableHour billableHour);

    boolean deleteHour(Long id);
}
