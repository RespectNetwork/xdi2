package xdi2.messaging.target.interceptor;

import xdi2.core.Graph;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.context.ExecutionContext;
import xdi2.messaging.exceptions.Xdi2MessagingException;
import xdi2.messaging.target.MessagingTarget;

public abstract class AbstractMessageEnvelopeInterceptor extends AbstractInterceptor<MessagingTarget> implements MessageEnvelopeInterceptor {

	@Override
	public InterceptorResult before(MessageEnvelope messageEnvelope, Graph resultGraph, ExecutionContext executionContext) throws Xdi2MessagingException {

		return InterceptorResult.DEFAULT;
	}

	@Override
	public InterceptorResult after(MessageEnvelope messageEnvelope, Graph resultGraph, ExecutionContext executionContext) throws Xdi2MessagingException {

		return InterceptorResult.DEFAULT;
	}

	@Override
	public void exception(MessageEnvelope messageEnvelope, Graph resultGraph, ExecutionContext executionContext, Exception ex) {

	}
}
