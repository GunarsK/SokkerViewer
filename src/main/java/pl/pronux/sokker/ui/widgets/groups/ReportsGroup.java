package pl.pronux.sokker.ui.widgets.groups;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import pl.pronux.sokker.model.Report;
import pl.pronux.sokker.resources.Messages;
import pl.pronux.sokker.ui.beans.Colors;
import pl.pronux.sokker.ui.beans.ConfigBean;

public class ReportsGroup extends Group {
	private StyledText styledText;

	public ReportsGroup(Composite arg0, int arg1) {
		super(arg0, arg1);
		this.setText(Messages.getString("ReportsGroup.reports")); 
		setLayout(new FillLayout());
		styledText = new StyledText(this, SWT.H_SCROLL | SWT.READ_ONLY);
		this.setFont(ConfigBean.getFontMain());
		this.setForeground(Colors.getBlueDescription());
		styledText.setBackground(this.getBackground());
		styledText.setFont(ConfigBean.getFontMain());
	}

	public void fill(List<Report> reports) {
		styledText.setText(""); 
		int i = 0;
		while ( i < 11 && i < reports.size() ) {
			Report report = reports.get(i);
			int start = styledText.getText().length();
			addText(String.format("%s %s\r\n", report.getDate().toDateTimeString(), report.getMessage())); 
			int length = styledText.getText().length() - start;
			
			Color color = statusColor(report.getStatus());
			if (color != null) {
				styledText.setStyleRange(new StyleRange(start + 19, length - 19, color, styledText.getBackground(), report.isChecked() ? SWT.NORMAL : SWT.BOLD));
			}
			i++;
		}
	}

	/** Text colour of a report's message by its status, null for none */
	private Color statusColor(int status) {
		if (status == Report.COST) {
			return Colors.getRedText();
		} else if (status == Report.INCOME) {
			return Colors.getGreenText();
		} else if (status == Report.INFO) {
			return Colors.getInfoText();
		}
		return null;
	}

	private void addText(String text) {
		styledText.append(text);
	}

	@Override
	protected void checkSubclass() {
		// super.checkSubclass();
	}

}
