package com.kondo.mss.report;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;

    public ReportServiceImpl(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Override
    public List<DailySalesResponse> findDailySales(LocalDate from, LocalDate to) {
        return reportRepository.findDailySales(from, to);
    }

    @Override
    public List<MonthlySalesResponse> findMonthlySales() {
        return reportRepository.findMonthlySales();
    }

    @Override
    public List<TopProductResponse> findTopProducts(LocalDate from, LocalDate to, int limit) {
        return reportRepository.findTopProducts(from.atStartOfDay(), to.atTime(LocalTime.MAX), limit);
    }

    @Override
    public List<CategorySalesResponse> findCategorySales(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        return reportRepository.findCategorySales(start, end);
    }

    @Override
    public DashboardResponse loadDashboard() {
        return reportRepository.loadDashboard();
    }
}
