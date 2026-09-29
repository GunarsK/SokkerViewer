package pl.pronux.sokker.model;

/** a published SokkerViewer release */
public class Release {

	private final String version;

	private final String url;

	/** the windows zip, null when the release has none */
	private final String downloadUrl;

	/** the zip's sha-256 in hex, null when github gave none */
	private final String sha256;

	public Release(String tag, String url) {
		this(tag, url, null, null);
	}

	/** tag like "v0.15.3"; the leading v is dropped */
	public Release(String tag, String url, String downloadUrl, String sha256) {
		this.version = tag.startsWith("v") ? tag.substring(1) : tag;
		this.url = url;
		this.downloadUrl = downloadUrl;
		this.sha256 = sha256;
	}

	public String getVersion() {
		return version;
	}

	public String getUrl() {
		return url;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public String getSha256() {
		return sha256;
	}

	/** numbers compared part by part, a missing part counts as 0 */
	public boolean isNewerThan(String current) {
		String[] mine = version.split("\\.");
		String[] theirs = current.split("\\.");
		try {
			for (int i = 0; i < Math.max(mine.length, theirs.length); i++) {
				int part = i < mine.length ? Integer.parseInt(mine[i]) : 0;
				int currentPart = i < theirs.length ? Integer.parseInt(theirs[i]) : 0;
				if (part != currentPart) {
					return part > currentPart;
				}
			}
		} catch (NumberFormatException e) {
			return false;
		}
		return false;
	}
}
