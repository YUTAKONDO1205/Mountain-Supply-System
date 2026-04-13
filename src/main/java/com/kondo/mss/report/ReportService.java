package com.kondo.mss.report;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {
    List<DailySalesResponse> findDailySales(LocalDate from, LocalDate to);

    List<MonthlySalesResponse> findMonthlySales();

    List<TopProductResponse> findTopProducts(LocalDate from, LocalDate to, int limit);

    List<CategorySalesResponse> findCategorySales(LocalDate from, LocalDate to);

    DashboardResponse loadDashboard();
}
