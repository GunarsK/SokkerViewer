package pl.pronux.sokker.downloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import pl.pronux.sokker.downloader.api.Json;
import pl.pronux.sokker.interfaces.ProgressMonitor;
import pl.pronux.sokker.model.Release;

/** client for SokkerViewer's releases on github */
public class ReleaseDownloader extends AbstractDownloader {

	private static final String LATEST_URL = "https://api.github.com/repos/GunarsK/SokkerViewer/releases/latest";

	/** the end of the windows bundle's file name */
	private static final String ZIP_SUFFIX = "-win64-with-java.zip";

	private static final String SHA256_PREFIX = "sha256:";

	private static final int CONNECT_TIMEOUT_MS = 15000;

	private static final int READ_TIMEOUT_MS = 30000;

	/** the newest published release; drafts and pre-releases never count */
	public Release getLatestRelease() throws IOException {
		HttpURLConnection connection = open(LATEST_URL);
		connection.setRequestProperty("Accept", "application/vnd.github+json");
		checkStatus(connection);
		return parse(read(connection.getInputStream()));
	}

	/** streams url into target; the sha-256 of what came, null when cancelled */
	public String download(String url, File target, ProgressMonitor monitor) throws IOException {
		HttpURLConnection connection = open(url);
		checkStatus(connection);
		long length = connection.getContentLengthLong();
		if (length > 0) {
			monitor.setTotalTime((int) (length / 1024));
		}
		MessageDigest sha256 = sha256();
		InputStream in = connection.getInputStream();
		OutputStream out = new FileOutputStream(target);
		try {
			byte[] buffer = new byte[65536];
			long total = 0;
			int read;
			while ((read = in.read(buffer)) != -1) {
				if (monitor.isCanceled()) {
					return null;
				}
				out.write(buffer, 0, read);
				sha256.update(buffer, 0, read);
				int before = (int) (total / 1024);
				total += read;
				monitor.worked((int) (total / 1024) - before);
			}
		} finally {
			out.close();
			in.close();
		}
		return hex(sha256.digest());
	}

	/** the release in github's json, with its windows zip when it has one */
	static Release parse(String json) throws IOException {
		JsonObject release;
		try {
			release = Json.parse(json);
		} catch (JsonParseException e) {
			throw new IOException("github answered no json", e);
		}
		String tag = Json.getString(release, "tag_name");
		String url = Json.getString(release, "html_url");
		if (tag == null || url == null) {
			throw new IOException("github release without tag_name or html_url");
		}
		JsonArray assets = Json.getArray(release, "assets");
		if (assets != null) {
			for (JsonElement element : assets) {
				JsonObject asset = element.isJsonObject() ? element.getAsJsonObject() : null;
				String name = Json.getString(asset, "name");
				if (name != null && name.endsWith(ZIP_SUFFIX)) {
					String digest = Json.getString(asset, "digest");
					String sha256 = digest != null && digest.startsWith(SHA256_PREFIX) ? digest.substring(SHA256_PREFIX.length()) : null;
					return new Release(tag, url, Json.getString(asset, "browser_download_url"), sha256);
				}
			}
		}
		return new Release(tag, url);
	}

	private HttpURLConnection open(String url) throws IOException {
		HttpURLConnection connection = getDefaultConnection(url);
		connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
		connection.setReadTimeout(READ_TIMEOUT_MS);
		connection.setRequestProperty("User-Agent", USER_AGENT);
		return connection;
	}

	private static void checkStatus(HttpURLConnection connection) throws IOException {
		int status = connection.getResponseCode();
		if (status != HttpURLConnection.HTTP_OK) {
			throw new IOException("github answered HTTP " + status);
		}
	}

	private static MessageDigest sha256() throws IOException {
		try {
			return MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new IOException(e);
		}
	}

	private static String hex(byte[] bytes) {
		StringBuilder hex = new StringBuilder();
		for (byte b : bytes) {
			hex.append(String.format("%02x", Integer.valueOf(b & 0xff)));
		}
		return hex.toString();
	}
}
