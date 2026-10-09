package pl.pronux.sokker.ui.widgets.tables;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import pl.pronux.sokker.actions.SettingsManager;
import pl.pronux.sokker.interfaces.SVComparator;
import pl.pronux.sokker.interfaces.Sort;
import pl.pronux.sokker.model.Person;
import pl.pronux.sokker.ui.beans.ConfigBean;
import pl.pronux.sokker.ui.listeners.NoteTableListener;
import pl.pronux.sokker.ui.listeners.TableLabelsListener;
import pl.pronux.sokker.ui.resources.ImageResources;
import pl.pronux.sokker.ui.widgets.shells.NoteShell;
import pl.pronux.sokker.utils.Log;

public abstract class SVTable<T> extends Table {

	/** Column data: width of a hidden column before it was hidden */
	private static final String HIDDEN_WIDTH = "hidden.width";

	/** Column data: resizable flag of a hidden column before it was hidden */
	private static final String HIDDEN_RESIZABLE = "hidden.resizable";

	/** Live tables per class */
	private static final Map<Class<?>, List<SVTable<?>>> TABLES = new HashMap<Class<?>, List<SVTable<?>>>();

	private Menu columnMenu;

	public SVTable(Composite parent, int style) {
		super(parent, style);
		List<SVTable<?>> tables = TABLES.get(getClass());
		if (tables == null) {
			tables = new ArrayList<SVTable<?>>();
			TABLES.put(getClass(), tables);
		}
		tables.add(this);
		addListener(SWT.Dispose, new Listener() {

			public void handleEvent(Event event) {
				TABLES.get(SVTable.this.getClass()).remove(SVTable.this);
				if (columnMenu != null) {
					columnMenu.dispose();
				}
			}
		});
		addListener(SWT.MenuDetect, new Listener() {

			public void handleEvent(Event event) {
				if (event.detail == SWT.MENU_MOUSE && isHeader(event.x, event.y)) {
					event.doit = false;
					showColumnMenu(event.x, event.y);
				}
			}
		});
		getDisplay().asyncExec(new Runnable() {

			public void run() {
				if (!isDisposed()) {
					hideColumns();
				}
			}
		});
	}

	/** True when the display point is on the column header */
	private boolean isHeader(int x, int y) {
		if (!getHeaderVisible()) {
			return false;
		}
		Point point = getDisplay().map(null, this, x, y);
		Rectangle area = getClientArea();
		return point.y >= area.y && point.y < area.y + getHeaderHeight();
	}

	/** One check item per titled column; the last shown one cannot be hidden */
	private void showColumnMenu(int x, int y) {
		if (columnMenu == null) {
			columnMenu = new Menu(getShell(), SWT.POP_UP);
		}
		for (MenuItem item : columnMenu.getItems()) {
			item.dispose();
		}
		Set<Integer> hidden = hiddenColumns();
		List<MenuItem> shown = new ArrayList<MenuItem>();
		for (int i = 0; i < getColumnCount(); i++) {
			String title = getColumn(i).getText().replace(Sort.ARROW_UP, "").replace(Sort.ARROW_DOWN, "").trim();
			if (title.isEmpty()) {
				continue;
			}
			final int column = i;
			MenuItem item = new MenuItem(columnMenu, SWT.CHECK);
			item.setText(title);
			item.setSelection(!hidden.contains(i));
			item.addListener(SWT.Selection, new Listener() {

				public void handleEvent(Event event) {
					toggleColumn(column);
				}
			});
			if (item.getSelection()) {
				shown.add(item);
			}
		}
		if (shown.size() == 1) {
			shown.get(0).setEnabled(false);
		}
		columnMenu.setLocation(x, y);
		columnMenu.setVisible(true);
	}

	/** Shows or hides a column in every live table of this class and saves the choice */
	private void toggleColumn(int column) {
		Set<Integer> hidden = hiddenColumns();
		if (!hidden.remove(column)) {
			hidden.add(column);
		}
		try {
			SettingsManager.getInstance().setHiddenColumns(getClass().getSimpleName(), getColumnCount(), hidden);
		} catch (IOException e) {
			Log.warning("hidden columns of " + getClass().getSimpleName(), e);
		}
		for (SVTable<?> table : TABLES.get(getClass())) {
			table.layoutColumns();
		}
	}

	private Set<Integer> hiddenColumns() {
		try {
			return SettingsManager.getInstance().getHiddenColumns(getClass().getSimpleName(), getColumnCount());
		} catch (IOException e) {
			Log.warning("hidden columns of " + getClass().getSimpleName(), e);
			return new TreeSet<Integer>();
		}
	}

	/** Sizes the columns, then hides the ones the user hid */
	public void layoutColumns() {
		Set<Integer> hidden = hiddenColumns();
		for (int i = 0; i < getColumnCount(); i++) {
			TableColumn column = getColumn(i);
			if (!hidden.contains(i) && column.getData(HIDDEN_WIDTH) != null) {
				column.setWidth((Integer) column.getData(HIDDEN_WIDTH));
				column.setResizable((Boolean) column.getData(HIDDEN_RESIZABLE));
				column.setData(HIDDEN_WIDTH, null);
				column.setData(HIDDEN_RESIZABLE, null);
			}
		}
		packColumns();
		hideColumns(hidden);
	}

	/** Packs every column but the last */
	protected void packColumns() {
		packColumns(0);
	}

	/** Packs every column but the last and the skipped ones, wider by the padding */
	protected void packColumns(int padding, int... skipped) {
		Set<Integer> skip = new HashSet<Integer>();
		for (int i : skipped) {
			skip.add(i);
		}
		for (int i = 0; i < getColumnCount() - 1; i++) {
			if (!skip.contains(i)) {
				TableColumn column = getColumn(i);
				column.pack();
				if (padding != 0) {
					column.setWidth(column.getWidth() + padding);
				}
			}
		}
	}

	/** Zero width for the hidden columns */
	protected void hideColumns() {
		hideColumns(hiddenColumns());
	}

	/** Remembers a column's width and resizable flag when it goes from shown to hidden */
	private void hideColumns(Set<Integer> hidden) {
		for (Integer i : hidden) {
			TableColumn column = getColumn(i);
			if (column.getData(HIDDEN_WIDTH) == null) {
				column.setData(HIDDEN_WIDTH, column.getWidth());
				column.setData(HIDDEN_RESIZABLE, column.getResizable());
			}
			column.setWidth(0);
			column.setResizable(false);
		}
	}

	@Override
	protected void checkSubclass() {
	}

	public void sort(SVComparator<T> comparator) {
	}

	protected SVComparator<T> getComparator() {
		return null;
	}

	protected void getChanges( int[] columns) {
		for (int i = 1; i < this.getItemCount(); i++) {
			for (int j = 0; j < columns.length; j++) {
				if (Double.valueOf(this.getItem(i).getText(columns[j]).replaceAll("[^0-9-]", "")).intValue() < Double.valueOf((this.getItem(i - 1).getText(columns[j]).replaceAll("[^0-9-]", ""))).intValue()) {    
					this.getItem(i - 1).setBackground(columns[j], ConfigBean.getColorIncrease());
				} else if (Double.valueOf(this.getItem(i).getText(columns[j]).replaceAll("[^0-9-]", "")).intValue() > Double.valueOf(this.getItem(i - 1).getText(columns[j]).replaceAll("[^0-9-]", "")).intValue()) {    
					this.getItem(i - 1).setBackground(columns[j], ConfigBean.getColorDecrease());
				}
			}
		}
	}

	public void setLabel(Label label, int column, TableItem item) {
	}
	
	protected void addLabelsListener() {
		Listener tableListener = new TableLabelsListener<T>(this);
		this.addListener(SWT.Dispose, tableListener);
		this.addListener(SWT.KeyDown, tableListener);
		this.addListener(SWT.MouseMove, tableListener);
		this.addListener(SWT.MouseHover, tableListener);
	}
	
	protected void getChanges(double max, double min, TableItem tableItem, int column) {
		if (max > min) {
			tableItem.setBackground(column, ConfigBean.getColorIncrease());
		} else if (max < min) {
			tableItem.setBackground(column, ConfigBean.getColorDecrease());
		}
	}

	/** Row colours of a week with or without training */
	public static void markTraining(TableItem item, boolean trained) {
		item.setForeground(trained ? null : ConfigBean.getColorUntrainedFg());
		item.setBackground(trained ? null : ConfigBean.getColorUntrainedBg());
	}

	public void openNote(TableItem item, String identifier, int column) {
		if(item.getData(identifier) != null && item.getData(identifier) instanceof Person) {
			Person person = (Person) item.getData(identifier);
			final NoteShell noteShell = new NoteShell(this.getShell(), SWT.PRIMARY_MODAL | SWT.CLOSE);
			noteShell.setPerson(person);
			noteShell.open();
			if ( person.getNote() != null) {
				if (person.getNote().isEmpty()) {
					item.setImage(column, null);
				} else {
					item.setImage(column, ImageResources.getImageResources("note.png"));
				}
			}
		}
	}
	
	protected void addNoteListener(String identifier, int column) {
		this.addListener(SWT.MouseDoubleClick, new NoteTableListener<T>(this, identifier, column));
	}
}
