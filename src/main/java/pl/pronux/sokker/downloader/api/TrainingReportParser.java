package pl.pronux.sokker.downloader.api;

import java.io.IOException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import pl.pronux.sokker.model.PlayerSkills;
import pl.pronux.sokker.model.Training;

/** turns an /api/training document into a TrainingWeek */
public class TrainingReportParser {

	public static TrainingWeek parseWeek(String json) throws IOException {
		JsonObject document = Json.parse(json);
		JsonArray players = Json.getArray(document, "players");
		if (players == null) {
			throw new IOException("/api/training without players");
		}
		TrainingWeek week = null;
		for (JsonElement element : players) {
			if (!element.isJsonObject()) {
				continue;
			}
			JsonObject player = element.getAsJsonObject();
			Integer id = Json.getInt(player, "id");
			JsonObject report = Json.getObject(player, "report");
			if (id == null || report == null) {
				continue;
			}
			if (week == null) {
				week = newWeek(report);
			}
			week.add(parseReport(id.intValue(), report));
		}
		if (week == null) {
			throw new IOException("/api/training without reports");
		}
		return week;
	}

	private static TrainingWeek newWeek(JsonObject report) throws IOException {
		Integer week = Json.getInt(report, "day.week");
		Integer day = Json.getInt(report, "day.day");
		Long timestamp = Json.getLong(report, "day.date.timestamp");
		if (week == null || day == null || timestamp == null) {
			throw new IOException("/api/training report without day.week, day.day or day.date.timestamp");
		}
		return new TrainingWeek(week.intValue(), day.intValue(), timestamp.longValue() * 1000L);
	}

	private static PlayerTrainingReport parseReport(int playerId, JsonObject report) {
		PlayerTrainingReport r = new PlayerTrainingReport();
		r.setPlayerId(playerId);
		r.setType(intOr(report, "type.code", Training.TYPE_UNKNOWN));
		r.setKind(intOr(report, "kind.code", 0));
		r.setFormation(intOr(report, "formation.code", Training.POSITION_NOT_SET));
		r.setValue(intOr(report, "playerValue.value", 0));
		PlayerSkills skills = r.getSkills();
		skills.setAge((byte) intOr(report, "age", 0));
		skills.setInjurydays(intOr(report, "injury.daysRemaining", 0));
		parseSkills(report, skills, false);
		skills.setTrainingIntensity(intOr(report, "intensity", -1));
		skills.setMinutesOfficial(intOr(report, "games.minutesOfficial", -1));
		skills.setMinutesFriendly(intOr(report, "games.minutesFriendly", -1));
		skills.setMinutesNational(intOr(report, "games.minutesNational", -1));
		skills.setTrainingInjuryDays(intOr(report, "injury.daysRemaining", -1));
		if (Json.getObject(report, "skillsChange") != null) {
			PlayerSkills before = new PlayerSkills();
			parseSkills(report, before, true);
			r.setSkillsBefore(before);
		}
		return r;
	}

	/** the report's skills, minus what that week's training changed when before */
	private static void parseSkills(JsonObject report, PlayerSkills to, boolean before) {
		to.setForm((byte) skill(report, "form", before));
		to.setDiscipline(skill(report, "tacticalDiscipline", before));
		to.setTeamwork(skill(report, "teamwork", before));
		to.setExperience(skill(report, "experience", before));
		to.setStamina((byte) skill(report, "stamina", before));
		to.setKeeper((byte) skill(report, "keeper", before));
		to.setPlaymaker((byte) skill(report, "playmaking", before));
		to.setPassing((byte) skill(report, "passing", before));
		to.setTechnique((byte) skill(report, "technique", before));
		to.setDefender((byte) skill(report, "defending", before));
		to.setScorer((byte) skill(report, "striker", before));
		to.setPace((byte) skill(report, "pace", before));
	}

	private static int skill(JsonObject report, String name, boolean before) {
		int value = intOr(report, "skills." + name, 0);
		return before ? value - intOr(report, "skillsChange." + name, 0) : value;
	}

	private static int intOr(JsonObject object, String path, int fallback) {
		Integer value = Json.getInt(object, path);
		return value == null ? fallback : value.intValue();
	}
}
