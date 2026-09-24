package pl.pronux.sokker.ui.widgets.styledtexts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;

import pl.pronux.sokker.model.Coach;
import pl.pronux.sokker.model.Training;
import pl.pronux.sokker.resources.Messages;
import pl.pronux.sokker.ui.beans.Colors;
import pl.pronux.sokker.ui.beans.ConfigBean;
import pl.pronux.sokker.ui.resources.ColorResources;
import pl.pronux.sokker.ui.resources.TrainingLabels;

public class TrainingSeasonField extends StyledText implements IDescription {

	@Override
	protected void checkSubclass() {
		// super.checkSubclass();
	}

	public TrainingSeasonField(Composite parent, int style) {
		super(parent, style);
		this.setBackground(parent.getBackground());
		this.setFont(ConfigBean.getFontDescription());
		this.setEnabled(false);
	}
	
	public void setInfo(Training training, int week) {
		String formation;
		String description;
		String assistants;
		String date = String.format("%s %-2d", Messages.getString("training.week"), week + 1);
		this.setRedraw(false);
		this.setText("");
		if (training == null) {
			this.append(String.format("%s %-30s", date, Messages.getString("training.notavailable")));
			this.addStyle(date.length(), this.getText().length() - date.length(), Colors.getGray(), this.getBackground(), SWT.NORMAL);
		} else {
			if (training.getHeadCoach() != null && training.getAssistants().size() < 4) {
				description = TrainingLabels.describeWithHeadCoach(training);

				this.append(String.format("%s %s", date, description));

				assistants = "";
				if (training.getAssistants().size() > 0) {
					for (int i = 0; i < training.getAssistants().size(); i++) {
						Coach trainer = training.getAssistants().get(i);
						if (i == training.getAssistants().size() - 1) {
							assistants += trainer.getGeneralskill();
						} else {
							assistants += String.format("%d, ", trainer.getGeneralskill());
						}
					}
					this.append(String.format(description.isEmpty() ? "[%s]" : ", [%s]", assistants));
				}

		
			} else {
				this.append(String.format("%s %s", date, Messages.getString("training.failed")));
				this.addStyle(date.length(), this.getText().length() - date.length(), ColorResources.getRed(), this.getBackground(), SWT.NORMAL);
			}
			if (training.getJuniorCoach() != null) {
				this.append(String.format(" jr[%d]", training.getJuniorCoach().getGeneralskill()));
			}

		}

		this.setRedraw(true);
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
