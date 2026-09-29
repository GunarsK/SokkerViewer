package pl.pronux.sokker.downloader.api;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.JsonObject;

import pl.pronux.sokker.downloader.AbstractDownloader;

/**
 * client for sokker.org's json api. Its session is separate from the xml session
 * (XMLSESSID): logging in here does not log the xml side in and the other way round.
 * Sends an honest product user agent because cloudflare challenges fake browser ones.
 */
public class ApiDownloader extends AbstractDownloader {

	private static final String BASE_URL = "https://sokker.org/api";

	private static final int CONNECT_TIMEOUT_MS = 15000;

	private static final int READ_TIMEOUT_MS = 30000;

	/** cookie name -> value, in the order sokker sent them */
	private final Map<String, String> cookies = new LinkedHashMap<String, String>();

	public void login(String login, String password) throws IOException {
		JsonObject body = new JsonObject();
		body.addProperty("login", login);
		body.addProperty("password", password);
		body.addProperty("remember", Boolean.FALSE);
		request("/auth/login", body.toString());
	}

	/** the training reports of one week, or of the latest completed training when week is null */
	public String getTraining(Integer week) throws IOException {
		return request(week == null ? "/training" : "/training?filter%5Bweek%5D=" + week, null);
	}

	/** one junior's level week by week, since he joined the academy */
	public String getJuniorGraph(int juniorId) throws IOException {
		return request("/junior/" + juniorId + "/graph", null);
	}

	private String request(String path, String jsonBody) throws IOException {
		HttpURLConnection connection = getDefaultConnection(BASE_URL + path);
		connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
		connection.setReadTimeout(READ_TIMEOUT_MS);
		connection.setInstanceFollowRedirects(false);
		connection.setRequestProperty("User-Agent", USER_AGENT);
		connection.setRequestProperty("Accept", "application/json");
		if (!cookies.isEmpty()) {
			connection.setRequestProperty("Cookie", cookieHeader());
		}
		if (jsonBody != null) {
			byte[] bytes = jsonBody.getBytes("UTF-8");
			connection.setRequestMethod(POST);
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
			connection.setFixedLengthStreamingMode(bytes.length);
			OutputStream out = connection.getOutputStream();
			try {
				out.write(bytes);
			} finally {
				out.close();
			}
		}
		int status = connection.getResponseCode();
		for (String cookie : readSetCookies(connection)) {
			int equals = cookie.indexOf('=');
			if (equals > 0) {
				cookies.put(cookie.substring(0, equals).trim(), cookie.substring(equals + 1).trim());
			}
		}
		// reading to the end and closing lets the jdk keep the tls connection for the next call;
		// disconnect() would throw it away
		String body = read(status >= 400 ? connection.getErrorStream() : connection.getInputStream());
		if (status < 200 || status >= 300) {
			throw new ApiException(status, connection.getContentType(), body);
		}
		return body;
	}

	private String cookieHeader() {
		StringBuilder header = new StringBuilder();
		for (Map.Entry<String, String> cookie : cookies.entrySet()) {
			if (header.length() > 0) {
				header.append("; ");
			}
			header.append(cookie.getKey()).append('=').append(cookie.getValue());
		}
		return header.toString();
	}
}
