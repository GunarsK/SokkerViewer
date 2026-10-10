package pl.pronux.sokker.comparators;

import java.text.Collator;
import java.util.Locale;

import pl.pronux.sokker.interfaces.Sort;
import pl.pronux.sokker.interfaces.SVComparator;
import pl.pronux.sokker.model.Player;

public class PlayerAssistantComparator implements SVComparator<Player>, Sort {

	public static final int NAME = 0;

	public static final int SURNAME = 1;

	/** first of the Player.POSITION_COUNT rating columns */
	public static final int RATING = 2;

	public static final int POSITION = RATING + Player.POSITION_COUNT;

	private final Collator coll = Collator.getInstance(Locale.getDefault());

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

		// Determine which field to sort on, then sort
		// on that field
		switch (column) {
			case NAME:
				rc = coll.compare(p1.getName(), p2.getName());
				break;
			case SURNAME:
				rc = coll.compare(p1.getSurname(), p2.getSurname());
				break;
			case POSITION:
				rc = Compare.values(p1.getPosition(), p2.getPosition());
				break;
			default:
				if (column >= RATING && column < POSITION) {
					rc = Compare.values(p1.getPositionTable()[column - RATING], p2.getPositionTable()[column - RATING]);
				}
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