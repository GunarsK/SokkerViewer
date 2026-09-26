package pl.pronux.sokker.model;

/** bounds of a player's talent, in weeks a junior needs per level */
public class Talent {

	/** the best talent sokker gives */
	public static final double BEST = 3.0;

	/** nothing known beyond the best talent */
	public static final Talent UNKNOWN = new Talent(BEST, Double.POSITIVE_INFINITY);

	/** lower bound, at least BEST */
	private final double min;

	/** upper bound, Double.POSITIVE_INFINITY when not known */
	private final double max;

	public Talent(double min, double max) {
		this.min = min;
		this.max = max;
	}

	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}

	/** "min-max" with "?" for an upper bound not known, empty when nothing beyond BEST is known */
	public String getText() {
		if (min <= BEST && max == Double.POSITIVE_INFINITY) {
			return "";
		}
		return String.format("%.2f", min) + "\u2013" + (max == Double.POSITIVE_INFINITY ? "?" : String.format("%.2f", max));
	}
}
