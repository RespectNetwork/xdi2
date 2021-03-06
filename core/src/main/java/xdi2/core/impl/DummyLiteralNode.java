package xdi2.core.impl;

import xdi2.core.ContextNode;
import xdi2.core.LiteralNode;

public class DummyLiteralNode extends AbstractLiteralNode implements LiteralNode {

	private static final long serialVersionUID = 3101871056623742994L;

	private Object literalData;

	public DummyLiteralNode(ContextNode contextNode, Object literalData) {

		super(contextNode);

		this.literalData = literalData;
	}

	public DummyLiteralNode(Object literalData) {

		this(new DummyContextNode(new DummyGraph(null, null), null, null, null, null, null), literalData);
	}

	@Override
	public Object getLiteralData() {

		return this.literalData;
	}

	@Override
	public void setLiteralData(Object literalData) {

		this.literalData = literalData;
	}
}
