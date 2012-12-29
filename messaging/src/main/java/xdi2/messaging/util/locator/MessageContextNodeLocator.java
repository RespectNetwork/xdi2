package xdi2.messaging.util.locator;

import xdi2.core.ContextNode;
import xdi2.core.Graph;
import xdi2.core.util.XRIUtil;
import xdi2.core.util.locator.GraphContextNodeLocator;
import xdi2.core.xri3.impl.XDI3Segment;
import xdi2.messaging.Message;

public class MessageContextNodeLocator extends GraphContextNodeLocator {

	public static final XDI3Segment XRI_FROM = new XDI3Segment("($from)");
	public static final XDI3Segment XRI_MSG = new XDI3Segment("($msg)");

	private Message message;

	public MessageContextNodeLocator(Graph graph, Message message) {

		super(graph);

		this.message = message;
	}

	@Override
	public XDI3Segment getContextNodeXri(XDI3Segment xri) {

		if (XRIUtil.startsWith(xri, XRI_MSG)) {

			XDI3Segment relativeXri = XRIUtil.relativeXri(xri, XRI_MSG);

			return new XDI3Segment("" + this.getMessage().getContextNode().getXri() + (relativeXri == null ? "" : relativeXri));
		}

		if (XRIUtil.startsWith(xri, XRI_FROM)) {

			XDI3Segment relativeXri = XRIUtil.relativeXri(xri, XRI_FROM);

			return new XDI3Segment("" + this.getMessage().getSender() + (relativeXri == null ? "" : relativeXri));
		}

		return super.getContextNodeXri(xri);
	}

	@Override
	public ContextNode locateContextNode(XDI3Segment xri) {

		if (XRIUtil.startsWith(xri, XRI_MSG)) {

			XDI3Segment relativeXri = XRIUtil.relativeXri(xri, XRI_MSG);

			ContextNode contextNode = this.getMessage().getContextNode();
			if (relativeXri != null) contextNode = contextNode.findContextNode(relativeXri, false);

			return contextNode;
		}

		if (XRIUtil.startsWith(xri, XRI_FROM)) {

			XDI3Segment relativeXri = XRIUtil.relativeXri(xri, XRI_FROM);

			ContextNode contextNode = this.getGraph().findContextNode(this.getMessage().getSender(), false);
			if (relativeXri != null) contextNode = contextNode.findContextNode(relativeXri, false);

			return contextNode;
		}

		return super.locateContextNode(xri);
	}

	public Message getMessage() {

		return this.message;
	}
}