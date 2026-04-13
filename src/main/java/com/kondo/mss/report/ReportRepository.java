package com.kondo.mss.report;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ReportRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReportRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<DailySalesResponse> findDailySales(LocalDate from, LocalDate to) {
        String sql = """
                SELECT CAST(o.ordered_at AS DATE) AS sale_date,
                       COUNT(DISTINCT o.id) AS order_count,
                       SUM(oi.quantity) AS total_quantity,
                       SUM(oi.line_amount) AS total_sales
                FROM orders o
                JOIN order_items oi ON oi.order_id = o.id
                WHERE CAST(o.ordered_at AS DATE) BETWEEN ? AND ?
                GROUP BY CAST(o.ordered_at AS DATE)
                ORDER BY sale_date
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new DailySalesResponse(
                rs.getDate("sale_date").toLocalDate(),
                rs.getInt("order_count"),
                rs.getInt("total_quantity"),
                rs.getBigDecimal("total_sales")), from, to);
    }

    public List<MonthlySalesResponse> findMonthlySales() {
        String sql = """
                SELECT YEAR(o.ordered_at) AS sales_year,
                       MONTH(o.ordered_at) AS sales_month,
                       COUNT(DISTINCT o.id) AS order_count,
                       SUM(oi.quantity) AS total_quantity,
                       SUM(oi.line_amount) AS total_sales
                FROM orders o
                JOIN order_items oi ON oi.order_id = o.id
                GROUP BY YEAR(o.ordered_at), MONTH(o.ordered_at)
                ORDER BY sales_year, sales_month
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new MonthlySalesResponse(
                rs.getInt("sales_year"),
                rs.getInt("sales_month"),
                rs.getInt("order_count"),
                rs.getInt("total_quantity"),
                rs.getBigDecimal("total_sales")));
    }

    public List<TopProductResponse> findTopProducts(LocalDateTime from, LocalDateTime to, int limit) {
        String sql = """
                SELECT p.id AS product_id,
                       p.code AS product_code,
                       p.name AS product_name,
                       SUM(oi.quantity) AS total_quantity,
                       SUM(oi.line_amount) AS total_sales
                FROM order_items oi
                JOIN orders o ON o.id = oi.order_id
                JOIN products p ON p.id = oi.product_id
                WHERE o.ordered_at BETWEEN ? AND ?
                GROUP BY p.id, p.code, p.name
                ORDER BY total_quantity DESC, total_sales DESC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new TopProductResponse(
                rs.getLong("product_id"),
                rs.getString("product_code"),
                rs.getString("product_name"),
                rs.getInt("total_quantity"),
                rs.getBigDecimal("total_sales")), from, to, limit);
    }

    public List<CategorySalesResponse> findCategorySales(LocalDateTime from, LocalDateTime to) {
        String sql = """
                SELECT p.category,
                       SUM(oi.quantity) AS total_quantity,
                       SUM(oi.line_amount) AS total_sales
                FROM order_items oi
                JOIN orders o ON o.id = oi.order_id
                JOIN products p ON p.id = oi.product_id
                WHERE o.ordered_at BETWEEN ? AND ?
                GROUP BY p.category
                ORDER BY total_sales DESC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new CategorySalesResponse(
                rs.getString("category"),
                rs.getInt("total_quantity"),
                rs.getBigDecimal("total_sales")), from, to);
    }

    public DashboardResponse loadDashboard() {
        Integer productCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM products", Integer.class);
        Integer lowStockCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM (
                    SELECT p.id
                    FROM products p
                    LEFT JOIN inventory_movements im ON im.product_id = p.id
                    GROUP BY p.id, p.reorder_point
                    HAVING COALESCE(SUM(im.quantity_delta), 0) <= p.reorder_point
                ) x
                """, Integer.class);
        java.math.BigDecimal todaySales = jdbcTemplate.queryForObject("""
                SELECT COALESCE(SUM(oi.line_amount), 0)
                FROM orders o
                JOIN order_items oi ON oi.order_id = o.id
                WHERE CAST(o.ordered_at AS DATE) = CURRENT_DATE
                """, java.math.BigDecimal.class);
        return new DashboardResponse(productCount, lowStockCount, todaySales);
    }
}
