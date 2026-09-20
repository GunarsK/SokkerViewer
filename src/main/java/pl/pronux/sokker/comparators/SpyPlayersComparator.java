package pl.pronux.sokker.comparators;

import java.text.Collator;
import java.util.Collections;
import java.util.Locale;

import pl.pronux.sokker.interfaces.Sort;
import pl.pronux.sokker.interfaces.SVComparator;
import pl.pronux.sokker.model.Player;
import pl.pronux.sokker.model.PlayerStats;

public class SpyPlayersComparator implements SVComparator<Player>, Sort {
	public static final int COUNTRY = 0;

	public static final int NAME = 1;

	public static final int SURNAME = 2;
	
	public static final int HEIGHT = 3;

	public static final int WEIGHT = 4;
	
	public static final int BMI = 5;
	
	public static final int VALUE = 6;

	public static final int SALARY = 7;

	public static final int AGE = 8;
	
	public static final int FORM = 9;

	public static final int DISCIPLINE = 10;

	public static final int EXPERIENCE = 11;

	public static final int TEAMWORK = 12;

	public static final int MATCHES = 13;

	public static final int GOALS = 14;

	public static final int ASSISTS = 15;

	public static final int RANKING_AVG = 16;
	
	public static final int RANKING_MAX = 17;
	
	public static final int RANKING_MIN = 18;
	
	public static final int PREFERRED_POSITION = 19;

	public static final int CARDS = 20;

	public static final int INJURY = 21;

	public static final int NOTE = 22;

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
			// rc = p1.getName().compareTo(p2.getName());
			break;
		case SURNAME:
			rc = coll.compare(p1.getSurname(), p2.getSurname());
			// rc = p1.getSurname().compareTo(p2.getSurname());
			break;
		case HEIGHT:
			rc = Compare.values(p1.getHeight(), p2.getHeight());
			break;
		case WEIGHT:
			rc = Compare.values(p1.getSkills()[p1.getSkills().length - 1].getWeight(), p2.getSkills()[p2.getSkills().length - 1].getWeight());
			break;
		case BMI:
			rc = Compare.values(p1.getSkills()[p1.getSkills().length - 1].getBmi(), p2.getSkills()[p2.getSkills().length - 1].getBmi());
			break;
		case COUNTRY:
			rc = Compare.values(p1.getCountryfrom(), p2.getCountryfrom());
			break;
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
		case DISCIPLINE:
			rc = Compare.values(p1.getSkills()[p1.getSkills().length - 1].getDiscipline(), p2.getSkills()[p2.getSkills().length - 1].getDiscipline());
			break;
		case EXPERIENCE:
			rc = Compare.values(p1.getSkills()[p1.getSkills().length - 1].getExperience(), p2.getSkills()[p2.getSkills().length - 1].getExperience());
			break;
		case TEAMWORK:
			rc = Compare.values(p1.getSkills()[p1.getSkills().length - 1].getTeamwork(), p2.getSkills()[p2.getSkills().length - 1].getTeamwork());
			break;
		case MATCHES:
			rc = Compare.values(p1.getSkills()[p1.getSkills().length - 1].getMatches(), p2.getSkills()[p2.getSkills().length - 1].getMatches());
			break;
		case GOALS:
			rc = Compare.values(p1.getSkills()[p1.getSkills().length - 1].getGoals(), p2.getSkills()[p2.getSkills().length - 1].getGoals());
			break;
		case ASSISTS:
			rc = Compare.values(p1.getSkills()[p1.getSkills().length - 1].getAssists(), p2.getSkills()[p2.getSkills().length - 1].getAssists());
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
		case RANKING_AVG:
			rc = Compare.values(p1.getAvgRating(), p2.getAvgRating());
			break;
		case PREFERRED_POSITION:
			rc = Compare.values(p1.getPreferredPosition(), p2.getPreferredPosition());
			break;
		case RANKING_MAX:
			if (p1.getPlayerMatchStatistics().size() == 0 && p2.getPlayerMatchStatistics().size() == 0) {
				rc = 0;
			} else if (p1.getPlayerMatchStatistics().size() > 0 && p2.getPlayerMatchStatistics().size() == 0) {
				rc = 1;
			} else if (p1.getPlayerMatchStatistics().size() == 0 && p2.getPlayerMatchStatistics().size() > 0) {
				rc = -1;
			} else {
				PlayerStats p1max = Collections.max(p1.getPlayerMatchStatistics(), new PlayerStatsComparator(PlayerStatsComparator.RATING, PlayerStatsComparator.ASCENDING));
				PlayerStats p2max = Collections.max(p2.getPlayerMatchStatistics(), new PlayerStatsComparator(PlayerStatsComparator.RATING, PlayerStatsComparator.ASCENDING));
				rc = Compare.values(p1max.getRating(), p2max.getRating());
			}
			break;
		case RANKING_MIN:
			if (p1.getPlayerMatchStatistics().size() == 0 && p2.getPlayerMatchStatistics().size() == 0) {
				rc = 0;
			} else if (p1.getPlayerMatchStatistics().size() > 0 && p2.getPlayerMatchStatistics().size() == 0) {
				rc = 1;
			} else if (p1.getPlayerMatchStatistics().size() == 0 && p2.getPlayerMatchStatistics().size() > 0) {
				rc = -1;
			} else {
				PlayerStats p1min = Collections.min(p1.getPlayerMatchStatistics(), new PlayerStatsComparator(PlayerStatsComparator.RATING, PlayerStatsComparator.ASCENDING));
				PlayerStats p2min = Collections.min(p2.getPlayerMatchStatistics(), new PlayerStatsComparator(PlayerStatsComparator.RATING, PlayerStatsComparator.ASCENDING));
				rc = Compare.values(p1min.getRating(), p2min.getRating());
			}
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