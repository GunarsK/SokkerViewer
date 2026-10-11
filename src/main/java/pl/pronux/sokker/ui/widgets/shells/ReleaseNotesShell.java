package pl.pronux.sokker.ui.widgets.shells;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import pl.pronux.sokker.model.Release;
import pl.pronux.sokker.resources.Messages;

/** a release's notes as plain text, closed with OK */
public class ReleaseNotesShell extends Shell {

	private static final int WIDTH = 560;

	private static final int HEIGHT = 420;

	private static final int BUTTON_WIDTH = 80;

	public ReleaseNotesShell(Shell parent, Release release) {
		super(parent, SWT.PRIMARY_MODAL | SWT.CLOSE | SWT.RESIZE);
		setText(String.format(Messages.getString("message.update.notes"), release.getVersion()));
		setLayout(new FormLayout());

		FormData formData = new FormData();
		formData.left = new FormAttachment(50, -BUTTON_WIDTH / 2);
		formData.bottom = new FormAttachment(100, -10);
		formData.width = BUTTON_WIDTH;

		Button okButton = new Button(this, SWT.NONE);
		okButton.setText(Messages.getString("button.ok"));
		okButton.setLayoutData(formData);
		okButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				close();
			}
		});
		setDefaultButton(okButton);

		formData = new FormData();
		formData.left = new FormAttachment(0, 10);
		formData.top = new FormAttachment(0, 10);
		formData.right = new FormAttachment(100, -10);
		formData.bottom = new FormAttachment(okButton, -10);

		StyledText notes = new StyledText(this, SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL | SWT.READ_ONLY);
		notes.setLayoutData(formData);
		notes.setText(plainText(release.getNotes()));

		setSize(WIDTH, HEIGHT);
		Rectangle area = getDisplay().getPrimaryMonitor().getClientArea();
		setLocation(area.x + (area.width - WIDTH) / 2, area.y + (area.height - HEIGHT) / 2);
	}

	@Override
	protected void checkSubclass() {
	}

	/** the notes without markdown's heading and bold markers */
	static String plainText(String markdown) {
		StringBuilder text = new StringBuilder();
		for (String line : markdown.split("\r?\n", -1)) {
			text.append(line.replaceFirst("^#{1,6}\\s+", "").replace("**", "")).append('\n');
		}
		return text.toString().trim();
	}
}
