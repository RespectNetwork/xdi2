package xdi2.core.features.nodetypes;

import java.util.Iterator;

import xdi2.core.ContextNode;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.XDIArc;
import xdi2.core.util.GraphUtil;
import xdi2.core.util.iterators.MappingIterator;
import xdi2.core.util.iterators.NotNullIterator;

public abstract class XdiAbstractEntity extends XdiAbstractSubGraph<XdiEntity> implements XdiEntity {

	private static final long serialVersionUID = 7648046902369626744L;

	protected XdiAbstractEntity(ContextNode contextNode) {

		super(contextNode);
	}

	/*
	 * Static methods
	 */

	/**
	 * Checks if a context node is a valid XDI entity.
	 * @param contextNode The context node to check.
	 * @return True if the context node is a valid XDI entity.
	 */
	public static boolean isValid(ContextNode contextNode) {

		if (contextNode == null) throw new NullPointerException();

		if (XdiEntitySingleton.isValid(contextNode)) return true; 
		if (XdiEntityInstanceUnordered.isValid(contextNode)) return true;
		if (XdiEntityInstanceOrdered.isValid(contextNode)) return true;

		return false;
	}

	/**
	 * Factory method that creates an XDI entity bound to a given context node.
	 * @param contextNode The context node that is an XDI entity.
	 * @return The XDI entity.
	 */
	public static XdiEntity fromContextNode(ContextNode contextNode) {

		if (contextNode == null) throw new NullPointerException();

		XdiEntity xdiEntity = null;

		if ((xdiEntity = XdiEntitySingleton.fromContextNode(contextNode)) != null) return xdiEntity;
		if ((xdiEntity = XdiEntityInstanceUnordered.fromContextNode(contextNode)) != null) return xdiEntity;
		if ((xdiEntity = XdiEntityInstanceOrdered.fromContextNode(contextNode)) != null) return xdiEntity;

		return null;
	}

	public static XdiEntity fromXDIAddress(XDIAddress XDIaddress) {

		return fromContextNode(GraphUtil.contextNodeFromComponents(XDIaddress));
	}

	/*
	 * Methods for arcs
	 */

	public static boolean isValidXDIArc(XDIArc XDIarc) {

		if (XDIarc == null) throw new NullPointerException();

		if (XdiEntitySingleton.isValidXDIArc(XDIarc)) return true; 
		if (XdiEntityInstanceUnordered.isValidXDIArc(XDIarc)) return true;
		if (XdiEntityInstanceOrdered.isValidXDIArc(XDIarc)) return true;

		return false;
	}

	/*
	 * Helper classes
	 */

	public static class MappingContextNodeXdiEntityIterator extends NotNullIterator<XdiEntity> {

		public MappingContextNodeXdiEntityIterator(Iterator<ContextNode> contextNodes) {

			super(new MappingIterator<ContextNode, XdiEntity> (contextNodes) {

				@Override
				public XdiEntity map(ContextNode contextNode) {

					return XdiAbstractEntity.fromContextNode(contextNode);
				}
			});
		}
	}
}
