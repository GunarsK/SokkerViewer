package pl.pronux.sokker.ui.plugins;

import java.sql.SQLException;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;

import pl.pronux.sokker.actions.AssistantManager;
import pl.pronux.sokker.actions.PlayersManager;
import pl.pronux.sokker.bean.SvBean;
import pl.pronux.sokker.data.cache.Cache;
import pl.pronux.sokker.data.sql.SQLSession;
import pl.pronux.sokker.handlers.SettingsHandler;
import pl.pronux.sokker.model.Player;
import pl.pronux.sokker.model.PlayerSkills;
import pl.pronux.sokker.model.SVNumberFormat;
import pl.pronux.sokker.model.SokkerViewerSettings;
import pl.pronux.sokker.resources.Messages;
import pl.pronux.sokker.ui.beans.ConfigBean;
import pl.pronux.sokker.ui.handlers.ViewerHandler;
import pl.pronux.sokker.ui.interfaces.IPlugin;
import pl.pronux.sokker.ui.interfaces.IViewConfigure;
import pl.pronux.sokker.ui.resources.CursorResources;
import pl.pronux.sokker.ui.resources.ImageResources;
import pl.pronux.sokker.ui.widgets.composites.DescriptionDoubleComposite;
import pl.pronux.sokker.ui.widgets.shells.BugReporter;
import pl.pronux.sokker.ui.widgets.tables.AssistantPlayersTable;

public class ViewAssistant implements IPlugin {

	private class Configure implements IViewConfigure {

		private PlayersManager playersManager = PlayersManager.getInstance();
		
		private Composite composite;

		private Table confTable;

		private Button defaultButton;

		private boolean init;

		private TreeItem treeItem;

		private boolean changed;
		
		private AssistantManager assistantManager = AssistantManager.getInstance();
		
		private void addTableEditor(final Table table) {
			final TableEditor editor = new TableEditor(table);
			editor.horizontalAlignment = SWT.LEFT;
			editor.grabHorizontal = true;
			table.addListener(SWT.MouseDown, new Listener() {
				public void handleEvent(Event event) {
					Point pt = new Point(event.x, event.y);
					final TableItem item = table.getItem(pt);
					if (item != null) {
						for (int i = 1; i < table.getColumnCount() - 1; i++) {
							Rectangle rect = item.getBounds(i);
							if (rect.contains(pt)) {
								final int column = i;
								final Text text = new Text(table, SWT.NONE);
								text.setTextLimit(3);
								text.setFont(ConfigBean.getFontTable());

								editor.setEditor(text, item, i);

								text.setText(item.getText(i));

								Listener textListener = new Listener() {
									public void handleEvent(final Event e) {
										switch (e.type) {
										case SWT.FocusOut:
											commitCell(text, item, column);
											text.dispose();
											break;
										case SWT.Traverse:
											switch (e.detail) {
											case SWT.TRAVERSE_RETURN:
												commitCell(text, item, column);
												break;
											case SWT.TRAVERSE_ESCAPE:
												text.dispose();
												e.doit = false;
												break;
											}
											break;
										case SWT.Verify:
											String string = e.text;
											char[] chars = new char[string.length()];
											string.getChars(0, chars.length, chars, 0);
											for (int j = 0; j < chars.length; j++) {
												if (!('0' <= chars[j] && chars[j] <= '9')) {
													e.doit = false;
													return;
												}
											}
											break;
										}
									}
								};
								text.addListener(SWT.FocusOut, textListener);
								text.addListener(SWT.Traverse, textListener);
								text.addListener(SWT.Verify, textListener);

								text.selectAll();
								text.setFocus();
								return;
							}
						}
					}

				}
			});

		}

		/** Writes the edited cell back unless the row passes 100 */
		private void commitCell(Text text, TableItem item, int column) {
			String temp = item.getText(column);
			if (text.getText().isEmpty()) {
				text.setText("0");
			}
			item.setText(column, text.getText());
			if (checkSumItem(item) > 100) {
				item.setText(column, temp);
				checkSumItem(item);
			}
			if (!item.getText(column).equals(temp)) {
				changed = true;
			}
		}

		public void applyChanges() {
			if (init && changed) {
				try {
					ViewerHandler.getViewer().setCursor(CursorResources.getCursor(SWT.CURSOR_WAIT));
					Cache.setAssistant(getConfigurationTableData(confTable));
					playersManager.updateAssistantData(Cache.getAssistant());
					playersManager.calculatePositionForAllPlayer(players, Cache.getAssistant());
					playersManager.updatePlayersPositions(players);
					playersTable.fill(players);
					ViewerHandler.getViewer().setCursor(CursorResources.getCursor(SWT.CURSOR_ARROW));
				} catch (SQLException e) {
					new BugReporter(composite.getDisplay()).openErrorMessage("ViewAssistant", e);
				}

				changed = false;
			}
		}

		private void checkSum(Table table) {
			for (int j = 0; j < table.getItemCount(); j++) {
				checkSumItem(table.getItem(j));
			}
		}

		private int checkSumItem(TableItem item) {
			int sum;
			Table table;

			table = item.getParent();
			sum = 0;
			for (int i = 1; i < table.getColumnCount() - 1; i++) {
				sum += Integer.valueOf(item.getText(i)).intValue();
			}
			if (sum <= 100) {
				item.setText(table.getColumnCount() - 1, String.valueOf(sum));
			} else {
				item.setText(table.getColumnCount() - 1, Messages.getString("table.error"));
			}

			return sum;
		}

		public void clear() {

		}

		public void dispose() {

		}

		private void fillConfigurationTable(Table table, int[][] data) {
			// Turn off drawing to avoid flicker
			table.setRedraw(false);

			table.removeAll();
			for (int i = 0; i < data.length; i++) {
				TableItem item = new TableItem(table, SWT.NONE);
				item.setText(0, Messages.getString("assistant.position." + data[i][0]));
				for (int j = 1; j < data[i].length; j++) {
					item.setText(j, String.valueOf(data[i][j]));
				}
			}

			checkSum(table);
			for (int i = 0; i < table.getColumnCount() - 1; i++) {
				table.getColumn(i).pack();
			}
			table.setRedraw(true);
		}

		public Composite getComposite() {
			return this.composite;
		}

		private int[][] getConfigurationTableData(Table table) {
			int[][] data = new int[table.getItemCount()][table.getColumnCount() - 1];

			for (int i = 0; i < data.length; i++) {
				data[i][0] = i + 1;
				for (int j = 1; j < data[i].length; j++) {
					data[i][j] = Integer.valueOf(table.getItem(i).getText(j)).intValue();
				}
			}
			return data;
		}

		public TreeItem getTreeItem() {
			return this.treeItem;
		}

		public void init(final Composite composite) {
			this.composite = composite;
			this.composite.setLayout(new FormLayout());

			FormData formData = new FormData(0,0);
			formData.top = new FormAttachment(50, 0);
			formData.left = new FormAttachment(50, 0);

			Label centerPoint = new Label(composite, SWT.NONE);
			centerPoint.setLayoutData(formData);

			formData = new FormData();
			formData.left = new FormAttachment(centerPoint, 0, SWT.CENTER);
			formData.top = new FormAttachment(0, 20);

			confTable = new Table(this.composite, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER);
			confTable.setLinesVisible(true);
			confTable.setHeaderVisible(true);
			confTable.setLayoutData(formData);
			confTable.setFont(ConfigBean.getFontTable());

			String[] titles = {
					Messages.getString("table.position"),
					Messages.getString("table.form"),
					Messages.getString("table.stamina"),
					Messages.getString("table.pace"),
					Messages.getString("table.technique"),
					Messages.getString("table.passing"),
					Messages.getString("table.keeper"),
					Messages.getString("table.defender"),
					Messages.getString("table.playmaker"),
					Messages.getString("table.scorer"),
					Messages.getString("assistant.sum")
			};

			for (int i = 0; i < titles.length; i++) {
				TableColumn column = new TableColumn(confTable, SWT.NONE);
				column.setText(titles[i]);
				column.setResizable(false);
				column.pack();
			}

			for (int i = 1; i <= Player.POSITION_COUNT; i++) {
				TableItem item = new TableItem(confTable, SWT.NONE);
				item.setText(0, Messages.getString("assistant.position." + i));
			}
			confTable.getColumn(0).pack();
			confTable.pack();



			formData = new FormData();
			formData.left = new FormAttachment(confTable, 0, SWT.CENTER);
			formData.top = new FormAttachment(confTable, 20);

			defaultButton = new Button(this.composite, SWT.NONE);
			defaultButton.setLayoutData(formData);
			defaultButton.setEnabled(false);
			defaultButton.setFont(ConfigBean.getFontTable());
			defaultButton.addListener(SWT.Selection, new Listener() {

				public void handleEvent(Event event) {
					MessageBox msg = new MessageBox(confTable.getShell(), SWT.YES | SWT.NO | SWT.ICON_WARNING);
					msg.setText(Messages.getString("message.WARNING"));
					msg.setMessage(Messages.getString("message.setDefaults"));

					if (msg.open() == SWT.YES) {
						ViewerHandler.getViewer().setCursor(CursorResources.getCursor(SWT.CURSOR_WAIT));
						int[][] data = new int[confTable.getItemCount()][];
						for (int i = 0; i < confTable.getItemCount(); i++) {
							String[] temp = SettingsHandler.getDefaultProperties().getProperty("assistant.default." + (i + 1)).split(";");
							data[i] = new int[temp.length + 1];
							data[i][0] = i + 1;
							for (int j = 0; j < temp.length; j++) {
								data[i][j + 1] = Integer.valueOf(temp[j]).intValue();
							}
						}

						try {
							Cache.setAssistant(data);
							playersManager.updateAssistantData(data);
							playersManager.calculatePositionForAllPlayer(players, data);
							playersManager.updatePlayersPositions(players);
							fillConfigurationTable(confTable, data);

							playersTable.fill(players);

							ViewerHandler.getViewer().setCursor(CursorResources.getCursor(SWT.CURSOR_ARROW));
						} catch (SQLException e) {
							new BugReporter(composite.getDisplay()).openErrorMessage("ViewAssistant", e);
						}
					}
				}
			});
			this.treeItem.setText(Messages.getString("tree.ViewAssistant"));
			this.defaultButton.setText(Messages.getString("button.default.system"));
			this.defaultButton.pack();


		}

		public void restoreDefaultChanges() {
			if (init && changed) {
				try {

					ViewerHandler.getViewer().setCursor(CursorResources.getCursor(SWT.CURSOR_WAIT));

					int[][] data = assistantManager.getAssistantData();

					Cache.setAssistant(data);
					playersManager.calculatePositionForAllPlayer(players, data);
					playersManager.updatePlayersPositions(players);
					fillConfigurationTable(confTable, data);

					playersTable.fill(players);

					ViewerHandler.getViewer().setCursor(CursorResources.getCursor(SWT.CURSOR_ARROW));

					changed = false;
				} catch (SQLException e) {
					try {
						SQLSession.close();
					} catch (SQLException e1) {
					}
					new BugReporter(composite.getDisplay()).openErrorMessage("ViewAssitant", e);
				}
			}
		}

		public void setSettings(SokkerViewerSettings sokkerViewerSettings) {

		}

		public void setTreeItem(TreeItem treeItem) {
			this.treeItem = treeItem;

		}

		public void set() {
			if (!init) {
				addTableEditor(confTable);
			}
			init = true;

			defaultButton.setEnabled(true);
			fillConfigurationTable(confTable, data);
		}

	}

	private Composite composite;

	private int[][] data;

	private DescriptionDoubleComposite descriptionComposite;

	private List<Player> players;

	private AssistantPlayersTable playersTable;

	private Sash sashHorizontal;

	private TreeItem treeItem;

	private void setPlayersView() {
		sashHorizontal = new Sash(composite, SWT.HORIZONTAL | SWT.NONE);

		FormData sashFormData = new FormData();
		sashFormData.top = new FormAttachment(0, 200);
		sashFormData.right = new FormAttachment(100, 0);
		sashFormData.left = new FormAttachment(0, 0);

		sashHorizontal.setLayoutData(sashFormData);
		sashHorizontal.setVisible(true);

		sashHorizontal.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				((FormData) sashHorizontal.getLayoutData()).top = new FormAttachment(0, event.y);
				sashHorizontal.getParent().layout();
			}
		});

		FormData viewFormData = new FormData();
		viewFormData.top = new FormAttachment(sashHorizontal, 0);
		viewFormData.right = new FormAttachment(100, 0);
		viewFormData.left = new FormAttachment(0, 0);
		viewFormData.bottom = new FormAttachment(100, 0);

		FormData descriptionFormData = new FormData();
		descriptionFormData.top = new FormAttachment(0, 0);
		descriptionFormData.right = new FormAttachment(100, 0);
		descriptionFormData.left = new FormAttachment(0, 0);
		descriptionFormData.bottom = new FormAttachment(sashHorizontal, 0);

		descriptionComposite = new DescriptionDoubleComposite(composite, SWT.BORDER);
		descriptionComposite.setLayoutData(descriptionFormData);
		descriptionComposite.setVisible(true);
		descriptionComposite.setFont(ConfigBean.getFontDescription());

		descriptionComposite.setLeftDescriptionStringFormat("%-20s%-15s\r\n");
		descriptionComposite.setLeftFirstColumnSize(20);
		descriptionComposite.setLeftSecondColumnSize(15);

		descriptionComposite.setRightDescriptionStringFormat("%-20s%-15s\r\n");
		descriptionComposite.setRightFirstColumnSize(20);
		descriptionComposite.setRightSecondColumnSize(15);

		playersTable = new AssistantPlayersTable(composite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		playersTable.setLayoutData(viewFormData);
	}

	public void clear() {

	}

	private void comparePlayers(DescriptionDoubleComposite description, Player p1, Player p2) {
		description.clearAll();
		String[][] valuesLeft = descriptionLabels();
		String[][] valuesRight = descriptionLabels();
		PlayerSkills s1 = p1.getSkills()[p1.getSkills().length - 1];
		PlayerSkills s2 = p2.getSkills()[p2.getSkills().length - 1];
		int[] textSize = new int[2];

		compareRow(description, valuesLeft[0], valuesRight[0], textSize, p1.getName(), p2.getName(), 0);
		compareRow(description, valuesLeft[1], valuesRight[1], textSize, p1.getSurname(), p2.getSurname(), 0);
		compareSkill(description, valuesLeft[2], valuesRight[2], textSize, "skill.b", s1.getForm(), s2.getForm());
		compareSkill(description, valuesLeft[3], valuesRight[3], textSize, "skill.b", s1.getStamina(), s2.getStamina());
		compareSkill(description, valuesLeft[4], valuesRight[4], textSize, "skill.b", s1.getPace(), s2.getPace());
		compareSkill(description, valuesLeft[5], valuesRight[5], textSize, "skill.b", s1.getTechnique(), s2.getTechnique());
		compareSkill(description, valuesLeft[6], valuesRight[6], textSize, "skill.c", s1.getPassing(), s2.getPassing());
		compareSkill(description, valuesLeft[7], valuesRight[7], textSize, "skill.a", s1.getKeeper(), s2.getKeeper());
		compareSkill(description, valuesLeft[8], valuesRight[8], textSize, "skill.a", s1.getDefender(), s2.getDefender());
		compareSkill(description, valuesLeft[9], valuesRight[9], textSize, "skill.a", s1.getPlaymaker(), s2.getPlaymaker());
		compareSkill(description, valuesLeft[10], valuesRight[10], textSize, "skill.a", s1.getScorer(), s2.getScorer());
		int summary1 = s1.getSummarySkill();
		int summary2 = s2.getSummarySkill();
		compareRow(description, valuesLeft[11], valuesRight[11], textSize, "[" + summary1 + "]" + difference(summary1, summary2), "[" + summary2 + "]" + difference(summary2, summary1), summary1 - summary2);
		compareRow(description, valuesLeft[12], valuesRight[12], textSize, Messages.getString("assistant.position." + p1.getPosition()), Messages.getString("assistant.position." + p2.getPosition()), 0);

		for (int i = 0; i < valuesLeft.length; i++) {
			description.addLeftText(valuesLeft[i]);
		}
		for (int i = 0; i < valuesRight.length; i++) {
			description.addRightText(valuesRight[i]);
		}

		description.setLeftColor();
		description.setRightColor();
	}

	/** Labels of the player description rows, values left empty */
	private String[][] descriptionLabels() {
		String[] labels = {
				Messages.getString("player.name"),
				Messages.getString("player.surname"),
				Messages.getString("player.form"),
				Messages.getString("player.stamina"),
				Messages.getString("player.pace"),
				Messages.getString("player.technique"),
				Messages.getString("player.passing"),
				Messages.getString("player.keeper"),
				Messages.getString("player.defender"),
				Messages.getString("player.playmaker"),
				Messages.getString("player.scorer"),
				Messages.getString("player.general"),
				Messages.getString("player.position")
		};
		String[][] values = new String[labels.length][2];
		for (int i = 0; i < labels.length; i++) {
			values[i][0] = labels[i];
		}
		return values;
	}

	/** Skill level name followed by its number in brackets */
	private String skillText(String prefix, int value) {
		return Messages.getString(prefix + value) + " [" + value + "] ";
	}

	/** Signed difference in brackets, such as "(+2)" */
	private String difference(int value, int other) {
		return "(" + SVNumberFormat.formatIntegerWithSignZero(value - other) + ")";
	}

	/** Compares one skill of two players in one description row */
	private void compareSkill(DescriptionDoubleComposite description, String[] left, String[] right, int[] textSize, String prefix, int value1, int value2) {
		compareRow(description, left, right, textSize, skillText(prefix, value1) + difference(value1, value2), skillText(prefix, value2) + difference(value2, value1), value1 - value2);
	}

	/** Fills one description row of both players, colouring the difference */
	private void compareRow(DescriptionDoubleComposite description, String[] left, String[] right, int[] textSize, String leftValue, String rightValue, int leftMinusRight) {
		textSize[0] += description.checkLeftFirstTextSize(left[0]);
		textSize[1] += description.checkRightFirstTextSize(right[0]);
		left[1] = leftValue;
		right[1] = rightValue;
		if (leftMinusRight > 0) {
			description.leftColorText(textSize[0], leftValue.length(), ConfigBean.getColorIncreaseDescription());
			description.rightColorText(textSize[1], rightValue.length(), ConfigBean.getColorDecreaseDescription());
		} else if (leftMinusRight < 0) {
			description.leftColorText(textSize[0], leftValue.length(), ConfigBean.getColorDecreaseDescription());
			description.rightColorText(textSize[1], rightValue.length(), ConfigBean.getColorIncreaseDescription());
		}
		textSize[0] += description.checkLeftSecondTextSize(leftValue);
		textSize[1] += description.checkRightSecondTextSize(rightValue);
	}

	public void dispose() {

	}

	public Composite getComposite() {
		return composite;
	}

	public IViewConfigure getConfigureComposite() {
		return new Configure();
	}

	public String getInfo() {
		return this.getClass().getSimpleName();
	}

	public String getStatusInfo() {
		return Messages.getString("progressBar.info.setInfoAssistant");
	}

	public TreeItem getTreeItem() {
		return treeItem;
	}

	public void init(Composite composite) {
		this.composite = composite;
		composite.setLayout(new FormLayout());

		setPlayersView();
		addPlayersTableListener();

		composite.layout(true);
	}

	public void setSettings(SokkerViewerSettings sokkerViewerSettings) {
	}

	public void setLayoutView(FormData formData) {
		this.composite.setLayoutData(formData);
	}

	private void setPlayerInfo(DescriptionDoubleComposite description, Player player) {
		description.clearAll();
		String[][] values = descriptionLabels();
		PlayerSkills skills = player.getSkills()[player.getSkills().length - 1];
		values[0][1] = player.getName();
		values[1][1] = player.getSurname();
		values[2][1] = skillText("skill.b", skills.getForm());
		values[3][1] = skillText("skill.b", skills.getStamina());
		values[4][1] = skillText("skill.b", skills.getPace());
		values[5][1] = skillText("skill.b", skills.getTechnique());
		values[6][1] = skillText("skill.c", skills.getPassing());
		values[7][1] = skillText("skill.a", skills.getKeeper());
		values[8][1] = skillText("skill.a", skills.getDefender());
		values[9][1] = skillText("skill.a", skills.getPlaymaker());
		values[10][1] = skillText("skill.a", skills.getScorer());
		values[11][1] = "[" + skills.getSummarySkill() + "]";
		values[12][1] = Messages.getString("assistant.position." + player.getPosition());

		for (int i = 0; i < values.length; i++) {
			description.addLeftText(values[i]);
		}
	}

	public void setSvBean(SvBean svBean) {

	}

	public void setTreeItem(TreeItem treeItem) {
		this.treeItem = treeItem;
		this.treeItem.setText(Messages.getString("tree.ViewAssistant"));
		this.treeItem.setImage(ImageResources.getImageResources("briefcase.png"));
	}

	public void set() {
		players = Cache.getPlayers();

		data = Cache.getAssistant();

		playersTable.fill(players);
	}

	/** Describes one selected player or compares two */
	private void addPlayersTableListener() {
		Listener playersTableListener = new Listener() {

			public void handleEvent(Event event) {
				switch (event.type) {
				case SWT.Selection:
					if (playersTable.getSelectionCount() == 1) {
						TableItem[] items = playersTable.getSelection();
						if (items.length == 1) {
							if (items[0] != null) {
								setPlayerInfo(descriptionComposite, (Player) items[0].getData("person"));
							}
						}
					}
					if (playersTable.getSelectionCount() == 2) {

						TableItem[] items = playersTable.getSelection();
						if (items.length == 2) {
							if (items[0] != null && items[1] != null) {
								comparePlayers(descriptionComposite, (Player) items[0].getData("person"), (Player) items[1].getData("person"));
							}
						}
					} else if (playersTable.getSelectionCount() > 2 || playersTable.getSelectionCount() < 1) {
						descriptionComposite.clearAll();
					}
					break;
				}
			}
		};
		playersTable.addListener(SWT.Selection, playersTableListener);
	}

	public void reload() {
	}

}
