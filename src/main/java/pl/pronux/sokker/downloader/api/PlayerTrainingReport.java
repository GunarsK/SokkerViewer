package pl.pronux.sokker.downloader.api;

import pl.pronux.sokker.model.Training;

/**
 * one player's training report for one week as /api/training returns it. Codes are sokker's:
 * type equals Training.TYPE_*, formation equals Training.FORMATION_*
 */
public class PlayerTrainingReport {

	/** kind 0: not run yet, 1: advanced (individual) slot, 2: formation slot, 3: no slot */
	public static final int KIND_INDIVIDUAL = 1;

	private int playerId;
	private int type = Training.TYPE_UNKNOWN;
	private int kind;
	private int formation = Training.POSITION_NOT_SET;
	private int intensity;
	private int age;
	private int value;
	private int injuryDays;
	private int form;
	private int discipline;
	private int teamwork;
	private int experience;
	private int stamina;
	private int keeper;
	private int playmaking;
	private int passing;
	private int technique;
	private int defending;
	private int striker;
	private int pace;

	/**
	 * false for a week the player was not in the team yet, and for a week that has not run;
	 * sokker still lists every current player for such weeks, with type 0 and no formation
	 */
	public boolean isPresent() {
		return type != Training.TYPE_UNKNOWN && formation != Training.POSITION_NOT_SET;
	}

	public int getPlayerId() {
		return playerId;
	}

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getKind() {
		return kind;
	}

	public void setKind(int kind) {
		this.kind = kind;
	}

	public int getFormation() {
		return formation;
	}

	public void setFormation(int formation) {
		this.formation = formation;
	}

	public int getIntensity() {
		return intensity;
	}

	public void setIntensity(int intensity) {
		this.intensity = intensity;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	/** player value that week, in the user's currency as the site shows it */
	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getInjuryDays() {
		return injuryDays;
	}

	public void setInjuryDays(int injuryDays) {
		this.injuryDays = injuryDays;
	}

	public int getForm() {
		return form;
	}

	public void setForm(int form) {
		this.form = form;
	}

	public int getDiscipline() {
		return discipline;
	}

	public void setDiscipline(int discipline) {
		this.discipline = discipline;
	}

	public int getTeamwork() {
		return teamwork;
	}

	public void setTeamwork(int teamwork) {
		this.teamwork = teamwork;
	}

	public int getExperience() {
		return experience;
	}

	public void setExperience(int experience) {
		this.experience = experience;
	}

	public int getStamina() {
		return stamina;
	}

	public void setStamina(int stamina) {
		this.stamina = stamina;
	}

	public int getKeeper() {
		return keeper;
	}

	public void setKeeper(int keeper) {
		this.keeper = keeper;
	}

	public int getPlaymaking() {
		return playmaking;
	}

	public void setPlaymaking(int playmaking) {
		this.playmaking = playmaking;
	}

	public int getPassing() {
		return passing;
	}

	public void setPassing(int passing) {
		this.passing = passing;
	}

	public int getTechnique() {
		return technique;
	}

	public void setTechnique(int technique) {
		this.technique = technique;
	}

	public int getDefending() {
		return defending;
	}

	public void setDefending(int defending) {
		this.defending = defending;
	}

	public int getStriker() {
		return striker;
	}

	public void setStriker(int striker) {
		this.striker = striker;
	}

	public int getPace() {
		return pace;
	}

	public void setPace(int pace) {
		this.pace = pace;
	}
}
