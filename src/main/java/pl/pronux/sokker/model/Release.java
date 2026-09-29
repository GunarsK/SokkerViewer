package pl.pronux.sokker.model;

/** a published SokkerViewer release */
public class Release {

	private final String version;

	private final String url;

	/** tag like "v0.15.3"; the leading v is dropped */
	public Release(String tag, String url) {
		this.version = tag.startsWith("v") ? tag.substring(1) : tag;
		this.url = url;
	}

	public String getVersion() {
		return version;
	}

	public String getUrl() {
		return url;
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
