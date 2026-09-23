package pl.pronux.sokker.ui.widgets.tables;


import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import pl.pronux.sokker.handlers.SettingsHandler;
import pl.pronux.sokker.model.League;
import pl.pronux.sokker.model.Player;
import pl.pronux.sokker.model.PlayerSkills;
import pl.pronux.sokker.model.PlayerStats;
import pl.pronux.sokker.model.SokkerDate;
import pl.pronux.sokker.model.Training;
import pl.pronux.sokker.resources.Messages;
import pl.pronux.sokker.ui.beans.Colors;
import pl.pronux.sokker.ui.beans.ConfigBean;
import pl.pronux.sokker.ui.handlers.DisplayHandler;
import pl.pronux.sokker.ui.resources.ColorResources;
import pl.pronux.sokker.ui.resources.Fonts;
import pl.pronux.sokker.ui.resources.ImageResources;

public class PlayerTable extends SVTable<Player> {

	public static final int VALUE = 1;
	
	public static final int SALARY = 2;
	
	public static final int AGE = 3;

	public static final int FORM = 4;

	public static final int STAMINA = 5;

	public static final int PACE = 6;

	public static final int TECHNIQUE = 7;

	public static final int PASSING = 8;

	public static final int KEEPER = 9;

	public static final int DEFENDER = 10;

	public static final int PLAYMAKER = 11;

	public static final int SCORER = 12;

	public static final int DISCIPLINE = 13;

	public static final int EXPERIENCE = 14;

	/** the last column with a number in every row, the last one a click can draw a graph of */
	public static final int TEAMWORK = 15;

	public static final int MATCH_INDEX_1ST = 22;
	public static final int MATCH_INDEX_2ND = MATCH_INDEX_1ST + 1;
	public static final int MATCH_INDEX_3RD = MATCH_INDEX_2ND + 1;
	
	
	public PlayerTable(Composite parent, int style) {
		super(parent, style);
		this.setVisible(false);
		this.setLinesVisible(true);
		this.setHeaderVisible(true);
		this.setFont(ConfigBean.getFontTable());

		String[] titles = new String[] {
				Messages.getString("table.date"), 
				Messages.getString("table.value"), 
				Messages.getString("table.salary"), 
				Messages.getString("table.age"), 
				//Messages.getString("table.weight"),
				//Messages.getString("table.bmi"), 
				Messages.getString("table.form"), 
				Messages.getString("table.stamina"), 
				Messages.getString("table.pace"), 
				Messages.getString("table.technique"), 
				Messages.getString("table.passing"), 
				Messages.getString("table.keeper"), 
				Messages.getString("table.defender"), 
				Messages.getString("table.playmaker"), 
				Messages.getString("table.scorer"), 
				Messages.getString("table.discipline"), 
				Messages.getString("table.experience"), 
				Messages.getString("table.teamwork"), 
				Messages.getString("table.formation"), 
				Messages.getString("table.training.type"),
				Messages.getString("table.training.slot"),
				Messages.getString("table.injury"),
				Messages.getString("table.training.intensity"),
				Messages.getString("table.minutes"),
				Messages.getString("table.1st"), 
				Messages.getString("table.2nd"), 
				Messages.getString("table.3rd"), 
				"" 
		};
		for (int j = 0; j < titles.length; j++) {
			TableColumn column = new TableColumn(this, SWT.RIGHT);
			column.setText(titles[j]);
			column.setResizable(false);
			column.setMoveable(false);
			if (titles[j].isEmpty()) {
				// column.setWidth(70);
				if (SettingsHandler.IS_LINUX) {
					column.pack();
				}
			} else {
				// column.setWidth(40);
				column.pack();
			}
		}
	}

	public void fill(Player player) {
		int max = 0;
		
		max = player.getSkills().length;
		for (int i = max - 1; i >= 0; i--) {
			int c = 0;
			TableItem item = new TableItem(this, SWT.NONE);
			if(!player.getSkills()[i].isPassTraining()) {
				item.setForeground(ColorResources.getDarkGray());
			}
			item.setData("date", player.getSkills()[i].getDate()); 
			item.setData("player_skill", player.getSkills()[i]); 
			item.setText(c++, player.getSkills()[i].getDate().getTrainingDate(SokkerDate.THURSDAY).toDateString());
			item.setText(c++, player.getSkills()[i].getValue().formatIntegerCurrency());
			item.setText(c++, player.getSkills()[i].getSalary().formatIntegerCurrency());
			item.setText(c++, String.valueOf(player.getSkills()[i].getTrainingAge()));
			//item.setText(c++, String.valueOf(player.getSkills()[i].getWeight()));
			//item.setText(c++, String.valueOf(player.getSkills()[i].getBmi()));
			item.setText(c++, String.valueOf(player.getSkills()[i].getForm()));
			item.setText(c++, String.valueOf(player.getSkills()[i].getStamina()));
			item.setText(c++, String.valueOf(player.getSkills()[i].getPace()));
			item.setText(c++, String.valueOf(player.getSkills()[i].getTechnique()));
			item.setText(c++, String.valueOf(player.getSkills()[i].getPassing()));
			item.setText(c++, String.valueOf(player.getSkills()[i].getKeeper()));
			item.setText(c++, String.valueOf(player.getSkills()[i].getDefender()));
			item.setText(c++, String.valueOf(player.getSkills()[i].getPlaymaker()));
			item.setText(c++, String.valueOf(player.getSkills()[i].getScorer()));
			item.setText(c++, String.valueOf(player.getSkills()[i].getDiscipline()));
			item.setText(c++, String.valueOf(player.getSkills()[i].getExperience()));
			item.setText(c++, String.valueOf(player.getSkills()[i].getTeamwork()));
			PlayerSkills skills = player.getSkills()[i];
			Training training = skills.getTraining();
			int position = skills.getTrainingPosition();
			int positionType = training != null ? training.getTypeForPosition(position) : Training.TYPE_NOT_SET;

			if (position != Training.POSITION_NOT_SET && positionType != Training.TYPE_NOT_SET) {
				// the position this player trained as, and the type trained for it
				item.setText(c++, Messages.getString("formation." + position));
				item.setText(c++, Messages.getString("training.type." + positionType + ".short"));
			} else if (training != null) {
				// no type for this player's position: fall back to the week's single type
				int weekType = training.getEffectiveType();
				if (weekType == Training.TYPE_PACE || weekType == Training.TYPE_STAMINA) {
					item.setText(c++, Messages.getString("formation." + Training.FORMATION_ALL));
				} else {
					item.setText(c++, Messages.getString("formation." + training.getFormation()));
				}
				item.setText(c++, Messages.getString("training.type." + weekType + ".short"));
			} else {
				item.setText(c++, "");
				item.setText(c++, "");
			}

			if (skills.getTrainingSlot() == Training.SLOT_ADVANCED) {
				item.setText(c++, Messages.getString("training.slot.advanced"));
			} else if (skills.getTrainingSlot() == Training.SLOT_FORMATION) {
				item.setText(c++, Messages.getString("training.slot.formation"));
			} else if (skills.getTrainingSlot() == Training.SLOT_MISSING) {
				item.setText(c++, Messages.getString("training.slot.missing"));
			} else {
				item.setText(c++, "");
			}
			// injured on the day of the training; a week sokker.org did not report shows the injury the row was synced with
			double injury = skills.getTrainingInjuryDays() >= 0 ? skills.getTrainingInjuryDays() : skills.getInjurydays();
			if (injury > 0) {
				item.setImage(c, ImageResources.getImageResources("injury.png"));
			}
			item.setText(c++, injury > 0 ? String.valueOf((int) Math.ceil(injury)) : "");
			item.setText(c++, skills.getTrainingIntensity() < 0 ? "" : skills.getTrainingIntensity() + "%");
			item.setText(c++, skills.getMinutesOfficial() < 0 ? ""
					: String.format("%d'/%d'/%d'", skills.getMinutesOfficial(), skills.getMinutesFriendly(), skills.getMinutesNational()));

			if (player.getPlayerMatchStatistics() != null) {
				int week = player.getSkills()[i].getDate().getTrainingDate(SokkerDate.THURSDAY).getSokkerDate().getWeek();
				for (PlayerStats playerStats : player.getPlayerMatchStatistics()) {
					if ((playerStats.getMatch().getWeek() == week && playerStats.getMatch().getDay() < 6) ||
						(playerStats.getMatch().getWeek() == week - 1 && playerStats.getMatch().getDay() == 6)) {
						
						if (playerStats.getFormation() >= 0 && playerStats.getFormation() <= 4 && playerStats.getTimePlayed() > 0) {
							League league = playerStats.getMatch().getLeague();
							int matchDay = playerStats.getMatch().getDay();

							int idx = 0;
							if (matchDay == 6) {
								idx = MATCH_INDEX_1ST;
							} else if (matchDay == 1) {
								idx = MATCH_INDEX_2ND;
							} else if (matchDay == 4) {
								idx = MATCH_INDEX_3RD;
							}

							if (idx > 0) {
								if ((league.getType() == League.TYPE_LEAGUE || league.getType() == League.TYPE_PLAYOFF) && league.getIsOfficial() == League.OFFICIAL) {
									item.setFont(idx, Fonts.getBoldFont(DisplayHandler.getDisplay(), item.getFont(idx).getFontData()));
								}
								item.setText(idx, String.format("%s (%d')", Messages.getString("formation." + playerStats.getFormation()) , playerStats.getTimePlayed()));
								if (playerStats.getFormation() == PlayerStats.GK) {
									item.setBackground(idx, Colors.getPositionGK());
								} else if (playerStats.getFormation() == PlayerStats.DEF) {
									item.setBackground(idx, Colors.getPositionDEF());
								} else if (playerStats.getFormation() == PlayerStats.MID) {
									item.setBackground(idx, Colors.getPositionMID());
								} else if (playerStats.getFormation() == PlayerStats.ATT) {
									item.setBackground(idx, Colors.getPositionATT());
								}
							}
							
						}
					}
				}
			} else {
				item.setText(MATCH_INDEX_1ST, ""); 
				item.setText(MATCH_INDEX_2ND, ""); 
				item.setText(MATCH_INDEX_3RD, ""); 
			}

			if (i > 0) {
				PlayerSkills now = player.getSkills()[i];
				PlayerSkills before = player.getSkills()[i - 1];
				int idx = 1;
				compare(now.getValue().toInt(), before.getValue().toInt(), item, idx++);
				compare(now.getSalary().toInt(), before.getSalary().toInt(), item, idx++);
				compare(now.getTrainingAge(), before.getTrainingAge(), item, idx++);
				//compare(now.getWeight(), before.getWeight(), item, idx++);
				//compare(now.getBmi(), before.getBmi(), item, idx++);
				compare(now.getForm(), before.getForm(), item, idx++);
				compare(now.getStamina(), before.getStamina(), item, idx++);
				compare(now.getPace(), before.getPace(), item, idx++);
				compare(now.getTechnique(), before.getTechnique(), item, idx++);
				compare(now.getPassing(), before.getPassing(), item, idx++);
				compare(now.getKeeper(), before.getKeeper(), item, idx++);
				compare(now.getDefender(), before.getDefender(), item, idx++);
				compare(now.getPlaymaker(), before.getPlaymaker(), item, idx++);
				compare(now.getScorer(), before.getScorer(), item, idx++);
				compare(now.getDiscipline(), before.getDiscipline(), item, idx++);
				compare(now.getExperience(), before.getExperience(), item, idx++);
				compare(now.getTeamwork(), before.getTeamwork(), item, idx++);
			}
		}

		for (int i = 0; i < this.getColumnCount() - 1; i++) {
			this.getColumn(i).pack();
//			this.getColumn(i).setWidth(this.getColumn(i).getWidth() + 5);
		}

		if (this.getColumn(MATCH_INDEX_1ST).getWidth() < this.getColumn(MATCH_INDEX_2ND).getWidth()) {
			this.getColumn(MATCH_INDEX_1ST).setWidth(this.getColumn(MATCH_INDEX_2ND).getWidth());
		} else {
			this.getColumn(MATCH_INDEX_2ND).setWidth(this.getColumn(MATCH_INDEX_1ST).getWidth());
		}
	}

	private void compare(double now, double before, TableItem item, int index) {
		if (before < now) {
			item.setBackground(index, ConfigBean.getColorIncrease());
		} else if (before > now) {
			item.setBackground(index, ConfigBean.getColorDecrease());
		}
	}
}
