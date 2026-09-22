package pl.pronux.sokker.data.sql.dto;

import java.sql.ResultSet;
import java.sql.SQLException;

import pl.pronux.sokker.model.LeagueSeason;
import pl.pronux.sokker.model.SokkerDate;

public class LeagueSeasonDto extends LeagueSeason {

	private ResultSet rs;

	public LeagueSeasonDto(ResultSet rs) {
		this.rs = rs;
	}

	public LeagueSeason getLeagueSeason() throws SQLException {
		this.setLeagueId(rs.getInt("league_id")); 
		this.setSeason(rs.getInt("season")); 
		// the season the site counts, from the season's first week
		this.setRawSeason(SokkerDate.seasonOf(rs.getInt("week")));
//		this.setSeasonRoundID(rs.getInt("season_round_id"));
		return this;
	}
}
