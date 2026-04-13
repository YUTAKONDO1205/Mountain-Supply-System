package com.kondo.mss.auth;

public record AppUser(Long id, String username, String password, String role, String fullName) {
}
