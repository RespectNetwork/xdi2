package xdi2.core.impl.wrapped;

import xdi2.core.ContextNode;
import xdi2.core.Graph;
import xdi2.core.impl.AbstractGraph;
import xdi2.core.impl.memory.MemoryContextNode;
import xdi2.core.impl.memory.MemoryGraph;

public class WrappedGraph extends AbstractGraph implements Graph {

	private static final long serialVersionUID = 8979035878235290607L;

	private WrapperStore wrapperStore;
	private MemoryGraph memoryGraph;

	WrappedGraph(WrappedGraphFactory graphFactory, String identifier, WrapperStore wrapperStore, MemoryGraph memoryGraph) {

		super(graphFactory, identifier);

		this.wrapperStore = wrapperStore;
		this.memoryGraph = memoryGraph;
	}

	@Override
	public ContextNode getRootContextNode(boolean subgraph) {

		MemoryContextNode memoryContextNode = (MemoryContextNode) this.getMemoryGraph().getRootContextNode(subgraph);

		return new WrappedContextNode(this, null, memoryContextNode);
	}

	@Override
	public void close() {

		this.getWrapperStore().save(this.getMemoryGraph());
		this.getMemoryGraph().close();
	}

	@Override
	public boolean supportsTransactions() {

		return false;
	}

	@Override
	public void beginTransaction() {

	}

	@Override
	public void commitTransaction() {

		this.getWrapperStore().save(this.getMemoryGraph());
	}

	@Override
	public void rollbackTransaction() {

	}

	public WrapperStore getWrapperStore() {

		return this.wrapperStore;
	}

	public MemoryGraph getMemoryGraph() {

		return this.memoryGraph;
	}
}
