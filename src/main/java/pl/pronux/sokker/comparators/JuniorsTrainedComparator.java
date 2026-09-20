package pl.pronux.sokker.comparators;

import java.text.Collator;
import java.util.Locale;

import pl.pronux.sokker.interfaces.Sort;
import pl.pronux.sokker.interfaces.SVComparator;
import pl.pronux.sokker.model.Junior;
import pl.pronux.sokker.model.Player;

public class JuniorsTrainedComparator implements SVComparator<Junior>, Sort {
//	public static final int ID = 0;

	public static final int NAME = 0;

	public static final int SURNAME = 1;

	public static final int FORMATION = 2;

	public static final int SKILL = 3;
	
	public static final int SUM = 4;
	
	public static final int STAMINA = 5;

	public static final int SUM_WITHOUT_STAMINA = 6;
	
	public static final int PACE = 7;
	
	public static final int TECHNIQUE = 8;
	
	public static final int PASSING = 9;
	
	public static final int KEEPER = 10;
	
	public static final int DEFENDER = 11;
	
	public static final int PLAYMAKER = 12;
	
	public static final int SCORER = 13;
	
	public static final int AGE = 14;
	
	public static final int WEEKS = 15;
	
	public static final int AVERAGE_JUMP = 16;
	
	public static final int COSTS = 17;
	
	private int column;

	private int direction;

	/**
	 * Compares two Player objects
	 *
	 * @param obj1
	 *            the first Player
	 * @param obj2
	 *            the second Player
	 * @return int
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Junior j1, Junior j2) {
		int rc = 0;
		Locale loc = Locale.getDefault();
		Collator coll = Collator.getInstance(loc);
		Player p1 = j1.getPlayer();
		Player p2 = j2.getPlayer();
		// Determine which field to sort on, then sort
		// on that field
		switch (column) {
//			case ID:
//				rc = (c1.getId() < c2.getId()) ? -1 : 1;
//				break;
			case NAME:
				rc = coll.compare(j1.getName(),j2.getName());
				break;
			case SURNAME:
				rc = coll.compare(j1.getSurname(),j2.getSurname());
				break;
			case FORMATION:
				rc = Compare.values(j1.getFormation(), j2.getFormation());
				break;
			case SKILL:
				rc = Compare.values(j1.getSkills()[j1.getSkills().length-1].getSkill(), j2.getSkills()[j2.getSkills().length-1].getSkill());
				break;
			case WEEKS:
				rc = Compare.values(j1.getSkills()[0].getWeeks(), j2.getSkills()[0].getWeeks());
				break;
			case COSTS:
				rc = Compare.values(j1.getAllMoneyToSpend().toInt(), j2.getAllMoneyToSpend().toInt());
				break;
			case AGE:
				if(p1 != null && p2 != null) {
					rc = Compare.values(p1.getSkills()[0].getAge(), p2.getSkills()[0].getAge());
				} else {
					rc = comparePlayers(p1, p2);
				}
				break;
			case STAMINA:
				if(p1 != null && p2 != null) {
					rc = Compare.values(p1.getSkills()[0].getStamina(), p2.getSkills()[0].getStamina());
				} else {
					rc = comparePlayers(p1, p2);
				}
				break;
			case PACE:
				if(p1 != null && p2 != null) {
					rc = Compare.values(p1.getSkills()[0].getPace(), p2.getSkills()[0].getPace());
				} else {
					rc = comparePlayers(p1, p2);
				}
				break;
			case TECHNIQUE:
				if(p1 != null && p2 != null) {
					rc = Compare.values(p1.getSkills()[0].getTechnique(), p2.getSkills()[0].getTechnique());
				} else {
					rc = comparePlayers(p1, p2);
				}
				break;
			case PASSING:
				if(p1 != null && p2 != null) {
					rc = Compare.values(p1.getSkills()[0].getPassing(), p2.getSkills()[0].getPassing());
				} else {
					rc = comparePlayers(p1, p2);
				}
				break;
			case KEEPER:
				if(p1 != null && p2 != null) {
					rc = Compare.values(p1.getSkills()[0].getKeeper(), p2.getSkills()[0].getKeeper());
				} else {
					rc = comparePlayers(p1, p2);
				}
				break;
			case DEFENDER:
				if(p1 != null && p2 != null) {
					rc = Compare.values(p1.getSkills()[0].getDefender(), p2.getSkills()[0].getDefender());
				} else {
					rc = comparePlayers(p1, p2);
				}
				break;
			case PLAYMAKER:
				if(p1 != null && p2 != null) {
					rc = Compare.values(p1.getSkills()[0].getPlaymaker(), p2.getSkills()[0].getPlaymaker());
				} else {
					rc = comparePlayers(p1, p2);
				}
				break;
			case SCORER:
				if(p1 != null && p2 != null) {
					rc = Compare.values(p1.getSkills()[0].getScorer(), p2.getSkills()[0].getScorer());
				} else {
					rc = comparePlayers(p1, p2);
				}
				break;
			case SUM:
				if(p1 != null && p2 != null) {
					rc = Compare.values(p1.getSkills()[0].getSummarySkill() + p1.getSkills()[0].getStamina(), p2.getSkills()[0].getSummarySkill() + p2.getSkills()[0].getStamina());
				} else {
					rc = comparePlayers(p1, p2);
				}
				break;
			case SUM_WITHOUT_STAMINA:
				if(p1 != null && p2 != null) {
					rc = Compare.values(p1.getSkills()[0].getSummarySkill(), p2.getSkills()[0].getSummarySkill());
				} else {
					rc = comparePlayers(p1, p2);
				}
				break;
			case AVERAGE_JUMP:
				if (j1.getPops() < 2 && j2.getPops() < 2) {
					rc = Compare.values(j1.getAveragePops(), j2.getAveragePops());
				} else if (j1.getPops() < 2 && j2.getPops() >= 2) {
					rc = 1;
				} else if (j1.getPops() >= 2 && j2.getPops() < 2) {
					rc = -1;
				} else {
					rc = Compare.values(j1.getAveragePops(), j2.getAveragePops());
				}
				break;
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

	private int comparePlayers(Player p1, Player p2) {
		if(p1 != null && p2 == null) {
			return 1;
		} else if(p1 == null && p2 != null) {
			return -1;
		} else {
			return 0;
		}
	}
	/**
	 * Sets the column for sorting
	 *
	 * @param column
	 *            the column
	 */
	public void setColumn(int column) {
		this.column = column;
	}

	public int getColumn() {
		return column;
	}

	/**
	 * Sets the direction for sorting
	 *
	 * @param direction
	 *            the direction
	 */
	public void setDirection(int direction) {
		this.direction = direction;
	}

	public int getDirection() {
		return direction;
	}

	/**
	 * Reverses the direction
	 */
	public void reverseDirection() {
		direction = 1 - direction;
	}
}