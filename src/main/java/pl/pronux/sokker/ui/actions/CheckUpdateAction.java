package pl.pronux.sokker.ui.actions;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import pl.pronux.sokker.actions.UpdateManager;
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

	/** runs on the ui thread after the check, unless an update starts; may be null */
	private final Runnable next;

	private CheckUpdateAction(Shell shell, boolean quiet, Runnable next) {
		this.shell = shell;
		this.display = shell.getDisplay();
		this.quiet = quiet;
		this.next = next;
	}

	/** runs the check off the ui thread */
	public static void start(Shell shell, boolean quiet) {
		start(shell, quiet, null);
	}

	/** runs the check off the ui thread, then next unless an update starts */
	public static void start(Shell shell, boolean quiet, Runnable next) {
		Thread thread = new Thread(new CheckUpdateAction(shell, quiet, next), "update check");
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
				if (!show(release) && next != null) {
					next.run();
				}
			}
		});
	}

	/** the main window, or its replacement after a look change */
	private Shell owner() {
		return shell.isDisposed() ? ViewerHandler.getViewer() : shell;
	}

	/** null when github could not be read */
	private static Release latestRelease() {
		try {
			return UpdateManager.downloader().getLatestRelease();
		} catch (IOException e) {
			Log.warning("github update check: " + e);
			return null;
		}
	}

	/** true when an update started */
	private boolean show(Release release) {
		Shell owner = owner();
		if (owner.isDisposed()) {
			return false;
		}
		if (release != null && release.isNewerThan(SV.SK_VERSION)) {
			if (UpdateManager.canUpdate(release)) {
				if (open(owner, SWT.YES | SWT.NO | SWT.ICON_QUESTION, String.format(Messages.getString("message.update.install"), release.getVersion())) == SWT.YES) {
					UpdateAction.start(owner, release);
					return true;
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
		return false;
	}

	private int open(Shell owner, int style, String message) {
		MessageBox msg = new MessageBox(owner, style);
		msg.setText(Messages.getString("viewer.menu.help.update"));
		msg.setMessage(message);
		return msg.open();
	}
}
