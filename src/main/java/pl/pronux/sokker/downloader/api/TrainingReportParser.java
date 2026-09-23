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
		skills.setForm((byte) intOr(report, "skills.form", 0));
		skills.setDiscipline(intOr(report, "skills.tacticalDiscipline", 0));
		skills.setTeamwork(intOr(report, "skills.teamwork", 0));
		skills.setExperience(intOr(report, "skills.experience", 0));
		skills.setStamina((byte) intOr(report, "skills.stamina", 0));
		skills.setKeeper((byte) intOr(report, "skills.keeper", 0));
		skills.setPlaymaker((byte) intOr(report, "skills.playmaking", 0));
		skills.setPassing((byte) intOr(report, "skills.passing", 0));
		skills.setTechnique((byte) intOr(report, "skills.technique", 0));
		skills.setDefender((byte) intOr(report, "skills.defending", 0));
		skills.setScorer((byte) intOr(report, "skills.striker", 0));
		skills.setPace((byte) intOr(report, "skills.pace", 0));
		skills.setTrainingIntensity(intOr(report, "intensity", -1));
		skills.setMinutesOfficial(intOr(report, "games.minutesOfficial", -1));
		skills.setMinutesFriendly(intOr(report, "games.minutesFriendly", -1));
		skills.setMinutesNational(intOr(report, "games.minutesNational", -1));
		skills.setTrainingInjuryDays(intOr(report, "injury.daysRemaining", -1));
		return r;
	}

	private static int intOr(JsonObject object, String path, int fallback) {
		Integer value = Json.getInt(object, path);
		return value == null ? fallback : value.intValue();
	}
}
