package xdi2.messaging.target.tests.impl.graph;

import java.io.IOException;

import xdi2.core.Graph;
import xdi2.core.impl.json.memory.MemoryJSONGraphFactory;

public class MemoryJSONGraphMessagingTargetTest extends AbstractGraphMessagingTargetTest {

	private static MemoryJSONGraphFactory graphFactory = new MemoryJSONGraphFactory();

	@Override
	protected Graph openGraph(String identifier) throws IOException {

		return graphFactory.openGraph(identifier);
	}
}
