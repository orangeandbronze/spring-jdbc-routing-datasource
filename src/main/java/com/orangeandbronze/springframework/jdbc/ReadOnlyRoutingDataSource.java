package com.orangeandbronze.springframework.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * {@link org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
 * AbstractRoutingDataSource} implementation that routes to a read-only data
 * source if the current transaction is read-only. This is useful for scaling-up
 * read-heavy database applications.
 * <p>
 * <b>Note:</b> In order to correctly determine if the current transaction is
 * read-only, it is necessary to wrap this data source with a
 * {@link org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy
 * LazyConnectionDataSourceProxy} to ensure that the connection is <em>not</em>
 * fetched during transaction creation, but during the first physical access.
 * See the LazyConnectionDataSourceProxy documentation for more details.
 * </p>
 *
 * @see org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy
 */
public class ReadOnlyRoutingDataSource extends AbstractRoutingDataSource {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private List<Object> dataSourceKeys;

	@Override
	public void setTargetDataSources(Map<Object, Object> targetDataSources) {
		super.setTargetDataSources(targetDataSources);
		this.dataSourceKeys = new ArrayList<>(targetDataSources.keySet());
	}

	@Override
	protected Object determineCurrentLookupKey() {
		if (TransactionSynchronizationManager
				.isCurrentTransactionReadOnly()
				&& !this.dataSourceKeys.isEmpty()) {
			int random = (int) Math.random() * this.dataSourceKeys.size();
			Object key = this.dataSourceKeys.get(random);
			logger.debug("Determined lookup key: {}", key);
			return key;
		}
		return null;
	}

}
