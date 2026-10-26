package pl.pronux.sokker.ui.widgets.tree;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import pl.pronux.sokker.handlers.SettingsHandler;
import pl.pronux.sokker.model.Coach;
import pl.pronux.sokker.model.SokkerDate;
import pl.pronux.sokker.model.Training;
import pl.pronux.sokker.resources.Messages;
import pl.pronux.sokker.ui.beans.Colors;
import pl.pronux.sokker.ui.beans.ConfigBean;
import pl.pronux.sokker.ui.resources.Fonts;

public class TrainingTree extends Tree {

	/** the spacer column the sokker.org marker is written into; wider than the others to fit it */
	private static final int COLUMN_API = 8;

	@Override
	protected void checkSubclass() {
		// super.checkSubclass();
	}

	public TrainingTree(Composite parent, int style) {
		super(parent, style);

		this.setHeaderVisible(true);
		this.setLinesVisible(true);

		String[] columns = { Messages.getString("table.date"), Messages.getString("formation." + Training.FORMATION_GK), Messages.getString("formation." + Training.FORMATION_DEF),
				Messages.getString("formation." + Training.FORMATION_MID), Messages.getString("formation." + Training.FORMATION_ATT), Messages.getString("table.coach.head"),
				Messages.getString("table.coach.juniors"), Messages.getString("table.coach.assistants"), " ", " ", " ", " ", "" };

		this.setFont(ConfigBean.getFontTable());

		// tworzymy kolumny dla trenerow

		for (int j = 0; j < columns.length; j++) {
			TreeColumn column = new TreeColumn(this, SWT.NONE);

			column.setAlignment(SWT.LEFT);
			column.setText(columns[j]);
			column.setResizable(false);

			if (j > 2) {
				if (columns[j].isEmpty()) {
					if (SettingsHandler.IS_LINUX) {
						column.pack();
					}
				} else {
					column.setWidth(j == COLUMN_API ? 80 : 40);
				}
			} else {
				column.setWidth(120);
			}
		}
	}

	public void fillTrainingTable(List<Training> trainings) {
		// Turn off drawing to avoid flicker
		this.setRedraw(false);

		// We remove all the table entries, sort our
		// rows, then add the entries
		this.removeAll();

		// List the entries using entrySet()
		for (Training training : trainings) {
			TreeItem item = new TreeItem(this, SWT.NONE);
			int c = 0;
			item.setData(Training.class.getName(), training);

			item.setText(c++, training.getDate().getTrainingDate(SokkerDate.THURSDAY).toDateString());
			for (int position = Training.FORMATION_GK; position <= Training.FORMATION_ATT; position++) {
				item.setText(c++, positionTypeText(training, position));
			}
			item.setText(c++, training.getHeadCoach() != null ? "1" : "0");
			item.setText(c++, training.getJuniorCoach() != null ? "1" : "0");
			item.setText(c++, String.valueOf(training.getAssistants().size()));
			if (training.isApiConfirmed()) {
				item.setText(c, Messages.getString("training.source.api"));
			}

			fillTrainingNode(item, training);
		}
		this.setRedraw(true);
	}

	/**
	 * short name of the type the given position trained in that week, empty when not known
	 */
	private String positionTypeText(Training training, int position) {
		int type = training.getEffectiveTypeForPosition(position);
		return type == Training.TYPE_NOT_SET ? "" : Messages.getString("training.type." + type + ".short");
	}

	private void fillTrainingNode(TreeItem treeItem, Training training) {

		treeItem.getParent().setRedraw(false);

		TreeItem item = new TreeItem(treeItem, SWT.NONE);
		item.setBackground(Colors.getTreeHeader());
		fillTreeItemVirtualColumns(item);

		if (training.getHeadCoach() != null) {
			item = new TreeItem(treeItem, SWT.NONE);
			item.setBackground(Colors.getCoachHead());
			Coach coach = training.getHeadCoach();
			fillTreeItem(item, coach, Coach.JOB_HEAD, training);
		}
		if (training.getJuniorCoach() != null) {
			Coach coach = training.getJuniorCoach();
			item = new TreeItem(treeItem, SWT.NONE);
			item.setBackground(Colors.getCoachJuniors());
			fillTreeItem(item, coach, Coach.JOB_JUNIORS, training);
		}

		for (Coach coach : training.getAssistants()) {
			item = new TreeItem(treeItem, SWT.NONE);
			item.setBackground(Colors.getCoachAssistant());
			fillTreeItem(item, coach, Coach.JOB_ASSISTANT, training);
		}

		treeItem.getParent().setRedraw(true);
	}

	private void fillTreeItemVirtualColumns(TreeItem item) {
		int c = 0;

		item.setData(Coach.class.getName(), null);
		item.setText(c++, Messages.getString("table.job"));
		item.setText(c++, Messages.getString("table.name"));
		item.setText(c++, Messages.getString("table.surname"));
		item.setText(c++, Messages.getString("table.generallSkill"));
		item.setText(c++, Messages.getString("table.stamina"));
		item.setText(c++, Messages.getString("table.pace"));
		item.setText(c++, Messages.getString("table.technique"));
		item.setText(c++, Messages.getString("table.passing"));
		item.setText(c++, Messages.getString("table.keeper"));
		item.setText(c++, Messages.getString("table.defender"));
		item.setText(c++, Messages.getString("table.playmaker"));
		item.setText(c++, Messages.getString("table.scorer"));
	}

	private void fillTreeItem(TreeItem item, Coach coach, int job, Training training) {
		int c = 0;

		switch (job) {
		case Coach.JOB_HEAD:
			item.setText(c++, Messages.getString("coach.job.head.short"));
			break;
		case Coach.JOB_JUNIORS:
			item.setText(c++, Messages.getString("coach.job.juniors.short"));
			break;
		case Coach.JOB_ASSISTANT:
			item.setText(c++, Messages.getString("coach.job.assistant.short"));
			break;
		default:
			return;
		}
		item.setData(Coach.class.getName(), coach);
		item.setText(c++, coach.getName());
		item.setText(c++, coach.getSurname());
		item.setText(c++, String.valueOf(coach.getGeneralskill()));
		item.setText(c++, String.valueOf(coach.getStamina()));
		item.setText(c++, String.valueOf(coach.getPace()));
		item.setText(c++, String.valueOf(coach.getTechnique()));
		item.setText(c++, String.valueOf(coach.getPassing()));
		item.setText(c++, String.valueOf(coach.getKeepers()));
		item.setText(c++, String.valueOf(coach.getDefenders()));
		item.setText(c++, String.valueOf(coach.getPlaymakers()));
		item.setText(c++, String.valueOf(coach.getScorers()));

		int boldColumn = job != Coach.JOB_HEAD ? 3 : trainedColumn(training.getEffectiveType());
		if (boldColumn >= 0) {
			item.setFont(boldColumn, Fonts.getBoldFont(this.getDisplay(), this.getFont().getFontData()));
		}
	}

	/** The head coach's column of the skill the week trained, -1 for none */
	private int trainedColumn(int type) {
		switch (type) {
		case Training.TYPE_STAMINA:
			return 4;
		case Training.TYPE_PACE:
			return 5;
		case Training.TYPE_TECHNIQUE:
			return 6;
		case Training.TYPE_PASSING:
			return 7;
		case Training.TYPE_KEEPER:
			return 8;
		case Training.TYPE_DEFENDING:
			return 9;
		case Training.TYPE_PLAYMAKING:
			return 10;
		case Training.TYPE_STRIKER:
			return 11;
		default:
			return -1;
		}
	}
}
