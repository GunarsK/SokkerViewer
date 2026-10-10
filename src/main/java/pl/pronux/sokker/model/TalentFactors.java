package pl.pronux.sokker.model;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/** the talent estimate's factors, sokker asistente's defaults unless set */
public final class TalentFactors {

	/** a factor with sokker asistente's default and its largest value */
	public enum Factor {
		AGE(1.094, 2), PACE_AGE(1.1, 2), SKILL(1.094, 2), TALENT(1, 2), GENERAL(0.15, 2), FORMATION(0.14, 2), PACE(0.662, 1), TECHNIQUE(0.882, 1),
		PASSING(1, 1), KEEPER(1, 1), DEFENDER(0.809, 1), PLAYMAKER(1, 1), STRIKER(0.809, 1);

		private final double defaultValue;

		private final double max;

		private Factor(double defaultValue, double max) {
			this.defaultValue = defaultValue;
			this.max = max;
		}

		public double getDefault() {
			return defaultValue;
		}

		public double getMax() {
			return max;
		}

		/** name in the database and the language keys */
		public String getKey() {
			return name().toLowerCase(Locale.ROOT);
		}
	}

	private static final Map<Factor, Double> values = new EnumMap<Factor, Double>(Factor.class);

	private TalentFactors() {
	}

	/** the factor's value, its default when not set */
	public static double get(Factor factor) {
		Double value = values.get(factor);
		return value == null ? factor.getDefault() : value.doubleValue();
	}

	/** replaces the set values; factors left out take their defaults */
	public static void set(Map<Factor, Double> factors) {
		values.clear();
		values.putAll(factors);
	}
}
