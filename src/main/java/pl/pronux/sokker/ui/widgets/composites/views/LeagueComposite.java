package pl.pronux.sokker.ui.widgets.composites.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import pl.pronux.sokker.model.LeagueRound;
import pl.pronux.sokker.ui.beans.ConfigBean;
import pl.pronux.sokker.ui.widgets.groups.LeagueTableGroup;

public class LeagueComposite extends Composite {

	private LeagueTableGroup leagueTableGroup;

	public LeagueComposite(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new FormLayout());
		FormData formData = new FormData();
		formData.left = new FormAttachment(50, 0);
		formData.top = new FormAttachment(0, 0);
		Label point = new Label(this, SWT.NONE);
		point.setLayoutData(formData);

		// no size: the group grows to fit the whole table
		formData = new FormData();
		formData.left = new FormAttachment(0, 10);
		formData.top = new FormAttachment(point, 10);

		leagueTableGroup = new LeagueTableGroup(this, SWT.NONE);
		leagueTableGroup.setLayoutData(formData);
		leagueTableGroup.setFont(ConfigBean.getFontTable());
	}

	public void fillLeague(LeagueRound leagueRound) {
		leagueTableGroup.fill(leagueRound);
		layout(true, true);
	}
}
