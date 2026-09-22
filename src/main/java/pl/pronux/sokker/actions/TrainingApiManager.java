package pl.pronux.sokker.actions;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import pl.pronux.sokker.data.sql.SQLQuery;
import pl.pronux.sokker.data.sql.SQLSession;
import pl.pronux.sokker.data.sql.dao.CountriesDao;
import pl.pronux.sokker.data.sql.dao.PlayersDao;
import pl.pronux.sokker.data.sql.dao.TeamsDao;
import pl.pronux.sokker.downloader.api.ApiDownloader;
import pl.pronux.sokker.downloader.api.ApiException;
import pl.pronux.sokker.downloader.api.PlayerTrainingReport;
import pl.pronux.sokker.downloader.api.TrainingReportParser;
import pl.pronux.sokker.downloader.api.TrainingWeek;
import pl.pronux.sokker.interfaces.DateConst;
import pl.pronux.sokker.interfaces.ProgressMonitor;
import pl.pronux.sokker.model.Club;
import pl.pronux.sokker.model.Country;
import pl.pronux.sokker.model.Date;
import pl.pronux.sokker.model.Money;
import pl.pronux.sokker.model.PlayerSkills;
import pl.pronux.sokker.model.SokkerDate;
import pl.pronux.sokker.model.SokkerViewerSettings;
import pl.pronux.sokker.model.Training;
import pl.pronux.sokker.utils.Log;

/**
 * writes what sokker.org's json api says about training weeks into the training and
 * player_skills tables. The xml sync stays the source of everything else; this corrects the
 * types of weeks the xml sync recorded and adds weeks it missed.
 */
public final class TrainingApiManager {

	/** weeks looked at after every sync: the latest completed one and the four before it, sokker's non-plus window */
	private static final int RECENT_WEEKS = 5;

	/** a player sold and bought back is absent for a few weeks; three empty weeks in a row end the history */
	private static final int EMPTY_WEEKS_TO_STOP = 3;

	/**
	 * SokkerDate's calendar is anchored on CET (zone offset minus an hour), so a machine west
	 * of it reads the week change that much later - up to 13 hours at the -12 zone
	 */
	private static final long MAX_ZONE_LAG = 13 * DateConst.HOUR;

	private static TrainingApiManager instance = new TrainingApiManager();

	private TrainingApiManager() {
	}

	public static TrainingApiManager getInstance() {
		return instance;
	}

	/** what a walk did */
	public static class Result {

		private int created;

		private int updated;

		private boolean forbidden;

		public int getCreated() {
			return created;
		}

		public int getUpdated() {
			return updated;
		}

		/** sokker refused an older week: the account has no plus, so the walk stopped there */
		public boolean isForbidden() {
			return forbidden;
		}

		public boolean hasChanges() {
			return created + updated > 0;
		}
	}

	/** after a sync: the last RECENT_WEEKS completed weeks */
	public Result synchronizeRecent(SokkerViewerSettings settings) throws IOException, SQLException {
		return synchronize(settings, RECENT_WEEKS, null);
	}

	/** the menu action: every week sokker still answers for, newest first */
	public Result importHistory(SokkerViewerSettings settings, ProgressMonitor monitor) throws IOException, SQLException {
		return synchronize(settings, Integer.MAX_VALUE, monitor);
	}

	private Result synchronize(SokkerViewerSettings settings, int maxWeeksBack, ProgressMonitor monitor) throws IOException, SQLException {
		boolean newConnection = SQLQuery.connect();
		try {
			Set<Integer> confirmed = new TeamsDao(SQLSession.getConnection()).getApiConfirmedWeeks();
			if (allConfirmed(confirmed, maxWeeksBack)) {
				return new Result();
			}
			return walk(openSession(settings), confirmed, maxWeeksBack, monitor);
		} finally {
			SQLQuery.close(newConnection);
		}
	}

	/**
	 * true when every week the walk could ask about is already confirmed, so there is nothing
	 * to log in for. Worth checking before opening a session: the login is a credential post,
	 * and a re-sync inside the same week is the common case. The window is measured a zone's
	 * lag into the future on purpose - an estimate that lands one week short of sokker's would
	 * confirm a window that does not contain the new week and skip importing it, while one
	 * that lands a week long only costs the login this was meant to save.
	 */
	private static boolean allConfirmed(Set<Integer> confirmed, int maxWeeksBack) {
		if (maxWeeksBack == Integer.MAX_VALUE) {
			return false;
		}
		int latest = new SokkerDate(System.currentTimeMillis() + MAX_ZONE_LAG).getTrainingWeek();
		for (int week = latest; week > 0 && week > latest - maxWeeksBack; week--) {
			if (!confirmed.contains(Integer.valueOf(week))) {
				return false;
			}
		}
		return true;
	}

	/** the one place SokkerViewer hands the user's credentials to the json api */
	private static ApiDownloader openSession(SokkerViewerSettings settings) throws IOException {
		ApiDownloader api = new ApiDownloader();
		api.setProxySettings(settings.getProxySettings());
		api.login(settings.getUsername(), settings.getPassword());
		return api;
	}

	/**
	 * from the latest completed week backwards. Stops at the first 403 (a non-plus account
	 * asked for a week sokker does not give away), after EMPTY_WEEKS_TO_STOP weeks the team was
	 * not playing in, when cancelled, or after maxWeeksBack weeks. Weeks whose row is already
	 * confirmed are not requested again: sokker's record of a week never changes.
	 */
	Result walk(ApiDownloader api, Set<Integer> confirmed, int maxWeeksBack, ProgressMonitor monitor) throws IOException, SQLException {
		Result result = new Result();
		Set<Integer> squad = new PlayersDao(SQLSession.getConnection()).getPlayerIds();
		double currencyRate = currencyRate();
		TrainingWeek latest = TrainingReportParser.parseWeek(api.getTraining(null));
		int emptyWeeks = 0;
		for (int week = latest.getWeek(); week > 0 && week > latest.getWeek() - maxWeeksBack; week--) {
			if ((monitor != null && monitor.isCanceled()) || emptyWeeks >= EMPTY_WEEKS_TO_STOP) {
				break;
			}
			if (confirmed.contains(Integer.valueOf(week))) {
				emptyWeeks = 0;
				continue;
			}
			TrainingWeek trainingWeek;
			try {
				trainingWeek = week == latest.getWeek() ? latest : TrainingReportParser.parseWeek(api.getTraining(Integer.valueOf(week)));
			} catch (ApiException e) {
				if (e.isForbidden()) {
					Log.info("sokker.org api: week " + week + " is not available to this account, stopping");
					result.forbidden = true;
					break;
				}
				throw e;
			}
			if (monitor != null) {
				monitor.subTask(trainingWeek.getDate().toDateString());
			}
			if (trainingWeek.isReal()) {
				emptyWeeks = 0;
				if (applyWeek(trainingWeek, squad, currencyRate)) {
					result.created++;
				} else {
					result.updated++;
				}
			} else {
				emptyWeeks++;
			}
			if (monitor != null) {
				monitor.worked(1);
			}
		}
		return result;
	}

	/** apply one week's reports in its own transaction; idempotent. True when the row was created */
	boolean applyWeek(TrainingWeek week, Set<Integer> squad, double currencyRate) throws SQLException {
		SQLSession.beginTransaction();
		try {
			boolean result = apply(week, squad, currencyRate);
			SQLSession.commit();
			return result;
		} catch (SQLException e) {
			SQLSession.rollback();
			throw e;
		} finally {
			SQLSession.endTransaction();
		}
	}

	private boolean apply(TrainingWeek week, Set<Integer> squad, double currencyRate) throws SQLException {
		TeamsDao teamsDao = new TeamsDao(SQLSession.getConnection());
		PlayersDao playersDao = new PlayersDao(SQLSession.getConnection());
		Date date = week.getDate();
		List<PlayerTrainingReport> known = new ArrayList<PlayerTrainingReport>();
		for (PlayerTrainingReport report : week.getPresentPlayers()) {
			if (squad.contains(Integer.valueOf(report.getPlayerId()))) {
				known.add(report);
			}
		}
		// a week read before the squad was synced has nowhere to put its reports; leaving it
		// unconfirmed lets a later walk redo it once the players are in the database
		boolean complete = known.size() == week.getPresentPlayers().size();
		Training training = teamsDao.getTrainingForDay(date);
		boolean created = training == null;
		if (created) {
			training = newTraining(week);
			training.setApiConfirmed(complete);
			teamsDao.addTraining(training);
			training.setId(teamsDao.getTrainingId(training));
		} else {
			setPositionTypes(training, week);
			training.setApiConfirmed(complete);
			teamsDao.updateTrainingTypes(training);
		}
		for (PlayerTrainingReport report : known) {
			int position = report.getFormation();
			int slot = slotOf(report);
			boolean passTraining = report.getIntensity() > 0;
			if (playersDao.updateTrainingAssignment(report.getPlayerId(), training.getId(), position, slot, passTraining) == 0) {
				PlayerSkills nearest = playersDao.getNearestPlayerSkills(report.getPlayerId(), week.getMillis());
				playersDao.addPlayerSkills(report.getPlayerId(), buildPlayerSkills(report, nearest, currencyRate), date, training.getId());
			}
		}
		Log.info("sokker.org api: training " + date.toDateString() + (created ? " created, " : " updated, ") + known.size() + " of "
				+ week.getPresentPlayers().size() + " players");
		return created;
	}

	/** the rate the club's country uses, the same one Money displays with; 1 when unknown */
	static double currencyRate() throws SQLException {
		Club club = new TeamsDao(SQLSession.getConnection()).getClub(ConfigurationManager.getInstance().getTeamId());
		for (Country country : new CountriesDao(SQLSession.getConnection()).getCountries()) {
			if (country.getCountryId() == club.getCountry() && country.getCurrencyRate() != null) {
				return country.getCurrencyRate().doubleValue();
			}
		}
		return 1.0;
	}

	static Training newTraining(TrainingWeek week) {
		Training training = new Training();
		training.setDate(week.getDate());
		setPositionTypes(training, week);
		training.setType(training.getEffectiveType());
		training.setFormation(Training.FORMATION_ALL);
		return training;
	}

	/**
	 * a snapshot row for a week the xml sync missed: the report's own skills, its value once
	 * converted, and the rest (wage, season stats, body) copied from the player's nearest
	 * real snapshot
	 */
	static PlayerSkills buildPlayerSkills(PlayerTrainingReport report, PlayerSkills nearest, double currencyRate) {
		PlayerSkills skills = report.getSkills();
		skills.setValue(new Money((int) Money.convertPricesToBase(report.getValue(), currencyRate)));
		skills.setTrainingPosition(report.getFormation());
		skills.setTrainingSlot(slotOf(report));
		skills.setPassTraining(report.getIntensity() > 0);
		if (nearest != null) {
			skills.setSalary(nearest.getSalary());
			skills.setMatches(nearest.getMatches());
			skills.setGoals(nearest.getGoals());
			skills.setAssists(nearest.getAssists());
			skills.setCards(nearest.getCards());
			skills.setWeight(nearest.getWeight());
			skills.setBmi(nearest.getBmi());
		} else {
			skills.setSalary(new Money(0));
		}
		return skills;
	}

	static void setPositionTypes(Training training, TrainingWeek week) {
		training.setTypeGk(week.typeForFormation(Training.FORMATION_GK));
		training.setTypeDef(week.typeForFormation(Training.FORMATION_DEF));
		training.setTypeMid(week.typeForFormation(Training.FORMATION_MID));
		training.setTypeAtt(week.typeForFormation(Training.FORMATION_ATT));
	}

	static int slotOf(PlayerTrainingReport report) {
		return report.getKind() == PlayerTrainingReport.KIND_INDIVIDUAL ? Training.SLOT_ADVANCED : Training.SLOT_FORMATION;
	}
}
