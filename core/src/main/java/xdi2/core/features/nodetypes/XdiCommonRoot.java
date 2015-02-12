package xdi2.core.features.nodetypes;

import xdi2.core.ContextNode;
import xdi2.core.Graph;
import xdi2.core.Relation;
import xdi2.core.constants.XDIDictionaryConstants;
import xdi2.core.syntax.XDIAddress;

/**
 * An XDI common root, represented as a context node.
 * 
 * @author markus
 */
public class XdiCommonRoot extends XdiAbstractRoot {

	private static final long serialVersionUID = 2956364705721958108L;

	protected XdiCommonRoot(ContextNode contextNode) {

		super(contextNode);
	}

	/*
	 * Static methods
	 */

	/**
	 * Given a graph, finds and returns the XDI common root.
	 * @param graph The graph.
	 * @return The XDI common root.
	 */
	public static XdiCommonRoot findCommonRoot(Graph graph) {

		ContextNode commonRootContextNode = graph.getRootContextNode(false);

		return new XdiCommonRoot(commonRootContextNode);
	}

	/**
	 * Checks if a context node is a valid XDI common root.
	 * @param contextNode The context node to check.
	 * @return True if the context node is a valid XDI common root.
	 */
	public static boolean isValid(ContextNode contextNode) {

		if (contextNode == null) throw new NullPointerException();

		if (contextNode.getXDIArc() == null) return true;
		if (! contextNode.getXDIArc().hasCs() && ! contextNode.getXDIArc().hasLiteral() && ! contextNode.getXDIArc().hasXRef()) return true;

		return false;
	}

	/**
	 * Factory method that creates an XDI common root bound to a given context node.
	 * @param contextNode The context node that is an XDI common root.
	 * @return The XDI common root.
	 */
	public static XdiCommonRoot fromContextNode(ContextNode contextNode) {

		if (contextNode == null) throw new NullPointerException();

		if (! isValid(contextNode)) return null;

		if (contextNode.getXDIArc() != null && contextNode.getXDIArc().isDefinition() && contextNode.getXDIArc().isVariable()) return new Definition.Variable(contextNode);
		if (contextNode.getXDIArc() != null && contextNode.getXDIArc().isDefinition() && ! contextNode.getXDIArc().isVariable()) return new Definition(contextNode);
		if (contextNode.getXDIArc() != null && ! contextNode.getXDIArc().isDefinition() && contextNode.getXDIArc().isVariable()) return new Variable(contextNode);
		return new XdiCommonRoot(contextNode);
	}

	/*
	 * Instance methods
	 */

	public XdiPeerRoot setSelfPeerRoot(XDIAddress XDIaddress) {

		XdiPeerRoot selfPeerRoot = this.getSelfPeerRoot();
		if (selfPeerRoot != null) selfPeerRoot.getContextNode().delete();

		if (XDIaddress == null) return null;

		selfPeerRoot = this.getPeerRoot(XDIaddress, true);

		ContextNode commonRootContextNode = this.getContextNode();
		ContextNode selfPeerRootContextNode = selfPeerRoot.getContextNode();

		commonRootContextNode.delRelations(XDIDictionaryConstants.XDI_ADD_IS_REF);
		commonRootContextNode.setRelation(XDIDictionaryConstants.XDI_ADD_IS_REF, selfPeerRootContextNode);

		selfPeerRootContextNode.delRelations(XDIDictionaryConstants.XDI_ADD_REF);
		selfPeerRootContextNode.setRelation(XDIDictionaryConstants.XDI_ADD_REF, commonRootContextNode);

		return selfPeerRoot;
	}

	public XdiPeerRoot getSelfPeerRoot() {

		Relation relation = this.getContextNode().getRelation(XDIDictionaryConstants.XDI_ADD_IS_REF);
		if (relation == null) return null;

		return XdiPeerRoot.fromContextNode(relation.follow());
	}

	/*
	 * Definition and Variable classes
	 */

	public static class Definition extends XdiCommonRoot implements XdiDefinition<XdiRoot> {

		private static final long serialVersionUID = -1203948822448500542L;

		private Definition(ContextNode contextNode) {

			super(contextNode);
		}

		public static boolean isValid(ContextNode contextNode) {

			return XdiCommonRoot.isValid(contextNode) &&
					contextNode.getXDIArc().isDefinition() &&
					! contextNode.getXDIArc().isVariable();
		}

		public static Definition fromContextNode(ContextNode contextNode) {

			if (contextNode == null) throw new NullPointerException();

			if (! isValid(contextNode)) return null;

			return new Definition(contextNode);
		}

		public static class Variable extends Definition implements XdiVariable<XdiRoot> {

			private static final long serialVersionUID = 52623107598209206L;

			private Variable(ContextNode contextNode) {

				super(contextNode);
			}

			public static boolean isValid(ContextNode contextNode) {

				return XdiCommonRoot.isValid(contextNode) &&
						contextNode.getXDIArc().isDefinition() &&
						contextNode.getXDIArc().isVariable();
			}

			public static Definition fromContextNode(ContextNode contextNode) {

				if (contextNode == null) throw new NullPointerException();

				if (! isValid(contextNode)) return null;

				return new Definition(contextNode);
			}
		}
	}

	public static class Variable extends XdiCommonRoot implements XdiVariable<XdiRoot> {

		private static final long serialVersionUID = 6816533299541990392L;

		private Variable(ContextNode contextNode) {

			super(contextNode);
		}

		public static boolean isValid(ContextNode contextNode) {

			return XdiCommonRoot.isValid(contextNode) &&
					! contextNode.getXDIArc().isDefinition() &&
					contextNode.getXDIArc().isVariable();
		}

		public static Variable fromContextNode(ContextNode contextNode) {

			if (contextNode == null) throw new NullPointerException();

			if (! isValid(contextNode)) return null;

			return new Variable(contextNode);
		}
	}
}
