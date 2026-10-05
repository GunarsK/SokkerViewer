package pl.pronux.sokker.actions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pl.pronux.sokker.downloader.ReleaseDownloader;
import pl.pronux.sokker.exceptions.SVException;
import pl.pronux.sokker.handlers.SettingsHandler;
import pl.pronux.sokker.interfaces.ProgressMonitor;
import pl.pronux.sokker.model.Release;
import pl.pronux.sokker.model.SokkerViewerSettings;
import pl.pronux.sokker.utils.file.Database;
import pl.pronux.sokker.utils.file.OperationOnFile;
import pl.pronux.sokker.utils.file.Zip;

/** downloads a release and hands it to the updater in Launcher.jar */
public final class UpdateManager {

	private static final String UPDATER_CLASS = "pl.pronux.sokker.launcher.Updater";

	/** this computer's bundle, null when none is built for it */
	private static final Bundle BUNDLE = Bundle.current();

	private UpdateManager() {
	}

	/** a bundle install and a release zip to download */
	public static boolean canUpdate(Release release) {
		File base = baseDirectory();
		return BUNDLE != null && release.getDownloadUrl() != null && new File(base, BUNDLE.getLauncher()).isFile()
			&& new File(base, "Launcher.jar").isFile() && new File(base, "runtime").isDirectory();
	}

	/** removes what the last update left in tmp/update */
	public static void cleanUp() {
		File dir = updateDirectory();
		if (dir.exists()) {
			OperationOnFile.deleteDir(dir);
		}
	}

	/** downloads, checks and unpacks the release; null when cancelled */
	public static File prepare(Release release, ProgressMonitor monitor) throws IOException {
		cleanUp();
		File dir = updateDirectory();
		if (!dir.mkdirs()) {
			throw new IOException("cannot create " + dir);
		}
		File zip = new File(dir, "release.zip");
		ReleaseDownloader downloader = new ReleaseDownloader();
		downloader.setProxySettings(settings().getProxySettings());
		String sha256 = downloader.download(release.getDownloadUrl(), zip, monitor);
		if (sha256 == null) {
			return null;
		}
		if (release.getSha256() != null && !release.getSha256().equalsIgnoreCase(sha256)) {
			throw new IOException("the download does not match github's checksum");
		}
		Zip.unzip(zip, dir);
		zip.delete();
		File unpacked = new File(dir, "SokkerViewer");
		if (!new File(unpacked, "Launcher.jar").isFile() || !java(unpacked).isFile()) {
			throw new IOException("the release zip has no SokkerViewer folder with its runtime");
		}
		makeExecutable(unpacked, BUNDLE);
		OperationOnFile.copyFile(new File(baseDirectory(), "Launcher.jar"), updaterJar());
		return unpacked;
	}

	/** marks the launcher and the runtime's programs executable */
	static void makeExecutable(File unpacked, Bundle bundle) throws IOException {
		List<File> programs = new ArrayList<File>();
		programs.add(new File(unpacked, bundle.getLauncher()));
		programs.add(new File(runtime(unpacked, "lib"), "jspawnhelper"));
		File[] bin = runtime(unpacked, "bin").listFiles();
		if (bin != null) {
			programs.addAll(Arrays.asList(bin));
		}
		for (File program : programs) {
			if (program.isFile() && !program.setExecutable(true, false)) {
				throw new IOException("cannot make " + program + " executable");
			}
		}
	}

	/** the database backup taken before an update */
	public static void backupDatabase() throws IOException {
		Database.backup(settings(), Database.UPDATE);
	}

	/** starts the updater, which waits for SokkerViewer to close */
	public static void startUpdater(File unpacked) throws IOException, SVException {
		SokkerViewerSettings settings = settings();
		settings.setCheckProperties(true);
		SettingsManager.getInstance().updateSettings(settings);
		File base = baseDirectory();
		ProcessBuilder builder = new ProcessBuilder(java(unpacked).getPath(), "-cp", updaterJar().getPath(), UPDATER_CLASS, base.getPath(),
			unpacked.getPath(), BUNDLE.getLauncher(), String.valueOf(ProcessHandle.current().pid()));
		builder.directory(base);
		builder.redirectErrorStream(true);
		builder.redirectOutput(ProcessBuilder.Redirect.appendTo(new File(base, "tmp" + File.separator + "update.log")));
		builder.start();
	}

	private static File java(File unpacked) {
		return new File(runtime(unpacked, "bin"), BUNDLE.getJava());
	}

	/** a folder of the release's java runtime */
	private static File runtime(File unpacked, String folder) {
		return new File(new File(unpacked, "runtime"), folder);
	}

	private static SokkerViewerSettings settings() {
		return SettingsHandler.getSokkerViewerSettings();
	}

	private static File baseDirectory() {
		return new File(settings().getBaseDirectory());
	}

	private static File updateDirectory() {
		return new File(baseDirectory(), "tmp" + File.separator + "update");
	}

	/** the copy of Launcher.jar the updater runs from */
	private static File updaterJar() {
		return new File(updateDirectory(), "updater.jar");
	}
}
