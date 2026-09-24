package pl.pronux.sokker.model;

import java.util.Calendar;

import pl.pronux.sokker.interfaces.DateConst;

public class SokkerDate {

	public static final byte SATURDAY = 0;
	public static final byte SUNDAY = 1;
	public static final byte MONDAY = 2;
	public static final byte TUESDAY = 3;
	public static final byte WEDNESDAY = 4;
	public static final byte THURSDAY = 5;
	public static final byte FRIDAY = 6;

//	public static final long beginDate = 1057470289859l - Date.day;
//	public static final long BEGIN_DATE = 1057379449859l - DateConst.HOUR * 6 - DateConst.MINUTE * 30 - DateConst.SECOND * 49 - 859;
	public static final long BEGIN_DATE = 1057356000000l + DateConst.SECOND;

	/**
	 * sokker paused for a week in the summer of 2025 without moving its week counter, so a
	 * calendar date after this maps to one week less than before (and a week to one more
	 * calendar week, see Date.getTrainingDate). Real dates more than a week past it map one
	 * week lower (see the week formulas); dates inside the pause week itself are not modelled.
	 */
	public static final long PAUSE = 1753833600000L;

	/** seasons had 16 weeks until this week (week 1 of season 61 on the site, week 0 here) and 13 since */
	public static final int SHORT_SEASONS_FROM_WEEK = 976;
	public static final int SHORT_SEASONS_FROM_SEASON = 61;
	public static final int LONG_SEASON_WEEKS = 16;
	public static final int SHORT_SEASON_WEEKS = 13;

	public static int seasonOf(int week) {
		if (week < SHORT_SEASONS_FROM_WEEK) {
			return week / LONG_SEASON_WEEKS;
		}
		return SHORT_SEASONS_FROM_SEASON + (week - SHORT_SEASONS_FROM_WEEK) / SHORT_SEASON_WEEKS;
	}

	/** week within the season, counted from 0 as it always was here */
	public static int seasonWeekOf(int week) {
		if (week < SHORT_SEASONS_FROM_WEEK) {
			return week % LONG_SEASON_WEEKS;
		}
		return (week - SHORT_SEASONS_FROM_WEEK) % SHORT_SEASON_WEEKS;
	}

	public static int seasonLength(int week) {
		return week < SHORT_SEASONS_FROM_WEEK ? LONG_SEASON_WEEKS : SHORT_SEASON_WEEKS;
	}

	public static boolean isLastWeekOf(int week) {
		return seasonWeekOf(week) == seasonLength(week) - 1;
	}

	private int day;

	private int week;

	public SokkerDate() {
	}

	public SokkerDate(long millis) {
		this.day = convertUpdateMillisToDay(millis);
		this.week = convertUpdateMillisToWeek(millis);
	}

	public SokkerDate(int day, int week) {
		this.day = day;
		this.week = week;
	}


	public static int convertMillisToDay(long millis) {
		int offset = 0 ;
		long timezone1 = 0;
		long begin;
//		begin = SokkerDate.BEGIN_DATE  - (6 * Date.HOUR) - (31 * Date.MINUTE);
		begin = SokkerDate.BEGIN_DATE;
		Calendar ca1 = Calendar.getInstance();
		Calendar ca2 = Calendar.getInstance();
		timezone1 = ca1.get(Calendar.ZONE_OFFSET) - DateConst.HOUR;
//		ca1.setTimeZone(TimeZone.getTimeZone("GMT+01:00"));
//		ca2.setTimeZone(TimeZone.getTimeZone("GMT+01:00"));

		ca1.setTimeInMillis(begin + timezone1);
		ca2.setTimeInMillis(millis + timezone1);

//		ca1.setTimeZone(TimeZone.getTimeZone("GMT+1:00"));
//		ca2.setTimeZone(TimeZone.getTimeZone("GMT+1:00"));
		offset = ca2.get(Calendar.DST_OFFSET) - ca1.get(Calendar.DST_OFFSET);

		return Long.valueOf(((ca2.getTimeInMillis() - begin + offset ) / DateConst.DAY % 7)).intValue();
	}

	public static int convertUpdateMillisToDay(long millis) {
//		int offset;
//		long begin;
//		begin = SokkerDate.beginDate;
//		Calendar ca1 = Calendar.getInstance();
//		Calendar ca2 = Calendar.getInstance();
//		ca1.setTimeInMillis(begin);
//		ca2.setTimeInMillis(millis );
//		ca1.setTimeZone(Calendar.getInstance().getTimeZone());
//		ca2.setTimeZone(Calendar.getInstance().getTimeZone());
//		offset = ca2.get(Calendar.DST_OFFSET) - ca1.get(Calendar.DST_OFFSET);
		int offset = 0 ;
		long timezone1 = 0;
		long begin;
		begin = SokkerDate.BEGIN_DATE;
//		begin = SokkerDate.BEGIN_DATE  - (6 * Date.HOUR) - (31 * Date.MINUTE);
		Calendar ca1 = Calendar.getInstance();
		Calendar ca2 = Calendar.getInstance();
		timezone1 = ca1.get(Calendar.ZONE_OFFSET) - DateConst.HOUR;
//		ca1.setTimeZone(TimeZone.getTimeZone("GMT+01:00"));
//		ca2.setTimeZone(TimeZone.getTimeZone("GMT+01:00"));

		ca1.setTimeInMillis(begin + timezone1);
		ca2.setTimeInMillis(millis + timezone1);

//		ca1.setTimeZone(TimeZone.getTimeZone("GMT+1:00"));
//		ca2.setTimeZone(TimeZone.getTimeZone("GMT+1:00"));
		offset = ca2.get(Calendar.DST_OFFSET) - ca1.get(Calendar.DST_OFFSET);

		return Long.valueOf(((ca2.getTimeInMillis() - begin + offset  ) / DateConst.DAY % 7)).intValue();
	}

	/**
	 * the millisecond the given day of the given sokker week starts at: the inverse of
	 * convertUpdateMillisToWeek, and the only other place the pause is applied
	 */
	public static long weekToMillis(int week, int day) {
		Calendar begin = Calendar.getInstance();
		Calendar target = Calendar.getInstance();
		begin.setTimeInMillis(BEGIN_DATE);
		target.setTimeInMillis(BEGIN_DATE + DateConst.WEEK * week + day * DateConst.DAY);
		// a week long pause sokker did not count: weeks from it on fall a calendar week later
		if (target.getTimeInMillis() > PAUSE) {
			target.setTimeInMillis(target.getTimeInMillis() + DateConst.WEEK);
		}
		int offset = target.get(Calendar.DST_OFFSET) - begin.get(Calendar.DST_OFFSET);
		return target.getTimeInMillis() - offset;
	}

	public static int convertUpdateMillisToWeek(long millis) {
//		int offset;
//		long begin;
//		begin = SokkerDate.beginDate ;
//		Calendar ca1 = Calendar.getInstance();
//		Calendar ca2 = Calendar.getInstance();
//		ca1.setTimeInMillis(begin);
//		ca2.setTimeInMillis(millis);
//		ca1.setTimeZone(Calendar.getInstance().getTimeZone());
//		ca2.setTimeZone(Calendar.getInstance().getTimeZone());
//		offset = ca2.get(Calendar.DST_OFFSET) - ca1.get(Calendar.DST_OFFSET);
//		return Long.valueOf((ca2.getTimeInMillis() - begin   + offset) / DateConst.week).intValue();
		int offset = 0 ;
		long timezone1 = 0;
		long begin;
		begin = SokkerDate.BEGIN_DATE;
		Calendar ca1 = Calendar.getInstance();
		Calendar ca2 = Calendar.getInstance();
		timezone1 = ca1.get(Calendar.ZONE_OFFSET) - DateConst.HOUR;
//		ca1.setTimeZone(TimeZone.getTimeZone("GMT+01:00"));
//		ca2.setTimeZone(TimeZone.getTimeZone("GMT+01:00"));

		ca1.setTimeInMillis(begin + timezone1);
		ca2.setTimeInMillis(millis + timezone1);

//		ca1.setTimeZone(TimeZone.getTimeZone("GMT+1:00"));
//		ca2.setTimeZone(TimeZone.getTimeZone("GMT+1:00"));
		offset = ca2.get(Calendar.DST_OFFSET) - ca1.get(Calendar.DST_OFFSET);
		int week = Long.valueOf((ca2.getTimeInMillis() - begin   + offset) / DateConst.WEEK).intValue();
		// the inverse of the correction in Date.getTrainingDate: a real date more than a week past
		// the pause came from a week one lower than the plain division says
		if (millis > PAUSE + DateConst.WEEK) {
			week--;
		}
		return week;
	}
	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public int getWeek() {
		return week;
	}

	public void setWeek(int week) {
		this.week = week;
	}
	
	public int getSeasonWeek() {
		return seasonWeekOf(week);
	}

	public int getSeason() {
		return seasonOf(week);
	}

	public boolean isLastSeasonWeek() {
		return isLastWeekOf(week);
	}

	/** the season whose age players have on this day */
	public int getAgeSeason() {
		return isLastSeasonWeek() && day == FRIDAY ? getSeason() + 1 : getSeason();
	}

	public int getTrainingWeek() {
		if(this.day >= SokkerDate.THURSDAY) {
			return this.week;
		} else {
			return this.week-1;
		}
	}
}
