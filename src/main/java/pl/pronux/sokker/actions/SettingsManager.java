package pl.pronux.sokker.actions;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import pl.pronux.sokker.data.properties.PropertiesDatabase;
import pl.pronux.sokker.data.properties.PropertiesSession;
import pl.pronux.sokker.data.properties.dao.SokkerViewerSettingsDao;
import pl.pronux.sokker.exceptions.SVException;
import pl.pronux.sokker.model.SokkerViewerSettings;

public final class SettingsManager {

	/** Key prefix of a table's hidden columns: column count, colon, indexes */
	private static final String HIDDEN_COLUMNS = "table.hidden.";

	private static SettingsManager instance = new SettingsManager();

	private SettingsManager() {
	}

	public static SettingsManager getInstance() {
		return instance;
	}

	public void updateSettings(SokkerViewerSettings settings) throws IOException, SVException {
		new SokkerViewerSettingsDao(PropertiesDatabase.getSession()).updateSokkerViewerSettings(settings);
	}
	
	public SokkerViewerSettings getSettings() throws IOException {
		return new SokkerViewerSettingsDao(PropertiesDatabase.getSession()).getSokkerViewerSettings();
	}

	/** Hidden column indexes of a table; none when its column count changed */
	public Set<Integer> getHiddenColumns(String table, int columnCount) throws IOException {
		Set<Integer> hidden = new TreeSet<Integer>();
		String value = PropertiesDatabase.getSession().getProperty(HIDDEN_COLUMNS + table);
		if (value == null || !value.startsWith(columnCount + ":")) {
			return hidden;
		}
		try {
			for (String index : value.substring(value.indexOf(':') + 1).split(",")) {
				if (!index.isEmpty()) {
					hidden.add(Integer.valueOf(index));
				}
			}
		} catch (NumberFormatException e) {
			hidden.clear();
		}
		return hidden;
	}

	public void setHiddenColumns(String table, int columnCount, Set<Integer> hidden) throws IOException {
		StringBuilder value = new StringBuilder().append(columnCount).append(':');
		String separator = "";
		for (Integer index : hidden) {
			value.append(separator).append(index);
			separator = ",";
		}
		PropertiesSession session = PropertiesDatabase.getSession();
		session.setProperty(HIDDEN_COLUMNS + table, value.toString());
		session.save();
	}

}
