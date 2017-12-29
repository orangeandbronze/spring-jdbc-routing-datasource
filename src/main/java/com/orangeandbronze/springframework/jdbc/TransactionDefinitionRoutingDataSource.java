package com.orangeandbronze.springframework.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
 * AbstractRoutingDataSource} implementation that routes to a read-only data
 * source if the current transaction is read-only (i.e.
 * <code>&#64;Transactional(readOnly=true)</code>). This is useful for
 * scaling-up read-heavy database applications.
 * <p>
 * <b>Note:</b> In order for this to have access to the current transaction and
 * determine if it is read-only, it is necessary to include a
 * {@link TransactionDefinitionInterceptor} which will intercept
 * {@link Transactional} methods.
 * </p>
 *
 * @see TransactionDefinitionInterceptor
 */
public class TransactionDefinitionRoutingDataSource extends AbstractRoutingDataSource {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private List<Object> dataSourceKeys;

	private final AtomicInteger currentIndex = new AtomicInteger();

	@Override
	public void setTargetDataSources(Map<Object, Object> targetDataSources) {
		super.setTargetDataSources(targetDataSources);
		this.dataSourceKeys = new ArrayList<>(targetDataSources.keySet());
	}

	@Override
	protected Object determineCurrentLookupKey() {
		if (TransactionDefinitionInterceptor
				.isCurrentTransactionReadOnly()
				&& !dataSourceKeys.isEmpty()) {
			int size = dataSourceKeys.size();
			Object key = dataSourceKeys.get(currentIndex.getAndIncrement() % size);
			logger.debug("Determined lookup key: {}", key);
			return key;
		}
		return null;
	}

}
