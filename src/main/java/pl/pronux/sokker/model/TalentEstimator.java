package pl.pronux.sokker.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** a player's talent from the trainings sokker.org reported, after sokker asistente */
public class TalentEstimator {

	/** normalised points one level costs a player of talent 1 */
	private static final double POINTS_PER_TALENT = 47;

	/** yearly growth of a level's cost from age 16 */
	private static final double AGE_FACTOR = 1.094;

	/** yearly growth of a pace level's cost from age 16 */
	private static final double PACE_AGE_FACTOR = 1.1;

	/** growth of a level's cost per level */
	private static final double LEVEL_FACTOR = 1.094;

	/** share of a training every skill gets */
	private static final double RESIDUAL = 0.15;

	/** share of the direct training outside the advanced slots */
	private static final double FORMATION = 0.14;

	/** head coach level that trains at full strength */
	private static final double FULL_HEAD_COACH = 16.5;

	/** assistants' level that trains at full strength */
	private static final double FULL_ASSISTANTS = 16.25;

	/** head coach skill assumed when no training has coaches */
	private static final int DEFAULT_HEAD_COACH = 16;

	/** assistant places the assistants' level is shared over */
	private static final int ASSISTANT_PLACES = 3;

	/** age from which trainings are left out */
	private static final int MAX_AGE = 28;

	/** a skill's top level, which no training raises */
	private static final int MAX_LEVEL = 18;

	/** injury days from which a player does not train */
	private static final int SEVERE_INJURY_DAYS = 8;

	/** skills the talent is read from, as training types */
	private static final int[] SKILLS = { Training.TYPE_PACE, Training.TYPE_TECHNIQUE, Training.TYPE_PASSING, Training.TYPE_KEEPER,
			Training.TYPE_DEFENDING, Training.TYPE_PLAYMAKING, Training.TYPE_STRIKER };

	/** per skill, its levels since the last break, oldest first, the last one the level it is at */
	private final List<List<Level>> chains = new ArrayList<List<Level>>();

	private double min = Talent.BEST;

	private double max = Double.POSITIVE_INFINITY;

	/** a skill's trainings at one level */
	private static class Level {

		private final int value;

		/** normalised points of all its trainings */
		private double points;

		/** normalised points of its trainings before the pop */
		private double beforePop;

		/** the pop training's points normalised at the next level */
		private double popAbove;

		private Level(int value) {
			this.value = value;
		}
	}

	private TalentEstimator() {
		for (int i = 0; i < SKILLS.length; i++) {
			chains.add(new ArrayList<Level>());
		}
	}

	/** sets on every row of the players the talent known once that row's training is counted */
	public static void estimate(List<Player> players, Collection<Training> trainings) {
		Map<Training, Training> coaches = new HashMap<Training, Training>();
		for (Training training : trainings) {
			coaches.put(training, coachesOf(training, trainings));
		}
		for (Player player : players) {
			estimate(player.getSkills(), coaches);
		}
	}

	private static void estimate(PlayerSkills[] rows, Map<Training, Training> coaches) {
		TalentEstimator estimator = new TalentEstimator();
		PlayerSkills before = null;
		for (PlayerSkills row : rows) {
			if (before != null && week(row) != week(before)) {
				if (week(row) == week(before) + 1 && isReported(row) && row.getTrainingAge() < MAX_AGE) {
					estimator.train(before, row, coaches.get(row.getTraining()));
				} else {
					estimator.clear();
				}
			}
			row.setTalent(new Talent(estimator.min, estimator.max));
			before = row;
		}
	}

	private static int week(PlayerSkills row) {
		return row.getDate().getSokkerDate().getTrainingWeek();
	}

	private static int week(Training training) {
		return training.getDate().getSokkerDate().getTrainingWeek();
	}

	/** true when sokker.org reported what the row's training gave the player */
	private static boolean isReported(PlayerSkills row) {
		int position = row.getTrainingPosition();
		return row.getTrainingIntensity() >= 0 && row.getTrainingSlot() != Training.SLOT_NOT_SET && position >= Training.FORMATION_GK
				&& position <= Training.FORMATION_ATT && row.getTraining() != null
				&& row.getTraining().getEffectiveTypeForPosition(position) > Training.TYPE_UNKNOWN;
	}

	private static boolean hasCoaches(Training training) {
		return training.getHeadCoach() != null || !training.getAssistants().isEmpty();
	}

	/** the training itself when it has coaches, else the nearest that has, the later of two as near */
	private static Training coachesOf(Training training, Collection<Training> trainings) {
		if (hasCoaches(training)) {
			return training;
		}
		int week = week(training);
		Training nearest = null;
		for (Training other : trainings) {
			if (hasCoaches(other) && (nearest == null || isNearer(week(other), week(nearest), week))) {
				nearest = other;
			}
		}
		return nearest;
	}

	/** true when week a is nearer the week than b, or as near and later */
	private static boolean isNearer(int a, int b, int week) {
		int difference = Math.abs(a - week) - Math.abs(b - week);
		return difference < 0 || difference == 0 && a > b;
	}

	private void clear() {
		for (List<Level> chain : chains) {
			chain.clear();
		}
	}

	private void train(PlayerSkills before, PlayerSkills row, Training coaches) {
		int age = row.getTrainingAge();
		for (int i = 0; i < SKILLS.length; i++) {
			int skill = SKILLS[i];
			int level = level(before, skill);
			int next = level(row, skill);
			List<Level> chain = chains.get(i);
			// a drop, a double pop or a training at the top level breaks the chain
			if (next < level || next > level + 1 || level >= MAX_LEVEL) {
				chain.clear();
				continue;
			}
			if (chain.isEmpty() || chain.get(chain.size() - 1).value != level) {
				chain.clear();
				chain.add(new Level(level));
			}
			Level current = chain.get(chain.size() - 1);
			double points = points(skill, row, coaches);
			double normalised = normalise(points, skill, age, level);
			current.points += normalised;
			if (next > level) {
				current.popAbove = normalise(points, skill, age, next);
				boundFromAbove(chain);
				chain.add(new Level(next));
			} else {
				current.beforePop += normalised;
			}
			boundFromBelow(chain);
		}
	}

	/** lower bound: points spent from a level on without leaving the last, per level */
	private void boundFromBelow(List<Level> chain) {
		int last = chain.size() - 1;
		double points = chain.get(last).beforePop;
		for (int first = last; first >= 0; first--) {
			if (first < last) {
				points += chain.get(first).points;
			}
			min = Math.max(min, points / (last - first + 1));
		}
	}

	/** upper bound: points from a pop up to the last pop, per pop */
	private void boundFromAbove(List<Level> chain) {
		int last = chain.size() - 1;
		double points = 0;
		for (int first = last - 1; first >= 0; first--) {
			points += chain.get(first + 1).points;
			max = Math.min(max, (chain.get(first).popAbove + points) / (last - first));
		}
	}

	/** what a training gave one skill, 0 - 100 */
	private static double points(int skill, PlayerSkills row, Training coaches) {
		int position = row.getTrainingPosition();
		if (row.getTrainingInjuryDays() >= SEVERE_INJURY_DAYS || skill == Training.TYPE_KEEPER && position != Training.FORMATION_GK) {
			return 0;
		}
		double effectiveness = Math.min(100, row.getTrainingIntensity());
		double headCoach = ((coaches == null ? DEFAULT_HEAD_COACH : coaches.getHeadCoachSkill(skill)) + 0.5) / FULL_HEAD_COACH;
		double assistants = coaches == null ? FULL_ASSISTANTS : assistantsLevel(coaches.getAssistants());
		double residual = effectiveness * RESIDUAL * (headCoach + assistants / FULL_ASSISTANTS) / 2;
		if (row.getTraining().getEffectiveTypeForPosition(position) != skill || !trainsAt(skill, position)) {
			return residual;
		}
		double direct = effectiveness * headCoach * (1 - RESIDUAL) * (row.getTrainingSlot() == Training.SLOT_ADVANCED ? 1 : FORMATION);
		return Math.min(100, direct + residual);
	}

	/** false for defending, playmaking or striker trained outside its own formation */
	private static boolean trainsAt(int skill, int position) {
		switch (skill) {
		case Training.TYPE_DEFENDING:
			return position == Training.FORMATION_DEF;
		case Training.TYPE_PLAYMAKING:
			return position == Training.FORMATION_MID;
		case Training.TYPE_STRIKER:
			return position == Training.FORMATION_ATT;
		default:
			return true;
		}
	}

	/** the assistants' level: each half a level above the mean of his general skill and his skills' mean */
	private static double assistantsLevel(List<Coach> assistants) {
		double sum = 0;
		for (Coach coach : assistants) {
			double mean = (coach.getStamina() + coach.getPace() + coach.getTechnique() + coach.getPassing() + coach.getKeepers() + coach.getDefenders()
					+ coach.getPlaymakers() + coach.getScorers()) / 8.0;
			sum += (mean + coach.getGeneralskill() + 1) / 2;
		}
		return Math.min(FULL_ASSISTANTS, sum / ASSISTANT_PLACES);
	}

	/** points in talent units, scaled to age 16 and the offset level */
	private static double normalise(double points, int skill, int age, int level) {
		double ageFactor = skill == Training.TYPE_PACE ? PACE_AGE_FACTOR : AGE_FACTOR;
		return points * Math.pow(ageFactor, 16 - age) * Math.pow(LEVEL_FACTOR, offset(skill) - level) / POINTS_PER_TALENT;
	}

	/** per skill, the level the talent scale is set at */
	private static double offset(int skill) {
		switch (skill) {
		case Training.TYPE_PACE:
			return 4.5;
		case Training.TYPE_TECHNIQUE:
			return 6;
		case Training.TYPE_DEFENDING:
		case Training.TYPE_STRIKER:
			return 5.5;
		default:
			return 6.8;
		}
	}

	private static int level(PlayerSkills row, int skill) {
		switch (skill) {
		case Training.TYPE_PACE:
			return row.getPace();
		case Training.TYPE_TECHNIQUE:
			return row.getTechnique();
		case Training.TYPE_PASSING:
			return row.getPassing();
		case Training.TYPE_KEEPER:
			return row.getKeeper();
		case Training.TYPE_DEFENDING:
			return row.getDefender();
		case Training.TYPE_PLAYMAKING:
			return row.getPlaymaker();
		default:
			return row.getScorer();
		}
	}
}
