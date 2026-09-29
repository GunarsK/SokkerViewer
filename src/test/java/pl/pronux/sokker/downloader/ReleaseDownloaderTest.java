package pl.pronux.sokker.downloader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.junit.Test;

import pl.pronux.sokker.model.Release;

public class ReleaseDownloaderTest {

	@Test
	public void picksTheWindowsZipAndItsChecksum() throws IOException {
		Release release = ReleaseDownloader.parse(json("{'tag_name':'v0.15.4','html_url':'https://github.com/r/v0.15.4','assets':["
			+ "{'name':'notes.txt','browser_download_url':'https://github.com/r/notes.txt','digest':'sha256:00'},"
			+ "{'name':'sokker-0.15.4-win64-with-java.zip','browser_download_url':'https://github.com/r/sokker.zip','digest':'sha256:abc123'}]}"));

		assertEquals("0.15.4", release.getVersion());
		assertEquals("https://github.com/r/v0.15.4", release.getUrl());
		assertEquals("https://github.com/r/sokker.zip", release.getDownloadUrl());
		assertEquals("abc123", release.getSha256());
	}

	@Test
	public void releaseWithoutTheZipHasNoDownload() throws IOException {
		Release release = ReleaseDownloader.parse(json("{'tag_name':'v0.15.4','html_url':'https://github.com/r/v0.15.4','assets':[]}"));

		assertNull(release.getDownloadUrl());
		assertNull(release.getSha256());
	}

	@Test
	public void checksumOtherThanSha256IsDropped() throws IOException {
		Release release = ReleaseDownloader.parse(json("{'tag_name':'v0.15.4','html_url':'https://github.com/r/v0.15.4','assets':["
			+ "{'name':'sokker-0.15.4-win64-with-java.zip','browser_download_url':'https://github.com/r/sokker.zip','digest':'md5:ff'}]}"));

		assertEquals("https://github.com/r/sokker.zip", release.getDownloadUrl());
		assertNull(release.getSha256());
	}

	@Test(expected = IOException.class)
	public void releaseWithoutTagIsRefused() throws IOException {
		ReleaseDownloader.parse(json("{'html_url':'https://github.com/r/v0.15.4'}"));
	}

	@Test(expected = IOException.class)
	public void htmlPageIsRefused() throws IOException {
		ReleaseDownloader.parse("<html><body>sign in to the wifi</body></html>");
	}

	/** single quotes turned into json's double quotes */
	private static String json(String singleQuoted) {
		return singleQuoted.replace((char) 39, (char) 34);
	}
}
