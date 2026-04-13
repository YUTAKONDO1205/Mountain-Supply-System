package com.kondo.mss.product;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public ProductRepository(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("products")
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

    public Optional<Product> findById(long id) {
        String sql = """
                SELECT id, code, name, category, unit_price, reorder_point
                FROM products
                WHERE id = ?
                """;
        return jdbcTemplate.query(sql,
                        (rs, rowNum) -> new Product(
                                rs.getLong("id"),
                                rs.getString("code"),
                                rs.getString("name"),
                                rs.getString("category"),
                                rs.getBigDecimal("unit_price"),
                                rs.getInt("reorder_point")),
                        id)
                .stream()
                .findFirst();
    }

    public List<Product> findAll() {
        String sql = """
                SELECT id, code, name, category, unit_price, reorder_point
                FROM products
                ORDER BY id
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Product(
                rs.getLong("id"),
                rs.getString("code"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getBigDecimal("unit_price"),
                rs.getInt("reorder_point")));
    }

    public List<Product> findByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        String sql = """
                SELECT id, code, name, category, unit_price, reorder_point
                FROM products
                WHERE id IN (:ids)
                ORDER BY id
                """;
        return namedParameterJdbcTemplate.query(sql,
                Map.of("ids", ids),
                (rs, rowNum) -> new Product(
                        rs.getLong("id"),
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getBigDecimal("unit_price"),
                        rs.getInt("reorder_point")));
    }

    public Map<Long, Product> findByIdsAsMap(Collection<Long> ids) {
        return findByIds(ids).stream().collect(Collectors.toMap(Product::id, product -> product));
    }
}
