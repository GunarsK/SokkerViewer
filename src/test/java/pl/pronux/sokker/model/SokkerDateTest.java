package pl.pronux.sokker.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.TimeZone;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SokkerDateTest {

	private static TimeZone defaultTimeZone;

	@BeforeClass
	public static void useSokkerTime() {
		defaultTimeZone = TimeZone.getDefault();
		// the week formulas anchor on polish midnight (see the ZONE_OFFSET - HOUR trick); a
		// utc+9 default would move the fixtures across a day boundary
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/Warsaw"));
	}

	@AfterClass
	public static void restoreTimeZone() {
		TimeZone.setDefault(defaultTimeZone);
	}

	/** training row 1 of a real database: 2011-11-25 17:52 UTC, stored as week 437 day 6 */
	private static final long BEFORE_PAUSE = 1322243576109L;

	/** training row 593 of the same database: 2026-09-20 17:06 UTC (sunday), stored as week 1210 day 1 */
	private static final long AFTER_PAUSE = 1789923997950L;

	/** thursday 2026-09-17 11:00 UTC: sokker's week 1209 day 5 */
	private static final long THURSDAY_1209 = 1789642800000L;

	@Test
	public void weekFromMillisMatchesSokkerBeforeThePause() {
		SokkerDate date = new SokkerDate(BEFORE_PAUSE);
		assertEquals(437, date.getWeek());
		assertEquals(6, date.getDay());
	}

	@Test
	public void weekFromMillisMatchesSokkerAfterThePause() {
		SokkerDate sunday = new SokkerDate(AFTER_PAUSE);
		assertEquals(1210, sunday.getWeek());
		assertEquals(1, sunday.getDay());
		SokkerDate thursday = new SokkerDate(THURSDAY_1209);
		assertEquals(1209, thursday.getWeek());
		assertEquals(5, thursday.getDay());
	}
	@Test
	public void trainingDateRoundTripsThroughTheWeekFormula() {
		for (int week : new int[] { 437, 975, 976, 1209, 1210 }) {
			Date date = new Date(0L);
			date.setSokkerDate(new SokkerDate(SokkerDate.THURSDAY, week));
			Date thursday = date.getTrainingDate(SokkerDate.THURSDAY);
			SokkerDate back = new SokkerDate(thursday.getMillis() + 12 * 3600000L);
			assertEquals("week " + week, week, back.getWeek());
			assertEquals("week " + week, SokkerDate.THURSDAY, back.getDay());
		}
	}

	@Test
	public void seasonsHadSixteenWeeksUntil976AndThirteenSince() {
		assertEquals(60, SokkerDate.seasonOf(975));
		assertEquals(15, SokkerDate.seasonWeekOf(975));
		assertEquals(61, SokkerDate.seasonOf(976));
		assertEquals(0, SokkerDate.seasonWeekOf(976));
		assertEquals(78, SokkerDate.seasonOf(1209));
		assertEquals(12, SokkerDate.seasonWeekOf(1209));
		assertEquals(79, SokkerDate.seasonOf(1210));
		assertEquals(0, SokkerDate.seasonWeekOf(1210));
		assertEquals(16, SokkerDate.seasonLength(975));
		assertEquals(13, SokkerDate.seasonLength(976));
	}

	@Test
	public void lastSeasonWeekFollowsTheSeasonLength() {
		assertTrue(new SokkerDate(5, 975).isLastSeasonWeek());
		assertFalse(new SokkerDate(5, 974).isLastSeasonWeek());
		assertTrue(new SokkerDate(5, 1209).isLastSeasonWeek());
		assertFalse(new SokkerDate(5, 1210).isLastSeasonWeek());
		assertEquals(78, new SokkerDate(5, 1209).getSeason());
		assertEquals(12, new SokkerDate(5, 1209).getSeasonWeek());
	}
}
