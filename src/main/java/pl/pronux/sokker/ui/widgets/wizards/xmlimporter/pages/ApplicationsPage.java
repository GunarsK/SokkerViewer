package pl.pronux.sokker.ui.widgets.wizards.xmlimporter.pages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import pl.pronux.sokker.resources.Messages;
import pl.pronux.sokker.ui.widgets.wizards.Wizard;
import pl.pronux.sokker.ui.widgets.wizards.pages.Page;

public class ApplicationsPage extends Page {

	public static final String PAGE_NAME = "APPLICATIONS_PAGE"; 
	public static final String SOKKER_ORGANIZER = "SokkerOrganizer";
	public static final String SOKKER_VIEWER = "SokkerViewer";
	public static final String SOKKER_ASISTENTE = "Sokker Asistente";
	private String application = SOKKER_VIEWER;

	public ApplicationsPage(Wizard parent) {
		super(parent, Messages.getString("importer.page.applicationstitle"), PAGE_NAME); 
	}
	
	public void createControl(Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.horizontalSpacing = 30;
		container.setLayout(gridLayout);

		addChoice(container, SOKKER_VIEWER);
		addChoice(container, SOKKER_ORGANIZER);
		addChoice(container, SOKKER_ASISTENTE);

		setContainer(container);
	}

	/** a radio button choosing the application */
	private void addChoice(Composite container, final String name) {
		final Button button = new Button(container, SWT.RADIO);
		button.setSelection(name.equals(application));
		button.setText(name);
		button.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (button.getSelection()) {
					setApplication(name);
				}
			}
		});
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}
}
