package pl.pronux.sokker.ui.widgets.tables;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import pl.pronux.sokker.handlers.SettingsHandler;
import pl.pronux.sokker.resources.Messages;
import pl.pronux.sokker.ui.beans.Colors;
import pl.pronux.sokker.ui.beans.ConfigBean;
import pl.pronux.sokker.ui.listeners.VerifyDigitsListener;

public class TrainingConfigurationTable extends Table {

	public static final int TYPE_ALL = 1;
	public static final int TYPE_DETAIL = 2;

	@Override
	protected void checkSubclass() {
		// super.checkSubclass();
	}

	private int type = TYPE_DETAIL;

	public TrainingConfigurationTable(Composite parent, int style) {
		super(parent, style);
		this.setFont(ConfigBean.getFontTable());
		this.setHeaderVisible(false);
		this.setLinesVisible(true);

		// the row labels, the four formations and an empty last column
		for (int i = 0; i < 6; i++) {
			TableColumn column = new TableColumn(this, SWT.NONE);
			column.setMoveable(false);
			column.setResizable(false);
			column.setAlignment(i > 0 ? SWT.RIGHT : SWT.LEFT);
			if (i < 5) {
				column.setWidth(35);
			} else if (SettingsHandler.IS_LINUX) {
				column.pack();
			}
		}

		TableItem item = new TableItem(this, SWT.NONE);
		for (int i = 0; i < 4; i++) {
			item.setText(i + 1, Messages.getString("formation." + i));
		}
		item.setForeground(Colors.getConfigHeader());

		for (int i = 0; i < 4; i++) {
			item = new TableItem(this, SWT.NONE);
			item.setText(0, Messages.getString("formation." + i));
			item.setForeground(0, Colors.getConfigRowLabel());
		}

		this.addEditor();
	}

	public void fill(Integer[][] settings) {
		for (int i = 0; i < settings.length; i++) {
			for (int j = 0; j < settings[i].length; j++) {
				this.getItem(i + 1).setText(j + 1, String.valueOf(settings[i][j]));
			}
		}
	}

	private void addEditor() {
		final TableEditor editor = new TableEditor(this);
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;

		this.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event event) {
				Rectangle clientArea = TrainingConfigurationTable.this.getClientArea();
				Point pt = new Point(event.x, event.y);
				final TableItem item = TrainingConfigurationTable.this.getItem(pt);
				if (item != null && TrainingConfigurationTable.this.indexOf(item) > 0) {
					boolean visible = false;
					for (int i = 1; i < TrainingConfigurationTable.this.getColumnCount() - 1; i++) {
						Rectangle rect = item.getBounds(i);
						if (rect.contains(pt)) {
							final int column = i;

							final Text text = new Text(TrainingConfigurationTable.this, SWT.RIGHT);
							text.setTextLimit(3);
							text.setFont(ConfigBean.getFontTable());
							if (item.getText(i).isEmpty()) {
								text.setText("0");
							} else {
								text.setText(item.getText(i));
							}

							editor.setEditor(text, item, i);

							Listener textListener = new Listener() {
								public void handleEvent(final Event event) {
									switch (event.type) {
										case SWT.FocusOut:
											int value = 0;
											if (!text.getText().isEmpty()) {
												value = Integer.valueOf(text.getText().replaceAll("[^0-9]", "")).intValue();
											}
											int previous = Integer.valueOf(item.getText(column).replaceAll("[^0-9]", "")).intValue();

											if (previous != value && value <= 100) {
												if(type == TYPE_ALL) {
													for (int i = 1; i < TrainingConfigurationTable.this.getColumnCount() - 1; i++) {
														item.setText(i, String.valueOf(value));
													}
												} else {
													item.setText(column, String.valueOf(value));	
												}
											}
											text.dispose();
											break;
										case SWT.Traverse:
											switch (event.detail) {
												case SWT.TRAVERSE_RETURN:
													value = 0;
													if (!text.getText().isEmpty()) {
														value = Integer.valueOf(text.getText().replaceAll("[^0-9]", "")).intValue();
													}
													previous = Integer.valueOf(item.getText(column).replaceAll("[^0-9]", "")).intValue();
													value = Integer.valueOf(text.getText().replaceAll("[^0-9]", "")).intValue();

													if (previous != value && value <= 100) {
														if(type == TYPE_ALL) {
															for (int i = 1; i < TrainingConfigurationTable.this.getColumnCount() - 1; i++) {
																item.setText(i, String.valueOf(value));
															}
														} else {
															item.setText(column, String.valueOf(value));	
														}
													}
													break;
												// FALL THROUGH
												case SWT.TRAVERSE_ESCAPE:
													text.dispose();
													event.doit = false;
													break;
											}
											break;
									}
								}
							};
							text.addListener(SWT.FocusOut, textListener);
							text.addListener(SWT.Traverse, textListener);
							text.addListener(SWT.Verify, new VerifyDigitsListener());

							text.selectAll();
							text.setFocus();
							return;
						}
						if (!visible && rect.intersects(clientArea)) {
							visible = true;
						}
					}
					if (!visible) {
						return;
					}
				}
			}
		});
	}

	public Integer[][] getValues() {
		Integer[][] values = new Integer[4][4];
		for (int i = 1; i < 5; i++) {
			for (int j = 1; j < 5; j++) {
				values[i - 1][j - 1] = Integer.valueOf(this.getItem(i).getText(j));
			}
		}
		return values;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

}
