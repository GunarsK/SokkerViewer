package pl.pronux.sokker.model;

import java.util.ArrayList;
import java.util.List;

import pl.pronux.sokker.bean.TrainingSummary;

public class Training implements Cloneable {

	public static final int FORMATION_GK = 0;
	public static final int FORMATION_DEF = 1;
	public static final int FORMATION_MID = 2;
	public static final int FORMATION_ATT = 3;
	public static final int FORMATION_ALL = 4;

	public static final int TYPE_UNKNOWN = 0;
	public static final int TYPE_STAMINA = 1;
	public static final int TYPE_KEEPER = 2;
	public static final int TYPE_PLAYMAKING = 3;
	public static final int TYPE_PASSING = 4;
	public static final int TYPE_TECHNIQUE = 5;
	public static final int TYPE_DEFENDING = 6;
	public static final int TYPE_STRIKER = 7;
	public static final int TYPE_PACE = 8;

	/** per position training type of a week that was recorded before sokker sent them */
	public static final int TYPE_NOT_SET = -1;

	/** training position of a player that was recorded before sokker sent it */
	public static final int POSITION_NOT_SET = -1;

	public static final int SLOT_NOT_SET = -1;
	public static final int SLOT_FORMATION = 0;
	public static final int SLOT_ADVANCED = 1;
	/** sokker gave the player no training that week, e.g. he played outside his formation */
	public static final int SLOT_MISSING = 2;

	public static final int NO_TRAINING = 1 << 1;
	public static final int NEW_TRAINING = 1 << 2;
	public static final int UPDATE_TRAINING = 1 << 3;
	public static final int UPDATE_PLAYERS = 1 << 4;

	private Date date;

	private int formation;

	private int typeGk = TYPE_NOT_SET;

	private int typeDef = TYPE_NOT_SET;

	private int typeMid = TYPE_NOT_SET;

	private int typeAtt = TYPE_NOT_SET;

	/** the four position types were confirmed by sokker.org's training report */
	private boolean apiConfirmed;

	private int id;

	private String note;

	private int type;

	private Coach headCoach;

	private Coach juniorCoach;

	private List<Coach> assistants = new ArrayList<Coach>();

	private List<Player> players;

	private List<Junior> juniors;

	private boolean reported;

	private int status;

	public List<Coach> getAssistants() {
		return assistants;
	}

	public void setAssistants(List<Coach> assistants) {
		this.assistants = assistants;
	}

	public Coach getHeadCoach() {
		return headCoach;
	}

	public void setHeadCoach(Coach headCoach) {
		this.headCoach = headCoach;
	}

	public Coach getJuniorCoach() {
		return juniorCoach;
	}

	public void setJuniorCoach(Coach juniorCoach) {
		this.juniorCoach = juniorCoach;
	}

	public Date getDate() {
		return date;
	}

	public int getFormation() {
		return formation;
	}

	public int getId() {
		return id;
	}

	public String getNote() {
		return note;
	}

	public int getType() {
		return type;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setFormation(int formation) {
		this.formation = formation;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getTypeGk() {
		return typeGk;
	}

	public void setTypeGk(int typeGk) {
		this.typeGk = typeGk;
	}

	public int getTypeDef() {
		return typeDef;
	}

	public void setTypeDef(int typeDef) {
		this.typeDef = typeDef;
	}

	public int getTypeMid() {
		return typeMid;
	}

	public void setTypeMid(int typeMid) {
		this.typeMid = typeMid;
	}

	public int getTypeAtt() {
		return typeAtt;
	}

	public void setTypeAtt(int typeAtt) {
		this.typeAtt = typeAtt;
	}

	public boolean isApiConfirmed() {
		return apiConfirmed;
	}

	public void setApiConfirmed(boolean apiConfirmed) {
		this.apiConfirmed = apiConfirmed;
	}

	/**
	 * type trained by the given position in this week, or TYPE_NOT_SET when not known
	 */
	public int getTypeForPosition(int position) {
		switch (position) {
		case FORMATION_GK:
			return typeGk;
		case FORMATION_DEF:
			return typeDef;
		case FORMATION_MID:
			return typeMid;
		case FORMATION_ATT:
			return typeAtt;
		default:
			return TYPE_NOT_SET;
		}
	}

	/**
	 * type the given position trained in this week, TYPE_NOT_SET when not known.
	 * Weeks recorded before sokker sent a type per position only know their single type,
	 * which applied to the whole squad when it was pace or stamina, and to one formation
	 * otherwise.
	 */
	public int getEffectiveTypeForPosition(int position) {
		if (hasPositionTypes()) {
			return getTypeForPosition(position);
		}
		if (type == TYPE_UNKNOWN) {
			return TYPE_NOT_SET;
		}
		boolean wholeSquad = type == TYPE_PACE || type == TYPE_STAMINA || formation == FORMATION_ALL;
		return wholeSquad || formation == position ? type : TYPE_NOT_SET;
	}

	/**
	 * true when this week was recorded with a training type per position
	 */
	public boolean hasPositionTypes() {
		return typeGk != TYPE_NOT_SET || typeDef != TYPE_NOT_SET || typeMid != TYPE_NOT_SET || typeAtt != TYPE_NOT_SET;
	}

	/**
	 * the week's single training type: the common type of the positions that trained when
	 * sokker's api gave them, TYPE_UNKNOWN when they disagree, and the legacy type recorded
	 * by the xml sync for weeks that have no per-position types. The legacy field is not
	 * rewritten by an api import, so readers asking "what did this week train" use this.
	 */
	public int getEffectiveType() {
		if (!hasPositionTypes()) {
			return type;
		}
		int common = TYPE_UNKNOWN;
		for (int position = FORMATION_GK; position <= FORMATION_ATT; position++) {
			int positionType = getTypeForPosition(position);
			if (positionType == TYPE_NOT_SET) {
				continue;
			}
			if (common == TYPE_UNKNOWN) {
				common = positionType;
			} else if (common != positionType) {
				return TYPE_UNKNOWN;
			}
		}
		return common;
	}

	/**
	 * true when something is known about what this week trained, either its single type or
	 * a type per position
	 */
	public boolean isTypeKnown() {
		return type != TYPE_UNKNOWN || hasPositionTypes();
	}

	public boolean isReported() {
		return reported;
	}

	public void setReported(boolean reported) {
		this.reported = reported;
	}

	public List<Junior> getJuniors() {
		return juniors;
	}

	public void setJuniors(List<Junior> juniors) {
		this.juniors = juniors;
	}

	public List<Player> getPlayers() {
		return players;
	}

	public void setPlayers(List<Player> players) {
		this.players = players;
	}

	// public Object clone() {
	// Object o = null;
	// try {
	// o = super.clone();
	// } catch (CloneNotSupportedException e) {
	// System.err.println("There is no posibillity to clone training object");
	// }
	// return o;
	// }

	public void copy(Training training) {
		this.setAssistants(training.getAssistants());
		this.setJuniors(training.getJuniors());
		this.setPlayers(training.getPlayers());
		this.setDate(training.getDate());
		this.setFormation(training.getFormation());
		this.setHeadCoach(training.getHeadCoach());
		this.setId(training.getId());
		this.setJuniorCoach(training.getJuniorCoach());
		this.setNote(training.getNote());
		this.setReported(training.isReported());
		this.setType(training.getType());
		this.setTypeGk(training.getTypeGk());
		this.setTypeDef(training.getTypeDef());
		this.setTypeMid(training.getTypeMid());
		this.setTypeAtt(training.getTypeAtt());
		this.setApiConfirmed(training.isApiConfirmed());
	}

	public Training clone() {
		Training training = new Training();
		training.setAssistants(this.getAssistants());
		training.setAssistants(new ArrayList<Coach>());
		for (Coach assistant : this.assistants) {
			training.getAssistants().add(assistant);
		}
		training.setJuniors(this.getJuniors());
		training.setPlayers(this.getPlayers());
		training.setDate(this.getDate());
		training.setFormation(this.getFormation());
		training.setHeadCoach(this.getHeadCoach());
		training.setId(this.getId());
		training.setJuniorCoach(this.getJuniorCoach());
		training.setNote(this.getNote());
		training.setReported(this.isReported());
		training.setType(this.getType());
		training.setTypeGk(this.getTypeGk());
		training.setTypeDef(this.getTypeDef());
		training.setTypeMid(this.getTypeMid());
		training.setTypeAtt(this.getTypeAtt());
		training.setApiConfirmed(this.isApiConfirmed());
		return training;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public TrainingSummary getTrainingSummary() {
		TrainingSummary trainingSummary = new TrainingSummary();
		trainingSummary = getJuniorsTrainingSummary(trainingSummary);
		trainingSummary = getPlayersTrainingSummary(trainingSummary);
		return trainingSummary;
	}

	private TrainingSummary getJuniorsTrainingSummary(TrainingSummary trainingSummary) {
		int max = 0;
		if (this.getJuniors().size() > 0) {
			for (Junior junior : this.getJuniors()) {
				JuniorSkills[] skills = junior.getSkills();
				max = skills.length;
				if (max > 1) {
					for (int i = max - 1; i > 0; i--) {
						if (this.equals(skills[i].getTraining())) {
							if (skills[i].getSkill() - skills[i - 1].getSkill() > 0) {
								trainingSummary.setJuniorsPops(trainingSummary.getJuniorsPops() + 1);
							} else if (skills[i].getSkill() - skills[i - 1].getSkill() < 0) {
								trainingSummary.setJuniorsFalls(trainingSummary.getJuniorsFalls() + 1);
							}
							break;
						}
					}
				}
			}
		}
		return trainingSummary;
	}

	private TrainingSummary getPlayersTrainingSummary(TrainingSummary trainingSummary) {
		int max = 0;
		if (this.getPlayers().size() > 0) {
			for (Player player : this.getPlayers()) {
				PlayerSkills[] skills = player.getSkills();
				max = skills.length;
				if (max > 1) {
					for (int i = max - 1; i > 0; i--) {
						if (this.equals(skills[i].getTraining())) {
							// a week with a type per position has no single trained type: what counts as
							// trained is what this player's own position trained. Weeks without them keep
							// answering with their single type, as they always did.
							int trainedType = hasPositionTypes() ? getTypeForPosition(skills[i].getTrainingPosition()) : type;
							if (skills[i].getStamina() - skills[i - 1].getStamina() > 0) {
								trainingSummary.setStaminaPops(trainingSummary.getStaminaPops() + 1);
								if (trainedType == Training.TYPE_STAMINA) {
									trainingSummary.setTrainedSkillsPops(trainingSummary.getTrainedSkillsPops() + 1);
								}
							} else if (skills[i].getStamina() - skills[i - 1].getStamina() < 0) {
								trainingSummary.setStaminaFalls(trainingSummary.getStaminaFalls() + 1);
								if (trainedType == Training.TYPE_STAMINA) {
									trainingSummary.setTrainedSkillsFalls(trainingSummary.getTrainedSkillsFalls() + 1);
								}
							}
							if (skills[i].getPace() - skills[i - 1].getPace() > 0) {
								trainingSummary.setAllSkillsPops(trainingSummary.getAllSkillsPops() + 1);
								if (trainedType == Training.TYPE_PACE) {
									trainingSummary.setTrainedSkillsPops(trainingSummary.getTrainedSkillsPops() + 1);
								}
							} else if (skills[i].getPace() - skills[i - 1].getPace() < 0) {
								trainingSummary.setAllSkillsFalls(trainingSummary.getAllSkillsFalls() + 1);
								if (trainedType == Training.TYPE_PACE) {
									trainingSummary.setTrainedSkillsFalls(trainingSummary.getTrainedSkillsFalls() + 1);
								}
							}
							if (skills[i].getTechnique() - skills[i - 1].getTechnique() > 0) {
								trainingSummary.setAllSkillsPops(trainingSummary.getAllSkillsPops() + 1);
								if (trainedType == Training.TYPE_TECHNIQUE) {
									trainingSummary.setTrainedSkillsPops(trainingSummary.getTrainedSkillsPops() + 1);
								}
							} else if (skills[i].getTechnique() - skills[i - 1].getTechnique() < 0) {
								trainingSummary.setAllSkillsFalls(trainingSummary.getAllSkillsFalls() + 1);
								if (trainedType == Training.TYPE_TECHNIQUE) {
									trainingSummary.setTrainedSkillsFalls(trainingSummary.getTrainedSkillsFalls() + 1);
								}
							}
							if (skills[i].getPassing() - skills[i - 1].getPassing() > 0) {
								trainingSummary.setAllSkillsPops(trainingSummary.getAllSkillsPops() + 1);
								if (trainedType == Training.TYPE_PASSING) {
									trainingSummary.setTrainedSkillsPops(trainingSummary.getTrainedSkillsPops() + 1);
								}
							} else if (skills[i].getPassing() - skills[i - 1].getPassing() < 0) {
								trainingSummary.setAllSkillsFalls(trainingSummary.getAllSkillsFalls() + 1);
								if (trainedType == Training.TYPE_PASSING) {
									trainingSummary.setTrainedSkillsFalls(trainingSummary.getTrainedSkillsFalls() + 1);
								}
							}
							if (skills[i].getKeeper() - skills[i - 1].getKeeper() > 0) {
								trainingSummary.setAllSkillsPops(trainingSummary.getAllSkillsPops() + 1);
								if (trainedType == Training.TYPE_KEEPER) {
									trainingSummary.setTrainedSkillsPops(trainingSummary.getTrainedSkillsPops() + 1);
								}
							} else if (skills[i].getKeeper() - skills[i - 1].getKeeper() < 0) {
								trainingSummary.setAllSkillsFalls(trainingSummary.getAllSkillsFalls() + 1);
								if (trainedType == Training.TYPE_KEEPER) {
									trainingSummary.setTrainedSkillsFalls(trainingSummary.getTrainedSkillsFalls() + 1);
								}
							}
							if (skills[i].getDefender() - skills[i - 1].getDefender() > 0) {
								trainingSummary.setAllSkillsPops(trainingSummary.getAllSkillsPops() + 1);
								if (trainedType == Training.TYPE_DEFENDING) {
									trainingSummary.setTrainedSkillsPops(trainingSummary.getTrainedSkillsPops() + 1);
								}
							} else if (skills[i].getDefender() - skills[i - 1].getDefender() < 0) {
								trainingSummary.setAllSkillsFalls(trainingSummary.getAllSkillsFalls() + 1);
								if (trainedType == Training.TYPE_DEFENDING) {
									trainingSummary.setTrainedSkillsFalls(trainingSummary.getTrainedSkillsFalls() + 1);
								}
							}
							if (skills[i].getPlaymaker() - skills[i - 1].getPlaymaker() > 0) {
								trainingSummary.setAllSkillsPops(trainingSummary.getAllSkillsPops() + 1);
								if (trainedType == Training.TYPE_PLAYMAKING) {
									trainingSummary.setTrainedSkillsPops(trainingSummary.getTrainedSkillsPops() + 1);
								}
							} else if (skills[i].getPlaymaker() - skills[i - 1].getPlaymaker() < 0) {
								trainingSummary.setAllSkillsFalls(trainingSummary.getAllSkillsFalls() + 1);
								if (trainedType == Training.TYPE_PLAYMAKING) {
									trainingSummary.setTrainedSkillsFalls(trainingSummary.getTrainedSkillsFalls() + 1);
								}
							}
							if (skills[i].getScorer() - skills[i - 1].getScorer() > 0) {
								trainingSummary.setAllSkillsPops(trainingSummary.getAllSkillsPops() + 1);
								if (trainedType == Training.TYPE_STRIKER) {
									trainingSummary.setTrainedSkillsPops(trainingSummary.getTrainedSkillsPops() + 1);
								}
							} else if (skills[i].getScorer() - skills[i - 1].getScorer() < 0) {
								trainingSummary.setAllSkillsFalls(trainingSummary.getAllSkillsFalls() + 1);
								if (trainedType == Training.TYPE_STRIKER) {
									trainingSummary.setTrainedSkillsFalls(trainingSummary.getTrainedSkillsFalls() + 1);
								}
							}
							break;
						}
					}
				}
			}
		}
		return trainingSummary;
	}
	
	/** the head coach's level in the skill a training type trains; 0 without one */
	public int getHeadCoachSkill(int type) {
		if (this.getHeadCoach() != null) {
			switch (type) {
			case Training.TYPE_DEFENDING:
				return getHeadCoach().getDefenders();
			case Training.TYPE_KEEPER:
				return getHeadCoach().getKeepers();
			case Training.TYPE_PACE:
				return getHeadCoach().getPace();
			case Training.TYPE_PASSING:
				return getHeadCoach().getPassing();
			case Training.TYPE_PLAYMAKING:
				return getHeadCoach().getPlaymakers();
			case Training.TYPE_STAMINA:
				return getHeadCoach().getStamina();
			case Training.TYPE_STRIKER:
				return getHeadCoach().getScorers();
			case Training.TYPE_TECHNIQUE:
				return getHeadCoach().getTechnique();
			}
		}
		return 0;
	}
}
