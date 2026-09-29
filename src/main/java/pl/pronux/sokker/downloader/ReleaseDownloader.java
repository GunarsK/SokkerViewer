package pl.pronux.sokker.downloader;

import java.io.IOException;
import java.net.HttpURLConnection;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import pl.pronux.sokker.downloader.api.Json;
import pl.pronux.sokker.model.Release;

/** client for SokkerViewer's releases on github */
public class ReleaseDownloader extends AbstractDownloader {

	private static final String LATEST_URL = "https://api.github.com/repos/GunarsK/SokkerViewer/releases/latest";

	private static final int CONNECT_TIMEOUT_MS = 15000;

	private static final int READ_TIMEOUT_MS = 30000;

	/** the newest published release; drafts and pre-releases never count */
	public Release getLatestRelease() throws IOException {
		HttpURLConnection connection = getDefaultConnection(LATEST_URL);
		connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
		connection.setReadTimeout(READ_TIMEOUT_MS);
		connection.setRequestProperty("User-Agent", USER_AGENT);
		connection.setRequestProperty("Accept", "application/vnd.github+json");
		int status = connection.getResponseCode();
		if (status != HttpURLConnection.HTTP_OK) {
			throw new IOException("github answered HTTP " + status);
		}
		JsonObject release;
		try {
			release = Json.parse(read(connection.getInputStream()));
		} catch (JsonParseException e) {
			throw new IOException("github answered no json", e);
		}
		String tag = Json.getString(release, "tag_name");
		String url = Json.getString(release, "html_url");
		if (tag == null || url == null) {
			throw new IOException("github release without tag_name or html_url");
		}
		return new Release(tag, url);
	}
}
