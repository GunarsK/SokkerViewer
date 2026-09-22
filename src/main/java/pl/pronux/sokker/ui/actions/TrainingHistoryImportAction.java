package pl.pronux.sokker.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import pl.pronux.sokker.actions.TrainingApiManager;
import pl.pronux.sokker.downloader.api.ApiException;
import pl.pronux.sokker.handlers.SettingsHandler;
import pl.pronux.sokker.interfaces.ProgressMonitor;
import pl.pronux.sokker.interfaces.RunnableWithProgress;
import pl.pronux.sokker.resources.Messages;
import pl.pronux.sokker.ui.events.UpdateEvent;
import pl.pronux.sokker.ui.handlers.ViewerHandler;
import pl.pronux.sokker.ui.interfaces.IEvents;
import pl.pronux.sokker.utils.Log;

/** menu action: pull every training week sokker.org still has for this team (plus accounts) */
public class TrainingHistoryImportAction implements RunnableWithProgress {

	private final Shell shell;

	private TrainingApiManager.Result result;

	private String error;

	private boolean finished;

	public TrainingHistoryImportAction(Shell shell) {
		this.shell = shell;
	}

	public void run(ProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		monitor.beginTask(Messages.getString("button.import.training.history"), 100);
		try {
			result = TrainingApiManager.getInstance().importHistory(SettingsHandler.getSokkerViewerSettings(), monitor);
		} catch (ApiException e) {
			Log.error("sokker.org api: training history import", e);
			error = e.isNotLoggedIn() ? Messages.getString("login.error.1") : e.getMessage();
		} catch (Exception e) {
			Log.error("sokker.org api: training history import", e);
			error = e.getMessage();
		} finally {
			monitor.done();
		}
	}

	/** the dialog fires this on the ui thread when it closes - twice, from the dialog and its bar */
	public void onFinish() {
		if (finished || shell.isDisposed()) {
			return;
		}
		finished = true;
		MessageBox msg = new MessageBox(shell, SWT.OK | (error != null ? SWT.ICON_ERROR : SWT.ICON_INFORMATION));
		msg.setText(Messages.getString("button.import.training.history"));
		msg.setMessage(message());
		msg.open();
		if (result != null && result.hasChanges()) {
			ViewerHandler.getViewer().notifyListeners(IEvents.LOAD_DATA, new UpdateEvent(false));
		}
	}

	private String message() {
		if (error != null) {
			return Messages.getString("message.training.history.failed") + ": " + error;
		}
		String text = String.format(Messages.getString("message.training.history.done"), Integer.valueOf(result.getCreated()), Integer.valueOf(result.getUpdated()));
		if (result.isForbidden()) {
			text += "\n" + Messages.getString("message.training.history.plus");
		}
		return text;
	}
}
