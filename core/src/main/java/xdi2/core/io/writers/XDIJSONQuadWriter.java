package xdi2.core.io.writers;

import java.io.IOException;
import java.io.Writer;
import java.util.Properties;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonWriter;

import xdi2.core.ContextNode;
import xdi2.core.Graph;
import xdi2.core.LiteralNode;
import xdi2.core.Relation;
import xdi2.core.exceptions.Xdi2RuntimeException;
import xdi2.core.features.nodetypes.XdiAbstractAttribute;
import xdi2.core.features.nodetypes.XdiAbstractEntity;
import xdi2.core.features.nodetypes.XdiAbstractRoot;
import xdi2.core.features.nodetypes.XdiAttributeCollection;
import xdi2.core.features.nodetypes.XdiCommonDefinition;
import xdi2.core.features.nodetypes.XdiCommonRoot;
import xdi2.core.features.nodetypes.XdiCommonVariable;
import xdi2.core.features.nodetypes.XdiEntityCollection;
import xdi2.core.impl.AbstractLiteralNode;
import xdi2.core.impl.memory.MemoryGraphFactory;
import xdi2.core.io.AbstractXDIWriter;
import xdi2.core.io.MimeType;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.util.CopyUtil;
import xdi2.core.util.XDIAddressUtil;

public class XDIJSONQuadWriter extends AbstractXDIWriter {

	private static final long serialVersionUID = 1077789049204778292L;

	public static final String FORMAT_NAME = "XDI/JSON/QUAD";
	public static final String FILE_EXTENSION = "json";
	public static final MimeType MIME_TYPE = new MimeType("application/xdi+json");

	private static final Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();

	public XDIJSONQuadWriter(Properties parameters) {

		super(parameters);
	}

	private void writeInternal(Graph graph, JsonObject jsonObject) throws IOException {

		// start with the common root node

		this.putRootIntoJsonObject(graph.getRootContextNode(), jsonObject);
	}

	@SuppressWarnings("resource")
	@Override
	public Writer write(Graph graph, Writer writer) throws IOException {

		// write ordered?

		Graph orderedGraph = null;

		if (this.isWriteOrdered()) {

			MemoryGraphFactory memoryGraphFactory = new MemoryGraphFactory();
			memoryGraphFactory.setSortmode(MemoryGraphFactory.SORTMODE_ALPHA);
			orderedGraph = memoryGraphFactory.openGraph();
			CopyUtil.copyGraph(graph, orderedGraph, null);

			graph = orderedGraph;
		}

		// write

		JsonObject jsonObject = new JsonObject();

		this.writeInternal(graph, jsonObject);

		JsonWriter jsonWriter = new JsonWriter(writer);
		if (this.isWritePretty()) jsonWriter.setIndent("  ");
		gson.toJson(jsonObject, jsonWriter);
		jsonWriter.flush();
		writer.flush();

		return writer;
	}

	private void putRootIntoJsonObject(ContextNode rootContextNode, JsonObject jsonObject) throws IOException {

		JsonObject rootJsonObject;

		// set up root

		if (XdiCommonRoot.isValid(rootContextNode)) {

			rootJsonObject = jsonObject;
		} else {

			rootJsonObject = new JsonObject();
			jsonObject.add(rootContextNode.getXDIAddress().toString(), rootJsonObject);
		}

		// context nodes

		for (ContextNode contextNode : rootContextNode.getContextNodes()) {

			if (this.isWriteImplied()) this.putContextNodeIntoJsonObject(contextNode, rootJsonObject);

			if (XdiCommonVariable.isValid(contextNode) || XdiCommonDefinition.isValid(contextNode)) {

				this.putRootIntoJsonObject(contextNode, rootJsonObject);
				continue;
			}

			if (XdiAbstractRoot.isValid(contextNode)) {

				this.putRootIntoJsonObject(contextNode, jsonObject);
				continue;
			}

			if (XdiAbstractEntity.isValid(contextNode) || XdiEntityCollection.isValid(contextNode)) {

				this.putEntityIntoJsonObject(contextNode, rootJsonObject, rootContextNode.getXDIAddress());
				continue;
			}

			if (XdiAbstractAttribute.isValid(contextNode) || XdiAttributeCollection.isValid(contextNode)) {

				this.putAttributeIntoJsonObject(contextNode, rootJsonObject, rootContextNode.getXDIAddress());
				continue;
			}

			throw new Xdi2RuntimeException("Unexpected context node: " + contextNode + " on root context node: " + rootContextNode);
		}

		// relations

		for (Relation relation : rootContextNode.getRelations()) {

			this.putRelationIntoJsonObject(relation, rootJsonObject);
		}

		// literal node

		if (rootContextNode.containsLiteralNode()) {

			throw new Xdi2RuntimeException("Unexpected literal node on root context node: " + rootContextNode);
		}

		// finish root

		if (rootJsonObject != jsonObject) {

			if (! this.isWriteImplied() && rootContextNode.getStatement().isImplied() && rootJsonObject.entrySet().isEmpty()) {
				//			if (rootJsonObject.entrySet().isEmpty() && ! rootContextNode.isEmpty()) {

				jsonObject.remove(rootContextNode.getXDIAddress().toString());
			}
		}
	}

	private void putEntityIntoJsonObject(ContextNode entityContextNode, JsonObject jsonObject, XDIAddress parentXDIAddress) throws IOException {

		XDIAddress XDIaddress = entityContextNode.getXDIAddress();
		XDIAddress localXDIAddress = XDIAddressUtil.localXDIAddress(XDIaddress, - parentXDIAddress.getNumXDIArcs());

		// set up entity

		JsonObject entityJsonObject = new JsonObject();
		jsonObject.add(localXDIAddress.toString(), entityJsonObject);

		// context nodes

		for (ContextNode contextNode : entityContextNode.getContextNodes()) {

			if (this.isWriteImplied()) this.putContextNodeIntoJsonObject(contextNode, entityJsonObject);

			if (XdiCommonVariable.isValid(contextNode) || XdiCommonDefinition.isValid(contextNode)) {

				this.putEntityIntoJsonObject(contextNode, jsonObject, parentXDIAddress);
				continue;
			}

			if (XdiAbstractEntity.isValid(contextNode) || XdiEntityCollection.isValid(contextNode)) {

				this.putEntityIntoJsonObject(contextNode, jsonObject, parentXDIAddress);
				continue;
			}

			if (XdiAbstractAttribute.isValid(contextNode) || XdiAttributeCollection.isValid(contextNode)) {

				this.putAttributeIntoJsonObject(contextNode, entityJsonObject, XDIaddress);
				continue;
			}

			throw new Xdi2RuntimeException("Unexpected context node: " + contextNode + " on entity context node: " + entityContextNode);
		}

		// relations

		for (Relation relation : entityContextNode.getRelations()) {

			this.putRelationIntoJsonObject(relation, entityJsonObject);
		}

		// literal node

		if (entityContextNode.containsLiteralNode()) {

			throw new Xdi2RuntimeException("Unexpected literal node on entity context node: " + entityContextNode);
		}

		// finish entity

		if (! this.isWriteImplied() && entityContextNode.getStatement().isImplied() && entityJsonObject.entrySet().isEmpty()) {
			//		TODO if (entityJsonObject.entrySet().isEmpty() && ! entityContextNode.isEmpty()) {

			jsonObject.remove(localXDIAddress.toString());
		}
	}

	private void putAttributeIntoJsonObject(ContextNode attributeContextNode, JsonObject jsonObject, XDIAddress parentXDIAddress) throws IOException {

		XDIAddress XDIaddress = attributeContextNode.getXDIAddress();
		XDIAddress localXDIAddress = XDIAddressUtil.localXDIAddress(XDIaddress, - parentXDIAddress.getNumXDIArcs());

		// set up attribute

		JsonObject attributeJsonObject = new JsonObject();
		jsonObject.add(localXDIAddress.toString(), attributeJsonObject);

		// context nodes

		for (ContextNode contextNode : attributeContextNode.getContextNodes()) {

			if (this.isWriteImplied()) this.putContextNodeIntoJsonObject(contextNode, attributeJsonObject);

			if (XdiCommonVariable.isValid(contextNode) || XdiCommonDefinition.isValid(contextNode)) {

				this.putAttributeIntoJsonObject(contextNode, jsonObject, parentXDIAddress);
				continue;
			}

			if (XdiAbstractAttribute.isValid(contextNode) || XdiAttributeCollection.isValid(contextNode)) {

				this.putAttributeIntoJsonObject(contextNode, jsonObject, parentXDIAddress);
				continue;
			}

			throw new Xdi2RuntimeException("Unexpected context node: " + contextNode + " on attribute context node: " + attributeContextNode);
		}

		// relations

		for (Relation relation : attributeContextNode.getRelations()) {

			this.putRelationIntoJsonObject(relation, attributeJsonObject);
		}

		// literal node

		if (attributeContextNode.containsLiteralNode()) {

			LiteralNode literalNode = attributeContextNode.getLiteralNode();

			this.putLiteralNodeIntoAttributeJsonObject(literalNode, attributeJsonObject, XDIaddress);
		}

		// finish attribute

		if (! this.isWriteImplied() && attributeContextNode.getStatement().isImplied() && attributeJsonObject.entrySet().isEmpty()) {
			//		if (attributeJsonObject.entrySet().isEmpty() && ! attributeContextNode.isEmpty()) {

			jsonObject.remove(localXDIAddress.toString());
		}
	}

	private void putLiteralNodeIntoAttributeJsonObject(LiteralNode literalNode, JsonObject attributeJsonObject, XDIAddress parentXDIAddress) {

		XDIAddress XDIaddress = literalNode.getXDIAddress();
		XDIAddress localXDIAddress = XDIAddressUtil.localXDIAddress(XDIaddress, - parentXDIAddress.getNumXDIArcs());

		JsonElement literalJsonElement = AbstractLiteralNode.literalDataToJsonElement(literalNode.getLiteralData());
		attributeJsonObject.add(localXDIAddress.toString(), literalJsonElement);
	}

	private void putContextNodeIntoJsonObject(ContextNode contextNode, JsonObject jsonObject) {

		JsonArray contextNodeJsonArray = jsonObject.getAsJsonArray("//");

		if (contextNodeJsonArray == null) {

			contextNodeJsonArray = new JsonArray();
			jsonObject.add("//", contextNodeJsonArray);
		}

		contextNodeJsonArray.add(new JsonPrimitive(contextNode.getXDIArc().toString()));
	}

	private void putRelationIntoJsonObject(Relation relation, JsonObject jsonObject) {

		if (! this.isWriteImplied() && relation.getStatement().isImplied()) return;

		JsonArray relationJsonArray = jsonObject.getAsJsonArray("/" + relation.getXDIAddress().toString());

		if (relationJsonArray == null) {

			relationJsonArray = new JsonArray();
			jsonObject.add("/" + relation.getXDIAddress().toString(), relationJsonArray);
		}

		relationJsonArray.add(new JsonPrimitive(relation.getTargetXDIAddress().toString()));
	}
}
