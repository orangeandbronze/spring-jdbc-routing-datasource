package com.orangeandbronze.springframework.jdbc;

import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.interceptor.TransactionAttributeSource;

/**
 * 
 * @see #transactionDefinitionInterceptor(TransactionAttributeSource)
 * @see #dataSource()
 */
@Configuration
@EnableTransactionManagement
@EnableAspectJAutoProxy
public class TransactionDefinitionRoutingDataSourceConfig {

	@Bean
	TransactionDefinitionInterceptor transactionDefinitionInterceptor(
			TransactionAttributeSource transactionAttributeSource) {
		return new TransactionDefinitionInterceptor(
				transactionAttributeSource);
	}

	@Bean
	DummyService someService1() {
		return new DummyService(jdbcTemplate());
	}

	@Bean
	DummyTransactionalService someService2() {
		return new DummyTransactionalService(jdbcTemplate());
	}

	@Bean
	JdbcTemplate jdbcTemplate() {
		return new JdbcTemplate(dataSource());
	}

	@Bean
	DataSourceTransactionManager transactionManager() {
		return new DataSourceTransactionManager(
				dataSource());
	}

	@Bean
	DataSource dataSource() {
		TransactionDefinitionRoutingDataSource tdrds =
				new TransactionDefinitionRoutingDataSource();
		Map<Object, Object> dataSources = new HashMap<>();
		dataSources.put("replica1", replica1());
		dataSources.put("replica2", replica2());
		tdrds.setTargetDataSources(dataSources);
		tdrds.setDefaultTargetDataSource(master());
		return tdrds;
	}

	@Bean
	DataSource master() {
		return spy(new EmbeddedDatabaseBuilder()
				.setName("master")
				.build());
	}

	@Bean
	DataSource replica1() {
		return spy(new EmbeddedDatabaseBuilder()
				.setName("replica1")
				.build());
	}

	@Bean
	DataSource replica2() {
		return spy(new EmbeddedDatabaseBuilder()
				.setName("replica2")
				.build());
	}

}
