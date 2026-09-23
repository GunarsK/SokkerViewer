package pl.pronux.sokker.model;

import java.io.Serializable;

public class PlayerSkills implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7253914177113200851L;

	private int experience;

	private int teamwork;

	private int discipline;

	private int cards;

	private int goals;

	private int matches;

	private int assists;

	private double injurydays;

	private int playerId;

	private int id;

	private Date date;

	private byte age;

	private Money value;

	private Money salary;

	private byte form;

	private byte stamina;

	private byte pace;

	private byte technique;

	private byte passing;

	private byte keeper;

	private byte defender;

	private byte playmaker;

	private byte scorer;

	private int trainingId;
	
	private boolean passTraining = true;

	private Training training;

	private int summarySkill;

	private double weight = 0;
	
	private double bmi = 0.0;

	private int trainingPosition = Training.POSITION_NOT_SET;

	private int trainingSlot = Training.SLOT_NOT_SET;

	/** sokker's training percentage that week, 0 - 100; -1 for weeks only the xml sync saw */
	private int trainingIntensity = -1;

	/** minutes played that week in official, friendly and national matches; -1 when not known */
	private int minutesOfficial = -1;

	private int minutesFriendly = -1;

	private int minutesNational = -1;

	/**
	 * injury days left on the day of that week's training, from sokker.org's report; -1 when not
	 * known. injurydays is the injury on the day the row was synced, the one the players list shows
	 */
	private int trainingInjuryDays = -1;

	public int[] getStatsTable() {
		int[] intTable = {
				value.toInt(),
				salary.toInt(),
				age,
				form,
				stamina,
				pace,
				technique,
				passing,
				keeper,
				defender,
				playmaker,
				scorer,
				discipline,
				experience,
				teamwork
		};
		return intTable;
	}

	public void setSummarySkill() {
		summarySkill = pace + technique + passing + keeper + defender + playmaker + scorer;
	}

	public int getSummarySkill() {
		return summarySkill;
	}


	public byte getAge() {
		return age;
	}

	public void setAge(byte age) {
		this.age = age;
	}

	/**
	 * the age the player had when this row's training ran. A row synced after a season rollover
	 * holds the new season's age but belongs to the old season's last training; players age a
	 * year at every season start, so take off the seasons between the sync and the training.
	 */
	public int getTrainingAge() {
		int seasons = date.getSeason().getSeasonNumber() - date.getTrainingDate(SokkerDate.THURSDAY).getSeason().getSeasonNumber();
		return age - seasons;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public byte getDefender() {
		return defender;
	}

	public void setDefender(byte defender) {
		this.defender = defender;
	}

	public byte getForm() {
		return form;
	}

	public void setForm(byte form) {
		this.form = form;
	}

	public int getPlayerId() {
		return playerId;
	}

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	public byte getKeeper() {
		return keeper;
	}

	public void setKeeper(byte keeper) {
		this.keeper = keeper;
	}

	public byte getPace() {
		return pace;
	}

	public void setPace(byte pace) {
		this.pace = pace;
	}

	public byte getPassing() {
		return passing;
	}

	public void setPassing(byte passing) {
		this.passing = passing;
	}

	public byte getPlaymaker() {
		return playmaker;
	}

	public void setPlaymaker(byte playmaker) {
		this.playmaker = playmaker;
	}

	public byte getStamina() {
		return stamina;
	}

	public void setStamina(byte stamina) {
		this.stamina = stamina;
	}

	public byte getScorer() {
		return scorer;
	}

	public void setScorer(byte scorer) {
		this.scorer = scorer;
	}

	public byte getTechnique() {
		return technique;
	}

	public void setTechnique(byte technique) {
		this.technique = technique;
	}

	public Money getValue() {
		return value;
	}

	public void setValue(Money value) {
		this.value = value;
	}

	public Money getSalary() {
		return salary;
	}

	public void setSalary(Money salary) {
		this.salary = salary;
	}

	public int getAssists() {
		return assists;
	}

	public void setAssists(int assists) {
		this.assists = assists;
	}

	public int getCards() {
		return cards;
	}

	public void setCards(int cards) {
		this.cards = cards;
	}

	public int getGoals() {
		return goals;
	}

	public void setGoals(int goals) {
		this.goals = goals;
	}

	public int getMatches() {
		return matches;
	}

	public void setMatches(int matches) {
		this.matches = matches;
	}

	public double getInjurydays() {
		return injurydays;
	}

	public void setInjurydays(double injurydays) {
		this.injurydays = injurydays;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getDiscipline() {
		return discipline;
	}

	public void setDiscipline(int discipline) {
		this.discipline = discipline;
	}

	public int getExperience() {
		return experience;
	}

	public void setExperience(int experience) {
		this.experience = experience;
	}

	public int getTeamwork() {
		return teamwork;
	}

	public void setTeamwork(int teamwork) {
		this.teamwork = teamwork;
	}

	public Training getTraining() {
		return training;
	}

	public void setTraining(Training training) {
		this.training = training;
	}

	public int getTrainingId() {
		return trainingId;
	}

	public void setTrainingId(int trainingId) {
		this.trainingId = trainingId;
	}

	public boolean isPassTraining() {
		return passTraining;
	}

	public void setPassTraining(boolean passTraining) {
		this.passTraining = passTraining;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public double getBmi() {
		return bmi;
	}

	public void setBmi(double bmi) {
		this.bmi = bmi;
	}

	/**
	 * position this player trained as in that week (Training.FORMATION_GK .. FORMATION_ATT),
	 * or Training.POSITION_NOT_SET for weeks recorded before sokker sent it
	 */
	public int getTrainingPosition() {
		return trainingPosition;
	}

	public void setTrainingPosition(int trainingPosition) {
		this.trainingPosition = trainingPosition;
	}

	/**
	 * Training.SLOT_ADVANCED when the player held one of the advanced training slots that
	 * week, SLOT_FORMATION when he trained with the formation, SLOT_NOT_SET when not known
	 */
	public int getTrainingSlot() {
		return trainingSlot;
	}

	public void setTrainingSlot(int trainingSlot) {
		this.trainingSlot = trainingSlot;
	}

	public int getTrainingIntensity() {
		return trainingIntensity;
	}

	public void setTrainingIntensity(int trainingIntensity) {
		this.trainingIntensity = trainingIntensity;
	}

	public int getMinutesOfficial() {
		return minutesOfficial;
	}

	public void setMinutesOfficial(int minutesOfficial) {
		this.minutesOfficial = minutesOfficial;
	}

	public int getMinutesFriendly() {
		return minutesFriendly;
	}

	public void setMinutesFriendly(int minutesFriendly) {
		this.minutesFriendly = minutesFriendly;
	}

	public int getMinutesNational() {
		return minutesNational;
	}

	public void setMinutesNational(int minutesNational) {
		this.minutesNational = minutesNational;
	}

	public int getTrainingInjuryDays() {
		return trainingInjuryDays;
	}

	public void setTrainingInjuryDays(int trainingInjuryDays) {
		this.trainingInjuryDays = trainingInjuryDays;
	}

	public boolean isInTrainingSlot() {
		return trainingSlot == Training.SLOT_ADVANCED;
	}

}
