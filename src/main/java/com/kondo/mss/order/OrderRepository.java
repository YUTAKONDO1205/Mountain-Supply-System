package com.kondo.mss.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert orderInsert;
    private final SimpleJdbcInsert orderItemInsert;

    public OrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.orderInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("orders")
                .usingGeneratedKeyColumns("id");
        this.orderItemInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("order_items")
                .usingGeneratedKeyColumns("id");
    }

    public long createOrder(String customerName, String orderStatus, BigDecimal totalAmount,
                            LocalDateTime orderedAt, String createdBy) {
        Number key = orderInsert.executeAndReturnKey(Map.of(
                "customer_name", customerName,
                "order_status", orderStatus,
                "total_amount", totalAmount,
                "ordered_at", orderedAt,
                "created_by", createdBy));
        return key.longValue();
    }

    public void createOrderItem(long orderId, long productId, int quantity,
                                BigDecimal unitPrice, BigDecimal lineAmount) {
        orderItemInsert.execute(Map.of(
                "order_id", orderId,
                "product_id", productId,
                "quantity", quantity,
                "unit_price", unitPrice,
                "line_amount", lineAmount));
    }

    public Optional<OrderHeader> findOrderHeaderById(long orderId) {
        String sql = """
                SELECT id, customer_name, order_status, total_amount, ordered_at, created_by
                FROM orders
                WHERE id = ?
                """;
        return jdbcTemplate.query(sql,
                        (rs, rowNum) -> new OrderHeader(
                                rs.getLong("id"),
                                rs.getString("customer_name"),
                                rs.getString("order_status"),
                                rs.getBigDecimal("total_amount"),
                                rs.getTimestamp("ordered_at").toLocalDateTime(),
                                rs.getString("created_by")),
                        orderId)
                .stream()
                .findFirst();
    }

    public List<OrderItemDetail> findOrderItemsByOrderId(long orderId) {
        String sql = """
                SELECT oi.product_id,
                       p.code AS product_code,
                       p.name AS product_name,
                       oi.quantity,
                       oi.unit_price,
                       oi.line_amount
                FROM order_items oi
                JOIN products p ON p.id = oi.product_id
                WHERE oi.order_id = ?
                ORDER BY oi.id
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new OrderItemDetail(
                rs.getLong("product_id"),
                rs.getString("product_code"),
                rs.getString("product_name"),
                rs.getInt("quantity"),
                rs.getBigDecimal("unit_price"),
                rs.getBigDecimal("line_amount")), orderId);
    }
}
