package pl.pronux.sokker.utils.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ZipTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void unpacksFoldersAndFiles() throws IOException {
		File archive = zip("SokkerViewer/", "", "SokkerViewer/lib/core.jar", "core", "SokkerViewer/Launcher.jar", "launcher");
		File target = folder.newFolder("target");

		Zip.unzip(archive, target);

		assertEquals("core", read(new File(target, "SokkerViewer/lib/core.jar")));
		assertEquals("launcher", read(new File(target, "SokkerViewer/Launcher.jar")));
	}

	@Test
	public void entryOutsideTheTargetIsRefused() throws IOException {
		File archive = zip("../evil.txt", "evil");
		File target = folder.newFolder("target");
		try {
			Zip.unzip(archive, target);
			fail("unzip wrote outside its target");
		} catch (IOException e) {
			assertFalse(new File(folder.getRoot(), "evil.txt").exists());
		}
	}

	/** name and content pairs; a name ending in / is a folder */
	private File zip(String... entries) throws IOException {
		File archive = folder.newFile("test.zip");
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(archive));
		try {
			for (int i = 0; i < entries.length; i += 2) {
				out.putNextEntry(new ZipEntry(entries[i]));
				out.write(entries[i + 1].getBytes("UTF-8"));
				out.closeEntry();
			}
		} finally {
			out.close();
		}
		return archive;
	}

	private static String read(File file) throws IOException {
		return new String(Files.readAllBytes(file.toPath()), "UTF-8");
	}
}
