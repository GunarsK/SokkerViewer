package pl.pronux.sokker.ui.widgets.tables;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import pl.pronux.sokker.actions.PlayersManager;
import pl.pronux.sokker.comparators.PlayerStatsComparator;
import pl.pronux.sokker.handlers.SettingsHandler;
import pl.pronux.sokker.interfaces.SVComparator;
import pl.pronux.sokker.model.League;
import pl.pronux.sokker.model.Match;
import pl.pronux.sokker.model.PlayerStats;
import pl.pronux.sokker.resources.Messages;
import pl.pronux.sokker.ui.beans.ConfigBean;
import pl.pronux.sokker.ui.listeners.PaintStarListener;
import pl.pronux.sokker.ui.listeners.SortTableListener;
import pl.pronux.sokker.ui.managers.MatchUIManager;
import pl.pronux.sokker.ui.resources.ColorResources;
import pl.pronux.sokker.ui.resources.ImageResources;
import pl.pronux.sokker.ui.widgets.interfaces.IViewSort;
import pl.pronux.sokker.ui.widgets.shells.BugReporter;

public class PlayerStatsTable extends SVTable<PlayerStats> implements IViewSort<PlayerStats> {

	private PlayersManager playersManager = PlayersManager.getInstance();

	private MatchUIManager matchUIManager = MatchUIManager.instance();

	private PlayerStatsComparator comparator;

	private List<PlayerStats> playerStats = new ArrayList<PlayerStats>();

	public PlayerStatsTable(Composite parent, int style) {
		super(parent, style);
		comparator = new PlayerStatsComparator();
		comparator.setDirection(PlayerStatsComparator.DESCENDING);
		comparator.setColumn(PlayerStatsComparator.DATE);

		this.setHeaderVisible(true);
		this.setLinesVisible(true);
		this.setFont(ConfigBean.getFontTable());
		this.setBackground(parent.getBackground());
		String[] columns = { "", 
							Messages.getString("table.date"), 
							Messages.getString("table.team.home"), 
							Messages.getString("table.team.away"), 
							Messages.getString("table.match.formation"), 
							Messages.getString("table.match.time"), 
							Messages.getString("table.match.rating"), 
							Messages.getString("table.match.rating"), 
							Messages.getString("table.match.goals"), 
							Messages.getString("table.match.shoots"), 
							Messages.getString("table.match.assists"), 
							Messages.getString("table.match.fouls"), 
							Messages.getString("table.match.injury"), 
							Messages.getString("table.match.cards"), 
							" " 
		};

		for (int i = 0; i < columns.length; i++) {
			TableColumn column = new TableColumn(this, SWT.LEFT);
			column.setText(columns[i]);

			if (i == columns.length - 1) {
				if (SettingsHandler.IS_LINUX) {
					column.pack();
				}
			} else if (i == PlayerStatsComparator.STARS) {
				column.setWidth(120);
			} else if (i == 0) {
				column.setWidth(22);
			} else {
				column.pack();
			}
		}

		for (int i = 1; i < this.getColumnCount() - 1; i++) {
			this.getColumn(i).addSelectionListener(new SortTableListener<PlayerStats>(this, comparator));
		}

		this.setSortColumn(this.getColumn(PlayerStatsComparator.DATE));
		this.setSortDirection(PlayerStatsComparator.DESCENDING);

		final TableEditor editor = new TableEditor(this);
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;

		this.addListener(SWT.PaintItem, new PaintStarListener(PlayerStatsComparator.STARS));
		this.addListener(SWT.MouseDown, new Listener() {

			public void handleEvent(Event event) {
				Point pt = new Point(event.x, event.y);
				final TableItem item = PlayerStatsTable.this.getItem(pt);
				if (item != null) {
					if (((PlayerStats) item.getData(PlayerStats.class.getName())).getIsInjured() == PlayerStats.NOT_INJURED) {
						return;
					}
					Rectangle rect = item.getBounds(PlayerStatsComparator.INJURY);
					if (rect.contains(pt)) {
						final Text text = new Text(PlayerStatsTable.this, SWT.RIGHT);
						text.setTextLimit(2);
						text.setFont(ConfigBean.getFontTable());

						editor.setEditor(text, item, PlayerStatsComparator.INJURY);
						if (item.getData(PlayerStats.class.getName()) != null && item.getData(PlayerStats.class.getName()) instanceof PlayerStats) {
							text.setText(String.valueOf(((PlayerStats) item.getData(PlayerStats.class.getName())).getInjuryDays()));
						} else {
							text.setText(item.getText(PlayerStatsComparator.INJURY));
						}

						Listener textListener = new Listener() {

							public void handleEvent(final Event e) {
								switch (e.type) {
								case SWT.FocusOut:
									saveInjury(item, text, "PlayerStatsTable -> injury1");
									text.dispose();
									break;
								case SWT.Traverse:
									switch (e.detail) {
									case SWT.TRAVERSE_RETURN:
										saveInjury(item, text, "PlayerStatsTable -> injury2");
										break;
									case SWT.TRAVERSE_ESCAPE:
										text.dispose();
										e.doit = false;
										break;
									}
									break;
								case SWT.Verify:
									String string = e.text;
									char[] chars = new char[string.length()];
									string.getChars(0, chars.length, chars, 0);
									for (int j = 0; j < chars.length; j++) {
										if (!('0' <= chars[j] && chars[j] <= '9')) {
											e.doit = false;
											return;
										}
									}
									break;
								}
							}
						};
						text.addListener(SWT.FocusOut, textListener);
						text.addListener(SWT.Traverse, textListener);
						text.addListener(SWT.Verify, textListener);

						text.selectAll();
						text.setFocus();
					}
				}

			}
		});

	}

	public void fill(List<PlayerStats> alPlayerStats) {
		this.setRedraw(false);
		this.remove(0, this.getItemCount() - 1);
		if (alPlayerStats == null) {
			this.setRedraw(true);
			return;
		}
		Collections.sort(alPlayerStats, comparator);
		this.playerStats = alPlayerStats;
		for (PlayerStats playerStats : alPlayerStats) {
			if (playerStats.getTimePlayed() > 0) {
				Match match = playerStats.getMatch();
				TableItem item = new TableItem(this, SWT.NONE);
				item.setData(PaintStarListener.class.getName(), playerStats.getRating());
				item.setData(PlayerStats.class.getName(), playerStats);
				int i = 0;
				if (match.getLeague() != null) {
					League league = match.getLeague();
					item.setImage(matchUIManager.getMatchImage(league));
					item.setBackground(matchUIManager.getMatchColor(league));
				}

				i++;

				item.setText(i++, match.getDateStarted().toDateString());
				item.setText(i++, match.getHomeTeamName());
				item.setText(i++, match.getAwayTeamName());

				if (playerStats.getFormation() >= 0 && playerStats.getFormation() <= 4) {
					item.setText(i++, Messages.getString("formation." + playerStats.getFormation())); 
				} else {
					item.setText(i++, ""); 
				}

				if (playerStats.getTimePlayed() == 0) {
					item.setForeground(ColorResources.getDarkGray());
				}
				item.setText(i++, playerStats.getTimePlayed() + "'"); 

				item.setText(i++, playerStats.getRating() + "%"); 
				i++;
				item.setText(i++, String.valueOf(playerStats.getGoals()));
				item.setText(i++, String.valueOf(playerStats.getShoots()));
				item.setText(i++, String.valueOf(playerStats.getAssists()));
				item.setText(i++, String.valueOf(playerStats.getFouls()));

				item.setText(i, ""); 
				if (SettingsHandler.IS_WINDOWS) {
					item.setBackground(i, this.getBackground());
				}

				if (playerStats.getIsInjured() == PlayerStats.INJURED) {
					if (playerStats.getInjuryDays() != 0) {
						item.setText(i, String.valueOf(playerStats.getInjuryDays()));
					}
					item.setImage(i++, ImageResources.getImageResources("injury.png")); 
				} else {
					i++;
				}

				item.setText(i, ""); 
				if (SettingsHandler.IS_WINDOWS) {
					item.setBackground(i, this.getBackground());
				}
				if (playerStats.getYellowCards() < 2 && playerStats.getRedCards() > 0) {
					item.setImage(i++, ImageResources.getImageResources("red_card.png")); 
				} else if (playerStats.getYellowCards() > 1 && playerStats.getRedCards() > 0) {
					item.setImage(i++, ImageResources.getImageResources("2_yellow_cards_1_red_card.png")); 
				} else if (playerStats.getYellowCards() == 1 && playerStats.getRedCards() < 1) {
					item.setImage(i++, ImageResources.getImageResources("yellow_card.png")); 
				} else if (playerStats.getYellowCards() > 1 && playerStats.getRedCards() < 1) {
					item.setImage(i++, ImageResources.getImageResources("2_yellow_cards.png")); 
				} else {
					i++;
				}

			}
		}
		this.layoutColumns();
		this.setRedraw(true);
	}

	@Override
	protected void packColumns() {
		packColumns(15, 0, PlayerStatsComparator.STARS);
	}

	/** Saves the injury days typed into the editor when they changed */
	private void saveInjury(TableItem item, Text text, String source) {
		if (text.getText().isEmpty()) {
			text.setText("0");
		}
		String shown = item.getText(PlayerStatsComparator.INJURY);
		int before = shown.isEmpty() ? 0 : Integer.valueOf(shown.replaceAll("[^0-9]", "")).intValue();
		int value = Integer.valueOf(text.getText().replaceAll("[^0-9]", "")).intValue();
		if (before != value) {
			PlayerStats stats = (PlayerStats) item.getData(PlayerStats.class.getName());
			stats.setInjuryDays(value);
			try {
				playersManager.updatePlayerStatsInjury(stats);
			} catch (SQLException e) {
				new BugReporter(getDisplay()).openErrorMessage(source, e);
			}
			item.setText(PlayerStatsComparator.INJURY, value != 0 ? String.valueOf(value) : "");
			repackInjury();
		}
	}

	/** Fits the injury column after an edit, keeping hidden columns hidden */
	private void repackInjury() {
		TableColumn column = this.getColumn(PlayerStatsComparator.INJURY);
		column.pack();
		column.setWidth(column.getWidth() + 15);
		this.hideColumns();
	}

	@Override
	public void sort(SVComparator<PlayerStats> comparator) {
		Collections.sort(playerStats, comparator);
		fill(playerStats);
	}

	@Override
	public SVComparator<PlayerStats> getComparator() {
		return comparator;
	}
}
