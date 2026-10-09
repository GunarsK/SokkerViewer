package pl.pronux.sokker.downloader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.junit.Test;

import pl.pronux.sokker.actions.Bundle;
import pl.pronux.sokker.model.Release;

public class ReleaseDownloaderTest {

	/** a release with notes, every bundle and the windows 8.1 zip */
	private static final String BUNDLES = "{'tag_name':'v0.15.4','html_url':'https://github.com/r/v0.15.4','assets':["
		+ "{'name':'notes.txt','browser_download_url':'https://github.com/r/notes.txt','digest':'sha256:00'},"
		+ "{'name':'sokker-0.15.4-windows-8.1-with-java.zip','browser_download_url':'https://github.com/r/win81.zip','digest':'sha256:81'},"
		+ "{'name':'sokker-0.15.4-macos-arm64-with-java.zip','browser_download_url':'https://github.com/r/mac.zip','digest':'sha256:def456'},"
		+ "{'name':'sokker-0.15.4-macos-x64-with-java.zip','browser_download_url':'https://github.com/r/intel.zip','digest':'sha256:789aaa'},"
		+ "{'name':'sokker-0.15.4-win64-with-java.zip','browser_download_url':'https://github.com/r/sokker.zip','digest':'sha256:abc123'}]}";

	@Test
	public void picksTheWindowsZipAndItsChecksum() throws IOException {
		Release release = ReleaseDownloader.parse(json(BUNDLES), Bundle.WINDOWS);

		assertEquals("0.15.4", release.getVersion());
		assertEquals("https://github.com/r/v0.15.4", release.getUrl());
		assertEquals("https://github.com/r/sokker.zip", release.getDownloadUrl());
		assertEquals("abc123", release.getSha256());
	}

	@Test
	public void picksTheAppleSiliconMacZipAndItsChecksum() throws IOException {
		Release release = ReleaseDownloader.parse(json(BUNDLES), Bundle.MACOS_ARM64);

		assertEquals("https://github.com/r/mac.zip", release.getDownloadUrl());
		assertEquals("def456", release.getSha256());
	}

	@Test
	public void picksTheIntelMacZipAndItsChecksum() throws IOException {
		Release release = ReleaseDownloader.parse(json(BUNDLES), Bundle.MACOS_X64);

		assertEquals("https://github.com/r/intel.zip", release.getDownloadUrl());
		assertEquals("789aaa", release.getSha256());
	}

	@Test
	public void systemWithoutBundleHasNoDownload() throws IOException {
		Release release = ReleaseDownloader.parse(json(BUNDLES), null);

		assertEquals("0.15.4", release.getVersion());
		assertNull(release.getDownloadUrl());
	}

	@Test
	public void releaseWithoutTheZipHasNoDownload() throws IOException {
		Release release = ReleaseDownloader.parse(json("{'tag_name':'v0.15.4','html_url':'https://github.com/r/v0.15.4','assets':[]}"), Bundle.WINDOWS);

		assertNull(release.getDownloadUrl());
		assertNull(release.getSha256());
	}

	@Test
	public void checksumOtherThanSha256IsDropped() throws IOException {
		Release release = ReleaseDownloader.parse(json("{'tag_name':'v0.15.4','html_url':'https://github.com/r/v0.15.4','assets':["
			+ "{'name':'sokker-0.15.4-win64-with-java.zip','browser_download_url':'https://github.com/r/sokker.zip','digest':'md5:ff'}]}"), Bundle.WINDOWS);

		assertEquals("https://github.com/r/sokker.zip", release.getDownloadUrl());
		assertNull(release.getSha256());
	}

	@Test(expected = IOException.class)
	public void releaseWithoutTagIsRefused() throws IOException {
		ReleaseDownloader.parse(json("{'html_url':'https://github.com/r/v0.15.4'}"), Bundle.WINDOWS);
	}

	@Test(expected = IOException.class)
	public void htmlPageIsRefused() throws IOException {
		ReleaseDownloader.parse("<html><body>sign in to the wifi</body></html>", Bundle.WINDOWS);
	}

	/** single quotes turned into json's double quotes */
	private static String json(String singleQuoted) {
		return singleQuoted.replace((char) 39, (char) 34);
	}
}
