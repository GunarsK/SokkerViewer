package pl.pronux.sokker.downloader.api;

import pl.pronux.sokker.model.PlayerSkills;
import pl.pronux.sokker.model.Training;

/**
 * one player's training report for one week as /api/training returns it. Codes are sokker's:
 * type equals Training.TYPE_*, formation equals Training.FORMATION_*. The skills the report
 * carries go straight onto a PlayerSkills, the row they end up as.
 */
public class PlayerTrainingReport {

	/** kind 0: not run yet, 1: advanced (individual) slot, 2: formation slot, 3: no slot */
	public static final int KIND_INDIVIDUAL = 1;

	private int type = Training.TYPE_UNKNOWN;
	private int kind;
	private int formation = Training.POSITION_NOT_SET;
	private int intensity;

	/** what the player was that week; its value is set once the club's currency rate is known */
	private final PlayerSkills skills = new PlayerSkills();

	/** player value that week, in the user's currency as the site shows it */
	private int value;

	/**
	 * false for a week the player was not in the team yet, and for a week that has not run;
	 * sokker still lists every current player for such weeks, with type 0 and no formation
	 */
	public boolean isPresent() {
		return type != Training.TYPE_UNKNOWN && formation != Training.POSITION_NOT_SET;
	}

	public PlayerSkills getSkills() {
		return skills;
	}

	public int getPlayerId() {
		return skills.getPlayerId();
	}

	public void setPlayerId(int playerId) {
		skills.setPlayerId(playerId);
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

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
}
