package pl.pronux.sokker.actions;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import pl.pronux.sokker.data.sql.SQLQuery;
import pl.pronux.sokker.data.sql.SQLSession;
import pl.pronux.sokker.data.sql.dao.CountriesDao;
import pl.pronux.sokker.data.sql.dao.JuniorsDao;
import pl.pronux.sokker.data.sql.dao.PlayersDao;
import pl.pronux.sokker.data.sql.dao.TeamsDao;
import pl.pronux.sokker.downloader.api.ApiDownloader;
import pl.pronux.sokker.downloader.api.ApiException;
import pl.pronux.sokker.downloader.api.JuniorGraphParser;
import pl.pronux.sokker.downloader.api.PlayerTrainingReport;
import pl.pronux.sokker.downloader.api.TrainingReportParser;
import pl.pronux.sokker.downloader.api.TrainingWeek;
import pl.pronux.sokker.interfaces.DateConst;
import pl.pronux.sokker.interfaces.ProgressMonitor;
import pl.pronux.sokker.model.Club;
import pl.pronux.sokker.model.Country;
import pl.pronux.sokker.model.Date;
import pl.pronux.sokker.model.Junior;
import pl.pronux.sokker.model.Money;
import pl.pronux.sokker.model.PlayerSkills;
import pl.pronux.sokker.model.SokkerDate;
import pl.pronux.sokker.model.SokkerViewerSettings;
import pl.pronux.sokker.model.Training;
import pl.pronux.sokker.utils.Log;

/** writes sokker.org's json api training weeks into the database */
public final class TrainingApiManager {

	/** confirmed latest weeks that let a sync skip the walk */
	private static final int RECENT_WEEKS = 5;

	/** empty weeks in a row that end the history */
	private static final int EMPTY_WEEKS_TO_STOP = 3;

	/** a western machine's largest lag behind sokker's week change */
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

		private int juniorWeeks;

		public int getCreated() {
			return created;
		}

		public int getUpdated() {
			return updated;
		}

		/** sokker refused an older week to an account without plus */
		public boolean isForbidden() {
			return forbidden;
		}

		/** junior rows added from their graphs */
		public int getJuniorWeeks() {
			return juniorWeeks;
		}

		public boolean hasChanges() {
			return created + updated + juniorWeeks > 0;
		}
	}

	/** after a sync: every unconfirmed week sokker still answers for */
	public Result synchronizeWeeks(SokkerViewerSettings settings, ProgressMonitor monitor) throws IOException, SQLException {
		return synchronize(settings, false, monitor);
	}

	/** the menu action: the walk, always, and every junior again */
	public Result importHistory(SokkerViewerSettings settings, ProgressMonitor monitor) throws IOException, SQLException {
		return synchronize(settings, true, monitor);
	}

	/** each junior's graph weeks the database has no row for */
	private int importJuniorHistory(ApiDownloader api, List<Junior> juniors, ProgressMonitor monitor) throws IOException, SQLException {
		JuniorsDao juniorsDao = new JuniorsDao(SQLSession.getConnection());
		int added = 0;
		for (Junior junior : juniors) {
			if (monitor != null && monitor.isCanceled()) {
				break;
			}
			if (monitor != null) {
				monitor.subTask(junior.getName() + " " + junior.getSurname());
			}
			SortedMap<Integer, Integer> levels = null;
			try {
				levels = JuniorGraphParser.parse(api.getJuniorGraph(junior.getId()));
			} catch (ApiException e) {
				if (e.isNotLoggedIn()) {
					throw e;
				}
				Log.info("sokker.org api: no graph for junior " + junior.getId() + ": " + e.getMessage());
				if (!e.isForbidden()) {
					continue;
				}
			} catch (IOException e) {
				Log.info("sokker.org api: no graph for junior " + junior.getId() + ": " + e.getMessage());
				continue;
			}
			SQLSession.beginTransaction();
			try {
				if (levels != null) {
					added += JuniorsManager.getInstance().addJuniorHistory(junior, levels);
				}
				juniorsDao.setApiHistory(junior.getId());
				SQLSession.commit();
			} catch (SQLException e) {
				SQLSession.rollback();
				throw e;
			} finally {
				SQLSession.endTransaction();
			}
		}
		Log.info("sokker.org api: junior history, " + added + " weeks added");
		return added;
	}

	private Result synchronize(SokkerViewerSettings settings, boolean menuImport, ProgressMonitor monitor) throws IOException, SQLException {
		boolean newConnection = SQLQuery.connect();
		try {
			Set<Integer> confirmed = new TeamsDao(SQLSession.getConnection()).getApiConfirmedWeeks();
			// the menu asks every junior, a sync only unasked ones
			JuniorsDao juniorsDao = new JuniorsDao(SQLSession.getConnection());
			List<Junior> juniors = menuImport ? juniorsDao.getJuniors(Junior.STATUS_IN_SCHOOL) : juniorsDao.getJuniorsWithoutApiHistory();
			boolean weeksConfirmed = !menuImport && recentConfirmed(confirmed);
			if (weeksConfirmed && juniors.isEmpty()) {
				return new Result();
			}
			ApiDownloader api = openSession(settings);
			Result result = weeksConfirmed ? new Result() : walk(api, confirmed, monitor);
			if (result.created > 0) {
				// created weeks lack junior rows
				juniors = juniorsDao.getJuniors(Junior.STATUS_IN_SCHOOL);
			}
			result.juniorWeeks = importJuniorHistory(api, juniors, monitor);
			return result;
		} finally {
			SQLQuery.close(newConnection);
		}
	}

	/** true when the latest RECENT_WEEKS weeks are confirmed */
	private static boolean recentConfirmed(Set<Integer> confirmed) {
		int latest = new SokkerDate(System.currentTimeMillis() + MAX_ZONE_LAG).getTrainingWeek();
		for (int week = latest; week > 0 && week > latest - RECENT_WEEKS; week--) {
			if (!confirmed.contains(Integer.valueOf(week))) {
				return false;
			}
		}
		return true;
	}

	/** logs in to the json api with the user's credentials */
	private static ApiDownloader openSession(SokkerViewerSettings settings) throws IOException {
		ApiDownloader api = new ApiDownloader();
		api.setProxySettings(settings.getProxySettings());
		api.login(settings.getUsername(), settings.getPassword());
		return api;
	}

	/** unconfirmed weeks backwards until a 403 or EMPTY_WEEKS_TO_STOP */
	Result walk(ApiDownloader api, Set<Integer> confirmed, ProgressMonitor monitor) throws IOException, SQLException {
		Result result = new Result();
		Set<Integer> squad = new PlayersDao(SQLSession.getConnection()).getPlayerIds();
		double currencyRate = currencyRate();
		TrainingWeek latest = TrainingReportParser.parseWeek(api.getTraining(null));
		int emptyWeeks = 0;
		for (int week = latest.getWeek(); week > 0; week--) {
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

	/** one week's reports in its own transaction; true when created */
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
		// a week with unknown players stays unconfirmed
		boolean complete = known.size() == week.getPresentPlayers().size();
		Training training = teamsDao.getTrainingForDay(date);
		boolean created = training == null;
		if (created) {
			training = newTraining(date, positionTypes(week));
			training.setApiConfirmed(complete);
			teamsDao.addTraining(training);
			training.setId(teamsDao.getTrainingId(training));
		} else {
			setPositionTypes(training, positionTypes(week));
			training.setApiConfirmed(complete);
			teamsDao.updateTrainingTypes(training);
		}
		for (PlayerTrainingReport report : known) {
			if (playersDao.updateTrainingAssignment(assignTraining(report), training.getId()) == 0) {
				PlayerSkills nearest = playersDao.getNearestPlayerSkills(report.getPlayerId(), week.getMillis());
				playersDao.addPlayerSkills(report.getPlayerId(), buildPlayerSkills(report, nearest, currencyRate), date, training.getId());
			}
			addStartingRow(playersDao, week, report);
		}
		Log.info("sokker.org api: training " + date.toDateString() + (created ? " created, " : " updated, ") + known.size() + " of "
				+ week.getPresentPlayers().size() + " players");
		return created;
	}

	/** the club country's currency rate; 1 when unknown */
	static double currencyRate() throws SQLException {
		Club club = new TeamsDao(SQLSession.getConnection()).getClub(ConfigurationManager.getInstance().getTeamId());
		for (Country country : new CountriesDao(SQLSession.getConnection()).getCountries()) {
			if (country.getCountryId() == club.getCountry() && country.getCurrencyRate() != null) {
				return country.getCurrencyRate().doubleValue();
			}
		}
		return 1.0;
	}

	static Training newTraining(Date date, int[] types) {
		Training training = new Training();
		training.setDate(date);
		setPositionTypes(training, types);
		training.setType(training.getEffectiveType());
		training.setFormation(Training.FORMATION_ALL);
		return training;
	}

	/** the report's row with its training assignment set */
	static PlayerSkills assignTraining(PlayerTrainingReport report) {
		PlayerSkills skills = report.getSkills();
		skills.setTrainingPosition(report.getFormation());
		skills.setTrainingSlot(slotOf(report));
		skills.setPassTraining(skills.getTrainingIntensity() > 0);
		return skills;
	}

	/** a row for a week the xml sync missed */
	static PlayerSkills buildPlayerSkills(PlayerTrainingReport report, PlayerSkills nearest, double currencyRate) {
		PlayerSkills skills = report.getSkills();
		skills.setValue(new Money((int) Money.convertPricesToBase(report.getValue(), currencyRate)));
		copyUnreported(nearest, skills);
		return skills;
	}

	/** copies wage, season stats, body; wage 0 without a row */
	static void copyUnreported(PlayerSkills from, PlayerSkills to) {
		if (from == null) {
			to.setSalary(new Money(0));
			return;
		}
		to.setSalary(from.getSalary());
		to.setMatches(from.getMatches());
		to.setGoals(from.getGoals());
		to.setAssists(from.getAssists());
		to.setCards(from.getCards());
		to.setWeight(from.getWeight());
		to.setBmi(from.getBmi());
	}

	/** made-up row for the week before a player's first training */
	private static void addStartingRow(PlayersDao playersDao, TrainingWeek week, PlayerTrainingReport report) throws SQLException {
		int playerId = report.getPlayerId();
		PlayerSkills before = report.getSkillsBefore();
		if (before == null || playersDao.hasPlayerSkillsBefore(playerId, week.getWeek())) {
			return;
		}
		int previousWeek = week.getWeek() - 1;
		Date date = new Date(SokkerDate.weekToMillis(previousWeek, SokkerDate.THURSDAY));
		date.setSokkerDate(new SokkerDate(SokkerDate.THURSDAY, previousWeek));
		PlayerSkills after = playersDao.getNearestPlayerSkills(playerId, date.getMillis());
		int seasons = SokkerDate.seasonOf(week.getWeek()) - SokkerDate.seasonOf(previousWeek);
		before.setAge((byte) (report.getSkills().getAge() - seasons));
		before.setValue(after.getValue());
		copyUnreported(after, before);
		playersDao.addMadeUpPlayerSkills(playerId, before, date);
	}

	/** types per formation; null leaves them not set */
	static void setPositionTypes(Training training, int[] types) {
		if (types != null) {
			training.setTypeGk(types[Training.FORMATION_GK]);
			training.setTypeDef(types[Training.FORMATION_DEF]);
			training.setTypeMid(types[Training.FORMATION_MID]);
			training.setTypeAtt(types[Training.FORMATION_ATT]);
		}
	}

	/** the week's type per formation */
	static int[] positionTypes(TrainingWeek week) {
		return new int[] { week.typeForFormation(Training.FORMATION_GK), week.typeForFormation(Training.FORMATION_DEF),
				week.typeForFormation(Training.FORMATION_MID), week.typeForFormation(Training.FORMATION_ATT) };
	}

	static int slotOf(PlayerTrainingReport report) {
		switch (report.getKind()) {
		case PlayerTrainingReport.KIND_INDIVIDUAL:
			return Training.SLOT_ADVANCED;
		case PlayerTrainingReport.KIND_FORMATION:
			return Training.SLOT_FORMATION;
		default:
			return Training.SLOT_MISSING;
		}
	}
}
