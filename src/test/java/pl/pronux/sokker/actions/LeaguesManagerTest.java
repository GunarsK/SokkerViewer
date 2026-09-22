package pl.pronux.sokker.actions;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import pl.pronux.sokker.bean.RoundState;
import pl.pronux.sokker.model.LeagueSeason;

public class LeaguesManagerTest {

	private static RoundState state(int round, int teams, int seasonMatches, int matches, int finished, int standings) {
		return new RoundState(16684, 68, round, teams, seasonMatches, matches, finished, standings);
	}

	private static List<Integer> rounds(RoundState... states) {
		List<RoundState> list = new ArrayList<RoundState>();
		for (RoundState s : states) {
			list.add(s);
		}
		List<Integer> numbers = new ArrayList<Integer>();
		for (LeagueSeason season : LeaguesManager.roundsNeedingStandings(list)) {
			numbers.add(Integer.valueOf(season.getRound()));
		}
		return numbers;
	}

	@Test
	public void playedRoundsOfAFullTwelveTeamSeasonWithoutStandingsAreSelected() {
		assertEquals("[1, 2]", rounds(state(1, 12, 132, 6, 6, 0), state(2, 12, 132, 6, 6, 0), state(3, 12, 132, 6, 4, 0)).toString());
	}

	@Test
	public void eightTeamSeasonsStillWork() {
		assertEquals("[1]", rounds(state(1, 8, 56, 4, 4, 0), state(2, 8, 56, 4, 3, 0)).toString());
	}

	@Test
	public void roundsWithCompleteStandingsAreSkippedPartialOnesRedone() {
		assertEquals("[2]", rounds(state(1, 12, 132, 6, 6, 12), state(2, 12, 132, 6, 6, 5)).toString());
	}

	@Test
	public void aSeasonTheDatabaseOnlyPartlyKnowsGetsNoTable() {
		// own matches only: 9 home teams seen, 16 matches, never 72
		assertEquals("[]", rounds(state(1, 9, 16, 1, 1, 0), state(15, 9, 16, 1, 1, 0)).toString());
	}

	@Test
	public void keepsTheDatabaseOrder() {
		assertEquals("[1, 2, 3]", rounds(state(1, 12, 132, 6, 6, 0), state(2, 12, 132, 6, 6, 0), state(3, 12, 132, 6, 6, 0)).toString());
	}
}
