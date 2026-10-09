package pl.pronux.sokker.ui.widgets.tables;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import pl.pronux.sokker.handlers.SettingsHandler;
import pl.pronux.sokker.model.League;
import pl.pronux.sokker.model.Match;
import pl.pronux.sokker.resources.Messages;
import pl.pronux.sokker.ui.beans.Colors;
import pl.pronux.sokker.ui.beans.ConfigBean;
import pl.pronux.sokker.ui.managers.MatchUIManager;
import pl.pronux.sokker.ui.resources.Fonts;

public class MatchesTable extends SVTable<Match> {

	private MatchUIManager matchUIManager = MatchUIManager.instance();

	public MatchesTable(Composite parent, int style) {
		super(parent, style);

		String[] columns = {
				"",
				Messages.getString("table.date"),
				Messages.getString("table.week"),
				Messages.getString("table.team.home"),
				Messages.getString("table.match.result"),
				Messages.getString("table.team.away"),
				""
		};

		for (int i = 0; i < columns.length; i++) {
			TableColumn column = new TableColumn(this, SWT.LEFT);
			column.setText(columns[i]);
			column.setResizable(false);
			column.setMoveable(false);
			if (i == 0) {
				column.setWidth(25);
			} else if (i == columns.length - 1) {
				if (SettingsHandler.IS_LINUX) {
					column.pack();
				}
			} else {
				column.pack();
			}
		}
		this.setLinesVisible(true);
		this.setBackground(parent.getBackground());
		this.setFont(ConfigBean.getFontTable());
		this.setHeaderVisible(true);
	}

	public void fill(int teamID, List<Match> matches) {
		this.setRedraw(false);

		this.remove(0, this.getItemCount() - 1);

		int c;
		for (Match match : matches) {
			c = 0;
			if (match.getIsFinished() == Match.FINISHED) {

				TableItem item = new TableItem(this, SWT.NONE);
				item.setData(Match.class.getName(), match);

				if (match.getLeague() != null) {
					League league = match.getLeague();
					item.setImage(matchUIManager.getMatchImage(league));
					item.setBackground(matchUIManager.getMatchColor(league));
				}
				c++;

				
				item.setText(c++, match.getDateStarted().toDateString());
				item.setText(c++, String.valueOf(match.getWeek()));
				item.setText(c++, match.getHomeTeamName());

				if (match.getHomeTeamScore() < 0 || match.getAwayTeamScore() < 0) {
					c++;
				} else {
					item.setFont(c, Fonts.getBoldFont(this.getDisplay(), this.getFont().getFontData()));
					if (match.getHomeTeamScore() > match.getAwayTeamScore()) {
						if(match.getHomeTeamId() == teamID) {
							item.setForeground(c, Colors.getMatchWin());	
						} else {
							item.setForeground(c, Colors.getMatchLost());
						}
					} else if (match.getHomeTeamScore() < match.getAwayTeamScore()) {
						if(match.getHomeTeamId() == teamID) {
							item.setForeground(c, Colors.getMatchLost());	
						} else {
							item.setForeground(c, Colors.getMatchWin());
						}
					} else {
						item.setForeground(c, Colors.getMatchDraw());
					}
					item.setText(c++, match.getHomeTeamScore() + " : " + match.getAwayTeamScore()); 
				}
				
				item.setText(c++, match.getAwayTeamName());
			}
		}

		this.layoutColumns();

		this.setRedraw(true);
	}

	@Override
	protected void packColumns() {
		packColumns(3, 0);
	}

}
