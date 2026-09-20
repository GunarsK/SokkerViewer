package pl.pronux.sokker.comparators;

import java.text.Collator;
import java.util.Locale;

import pl.pronux.sokker.interfaces.Sort;
import pl.pronux.sokker.interfaces.SVComparator;
import pl.pronux.sokker.model.PlayerStats;

public class MatchPlayersDetailsComparator implements SVComparator<PlayerStats>, Sort {
	private int column;

	private int direction;

	public static final int SUBSTITUTIONS = 0;

	public static final int NUMBER = 1;

	public static final int PLAYER = 2;

	public static final int FORMATION = 3;

	public static final int TIME = 4;

	public static final int STARS = 5;

	public static final int GOALS = 6;

	public static final int SHOOTS = 7;

	public static final int ASSISTS = 8;
	
	public static final int FOULS = 9;

	public static final int INJURY = 10;

	public static final int CARDS = 11;
	
	public MatchPlayersDetailsComparator() {
	}

	public MatchPlayersDetailsComparator(int column, int direction) {
		this.column = column;
		this.direction = direction;
	}

	public int compare(PlayerStats ps1, PlayerStats ps2) {
		int rc = 0;
		Locale loc = Locale.getDefault();
		Collator coll = Collator.getInstance(loc);

		switch (column) {
		case SUBSTITUTIONS:
			break;
		case NUMBER:
			if(ps1.getNumber() < ps2.getNumber()) {
				rc = -1;
			} else if(ps1.getNumber() > ps2.getNumber()) {
				rc = 1;
			} else {
				rc = Compare.values(ps1.getTimeIn(), ps2.getTimeIn());
			}
			break;
		case PLAYER:
			if(ps1.getPlayer() == null && ps2.getPlayer() == null) {
				rc = Compare.values(ps1.getPlayerId(), ps2.getPlayerId());
			} else if(ps1.getPlayer() != null && ps2.getPlayer() == null) {
				rc = 1;
			} else if(ps1.getPlayer() == null && ps2.getPlayer() != null) {
				rc = -1;
			} else if(ps1.getPlayer() != null && ps2.getPlayer() != null) {
				rc = coll.compare(ps1.getPlayer().getSurname(), ps2.getPlayer().getSurname());
			}
			break;
		case FORMATION:
			rc = Compare.values(ps1.getFormation(), ps2.getFormation());
			break;
		case STARS:
			rc = Compare.values(ps1.getRating(), ps2.getRating());
			break;
		case GOALS:
			rc = Compare.values(ps1.getGoals(), ps2.getGoals());
			break;
		case SHOOTS:
			rc = Compare.values(ps1.getShoots(), ps2.getShoots());
			break;
		case ASSISTS:
			rc = Compare.values(ps1.getAssists(), ps2.getAssists());
			break;
		case FOULS:
			rc = Compare.values(ps1.getFouls(), ps2.getFouls());
			break;
		case INJURY:
			rc = Compare.values(ps1.getIsInjured(), ps2.getIsInjured());
			break;
		case TIME:
			rc = Compare.values(ps1.getTimePlayed(), ps2.getTimePlayed());
			break;
		case CARDS:
			if(ps1.getRedCards() < ps2.getRedCards()) {
				rc = -1;
			} else if(ps1.getRedCards() > ps2.getRedCards()) {
				rc = 1;
			} else {
				if(ps1.getYellowCards() < ps2.getYellowCards()) {
					rc = -1;
				} else {
					rc = 1;
				}
			}
		default:
				break;
		}
		// Check the direction for sort and flip the sign
		// if appropriate
		if (direction == DESCENDING) {
			rc = -rc;
		}
		return rc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pl.pronux.sokker.ExtendedComparator#getColumn()
	 */
	public int getColumn() {
		return column;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pl.pronux.sokker.ExtendedComparator#getDirection()
	 */
	public int getDirection() {
		return direction;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pl.pronux.sokker.ExtendedComparator#reverseDirection()
	 */
	public void reverseDirection() {
		direction = 1 - direction;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pl.pronux.sokker.ExtendedComparator#setColumn(int)
	 */
	public void setColumn(int column) {
		this.column = column;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pl.pronux.sokker.ExtendedComparator#setDirection(int)
	 */
	public void setDirection(int direction) {
		this.direction = direction;
	}

}
