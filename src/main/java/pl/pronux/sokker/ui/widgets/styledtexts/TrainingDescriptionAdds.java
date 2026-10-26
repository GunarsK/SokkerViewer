package pl.pronux.sokker.ui.widgets.styledtexts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;

import pl.pronux.sokker.bean.TrainingSummary;
import pl.pronux.sokker.model.SokkerDate;
import pl.pronux.sokker.model.Training;
import pl.pronux.sokker.resources.Messages;
import pl.pronux.sokker.ui.beans.Colors;
import pl.pronux.sokker.ui.beans.ConfigBean;

public class TrainingDescriptionAdds extends StyledText implements IDescription {

	@Override
	protected void checkSubclass() {
		// super.checkSubclass();
	}

	public TrainingDescriptionAdds(Composite parent, int style) {
		super(parent, style);
		this.setBackground(parent.getBackground());
		this.setFont(ConfigBean.getFontDescription());
	}

	public void setInfo(Training training) {
		this.setRedraw(false);
		this.setText("");
		addColored(String.format("%-25s%-15s", Messages.getString("training.date"), training.getDate().getTrainingDate(SokkerDate.THURSDAY).toDateString()), Colors.getMutedText());
		this.append(NEW_LINE);

		addColored(String.format("%-25s%-15s", Messages.getString("training.date.insert"), training.getDate().toDateString()), Colors.getMutedText());
		this.append(NEW_LINE);

		addColored(String.format("%-25s%-15d", Messages.getString("training.week"), training.getDate().getTrainingDate(SokkerDate.THURSDAY).getSokkerDate().getSeasonWeek() + 1), Colors.getMutedText());
		this.append(NEW_LINE);

		this.append(NEW_LINE);

		if (training.getJuniorCoach() != null) {
			String skill = getSkill(training.getJuniorCoach().getGeneralskill());
			this.append(String.format("%-25s %s", Messages.getString("coach.job.juniors"), skill));
			this.addStyle(this.getText().length() - skill.length(), 4, Colors.getMutedText(), this.getBackground(), SWT.NORMAL);
			this.addStyle(this.getText().length() - skill.length() + 4, skill.length() - 4, Colors.getTrainerGeneralSkill(), this.getBackground(), SWT.BOLD);
		} else {
			addColored(String.format("%-25s %s", Messages.getString("coach.job.juniors"), "-"), Colors.getMutedText());
		}
		this.append(NEW_LINE);
		this.append(NEW_LINE);
		this.append(NEW_LINE);
		this.append(NEW_LINE);
		this.append(NEW_LINE);
		this.append(NEW_LINE);

		this.append(String.format("%s:", Messages.getString("summary")));
		this.append(NEW_LINE);
		this.append(NEW_LINE);

		TrainingSummary trainedSkillSummary = training.getTrainingSummary();
		addSummaryRow("training.skill.stamina", trainedSkillSummary.getStaminaPops(), trainedSkillSummary.getStaminaFalls());
		addSummaryRow("training.skill.trained", trainedSkillSummary.getTrainedSkillsPops(), trainedSkillSummary.getTrainedSkillsFalls());
		addSummaryRow("training.skill.others", trainedSkillSummary.getOthersSkillsPops(), trainedSkillSummary.getOthersSkillsFalls());
		addSummaryRow("training.skill.juniors", trainedSkillSummary.getJuniorsPops(), trainedSkillSummary.getJuniorsFalls());

		this.setRedraw(true);
	}

	/** One summary line: the label, then pops in green and falls in red, "-" for none */
	private void addSummaryRow(String key, int pops, int falls) {
		if (pops > 0 || falls > 0) {
			this.append(String.format("%-25s", Messages.getString(key)));
			if (pops > 0) {
				addColored(String.format("+%d", pops), Colors.getGreenText());
			} else {
				this.append("-");
			}
			this.append("/");
			if (falls > 0) {
				addColored(String.format("-%d", falls), Colors.getRedText());
			} else {
				this.append("-");
			}
		} else {
			this.append(String.format("%-25s -/-", Messages.getString(key)));
		}
		this.append(NEW_LINE);
	}

	private void addColored(String text, Color foreground) {
		this.append(text);
		this.addStyle(this.getText().length() - text.length(), text.length(), foreground, this.getBackground(), SWT.NORMAL);
	}

	private String getSkill(int skill) {
		return String.format("%4s%s", String.format("[%d]", skill), Messages.getString("skill.a" + skill));  
	}

	private void addStyle(int start, int length, Color foreground, Color background, int style) {
		StyleRange range = new StyleRange();
		range.foreground = foreground;
		range.background = background;
		range.start = start;
		range.fontStyle = style;
		range.length = length;
		this.setStyleRange(range);
	}

}
