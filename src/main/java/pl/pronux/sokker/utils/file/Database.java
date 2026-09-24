package pl.pronux.sokker.utils.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import pl.pronux.sokker.model.SokkerViewerSettings;
import pl.pronux.sokker.utils.Log;

public class Database {

	private static final String PREFIX = "db_file_";

	private static final String SCRIPT = ".script";

	/** the files of an hsqldb database */
	private static final String[] FILES = {".properties", SCRIPT, ".log", ".data", ".backup", ".lck"};

	private static final String CREATE_SYSTEM = "CREATE MEMORY TABLE SYSTEM(";

	private static final String INSERT_SYSTEM = "INSERT INTO SYSTEM VALUES(";

	public static boolean backup(SokkerViewerSettings settings, String filename) throws IOException {
		File dbDir = new File(settings.getBackupDirectory());

		if (!dbDir.exists() && !OperationOnFile.createDirectory(dbDir)) {
			throw new IOException("Missing backup directory"); 
		}
		if (new File(dbDir, settings.getUsername()).exists()) {
			dbDir = new File(dbDir, settings.getUsername());
		} else {
			dbDir = new File(dbDir, settings.getUsername());
			dbDir.mkdir();
		}

		File dbFile = new File(settings.getBaseDirectory() + File.separator + "db" + File.separator + "db_file_" + settings.getUsername() + ".script");   
		if (dbFile.exists()) {
			File dbBakFile = new File(dbDir, filename);
			OperationOnFile.copyFile(dbFile, dbBakFile);
		}
		return true;
	}

	public static boolean backup(SokkerViewerSettings settings) throws IOException {
		return backup(settings, new GregorianCalendar().getTimeInMillis() + ".bak"); 
	}

	public static void restore(SokkerViewerSettings settings, String filename) throws IOException {
		File dbDir = new File(settings.getBackupDirectory() + File.separator + settings.getUsername() + File.separator);
		File dbFile = new File(settings.getBaseDirectory() + File.separator + "db" + File.separator + "db_file_" + settings.getUsername() + ".script");   
		File dbBakFile = new File(dbDir, filename);
		OperationOnFile.copyFile(dbBakFile, dbFile);
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
