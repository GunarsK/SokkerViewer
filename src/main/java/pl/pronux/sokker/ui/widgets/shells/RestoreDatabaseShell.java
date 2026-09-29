package pl.pronux.sokker.ui.widgets.shells;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import pl.pronux.sokker.handlers.SettingsHandler;
import pl.pronux.sokker.resources.Messages;
import pl.pronux.sokker.ui.beans.ConfigBean;
import pl.pronux.sokker.ui.events.UpdateEvent;
import pl.pronux.sokker.ui.handlers.ViewerHandler;
import pl.pronux.sokker.ui.interfaces.IEvents;
import pl.pronux.sokker.ui.widgets.dialogs.MessageDialog;
import pl.pronux.sokker.utils.file.Database;

public class RestoreDatabaseShell extends Shell {
	private Shell shell;

	private Combo combo;

	private Button button;

	private Listener backupDbListener;

	private CLabel label;

	private List<File> files;

	@Override
	protected void checkSubclass() {
		// super.checkSubclass();
	}

	public RestoreDatabaseShell(Shell parent, int style) {
		super(parent, style);
		
		this.shell = this;
		
		this.setLayout(new FormLayout());
		this.setSize(350, 150);
		this.setText(Messages.getString("shell.restore.db.title")); 
		
		backupDbListener = new Listener() {

			public void handleEvent(Event arg0) {
				try {
					if(files.size() > 0) {
						MessageBox msg = new MessageBox(shell, SWT.YES | SWT.NO | SWT.ICON_INFORMATION);
						msg.setText(Messages.getString("message.db.restore.question.title")); 
						msg.setMessage(Messages.getString("message.db.restore.question.text")); 
						if (msg.open() == SWT.YES) {
							File file = files.get(combo.getSelectionIndex());
							Database.restore(SettingsHandler.getSokkerViewerSettings(), file.getName());
							shell.close();
							ViewerHandler.getViewer().notifyListeners(IEvents.LOAD_DATA, new UpdateEvent(false));
						}
					}
				} catch (IOException e) {
					new BugReporter(RestoreDatabaseShell.this.getDisplay()).openErrorMessage("Restore database", e);
				}
			}
		};
		
		FormData formData = new FormData();
		formData.left = new FormAttachment(0, 5);
		formData.right = new FormAttachment(100, -5);
		formData.top = new FormAttachment(20, 0);

		label = new CLabel(this, SWT.NONE);
		label.setLayoutData(formData);
		label.setText(Messages.getString("viewer.label.backup.text")); 

		formData = new FormData();
		formData.left = new FormAttachment(0, 5);
		formData.right = new FormAttachment(100, -5);
		formData.top = new FormAttachment(label, 0);

		combo = new Combo(this, SWT.READ_ONLY);
		combo.setVisibleItemCount(15);
		combo.setLayoutData(formData);
		combo.setFont(ConfigBean.getFontMain());

		formData = new FormData();
		formData.left = new FormAttachment(combo, 0, SWT.CENTER);
		formData.top = new FormAttachment(combo, 10);
		formData.width = 100;

		button = new Button(this, SWT.NONE);
		button.setLayoutData(formData);
		button.setFont(ConfigBean.getFontMain());
		button.setText(Messages.getString("button.database.restore")); 
		button.addListener(SWT.Selection, backupDbListener);

		// shell.pack();
		Rectangle splashRect = this.getBounds();
		Rectangle displayRect = this.getDisplay().getPrimaryMonitor().getClientArea();
		int x = (displayRect.width - splashRect.width) / 2;
		int y = (displayRect.height - splashRect.height) / 2;
		this.setLocation(x, y);

	}

	public void addItems(List<File> files) {

		this.files = files;
		// one row per file, so the selection index picks the file
		for (File file : files) {
			combo.add(String.format("%s (%s %s [%.2f kb] )", file.getName(), new Date(file.lastModified()).toString(), new Time(file.lastModified()).toString(), Double.valueOf(file.length() / 1000)));
		}
		if (combo.getItemCount() == 0) {
			button.setEnabled(false);
		} else {
			combo.select(0);
		}
	}
	
	@Override
	public void open() {
		List<File> files = Database.backups(SettingsHandler.getSokkerViewerSettings());
		if (files.isEmpty()) {
			MessageDialog.openErrorMessage(this, Messages.getString("message.viewer.db.restore.error.text"));
		}
		this.addItems(files);
		super.open();
	}
}
