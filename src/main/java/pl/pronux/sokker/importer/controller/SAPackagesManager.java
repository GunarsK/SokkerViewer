package pl.pronux.sokker.importer.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;

import pl.pronux.sokker.importer.model.IXMLpack;
import pl.pronux.sokker.importer.model.SApack;
import pl.pronux.sokker.interfaces.ProgressMonitor;
import pl.pronux.sokker.model.Money;
import pl.pronux.sokker.model.Player;
import pl.pronux.sokker.model.PlayerSkills;
import pl.pronux.sokker.model.SokkerDate;
import pl.pronux.sokker.model.Training;
import pl.pronux.sokker.resources.Messages;
import pl.pronux.sokker.utils.Log;

/** sokker asistente's player history, one pack per week */
public class SAPackagesManager extends PackagesManager {

	/** first week whose blocks carry the advanced training flag */
	private static final int FIRST_ADVANCED_WEEK = 993;

	/** last season week whose age both apps agree on */
	private static final int LAST_SAME_AGE_WEEK = 10;

	/** sokker asistente keeps a quarter of the value */
	private static final int VALUE_SCALE = 4;

	/** sokker asistente's training types, in sokker viewer's order from 1 */
	private static final List<String> TYPES = Arrays.asList("Condicion", "Porteria", "Creacion", "Pases", "Tecnica", "Defensa", "Anotacion", "Rapidez");

	/** sokker asistente's positions, in formation order */
	private static final List<String> POSITIONS = Arrays.asList("GK", "DEF", "MID", "ATT");

	private File directory;

	private int teamID;

	private List<IXMLpack> packages;

	/** player records that did not parse */
	private int unreadable;

	/** one player's record: header and weekly blocks, newest first */
	private static class Record {

		private int id;

		private String name;

		private int country;

		/** a former player's record, copied on after he left */
		private boolean former;

		/** training week of the header's date */
		private int week;

		private List<Block> blocks = new ArrayList<Block>();

		/** a real block, not copied on after the player left */
		private boolean isUsable(Block block) {
			return block.isReal() && !(former && block.week > week);
		}
	}

	/** one week of a record */
	private static class Block {

		private int week;

		private Integer age;

		private Integer value;

		/** stamina, pace, technique, passing, keeper, defender, playmaker, striker */
		private Integer[] skills = new Integer[8];

		private int injury;

		private Integer form;

		/** formation trained in, -1 when not set */
		private int position;

		private float minutes;

		private int experience;

		private int discipline;

		private int teamwork;

		private boolean advanced;

		/** false for a gap filler with an empty field */
		private boolean isReal() {
			if (age == null || value == null || form == null) {
				return false;
			}
			for (Integer skill : skills) {
				if (skill == null) {
					return false;
				}
			}
			return true;
		}
	}

	public SAPackagesManager(String directory, int teamID) {
		this.directory = new File(directory);
		this.teamID = teamID;
	}

	public void run(ProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		packages = new ArrayList<IXMLpack>();
		try {
			File[] users = directory.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.startsWith("_") && name.endsWith(".properties");
				}
			});
			if (users != null && users.length > 1) {
				monitor.setTaskName(Messages.getString("importer.asistente.users"));
				return;
			}
			Map<Integer, int[]> types = users == null || users.length == 0 ? new HashMap<Integer, int[]>() : readTypes(users[0]);
			Properties current = load(new File(directory, teamID + ".properties"));
			Properties former = load(new File(directory, teamID + "_historico.properties"));
			monitor.beginTask(Messages.getString("importer.asistente.reading"), current.size() + former.size());
			Map<Integer, SApack> weeks = new TreeMap<Integer, SApack>();
			Set<String> taken = new HashSet<String>();
			addRecords(current, false, weeks, taken, monitor);
			addRecords(former, true, weeks, taken, monitor);
			for (Map.Entry<Integer, SApack> week : weeks.entrySet()) {
				week.getValue().setTypes(types.get(Integer.valueOf(week.getKey().intValue() - 1)));
				packages.add(week.getValue());
			}
			String found = String.format(Messages.getString("PackagesManager.found"), packages.size(), Messages.getString("button.import"));
			monitor.setTaskName(unreadable == 0 ? found : found + " " + String.format(Messages.getString("PackagesManager.unreadable"), unreadable));
		} catch (IOException e) {
			Log.error("Sokker Asistente importer", e);
		} finally {
			monitor.done();
		}
	}

	private static Properties load(File file) throws IOException {
		Properties properties = new Properties();
		if (file.exists()) {
			InputStream in = new FileInputStream(file);
			try {
				properties.load(in);
			} finally {
				in.close();
			}
		}
		return properties;
	}

	/** types per week from the user file's entrenamientoN lines only */
	private static Map<Integer, int[]> readTypes(File file) throws IOException {
		StringBuilder lines = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "ISO-8859-1"));
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("entrenamiento")) {
					lines.append(line).append('\n');
				}
			}
		} finally {
			reader.close();
		}
		Properties properties = new Properties();
		properties.load(new StringReader(lines.toString()));
		Map<Integer, int[]> types = new HashMap<Integer, int[]>();
		for (String key : properties.stringPropertyNames()) {
			if (key.matches("entrenamiento[0-9]+")) {
				int[] week = types(properties.getProperty(key));
				if (week != null) {
					types.put(Integer.valueOf(key.substring("entrenamiento".length())), week);
				}
			}
		}
		return types;
	}

	/** types per formation; null without them or a head coach */
	private static int[] types(String value) {
		String[] fields = value.split(",", -1);
		String[] names = fields[0].split("-", -1);
		if (names.length < POSITIONS.size() || fields.length < 3 || fields[2].isEmpty()) {
			return null;
		}
		int[] types = new int[POSITIONS.size()];
		for (int i = 0; i < types.length; i++) {
			int index = TYPES.indexOf(names[i]);
			types[i] = index < 0 ? Training.TYPE_NOT_SET : index + 1;
		}
		return types;
	}

	/** readable records' rows by week, one per player and week */
	private void addRecords(Properties records, boolean former, Map<Integer, SApack> weeks, Set<String> taken, ProgressMonitor monitor) {
		for (String key : new TreeSet<String>(records.stringPropertyNames())) {
			monitor.worked(1);
			if (!key.matches("[0-9]+")) {
				continue;
			}
			Record record;
			try {
				record = parse(key, records.getProperty(key));
			} catch (RuntimeException e) {
				unreadable++;
				Log.warning("Sokker Asistente importer: player " + key + " not read", e);
				continue;
			}
			record.former = former;
			Map<Integer, Block> byWeek = new HashMap<Integer, Block>();
			for (Block block : record.blocks) {
				if (!byWeek.containsKey(Integer.valueOf(block.week))) {
					byWeek.put(Integer.valueOf(block.week), block);
				}
			}
			for (Block block : record.blocks) {
				if (record.isUsable(block) && taken.add(record.id + "@" + block.week)) {
					SApack pack = weeks.get(Integer.valueOf(block.week));
					if (pack == null) {
						pack = new SApack(block.week);
						weeks.put(Integer.valueOf(block.week), pack);
					}
					pack.getPlayers().add(player(record, block, byWeek.get(Integer.valueOf(block.week - 1))));
				}
			}
		}
	}

	/** a record; optional parts found by their markers */
	private static Record parse(String key, String value) {
		String[] fields = value.split(",", -1);
		Record record = new Record();
		record.id = Integer.parseInt(key);
		int i = 0;
		record.name = fields[i++];
		i += 2; // team, position
		record.country = Integer.parseInt(fields[i++]);
		record.week = trainingWeek(fields[i++]);
		i += 6; // updated, cards, national team, injury, for sale, notes
		if (fields[i].startsWith("-")) {
			i += 4; // wage, height, weight, bmi
			if (fields[i].startsWith("-")) {
				i += 2; // talent, highlight
			}
			if (fields[i].startsWith("#")) {
				i++; // colour
				if (fields[i].startsWith("-")) {
					i++; // second login
				}
				if (fields[i].startsWith("-")) {
					i++; // bot
				}
			}
		}
		while (i < fields.length && !fields[i].equals("*")) {
			Block block = new Block();
			block.week = Integer.parseInt(fields[i++]);
			block.age = optional(fields[i++]);
			block.value = optional(fields[i++]);
			for (int k = 0; k < block.skills.length; k++) {
				block.skills[k] = optional(fields[i++]);
			}
			if (fields[i].startsWith("-")) {
				block.injury = Math.abs(Integer.parseInt(fields[i++]));
			}
			block.form = optional(fields[i++]);
			block.position = POSITIONS.indexOf(fields[i++]);
			block.minutes = Float.parseFloat(fields[i++]);
			if (i < fields.length && fields[i].startsWith("-")) {
				block.experience = Math.abs(Integer.parseInt(fields[i++]));
				block.discipline = number(fields[i++]);
				block.teamwork = number(fields[i++]);
			}
			if (block.week >= FIRST_ADVANCED_WEEK) {
				block.advanced = Boolean.parseBoolean(fields[i++]);
			}
			record.blocks.add(block);
		}
		return record;
	}

	private static int number(String field) {
		return field.isEmpty() ? 0 : Integer.parseInt(field);
	}

	/** a number, null for an empty field */
	private static Integer optional(String field) {
		return field.isEmpty() ? null : Integer.valueOf(field);
	}

	/** training week of a sokker asistente server date */
	private static int trainingWeek(String date) {
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		format.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));
		try {
			return new SokkerDate(format.parse(date).getTime()).getTrainingWeek();
		} catch (ParseException e) {
			throw new IllegalArgumentException("date " + date, e);
		}
	}

	/** the block's player row, trained per the week before's block */
	private Player player(Record record, Block block, Block before) {
		PlayerSkills skills = new PlayerSkills();
		skills.setAge((byte) age(record, block));
		skills.setValue(new Money(block.value.intValue() * VALUE_SCALE));
		skills.setStamina(block.skills[0].byteValue());
		skills.setPace(block.skills[1].byteValue());
		skills.setTechnique(block.skills[2].byteValue());
		skills.setPassing(block.skills[3].byteValue());
		skills.setKeeper(block.skills[4].byteValue());
		skills.setDefender(block.skills[5].byteValue());
		skills.setPlaymaker(block.skills[6].byteValue());
		skills.setScorer(block.skills[7].byteValue());
		skills.setForm(block.form.byteValue());
		skills.setInjurydays(block.injury);
		skills.setExperience(block.experience);
		skills.setDiscipline(block.discipline);
		skills.setTeamwork(block.teamwork);
		if (before != null && before.isReal() && before.position >= 0) {
			int intensity = Math.min(100, Math.round(before.minutes));
			skills.setTrainingIntensity(intensity);
			skills.setPassTraining(intensity > 0);
			skills.setTrainingPosition(before.position);
			skills.setTrainingSlot(before.advanced ? Training.SLOT_ADVANCED : intensity > 0 ? Training.SLOT_FORMATION : Training.SLOT_MISSING);
			skills.setTrainingInjuryDays(before.injury);
		}
		Player player = new Player();
		player.setId(record.id);
		int space = record.name.indexOf(' ');
		player.setName(space < 0 ? record.name : record.name.substring(0, space));
		player.setSurname(space < 0 ? "" : record.name.substring(space + 1));
		player.setCountryfrom(record.country);
		player.setTeamId(teamID);
		player.setSkills(new PlayerSkills[] { skills });
		return player;
	}

	/** age at the block's week from the nearest early-season block */
	private static int age(Record record, Block block) {
		Block nearest = null;
		for (Block other : record.blocks) {
			if (record.isUsable(other) && SokkerDate.seasonWeekOf(other.week) <= LAST_SAME_AGE_WEEK
					&& (nearest == null || Math.abs(other.week - block.week) < Math.abs(nearest.week - block.week))) {
				nearest = other;
			}
		}
		if (nearest == null) {
			return block.age.intValue();
		}
		return nearest.age.intValue() + SokkerDate.seasonOf(block.week) - SokkerDate.seasonOf(nearest.week);
	}

	public List<IXMLpack> getPackages() {
		return packages;
	}

	public void setPackages(List<IXMLpack> packages) {
		this.packages = packages;
	}

	public void onFinish() {
	}
}
