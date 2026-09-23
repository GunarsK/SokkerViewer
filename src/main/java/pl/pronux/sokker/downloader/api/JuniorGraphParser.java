package pl.pronux.sokker.downloader.api;

import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/** turns an /api/junior/{id}/graph document into the junior's level week by week */
public class JuniorGraphParser {

	/**
	 * training week to the level it left the junior at, oldest first. The graph plots a level
	 * under the week it was seen in, the one after its training.
	 */
	public static SortedMap<Integer, Integer> parse(String json) throws IOException {
		JsonArray values = Json.getArray(Json.parse(json), "values");
		if (values == null) {
			throw new IOException("/api/junior/{id}/graph without values");
		}
		SortedMap<Integer, Integer> levels = new TreeMap<Integer, Integer>();
		for (JsonElement element : values) {
			if (!element.isJsonObject()) {
				continue;
			}
			Integer week = Json.getInt(element.getAsJsonObject(), "x");
			Integer level = Json.getInt(element.getAsJsonObject(), "y");
			if (week != null && level != null) {
				levels.put(Integer.valueOf(week.intValue() - 1), level);
			}
		}
		return levels;
	}
}
