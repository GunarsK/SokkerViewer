package pl.pronux.sokker.ui.resources;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import pl.pronux.sokker.handlers.SettingsHandler;
import pl.pronux.sokker.ui.beans.Colors;
import pl.pronux.sokker.utils.Log;

/** SV's light or dark look, chosen once at startup by the Appearance setting */
public final class Theme {

	private static final String SYSTEM = "system";

	private static final String LIGHT = "light";

	private static final String DARK = "dark";

	/** Appearance choices, in the Preferences order */
	public static final String[] MODES = {SYSTEM, LIGHT, DARK};

	/** The Appearance setting in effect, null before init */
	private static String mode;

	private static boolean dark;

	private static boolean skinned;

	/** Widget data key of a table or tree whose rows are shaded in place of grid lines */
	private static final String SHADED_ROWS = "theme.shadedRows";

	/** Group frame in the border grey over the native near-white one; SWT paints the title's background afterwards */
	private static final Listener FRAME = new Listener() {
		public void handleEvent(Event event) {
			Control group = (Control) event.widget;
			Point size = group.getSize();
			int top = event.gc.getFontMetrics().getHeight() / 2;
			event.gc.setForeground(group.getBackground());
			event.gc.setLineWidth(3);
			event.gc.drawRectangle(1, top, size.x - 3, size.y - top - 2);
			event.gc.setLineWidth(1);
			event.gc.setForeground(Colors.getBorderLine());
			event.gc.drawRectangle(0, top, size.x - 1, size.y - top - 1);
		}
	};

	/** Gives each new control and row the theme's colours where it still has the platform's defaults */
	private static final Listener SKIN = new Listener() {
		public void handleEvent(Event event) {
			if (event.widget instanceof TableItem || event.widget instanceof TreeItem) {
				shadeRow((Item) event.widget);
			} else if (event.widget instanceof Control) {
				skinControl((Control) event.widget);
			}
		}
	};

	private Theme() {
	}

	/** Picks the theme and switches the native look: at startup, right after the Display exists, and before a rebuild */
	public static void init(Display display, String mode) {
		if (mode.equals(Theme.mode)) {
			return;
		}
		boolean switching = Theme.mode != null;
		Theme.mode = mode;
		boolean wasDark = dark;
		boolean system = Display.isSystemDarkTheme();
		dark = DARK.equals(mode) || (!LIGHT.equals(mode) && system && !SettingsHandler.IS_WINDOWS);
		if (SettingsHandler.IS_MACOSX && (!SYSTEM.equals(mode) || switching)) {
			setNativeTheme("org.eclipse.swt.internal.cocoa.OS", dark);
		} else if (SettingsHandler.IS_WINDOWS && (dark || wasDark)) {
			setNativeTheme("org.eclipse.swt.internal.win32.OS", dark);
			// flat one-pixel borders instead of the light 3D edge
			display.setData("org.eclipse.swt.internal.win32.all.use_WS_BORDER", dark);
		}
		// Windows widgets never turn dark on their own; GTK and Cocoa paint some colours from the system theme
		skinned = SettingsHandler.IS_WINDOWS ? dark : system != dark;
		display.removeListener(SWT.Skin, SKIN);
		if (skinned) {
			display.addListener(SWT.Skin, SKIN);
		}
	}

	public static boolean isDark() {
		return dark;
	}

	/** True when SV paints the widgets' colours itself instead of the platform */
	public static boolean isSkinned() {
		return skinned;
	}

	private static void skinControl(Control control) {
		// combos keep SWT's own dark look; colours set here would not reach its drop-down list
		if (control instanceof Combo) {
			return;
		}
		Color background = control.getBackground();
		if (background.equals(ColorResources.getListBackground())) {
			control.setBackground(Colors.getListBackground());
		} else if (background.equals(ColorResources.getWidgetBackground())) {
			control.setBackground(Colors.getPanelBackground());
		}
		// Windows gives only some headers its dark look
		Color header = Colors.getPanelBackground();
		if (control instanceof Table) {
			Table table = (Table) control;
			table.setHeaderBackground(header);
			table.setHeaderForeground(textOn(header));
			// the native grid lines cannot be recoloured, so dark tables shade every other row instead
			if (dark && table.getLinesVisible()) {
				table.setLinesVisible(false);
				table.setData(SHADED_ROWS, Boolean.TRUE);
			}
		} else if (control instanceof Tree) {
			Tree tree = (Tree) control;
			tree.setHeaderBackground(header);
			tree.setHeaderForeground(textOn(header));
			if (dark && tree.getLinesVisible()) {
				tree.setLinesVisible(false);
				tree.setData(SHADED_ROWS, Boolean.TRUE);
			}
		}
		if (dark && SettingsHandler.IS_WINDOWS) {
			if (control instanceof Group) {
				control.addListener(SWT.Paint, FRAME);
			}
			if (control instanceof Label && (control.getStyle() & SWT.SEPARATOR) != 0) {
				replaceSeparator(control);
				return;
			}
		}
		// ExpandBar item headers keep the native look, so its text keeps the native colour
		if (control instanceof ExpandBar) {
			return;
		}
		Color foreground = control.getForeground();
		if (foreground.equals(ColorResources.getSystemColor(SWT.COLOR_LIST_FOREGROUND))
			|| foreground.equals(ColorResources.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND))
			|| foreground.equals(ColorResources.getBlack())) {
			control.setForeground(textOn(control.getBackground()));
		}
	}

	/** Every other row of a shaded table or tree gets a lighter shade; rows SV coloured keep their colour */
	private static void shadeRow(Item item) {
		if (item instanceof TableItem) {
			TableItem row = (TableItem) item;
			Table table = row.getParent();
			if (table.getData(SHADED_ROWS) != null && table.indexOf(row) % 2 == 1 && row.getBackground().equals(table.getBackground())) {
				row.setBackground(shade(table.getBackground()));
			}
		} else {
			TreeItem row = (TreeItem) item;
			Tree tree = row.getParent();
			int index = row.getParentItem() != null ? row.getParentItem().indexOf(row) : tree.indexOf(row);
			if (tree.getData(SHADED_ROWS) != null && index % 2 == 1 && row.getBackground().equals(tree.getBackground())) {
				row.setBackground(shade(tree.getBackground()));
			}
		}
	}

	/** A slightly lighter grey than the background */
	private static Color shade(Color background) {
		return ColorResources.getColor(Math.min(255, background.getRed() + 8), Math.min(255, background.getGreen() + 8), Math.min(255, background.getBlue() + 8));
	}

	/** Hides a native separator, which Windows draws near-white, and draws a border-grey line in its place */
	private static void replaceSeparator(final Control separator) {
		final Composite parent = separator.getParent();
		separator.setVisible(false);
		parent.addListener(SWT.Paint, new Listener() {
			public void handleEvent(Event event) {
				if (separator.isDisposed()) {
					return;
				}
				Rectangle bounds = separator.getBounds();
				event.gc.setForeground(Colors.getBorderLine());
				if ((separator.getStyle() & SWT.VERTICAL) != 0) {
					int x = bounds.x + bounds.width / 2;
					event.gc.drawLine(x, bounds.y, x, bounds.y + bounds.height - 1);
				} else {
					int y = bounds.y + bounds.height / 2;
					event.gc.drawLine(bounds.x, y, bounds.x + bounds.width - 1, y);
				}
			}
		});
		Listener moved = new Listener() {
			public void handleEvent(Event event) {
				parent.redraw();
			}
		};
		separator.addListener(SWT.Move, moved);
		separator.addListener(SWT.Resize, moved);
	}

	/** SWT's internal OS.setTheme(boolean), which only the running platform's jar has */
	private static void setNativeTheme(String osClass, boolean on) {
		try {
			Class.forName(osClass).getMethod("setTheme", boolean.class).invoke(null, on);
		} catch (ReflectiveOperationException e) {
			Log.warning("Theme", e);
		}
	}

	/** Light text on a dark background, black on a light one */
	private static Color textOn(Color background) {
		boolean darkBackground = background.getRed() * 299 + background.getGreen() * 587 + background.getBlue() * 114 < 128000;
		return darkBackground ? Colors.getLightText() : ColorResources.getBlack();
	}
}
