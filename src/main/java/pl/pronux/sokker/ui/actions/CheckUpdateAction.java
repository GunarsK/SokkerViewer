package pl.pronux.sokker.ui.actions;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import pl.pronux.sokker.actions.UpdateManager;
import pl.pronux.sokker.downloader.ReleaseDownloader;
import pl.pronux.sokker.handlers.SettingsHandler;
import pl.pronux.sokker.interfaces.SV;
import pl.pronux.sokker.model.Release;
import pl.pronux.sokker.resources.Messages;
import pl.pronux.sokker.ui.handlers.ViewerHandler;
import pl.pronux.sokker.utils.Log;

/** offers a newer github release: installs it, or opens its page */
public class CheckUpdateAction implements Runnable {

	private final Shell shell;

	private final Display display;

	/** startup check: silent unless a newer release exists */
	private final boolean quiet;

	private CheckUpdateAction(Shell shell, boolean quiet) {
		this.shell = shell;
		this.display = shell.getDisplay();
		this.quiet = quiet;
	}

	/** runs the check off the ui thread */
	public static void start(Shell shell, boolean quiet) {
		Thread thread = new Thread(new CheckUpdateAction(shell, quiet), "update check");
		thread.setDaemon(true);
		thread.start();
	}

	public void run() {
		final Release release = latestRelease();
		if (display.isDisposed()) {
			return;
		}
		display.asyncExec(new Runnable() {
			public void run() {
				show(release);
			}
		});
	}

	/** The main window to ask on: its replacement once a look change rebuilt it */
	private Shell owner() {
		return shell.isDisposed() ? ViewerHandler.getViewer() : shell;
	}

	/** null when github could not be read */
	private static Release latestRelease() {
		try {
			ReleaseDownloader downloader = new ReleaseDownloader();
			downloader.setProxySettings(SettingsHandler.getSokkerViewerSettings().getProxySettings());
			return downloader.getLatestRelease();
		} catch (IOException e) {
			Log.warning("github update check: " + e);
			return null;
		}
	}

	private void show(Release release) {
		Shell owner = owner();
		if (owner.isDisposed()) {
			return;
		}
		if (release != null && release.isNewerThan(SV.SK_VERSION)) {
			if (UpdateManager.canUpdate(release)) {
				if (open(owner, SWT.YES | SWT.NO | SWT.ICON_QUESTION, String.format(Messages.getString("message.update.install"), release.getVersion())) == SWT.YES) {
					UpdateAction.start(owner, release);
				}
			} else if (open(owner, SWT.YES | SWT.NO | SWT.ICON_QUESTION, String.format(Messages.getString("message.update.info"), release.getVersion())) == SWT.YES) {
				Program.launch(release.getUrl());
			}
		} else if (!quiet) {
			if (release == null) {
				open(owner, SWT.OK | SWT.ICON_ERROR, Messages.getString("message.error.connection"));
			} else {
				open(owner, SWT.OK | SWT.ICON_INFORMATION, Messages.getString("updater.label.info.empty"));
			}
		}
	}

	private int open(Shell owner, int style, String message) {
		MessageBox msg = new MessageBox(owner, style);
		msg.setText(Messages.getString("viewer.menu.help.update"));
		msg.setMessage(message);
		return msg.open();
	}
}
