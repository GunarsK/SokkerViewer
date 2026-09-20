package pl.pronux.sokker.comparators;

import java.text.Collator;
import java.util.Locale;

import pl.pronux.sokker.interfaces.Sort;
import pl.pronux.sokker.interfaces.SVComparator;
import pl.pronux.sokker.model.Player;

public class PlayerTranslatorComparator implements SVComparator<Player>, Sort {
//	public static final int ID = 0;

	public static final int NAME = 0;

	public static final int SURNAME = 1;
	
	public static final int CLUB = 2;
	
	public static final int VALUE = 3;
	
	public static final int SALARY = 4;
	
	public static final int AGE = 5;
	
	public static final int FORM = 6;
	
	public static final int STAMINA = 7;
	
	public static final int PACE = 8;
	
	public static final int TECHNIQUE = 9;
	
	public static final int PASSING = 10;
	
	public static final int KEEPER = 11;
	
	public static final int DEFENDER = 12;
	
	public static final int PLAYMAKER = 13;
	
	public static final int SCORER = 14;
	
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
	public int compare(Player p1, Player p2) {
		int rc = 0;
		Locale loc = Locale.getDefault();
		Collator coll = Collator.getInstance(loc);

		// Determine which field to sort on, then sort
		// on that field
		switch (column) {
			case NAME:
				rc = coll.compare(p1.getName(), p2.getName());
//				rc = p1.getName().compareTo(p2.getName());
				break;
			case SURNAME:
				rc = coll.compare(p1.getSurname(), p2.getSurname());
//				rc = p1.getSurname().compareTo(p2.getSurname());
				break;
			case VALUE:
				rc = p1.getSkills()[p1.getSkills().length-1].getValue().compareTo(p2.getSkills()[p2.getSkills().length-1].getValue());
				break;
			case SALARY:
				rc = p1.getSkills()[p1.getSkills().length-1].getSalary().compareTo(p2.getSkills()[p2.getSkills().length-1].getSalary());
				break;
			case AGE:
				rc = Compare.values(p1.getSkills()[p1.getSkills().length-1].getAge(), p2.getSkills()[p2.getSkills().length-1].getAge());
				break;
			case FORM:
				rc = Compare.values(p1.getSkills()[p1.getSkills().length-1].getForm(), p2.getSkills()[p2.getSkills().length-1].getForm());
				break;
			case STAMINA:
				rc = Compare.values(p1.getSkills()[p1.getSkills().length-1].getStamina(), p2.getSkills()[p2.getSkills().length-1].getStamina());
				break;
			case PACE:
				rc = Compare.values(p1.getSkills()[p1.getSkills().length-1].getPace(), p2.getSkills()[p2.getSkills().length-1].getPace());
				break;
			case TECHNIQUE:
				rc = Compare.values(p1.getSkills()[p1.getSkills().length-1].getTechnique(), p2.getSkills()[p2.getSkills().length-1].getTechnique());
				break;
			case PASSING:
				rc = Compare.values(p1.getSkills()[p1.getSkills().length-1].getPassing(), p2.getSkills()[p2.getSkills().length-1].getPassing());
				break;
			case KEEPER:
				rc = Compare.values(p1.getSkills()[p1.getSkills().length-1].getKeeper(), p2.getSkills()[p2.getSkills().length-1].getKeeper());
				break;
			case DEFENDER:
				rc = Compare.values(p1.getSkills()[p1.getSkills().length-1].getDefender(), p2.getSkills()[p2.getSkills().length-1].getDefender());
				break;
			case PLAYMAKER:
				rc = Compare.values(p1.getSkills()[p1.getSkills().length-1].getPlaymaker(), p2.getSkills()[p2.getSkills().length-1].getPlaymaker());
				break;
			case SCORER:
				rc = Compare.values(p1.getSkills()[p1.getSkills().length-1].getScorer(), p2.getSkills()[p2.getSkills().length-1].getScorer());
				break;
			case CLUB:
				rc = p1.getClubName().compareTo(p2.getClubName());
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