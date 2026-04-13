package com.kondo.mss.auth;

import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AppUserRepository {

    private final JdbcTemplate jdbcTemplate;

    public AppUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<AppUser> findByUsername(String username) {
        String sql = """
                SELECT id, username, password, role, full_name
                FROM app_users
                WHERE username = ?
                """;
        return jdbcTemplate.query(sql,
                        (rs, rowNum) -> new AppUser(
                                rs.getLong("id"),
                                rs.getString("username"),
                                rs.getString("password"),
                                rs.getString("role"),
                                rs.getString("full_name")),
                        username)
                .stream()
                .findFirst();
    }
}
