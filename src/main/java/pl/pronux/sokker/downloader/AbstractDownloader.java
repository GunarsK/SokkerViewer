package pl.pronux.sokker.downloader;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import pl.pronux.sokker.interfaces.SV;
import pl.pronux.sokker.model.ProxySettings;

public class AbstractDownloader {

	public static final String POST = "POST";
	public static final String GET = "GET";

	/** the user agent sent to sokker.org */
	protected static final String USER_AGENT = "SokkerViewer/" + SV.SK_VERSION;

	private Proxy proxy = Proxy.NO_PROXY;

	private String proxyAuth;

	public Proxy getProxy() {
		return proxy;
	}

	public void setProxy(Proxy proxy) {
		this.proxy = proxy;
	}

	public String getProxyAuth() {
		return proxyAuth;
	}

	public void setProxyAuth(String proxyAuth) {
		this.proxyAuth = proxyAuth;
	}

	/** the proxy from the user's settings, none when they are null */
	public void setProxySettings(ProxySettings proxySettings) {
		if (proxySettings != null) {
			setProxy(proxySettings.getProxy());
			setProxyAuth(proxySettings.getProxyAuthentication());
		}
	}

	/**
	 * the name=value part of every Set-Cookie header of the answer, in order. sokker.org sends
	 * several cookies on log-in and getHeaderField("Set-Cookie") returns only the last one,
	 * which is not the session.
	 */
	protected static List<String> readSetCookies(HttpURLConnection connection) {
		List<String> cookies = new ArrayList<String>();
		for (int i = 1; connection.getHeaderFieldKey(i) != null || connection.getHeaderField(i) != null; i++) {
			if ("Set-Cookie".equalsIgnoreCase(connection.getHeaderFieldKey(i))) {
				cookies.add(connection.getHeaderField(i).split(";", 2)[0]);
			}
		}
		return cookies;
	}

	protected HttpURLConnection getDefaultConnection(String urlString, String type) throws IOException {

		HttpURLConnection connection = getDefaultConnection(urlString);

		if (type != null) {
			connection.setRequestMethod(type); 	
		}
		connection.setRequestProperty("User-Agent", USER_AGENT);  
		connection.setRequestProperty("Accept", "text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");  
		connection.setRequestProperty("Accept-Language", "pl");  
		connection.setRequestProperty("Accept-Charset", "UTF-8,*");  
		connection.setRequestProperty("Keep-Alive", "300");  
		connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");  
		return connection;
	}

	protected HttpURLConnection getDefaultConnection(String urlString) throws IOException {
		URL url = new URL(urlString);
		HttpURLConnection connection;
		if (Proxy.NO_PROXY.equals(proxy)) {
			connection = (HttpURLConnection) url.openConnection();
		} else {
			connection = (HttpURLConnection) url.openConnection(proxy);
			if (proxyAuth != null) {
				connection.setRequestProperty("Proxy-Authorization", "Basic " + proxyAuth);  
			}
		}
		return connection;
	}
}
