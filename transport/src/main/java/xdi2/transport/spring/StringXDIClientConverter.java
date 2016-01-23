package xdi2.transport.spring;

import org.springframework.core.convert.converter.Converter;

import xdi2.client.XDIClient;
import xdi2.client.impl.http.XDIHttpClient;

public class StringXDIClientConverter implements Converter<String, XDIClient<?>> {

	@Override
	public XDIClient<?> convert(String string) {

		return new XDIHttpClient(string);
	}
}
