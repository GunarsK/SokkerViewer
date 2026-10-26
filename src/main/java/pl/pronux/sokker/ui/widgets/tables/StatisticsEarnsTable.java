package pl.pronux.sokker.ui.widgets.tables;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import pl.pronux.sokker.handlers.SettingsHandler;
import pl.pronux.sokker.model.Club;
import pl.pronux.sokker.model.SokkerDate;
import pl.pronux.sokker.resources.Messages;
import pl.pronux.sokker.ui.beans.ConfigBean;

public class StatisticsEarnsTable extends SVTable<Club> {

	public static final int CLUB_MONEY = 1;

	public StatisticsEarnsTable(Composite parent, int style) {
		super(parent, style);
		this.setFont(ConfigBean.getFontTable());

		this.setHeaderVisible(true);
		this.setLinesVisible(true);
		String[] columns2 = { Messages.getString("table.date"), 
							 Messages.getString("statistics.earns"), 
							 "" 
		};

		for (int i = 0; i < columns2.length; i++) {
			TableColumn column = new TableColumn(this, SWT.NONE);
			column.setText(columns2[i]);
			column.setMoveable(false);
			column.setResizable(false);

			column.setAlignment(SWT.RIGHT);

			if (columns2[i].isEmpty()) {
				if (SettingsHandler.IS_LINUX) {
					column.pack();
				}
			} else {
				column.pack();
			}
		}

	}

	public void fill(Club club) {

		for (int i = club.getClubBudget().size() - 1; i >= 0; i--) {
			TableItem item = new TableItem(this, SWT.NONE);
			item.setData("date", club.getClubBudget().get(i).getDate()); 
			item.setText(0, club.getClubBudget().get(i).getDate().getTrainingDate(SokkerDate.SATURDAY).toDateString());
			item.setText(1, club.getClubBudget().get(i).getMoney().formatIntegerCurrency());
			markSeason(item, club.getClubBudget().get(i).getDate().getSokkerDate().getSeason());
		}

		int[] columns = { 1 };
		this.getChanges(columns);

		this.layoutColumns();
	}

	@Override
	protected void packColumns() {
		packColumns(15);
	}

}
