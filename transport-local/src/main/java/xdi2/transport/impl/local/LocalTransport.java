package xdi2.transport.impl.local;

import java.util.ArrayList;
import java.util.List;

import xdi2.core.Graph;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.response.TransportMessagingResponse;
import xdi2.messaging.target.MessagingTarget;
import xdi2.messaging.target.impl.graph.GraphMessagingTarget;
import xdi2.transport.exceptions.Xdi2TransportException;
import xdi2.transport.impl.AbstractTransport;

public class LocalTransport extends AbstractTransport<LocalTransportRequest, LocalTransportResponse> {

	private MessagingTarget messagingTarget;

	public LocalTransport(MessagingTarget messagingTarget) {

		super();

		this.messagingTarget = messagingTarget;
	}

	public LocalTransport(Graph graph) {

		try {

			GraphMessagingTarget messagingTarget = new GraphMessagingTarget();
			messagingTarget.setGraph(graph);
			messagingTarget.init();

			this.messagingTarget = messagingTarget;
		} catch (Exception ex) {

			throw new RuntimeException("Cannot initialize messaging target: " + ex.getMessage(), ex);
		}
	}

	/*
	 * Init and shutdown
	 */

	@Override
	public void init() throws Exception {

		super.init();
	}

	@Override
	public void shutdown() throws Exception{

		List<Exception> exs = new ArrayList<Exception> ();

		try {

			super.shutdown();
		} catch (Exception ex) {

			exs.add(ex);
		}

		// shut down messaging target

		try {

			this.messagingTarget.shutdown();

			super.shutdown();
		} catch (Exception ex) {

			exs.add(ex);
		}

		if (exs.size() > 1) throw new Exception("Multiple exceptions while shutting down: " + exs);
		if (exs.size() > 0) throw exs.get(0);
	}

	@Override
	public void execute(LocalTransportRequest request, LocalTransportResponse response) throws Xdi2TransportException {

		// read request

		MessageEnvelope messageEnvelope = request.getMessageEnvelope();

		// execute the messaging request

		TransportMessagingResponse messagingResponse = this.execute(messageEnvelope, this.getMessagingTarget(), request, response);
		if (messagingResponse == null || messagingResponse.getGraph() == null) throw new Xdi2TransportException("No messaging response.");

		// write response

		response.setMessagingResponse(messagingResponse);
	}

	public MessagingTarget getMessagingTarget() {

		return this.messagingTarget;
	}
}
