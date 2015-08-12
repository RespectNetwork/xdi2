package xdi2.transport.impl.http.impl.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.transport.impl.http.HttpTransportRequest;
import xdi2.transport.impl.http.impl.AbstractHttpTransportRequest;

public final class ServletHttpTransportRequest extends AbstractHttpTransportRequest implements HttpTransportRequest {

	private static final Logger log = LoggerFactory.getLogger(ServletHttpTransportRequest.class);

	private HttpServletRequest httpServletRequest;
	private String method;
	private String baseUri;
	private String requestPath;

	private ServletHttpTransportRequest(HttpServletRequest httpServletRequest, String method, String baseUri, String requestPath) { 

		this.httpServletRequest = httpServletRequest;
		this.method = method;
		this.baseUri = baseUri;
		this.requestPath = requestPath;
	}

	public static ServletHttpTransportRequest fromHttpServletRequest(HttpServletRequest httpServletRequest) {

		String requestUri = httpServletRequest.getRequestURI();
		if (log.isDebugEnabled()) log.debug("Request URI: " + requestUri);

		String contextPath = httpServletRequest.getContextPath(); 
		if (contextPath.endsWith("/")) contextPath = contextPath.substring(0, contextPath.length() - 1);

		String servletPath = httpServletRequest.getServletPath();
		if (servletPath.endsWith("/")) servletPath = servletPath.substring(0, servletPath.length() - 1);

		String requestPath = requestUri.substring(contextPath.length() + servletPath.length());
		if (! requestPath.startsWith("/")) requestPath = "/" + requestPath;

		try {

			requestPath = URLDecoder.decode(requestPath, "UTF-8");
		} catch (UnsupportedEncodingException ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}

		String method = httpServletRequest.getMethod();

		String baseUri = httpServletRequest.getRequestURL().toString().substring(0, httpServletRequest.getRequestURL().length() - requestPath.length() + 1);
		if (baseUri.endsWith("/")) baseUri = baseUri.substring(0, baseUri.length() - 1);

		return new ServletHttpTransportRequest(httpServletRequest, method, baseUri, requestPath);
	}

	public HttpServletRequest getHttpServletRequest() {

		return this.httpServletRequest;
	}

	@Override
	public String getMethod() {

		return this.method;
	}

	@Override
	public String getBaseUri() {

		return this.baseUri;
	}

	@Override
	public String getRequestPath() {

		return this.requestPath;
	}

	@Override
	public String getParameter(String name) {

		return this.httpServletRequest.getParameter(name);
	}

	@Override
	public String getHeader(String name) {

		return this.httpServletRequest.getHeader(name);
	}

	@Override
	public String getContentType() {

		return this.httpServletRequest.getContentType();
	}

	@Override
	public InputStream getBodyInputStream() throws IOException {

		return this.httpServletRequest.getInputStream();
	}

	@Override
	public String getRemoteAddr() {

		return this.httpServletRequest.getRemoteAddr();
	}
}