package com.orangeandbronze.springframework.jdbc;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public abstract class AbstractRoutingDataSourceTests {

	@Autowired
	private DummyService someService1;
	@Autowired
	private DummyTransactionalService someService2;
	@Autowired
	@Qualifier("master")
	private DataSource master;
	@Autowired
	@Qualifier("replica")
	private DataSource replica;

	@Before
	public void setUp() throws Exception {
		reset(master); // to zero interactions
		reset(replica); // to zero interactions
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void doNonTransactional() throws Exception {
		someService1.doNonTransactional();
		verify(master, atLeastOnce()).getConnection();
		verifyZeroInteractions(replica);
	}

	@Test
	public void doReadsAndWrites1() throws Exception {
		someService1.doReadsAndWrites();
		verify(master).getConnection();
		verifyZeroInteractions(replica);
	}

	@Test
	public void doReadsOnly1() throws Exception {
		someService1.doReadsOnly();
		verify(replica).getConnection();
		verifyZeroInteractions(master);
	}

	@Test
	public void doReadsAndWrites2() throws Exception {
		someService2.doReadsAndWrites();
		verify(master).getConnection();
		verifyZeroInteractions(replica);
	}

	@Test
	public void doReadsOnly2() throws Exception {
		someService2.doReadsOnly();
		verify(replica).getConnection();
		verifyZeroInteractions(master);
	}

}