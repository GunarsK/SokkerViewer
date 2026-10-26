package pl.pronux.sokker.ui.widgets.groups;

import java.sql.SQLException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import pl.pronux.sokker.actions.PlayersManager;
import pl.pronux.sokker.data.cache.Cache;
import pl.pronux.sokker.model.PlayerArchive;
import pl.pronux.sokker.resources.Messages;
import pl.pronux.sokker.ui.beans.Colors;
import pl.pronux.sokker.ui.beans.ConfigBean;
import pl.pronux.sokker.ui.resources.FlagsResources;
import pl.pronux.sokker.ui.widgets.composites.CountryChooser;
import pl.pronux.sokker.ui.widgets.dialogs.MessageDialog;
import pl.pronux.sokker.ui.widgets.shells.BugReporter;

public class ArchiveInformationGroup extends Group {

	private PlayersManager playersManager = PlayersManager.getInstance();

	private Text idText;
	private Text nameText;
	private Text surnameText;
	private Text youthTeamIdText;
	private Button saveButton;
	private PlayerArchive playerArchive;
	private Label countryImageLabel;
	private Listener countryListener;
	private Label nameWarningLabel;
	private Label surnameWarningLabel;
	private Label countryWarningLabel;

	@Override
	protected void checkSubclass() {
		// super.checkSubclass();
	}

	public ArchiveInformationGroup(Composite parent, int style) {
		super(parent, style & ~SWT.BORDER);

		GridLayout layout = new GridLayout();
		layout.numColumns = 3;

		this.setLayout(layout);
		this.setFont(ConfigBean.getFontMain());
		this.setText(Messages.getString("archive.group.information.text"));
		this.setForeground(Colors.getBlueDescription());

		GridData warningGD = new GridData();
		warningGD.widthHint = 5;

		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;

		addLabel("player.id");
		idText = addField(gridData);
		addWarning(warningGD);

		addLabel("player.name");
		nameText = addField(gridData);
		nameWarningLabel = addWarning(warningGD);

		addLabel("player.surname");
		surnameText = addField(gridData);
		surnameWarningLabel = addWarning(warningGD);

		addLabel("player.youth.team.id");
		youthTeamIdText = addField(gridData);
		addWarning(warningGD);

		addLabel("player.country");

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.CENTER;
		gridData.widthHint = 20;

		countryImageLabel = new Label(this, SWT.NONE);
		countryImageLabel.setLayoutData(gridData);

		countryWarningLabel = addWarning(warningGD);

		gridData = new GridData();
		gridData.widthHint = 80;
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.CENTER;

		saveButton = new Button(this, SWT.PUSH);
		saveButton.setLayoutData(gridData);
		saveButton.setText(Messages.getString("button.save"));
		saveButton.setEnabled(false);
		saveButton.setFont(this.getFont());

		saveButton.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event arg0) {
				if (playerArchive != null) {
					int countryID = (Integer) countryImageLabel.getData("id");
					if (nameText.getText().isEmpty() || surnameText.getText().isEmpty() || countryID == FlagsResources.EMPTY_FLAG) {
						nameWarningLabel.setVisible(nameText.getText().isEmpty());
						surnameWarningLabel.setVisible(surnameText.getText().isEmpty());
						countryWarningLabel.setVisible(countryID == FlagsResources.EMPTY_FLAG);
					} else {
						countryWarningLabel.setVisible(false);
						surnameWarningLabel.setVisible(false);
						nameWarningLabel.setVisible(false);
						playerArchive.setName(nameText.getText());
						playerArchive.setSurname(surnameText.getText());
						playerArchive.setCountryId(countryID);
						if (youthTeamIdText.getText().matches("[0-9]+")) {
							playerArchive.setYouthTeamId(Integer.valueOf(youthTeamIdText.getText()));
						}
						playerArchive.setExistsInSokker(PlayerArchive.EXISTS_IN_SOKKER_COMPLETED);
						try {
							playersManager.updatePlayerArchive(playerArchive);
							MessageDialog.openInformationMessage(ArchiveInformationGroup.this.getShell(), Messages.getString("saved"));
						} catch (SQLException e) {
							new BugReporter(ArchiveInformationGroup.this.getDisplay()).openErrorMessage("Update Player", e);
						}
					}
				}
			}

		});

		countryListener = new Listener() {

			public void handleEvent(Event event) {
				switch (event.type) {
				case SWT.MouseDown:
					CountryChooser countryChooser = new CountryChooser(ArchiveInformationGroup.this.getShell(), SWT.TOOL | SWT.PRIMARY_MODAL);
					countryChooser.open(Cache.getCountries());
					int id = countryChooser.getId();
					countryImageLabel.setImage(FlagsResources.getFlag(id));
					countryImageLabel.setData("id", id);
					break;
				default:
					break;
				}

			}
		};

	}

	public void fill(PlayerArchive playerArchive) {
		countryImageLabel.removeListener(SWT.MouseDown, countryListener);
		this.playerArchive = playerArchive;
		idText.setText(String.valueOf(playerArchive.getId()));
		nameText.setText(playerArchive.getName());
		surnameText.setText(playerArchive.getSurname());
		youthTeamIdText.setText(String.valueOf(playerArchive.getYouthTeamId()));
		if (playerArchive.getCountryId() == 0) {
			countryImageLabel.setImage(FlagsResources.getFlag(FlagsResources.EMPTY_FLAG));
			countryImageLabel.setData("id", FlagsResources.EMPTY_FLAG);
		} else {
			countryImageLabel.setImage(FlagsResources.getFlag(playerArchive.getCountryId()));
			countryImageLabel.setData("id", playerArchive.getCountryId());
		}

		boolean editable = playerArchive.getExistsInSokker() == PlayerArchive.EXISTS_IN_SOKKER_FALSE
				|| playerArchive.getExistsInSokker() == PlayerArchive.EXISTS_IN_SOKKER_COMPLETED;
		for (Text text : new Text[] {nameText, surnameText, youthTeamIdText}) {
			text.setEditable(editable);
			text.setBackground(editable ? Colors.getListBackground() : Colors.getPanelBackground());
		}
		if (editable) {
			countryImageLabel.addListener(SWT.MouseDown, countryListener);
		}
		saveButton.setEnabled(editable);
	}

	private void addLabel(String key) {
		Label label = new Label(this, SWT.LEFT);
		label.setText(Messages.getString(key));
		label.setFont(this.getFont());
	}

	/** A read-only field, editable once fill allows it */
	private Text addField(GridData gridData) {
		Text text = new Text(this, SWT.BORDER);
		text.setLayoutData(gridData);
		text.setEditable(false);
		text.setBackground(Colors.getPanelBackground());
		text.setFont(this.getFont());
		return text;
	}

	/** The red mark shown next to a field that must be filled */
	private Label addWarning(GridData warningGD) {
		Label label = new Label(this, SWT.NONE);
		label.setForeground(Colors.getRedText());
		label.setText("*");
		label.setVisible(false);
		label.setLayoutData(warningGD);
		return label;
	}
}
