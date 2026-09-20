package pl.pronux.sokker.comparators;

import java.text.Collator;
import java.util.Locale;

import pl.pronux.sokker.interfaces.Sort;
import pl.pronux.sokker.interfaces.SVComparator;
import pl.pronux.sokker.model.Player;

public class PlayerComparator implements SVComparator<Player>, Sort {
	// public static final int ID = 0;

	public static final int NAME = 1;

	public static final int SURNAME = 2;
	
	public static final int HEIGHT = 3; 

	//public static final int WEIGHT = 4;
	
	//public static final int BMI = 5;
	
	public static final int VALUE = 4;

	public static final int SALARY = 5;

	public static final int AGE = 6;

	public static final int FORM = 7;

	public static final int STAMINA = 8;

	public static final int PACE = 9;

	public static final int TECHNIQUE = 10;

	public static final int PASSING = 11;

	public static final int KEEPER = 12;

	public static final int DEFENDER = 13;

	public static final int PLAYMAKER = 14;

	public static final int SCORER = 15;

	public static final int DISCIPLINE = 16;

	public static final int EXPERIENCE = 17;

	public static final int TEAMWORK = 18;

	public static final int CARDS = 19;

	public static final int INJURY = 20;
	
	public static final int NOTE = 21;

	private int column;

	private int direction;

	/**
	 * Compares two Player objects
	 * 
	 * @param obj1
	 *          the first Player
	 * @param obj2
	 *          the second Player
	 * @return int
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Player p1, Player p2) {
		int rc = 0;
		Locale loc = Locale.getDefault();
		Collator coll = Collator.getInstance(loc);

		// Determine which field to sort on, then sort
		// on that field
		switch (column) {
		case NAME:
			rc = coll.compare(p1.getName(), p2.getName());
			// rc = p1.getName().compareTo(p2.getName());
			break;
		case SURNAME:
			rc = coll.compare(p1.getSurname(), p2.getSurname());
			// rc = p1.getSurname().compareTo(p2.getSurname());
			break;
		case HEIGHT:
			rc = Compare.values(p1.getHeight(), p2.getHeight());
			break;
		//case WEIGHT:
		//	rc = p1.getSkills()[p1.getSkills().length - 1].getWeight() < p2.getSkills()[p2.getSkills().length - 1].getWeight() ? -1 : 1;
		//	break;
		//case BMI:
		//	rc = p1.getSkills()[p1.getSkills().length - 1].getBmi() < p2.getSkills()[p2.getSkills().length - 1].getBmi() ? -1 : 1;
		//	break;
		case VALUE:
			rc = p1.getSkills()[p1.getSkills().length - 1].getValue().compareTo(p2.getSkills()[p2.getSkills().length - 1].getValue());
			break;
		case SALARY:
			rc = p1.getSkills()[p1.getSkills().length - 1].getSalary().compareTo(p2.getSkills()[p2.getSkills().length - 1].getSalary());
			break;
		case AGE:
			rc = Compare.values(p1.getSkills()[p1.getSkills().length - 1].getAge(), p2.getSkills()[p2.getSkills().length - 1].getAge());
			break;
		case FORM:
			rc = Compare.values(p1.getSkills()[p1.getSkills().length - 1].getForm(), p2.getSkills()[p2.getSkills().length - 1].getForm());
			break;
		case STAMINA:
			rc = Compare.values(p1.getSkills()[p1.getSkills().length - 1].getStamina(), p2.getSkills()[p2.getSkills().length - 1].getStamina());
			break;
		case PACE:
			rc = Compare.values(p1.getSkills()[p1.getSkills().length - 1].getPace(), p2.getSkills()[p2.getSkills().length - 1].getPace());
			break;
		case TECHNIQUE:
			rc = Compare.values(p1.getSkills()[p1.getSkills().length - 1].getTechnique(), p2.getSkills()[p2.getSkills().length - 1].getTechnique());
			break;
		case PASSING:
			rc = Compare.values(p1.getSkills()[p1.getSkills().length - 1].getPassing(), p2.getSkills()[p2.getSkills().length - 1].getPassing());
			break;
		case KEEPER:
			rc = Compare.values(p1.getSkills()[p1.getSkills().length - 1].getKeeper(), p2.getSkills()[p2.getSkills().length - 1].getKeeper());
			break;
		case DEFENDER:
			rc = Compare.values(p1.getSkills()[p1.getSkills().length - 1].getDefender(), p2.getSkills()[p2.getSkills().length - 1].getDefender());
			break;
		case PLAYMAKER:
			rc = Compare.values(p1.getSkills()[p1.getSkills().length - 1].getPlaymaker(), p2.getSkills()[p2.getSkills().length - 1].getPlaymaker());
			break;
		case SCORER:
			rc = Compare.values(p1.getSkills()[p1.getSkills().length - 1].getScorer(), p2.getSkills()[p2.getSkills().length - 1].getScorer());
			break;
		case DISCIPLINE:
			rc = Compare.values(p1.getSkills()[p1.getSkills().length - 1].getDiscipline(), p2.getSkills()[p2.getSkills().length - 1].getDiscipline());
			break;
		case EXPERIENCE:
			rc = Compare.values(p1.getSkills()[p1.getSkills().length - 1].getExperience(), p2.getSkills()[p2.getSkills().length - 1].getExperience());
			break;
		case TEAMWORK:
			rc = Compare.values(p1.getSkills()[p1.getSkills().length - 1].getTeamwork(), p2.getSkills()[p2.getSkills().length - 1].getTeamwork());
			break;

		case NOTE:
			if (p1.getNote() == null && p2.getNote() == null) {
				rc = 0;
			} else if (p1.getNote() != null && p2.getNote() == null) {
				rc = 1;
			} else if (p1.getNote() == null && p2.getNote() != null) {
				rc = -1;
			} else {
				rc = (p1.getNote().compareTo(p2.getNote()));
			}
			break;
		case CARDS:
			rc = Compare.values(p1.getSkills()[p1.getSkills().length - 1].getCards(), p2.getSkills()[p2.getSkills().length - 1].getCards());
			break;
		case INJURY:
			rc = Compare.values(p1.getSkills()[p1.getSkills().length - 1].getInjurydays(), p2.getSkills()[p2.getSkills().length - 1].getInjurydays());
			break;
		default:
			// TODO: Implement 'default' statement
			break;
		}

		// Check the direction for sort and flip the sign
		// if appropriate
		if (direction == DESCENDING) {
			rc = -rc;
		}
		return rc;
	}

	/**
	 * Sets the column for sorting
	 * 
	 * @param column
	 *          the column
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
	 *          the direction
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