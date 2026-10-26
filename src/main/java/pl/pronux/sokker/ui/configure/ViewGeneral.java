package pl.pronux.sokker.ui.configure;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TreeItem;

import pl.pronux.sokker.actions.SettingsManager;
import pl.pronux.sokker.enums.Language;
import pl.pronux.sokker.exceptions.SVException;
import pl.pronux.sokker.model.SokkerViewerSettings;
import pl.pronux.sokker.resources.Messages;
import pl.pronux.sokker.ui.handlers.ViewerHandler;
import pl.pronux.sokker.ui.interfaces.IViewConfigure;
import pl.pronux.sokker.ui.resources.Theme;
import pl.pronux.sokker.ui.widgets.shells.BugReporter;

public class ViewGeneral implements IViewConfigure {

	private SettingsManager settingsManager = SettingsManager.getInstance();
	
	private TreeItem treeItem;

	private Composite composite;

	private SokkerViewerSettings settings;

	private Button startupButton;

	private Button downloadButton;

	private Combo confShellLangTypeCombo;

	private Button closeButton;

	private Button updateCheckButton;

	private Combo themeCombo;

	public void clear() {

	}

	public void dispose() {

	}

	public Composite getComposite() {
		return composite;
	}

	public TreeItem getTreeItem() {
		return treeItem;
	}

	public void init(Composite composite) {
		this.composite = composite;

		composite.setLayout(new FormLayout());
		FormData formData;

		formData = new FormData();
		formData.top = new FormAttachment(0, 10);
		formData.left = new FormAttachment(0, 10);

		startupButton = new Button(composite, SWT.CHECK);
		startupButton.setLayoutData(formData);

		startupButton.pack();

		formData = new FormData();
		formData.top = new FormAttachment(startupButton, 10);
		formData.left = new FormAttachment(0, 10);

		downloadButton = new Button(composite, SWT.CHECK);
		downloadButton.setLayoutData(formData);

		downloadButton.pack();

		formData = new FormData();
		formData.top = new FormAttachment(downloadButton, 10);
		formData.left = new FormAttachment(0, 10);

		closeButton = new Button(composite, SWT.CHECK);
		closeButton.setLayoutData(formData);

		closeButton.pack();

		formData = new FormData();
		formData.top = new FormAttachment(closeButton, 10);
		formData.left = new FormAttachment(0, 10);

		updateCheckButton = new Button(composite, SWT.CHECK);
		updateCheckButton.setLayoutData(formData);

		updateCheckButton.pack();

		Label confShellLangTypeLabel = new Label(composite, SWT.VERTICAL | SWT.SHADOW_NONE);
		confShellLangTypeLabel.setText(Messages.getString("confShell.langtype"));

		formData = new FormData();
		formData.top = new FormAttachment(updateCheckButton, 10);
		formData.left = new FormAttachment(confShellLangTypeLabel, 20);
		formData.right = new FormAttachment(100, -20);

		confShellLangTypeCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		confShellLangTypeCombo.setItems(Language.languageNames());
		confShellLangTypeCombo.setLayoutData(formData);
		confShellLangTypeCombo.pack();

		formData = new FormData();
		formData.top = new FormAttachment(confShellLangTypeCombo, 0, SWT.CENTER);
		formData.left = new FormAttachment(0, 20);
		formData.height = 15;

		confShellLangTypeLabel.setLayoutData(formData);
		confShellLangTypeLabel.pack();

		Label themeLabel = new Label(composite, SWT.VERTICAL | SWT.SHADOW_NONE);
		themeLabel.setText(Messages.getString("configure.theme"));

		formData = new FormData();
		formData.top = new FormAttachment(confShellLangTypeCombo, 10);
		formData.left = new FormAttachment(themeLabel, 20);
		formData.right = new FormAttachment(100, -20);

		themeCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		for (String mode : Theme.MODES) {
			themeCombo.add(Messages.getString("configure.theme." + mode));
		}
		themeCombo.setLayoutData(formData);
		themeCombo.pack();

		formData = new FormData();
		formData.top = new FormAttachment(themeCombo, 0, SWT.CENTER);
		formData.left = new FormAttachment(0, 20);
		formData.height = 15;

		themeLabel.setLayoutData(formData);
		themeLabel.pack();

		setWidgets();
		startupButton.setText(Messages.getString("configure.startup"));
		downloadButton.setText(Messages.getString("configure.startup.download"));
		closeButton.setText(Messages.getString("configure.info.close"));
		updateCheckButton.setText(Messages.getString("configure.info.update"));
		treeItem.setText(Messages.getString("configure.general"));
		composite.layout(true);
	}

	private void setWidgets() {
		downloadButton.setSelection(settings.isUpdate());
		startupButton.setSelection(settings.isStartup());
		closeButton.setSelection(settings.isInfoClose());
		updateCheckButton.setSelection(settings.isInfoUpdate());

		String language = Language.getLanguageName(settings.getLangCode());
		if (language != null) {
			confShellLangTypeCombo.setText(language);
			confShellLangTypeCombo.pack();
		}
		themeCombo.select(Math.max(0, Arrays.asList(Theme.MODES).indexOf(settings.getTheme())));
	}

	public void setSettings(SokkerViewerSettings sokkerViewerSettings) {
		this.settings = sokkerViewerSettings;
	}

	public void setTreeItem(TreeItem treeItem) {
		this.treeItem = treeItem;
	}

	public void set() {
		setWidgets();
	}

	public void applyChanges() {
		settings.setStartup(startupButton.getSelection());
		settings.setInfoClose(closeButton.getSelection());
		settings.setInfoUpdate(updateCheckButton.getSelection());
		settings.setUpdate(downloadButton.getSelection());
		String theme = Theme.MODES[themeCombo.getSelectionIndex()];
		if (!theme.equals(settings.getTheme())) {
			settings.setTheme(theme);
			ViewerHandler.getViewer().getConfigurator().rebuildOnClose();
		}

		String text = confShellLangTypeCombo.getItem(confShellLangTypeCombo.getSelectionIndex());
		String langCode = Language.getLanguageCode(text);

		if (langCode != null && !langCode.equals(settings.getLangCode())) {
			settings.setLangCode(langCode);
			Messages.setDefault(new Locale(settings.getLangCode()));
		}
		try {
			settingsManager.updateSettings(settings);
		} catch (IOException e1) {
			new BugReporter(composite.getDisplay()).openErrorMessage("Configurator", e1);
		} catch (SVException e1) {
			new BugReporter(composite.getDisplay()).openErrorMessage("Configurator", e1);
		}
	}

	public void restoreDefaultChanges() {
		setWidgets();

	}

}
