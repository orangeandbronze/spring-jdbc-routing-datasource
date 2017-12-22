package com.orangeandbronze.springframework.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * Dummy service with {@link Transactional} at the class-level.
 */
@Transactional
public class DummyTransactionalService {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public DummyTransactionalService(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void doReadsAndWrites() {
		jdbcTemplate.queryForObject(
				"SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS", Long.class);
		jdbcTemplate.queryForObject(
				"SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS", Long.class);
		jdbcTemplate.queryForObject(
				"SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS", Long.class);
	}

	@Transactional(readOnly=true)
	public void doReadsOnly() {
		jdbcTemplate.queryForObject(
				"SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS", Long.class);
		jdbcTemplate.queryForObject(
				"SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS", Long.class);
		jdbcTemplate.queryForObject(
				"SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS", Long.class);
	}

}
