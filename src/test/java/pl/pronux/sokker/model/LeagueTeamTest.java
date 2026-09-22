package pl.pronux.sokker.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LeagueTeamTest {

	/** points, 500 + goal difference, goals scored, wins, begin place - the last three written with %03d */
	private static LeagueTeam ranked(String rankTotal) {
		LeagueTeam team = new LeagueTeam();
		team.setRankTotal(rankTotal);
		return team;
	}

	@Test
	public void beginPlaceIsTheWholeThreeDigitField() {
		assertEquals(10, ranked("3501002001010").getBeginPlace());
		assertEquals(12, ranked("3501002001012").getBeginPlace());
	}

	@Test
	public void eightTeamPlacesStillRead() {
		assertEquals(8, ranked("3501002001008").getBeginPlace());
		assertEquals(1, ranked("3501002001001").getBeginPlace());
	}

	/** round 0 rows come from sokker's league xml, where the rank is just the place */
	@Test
	public void theBarePlaceFromTheLeagueXmlIsRead() {
		assertEquals(2, ranked("2").getBeginPlace());
		assertEquals(12, ranked("12").getBeginPlace());
	}

	@Test
	public void aTeamWithoutARankDoesNotThrow() {
		assertEquals(0, new LeagueTeam().getBeginPlace());
		assertEquals(0, ranked("").getBeginPlace());
	}

	@Test
	public void anExplicitPlaceIsKept() {
		LeagueTeam team = ranked("3501002001008");
		team.setBeginPlace(11);
		assertEquals(11, team.getBeginPlace());
	}
}
