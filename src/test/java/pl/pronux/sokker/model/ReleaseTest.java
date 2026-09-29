package pl.pronux.sokker.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ReleaseTest {

	private static final String URL = "https://github.com/GunarsK/SokkerViewer/releases/tag/v0.15.4";

	@Test
	public void tagLosesItsLeadingV() {
		assertEquals("0.15.4", new Release("v0.15.4", URL).getVersion());
		assertEquals("0.15.4", new Release("0.15.4", URL).getVersion());
	}

	@Test
	public void higherPartIsNewer() {
		assertTrue(new Release("v0.15.4", URL).isNewerThan("0.15.3"));
		assertTrue(new Release("v0.16.0", URL).isNewerThan("0.15.9"));
		assertTrue(new Release("v1.0.0", URL).isNewerThan("0.15.3"));
	}

	@Test
	public void partsCompareAsNumbers() {
		assertTrue(new Release("v0.15.10", URL).isNewerThan("0.15.9"));
		assertFalse(new Release("v0.15.9", URL).isNewerThan("0.15.10"));
	}

	@Test
	public void sameOrLowerIsNotNewer() {
		assertFalse(new Release("v0.15.3", URL).isNewerThan("0.15.3"));
		assertFalse(new Release("v0.15.2", URL).isNewerThan("0.15.3"));
		assertFalse(new Release("v0.14.9", URL).isNewerThan("0.15.0"));
	}

	@Test
	public void missingPartCountsAsZero() {
		assertFalse(new Release("v0.16", URL).isNewerThan("0.16.0"));
		assertTrue(new Release("v0.16.1", URL).isNewerThan("0.16"));
	}

	@Test
	public void unreadableTagIsNotNewer() {
		assertFalse(new Release("latest", URL).isNewerThan("0.15.3"));
	}
}
