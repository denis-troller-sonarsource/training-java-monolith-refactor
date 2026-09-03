package com.sourcegraph.demo.bigbadmonolith.timesheet.service;

import com.sourcegraph.demo.bigbadmonolith.timesheet.api.BillableHour;
import com.sourcegraph.demo.bigbadmonolith.timesheet.api.BillableHourRepository;
import com.sourcegraph.demo.bigbadmonolith.timesheet.api.BillableHourService;
import com.sourcegraph.demo.bigbadmonolith.timesheet.repository.JdbcBillableHourRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

/**
 * Default {@link BillableHourService}, delegating persistence to a
 * {@link BillableHourRepository}.
 */
@ApplicationScoped
public class DefaultBillableHourService implements BillableHourService {

    private final BillableHourRepository billableHourRepository;

    @Inject
    public DefaultBillableHourService(BillableHourRepository billableHourRepository) {
        this.billableHourRepository = billableHourRepository;
    }

    /**
     * No-arg constructor for non-CDI callers reached via {@link java.util.ServiceLoader}
     * (see {@link com.sourcegraph.demo.bigbadmonolith.timesheet.api.Timesheet}). Classpath-mode
     * ServiceLoader requires a public no-arg constructor, so this self-wires the default JDBC
     * repository. Retired once the web layer is fully CDI-managed (Phase 5).
     */
    public DefaultBillableHourService() {
        this(new JdbcBillableHourRepository());
    }

    @Override
    public BillableHour logHour(BillableHour billableHour) {
        return billableHourRepository.save(billableHour);
    }

    @Override
    public BillableHour getHour(Long id) {
        return billableHourRepository.findById(id);
    }

    @Override
    public List<BillableHour> listHoursForCustomer(Long customerId) {
        return billableHourRepository.findByCustomerId(customerId);
    }

    @Override
    public List<BillableHour> listHoursForUser(Long userId) {
        return billableHourRepository.findByUserId(userId);
    }

    @Override
    public List<BillableHour> listHours() {
        return billableHourRepository.findAll();
    }

    @Override
    public boolean updateHour(BillableHour billableHour) {
        return billableHourRepository.update(billableHour);
    }

    @Override
    public boolean deleteHour(Long id) {
        return billableHourRepository.delete(id);
    }
}
