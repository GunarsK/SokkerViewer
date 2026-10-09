package pl.pronux.sokker.actions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class UpdateManagerTest {

	private static final boolean WINDOWS = System.getProperty("os.name").startsWith("Windows");

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void macLauncherAndRuntimeProgramsBecomeExecutable() throws IOException {
		assumeFalse(WINDOWS);
		File unpacked = folder.newFolder("SokkerViewer");
		File launcher = file(unpacked, "SokkerViewer.command");
		File java = file(unpacked, "runtime/bin/java");
		File spawnHelper = file(unpacked, "runtime/lib/jspawnhelper");
		File library = file(unpacked, "runtime/lib/libjli.dylib");

		UpdateManager.makeExecutable(unpacked, Bundle.MACOS_ARM64);

		assertTrue(launcher.canExecute());
		assertTrue(java.canExecute());
		assertTrue(spawnHelper.canExecute());
		assertFalse(library.canExecute());
	}

	/** an empty file nobody may execute */
	private static File file(File parent, String path) throws IOException {
		File file = new File(parent, path);
		file.getParentFile().mkdirs();
		file.createNewFile();
		file.setExecutable(false, false);
		return file;
	}
}
