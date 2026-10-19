package pl.pronux.sokker.ui.plugins;

import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeItem;

import pl.pronux.sokker.bean.SvBean;
import pl.pronux.sokker.data.cache.Cache;
import pl.pronux.sokker.model.Club;
import pl.pronux.sokker.model.League;
import pl.pronux.sokker.model.LeagueRound;
import pl.pronux.sokker.model.LeagueSeason;
import pl.pronux.sokker.model.Match;
import pl.pronux.sokker.model.SokkerViewerSettings;
import pl.pronux.sokker.resources.Messages;
import pl.pronux.sokker.ui.interfaces.IPlugin;
import pl.pronux.sokker.ui.interfaces.IViewConfigure;
import pl.pronux.sokker.ui.resources.ColorResources;
import pl.pronux.sokker.ui.resources.ImageResources;
import pl.pronux.sokker.ui.widgets.composites.views.LeagueComposite;
import pl.pronux.sokker.ui.widgets.tabs.MatchTabComposite;

public class ViewLeague implements IPlugin {

	private Composite composite;

	private TreeItem treeItem;

	private List<LeagueSeason> leagueSeasons;

	private LeagueComposite leagueComposite;

	private MatchTabComposite matchComposite;

	private FormData viewFormData;

	private Map<Integer, Club> clubMap;

	private Composite current;

	private LeagueRound shownRound;

	private Match shownMatch;

	public void clear() {

	}

	public void dispose() {

	}

	public Composite getComposite() {
		return this.composite;
	}

	public IViewConfigure getConfigureComposite() {
		return null;
	}

	public String getInfo() {
		return this.getClass().getSimpleName();
	}

	public String getStatusInfo() {
		return Messages.getString("progressBar.info.setInfoLeague");
	}

	public TreeItem getTreeItem() {
		return this.treeItem;
	}

	public void init(Composite composite) {
		this.composite = composite;
		
		viewFormData = new FormData();
		viewFormData.top = new FormAttachment(0,0);
		viewFormData.left = new FormAttachment(0,0);
		viewFormData.right = new FormAttachment(100,0);
		viewFormData.bottom = new FormAttachment(100,0);
		
		composite.setLayout(new FormLayout());
		addView(composite);
		composite.layout();
	}

	private void addView(Composite composite) {
		leagueComposite = new LeagueComposite(composite, SWT.NONE);
		leagueComposite.setLayoutData(viewFormData);
		leagueComposite.setVisible(false);
		show(leagueComposite);
		
		matchComposite = new MatchTabComposite(composite, SWT.NONE);
		matchComposite.setLayoutData(viewFormData);
		matchComposite.setVisible(false);
	}

	public void setSettings(SokkerViewerSettings sokkerViewerSettings) {

	}

	public void setSvBean(SvBean svBean) {

	}

	public void setTreeItem(final TreeItem treeItem) {
		this.treeItem = treeItem;
		treeItem.setText(Messages.getString("tree.ViewLeague"));
		treeItem.setImage(ImageResources.getImageResources("league.png"));

		treeItem.getParent().addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event event) {
				showItem(treeItem.getParent().getItem(new Point(event.x, event.y)));
			}
		});
		treeItem.getParent().addListener(SWT.KeyUp, new Listener() {
			public void handleEvent(Event event) {
				TreeItem[] selection = treeItem.getParent().getSelection();
				showItem(selection.length > 0 ? selection[selection.length - 1] : null);
			}
		});
	}

	private void showItem(TreeItem item) {
		// the tree is shared: Matches and Spy hang Match data on their own items
		if (item == null || !isUnderLeague(item)) {
			return;
		}
		if (item.getData(LeagueRound.class.getName()) != null) {
			shownRound = (LeagueRound) item.getData(LeagueRound.class.getName());
			leagueComposite.fillLeague(shownRound);
			show(leagueComposite);
		}
		if (item.getData(Match.class.getName()) != null) {
			shownMatch = (Match) item.getData(Match.class.getName());
			matchComposite.fill(shownMatch);
			show(matchComposite);
		}
	}

	private boolean isUnderLeague(TreeItem item) {
		for (TreeItem parent = item.getParentItem(); parent != null; parent = parent.getParentItem()) {
			if (parent == treeItem) {
				return true;
			}
		}
		return false;
	}

	public void set() {
		leagueSeasons = Cache.getLeagueSeasons();
		clubMap = Cache.getClubMap();
		fillTree(this.treeItem, leagueSeasons);
		refreshShown();
	}

	/** Refills the open round or match from the freshly loaded seasons, so a sync shows its results at once */
	private void refreshShown() {
		for (LeagueSeason leagueSeason : leagueSeasons) {
			for (LeagueRound round : leagueSeason.getRounds()) {
				if (shownRound != null && sameRound(round, shownRound)) {
					shownRound = round;
					leagueComposite.fillLeague(round);
				}
				for (Match match : round.getMatches()) {
					if (shownMatch != null && match.getMatchId() == shownMatch.getMatchId()) {
						shownMatch = match;
						int tab = matchComposite.getSelectionIndex();
						matchComposite.fill(match); // fill() jumps to the first tab
						matchComposite.setSelection(tab);
					}
				}
			}
		}
	}

	private boolean sameRound(LeagueRound a, LeagueRound b) {
		return a.getRoundNumber() == b.getRoundNumber() && a.getLeagueSeason().getLeagueId() == b.getLeagueSeason().getLeagueId()
			&& a.getLeagueSeason().getSeason() == b.getLeagueSeason().getSeason();
	}

	private void fillTree(TreeItem treeItem, List<LeagueSeason> leagueSeasons) {
		treeItem.removeAll();
		for (LeagueSeason leagueSeason : leagueSeasons) {
			if (leagueSeason.getLeague() != null && leagueSeason.getLeague().getType() == League.TYPE_LEAGUE) {
				TreeItem treeItemSeason = new TreeItem(treeItem, SWT.NONE);
				treeItemSeason.setImage(ImageResources.getImageResources("seasons.png"));
				treeItemSeason.setText(String.format("%s %2d (%s)", Messages.getString("league.season"), leagueSeason.getRawSeason(), leagueSeason.getLeague().getName()));
				treeItemSeason.setData(LeagueSeason.class.getName(), leagueSeason);
				for (LeagueRound round : leagueSeason.getRounds()) {
					TreeItem treeItemRound = new TreeItem(treeItemSeason, SWT.NONE);
					if (round.getRoundNumber() == 0) {
						treeItemRound.setImage(ImageResources.getImageResources("round0.png"));
					} else if (round.getRoundNumber() == 15) {
						treeItemRound.setImage(ImageResources.getImageResources("round15.png"));
					} else {
						treeItemRound.setImage(ImageResources.getImageResources("round.png"));
					}

					treeItemRound.setText(Messages.getString("league.round") + " " + round.getRoundNumber());
					treeItemRound.setData(LeagueRound.class.getName(), round);
					for (Match match : round.getMatches()) {
						TreeItem treeItemMatch = new TreeItem(treeItemRound, SWT.NONE);
						treeItemMatch.setImage(ImageResources.getImageResources("match.png"));
						treeItemMatch.setText(teamName(match.getHomeTeamName(), match.getHomeTeamId()) + " - " + teamName(match.getAwayTeamName(), match.getAwayTeamId()));
						treeItemMatch.setData(Match.class.getName(), match);
						if (match.getIsFinished() == Match.NOT_FINISHED) {
							treeItemMatch.setForeground(ColorResources.getGray());
							treeItemRound.setForeground(ColorResources.getGray());
						}
					}
				}
			}
		}
	}
	
	/** The match's own team name, else the club's first known name, else the team id */
	private String teamName(String name, int teamId) {
		if (name != null) {
			return name;
		}
		Club club = clubMap.get(teamId);
		if (club != null && club.getClubName().get(0).getName() != null) {
			return club.getClubName().get(0).getName();
		}
		return String.valueOf(teamId);
	}

	private void show(Composite composite) {
		if (composite == current) {
			return;
		}
		if (current != null) {
			current.setVisible(false);
		}
		current = composite;
		current.setVisible(true);
	}

	public void reload() {
		// TODO Auto-generated method stub

	}

}
