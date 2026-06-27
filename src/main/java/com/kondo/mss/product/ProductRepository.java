package com.kondo.mss.product;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepository {

    private static final RowMapper<Product> PRODUCT_ROW_MAPPER = (rs, rowNum) -> new Product(
            rs.getLong("id"),
            rs.getString("code"),
            rs.getString("name"),
            rs.getString("category"),
            rs.getBigDecimal("unit_price"),
            rs.getInt("reorder_point"),
            rs.getBoolean("is_active"));

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public ProductRepository(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("products")
                .usingColumns("code", "name", "category", "unit_price", "reorder_point")
                .usingGeneratedKeyColumns("id");
    }

    public boolean existsByCode(String code) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM products WHERE code = ?",
                Integer.class,
                code);
        return count != null && count > 0;
    }

    public long create(ProductCreateRequest request) {
        Number key = simpleJdbcInsert.executeAndReturnKey(new MapSqlParameterSource()
                .addValue("code", request.code())
                .addValue("name", request.name())
                .addValue("category", request.category())
                .addValue("unit_price", request.unitPrice())
                .addValue("reorder_point", request.reorderPoint()));
        return key.longValue();
    }

    public int deactivate(long id) {
        return jdbcTemplate.update(
                "UPDATE products SET is_active = FALSE, updated_at = CURRENT_TIMESTAMP WHERE id = ?", id);
    }

    public int update(long id, ProductUpdateRequest request) {
        String sql = """
                UPDATE products
                SET name = ?,
                    category = ?,
                    unit_price = ?,
                    reorder_point = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;
        return jdbcTemplate.update(sql,
                request.name(),
                request.category(),
                request.unitPrice(),
                request.reorderPoint(),
                id);
    }

    public Optional<Product> findById(long id) {
        String sql = """
                SELECT id, code, name, category, unit_price, reorder_point, is_active
                FROM products
                WHERE id = ?
                """;
        return jdbcTemplate.query(sql, PRODUCT_ROW_MAPPER, id)
                .stream()
                .findFirst();
    }

    public List<Product> findAll() {
        String sql = """
                SELECT id, code, name, category, unit_price, reorder_point, is_active
                FROM products
                WHERE is_active = TRUE
                ORDER BY id
                """;
        return jdbcTemplate.query(sql, PRODUCT_ROW_MAPPER);
    }

    public List<Product> findByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        String sql = """
                SELECT id, code, name, category, unit_price, reorder_point, is_active
                FROM products
                WHERE id IN (:ids)
                ORDER BY id
                """;
        return namedParameterJdbcTemplate.query(sql, Map.of("ids", ids), PRODUCT_ROW_MAPPER);
    }

    public Map<Long, Product> findByIdsAsMap(Collection<Long> ids) {
        return findByIds(ids).stream().collect(Collectors.toMap(Product::id, product -> product));
    }
}
