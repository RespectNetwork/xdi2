package xdi2.messaging.target.contributor.impl.proxy.manipulator.impl.signing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.core.features.signatures.Signature;
import xdi2.core.util.iterators.ReadOnlyIterator;
import xdi2.messaging.Message;
import xdi2.messaging.MessageResult;
import xdi2.messaging.target.MessagingTarget;
import xdi2.messaging.target.contributor.impl.proxy.manipulator.AbstractProxyManipulator;
import xdi2.messaging.target.contributor.impl.proxy.manipulator.ProxyManipulator;
import xdi2.messaging.target.exceptions.Xdi2MessagingException;
import xdi2.messaging.target.execution.ExecutionContext;

public class SigningProxyManipulator extends AbstractProxyManipulator implements ProxyManipulator {

	private static Logger log = LoggerFactory.getLogger(SigningProxyManipulator.class.getName());

	private Signer signer;

	/*
	 * Init and shutdown
	 */

	@Override
	public void init(MessagingTarget messagingTarget) throws Exception {

		super.init(messagingTarget);

		this.getSigner().init(messagingTarget, this);
	}

	@Override
	public void shutdown(MessagingTarget messagingTarget) throws Exception {

		super.shutdown(messagingTarget);

		this.getSigner().shutdown(messagingTarget, this);
	}

	/*
	 * ProxyManipulator
	 */

	@Override
	public void manipulate(Message message, ExecutionContext executionContext) throws Xdi2MessagingException {

		// check if the message already has a signature

		ReadOnlyIterator<Signature<?, ?>> signatures = message.getSignatures();

		if (signatures.hasNext()) {

			if (log.isWarnEnabled()) log.warn("Message " + message + " already has signature " + signatures.next());

			return;
		}

		// sign the message

		Signature<?, ?> signature = this.getSigner().sign(message);

		if (signature == null) {

			if (log.isWarnEnabled()) log.warn("Could not create signature for message " + message + " via " + this.getSigner().getClass().getSimpleName());

			return;
		}

		if (log.isDebugEnabled()) log.debug("Created signature " + signature + " for message " + message + " via " + this.getSigner().getClass().getSimpleName());
	}

	@Override
	public void manipulate(MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

	}

	/*
	 * Getters and setters
	 */

	public Signer getSigner() {

		return this.signer;
	}

	public void setSigner(Signer signer) {

		this.signer = signer;
	}
}
