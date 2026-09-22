package pl.pronux.sokker.downloader.api;

import java.util.ArrayList;
import java.util.List;

import pl.pronux.sokker.model.Date;
import pl.pronux.sokker.model.SokkerDate;
import pl.pronux.sokker.model.Training;

/** the training reports of every current player for one sokker week */
public class TrainingWeek {

	private final int week;

	private final int day;

	private final long millis;

	private final List<PlayerTrainingReport> players = new ArrayList<PlayerTrainingReport>();

	public TrainingWeek(int week, int day, long millis) {
		this.week = week;
		this.day = day;
		this.millis = millis;
	}

	/** only players that trained: sokker sends a report for every player, absent ones with no position */
	public void add(PlayerTrainingReport report) {
		if (report.isPresent()) {
			players.add(report);
		}
	}

	public int getWeek() {
		return week;
	}

	public long getMillis() {
		return millis;
	}

	/** the training's date with the week and day sokker sent, the way TrainingDto keeps db rows */
	public Date getDate() {
		Date date = new Date(millis);
		date.setSokkerDate(new SokkerDate(day, week));
		return date;
	}

	/** a week the team really trained in; false for the current unrun week and pre-team weeks */
	public boolean isReal() {
		return !players.isEmpty();
	}

	public List<PlayerTrainingReport> getPresentPlayers() {
		return players;
	}

	/** the type every player of that formation trained, TYPE_NOT_SET when nobody trained there */
	public int typeForFormation(int formation) {
		for (PlayerTrainingReport report : players) {
			if (report.getFormation() == formation) {
				return report.getType();
			}
		}
		return Training.TYPE_NOT_SET;
	}
}
