package xdi2.messaging.target.interceptor.impl;

import xdi2.core.exceptions.Xdi2RuntimeException;
import xdi2.messaging.Message;
import xdi2.messaging.target.MessagingTarget;
import xdi2.messaging.target.Prototype;
import xdi2.messaging.target.exceptions.Xdi2MessagingException;
import xdi2.messaging.target.execution.ExecutionContext;
import xdi2.messaging.target.execution.ExecutionResult;
import xdi2.messaging.target.interceptor.InterceptorResult;
import xdi2.messaging.target.interceptor.MessageInterceptor;

/**
 * This interceptor checks if the source peer root of a message matches the sender of the message.
 * 
 * @author markus
 */
public class FromInterceptor extends AbstractInterceptor<MessagingTarget> implements MessageInterceptor, Prototype<FromInterceptor> {

	/*
	 * Prototype
	 */

	@Override
	public FromInterceptor instanceFor(PrototypingContext prototypingContext) {

		// done

		return this;
	}

	/*
	 * MessageInterceptor
	 */

	@Override
	public InterceptorResult before(Message message, ExecutionResult executionResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		throw new Xdi2RuntimeException("Not implemented.");
	}

	@Override
	public InterceptorResult after(Message message, ExecutionResult executionResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		return InterceptorResult.DEFAULT;
	}
}
