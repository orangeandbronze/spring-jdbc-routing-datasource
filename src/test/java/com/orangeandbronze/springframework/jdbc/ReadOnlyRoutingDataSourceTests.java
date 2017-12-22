package com.orangeandbronze.springframework.jdbc;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SuppressWarnings("unused")
@RunWith(SpringRunner.class)
@ContextConfiguration(classes={ ReadOnlyRoutingDataSourceConfig.class })
public class ReadOnlyRoutingDataSourceTests
	extends AbstractRoutingDataSourceTests {

}
