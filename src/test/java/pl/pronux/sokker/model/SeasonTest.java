package pl.pronux.sokker.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.TimeZone;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SeasonTest {

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

	@Test
	public void seasonFromWeek() {
		Season season = new Season(1210);
		assertEquals(79, season.getSeasonNumber());
		assertEquals(0, season.getSeasonWeek());
	}

	@Test
	public void seasonFromMillisGoesThroughTheCorrectedWeek() {
		Season season = new Season(1789923997950L);
		assertEquals(79, season.getSeasonNumber());
		assertEquals(0, season.getSeasonWeek());
	}

	@Test
	public void dateUsesItsStoredWeekWhenItHasOne() {
		Date date = new Date(1789923997950L);
		date.setSokkerDate(new SokkerDate(5, 1209));
		assertEquals(78, date.getSeason().getSeasonNumber());
		assertEquals(12, date.getSeason().getSeasonWeek());
	}

	@Test
	public void lastWeekFollowsTheSeasonLength() {
		assertTrue(new Season(975).isLastWeek());
		assertTrue(new Season(1209).isLastWeek());
		assertFalse(new Season(1210).isLastWeek());
	}
}
