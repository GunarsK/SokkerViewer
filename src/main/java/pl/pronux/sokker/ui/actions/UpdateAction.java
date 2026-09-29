package pl.pronux.sokker.ui.actions;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import pl.pronux.sokker.actions.UpdateManager;
import pl.pronux.sokker.interfaces.ProgressMonitor;
import pl.pronux.sokker.interfaces.RunnableWithProgress;
import pl.pronux.sokker.model.Release;
import pl.pronux.sokker.resources.Messages;
import pl.pronux.sokker.ui.handlers.ViewerHandler;
import pl.pronux.sokker.ui.widgets.dialogs.ProgressBarDialog;
import pl.pronux.sokker.utils.Log;

/** downloads a newer release, then closes SokkerViewer for the updater */
public class UpdateAction implements RunnableWithProgress {

	private static final long SYNC_POLL_MS = 500;

	/** true from start until the update fails or is cancelled; ui thread only */
	private static boolean running;

	private final Shell shell;

	private final Release release;

	/** the unpacked release, set only once it is ready to install */
	private volatile File unpacked;

	private volatile String error;

	private boolean finished;

	private UpdateAction(Shell shell, Release release) {
		this.shell = shell;
		this.release = release;
	}

	/** shows the download progress, unless an update already runs */
	public static void start(Shell shell, Release release) {
		if (running) {
			return;
		}
		running = true;
		try {
			new ProgressBarDialog(shell, SWT.PRIMARY_MODAL | SWT.CLOSE).run(false, true, true, new UpdateAction(shell, release));
		} catch (InterruptedException e) {
			running = false;
			Log.error("update", e);
		} catch (InvocationTargetException e) {
			running = false;
			Log.error("update", e);
		}
	}

	public void run(ProgressMonitor monitor) {
		monitor.beginTask(String.format(Messages.getString("message.update.download"), release.getVersion()), 1);
		try {
			File ready = UpdateManager.prepare(release, monitor);
			// a running sync finishes before the restart
			if (ready != null && CoreAction.isRunning()) {
				monitor.setTaskName(Messages.getString("message.update.wait"));
				while (CoreAction.isRunning() && !monitor.isCanceled()) {
					Thread.sleep(SYNC_POLL_MS);
				}
			}
			if (ready != null && !monitor.isCanceled()) {
				UpdateManager.backupDatabase();
				unpacked = ready;
			}
		} catch (Exception e) {
			Log.error("update to " + release.getVersion(), e);
			error = describe(e);
		} finally {
			monitor.done();
		}
	}

	/** runs on the ui thread when the dialog closes, maybe twice */
	public void onFinish() {
		if (finished || shell.isDisposed()) {
			return;
		}
		finished = true;
		if (error == null && unpacked != null) {
			try {
				UpdateManager.startUpdater(unpacked);
				// the updater waits for SokkerViewer to close
				shell.getDisplay().asyncExec(new Runnable() {
					public void run() {
						if (!ViewerHandler.getViewer().isDisposed()) {
							ViewerHandler.getViewer().dispose();
						}
					}
				});
				return;
			} catch (Exception e) {
				Log.error("starting the updater", e);
				error = describe(e);
			}
		}
		running = false;
		UpdateManager.cleanUp();
		if (error != null) {
			MessageBox msg = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
			msg.setText(Messages.getString("viewer.menu.help.update"));
			msg.setMessage(Messages.getString("message.update.failed") + ": " + error);
			msg.open();
		}
	}

	/** the exception's message, or its class name when it has none */
	private static String describe(Exception e) {
		return e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
	}
}
