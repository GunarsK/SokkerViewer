package pl.pronux.sokker.comparators;

/**
 * three way comparison of two values.
 *
 * The comparators used to write <code>a &lt; b ? -1 : 1</code>, which never returns 0, so
 * two equal values compared as "a before b" and as "b before a" at the same time. Sorting
 * a table with enough equal values then failed with "Comparison method violates its
 * general contract".
 */
public final class Compare {

	private Compare() {
	}

	public static int values(int v1, int v2) {
		return v1 < v2 ? -1 : (v1 > v2 ? 1 : 0);
	}

	public static int values(long v1, long v2) {
		return v1 < v2 ? -1 : (v1 > v2 ? 1 : 0);
	}

	public static int values(double v1, double v2) {
		return v1 < v2 ? -1 : (v1 > v2 ? 1 : 0);
	}
}
