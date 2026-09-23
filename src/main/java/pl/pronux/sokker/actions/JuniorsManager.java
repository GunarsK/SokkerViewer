package pl.pronux.sokker.actions;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import pl.pronux.sokker.data.sql.SQLSession;
import pl.pronux.sokker.data.sql.dao.JuniorsDao;
import pl.pronux.sokker.data.sql.dao.TeamsDao;
import pl.pronux.sokker.model.Date;
import pl.pronux.sokker.model.Junior;
import pl.pronux.sokker.model.JuniorSkills;
import pl.pronux.sokker.model.SokkerDate;
import pl.pronux.sokker.model.Training;

public final class JuniorsManager {

	private static JuniorsManager instance = new JuniorsManager();

	private JuniorsManager() {
	}

	public static JuniorsManager getInstance() {
		return instance;
	}

	public void addJuniors(List<Junior> juniors, Training training, int clubId) throws SQLException {
		JuniorsDao juniorsDao = new JuniorsDao(SQLSession.getConnection());
		for (Junior junior : juniors) {
			if (!juniorsDao.existsJunior(junior.getId())) {
				juniorsDao.addJunior(junior);
				juniorsDao.addJuniorSkills(junior.getId(), junior.getSkills()[0], training);
			} else {
				if (juniorsDao.existsJuniorHistory(junior.getId())) {
					juniorsDao.moveJunior(junior.getId(), Junior.STATUS_IN_SCHOOL, clubId);
				}
				if ((training.getStatus() & Training.NEW_TRAINING) != 0) {
					juniorsDao.addJuniorSkills(junior.getId(), junior.getSkills()[0], training);
				} else {
					juniorsDao.updateJuniorSkills(junior.getId(), junior.getSkills()[0], training.getDate());
				}
				juniorsDao.updateJunior(junior);
			}
		}

		String sTemp = "("; 

		for (int i = 0; i < juniors.size(); i++) {
			// warunek dla ostatniego stringa zeby nie dodawac na koncu ','
			if (i == juniors.size() - 1) {
				sTemp += juniors.get(i).getId();
				break;
			}
			sTemp += juniors.get(i).getId() + ","; 
		}
		sTemp += ")"; 

		if (juniors.isEmpty()) {
			juniorsDao.moveTrainedJuniors(clubId);
			juniorsDao.removeTrainedJuniors(clubId);
		} else {
			juniorsDao.moveTrainedJuniors(sTemp, clubId);
			juniorsDao.removeTrainedJuniors(sTemp, clubId);
		}
	}

	public void importJuniors(List<Junior> juniors, Training training, int clubId) throws SQLException {
		JuniorsDao juniorsDao = new JuniorsDao(SQLSession.getConnection());

		for (Junior junior : juniors) {

			if (!juniorsDao.existsJunior(junior.getId())) {
				juniorsDao.addJunior(junior);
				juniorsDao.addJuniorSkills(junior.getId(), junior.getSkills()[0], training);
			} else {
				if (juniorsDao.existsJuniorHistory(junior.getId())) {
					juniorsDao.moveJunior(junior.getId(), Junior.STATUS_IN_SCHOOL, clubId);
				}
				if ((training.getStatus() & Training.NEW_TRAINING) != 0 || juniorsDao.getJuniorSkills(junior, training) == null) {
					juniorsDao.addJuniorSkills(junior.getId(), junior.getSkills()[0], training);
				}
			}
		}
	}

	/**
	 * rows for the training weeks in levels older than the junior's newest row that have no row
	 * yet; existing rows are never touched. Returns the number of rows added. Rows are matched by
	 * training week: the xml sync files the latest training under the day it ran, often early
	 * the next week.
	 */
	public int addJuniorHistory(Junior junior, SortedMap<Integer, Integer> levels) throws SQLException {
		JuniorsDao juniorsDao = new JuniorsDao(SQLSession.getConnection());
		TeamsDao teamsDao = new TeamsDao(SQLSession.getConnection());
		JuniorSkills[] rows = juniorsDao.getJuniorsSkills(junior, new HashMap<Integer, Training>());
		if (rows.length == 0) {
			return 0;
		}
		Set<Integer> known = new HashSet<Integer>();
		for (JuniorSkills row : rows) {
			known.add(Integer.valueOf(row.getDate().getSokkerDate().getTrainingWeek()));
		}
		JuniorSkills newest = rows[rows.length - 1];
		int newestWeek = newest.getDate().getSokkerDate().getTrainingWeek();
		int added = 0;
		for (Map.Entry<Integer, Integer> level : levels.entrySet()) {
			int week = level.getKey().intValue();
			if (week >= newestWeek || known.contains(Integer.valueOf(week))) {
				continue;
			}
			Date date = new Date(SokkerDate.weekToMillis(week, SokkerDate.THURSDAY));
			date.setSokkerDate(new SokkerDate(SokkerDate.THURSDAY, week));
			JuniorSkills skills = new JuniorSkills();
			skills.setSkill(level.getValue().intValue());
			skills.setWeeks(newest.getWeeks() + newestWeek - week);
			// the newest row's age is the one sokker showed on the day that row was synced
			skills.setAge(newest.getAge() == 0 ? 0 : getJuniorAge(newest.getDate(), date, newest.getAge()));
			Training training = teamsDao.getTrainingForDay(date);
			if (training == null) {
				// a row always has its training, the way repairDatabase keeps it: the xml import
				// and the training reports find a week's junior rows through it
				training = new Training();
				training.setDate(date);
				training.setType(Training.TYPE_UNKNOWN);
				training.setFormation(Training.FORMATION_ALL);
				teamsDao.addTraining(training);
				training.setId(teamsDao.getTrainingId(training));
			}
			juniorsDao.addJuniorSkills(junior.getId(), skills, training);
			added++;
		}
		return added;
	}

	public void completeJuniorsAge(Date currentDay) throws SQLException {
		JuniorsDao juniorsDao = new JuniorsDao(SQLSession.getConnection());
		List<Junior> juniors = juniorsDao.getJuniors(Junior.STATUS_IN_SCHOOL);
		for (Junior junior : juniors) {
			JuniorSkills[] juniorSkills = juniorsDao.getJuniorsSkills(junior, new HashMap<Integer, Training>());
			if (juniorSkills[0].getAge() == 0 && juniorSkills[juniorSkills.length - 1].getAge() != 0) {
				int age = juniorSkills[juniorSkills.length - 1].getAge();
				for (JuniorSkills skills : juniorSkills) {
					if (skills.getAge() == 0) {
						skills.setAge(getJuniorAge(currentDay, skills.getDate(), age));
						juniorsDao.updateJuniorSkills(skills);
					}
				}
			}
		}
	}

	private int getJuniorAge(Date currentDay, Date oldDate, int age) {
		int previousSeason = oldDate.getSokkerDate().getSeason();
		if (oldDate.getSokkerDate().isLastSeasonWeek() && oldDate.getSokkerDate().getDay() == SokkerDate.FRIDAY) {
			previousSeason += 1;
		}
		int currentSeason = currentDay.getSokkerDate().getSeason();
		return age - (currentSeason - previousSeason);
	}
}