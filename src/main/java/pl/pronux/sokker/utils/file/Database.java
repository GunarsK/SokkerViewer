package pl.pronux.sokker.utils.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import pl.pronux.sokker.model.SokkerViewerSettings;
import pl.pronux.sokker.utils.Log;

public class Database {

	/** why a backup was made, the end of its file name */
	public static final String MANUAL = "manual";

	public static final String UPDATE = "update";

	public static final String IMPORT = "import";

	public static final String AUTO = "auto";

	/** backups kept per reason */
	static final int BACKUPS_KEPT = 10;

	private static final String BACKUP_DAY = "yyyy-MM-dd";

	private static final String BACKUP_TIME = BACKUP_DAY + "_HH-mm-ss";

	private static final String BAK = ".bak";

	private static final String PREFIX = "db_file_";

	private static final String SCRIPT = ".script";

	private static final String LOG = ".log";

	/** the files of an hsqldb database */
	private static final String[] FILES = {".properties", SCRIPT, LOG, ".data", ".backup", ".lck"};

	private static final String CREATE_SYSTEM = "CREATE MEMORY TABLE SYSTEM(";

	private static final String INSERT_SYSTEM = "INSERT INTO SYSTEM VALUES(";

	/** copies the database to [time]_[login]_[reason].bak; never overwrites, keeps the newest per reason, auto once a day */
	public static boolean backup(SokkerViewerSettings settings, String reason) throws IOException {
		File dbDir = backupDirectory(settings);
		if (!dbDir.isDirectory() && !dbDir.mkdirs()) {
			throw new IOException("Missing backup directory");
		}
		if (AUTO.equals(reason) && madeToday(dbDir, reason)) {
			return true;
		}
		// without a .log the .script already holds everything
		if (new File(databasePath(settings) + LOG).exists()) {
			execute(settings, "CHECKPOINT");
		}
		File dbFile = new File(databasePath(settings) + SCRIPT);
		if (dbFile.exists()) {
			OperationOnFile.copyFile(dbFile, newBackupFile(dbDir, settings.getUsername(), reason, new Date()));
			prune(dbDir, reason);
		}
		return true;
	}

	/** replaces the database with a backup, shutting the open database down first */
	public static void restore(SokkerViewerSettings settings, String filename) throws IOException {
		File dbFile = new File(databasePath(settings) + SCRIPT);
		File dbBakFile = new File(backupDirectory(settings), filename);
		execute(settings, "SHUTDOWN");
		OperationOnFile.copyFile(dbBakFile, dbFile);
	}

	/** a file no backup has yet; a second one in the same second gets a counter */
	static File newBackupFile(File dir, String login, String reason, Date date) {
		String time = new SimpleDateFormat(BACKUP_TIME).format(date);
		File file = new File(dir, time + "_" + login + "_" + reason + BAK);
		for (int n = 2; file.exists(); n++) {
			file = new File(dir, time + "-" + n + "_" + login + "_" + reason + BAK);
		}
		return file;
	}

	/** the login's backups, newest first */
	public static List<File> backups(SokkerViewerSettings settings) {
		return backups(backupDirectory(settings), BAK);
	}

	/** deletes all but the newest BACKUPS_KEPT backups made for the reason */
	static void prune(File dir, String reason) {
		List<File> files = backups(dir, "_" + reason + BAK);
		for (File file : files.subList(Math.min(BACKUPS_KEPT, files.size()), files.size())) {
			if (!file.delete()) {
				Log.warning("Cannot delete old backup " + file);
			}
		}
	}

	/** true when the reason's newest backup is named with today's date */
	private static boolean madeToday(File dir, String reason) {
		List<File> files = backups(dir, "_" + reason + BAK);
		return !files.isEmpty() && files.get(0).getName().startsWith(new SimpleDateFormat(BACKUP_DAY).format(new Date()));
	}

	/** the folder's files ending in suffix, newest first */
	private static List<File> backups(File dir, final String suffix) {
		File[] files = dir.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.isFile() && file.getName().endsWith(suffix);
			}
		});
		List<File> list = files == null ? new ArrayList<File>() : new ArrayList<File>(Arrays.asList(files));
		Collections.sort(list, new Comparator<File>() {
			public int compare(File first, File second) {
				int byTime = Long.valueOf(second.lastModified()).compareTo(Long.valueOf(first.lastModified()));
				return byTime != 0 ? byTime : second.getName().compareTo(first.getName());
			}
		});
		return list;
	}

	private static File backupDirectory(SokkerViewerSettings settings) {
		return new File(settings.getBackupDirectory(), settings.getUsername());
	}

	/** runs one statement on the login's database; nothing when it has none yet */
	private static void execute(SokkerViewerSettings settings, String sql) throws IOException {
		String path = databasePath(settings);
		if (!new File(path + SCRIPT).exists() && !new File(path + LOG).exists()) {
			return;
		}
		try {
			Class.forName("org.hsqldb.jdbcDriver");
			Connection connection = DriverManager.getConnection("jdbc:hsqldb:" + path + ";shutdown=true", "sa", "");
			try {
				connection.createStatement().execute(sql);
			} finally {
				connection.close();
			}
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		} catch (SQLException e) {
			throw new IOException(sql + " failed on " + path, e);
		}
	}

	private static String databasePath(SokkerViewerSettings settings) {
		return settings.getBaseDirectory() + File.separator + "db" + File.separator + PREFIX + settings.getUsername();
	}

	/** renames the team's most recently synced database, with its xml and backup folders, to the login */
	public static void link(SokkerViewerSettings settings, int teamId) throws IOException {
		File dbDir = new File(settings.getBaseDirectory() + File.separator + "db");
		String name = null;
		long latest = Long.MIN_VALUE;
		for (File file : OperationOnFile.getDirList(dbDir)) {
			if (file.getName().startsWith(PREFIX) && file.getName().endsWith(SCRIPT)) {
				long millis = lastSync(file, teamId);
				if (millis > latest) {
					latest = millis;
					name = file.getName().substring(PREFIX.length(), file.getName().length() - SCRIPT.length());
				}
			}
		}
		if (name == null) {
			return;
		}
		String login = settings.getUsername();
		List<String> renamed = new ArrayList<String>();
		for (String extension : FILES) {
			File file = new File(dbDir, PREFIX + name + extension);
			File target = new File(dbDir, PREFIX + login + extension);
			if (file.exists()) {
				if (target.exists() || !file.renameTo(target)) {
					for (String done : renamed) {
						new File(dbDir, PREFIX + login + done).renameTo(new File(dbDir, PREFIX + name + done));
					}
					throw new IOException("Cannot rename " + file + " to " + target);
				}
				renamed.add(extension);
			}
		}
		rename(new File(settings.getBaseDirectory() + File.separator + "xml", name), login);
		rename(new File(settings.getBackupDirectory(), name), login);
		Log.info("Team " + teamId + ": database " + PREFIX + name + " renamed to " + PREFIX + login);
	}

	/** renames a login's folder to another login */
	private static void rename(File folder, String login) {
		File target = new File(folder.getParentFile(), login);
		if (folder.exists() && (target.exists() || !folder.renameTo(target))) {
			Log.warning("Cannot rename " + folder + " to " + target);
		}
	}

	/** last sync time from a database script's SYSTEM row; Long.MIN_VALUE when the row is not the team's */
	private static long lastSync(File script, int teamId) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(script), "ISO-8859-1"));
		try {
			List<String> columns = new ArrayList<String>();
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith(CREATE_SYSTEM)) {
					for (String column : line.substring(CREATE_SYSTEM.length()).split(",")) {
						columns.add(column.trim().split(" ")[0]);
					}
				} else if (line.startsWith(INSERT_SYSTEM)) {
					String[] values = line.substring(INSERT_SYSTEM.length(), line.length() - 1).split(",");
					int team = columns.indexOf("TEAM_ID");
					int millis = columns.indexOf("LAST_MODIFICATION_MILLIS");
					if (team < 0 || millis < 0 || !values[team].equals(String.valueOf(teamId))) {
						return Long.MIN_VALUE;
					}
					return values[millis].equals("NULL") ? 0 : Long.parseLong(values[millis]);
				}
			}
			return Long.MIN_VALUE;
		} finally {
			reader.close();
		}
	}
}
