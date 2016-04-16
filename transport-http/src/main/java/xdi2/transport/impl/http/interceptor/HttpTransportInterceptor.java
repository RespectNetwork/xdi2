package xdi2.transport.impl.http.interceptor;

import java.io.IOException;

import xdi2.transport.exceptions.Xdi2TransportException;
import xdi2.transport.impl.http.HttpTransport;
import xdi2.transport.impl.http.HttpTransportRequest;
import xdi2.transport.impl.http.HttpTransportResponse;
import xdi2.transport.interceptor.TransportInterceptor;
import xdi2.transport.registry.impl.uri.UriMessagingTargetMount;

/**
 * Interceptor that is executed when it receives an incoming HTTP request.
 * 
 * @author markus
 */
public interface HttpTransportInterceptor extends TransportInterceptor {

	/**
	 * Run when the HTTP transport receives a GET request.
	 * @return True, if the request has been fully handled.
	 */
	public boolean processGetRequest(HttpTransport httpTransport, HttpTransportRequest request, HttpTransportResponse response, UriMessagingTargetMount messagingTargetMount) throws Xdi2TransportException, IOException;

	/**
	 * Run when the HTTP transport receives a POST request.
	 * @return True, if the request has been fully handled.
	 */
	public boolean processPostRequest(HttpTransport httpTransport, HttpTransportRequest request, HttpTransportResponse response, UriMessagingTargetMount messagingTargetMount) throws Xdi2TransportException, IOException;

	/**
	 * Run when the HTTP transport receives a PUT request.
	 * @return True, if the request has been fully handled.
	 */
	public boolean processPutRequest(HttpTransport httpTransport, HttpTransportRequest request, HttpTransportResponse response, UriMessagingTargetMount messagingTargetMount) throws Xdi2TransportException, IOException;

	/**
	 * Run when the HTTP transport receives a DELETE request.
	 * @return True, if the request has been fully handled.
	 */
	public boolean processDeleteRequest(HttpTransport httpTransport, HttpTransportRequest request, HttpTransportResponse response, UriMessagingTargetMount messagingTargetMount) throws Xdi2TransportException, IOException;
}
