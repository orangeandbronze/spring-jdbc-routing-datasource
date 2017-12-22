package com.orangeandbronze.springframework.jdbc;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.Ordered;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAttributeSource;

/**
 * Around advice for methods that are marked as {@link Transactional}
 * that remembers the {@link TransactionDefinition} for other components
 * to use.
 * <p>
 * This is used by {@link TransactionDefinitionRoutingDataSource} to
 * determine if the current transaction is read-only.
 * </p>
 *
 * @see TransactionDefinitionRoutingDataSource
 * @see #currentTransactionDefinition()
 * @see #isCurrentTransactionReadOnly()
 */
@Aspect
public class TransactionDefinitionInterceptor implements Ordered {

	private static final ThreadLocal<TransactionDefinition> transactionDefintionHolder =
			new NamedThreadLocal<TransactionDefinition>(
					"Current aspect-driven transaction definition");

	public static TransactionDefinition currentTransactionDefinition() {
		return transactionDefintionHolder.get();
	}

	public static boolean isCurrentTransactionReadOnly() {
		return transactionDefintionHolder.get() != null ?
				transactionDefintionHolder.get().isReadOnly() : false;
	}
	
	private final TransactionAttributeSource transactionAttributeSource;

	private int order = Ordered.LOWEST_PRECEDENCE - 10; // before Spring's TransactionInterceptor

	public TransactionDefinitionInterceptor(
			TransactionAttributeSource transactionAttributeSource) {
		this.transactionAttributeSource = transactionAttributeSource;
	}

	@Around("@annotation(transactional)")
	public Object rememberTransactionDefinition(
			ProceedingJoinPoint joinPoint,
			Transactional transactional) throws Throwable {
		Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
		Class<?> targetClass = joinPoint.getTarget().getClass();
		TransactionDefinition transactionDefintion =
				transactionAttributeSource.getTransactionAttribute(method, targetClass);
		boolean restore = false;
		if (transactionDefintionHolder.get() == null) {
			// Store state to allow other components to determine if TX is read-only
			transactionDefintionHolder.set(transactionDefintion);
			restore = true;
		}
		try {
			return joinPoint.proceed();
		} finally {
			if (restore) {
				transactionDefintionHolder.set(null);
			}
		}
	}

	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public int getOrder() {
		return order;
	}

}
