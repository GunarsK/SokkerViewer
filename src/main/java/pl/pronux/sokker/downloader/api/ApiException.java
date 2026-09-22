package pl.pronux.sokker.downloader.api;

import java.io.IOException;

/** a non 2xx answer from the sokker.org json api */
public class ApiException extends IOException {

	private static final long serialVersionUID = 1L;

	private final int statusCode;

	private final boolean cloudflareChallenge;

	public ApiException(int statusCode, String contentType, String body) {
		this(statusCode, isChallenge(statusCode, contentType, body));
	}

	private ApiException(int statusCode, boolean cloudflareChallenge) {
		super("sokker.org api answered HTTP " + statusCode + (cloudflareChallenge ? " (cloudflare browser challenge - user agent rejected)" : ""));
		this.statusCode = statusCode;
		this.cloudflareChallenge = cloudflareChallenge;
	}

	/**
	 * cloudflare answers clients it does not trust with a browser challenge page instead of
	 * json; a fake browser user agent without a javascript engine triggers it
	 */
	private static boolean isChallenge(int statusCode, String contentType, String body) {
		return statusCode == 403 && contentType != null && contentType.toLowerCase().contains("text/html") && body != null
			&& (body.contains("__cf_chl_") || body.contains("challenge-platform") || body.contains("Just a moment"));
	}

	public boolean isNotLoggedIn() {
		return statusCode == 401;
	}

	/** a real 403 from sokker, e.g. a training week outside a non-plus user's window */
	public boolean isForbidden() {
		return statusCode == 403 && !cloudflareChallenge;
	}
}
