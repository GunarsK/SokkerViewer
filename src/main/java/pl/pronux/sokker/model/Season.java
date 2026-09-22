package pl.pronux.sokker.model;

public class Season {
	private final int week;

	public Season(long date) {
		this(SokkerDate.convertUpdateMillisToWeek(date));
	}

	public Season(int week) {
		this.week = week;
	}

	/** the season's final week, 16th until week 976 and 13th since */
	public boolean isLastWeek() {
		return SokkerDate.isLastWeekOf(week);
	}

	public int getSeasonNumber() {
		return SokkerDate.seasonOf(week);
	}

//	public void setSeasonNumber(int seasonNumber) {
//		this.seasonNumber = seasonNumber;
//	}

	public int getSeasonWeek() {
		return SokkerDate.seasonWeekOf(week);
	}

//	public void setSeasonWeek(int seasonWeek) {
//		this.seasonWeek = seasonWeek;
//	}


//	public void setDate(Date date) {
//		this.date = date;
//	}
	
}
