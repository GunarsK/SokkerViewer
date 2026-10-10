package pl.pronux.sokker.ui.plugins;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;

import pl.pronux.sokker.actions.ConfigurationManager;
import pl.pronux.sokker.actions.PlayersManager;
import pl.pronux.sokker.bean.SvBean;
import pl.pronux.sokker.comparators.PlayerComparator;
import pl.pronux.sokker.data.cache.Cache;
import pl.pronux.sokker.interfaces.Sort;
import pl.pronux.sokker.model.Date;
import pl.pronux.sokker.model.Junior;
import pl.pronux.sokker.model.Player;
import pl.pronux.sokker.model.PlayerInterface;
import pl.pronux.sokker.model.PlayerSkills;
import pl.pronux.sokker.model.SokkerViewerSettings;
import pl.pronux.sokker.model.TalentEstimator;
import pl.pronux.sokker.model.TalentFactors;
import pl.pronux.sokker.model.TalentFactors.Factor;
import pl.pronux.sokker.resources.Messages;
import pl.pronux.sokker.ui.beans.ConfigBean;
import pl.pronux.sokker.ui.events.TranslateEvent;
import pl.pronux.sokker.ui.handlers.ViewerHandler;
import pl.pronux.sokker.ui.interfaces.IEvents;
import pl.pronux.sokker.ui.interfaces.IPlugin;
import pl.pronux.sokker.ui.interfaces.IViewConfigure;
import pl.pronux.sokker.ui.resources.ColorResources;
import pl.pronux.sokker.ui.resources.CursorResources;
import pl.pronux.sokker.ui.resources.FlagsResources;
import pl.pronux.sokker.ui.resources.ImageResources;
import pl.pronux.sokker.ui.widgets.composites.ChartDateComposite;
import pl.pronux.sokker.ui.widgets.composites.DescriptionSingleComposite;
import pl.pronux.sokker.ui.widgets.composites.JuniorTrainedDescriptionComposite;
import pl.pronux.sokker.ui.widgets.composites.PlayerDescriptionComposite;
import pl.pronux.sokker.ui.widgets.composites.PlayersDescriptionComposite;
import pl.pronux.sokker.ui.widgets.composites.ViewComposite;
import pl.pronux.sokker.ui.widgets.composites.views.JuniorChartsComposite;
import pl.pronux.sokker.ui.widgets.composites.views.PlayerChartsComposite;
import pl.pronux.sokker.ui.widgets.composites.views.PlayerStatsComposite;
import pl.pronux.sokker.ui.widgets.dialogs.MessageDialog;
import pl.pronux.sokker.ui.widgets.shells.BugReporter;
import pl.pronux.sokker.ui.widgets.shells.NoteShell;
import pl.pronux.sokker.ui.widgets.tables.JuniorTrainedTable;
import pl.pronux.sokker.ui.widgets.tables.PlayerTable;
import pl.pronux.sokker.ui.widgets.tables.PlayersTable;
import pl.pronux.sokker.ui.widgets.tables.SVTable;
import pl.pronux.sokker.utils.file.OperationOnFile;
import pl.pronux.sokker.utils.pdf.PDFexport;

public class ViewPlayers implements IPlugin, Sort {

	private class Configure implements IViewConfigure {

		/** spinner steps per factor unit */
		private static final int SCALE = 1000;

		private ConfigurationManager configurationManager = ConfigurationManager.getInstance();

		private Composite composite;

		private TreeItem treeItem;

		private Map<Factor, Spinner> spinners = new EnumMap<Factor, Spinner>(Factor.class);

		private Button defaultButton;

		private boolean init;

		public void applyChanges() {
			if (!init) {
				return;
			}
			Map<Factor, Double> factors = new EnumMap<Factor, Double>(Factor.class);
			boolean changed = false;
			for (Factor factor : Factor.values()) {
				double value = spinners.get(factor).getSelection() / (double) SCALE;
				factors.put(factor, value);
				if (value != TalentFactors.get(factor)) {
					changed = true;
				}
			}
			if (changed) {
				apply(factors);
			}
		}

		/** stores the factors, re-estimates talents and refills the list */
		private void apply(Map<Factor, Double> factors) {
			ViewerHandler.getViewer().setCursor(CursorResources.getCursor(SWT.CURSOR_WAIT));
			try {
				configurationManager.setTalentFactors(factors);
				TalentFactors.set(factors);
				List<Player> all = new ArrayList<Player>(Cache.getPlayers());
				all.addAll(Cache.getPlayersHistory());
				all.addAll(Cache.getPlayersTrash());
				TalentEstimator.estimate(all, Cache.getTrainingsMap().values());
				refillPlayersTable();
				showFactors();
			} catch (SQLException e) {
				new BugReporter(composite.getDisplay()).openErrorMessage("ViewPlayers", e);
			} finally {
				ViewerHandler.getViewer().setCursor(CursorResources.getCursor(SWT.CURSOR_ARROW));
			}
		}

		/** puts the current factors into the spinners */
		private void showFactors() {
			for (Factor factor : Factor.values()) {
				spinners.get(factor).setSelection((int) Math.round(TalentFactors.get(factor) * SCALE));
			}
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
			this.composite.setLayout(new FormLayout());

			FormData formData = new FormData(350, SWT.DEFAULT);
			formData.top = new FormAttachment(0, 10);
			formData.left = new FormAttachment(0, 10);

			FormLayout groupLayout = new FormLayout();
			groupLayout.marginBottom = 10;

			Group group = new Group(composite, SWT.NONE);
			group.setLayoutData(formData);
			group.setLayout(groupLayout);
			group.setText(Messages.getString("configure.talentSettings"));

			Spinner previous = null;
			for (Factor factor : Factor.values()) {
				formData = new FormData(70, SWT.DEFAULT);
				formData.top = previous == null ? new FormAttachment(0, 10) : new FormAttachment(previous, 8);
				formData.left = new FormAttachment(0, 20);

				Spinner spinner = new Spinner(group, SWT.BORDER);
				spinner.setValues(1, 1, (int) Math.round(factor.getMax() * SCALE), 3, 1, 100);
				spinner.setLayoutData(formData);
				spinners.put(factor, spinner);

				formData = new FormData();
				formData.top = new FormAttachment(spinner, 0, SWT.CENTER);
				formData.left = new FormAttachment(spinner, 10);

				Label label = new Label(group, SWT.NONE);
				label.setLayoutData(formData);
				label.setText(Messages.getString("talent.factor." + factor.getKey()));
				previous = spinner;
			}
			showFactors();

			formData = new FormData();
			formData.top = new FormAttachment(group, 10);
			formData.left = new FormAttachment(group, 0, SWT.CENTER);

			defaultButton = new Button(composite, SWT.NONE);
			defaultButton.setLayoutData(formData);
			defaultButton.setEnabled(false);
			defaultButton.setText(Messages.getString("button.default.system"));
			defaultButton.pack();
			defaultButton.addListener(SWT.Selection, new Listener() {

				public void handleEvent(Event event) {
					MessageBox msg = new MessageBox(composite.getShell(), SWT.YES | SWT.NO | SWT.ICON_WARNING);
					msg.setText(Messages.getString("message.WARNING"));
					msg.setMessage(Messages.getString("message.setDefaults"));
					if (msg.open() == SWT.YES) {
						apply(new EnumMap<Factor, Double>(Factor.class));
					}
				}
			});
		}

		public void restoreDefaultChanges() {
			showFactors();
		}

		public void set() {
			init = true;
			defaultButton.setEnabled(true);
			showFactors();
		}

		public void setSettings(SokkerViewerSettings sokkerViewerSettings) {
		}

		public void setTreeItem(TreeItem treeItem) {
			this.treeItem = treeItem;
			this.treeItem.setText(Messages.getString("tree.ViewPlayers"));
		}
	}

	private PlayersManager playersManager = PlayersManager.getInstance();

	private TreeItem _treeItem;

	private Clipboard clipboard;

	private String cbData;

	private Combo comboFilter;

	private Listener comboFilterListner;

	private Composite vComposite;

	private SokkerViewerSettings settings;

	private Composite currentComposite;

	private Composite currentDesc;

	private Table currentView;

	private FormData descriptionFormData;

	private ChartDateComposite graphComposite;

	private Listener graphList;

	private PlayerChartsComposite graphsComposite;

	private Map<Integer, TreeItem> itemMap;

	private JuniorTrainedDescriptionComposite juniorTrainedComposite;

	private JuniorTrainedTable juniorTrainedTable;

	private Menu menuClear;

	private Menu menuPopUp;

	private Menu menuPopUpHistory;

	private Menu menuPopUpParentTree;

	private List<Player> players;

	private PlayersDescriptionComposite playersDescription;

	private PlayersTable playersTable;

	private PlayerTable playerView;

	private Composite previousDesc;

	private Composite toolBarComposite;

	private JuniorTrainedDescriptionComposite universalJuniorComposite;

	private PlayerDescriptionComposite playerDescription;

	private FormData viewFormData;

	private Map<Integer, Table> viewMap;

	private Composite composite;

	private PlayerStatsComposite playerStatsComposite;

	private JuniorChartsComposite juniorGraphsComposite;

	private void addJuniorTrainedView() {
		juniorTrainedTable = new JuniorTrainedTable(vComposite, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
		juniorTrainedTable.setLayoutData(viewFormData);
		juniorTrainedTable.setVisible(false);

		juniorTrainedTable.addListener(SWT.MouseDown, new Listener() {

			public void handleEvent(Event event) {
				if (event.button == 1) {
					Point point = new Point(event.x, event.y);
					TableItem item = currentView.getItem(point);
					if (item != null) {
						if (currentDesc instanceof ChartDateComposite) {
							((ChartDateComposite) currentDesc).setMarkers((Date) item.getData("date"), Calendar.THURSDAY, -1);
						} else if (currentDesc instanceof DescriptionSingleComposite) {
							int index = item.getParent().indexOf(item);
							universalJuniorComposite.setStatsJuniorInfo((Junior) item.getParent().getData(Junior.class.getName()), index);
							showDescription(universalJuniorComposite);
						}
					}
				} else if (event.button == 3) {
					showDescription(previousDesc);
				}
			}
		});

		for (int j = 1; j < juniorTrainedTable.getColumnCount() - 2; j++) {
			juniorTrainedTable.getColumn(j).addListener(SWT.Selection, graphList);
		}
	}

	private void addListener() {

		comboFilterListner = new Listener() {

			public void handleEvent(Event event) {
				refillPlayersTable();
			}
		};

		graphList = new Listener() {

			public void handleEvent(Event event) {

				TableColumn tempColumn = (TableColumn) event.widget;
				Table tempTable = (tempColumn).getParent();

				int[] tempIntTable = new int[tempTable.getItemCount()];
				String[] tempDateTable = new String[tempTable.getItemCount()];
				// sprawdzamy czy tabela zawiera elementy
				int k = tempTable.indexOf(tempColumn);
				for (int x = 0; x < tempTable.getItemCount(); x++) {
					tempIntTable[x] = Integer.valueOf(tempTable.getItem(x).getText(k).replaceAll("[^0-9]", "")).intValue();
					tempDateTable[x] = tempTable.getItem(x).getText(0);
				}

				graphComposite.setColumn(k);

				switch (k) {
				case PlayerTable.VALUE:
					graphComposite.fillGraph(tempIntTable, tempDateTable, Calendar.THURSDAY, false, -1);
					break;
				case PlayerTable.SALARY:
					graphComposite.fillGraph(tempIntTable, tempDateTable, Calendar.THURSDAY, true, -1);
					break;
				case PlayerTable.AGE:
					graphComposite.fillGraph(tempIntTable, tempDateTable, Calendar.THURSDAY, true, -1);
					break;
				case PlayerTable.STAMINA:
					graphComposite.fillGraph(tempIntTable, tempDateTable, Calendar.THURSDAY, true, 12);
					break;
				default:
					graphComposite.fillGraph(tempIntTable, tempDateTable, Calendar.THURSDAY, true, 18);
					break;
				}
				showDescription(graphComposite);
			}
		};

	}

	private void fillTree(List<Player> players) {
		PlayerComparator comparator = new PlayerComparator();
		comparator.setColumn(PlayerComparator.SURNAME);
		comparator.setDirection(PlayerComparator.ASCENDING);
		Collections.sort(players, comparator);

		for (int i = 0; i < players.size(); i++) {
			TreeItem item = new TreeItem(_treeItem, SWT.NONE);
			item.setData(Player.class.getName(), players.get(i));
			item.setText(players.get(i).getSurname() + " " + players.get(i).getName());
			item.setImage(FlagsResources.getFlag(players.get(i).getCountryfrom()));

			if (players.get(i).getSkills().length < 2) {
				item.setForeground(ConfigBean.getColorNewTreeObject());
			}

			if (players.get(i).getSkills()[players.get(i).getSkills().length - 1].getCards() > 2
				|| players.get(i).getSkills()[players.get(i).getSkills().length - 1].getInjurydays() > 0) {
				item.setForeground(ColorResources.getRed());
			}

			itemMap.put(players.get(i).getId(), item);

			if (players.get(i).getJunior() != null) {
				TreeItem juniorItem = new TreeItem(item, 0);
				juniorItem.setImage(ImageResources.getImageResources("junior.png"));
				juniorItem.setText(Messages.getString("tree.junior"));
				juniorItem.setData("juniorId", Integer.valueOf(players.get(i).getJunior().getId()));

				TreeItem chartItem = new TreeItem(juniorItem, SWT.NONE);
				chartItem.setText(Messages.getString("charts"));
				chartItem.setData("juniorCharts", players.get(i).getJunior());
				chartItem.setImage(ImageResources.getImageResources("chart_blue.png"));
			}

			TreeItem chartItem = new TreeItem(item, SWT.NONE);
			chartItem.setText(Messages.getString("charts"));
			chartItem.setData("playerCharts", players.get(i));
			chartItem.setImage(ImageResources.getImageResources("chart_blue.png"));

			TreeItem statisticsItem = new TreeItem(item, SWT.NONE);
			statisticsItem.setText(Messages.getString("matches"));
			statisticsItem.setData("playerStatistics", players.get(i));
			statisticsItem.setImage(ImageResources.getImageResources("player_history.png"));
		}

		Listener listener = new Listener() {

			public void handleEvent(Event event) {
				TreeItem item = null;
				switch (event.type) {
				case SWT.MouseDown:
					Point point = new Point(event.x, event.y);
					item = _treeItem.getParent().getItem(point);
					break;
				case SWT.KeyUp:
					for (int i = 0; i < _treeItem.getParent().getSelection().length; i++) {
						item = _treeItem.getParent().getSelection()[i];
					}
					break;
				}

				if (item != null) {
					if (item.getParentItem() != null && item.getParentItem().equals(_treeItem)) {
						showMainView(vComposite);
						comboFilter.setVisible(false);
						Player player = (Player) item.getData(Player.class.getName());

						if (event.type == SWT.MouseDown && event.button == 3) {
							setCbData(player);
							menuPopUp.setData("item", item);
							_treeItem.getParent().setMenu(menuPopUp);
							_treeItem.getParent().getMenu().setVisible(true);
						}
						showDescription(player);
						showView(player);

					} else if (item.equals(_treeItem)) {
						showMainView(vComposite);

						if (event.type == SWT.MouseDown && event.button == 3) {
							cbData = playersDescription.getText();
							_treeItem.getParent().setMenu(menuPopUpParentTree);
							_treeItem.getParent().getMenu().setVisible(true);
						}
						comboFilter.setVisible(true);
						showDescription(playersDescription);
						showView(playersTable);

					} else if (item.getData("juniorId") != null) {

						if (item.getParentItem().getParentItem().equals(_treeItem)) {
							showMainView(vComposite);
							comboFilter.setVisible(false);
							juniorTrainedTable.removeAll();
							Integer id = (Integer) item.getData("juniorId");
							juniorTrainedTable.fill(Cache.getJuniorsTrainedMap().get(id));
							juniorTrainedComposite.setStatsJuniorInfo(Cache.getJuniorsTrainedMap().get(id));
							showDescription(juniorTrainedComposite);
							showView(juniorTrainedTable);
						}
					} else if (item.getData("playerCharts") != null) {

						if (item.getParentItem().getParentItem().equals(_treeItem)) {
							if (item.getData("playerCharts") instanceof Player) {
								Player player = (Player) item.getData("playerCharts");
								graphsComposite.fill(player);
								showMainView(graphsComposite);
							}
						}
					} else if (item.getData("playerStatistics") != null) {

						if (item.getParentItem().getParentItem().equals(_treeItem)) {
							if (item.getData("playerStatistics") instanceof Player) {
								Player player = (Player) item.getData("playerStatistics");
								playerStatsComposite.fill(player);
								showMainView(playerStatsComposite);
							}
						}
					} else if (item.getData("juniorCharts") != null) {

						if (item.getParentItem().getData("juniorId") != null && item.getParentItem().getParentItem().getParentItem() != null
							&& item.getParentItem().getParentItem().getParentItem().equals(_treeItem)) {
							if (item.getData("juniorCharts") instanceof Junior) {
								Junior junior = (Junior) item.getData("juniorCharts");
								juniorGraphsComposite.fill(junior);
								showMainView(juniorGraphsComposite);
							}
						}
					}
				}
			}
		};
		_treeItem.getParent().addListener(SWT.MouseDown, listener);
		_treeItem.getParent().addListener(SWT.KeyUp, listener);
	}

	private void addPlayerView(Player player) {

		playerView = new PlayerTable(vComposite, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
		playerView.setLayoutData(viewFormData);
		playerView.setData(Player.class.getName(), player);

		playerView.addListener(SWT.MouseDoubleClick, new Listener() {

			public void handleEvent(Event event) {
				if (event.button == 1) {
					comboFilter.setVisible(true);
					_treeItem.getParent().setSelection(new TreeItem[] { _treeItem });
					showView(playersTable);
					showDescription(playersDescription);
				}
			}
		});

		playerView.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event event) {
				if (event != null && event.item != null && event.widget.getData(Player.class.getName()) != null) {
					TableItem item = (TableItem) event.item;
					if (currentDesc instanceof ChartDateComposite) {
						((ChartDateComposite) currentDesc).setMarkers((Date) item.getData("date"), Calendar.THURSDAY, Integer.valueOf(item
							.getText(((ChartDateComposite) currentDesc).getColumn())
							.replaceAll("[^0-9-]", "")));
					} else if (currentDesc instanceof PlayerDescriptionComposite) {
						int index = item.getParent().indexOf(item);
						playerDescription.setStatsPlayerInfo((Player) item.getParent().getData(Player.class.getName()), index);
						showDescription(playerDescription);
					}
				}
			}

		});

		playerView.addListener(SWT.MouseDown, new Listener() {

			public void handleEvent(Event event) {
				Point point = new Point(event.x, event.y);
				TableItem item = currentView.getItem(point);

				if (event.button == 3) {
					if (item != null) {
						if (item.getData("player_skill") != null) {
							menuPopUpHistory.setData("item", item);
							PlayerSkills pSkills = (PlayerSkills) item.getData("player_skill");
							if (pSkills.isPassTraining()) {
								menuPopUpHistory.getItem(1).setText(Messages.getString("popup.training.unpassed"));
							} else {
								menuPopUpHistory.getItem(1).setText(Messages.getString("popup.training.passed"));
							}
						}

						if (currentDesc instanceof PlayerDescriptionComposite) {
							playerView.setMenu(menuPopUpHistory);
							playerView.getMenu().setVisible(true);
						}
					} else {
						playerView.setMenu(menuClear);
						showDescription(previousDesc);
					}
				}
			}

		});
		playerView.fill(player);
		viewMap.put(player.getId(), playerView);
		for (int j = PlayerTable.VALUE; j <= PlayerTable.TEAMWORK; j++) {
			playerView.getColumn(j).addListener(SWT.Selection, graphList);
		}
	}

	private void addPopupMenu() {
		// added popup menu
		menuPopUp = new Menu(vComposite.getShell(), SWT.POP_UP);

		MenuItem menuItem;

		menuItem = new MenuItem(menuPopUp, SWT.PUSH);
		menuItem.setText(Messages.getString("popup.note.open"));
		menuItem.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event event) {
				if (menuPopUp.getData("item") != null) {
					Item item = (Item) menuPopUp.getData("item");
					if (item.getData(Player.class.getName()) != null) {
						openNote(item);
					}
				}
			}
		});

		menuItem = new MenuItem(menuPopUp, SWT.SEPARATOR);
		menuItem = new MenuItem(menuPopUp, SWT.PUSH);
		menuItem.setText(Messages.getString("popup.clipboard"));
		menuItem.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {
				TextTransfer textTransfer = TextTransfer.getInstance();
				clipboard.setContents(new Object[] { cbData }, new Transfer[] { textTransfer });
			}
		});

		menuItem = new MenuItem(menuPopUp, SWT.SEPARATOR);

		menuItem = new MenuItem(menuPopUp, SWT.PUSH);
		menuItem.setText(Messages.getString("popup.translate.player"));
		menuItem.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {
				if (menuPopUp.getData("item") != null) {
					Item item = (Item) menuPopUp.getData("item");
					if (item.getData(Player.class.getName()) != null && item.getData(Player.class.getName()) instanceof Player) {
						Player player = (Player) item.getData(Player.class.getName());
						ViewerHandler.getViewer().notifyListeners(IEvents.TRANSLATE_PLAYER, new TranslateEvent(player));
					}
				}
			}
		});

		menuItem = new MenuItem(menuPopUp, SWT.SEPARATOR);

		menuItem = new MenuItem(menuPopUp, SWT.PUSH);
		menuItem.setEnabled(false);
		menuItem.setText(Messages.getString("popup.import.player"));
		menuItem.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {
				// FIXME
				String[] extensions = { "*.sv_" };
				FileDialog fileDialog = new FileDialog(vComposite.getShell(), SWT.OPEN);

				fileDialog.setText(Messages.getString("confShell.chooser.title"));
				fileDialog.setFilterExtensions(extensions);
				fileDialog.setFilterPath(settings.getBaseDirectory());
				String tempPropsFile = fileDialog.open();

				if (tempPropsFile != null) {
					PlayerInterface player = null;
					try {
						player = OperationOnFile.serializePlayer(tempPropsFile);
					} catch (Exception e2) {
					}
					if (player != null) {
						if (menuPopUp.getData("item") != null) {
							TableItem item = (TableItem) menuPopUp.getData("item");
							if (item.getData(Player.class.getName()) != null) {
								Player currentPlayer = (Player) item.getData(Player.class.getName());

								if (player.getId() == currentPlayer.getId()) {
									try {
										playersManager.importPlayer(player);
									} catch (SQLException e1) {
										new BugReporter(composite.getDisplay()).openErrorMessage("ViewPlayer", e1);
									}

									MessageBox msg = new MessageBox(vComposite.getShell(), SWT.OK | SWT.ICON_INFORMATION);
									msg.setMessage(Messages.getString("message.import.player.text"));
									msg.open();
								} else {
									MessageDialog.openErrorMessage(vComposite.getShell(), Messages.getString("message.import.player.error.diffrent.text"));
								}
							}
						}
					} else {
						MessageDialog.openErrorMessage(vComposite.getShell(), Messages.getString("message.import.player.error.format.text"));
					}
				}
			}
		});

		menuItem = new MenuItem(menuPopUp, SWT.SEPARATOR);

		menuItem = new MenuItem(menuPopUp, SWT.PUSH);
		menuItem.setText(Messages.getString("popup.generateToPDF"));
		menuItem.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event event) {
				if (menuPopUp.getData("item") != null) {
					Item item = (Item) menuPopUp.getData("item");
					if (item.getData(Player.class.getName()) != null) {
						Player currentPlayer = (Player) item.getData(Player.class.getName());
						String[] extensions = { "*.pdf" };
						FileDialog fileDialog = new FileDialog(vComposite.getShell(), SWT.SAVE);
						fileDialog.setText(Messages.getString("confShell.chooser.title"));
						fileDialog.setFilterExtensions(extensions);
						fileDialog.setFilterPath(settings.getBaseDirectory());
						fileDialog.setFileName(currentPlayer.getSurname() + "_" + currentPlayer.getName());

						String propsInputFile = fileDialog.open();
						if (propsInputFile != null) {
							PDFexport.export(propsInputFile, currentPlayer);
						}
					}
				}
			}
		});

		menuClear = new Menu(vComposite.getShell(), SWT.POP_UP);
	}

	private void addPopupMenuHistory() {
		MenuItem menuItem;

		// added popup menu
		menuPopUpHistory = new Menu(vComposite.getShell(), SWT.POP_UP);
		menuItem = new MenuItem(menuPopUpHistory, SWT.PUSH);
		menuItem.setText(Messages.getString("popup.clipboard"));
		menuItem.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {
				TextTransfer textTransfer = TextTransfer.getInstance();
				clipboard.setContents(new Object[] { playerDescription.getText() }, new Transfer[] { textTransfer });
			}
		});

		menuItem = new MenuItem(menuPopUpHistory, SWT.PUSH);
		menuItem.setText(Messages.getString("popup.training.unpassed"));
		menuItem.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event event) {
				if (event.widget instanceof MenuItem) {
					Object o = ((MenuItem) event.widget).getParent().getData("item");
					if (o instanceof TableItem) {
						TableItem item = (TableItem) o;
						if (item.getData("player_skill") != null && item.getData("player_skill") instanceof PlayerSkills) {
							PlayerSkills pSkills = (PlayerSkills) item.getData("player_skill");
							try {
								playersManager.changePlayerPassTraining(pSkills);
								SVTable.markTraining(item, pSkills.isPassTraining());
							} catch (SQLException e) {
								new BugReporter(composite.getDisplay()).openErrorMessage("ViewPlayer", e);
							}
						}
					}
				}
			}
		});

	}

	private void addPopupMenuParentTree() {
		// added popup menu
		menuPopUpParentTree = new Menu(vComposite.getShell(), SWT.POP_UP);
		MenuItem menuItem = new MenuItem(menuPopUpParentTree, SWT.PUSH);
		menuItem.setText(Messages.getString("popup.clipboard"));
		menuItem.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {
				TextTransfer textTransfer = TextTransfer.getInstance();
				clipboard.setContents(new Object[] { cbData }, new Transfer[] { textTransfer });
			}
		});
	}

	private void addToolBar() {

		toolBarComposite = new Composite(vComposite, SWT.BORDER);

		FormData toolBarFormData = new FormData();
		toolBarFormData.top = new FormAttachment(100, -35);
		toolBarFormData.left = new FormAttachment(0, 0);
		toolBarFormData.right = new FormAttachment(100, 0);
		toolBarFormData.bottom = new FormAttachment(100, 0);

		toolBarComposite.setLayoutData(toolBarFormData);
		toolBarComposite.setLayout(new FormLayout());
		viewFormData.bottom = new FormAttachment(toolBarComposite, 0);
		comboFilter = new Combo(toolBarComposite, SWT.READ_ONLY);
		comboFilter.add(Messages.getString("view.all"));
		comboFilter.add(Messages.getString("view.jumps"));
		comboFilter.setText(Messages.getString("view.all"));
		comboFilter.setFont(ConfigBean.getFontMain());

		FormData formData = new FormData(100, 25);
		formData.top = new FormAttachment(0, 5);
		formData.left = new FormAttachment(0, 5);
		comboFilter.setLayoutData(formData);
		comboFilter.addListener(SWT.Selection, comboFilterListner);
	}

	private void addViewComposite() {
		playersTable = new PlayersTable(vComposite, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
		playersTable.setLayoutData(viewFormData);
		playersTable.setVisible(true);

		playersTable.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event event) {
				if (event != null) {
					playerDescription.setStatsPlayerInfo((Player) event.item.getData(Player.class.getName()));
					showDescription(playerDescription);
				}
			}

		});

		playersTable.addListener(SWT.MouseDown, new Listener() {

			public void handleEvent(Event event) {
				if (event.button == 3) {
					Point pt = new Point(event.x, event.y);
					TableItem item = playersTable.getItem(pt);
					if (item != null) {
						Player player = (Player) item.getData(Player.class.getName());
						menuPopUp.setData("item", item);
						setCbData(player);
						playersTable.setMenu(menuPopUp);
						playersTable.getMenu().setVisible(true);
					} else {
						playersTable.setMenu(menuClear);
						showDescription(playersDescription);
					}
				}
			}
		});
		playersTable.addListener(SWT.MouseDoubleClick, new Listener() {

			public void handleEvent(Event event) {
				Rectangle clientArea = playersTable.getClientArea();
				Point pt = new Point(event.x, event.y);
				int index = playersTable.getTopIndex();

				while (index < playersTable.getItemCount()) {
					boolean visible = false;
					TableItem item = playersTable.getItem(index);
					for (int i = 0; i < playersTable.getColumnCount(); i++) {
						Rectangle rect = item.getBounds(i);
						if (rect.contains(pt) && i != PlayerComparator.NOTE) {
							_treeItem.getParent().setSelection(
															   new TreeItem[] { (TreeItem) itemMap.get(((Player) item.getData(Player.class.getName()))
																   .getId()) });

							comboFilter.setVisible(false);
							showView((Player) item.getData(Player.class.getName()));
							showDescription((Player) item.getData(Player.class.getName()));
						} else if (rect.contains(pt) && i == PlayerComparator.NOTE) {
							openNote(item);
						}
						if (!visible && rect.intersects(clientArea)) {
							visible = true;
						}
					}
					if (!visible) {
						return;
					}
					index++;
				}
			}
		});

		final TableColumn[] columns = playersTable.getColumns();

		for (int i = 1; i < columns.length - 1; i++) {

			columns[i].addListener(SWT.Selection, new Listener() {

				public void handleEvent(Event event) {
					int column = playersTable.indexOf((TableColumn) event.widget);

					PlayerComparator comparator = playersTable.getComparator();

					if (column != comparator.getColumn()) {
						if (comparator.getDirection() == 0) {
							playersTable.getColumn(comparator.getColumn()).setText(
																				   playersTable
																					   .getColumn(comparator.getColumn())
																					   .getText()
																					   .replaceAll(ARROW_UP, ""));
						} else {
							playersTable.getColumn(comparator.getColumn()).setText(
																				   playersTable
																					   .getColumn(comparator.getColumn())
																					   .getText()
																					   .replaceAll(ARROW_DOWN, ""));
						}
						comparator.setDirection(0);
						playersTable.getColumn(column).setText(playersTable.getColumn(column).getText() + ARROW_UP);
					} else {
						if (comparator.getDirection() == 0) {
							playersTable.getColumn(comparator.getColumn()).setText(
																				   playersTable
																					   .getColumn(comparator.getColumn())
																					   .getText()
																					   .replaceAll(ARROW_UP, ARROW_DOWN));
						} else {
							playersTable.getColumn(comparator.getColumn()).setText(
																				   playersTable
																					   .getColumn(comparator.getColumn())
																					   .getText()
																					   .replaceAll(ARROW_DOWN, ARROW_UP));
						}
						comparator.reverseDirection();
					}

					comparator.setColumn(column);
					refillPlayersTable();
				}
			});
		}
	}

	/** refills the players list, keeping its filter */
	private void refillPlayersTable() {
		playersTable.setRedraw(false);
		playersTable.fill(players);
		playersTable.filterTable(comboFilter.getText());
		playersTable.setRedraw(true);
	}

	public void clear() {
		playersDescription.clearAll();
		playersTable.removeAll();
		_treeItem.removeAll();
		players.clear();
		viewMap.clear();
		itemMap.clear();
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
		return Messages.getString("progressBar.info.setInfoPlayers");
	}

	public TreeItem getTreeItem() {
		return _treeItem;
	}

	public void init(Composite composite) {

		FormData formData = new FormData();
		formData.top = new FormAttachment(0, 0);
		formData.left = new FormAttachment(0, 0);
		formData.right = new FormAttachment(100, 0);
		formData.bottom = new FormAttachment(100, 0);
		this.composite = composite;
		this.composite.setLayout(new FormLayout());

		this.vComposite = new ViewComposite(composite, composite.getStyle());
		this.vComposite.setLayoutData(formData);
		vComposite.setVisible(false);

		viewFormData = ((ViewComposite) this.vComposite).getViewFormData();
		descriptionFormData = ((ViewComposite) this.vComposite).getDescriptionFormData();

		graphsComposite = new PlayerChartsComposite(this.composite, SWT.NONE);
		graphsComposite.setLayoutData(formData);
		graphsComposite.setVisible(false);

		juniorGraphsComposite = new JuniorChartsComposite(this.composite, SWT.NONE);
		juniorGraphsComposite.setLayoutData(formData);
		juniorGraphsComposite.setVisible(false);

		playerStatsComposite = new PlayerStatsComposite(this.composite, SWT.BORDER);
		playerStatsComposite.setLayoutData(formData);
		playerStatsComposite.setVisible(false);

		showMainView(vComposite);

		clipboard = ViewerHandler.getClipboard();
		players = new ArrayList<Player>();

		viewMap = new HashMap<Integer, Table>();
		itemMap = new HashMap<Integer, TreeItem>();

		addListener();

		addViewComposite();

		playersDescription = new PlayersDescriptionComposite(vComposite, SWT.BORDER);
		playersDescription.setLayoutData(descriptionFormData);
		playersDescription.setVisible(false);

		graphComposite = new ChartDateComposite(vComposite, SWT.BORDER);
		graphComposite.setLayoutData(descriptionFormData);
		graphComposite.setVisible(false);

		playerDescription = new PlayerDescriptionComposite(vComposite, SWT.BORDER);
		playerDescription.setLayoutData(descriptionFormData);
		playerDescription.setVisible(false);

		universalJuniorComposite = new JuniorTrainedDescriptionComposite(vComposite, SWT.BORDER);
		universalJuniorComposite.setLayoutData(descriptionFormData);
		universalJuniorComposite.setVisible(false);

		addJuniorTrainedView();

		juniorTrainedComposite = new JuniorTrainedDescriptionComposite(vComposite, SWT.BORDER);
		juniorTrainedComposite.setLayoutData(descriptionFormData);
		juniorTrainedComposite.setVisible(false);

		addToolBar();
		addPopupMenu();
		addPopupMenuParentTree();
		addPopupMenuHistory();

		currentDesc = playersDescription;
		currentView = playersTable;

		composite.layout();
	}

	private void openNote(Item item) {
		Player player = (Player) item.getData(Player.class.getName());
		final NoteShell noteShell = new NoteShell(vComposite.getShell(), SWT.PRIMARY_MODAL | SWT.CLOSE);
		noteShell.setPerson(player);
		noteShell.open();

		if (item instanceof TreeItem) {
			playersTable.fill(players);
		} else if (item instanceof TableItem && player.getNote() != null) {
			if (player.getNote().isEmpty()) {
				((TableItem) item).setImage(PlayerComparator.NOTE, null);
			} else {
				((TableItem) item).setImage(PlayerComparator.NOTE, ImageResources.getImageResources("note.png"));
			}
		}
	}

	public void reload() {
	}

	private void setCbData(Player player) {
		// TODO do poprawy !!!!!!!!
		// FIXME: uzyc narzedzia do raportowania pilkarzy
		cbData = String.format("%-20s%-15s\r\n", new Object[] { Messages.getString("club"),
															   Cache.getClub().getClubName().get(Cache.getClub().getClubName().size() - 1).getName() });
		if (player.getJunior() != null) {

			Junior junior = player.getJunior();
			cbData += "====================================\r\n";
			cbData += String.format("%-35s\r\n", new Object[] { Messages.getString("junior") });
			cbData += "====================================\r\n";
			cbData += String.format("%-20s%-15s\r\n", new Object[] { Messages.getString("junior.id"), junior.getId() });
			cbData += String.format("%-20s%-15s\r\n",
									new Object[] {
												  Messages.getString("junior.skill"),
												  Messages.getString("skill.a" + junior.getSkills()[junior.getSkills().length - 1].getSkill()) + "["
																  + junior.getSkills()[junior.getSkills().length - 1].getSkill() + "]" });
			cbData += String.format("%-20s%-15s\r\n", new Object[] { Messages.getString("junior.weeksAll"), junior.getSkills()[0].getWeeks() });
			cbData += String.format("%-20s%-15s\r\n", new Object[] { Messages.getString("junior.numberOfJumps"), junior.getPops() });
			cbData += String.format("%-20s%-15s\r\n", new Object[] { Messages.getString("junior.averageJumps"), junior.getAveragePops() });
		}
	}

	public void setSettings(SokkerViewerSettings sokkerViewerSettings) {
		this.settings = sokkerViewerSettings;
	}

	public void setSvBean(SvBean svBean) {
	}

	public void setTreeItem(TreeItem treeItem) {
		this._treeItem = treeItem;
		_treeItem.setData("view", this);
		_treeItem.setText(Messages.getString("tree.ViewPlayers"));
		_treeItem.setImage(ImageResources.getImageResources("player.png"));
	}

	public void set() {
		players = Cache.getPlayers();
		playersTable.fill(players);
		playersDescription.setDescription(players);
		fillTree(players);
		vComposite.layout(true);
	}

	private void showDescription(Composite composite) {
		if (!(currentDesc instanceof ChartDateComposite)) {
			previousDesc = currentDesc;
		}
		currentDesc.setVisible(false);
		currentDesc = composite;
		currentDesc.setVisible(true);
	}

	private void showDescription(Player player) {
		playerDescription.setStatsPlayerInfo(player);
		showDescription(playerDescription);
	}

	private void showMainView(Composite composite) {
		composite.getParent().setRedraw(false);
		if (currentComposite != null) {
			currentComposite.setVisible(false);
		}
		currentComposite = composite;
		currentComposite.setVisible(true);
		composite.getParent().setRedraw(true);
	}

	private void showView(int id) {
		currentView.setVisible(false);
		currentView = (Table) viewMap.get(id);
		currentView.setVisible(true);
	}

	private void showView(Player player) {
		if (viewMap.get(player.getId()) == null) {
			addPlayerView(player);
			vComposite.layout(new Control[] { viewMap.get(player.getId()) });
		}
		showView(player.getId());
	}

	private void showView(Table table) {
		currentView.setVisible(false);
		currentView = table;
		currentView.setVisible(true);
	}
}
