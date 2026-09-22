package pl.pronux.sokker.bean;

/** what the standings of one league round need to know, straight from the database */
public class RoundState {

	private final int leagueId;

	private final int season;

	private final int round;

	/** teams in the season, counted from its fixtures */
	private final int teams;

	/** matches the database has for the whole season */
	private final int seasonMatches;

	/** matches the database has for this round */
	private final int matches;

	/** of those, played */
	private final int finished;

	/** standings rows already stored for this round */
	private final int standings;

	public RoundState(int leagueId, int season, int round, int teams, int seasonMatches, int matches, int finished, int standings) {
		this.leagueId = leagueId;
		this.season = season;
		this.round = round;
		this.teams = teams;
		this.seasonMatches = seasonMatches;
		this.matches = matches;
		this.finished = finished;
		this.standings = standings;
	}

	public int getLeagueId() {
		return leagueId;
	}

	public int getSeason() {
		return season;
	}

	public int getRound() {
		return round;
	}

	public int getTeams() {
		return teams;
	}

	public int getSeasonMatches() {
		return seasonMatches;
	}

	public int getMatches() {
		return matches;
	}

	public int getFinished() {
		return finished;
	}

	public int getStandings() {
		return standings;
	}
}
