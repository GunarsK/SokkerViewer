package pl.pronux.sokker.ui.configure;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.TreeItem;

import pl.pronux.sokker.model.SokkerViewerSettings;
import pl.pronux.sokker.resources.Messages;
import pl.pronux.sokker.ui.beans.ConfigBean;
import pl.pronux.sokker.ui.handlers.DisplayHandler;
import pl.pronux.sokker.ui.handlers.ViewerHandler;
import pl.pronux.sokker.ui.interfaces.IViewConfigure;
import pl.pronux.sokker.ui.resources.ColorResources;
import pl.pronux.sokker.ui.resources.Fonts;
import pl.pronux.sokker.ui.widgets.buttons.ColorButton;
import pl.pronux.sokker.ui.widgets.shells.BugReporter;

public class ViewLookAndFeel implements IViewConfigure {

	private Map<Button, Label> buttonLabelMap;

	private Group colorGroup;

	private Map<String, Color> colorMap;

	private Composite composite;

	private Group fontGroup;

	private Map<String, FontData> fontButtonMap;

	private TreeItem treeItem;

	private Map<String, ColorButton> colorButtonMap;

	private Button systemDefaults;

	public void applyChanges() {
		if (!changed()) {
			return;
		}
		Properties properties = new Properties();
		for (String key : ConfigBean.COLORS) {
			Color color = colorMap.get(key);
			properties.setProperty(key, String.format("%d,%d,%d", color.getRed(), color.getGreen(), color.getBlue()));
		}
		for (String key : ConfigBean.FONTS) {
			properties.setProperty(key, fontButtonMap.get(key).toString());
		}
		save(properties);
	}

	/** True when a colour or font differs from the loaded look */
	private boolean changed() {
		for (String key : ConfigBean.COLORS) {
			if (!colorMap.get(key).equals(ConfigBean.getColor(key))) {
				return true;
			}
		}
		for (String key : ConfigBean.FONTS) {
			if (!fontButtonMap.get(key).equals(ConfigBean.getFont(key).getFontData()[0])) {
				return true;
			}
		}
		return false;
	}

	/** Saves the look; the main window is rebuilt when Preferences closes */
	private void save(Properties properties) {
		try {
			ConfigBean.save(properties);
		} catch (IOException e) {
			new BugReporter(composite.getDisplay()).openErrorMessage("ViewComposite LookAndFeel IO", e);
			return;
		}
		reloadMaps();
		ViewerHandler.getViewer().getConfigurator().rebuildOnClose();
	}

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

	public void init(final Composite composite) {

		this.composite = composite;
		
		colorMap = new HashMap<String, Color>();
		fontButtonMap = new HashMap<String, FontData>();

		buttonLabelMap = new HashMap<Button, Label>();
		colorButtonMap = new HashMap<String, ColorButton>();

		reloadMaps();

		composite.setLayout(new FormLayout());
		FormData formData;

		formData = new FormData(350, 30 * ConfigBean.COLORS.length + 50);
		formData.top = new FormAttachment(0, 10);
		formData.left = new FormAttachment(0, 10);

		colorGroup = new Group(composite, SWT.NONE);
		colorGroup.setLayoutData(formData);
		colorGroup.setLayout(new FormLayout());

		for (int i = 0; i < ConfigBean.COLORS.length; i++) {
			final String key = ConfigBean.COLORS[i];
			formData = new FormData(50, 25);
			formData.top = new FormAttachment(0, 30 * i + 10);
			formData.left = new FormAttachment(0, 20);

			final ColorButton button = new ColorButton(colorGroup, SWT.NONE);
			button.setLayoutData(formData);
			colorButtonMap.put(key, button);

			button.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					ColorDialog dialog = new ColorDialog(composite.getShell(), SWT.NONE);
					dialog.setRGB(colorMap.get(key).getRGB());
					RGB rgb = dialog.open();
					if (rgb != null) {
						Color newColor = ColorResources.getColor(rgb);
						button.setColor(newColor);
						colorMap.put(key, newColor);
					}
				}
			});

			formData = new FormData(250, 25);
			formData.top = new FormAttachment(0, 30 * i + 15);
			formData.left = new FormAttachment(button, 10);

			Label label = new Label(colorGroup, SWT.NONE);
			label.setLayoutData(formData);
			label.setText(Messages.getString(key));
		}

		// ******************* END COLOR SETTINGS
		// ******************************************

		// ******************* FONT SETTINGS
		// ***********************************************

		formData = new FormData(350, 100);
		formData.top = new FormAttachment(colorGroup, 10);
		formData.left = new FormAttachment(0, 10);

		fontGroup = new Group(composite, SWT.NONE);
		fontGroup.setLayoutData(formData);
		fontGroup.setLayout(new FormLayout());

		for (int i = 0; i < ConfigBean.FONTS.length; i++) {
			final String key = ConfigBean.FONTS[i];
			formData = new FormData(50, 25);
			formData.top = new FormAttachment(0, 30 * i + 10);
			formData.left = new FormAttachment(0, 20);

			final ColorButton button = new ColorButton(fontGroup, SWT.NONE);
			button.setText("Abc");
			button.setLayoutData(formData);
			colorButtonMap.put(key, button);

			button.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					FontDialog dialog = new FontDialog(composite.getShell(), SWT.NONE);
					FontData[] fontDataTable = {
						fontButtonMap.get(key)
					};
					dialog.setFontList(fontDataTable);
					FontData fontData = dialog.open();
					if (fontData != null && !fontButtonMap.get(key).equals(fontData)) {
						button.setFont(Fonts.getFont(DisplayHandler.getDisplay(), new FontData[] {fontData}));
						buttonLabelMap.get(button).setFont(button.getFont());
						fontButtonMap.put(key, fontData);
					}
				}
			});

			formData = new FormData(250, 25);
			formData.top = new FormAttachment(0, 30 * i + 15);
			formData.left = new FormAttachment(button, 10);

			Label label = new Label(fontGroup, SWT.NONE);
			label.setLayoutData(formData);
			label.setText(Messages.getString(key));

			buttonLabelMap.put(button, label);
		}

		formData = new FormData();
		formData.top = new FormAttachment(fontGroup, 10);
		formData.left = new FormAttachment(fontGroup, 0, SWT.CENTER);

		systemDefaults = new Button(composite, SWT.NONE);
		systemDefaults.setLayoutData(formData);
		systemDefaults.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event event) {
				systemDefaultChanges();
			}

		});

		// ****************** END FONT SETTINGS
		// *******************************************

		setWidgets();
		treeItem.setText(Messages.getString("configure.look")); 
		colorGroup.setText(Messages.getString("configure.colorSettings"));
		fontGroup.setText(Messages.getString("configure.fontSettings")); 
		systemDefaults.setText(Messages.getString("button.default.system")); 
		systemDefaults.pack();

		composite.layout(true);
	}

	private void reloadMaps() {
		for (String key : ConfigBean.COLORS) {
			colorMap.put(key, ConfigBean.getColor(key));
		}
		for (String key : ConfigBean.FONTS) {
			fontButtonMap.put(key, ConfigBean.getFont(key).getFontData()[0]);
		}
	}

	private void systemDefaultChanges() {
		MessageBox messageBox = new MessageBox(composite.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
		messageBox.setMessage(Messages.getString("message.question.settings"));
		messageBox.setText(Messages.getString("message.QUESTION"));
		if (messageBox.open() == SWT.NO) {
			return;
		}
		save(new Properties());
		setWidgets();
	}

	public void restoreDefaultChanges() {
		reloadMaps();
		setWidgets();

	}

	public void setSettings(SokkerViewerSettings sokkerViewerSettings) {
	}

	public void setTreeItem(TreeItem treeItem) {
		this.treeItem = treeItem;
	}

	public void set() {
	}

	private void setWidgets() {
		for (String key : ConfigBean.COLORS) {
			ColorButton button = colorButtonMap.get(key);
			button.setColor(colorMap.get(key));
			button.redraw();
		}

		for (String key : ConfigBean.FONTS) {
			Button button = colorButtonMap.get(key);
			button.setFont(Fonts.getFont(DisplayHandler.getDisplay(), new FontData[] {fontButtonMap.get(key)}));
			buttonLabelMap.get(button).setFont(button.getFont());
		}
	}


}
