package pl.pronux.sokker.launcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class UpdaterTest {

	private static final boolean WINDOWS = System.getProperty("os.name").startsWith("Windows");

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void swapReplacesProgramFilesAndKeepsThePlayersData() throws IOException {
		Path install = install();
		Path release = release();

		assertTrue(new Updater(install, release).swap());

		assertEquals("new", read(install.resolve("Launcher.jar")));
		assertEquals("new", read(install.resolve("lib/core.jar")));
		assertFalse(Files.exists(install.resolve("lib/gone.jar")));
		assertEquals("new", read(install.resolve("runtime/bin/javaw.exe")));
		assertEquals("data", read(install.resolve("db/db_file_team.script")));
		assertEquals("mine", read(install.resolve("settings/sokker.properties")));
		assertEquals("log", read(install.resolve("sokker.log")));
		assertFalse(Files.exists(oldFiles(release)));
	}

	@Test
	public void swapKeepsFontsThePlayerAdded() throws IOException {
		Path install = install();
		write(install.resolve("fonts/FreeMono.ttf"), "old");
		write(install.resolve("fonts/cour.ttf"), "mine");
		Path release = release();
		write(release.resolve("fonts/FreeMono.ttf"), "new");

		assertTrue(new Updater(install, release).swap());

		assertEquals("new", read(install.resolve("fonts/FreeMono.ttf")));
		assertEquals("mine", read(install.resolve("fonts/cour.ttf")));
	}

	@Test
	public void dataFoldersAreNotProgramFiles() throws IOException {
		Path release = folder.newFolder("SokkerViewer").toPath();
		for (String name : Arrays.asList("bak", "db", "Settings", "tmp", "xml", "lib", "runtime")) {
			Files.createDirectories(release.resolve(name));
		}
		write(release.resolve("Launcher.jar"), "new");

		assertEquals(Arrays.asList("Launcher.jar", "lib", "runtime"), new Updater(folder.getRoot().toPath(), release).programFiles());
	}

	@Test
	public void swapWaitsWhileAProgramFileIsOpen() throws Exception {
		assumeTrue(WINDOWS);
		Path install = install();
		Path release = release();
		final FileInputStream open = new FileInputStream(install.resolve("lib/core.jar").toFile());
		Thread closer = new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(1500);
					open.close();
				} catch (Exception e) {
				}
			}
		};
		long started = System.currentTimeMillis();
		closer.start();

		assertTrue(new Updater(install, release).swap());

		assertTrue(System.currentTimeMillis() - started >= 1000);
		assertEquals("new", read(install.resolve("lib/core.jar")));
	}

	@Test
	public void failedCopyPutsTheOldFilesBack() throws IOException {
		assumeTrue(WINDOWS);
		Path install = install();
		Path release = release();
		RandomAccessFile file = new RandomAccessFile(release.resolve("lib/core.jar").toFile(), "rw");
		FileLock lock = file.getChannel().lock();
		try {
			assertFalse(new Updater(install, release).swap());
		} finally {
			lock.release();
			file.close();
		}

		assertEquals("old", read(install.resolve("Launcher.jar")));
		assertEquals("old", read(install.resolve("lib/core.jar")));
		assertEquals("old", read(install.resolve("lib/gone.jar")));
		assertFalse(Files.exists(install.resolve("runtime")));
		assertEquals("data", read(install.resolve("db/db_file_team.script")));
		assertFalse(Files.exists(oldFiles(release)));
	}

	/** where the updater parks the install's old program files */
	private static Path oldFiles(Path release) {
		return release.getParent().resolveSibling("update-old");
	}

	private Path install() throws IOException {
		Path install = folder.newFolder("install").toPath();
		write(install.resolve("Launcher.jar"), "old");
		write(install.resolve("lib/core.jar"), "old");
		write(install.resolve("lib/gone.jar"), "old");
		write(install.resolve("db/db_file_team.script"), "data");
		write(install.resolve("settings/sokker.properties"), "mine");
		write(install.resolve("sokker.log"), "log");
		return install;
	}

	private Path release() throws IOException {
		Path release = folder.newFolder("tmp", "update", "SokkerViewer").toPath();
		write(release.resolve("Launcher.jar"), "new");
		write(release.resolve("lib/core.jar"), "new");
		write(release.resolve("runtime/bin/javaw.exe"), "new");
		write(release.resolve("settings/sokker.properties"), "default");
		Files.createDirectories(release.resolve("db"));
		return release;
	}

	private static void write(Path path, String text) throws IOException {
		Files.createDirectories(path.getParent());
		Files.write(path, text.getBytes("UTF-8"));
	}

	private static String read(Path path) throws IOException {
		return new String(Files.readAllBytes(path), "UTF-8");
	}
}
