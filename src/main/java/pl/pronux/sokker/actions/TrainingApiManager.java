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
import pl.pronux.sokker.interfaces.ProgressMonitor;
import pl.pronux.sokker.model.Club;
import pl.pronux.sokker.model.Country;
import pl.pronux.sokker.model.Date;
import pl.pronux.sokker.model.Money;
import pl.pronux.sokker.model.PlayerSkills;
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
		boolean newConnection = SQLQuery.connect();
		try {
			return walk(openSession(settings), RECENT_WEEKS, null);
		} finally {
			SQLQuery.close(newConnection);
		}
	}

	/** the menu action: every week sokker still answers for, newest first */
	public Result importHistory(SokkerViewerSettings settings, ProgressMonitor monitor) throws IOException, SQLException {
		boolean newConnection = SQLQuery.connect();
		try {
			return walk(openSession(settings), Integer.MAX_VALUE, monitor);
		} finally {
			SQLQuery.close(newConnection);
		}
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
	Result walk(ApiDownloader api, int maxWeeksBack, ProgressMonitor monitor) throws IOException, SQLException {
		Result result = new Result();
		Set<Integer> confirmed = new TeamsDao(SQLSession.getConnection()).getApiConfirmedWeeks();
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
				if (applyWeek(trainingWeek, currencyRate)) {
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
	boolean applyWeek(TrainingWeek week, double currencyRate) throws SQLException {
		SQLSession.beginTransaction();
		try {
			boolean result = apply(week, currencyRate);
			SQLSession.commit();
			return result;
		} catch (SQLException e) {
			SQLSession.rollback();
			throw e;
		} finally {
			SQLSession.endTransaction();
		}
	}

	private boolean apply(TrainingWeek week, double currencyRate) throws SQLException {
		TeamsDao teamsDao = new TeamsDao(SQLSession.getConnection());
		PlayersDao playersDao = new PlayersDao(SQLSession.getConnection());
		Date date = week.getDate();
		List<PlayerTrainingReport> known = new ArrayList<PlayerTrainingReport>();
		for (PlayerTrainingReport report : week.getPresentPlayers()) {
			if (playersDao.existsPlayer(report.getPlayerId())) {
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
		training.setType(commonType(training));
		training.setFormation(Training.FORMATION_ALL);
		return training;
	}

	/**
	 * a snapshot row for a week the xml sync missed: everything the report carries comes from
	 * it, the rest (wage, season stats, body) is copied from the player's nearest real snapshot
	 */
	static PlayerSkills buildPlayerSkills(PlayerTrainingReport report, PlayerSkills nearest, double currencyRate) {
		PlayerSkills skills = new PlayerSkills();
		skills.setAge((byte) report.getAge());
		skills.setValue(new Money((int) Money.convertPricesToBase(report.getValue(), currencyRate)));
		skills.setInjurydays(report.getInjuryDays());
		skills.setForm((byte) report.getForm());
		skills.setDiscipline(report.getDiscipline());
		skills.setTeamwork(report.getTeamwork());
		skills.setExperience(report.getExperience());
		skills.setStamina((byte) report.getStamina());
		skills.setKeeper((byte) report.getKeeper());
		skills.setPlaymaker((byte) report.getPlaymaking());
		skills.setPassing((byte) report.getPassing());
		skills.setTechnique((byte) report.getTechnique());
		skills.setDefender((byte) report.getDefending());
		skills.setScorer((byte) report.getStriker());
		skills.setPace((byte) report.getPace());
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

	/** the single type when every trained position had the same one, else TYPE_UNKNOWN */
	static int commonType(Training training) {
		int common = Training.TYPE_UNKNOWN;
		for (int position = Training.FORMATION_GK; position <= Training.FORMATION_ATT; position++) {
			int type = training.getTypeForPosition(position);
			if (type == Training.TYPE_NOT_SET) {
				continue;
			}
			if (common == Training.TYPE_UNKNOWN) {
				common = type;
			} else if (common != type) {
				return Training.TYPE_UNKNOWN;
			}
		}
		return common;
	}

	static int slotOf(PlayerTrainingReport report) {
		return report.getKind() == PlayerTrainingReport.KIND_INDIVIDUAL ? Training.SLOT_ADVANCED : Training.SLOT_FORMATION;
	}
}
