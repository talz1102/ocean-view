package com.ocean.resort.service.impl;

import com.ocean.resort.service.UserStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Component
public class MySqlUserStoreImpl implements UserStore {
	private final JdbcTemplate jdbcTemplate;

	public MySqlUserStoreImpl(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public boolean isValidCredential(String username, String rawPassword) {
		String sql = "SELECT password_hash FROM app_users WHERE username = ?";
		Optional<String> expectedHash = jdbcTemplate.query(
				sql,
				(rs, rowNum) -> rs.getString("password_hash"),
				username
		).stream().findFirst();

		if (expectedHash.isEmpty()) {
			return false;
		}

		return expectedHash.get().equals(hashPassword(rawPassword));
	}

	private String hashPassword(String rawPassword) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
			StringBuilder hex = new StringBuilder();
			for (byte b : hash) {
				hex.append(String.format("%02x", b));
			}
			return hex.toString();
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 not available", ex);
		}
	}
}
