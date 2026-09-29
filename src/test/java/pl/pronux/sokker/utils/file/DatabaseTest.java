package pl.pronux.sokker.utils.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.Date;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import pl.pronux.sokker.model.SokkerViewerSettings;

public class DatabaseTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private SokkerViewerSettings settings;

	private File backups;

	@Before
	public void settings() throws Exception {
		Class.forName("org.hsqldb.jdbcDriver");
		settings = new SokkerViewerSettings();
		settings.setBaseDirectory(folder.getRoot().getPath());
		settings.setUsername("team");
		settings.setBackupDirectory(folder.newFolder("bak").getPath());
		folder.newFolder("db");
		backups = new File(settings.getBackupDirectory(), "team");
	}

	@Test
	public void backupIsNamedByTimeLoginAndReason() throws Exception {
		createDatabase();

		Database.backup(settings, Database.UPDATE);

		String[] names = backups.list();
		assertEquals(1, names.length);
		assertTrue(names[0], names[0].matches("[0-9]{4}-[0-9]{2}-[0-9]{2}_[0-9]{2}-[0-9]{2}-[0-9]{2}_team_update[.]bak"));
	}

	@Test
	public void backupsNeverOverwriteAndKeepTheNewestPerReason() throws Exception {
		createDatabase();

		for (int i = 0; i < Database.BACKUPS_KEPT + 3; i++) {
			Database.backup(settings, Database.MANUAL);
		}
		Database.backup(settings, Database.IMPORT);

		assertEquals(Database.BACKUPS_KEPT, count("_team_manual.bak"));
		assertEquals(1, count("_team_import.bak"));
	}

	@Test
	public void autoBackupIsMadeOnceADay() throws Exception {
		createDatabase();

		Database.backup(settings, Database.AUTO);
		Database.backup(settings, Database.AUTO);

		assertEquals(1, count("_team_auto.bak"));
	}

	@Test
	public void autoBackupFromYesterdayDoesNotStopTodays() throws Exception {
		createDatabase();
		assertTrue(backups.mkdirs());
		long yesterday = System.currentTimeMillis() - 24L * 60 * 60 * 1000;
		File old = Database.newBackupFile(backups, "team", Database.AUTO, new Date(yesterday));
		assertTrue(old.createNewFile());
		assertTrue(old.setLastModified(yesterday));

		Database.backup(settings, Database.AUTO);

		assertEquals(2, count("_team_auto.bak"));
	}

	@Test
	public void secondBackupInTheSameSecondGetsACounter() throws IOException {
		assertTrue(backups.mkdirs());
		Date now = new Date();
		File first = Database.newBackupFile(backups, "team", Database.MANUAL, now);
		assertTrue(first.createNewFile());

		File second = Database.newBackupFile(backups, "team", Database.MANUAL, now);

		assertNotEquals(first, second);
		assertTrue(second.getName().endsWith("_team_manual.bak"));
	}

	@Test
	public void backupWithoutADatabaseCopiesNothing() throws IOException {
		assertTrue(Database.backup(settings, Database.MANUAL));

		assertEquals(0, backups.list().length);
		assertFalse(new File(folder.getRoot(), "db/db_file_team.script").exists());
	}

	@Test
	public void backupHoldsWhatTheOpenDatabaseHasNotSaved() throws Exception {
		createDatabase();
		Connection open = connect();
		try {
			open.createStatement().execute("UPDATE T SET X = 2");

			Database.backup(settings, Database.UPDATE);
		} finally {
			open.close();
		}

		String script = new String(Files.readAllBytes(backups.listFiles()[0].toPath()), "ISO-8859-1");
		assertTrue(script.contains("INSERT INTO T VALUES(2)"));
	}

	@Test
	public void restoreReplacesTheDatabaseWhileAConnectionIsOpen() throws Exception {
		createDatabase();
		Database.backup(settings, Database.MANUAL);
		Connection open = connect();
		open.createStatement().execute("UPDATE T SET X = 2");

		Database.restore(settings, backups.list()[0]);

		Connection reopened = connect();
		try {
			ResultSet row = reopened.createStatement().executeQuery("SELECT X FROM T");
			assertTrue(row.next());
			assertEquals(1, row.getInt(1));
		} finally {
			reopened.close();
			open.close();
		}
	}

	/** a database with one row, x = 1, closed cleanly */
	private void createDatabase() throws Exception {
		Connection connection = connect();
		try {
			connection.createStatement().execute("CREATE TABLE T(X INTEGER)");
			connection.createStatement().execute("INSERT INTO T VALUES(1)");
		} finally {
			connection.close();
		}
	}

	private Connection connect() throws Exception {
		String path = new File(folder.getRoot(), "db" + File.separator + "db_file_team").getPath();
		return DriverManager.getConnection("jdbc:hsqldb:" + path + ";shutdown=true", "sa", "");
	}

	private int count(final String suffix) {
		return backups.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(suffix);
			}
		}).length;
	}
}
