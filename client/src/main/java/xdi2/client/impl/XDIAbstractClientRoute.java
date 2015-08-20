package xdi2.client.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.client.XDIClient;
import xdi2.client.XDIClientRoute;
import xdi2.client.exceptions.Xdi2AgentException;
import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.client.manipulator.Manipulator;
import xdi2.core.ContextNode;
import xdi2.core.Graph;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.XDIArc;
import xdi2.messaging.Message;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.response.MessagingResponse;

public abstract class XDIAbstractClientRoute <CLIENT extends XDIClient> implements XDIClientRoute<CLIENT> {

	private static final Logger log = LoggerFactory.getLogger(XDIAbstractClientRoute.class);

	private XDIArc toPeerRootXDIArc;
	private ManipulatorList manipulators;

	protected XDIAbstractClientRoute(XDIArc toPeerRootXDIArc) {

		this.toPeerRootXDIArc = toPeerRootXDIArc;
		this.manipulators = new ManipulatorList();
	}

	@Override
	public final CLIENT constructXDIClient() {

		// client construction step

		CLIENT xdiClient = this.constructXDIClientInternal();

		// add manipulators if supported

		if (xdiClient instanceof XDIAbstractClient && this.getManipulators() != null) {

			((XDIAbstractClient) xdiClient).getManipulators().addManipulators(this.getManipulators());
		}

		// done

		return xdiClient;
	}

	protected abstract CLIENT constructXDIClientInternal();

	@Override
	public MessageEnvelope createMessageEnvelope() {

		return new MessageEnvelope();
	}

	@Override
	public Message createMessage(MessageEnvelope messageEnvelope, XDIAddress senderXDIAddress, long index) {

		Message message = messageEnvelope.createMessage(senderXDIAddress, index);
		if (this.getToPeerRootXDIArc() != null) message.setToPeerRootXDIArc(this.getToPeerRootXDIArc());

		return message;
	}

	@Override
	public Message createMessage(MessageEnvelope messageEnvelope, XDIAddress senderXDIAddress) {

		Message message = messageEnvelope.createMessage(senderXDIAddress);

		if (this.getToPeerRootXDIArc() != null) message.setToPeerRootXDIArc(this.getToPeerRootXDIArc());

		return message;
	}

	@Override
	public Message createMessage(MessageEnvelope messageEnvelope) {

		Message message = messageEnvelope.createMessage();
		if (this.getToPeerRootXDIArc() != null) message.setToPeerRootXDIArc(this.getToPeerRootXDIArc());

		return message;
	}

	/*
	 * $get helper methods
	 */

	@Override
	public ContextNode get(XDIAddress XDIaddress, XDIAddress senderXDIAddress, Manipulator... manipulators) throws Xdi2AgentException, Xdi2ClientException {

		// client construction step

		XDIClient xdiClient = this.constructXDIClient();

		// add manipulators if supported

		if (xdiClient instanceof XDIAbstractClient && manipulators != null) {

			((XDIAbstractClient) xdiClient).getManipulators().addManipulators(manipulators);
		}

		// message envelope construction step

		MessageEnvelope messageEnvelope = this.createMessageEnvelope();
		Message message = this.createMessage(messageEnvelope);
		message.createGetOperation(XDIaddress);

		// send the message envelope

		MessagingResponse messagingResponse = xdiClient.send(messageEnvelope);
		Graph resultGraph = messagingResponse.getResultGraph();

		// close the client

		xdiClient.close();

		// let's look for our XDI address in the message result

		ContextNode contextNode = resultGraph.getDeepContextNode(XDIaddress);

		if (contextNode == null) {

			if (log.isDebugEnabled()) log.debug("Unable to find context node. Giving up for address " + XDIaddress);
			return null;
		}

		// done

		if (log.isDebugEnabled()) log.debug("Found context node in result graph for address " + XDIaddress);
		return contextNode;
	}

	@Override
	public ContextNode get(XDIAddress XDIaddress, XDIAddress senderXDIAddress) throws Xdi2AgentException, Xdi2ClientException {

		return this.get(XDIaddress, senderXDIAddress, (Manipulator[]) null);
	}

	@Override
	public ContextNode get(XDIAddress XDIaddress, Manipulator... manipulators) throws Xdi2AgentException, Xdi2ClientException {

		return this.get(XDIaddress, null, manipulators);
	}

	@Override
	public ContextNode get(XDIAddress XDIaddress) throws Xdi2AgentException, Xdi2ClientException {

		return this.get(XDIaddress, null, (Manipulator[]) null);
	}

	/*
	 * Object methods
	 */

	@Override
	public String toString() {

		return this.getToPeerRootXDIArc() + " -> " + this.getClass().getSimpleName();
	}

	/*
	 * Getters and setters
	 */

	@Override
	public XDIArc getToPeerRootXDIArc() {

		return this.toPeerRootXDIArc;
	}

	public void setToPeerRootXDIArc(XDIArc toPeerRootXDIArc) {

		this.toPeerRootXDIArc = toPeerRootXDIArc;
	}

	public ManipulatorList getManipulators() {

		return this.manipulators;
	}

	public void setManipulators(ManipulatorList manipulators) {

		this.manipulators = manipulators;
	}
}
