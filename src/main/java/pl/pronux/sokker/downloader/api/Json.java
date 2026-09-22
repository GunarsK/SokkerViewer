package pl.pronux.sokker.downloader.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * read-only access to a gson tree by dotted path ("report.type.code"); every reader answers
 * null for a missing or json-null node, so callers decide what a missing field means
 */
public final class Json {

	private Json() {
	}

	/** the document as an object, null when it is not a json object */
	public static JsonObject parse(String json) {
		JsonElement element = JsonParser.parseString(json);
		return element.isJsonObject() ? element.getAsJsonObject() : null;
	}

	private static JsonElement get(JsonObject object, String path) {
		JsonElement current = object;
		for (String part : path.split("\\.")) {
			if (current == null || !current.isJsonObject()) {
				return null;
			}
			current = current.getAsJsonObject().get(part);
		}
		return current == null || current.isJsonNull() ? null : current;
	}

	public static Integer getInt(JsonObject object, String path) {
		JsonElement element = get(object, path);
		return element != null && element.isJsonPrimitive() ? Integer.valueOf(element.getAsInt()) : null;
	}

	public static Long getLong(JsonObject object, String path) {
		JsonElement element = get(object, path);
		return element != null && element.isJsonPrimitive() ? Long.valueOf(element.getAsLong()) : null;
	}

	public static JsonObject getObject(JsonObject object, String path) {
		JsonElement element = get(object, path);
		return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
	}

	public static JsonArray getArray(JsonObject object, String path) {
		JsonElement element = get(object, path);
		return element != null && element.isJsonArray() ? element.getAsJsonArray() : null;
	}
}
