package xdi2.core.impl.json;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import xdi2.core.ContextNode;
import xdi2.core.Graph;
import xdi2.core.GraphFactory;
import xdi2.core.constants.XDIConstants;
import xdi2.core.exceptions.Xdi2RuntimeException;
import xdi2.core.impl.AbstractGraph;
import xdi2.core.util.iterators.IteratorContains;
import xdi2.core.util.iterators.IteratorRemover;

public class JSONGraph extends AbstractGraph implements Graph {

	private static final long serialVersionUID = -7459785412219244590L;

	private static final Logger log = LoggerFactory.getLogger(JSONContextNode.class);

	private final JSONStore jsonStore;

	private final JSONContextNode jsonRootContextNode;
	private final Map<String, JsonObject> jsonObjectsCached;
	private final Set<String> jsonObjectsCachedWithPrefix;

	private boolean useCache;

	private StringBuffer logBuffer;
	private boolean logEnabled;

	JSONGraph(GraphFactory graphFactory, String identifier, JSONStore jsonStore) {

		super(graphFactory, identifier);

		this.jsonStore = jsonStore;

		this.jsonRootContextNode = new JSONContextNode(this, null, null, XDIConstants.XDI_ADD_ROOT);

		this.jsonObjectsCached = new HashMap<String, JsonObject> ();
		this.jsonObjectsCachedWithPrefix = new HashSet<String> ();

		this.useCache = false;

		this.logBuffer = new StringBuffer();
		this.logEnabled = false;
	}

	@Override
	public ContextNode getRootContextNode(boolean subgraph) {

		// retrieve subgraph?

		if (subgraph) {

			this.jsonLoadWithPrefix("");
		}

		// done

		return this.jsonRootContextNode;
	}

	@Override
	public void close() {

		this.jsonStore.close();
	}

	/*
	 * Methods related to transactions
	 */

	@Override
	public boolean supportsTransactions() {

		return this.jsonStore.supportsTransactions();
	}

	@Override
	public void beginTransaction() {

		this.jsonObjectsCached.clear();
		this.jsonObjectsCachedWithPrefix.clear();

		this.useCache = true;

		this.jsonStore.beginTransaction();
	}

	@Override
	public void commitTransaction() {

		this.jsonObjectsCached.clear();
		this.jsonObjectsCachedWithPrefix.clear();

		this.useCache = false;

		this.jsonStore.commitTransaction();
	}

	@Override
	public void rollbackTransaction() {

		this.jsonObjectsCached.clear();
		this.jsonObjectsCachedWithPrefix.clear();

		this.useCache = false;

		this.jsonStore.rollbackTransaction();
	}

	/*
	 * Getters and setters
	 */

	public JSONStore getJsonStore() {

		return this.jsonStore;
	}

	public StringBuffer getLogBuffer() {

		return this.logBuffer;
	}

	public boolean getLogEnabled() {

		return this.logEnabled;
	}

	public void setLogEnabled(boolean logEnabled) {

		this.logEnabled = logEnabled;
	}

	public void resetLogBuffer() {

		this.logBuffer = new StringBuffer();
	}

	/*
	 * Helper methods
	 */

	JsonObject jsonLoad(String id) {

		JsonObject jsonObjectCached = null;
		JsonObject jsonObject = null;

		try {

			if (this.useCache) {

				jsonObjectCached = this.jsonObjectsCached.get(id);

				if (jsonObjectCached != null) {

					jsonObject = jsonObjectCached;
					return jsonObject;
				}
			}

			try {

				jsonObject = this.jsonStore.load(id);
				if (jsonObject == null) jsonObject = new JsonObject();

				if (this.useCache) {

					this.jsonObjectsCached.put(id, jsonObject);
				}

				return jsonObject;
			} catch (IOException ex) {

				throw new Xdi2RuntimeException("Cannot load JSON at " + id + ": " + ex.getMessage(), ex);
			}
		} finally {
		// Statement below causing ConcurrentModificationException. Commenting till the time we solve this issue.    
		//	if (log.isTraceEnabled()) log.trace("load( " + id + " , " + jsonObject + " , cache " + (jsonObjectCached != null ? "HIT" : "MISS") + " )");

		//	if (this.getLogEnabled()) this.logBuffer.append("load( " + id + " , " + jsonObject + " , cache " + (jsonObjectCached != null ? "HIT" : "MISS") + " )\n");
		}
	}

	Map<String, JsonObject> jsonLoadWithPrefix(String id) {

		JsonObject jsonObjectCached = null;
		Map<String, JsonObject> jsonObjects = null;

		try {

			if (this.useCache) {

				boolean jsonObjectCachedWithPrefix = this.jsonObjectsCachedWithPrefix.contains(id);

				if (jsonObjectCachedWithPrefix) {

					jsonObjectCached = this.jsonObjectsCached.get(id);

					jsonObjects = Collections.singletonMap(id, jsonObjectCached);
					return jsonObjects;
				}
			}

			try {

				jsonObjects = this.jsonStore.loadWithPrefix(id);

				if (this.useCache) {

					this.jsonObjectsCached.putAll(jsonObjects);
					this.jsonObjectsCachedWithPrefix.addAll(jsonObjects.keySet());
				}

				return jsonObjects;
			} catch (IOException ex) {

				throw new Xdi2RuntimeException("Cannot loadWithPrefix JSON at " + id + ": " + ex.getMessage(), ex);
			}
		} finally {
		 // Statement below may cause ConcurrentModificationException. Commenting till the time we solve this issue.
		//	if (log.isTraceEnabled()) log.trace("loadWithPrefix( " + id + " , " + jsonObjects + " , cache " + (jsonObjectCached != null ? "HIT" : "MISS") + " )");

		//	if (this.getLogEnabled()) this.logBuffer.append("loadWithPrefix( " + id + " , " + jsonObjects + " , cache " + (jsonObjectCached != null ? "HIT" : "MISS") + " )\n");
		}
	}

	void jsonSave(String id, JsonObject jsonObject) {

		if (log.isTraceEnabled()) log.trace("save( " + id + " , " + jsonObject + " )");

		if (this.getLogEnabled()) this.logBuffer.append("save( " + id + " , " + jsonObject + " )\n");

		try {

			this.jsonStore.save(id, jsonObject);

			if (this.useCache) {

				this.jsonObjectsCached.put(id, jsonObject);
			}
		} catch (IOException ex) {

			throw new Xdi2RuntimeException("Cannot save JSON at " + id + ": " + ex.getMessage(), ex);
		}
	}

	void jsonSaveToArray(String id, String key, JsonPrimitive jsonPrimitive) {

		if (log.isTraceEnabled()) log.trace("saveToArray( " + id + " , " + key + " , " + jsonPrimitive + " )");

		if (this.getLogEnabled()) this.logBuffer.append("saveToArray( " + id + " , " + key + " , " + jsonPrimitive + " )\n");

		try {

			this.jsonStore.saveToArray(id, key, jsonPrimitive);

			if (this.useCache) {

				JsonObject jsonObject = this.jsonObjectsCached.get(id);

				if (jsonObject == null) {

					jsonObject = new JsonObject();
					JsonArray jsonArray = new JsonArray();
					jsonArray.add(jsonPrimitive);
					jsonObject.add(key, jsonArray);
				} else {

					JsonArray jsonArray = jsonObject.getAsJsonArray(key);

					if (jsonArray == null) { 

						jsonArray = new JsonArray();
						jsonArray.add(jsonPrimitive);
						jsonObject.add(key, jsonArray);
					} else {

						if (! new IteratorContains<JsonElement> (jsonArray.iterator(), jsonPrimitive).contains()) jsonArray.add(jsonPrimitive);
					}
				}

				this.jsonObjectsCached.put(id, jsonObject);
			}
		} catch (IOException ex) {

			throw new Xdi2RuntimeException("Cannot save JSON to array " + id + ": " + ex.getMessage(), ex);
		}
	}

	void jsonSaveToObject(String id, String key, JsonElement jsonElement) {

		if (log.isTraceEnabled()) log.trace("saveToObject( " + id + " , " + key + " , " + jsonElement + " )");

		if (this.getLogEnabled()) this.logBuffer.append("saveToObject( " + id + " , " + key + " , " + jsonElement + " )\n");

		try {

			this.jsonStore.saveToObject(id, key, jsonElement);

			if (this.useCache) {

				JsonObject jsonObject = this.jsonObjectsCached.get(id);

				if (jsonObject == null) {

					jsonObject = new JsonObject();
					jsonObject.add(key, jsonElement);
				} else {

					jsonObject.add(key, jsonElement);
				}

				this.jsonObjectsCached.put(id, jsonObject);
			}
		} catch (IOException ex) {

			throw new Xdi2RuntimeException("Cannot save JSON to object " + id + ": " + ex.getMessage(), ex);
		}
	}

	void jsonDelete(String id) {

		if (log.isTraceEnabled()) log.trace("delete( " + id + " )");

		if (this.getLogEnabled()) this.logBuffer.append("delete( " + id + " )\n");

		try {

			this.jsonStore.delete(id);

			if (this.useCache) {
				for (Iterator<Entry<String, JsonObject>> iterator = this.jsonObjectsCached.entrySet().iterator(); iterator.hasNext(); ) {

					if (iterator.next().getKey().startsWith(id)) iterator.remove();
				}
			}
		} catch (IOException ex) {

			throw new Xdi2RuntimeException("Cannot delete JSON " + id + ": " + ex.getMessage(), ex);
		}
	}

	void jsonDeleteFromArray(String id, String key, JsonPrimitive jsonPrimitive) {

		if (log.isTraceEnabled()) log.trace("deleteFromArray( " + id + " , " + key + " , " + jsonPrimitive + " )");

		if (this.getLogEnabled()) this.logBuffer.append("deleteFromArray( " + id + " , " + key + " , " + jsonPrimitive + " )\n");

		try {

			this.jsonStore.deleteFromArray(id, key, jsonPrimitive);

			if (this.useCache) {

				JsonObject jsonObject = this.jsonObjectsCached.get(id);
				if (jsonObject == null) return;

				JsonArray jsonArray = jsonObject.getAsJsonArray(key);
				if (jsonArray == null) return;

				new IteratorRemover<JsonElement> (jsonArray.iterator(), jsonPrimitive).remove();

				this.jsonObjectsCached.put(id, jsonObject);
			}
		} catch (IOException ex) {

			throw new Xdi2RuntimeException("Cannot remove JSON from array " + id + ": " + ex.getMessage(), ex);
		}
	}

	void jsonDeleteFromObject(String id, String key) {

		if (log.isTraceEnabled()) log.trace("deleteFromObject( " + id + " , " + key + " )");

		if (this.getLogEnabled()) this.logBuffer.append("deleteFromObject( " + id + " , " + key + " )\n");

		try {

			this.jsonStore.deleteFromObject(id, key);

			if (this.useCache) {

				JsonObject jsonObject = this.jsonObjectsCached.get(id);
				if (jsonObject == null) return;

				jsonObject.remove(key);

				this.jsonObjectsCached.put(id, jsonObject);
			}
		} catch (IOException ex) {

			throw new Xdi2RuntimeException("Cannot remove JSON from object " + id + ": " + ex.getMessage(), ex);
		}
	}
}
