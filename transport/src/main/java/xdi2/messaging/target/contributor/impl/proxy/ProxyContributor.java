package xdi2.messaging.target.contributor.impl.proxy;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.client.XDIClient;
import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.client.http.XDIHttpClient;
import xdi2.core.features.nodetypes.XdiPeerRoot;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.XDIArc;
import xdi2.core.syntax.XDIStatement;
import xdi2.core.util.CopyUtil;
import xdi2.core.util.StatementUtil;
import xdi2.core.util.AddressUtil;
import xdi2.discovery.XDIDiscoveryClient;
import xdi2.discovery.XDIDiscoveryResult;
import xdi2.messaging.Message;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.MessageResult;
import xdi2.messaging.Operation;
import xdi2.messaging.context.ExecutionContext;
import xdi2.messaging.exceptions.Xdi2MessagingException;
import xdi2.messaging.target.MessagingTarget;
import xdi2.messaging.target.Prototype;
import xdi2.messaging.target.contributor.AbstractContributor;
import xdi2.messaging.target.contributor.ContributorMount;
import xdi2.messaging.target.contributor.ContributorResult;
import xdi2.messaging.target.contributor.impl.proxy.manipulator.ProxyManipulator;
import xdi2.messaging.target.interceptor.InterceptorResult;
import xdi2.messaging.target.interceptor.MessageInterceptor;
import xdi2.messaging.util.MessagingCloneUtil;

/**
 * This contributor can answer requests by forwarding them to another XDI endpoint.
 */
@ContributorMount(
		contributorXris={""}
		)
public class ProxyContributor extends AbstractContributor implements MessageInterceptor, Prototype<ProxyContributor> {

	private static final Logger log = LoggerFactory.getLogger(ProxyContributor.class);

	private XDIArc toPeerRootXri;
	private XDIClient xdiClient;
	private XDIAddress linkContractXri;

	private XDIDiscoveryClient xdiDiscoveryClient;

	private boolean skipParentContributors;
	private boolean skipSiblingContributors;
	private boolean skipMessagingTarget;
	private List<ProxyManipulator> proxyManipulators;

	public ProxyContributor() {

		this.skipParentContributors = false;
		this.skipSiblingContributors = false;
		this.skipMessagingTarget = true;
		this.proxyManipulators = new ArrayList<ProxyManipulator> ();
	}

	/*
	 * Prototype
	 */

	@Override
	public ProxyContributor instanceFor(xdi2.messaging.target.Prototype.PrototypingContext prototypingContext) throws Xdi2MessagingException {

		// done

		return this;
	}

	/*
	 * Init and shutdown
	 */

	@Override
	public void init(MessagingTarget messagingTarget) throws Exception {

		super.init(messagingTarget);

		// if we don't have an XDI discovery client, use the default one

		if (this.xdiDiscoveryClient == null) {

			this.xdiDiscoveryClient = new XDIDiscoveryClient();
		}

		// if we have a static forwarding target, but no XDI client, use XDI discovery to create one

		if (this.toPeerRootXri != null && this.xdiClient == null) {

			XDIDiscoveryResult xdiDiscoveryResult = this.getXdiDiscoveryClient().discoverFromRegistry(XdiPeerRoot.getAddressOfPeerRootArc(this.toPeerRootXri), null);

			if (xdiDiscoveryResult.getXdiEndpointUri() == null) throw new RuntimeException("Could not discover XDI endpoint URI for " + this.toPeerRootXri);

			this.xdiClient = new XDIHttpClient(xdiDiscoveryResult.getXdiEndpointUri());
		}
	}

	/*
	 * MessageInterceptor
	 */

	@Override
	public InterceptorResult before(Message message, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		// if there is a static forwarding target, we use it

		if (this.getToPeerRootXri() != null && this.getXdiClient() != null) {

			XDIArc staticForwardingTargetToPeerRootXri = this.getToPeerRootXri();
			XDIClient staticForwardingTargetXdiClient = this.getXdiClient();
			XDIAddress staticLinkContractXri = this.getLinkContractXri();

			if (log.isDebugEnabled()) log.debug("Setting static forwarding target: " + staticForwardingTargetToPeerRootXri + " (" + staticForwardingTargetXdiClient + ") with link contract XRI " + staticLinkContractXri);

			putToPeerRootXri(executionContext, staticForwardingTargetToPeerRootXri, this);
			putXdiClient(executionContext, staticForwardingTargetXdiClient, this);
			putLinkContractXri(executionContext, staticLinkContractXri, this);

			return InterceptorResult.DEFAULT;
		}

		// no static forwarding target, so we check if the target is self

		MessagingTarget messagingTarget = executionContext.getCurrentMessagingTarget();
		XDIArc ownerPeerRootXri = messagingTarget.getOwnerPeerRootXri();
		XDIArc toPeerRootXri = message.getToPeerRootXri();

		if (toPeerRootXri == null || toPeerRootXri.equals(ownerPeerRootXri)) {

			if (log.isDebugEnabled()) log.debug("Not setting any forwarding target for self request to " + ownerPeerRootXri);

			return InterceptorResult.DEFAULT;
		}

		// no static forwarding target, and target is not self, so we check if the target is local

		/* SKIP THIS FOR NOW

		Transport<?, ?> transport = executionContext.getTransport();

		if (transport instanceof HttpTransport) {

			HttpTransport httpTransport = (HttpTransport) transport;

			MessagingTargetMount messagingTargetMount;

			try {

				messagingTargetMount = httpTransport.getHttpMessagingTargetRegistry().lookup(toPeerRootXri);
			} catch (Xdi2TransportException ex) {

				throw new Xdi2MessagingException("Unable to locally look up messaging target for peer root XRI " + toPeerRootXri, ex, executionContext);
			}

			if (messagingTargetMount != null) {

				XDIArc dynamicForwardingTargetToPeerRootXri = toPeerRootXri;
				XDIClient dynamicForwardingTargetXdiClient = new XDILocalClient(messagingTargetMount.getMessagingTarget());
				XDIAddress dynamicLinkContractXri = message.getLinkContractXri();

				if (log.isDebugEnabled()) log.debug("Setting dynamic local forwarding target: " + dynamicForwardingTargetToPeerRootXri + " (" + dynamicForwardingTargetXdiClient + ") with link contract XRI " + dynamicLinkContractXri);

				putToPeerRootXri(executionContext, dynamicForwardingTargetToPeerRootXri, this);
				putXdiClient(executionContext, dynamicForwardingTargetXdiClient, this);
				putLinkContractXri(executionContext, dynamicLinkContractXri, this);

				return InterceptorResult.DEFAULT;
			}
		}

		 */

		// no static forwarding target, and target is not self, and target is not local, so we discover the remote forwarding target dynamically

		XDIDiscoveryResult xdiDiscoveryResult;

		try {

			xdiDiscoveryResult = this.getXdiDiscoveryClient().discoverFromRegistry(XdiPeerRoot.getAddressOfPeerRootArc(toPeerRootXri), null);
		} catch (Xdi2ClientException ex) {

			throw new Xdi2MessagingException("XDI Discovery failed on " + toPeerRootXri + ": " + ex.getMessage(), ex, executionContext);
		}

		if (xdiDiscoveryResult.getCloudNumber() == null) throw new Xdi2MessagingException("Could not discover Cloud Number for forwarding target at " + toPeerRootXri, null, executionContext);
		if (xdiDiscoveryResult.getXdiEndpointUri() == null) throw new Xdi2MessagingException("Could not discover XDI endpoint URI for forwarding target at " + toPeerRootXri, null, executionContext);

		XDIArc dynamicForwardingTargetToPeerRootXri = toPeerRootXri;
		XDIClient dynamicForwardingTargetXdiClient = new XDIHttpClient(xdiDiscoveryResult.getXdiEndpointUri());
		XDIAddress dynamicLinkContractXri = message.getLinkContractXri();

		if (log.isDebugEnabled()) log.debug("Setting dynamic remote forwarding target: " + dynamicForwardingTargetToPeerRootXri + " (" + dynamicForwardingTargetXdiClient + ") with link contract XRI " + dynamicLinkContractXri);

		putToPeerRootXri(executionContext, dynamicForwardingTargetToPeerRootXri, this);
		putXdiClient(executionContext, dynamicForwardingTargetXdiClient, this);
		putLinkContractXri(executionContext, dynamicLinkContractXri, this);

		return InterceptorResult.DEFAULT;
	}

	@Override
	public InterceptorResult after(Message message, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		return InterceptorResult.DEFAULT;
	}

	/*
	 * Contributor methods
	 */

	@Override
	public ContributorResult executeOnAddress(XDIAddress[] contributorXris, XDIAddress contributorsXri, XDIAddress relativeTargetAddress, Operation operation, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		// check forwarding target

		XDIArc toPeerRootXri = getToPeerRootXri(executionContext, this);
		XDIClient xdiClient = getXdiClient(executionContext, this);
		XDIAddress linkContractXri = getLinkContractXri(executionContext, this);

		if (toPeerRootXri == null || xdiClient == null || linkContractXri == null) return ContributorResult.DEFAULT;

		// prepare the forwarding message envelope

		Message message = operation.getMessage();
		if (log.isDebugEnabled()) log.debug("Preparing message for forwarding: " + message);

		XDIAddress targetAddress = AddressUtil.concatAddresses(contributorsXri, relativeTargetAddress);

		Message forwardingMessage = MessagingCloneUtil.cloneMessage(message);

		forwardingMessage.setToPeerRootXri(toPeerRootXri);
		forwardingMessage.setLinkContractXri(linkContractXri);
		forwardingMessage.deleteOperations();
		forwardingMessage.createOperation(operation.getOperationXri(), targetAddress);

		if (log.isDebugEnabled()) log.debug("Prepared message for forwarding: " + forwardingMessage);

		MessageEnvelope forwardingMessageEnvelope = forwardingMessage.getMessageEnvelope();

		// manipulate the forwarding message envelope

		for (ProxyManipulator proxyManipulator : this.proxyManipulators) {

			if (log.isDebugEnabled()) log.debug("Executing proxy manipulator " + proxyManipulator.getClass().getSimpleName() + " with operation " + operation.getOperationXri() + " on address " + targetAddress + " (message envelope).");

			proxyManipulator.manipulate(forwardingMessageEnvelope, executionContext);
		}

		// prepare the forwarding message result

		MessageResult forwardingMessageResult = new MessageResult();

		// send the forwarding message envelope

		try {

			if (log.isDebugEnabled() && this.getXdiClient() instanceof XDIHttpClient) log.debug("Forwarding operation " + operation.getOperationXri() + " on target address " + targetAddress + " to " + ((XDIHttpClient) this.getXdiClient()).getEndpointUri() + ".");

			xdiClient.send(forwardingMessageEnvelope, forwardingMessageResult);
		} catch (Xdi2ClientException ex) {

			throw new Xdi2MessagingException("Problem while forwarding XDI request: " + ex.getMessage(), ex, executionContext);
		}

		// manipulate the forwarding message result

		for (ProxyManipulator proxyManipulator : this.proxyManipulators) {

			if (log.isDebugEnabled()) log.debug("Executing proxy manipulator " + proxyManipulator.getClass().getSimpleName() + " with operation " + operation.getOperationXri() + " on address " + targetAddress + " (message result).");

			proxyManipulator.manipulate(forwardingMessageResult, executionContext);
		}

		// done

		CopyUtil.copyGraph(forwardingMessageResult.getGraph(), messageResult.getGraph(), null);

		return new ContributorResult(this.isSkipParentContributors(), this.isSkipSiblingContributors(), this.isSkipMessagingTarget());
	}

	@Override
	public ContributorResult executeOnStatement(XDIAddress[] contributorXris, XDIAddress contributorsXri, XDIStatement relativeTargetStatement, Operation operation, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		// check forwarding target

		XDIArc toPeerRootXri = getToPeerRootXri(executionContext, this);
		XDIClient xdiClient = getXdiClient(executionContext, this);
		XDIAddress linkContractXri = getLinkContractXri(executionContext, this);

		if (toPeerRootXri == null || xdiClient == null || linkContractXri == null) return ContributorResult.DEFAULT;

		// prepare the forwarding message envelope

		Message message = operation.getMessage();
		if (log.isDebugEnabled()) log.debug("Message as a basis for forwarding: " + message);

		XDIStatement targetStatement = StatementUtil.concatAddressStatement(contributorsXri, relativeTargetStatement);

		Message forwardingMessage = new MessageEnvelope().createMessage(message.getSenderAddress());

		forwardingMessage.setToPeerRootXri(toPeerRootXri);
		forwardingMessage.setLinkContractXri(linkContractXri);
		forwardingMessage.deleteOperations();
		forwardingMessage.createOperation(operation.getOperationXri(), targetStatement);

		MessageEnvelope forwardingMessageEnvelope = forwardingMessage.getMessageEnvelope();

		if (log.isDebugEnabled()) log.debug("Message envelope for forwarding: " + forwardingMessageEnvelope);

		// manipulate the forwarding message envelope

		for (ProxyManipulator proxyManipulator : this.proxyManipulators) {

			if (log.isDebugEnabled()) log.debug("Executing manipulator " + proxyManipulator.getClass().getSimpleName() + " with operation " + operation.getOperationXri() + " on statement " + targetStatement + " (message envelope).");

			proxyManipulator.manipulate(forwardingMessageEnvelope, executionContext);
		}

		if (log.isDebugEnabled()) log.debug("Manipulated message envelope for forwarding: " + forwardingMessageEnvelope);

		// prepare the forwarding message result

		MessageResult forwardingMessageResult = new MessageResult();

		// send the forwarding message envelope

		try {

			if (log.isDebugEnabled() && this.getXdiClient() instanceof XDIHttpClient) log.debug("Forwarding operation " + operation.getOperationXri() + " on target statement " + targetStatement + " to " + ((XDIHttpClient) this.getXdiClient()).getEndpointUri() + ".");

			xdiClient.send(forwardingMessageEnvelope, forwardingMessageResult);
		} catch (Xdi2ClientException ex) {

			throw new Xdi2MessagingException("Problem while forwarding XDI request: " + ex.getMessage(), ex, executionContext);
		}

		// manipulate the forwarding message result

		for (ProxyManipulator proxyManipulator : this.proxyManipulators) {

			if (log.isDebugEnabled()) log.debug("Executing manipulator " + proxyManipulator.getClass().getSimpleName() + " with operation " + operation.getOperationXri() + " on statement " + targetStatement + " (message result).");

			proxyManipulator.manipulate(forwardingMessageResult, executionContext);
		}

		if (log.isDebugEnabled()) log.debug("Manipulated message result from forwarding: " + forwardingMessageResult);

		// done

		CopyUtil.copyGraph(forwardingMessageResult.getGraph(), messageResult.getGraph(), null);

		return new ContributorResult(this.isSkipParentContributors(), this.isSkipSiblingContributors(), this.isSkipMessagingTarget());
	}

	/*
	 * Getters and setters
	 */

	public XDIArc getToPeerRootXri() {

		return this.toPeerRootXri;
	}

	public void setToPeerRootXri(XDIArc toPeerRootXri) {

		this.toPeerRootXri = toPeerRootXri;
	}

	public XDIClient getXdiClient() {

		return this.xdiClient;
	}

	public void setXdiClient(XDIClient xdiClient) {

		this.xdiClient = xdiClient;
	}

	public XDIAddress getLinkContractXri() {

		return this.linkContractXri;
	}

	public void setLinkContractXri(XDIAddress linkContractXri) {

		this.linkContractXri = linkContractXri;
	}

	public XDIDiscoveryClient getXdiDiscoveryClient() {

		return this.xdiDiscoveryClient;
	}

	public void setXdiDiscoveryClient(XDIDiscoveryClient xdiDiscoveryClient) {

		this.xdiDiscoveryClient = xdiDiscoveryClient;
	}

	public boolean isSkipParentContributors() {

		return this.skipParentContributors;
	}

	public void setSkipParentContributors(boolean skipParentContributors) {

		this.skipParentContributors = skipParentContributors;
	}

	public boolean isSkipSiblingContributors() {

		return this.skipSiblingContributors;
	}

	public void setSkipSiblingContributors(boolean skipSiblingContributors) {

		this.skipSiblingContributors = skipSiblingContributors;
	}

	public boolean isSkipMessagingTarget() {

		return this.skipMessagingTarget;
	}

	public void setSkipMessagingTarget(boolean skipMessagingTarget) {

		this.skipMessagingTarget = skipMessagingTarget;
	}

	public List<ProxyManipulator> getProxyManipulators() {

		return this.proxyManipulators;
	}

	public void setProxyManipulators(List<ProxyManipulator> proxyManipulators) {

		this.proxyManipulators = proxyManipulators;
	}

	/*
	 * ExecutionContext helper methods
	 */

	private static final String EXECUTIONCONTEXT_KEY_TO_PEER_ROOT_XRI_PER_MESSAGE = ProxyContributor.class.getCanonicalName() + "#topeerrootxripermessage";
	private static final String EXECUTIONCONTEXT_KEY_XDI_CLIENT_PER_MESSAGE = ProxyContributor.class.getCanonicalName() + "#xdiclientpermessage";
	private static final String EXECUTIONCONTEXT_KEY_LINK_CONTRACT_XRI_PER_MESSAGE = ProxyContributor.class.getCanonicalName() + "#linkcontractxri";

	public static XDIArc getToPeerRootXri(ExecutionContext executionContext, ProxyContributor proxyContributor) {

		return (XDIArc) executionContext.getMessageAttribute(EXECUTIONCONTEXT_KEY_TO_PEER_ROOT_XRI_PER_MESSAGE + Integer.toString(System.identityHashCode(proxyContributor)));
	}

	public static void putToPeerRootXri(ExecutionContext executionContext, XDIArc toPeerRootXri, ProxyContributor proxyContributor) {

		executionContext.putMessageAttribute(EXECUTIONCONTEXT_KEY_TO_PEER_ROOT_XRI_PER_MESSAGE + Integer.toString(System.identityHashCode(proxyContributor)), toPeerRootXri);
	}

	public static XDIClient getXdiClient(ExecutionContext executionContext, ProxyContributor proxyContributor) {

		return (XDIClient) executionContext.getMessageAttribute(EXECUTIONCONTEXT_KEY_XDI_CLIENT_PER_MESSAGE + Integer.toString(System.identityHashCode(proxyContributor)));
	}

	public static void putXdiClient(ExecutionContext executionContext, XDIClient xdiClient, ProxyContributor proxyContributor) {

		executionContext.putMessageAttribute(EXECUTIONCONTEXT_KEY_XDI_CLIENT_PER_MESSAGE + Integer.toString(System.identityHashCode(proxyContributor)), xdiClient);
	}

	public static XDIAddress getLinkContractXri(ExecutionContext executionContext, ProxyContributor proxyContributor) {

		return (XDIAddress) executionContext.getMessageAttribute(EXECUTIONCONTEXT_KEY_LINK_CONTRACT_XRI_PER_MESSAGE + Integer.toString(System.identityHashCode(proxyContributor)));
	}

	public static void putLinkContractXri(ExecutionContext executionContext, XDIAddress linkContractXri, ProxyContributor proxyContributor) {

		executionContext.putMessageAttribute(EXECUTIONCONTEXT_KEY_LINK_CONTRACT_XRI_PER_MESSAGE + Integer.toString(System.identityHashCode(proxyContributor)), linkContractXri);
	}
}
