package pl.pronux.sokker.ui.actions;

import java.io.IOException;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import pl.pronux.sokker.actions.SettingsManager;
import pl.pronux.sokker.actions.UpdateManager;
import pl.pronux.sokker.exceptions.SVException;
import pl.pronux.sokker.handlers.SettingsHandler;
import pl.pronux.sokker.interfaces.SV;
import pl.pronux.sokker.model.Release;
import pl.pronux.sokker.model.SokkerViewerSettings;
import pl.pronux.sokker.ui.handlers.ViewerHandler;
import pl.pronux.sokker.ui.widgets.shells.ReleaseNotesShell;
import pl.pronux.sokker.utils.Log;

/** shows the running version's github release notes once, on its first start */
public class ReleaseNotesAction implements Runnable {

	private final Display display;

	private ReleaseNotesAction(Display display) {
		this.display = display;
	}

	/** the notes fetch when this version has not run yet, else null */
	public static ReleaseNotesAction due(Display display) {
		SokkerViewerSettings settings = SettingsHandler.getSokkerViewerSettings();
		String lastVersion = settings.getLastVersion();
		if (lastVersion.isEmpty() && (settings.getUsername() == null || settings.getUsername().isEmpty())) {
			// a fresh install
			store();
			return null;
		}
		if (!lastVersion.isEmpty() && !Release.isNewer(SV.SK_VERSION, lastVersion)) {
			return null;
		}
		return new ReleaseNotesAction(display);
	}

	/** fetches the notes off the ui thread */
	public void run() {
		Thread thread = new Thread(new Runnable() {
			public void run() {
				fetch();
			}
		}, "release notes");
		thread.setDaemon(true);
		thread.start();
	}

	private void fetch() {
		final Release release = runningRelease();
		if (release == null || display.isDisposed()) {
			return;
		}
		display.asyncExec(new Runnable() {
			public void run() {
				show(release);
			}
		});
	}

	/** null when github could not be read */
	private static Release runningRelease() {
		try {
			return UpdateManager.downloader().getRelease(SV.SK_VERSION);
		} catch (IOException e) {
			Log.warning("github release notes: " + e);
			return null;
		}
	}

	private static void show(Release release) {
		Shell owner = ViewerHandler.getViewer();
		if (owner.isDisposed()) {
			return;
		}
		if (release.getNotes() != null && !release.getNotes().trim().isEmpty()) {
			new ReleaseNotesShell(owner, release).open();
		}
		store();
	}

	/** remembers that this version has run */
	private static void store() {
		SokkerViewerSettings settings = SettingsHandler.getSokkerViewerSettings();
		settings.setLastVersion(SV.SK_VERSION);
		try {
			SettingsManager.getInstance().updateSettings(settings);
		} catch (IOException e) {
			Log.error("release notes: version not stored", e);
		} catch (SVException e) {
			Log.error("release notes: version not stored", e);
		}
	}
}
