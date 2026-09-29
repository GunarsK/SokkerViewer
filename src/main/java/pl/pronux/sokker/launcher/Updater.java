package pl.pronux.sokker.launcher;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * swaps SokkerViewer's program files for an unpacked release after SokkerViewer closes.
 * Runs alone from a copy of Launcher.jar: jdk classes only.
 */
public final class Updater {

	/** the player's data next to the program, never replaced */
	private static final List<String> DATA = Arrays.asList("bak", "db", "settings", "tmp", "xml");

	/** program folder where players may add their own fonts */
	private static final String FONTS = "fonts";

	private static final long WAIT_MS = 60000;

	private static final long RETRY_MS = 500;

	private final Path install;

	private final Path release;

	/** the install's old program files during the swap, outside tmp/update */
	private final Path backup;

	Updater(Path install, Path release) {
		this.install = install;
		this.release = release;
		this.backup = release.getParent().resolveSibling("update-old");
	}

	/** args: the install folder and the unpacked release folder */
	public static void main(String[] args) {
		if (args.length != 2) {
			log("usage: Updater <install folder> <release folder>");
			return;
		}
		Path install = Paths.get(args[0]);
		Path release = Paths.get(args[1]);
		if (!Files.isRegularFile(install.resolve("Launcher.jar")) || !Files.isRegularFile(release.resolve("Launcher.jar"))) {
			log("not a SokkerViewer folder: " + install + " or " + release);
			return;
		}
		new Updater(install, release).swap();
		start(install);
	}

	/** true when the release's program files replaced the install's */
	boolean swap() {
		List<String> names;
		try {
			names = programFiles();
		} catch (IOException e) {
			log("cannot read " + release + ": " + e);
			return false;
		}
		// left by an earlier failed update
		deleteQuietly(backup);
		List<String> moved = new ArrayList<String>();
		List<String> copied = new ArrayList<String>();
		try {
			moveOut(names, moved);
			for (String name : names) {
				copied.add(name);
				copy(release.resolve(name), install.resolve(name));
			}
		} catch (IOException e) {
			log("update failed, restoring the old files: " + e);
			if (restore(moved, copied)) {
				deleteQuietly(backup);
			}
			return false;
		}
		keepFonts();
		deleteQuietly(backup);
		log("updated " + install);
		return true;
	}

	/** the release's top-level entries except the player's data */
	List<String> programFiles() throws IOException {
		List<String> names = new ArrayList<String>();
		DirectoryStream<Path> entries = Files.newDirectoryStream(release);
		try {
			for (Path entry : entries) {
				String name = entry.getFileName().toString();
				if (!DATA.contains(name.toLowerCase(Locale.ROOT))) {
					names.add(name);
				}
			}
		} finally {
			entries.close();
		}
		Collections.sort(names);
		return names;
	}

	/** moves the install's program files aside, retrying while SokkerViewer still holds them */
	private void moveOut(List<String> names, List<String> moved) throws IOException {
		Files.createDirectories(backup);
		long deadline = System.currentTimeMillis() + WAIT_MS;
		for (String name : names) {
			Path source = install.resolve(name);
			if (!Files.exists(source)) {
				continue;
			}
			while (true) {
				try {
					Files.move(source, backup.resolve(name));
					moved.add(name);
					break;
				} catch (IOException e) {
					if (System.currentTimeMillis() > deadline) {
						throw e;
					}
					sleep();
				}
			}
		}
	}

	/** removes what was copied, then moves the old program files back; true when all came back */
	private boolean restore(List<String> moved, List<String> copied) {
		for (String name : copied) {
			deleteQuietly(install.resolve(name));
		}
		boolean complete = true;
		for (String name : moved) {
			try {
				Files.move(backup.resolve(name), install.resolve(name));
			} catch (IOException e) {
				log("cannot restore " + name + ", it is in " + backup + ": " + e);
				complete = false;
			}
		}
		return complete;
	}

	/** moves fonts the player added, which the release lacks, into the new fonts folder */
	private void keepFonts() {
		Path old = backup.resolve(FONTS);
		Path fonts = install.resolve(FONTS);
		if (!Files.isDirectory(old) || !Files.isDirectory(fonts)) {
			return;
		}
		try {
			DirectoryStream<Path> entries = Files.newDirectoryStream(old);
			try {
				for (Path font : entries) {
					Path target = fonts.resolve(font.getFileName().toString());
					if (!Files.exists(target)) {
						Files.move(font, target);
					}
				}
			} finally {
				entries.close();
			}
		} catch (IOException e) {
			log("cannot keep the player's fonts: " + e);
		}
	}

	private static void copy(final Path source, final Path target) throws IOException {
		Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				Files.createDirectories(target.resolve(source.relativize(dir).toString()));
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.copy(file, target.resolve(source.relativize(file).toString()), StandardCopyOption.COPY_ATTRIBUTES);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	private static void deleteQuietly(Path path) {
		if (!Files.exists(path)) {
			return;
		}
		try {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			log("cannot delete " + path + ": " + e);
		}
	}

	/** starts SokkerViewer again, updated or not */
	private static void start(Path install) {
		try {
			new ProcessBuilder(install.resolve("SokkerViewer.exe").toString()).directory(install.toFile()).start();
		} catch (IOException e) {
			log("cannot start SokkerViewer: " + e);
		}
	}

	private static void sleep() {
		try {
			Thread.sleep(RETRY_MS);
		} catch (InterruptedException e) {
		}
	}

	private static void log(String message) {
		System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " updater: " + message);
	}
}
