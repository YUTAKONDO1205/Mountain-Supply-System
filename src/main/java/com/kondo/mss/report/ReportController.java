package com.kondo.mss.report;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kondo.mss.common.ApiResponse;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/sales/daily")
    public ApiResponse<List<DailySalesResponse>> dailySales(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return new ApiResponse<>("日別売上を取得しました。", reportService.findDailySales(from, to));
    }

    @GetMapping("/sales/monthly")
    public ApiResponse<List<MonthlySalesResponse>> monthlySales() {
        return new ApiResponse<>("月別売上を取得しました。", reportService.findMonthlySales());
    }

    @GetMapping("/sales/top-products")
    public ApiResponse<List<TopProductResponse>> topProducts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "5") int limit) {
        return new ApiResponse<>("売れ筋商品を取得しました。", reportService.findTopProducts(from, to, limit));
    }

    @GetMapping("/sales/by-category")
    public ApiResponse<List<CategorySalesResponse>> categorySales(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return new ApiResponse<>("カテゴリ別売上を取得しました。", reportService.findCategorySales(from, to));
    }

    @GetMapping("/dashboard")
    public ApiResponse<DashboardResponse> dashboard() {
        return new ApiResponse<>("ダッシュボード情報を取得しました。", reportService.loadDashboard());
    }
}
