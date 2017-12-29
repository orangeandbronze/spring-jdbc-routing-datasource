package com.orangeandbronze.springframework.jdbc;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

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
	@Qualifier("replica1")
	private DataSource replica1;
	@Autowired
	@Qualifier("replica2")
	private DataSource replica2;

	@Before
	public void setUp() throws Exception {
		reset(master); // to zero interactions
		reset(replica1); // to zero interactions
		reset(replica2); // to zero interactions
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void doNonTransactional() throws Exception {
		someService1.doNonTransactional();
		verify(master, atLeastOnce()).getConnection();
		verifyZeroInteractions(replica1);
		verifyZeroInteractions(replica2);
	}

	@Test
	public void doReadsAndWrites1() throws Exception {
		someService1.doReadsAndWrites();
		verify(master).getConnection();
		verifyZeroInteractions(replica1);
		verifyZeroInteractions(replica2);
	}

	@Test
	public void doReadsOnly1() throws Exception {
		someService1.doReadsOnly();
		// Verify that one of the replicas was used
		assertEquals(1, 
				mockingDetails(replica1).getInvocations().size()
				+ mockingDetails(replica2).getInvocations().size());
		verifyZeroInteractions(master);
	}

	@Test
	public void doReadsAndWrites2() throws Exception {
		someService2.doReadsAndWrites();
		verify(master).getConnection();
		verifyZeroInteractions(replica1);
		verifyZeroInteractions(replica2);
	}

	@Test
	public void doReadsOnly2() throws Exception {
		someService2.doReadsOnly();
		// Verify that one of the replicas was used
		assertEquals(1, 
				mockingDetails(replica1).getInvocations().size()
				+ mockingDetails(replica2).getInvocations().size());
		verifyZeroInteractions(master);
	}

	@Test
	public void randomizesOnReadReplicas() throws Exception {
		someService1.doReadsOnly();
		someService2.doReadsOnly();
		someService1.doReadsOnly();
		someService2.doReadsOnly();
		// Verify that one of the replicas was used
		assertEquals(4, 
				mockingDetails(replica1).getInvocations().size()
				+ mockingDetails(replica2).getInvocations().size());
		verifyZeroInteractions(master);
	}

}