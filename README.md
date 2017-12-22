# Some Implementations of Spring's AbstractRoutingDataSource

It can sometimes be useful to use a read-only replica database to increase the performance of read-heavy applications. Here, we have two:

- [ReadOnlyRoutingDataSource](https://github.com/orangeandbronze/spring-jdbc-routing-datasource/blob/master/src/main/java/com/orangeandbronze/springframework/jdbc/ReadOnlyRoutingDataSource.java)
- [TransactionDefinitionRoutingDataSource](https://github.com/orangeandbronze/spring-jdbc-routing-datasource/blob/master/src/main/java/com/orangeandbronze/springframework/jdbc/TransactionDefinitionRoutingDataSource.java)

## ReadOnlyRoutingDataSource + Spring's LazyConnectionDataSourceProxy

This uses Spring's [TransactionSynchronizationManager](http://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/transaction/support/TransactionSynchronizationManager.html). You just need to set `@Transactional(readOnly = true|false)`.

```java
@Configuration
@EnableTransactionManagement
public class ... {

    @Bean
    DataSourceTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    @Bean
    DataSource dataSource() {
        LazyConnectionDataSourceProxy ds =
                new LazyConnectionDataSourceProxy(routingDataSource());
        return ds;
    }

    @Bean
    DataSource routingDataSource() {
    	ReadOnlyRoutingDataSource rds = new ...();
    	Map<Object, Object> dataSources = new ...();
    	dataSources.put("readWrite", master());
    	dataSources.put("readOnly", replica());
    	rds.setTargetDataSources(dataSources);
    	rds.setDefaultTargetDataSource(master());
    	return rds;
    }

    @Bean
    DataSource master() {...}

    @Bean
    DataSource replica() {...}

}
```

The `LazyConnectionDataSourceProxy` was necessary since Spring will initially get a `Connection` *before* a transaction synchronization is initialized. Thus, the needed information in `TransactionSynchronizationManager` has not yet been provided when the `getConnection()` call is made. To work around this, the `LazyConnectionDataSourceProxy` provides a *dummy* connection until the first creation of a `Statement`.

A much *cleaner* implementation is provided by `TransactionDefinitionRoutingDataSource`.

## TransactionDefinitionRoutingDataSource

In an effort to avoid the `LazyConnectionDataSourceProxy` *work-around* used by `ReadOnlyRoutingDataSource`, an aspect is created to remember the `TransactionDefinition` of the current transaction (if any). This can be used by other components to determine if the current transaction is read-only.

One such component is the `TransactionDefinitionRoutingDataSource`. Instead of using `TransactionSynchronizationManager`, it uses `TransactionDefinitionInterceptor` to get the `TransactionDefinition` and determine if the current transaction is read-only. This provides a cleaner approach. Again, you just need to set `@Transactional(readOnly = true|false)`.

```java
@Configuration
@EnableTransactionManagement
@EnableAspectJAutoProxy
public class ... {

    @Bean
    TransactionDefinitionInterceptor transactionDefinitionInterceptor(
            TransactionAttributeSource transactionAttributeSource) {
        return new TransactionDefinitionInterceptor(
                transactionAttributeSource);
    }

    @Bean
    DataSourceTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    @Bean
    DataSource dataSource() {
    	TransactionDefinitionRoutingDataSource tdrds = new ...();
    	Map<Object, Object> dataSources = new ...();
    	dataSources.put("readWrite", master());
    	dataSources.put("readOnly", replica());
    	tdrds.setTargetDataSources(dataSources);
    	tdrds.setDefaultTargetDataSource(master());
    	return tdrds;
    }

    @Bean
    DataSource master() {...}

    @Bean
    DataSource replica() {...}

}
```

Please see the [configuration](https://github.com/orangeandbronze/spring-jdbc-routing-datasource/blob/master/src/test/java/com/orangeandbronze/springframework/jdbc/TransactionDefinitionRoutingDataSourceConfig.java) used in tests. You can use this as a starting point.