package xdi2.messaging.target.interceptor;

import xdi2.core.Graph;
import xdi2.messaging.Message;
import xdi2.messaging.context.ExecutionContext;
import xdi2.messaging.exceptions.Xdi2MessagingException;
import xdi2.messaging.target.MessagingTarget;

/**
 * Interceptor that is executed before and after a message is executed.
 * 
 * @author markus
 */
public interface MessageInterceptor extends Interceptor<MessagingTarget> {

	/**
	 * Run before a message is executed.
	 * @param message The message to process.
	 * @param messageResult The message result.
	 * @param executionContext The current execution context.
	 * @return True, if the message has been fully handled and the server should stop processing it.
	 */
	public InterceptorResult before(Message message, Graph resultGraph, ExecutionContext executionContext) throws Xdi2MessagingException;

	/**
	 * Run after a message is executed.
	 * @param message The message to process.
	 * @param messageResult The message result.
	 * @param executionContext The current execution context.
	 * @return True, if the message has been fully handled and the server should stop processing it.
	 */
	public InterceptorResult after(Message message, Graph resultGraph, ExecutionContext executionContext) throws Xdi2MessagingException;
}
