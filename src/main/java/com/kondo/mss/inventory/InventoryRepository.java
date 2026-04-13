package com.kondo.mss.inventory;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

@Repository
public class InventoryRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public InventoryRepository(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("inventory_movements")
                .usingGeneratedKeyColumns("id");
    }

    public long createMovement(long productId, String movementType, int quantityDelta,
                               String referenceType, Long referenceId, String note) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("product_id", productId);
        params.put("movement_type", movementType);
        params.put("quantity_delta", quantityDelta);
        params.put("reference_type", referenceType);
        params.put("reference_id", referenceId);
        params.put("note", note);
        params.put("created_at", LocalDateTime.now());
        Number key = simpleJdbcInsert.executeAndReturnKey(params);
        return key.longValue();
    }

    public int getCurrentStock(long productId) {
        Integer stock = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(quantity_delta), 0) FROM inventory_movements WHERE product_id = ?",
                Integer.class,
                productId);
        return stock == null ? 0 : stock;
    }

    public Map<Long, Integer> getCurrentStocks(Collection<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyMap();
        }
        String sql = """
                SELECT product_id, COALESCE(SUM(quantity_delta), 0) AS current_stock
                FROM inventory_movements
                WHERE product_id IN (:productIds)
                GROUP BY product_id
                """;
        return namedParameterJdbcTemplate.query(sql, Map.of("productIds", productIds), rs -> {
            Map<Long, Integer> result = new java.util.HashMap<>();
            while (rs.next()) {
                result.put(rs.getLong("product_id"), rs.getInt("current_stock"));
            }
            return result;
        });
    }

    public List<InventoryStockResponse> findAllCurrentStocks() {
        String sql = """
                SELECT p.id AS product_id,
                       p.code,
                       p.name,
                       p.category,
                       p.reorder_point,
                       COALESCE(SUM(im.quantity_delta), 0) AS current_stock
                FROM products p
                LEFT JOIN inventory_movements im ON im.product_id = p.id
                GROUP BY p.id, p.code, p.name, p.category, p.reorder_point
                ORDER BY p.id
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new InventoryStockResponse(
                rs.getLong("product_id"),
                rs.getString("code"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getInt("reorder_point"),
                rs.getInt("current_stock")));
    }

    public List<LowStockResponse> findLowStockProducts() {
        String sql = """
                SELECT s.product_id,
                       s.code,
                       s.name,
                       s.current_stock,
                       s.reorder_point,
                       COALESCE(r.recent_30_day_sales, 0) AS recent_30_day_sales
                FROM (
                    SELECT p.id AS product_id,
                           p.code,
                           p.name,
                           p.reorder_point,
                           COALESCE(SUM(im.quantity_delta), 0) AS current_stock
                    FROM products p
                    LEFT JOIN inventory_movements im ON im.product_id = p.id
                    GROUP BY p.id, p.code, p.name, p.reorder_point
                ) s
                LEFT JOIN (
                    SELECT oi.product_id,
                           SUM(oi.quantity) AS recent_30_day_sales
                    FROM order_items oi
                    JOIN orders o ON o.id = oi.order_id
                    WHERE o.ordered_at >= DATEADD('DAY', -30, CURRENT_TIMESTAMP)
                    GROUP BY oi.product_id
                ) r ON r.product_id = s.product_id
                WHERE s.current_stock <= s.reorder_point
                ORDER BY s.current_stock ASC, s.name ASC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new LowStockResponse(
                rs.getLong("product_id"),
                rs.getString("code"),
                rs.getString("name"),
                rs.getInt("current_stock"),
                rs.getInt("reorder_point"),
                rs.getInt("recent_30_day_sales")));
    }
}
