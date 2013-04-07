package xdi2.core.features.contextfunctions;

import xdi2.core.ContextNode;
import xdi2.core.features.roots.XdiRoot;
import xdi2.core.xri3.XDI3SubSegment;

/**
 * An XDI subgraph according to the multiplicity pattern, represented as a context node.
 * 
 * @author markus
 */
public abstract class XdiAbstractSubGraph implements XdiSubGraph {

	private static final long serialVersionUID = -8756059289169602694L;

	private ContextNode contextNode;

	protected XdiAbstractSubGraph(ContextNode contextNode) {

		if (contextNode == null) throw new NullPointerException();

		this.contextNode = contextNode;
	}

	@Override
	public ContextNode getContextNode() {

		return this.contextNode;
	}

	/*
	 * Static methods
	 */

	/**
	 * Checks if a context node is a valid XDI subgraph.
	 * @param contextNode The context node to check.
	 * @return True if the context node is a valid XDI subgraph.
	 */
	public static boolean isValid(ContextNode contextNode) {

		return true;
	}

	/**
	 * Factory method that creates a XDI subgraph bound to a given context node.
	 * @param contextNode The context node that is a XDI subgraph.
	 * @return The XDI subgraph.
	 */
	public static XdiSubGraph fromContextNode(ContextNode contextNode) {

		XdiSubGraph xdiSubGraph = null;

		if ((xdiSubGraph = XdiRoot.fromContextNode(contextNode)) != null) return xdiSubGraph;
		if ((xdiSubGraph = XdiAbstractSingleton.fromContextNode(contextNode)) != null) return xdiSubGraph;
		if ((xdiSubGraph = XdiAbstractClass.fromContextNode(contextNode)) != null) return xdiSubGraph;
		if ((xdiSubGraph = XdiAbstractInstance.fromContextNode(contextNode)) != null) return xdiSubGraph;
		if ((xdiSubGraph = XdiAbstractElement.fromContextNode(contextNode)) != null) return xdiSubGraph;
		if ((xdiSubGraph = XdiValue.fromContextNode(contextNode)) != null) return xdiSubGraph;

		return null;
	}

	/**
	 * @param Returns the "base" arc XRI, without context function syntax.
	 * @return The "base" arc XRI.
	 */
	public static XDI3SubSegment getBaseArcXri(XDI3SubSegment arcXri) {

		if (arcXri.hasXRef() && arcXri.getXRef().hasSegment()) {

			return arcXri.getXRef().getSegment().getFirstSubSegment();
		} else {

			return arcXri;
		}
	}

	/*
	 * Instance methods
	 */

	/**
	 * @param Returns the "base" arc XRI, without context function syntax.
	 * @return The "base" arc XRI.
	 */
	@Override
	public XDI3SubSegment getBaseArcXri() {

		return getBaseArcXri(this.getContextNode().getArcXri());
	}

	/**
	 * Creates or returns an XDI entity class under this XDI subgraph.
	 * @param arcXri The "base" arc XRI of the XDI entity class, without context function syntax.
	 * @param create Whether or not to create the context node if it doesn't exist.
	 * @return The XDI entity class.
	 */
	@Override
	public XdiEntityClass getXdiEntityClass(XDI3SubSegment arcXri, boolean create) {

		XDI3SubSegment entityClassArcXri = XdiEntityClass.createArcXri(arcXri);

		ContextNode entityClassContextNode = this.getContextNode().getContextNode(entityClassArcXri);
		if (entityClassContextNode == null && create) entityClassContextNode = this.getContextNode().createContextNode(entityClassArcXri);
		if (entityClassContextNode == null) return null;

		return new XdiEntityClass(entityClassContextNode);
	}

	/**
	 * Creates or returns an XDI attribute class under this XDI subgraph.
	 * @param arcXri The "base" arc XRI of the XDI attribute class, without context function syntax.
	 * @param create Whether or not to create the context node if it doesn't exist.
	 * @return The XDI attribute class.
	 */
	@Override
	public XdiAttributeClass getXdiAttributeClass(XDI3SubSegment arcXri, boolean create) {

		XDI3SubSegment attributeClassArcXri = XdiAttributeClass.createArcXri(arcXri);

		ContextNode attributeClassContextNode = this.getContextNode().getContextNode(attributeClassArcXri);
		if (attributeClassContextNode == null && create) attributeClassContextNode = this.getContextNode().createContextNode(attributeClassArcXri);
		if (attributeClassContextNode == null) return null;

		return new XdiAttributeClass(attributeClassContextNode);
	}

	/**
	 * Creates or returns an XDI attribute singleton under this XDI subgraph.
	 * @param arcXri The "base" arc XRI of the XDI attribute singleton, without multiplicity syntax.
	 * @param create Whether or not to create the context node if it doesn't exist.
	 * @return The XDI attribute singleton.
	 */
	@Override
	public XdiAttributeSingleton getXdiAttributeSingleton(XDI3SubSegment arcXri, boolean create) {

		XDI3SubSegment attributeSingletonArcXri = XdiAttributeSingleton.createArcXri(arcXri);

		ContextNode attributeSingletonContextNode = this.getContextNode().getContextNode(attributeSingletonArcXri);
		if (attributeSingletonContextNode == null && create) attributeSingletonContextNode = this.getContextNode().createContextNode(attributeSingletonArcXri);
		if (attributeSingletonContextNode == null) return null;

		return new XdiAttributeSingleton(attributeSingletonContextNode);
	}

	/**
	 * Creates or returns an XDI entity singleton under this XDI subgraph.
	 * @param arcXri The "base" arc XRI of the XDI entity singleton, without multiplicity syntax.
	 * @param create Whether or not to create the context node if it doesn't exist.
	 * @return The XDI entity singleton.
	 */
	@Override
	public XdiEntitySingleton getXdiEntitySingleton(XDI3SubSegment arcXri, boolean create) {

		XDI3SubSegment entitySingletonArcXri = XdiEntitySingleton.createArcXri(arcXri);

		ContextNode entitySingletonContextNode = this.getContextNode().getContextNode(entitySingletonArcXri);
		if (entitySingletonContextNode == null && create) entitySingletonContextNode = this.getContextNode().createContextNode(entitySingletonArcXri);
		if (entitySingletonContextNode == null) return null;

		return new XdiEntitySingleton(entitySingletonContextNode);
	}

	/*
	 * Object methods
	 */

	@Override
	public String toString() {

		return this.getContextNode().toString();
	}

	@Override
	public boolean equals(Object object) {

		if (object == null || ! (object instanceof XdiSubGraph)) return false;
		if (object == this) return true;

		XdiSubGraph other = (XdiSubGraph) object;

		// two subgraphs are equal if their context nodes are equal

		return this.getContextNode().equals(other.getContextNode());
	}

	@Override
	public int hashCode() {

		int hashCode = 1;

		hashCode = (hashCode * 31) + this.getContextNode().hashCode();

		return hashCode;
	}

	@Override
	public int compareTo(XdiSubGraph other) {

		if (other == null || other == this) return 0;

		return this.getContextNode().compareTo(other.getContextNode());
	}
}
