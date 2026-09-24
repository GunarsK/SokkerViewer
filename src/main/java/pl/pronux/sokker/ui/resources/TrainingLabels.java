package pl.pronux.sokker.ui.resources;

import pl.pronux.sokker.model.Training;
import pl.pronux.sokker.resources.Messages;

/**
 * texts describing the training of a week, shared by the places that show it as a
 * sentence rather than as table columns.
 */
public final class TrainingLabels {

	private TrainingLabels() {
	}

	/**
	 * the types trained in that week, one per position, for example "GK: Pace, DEF: Pace,
	 * MID: Technique, ATT: Striker". Weeks recorded before sokker sent a type per position
	 * name only the positions their single type applied to.
	 */
	public static String describe(Training training) {
		return describe(training, false);
	}

	/** describe with the head coach's level in each type's skill, for example "GK: Pace [14]" */
	public static String describeWithHeadCoach(Training training) {
		return describe(training, true);
	}

	private static String describe(Training training, boolean headCoach) {
		if (training == null) {
			return "";
		}
		StringBuilder text = new StringBuilder();
		for (int position = Training.FORMATION_GK; position <= Training.FORMATION_ATT; position++) {
			int type = training.getEffectiveTypeForPosition(position);
			if (type == Training.TYPE_NOT_SET) {
				continue;
			}
			if (text.length() > 0) {
				text.append(", ");
			}
			text.append(Messages.getString("formation." + position));
			text.append(": ");
			text.append(Messages.getString("training.type." + type));
			if (headCoach) {
				text.append(" [").append(training.getHeadCoachSkill(type)).append(']');
			}
		}
		return text.toString();
	}
}
