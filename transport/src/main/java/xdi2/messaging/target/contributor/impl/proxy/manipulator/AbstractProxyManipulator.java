package xdi2.messaging.target.contributor.impl.proxy.manipulator;

import xdi2.core.Graph;
import xdi2.messaging.context.ExecutionContext;
import xdi2.messaging.exceptions.Xdi2MessagingException;
import xdi2.messaging.operations.Operation;
import xdi2.messaging.request.RequestMessage;
import xdi2.messaging.request.RequestMessageEnvelope;
import xdi2.messaging.target.MessagingTarget;

public abstract class AbstractProxyManipulator implements ProxyManipulator {

	@Override
	public void init(MessagingTarget messagingTarget) throws Exception {

	}

	@Override
	public void shutdown(MessagingTarget messagingTarget) throws Exception {

	}

	@Override
	public void manipulate(RequestMessageEnvelope messageEnvelope, ExecutionContext executionContext) throws Xdi2MessagingException {

		for (RequestMessage message : messageEnvelope.getMessages()) {

			this.manipulate(message, executionContext);
		}
	}

	public void manipulate(RequestMessage message, ExecutionContext executionContext) throws Xdi2MessagingException {

		for (Operation operation : message.getOperations()) {

			this.manipulate(operation, executionContext);
		}
	}

	public void manipulate(Operation operation, ExecutionContext executionContext) throws Xdi2MessagingException {

	}

	@Override
	public void manipulate(Graph resultGraph, ExecutionContext executionContext) throws Xdi2MessagingException {

	}
}
