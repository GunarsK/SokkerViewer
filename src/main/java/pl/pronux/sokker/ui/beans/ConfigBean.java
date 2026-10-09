package pl.pronux.sokker.ui.beans;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

import pl.pronux.sokker.handlers.SettingsHandler;
import pl.pronux.sokker.ui.handlers.DisplayHandler;
import pl.pronux.sokker.ui.resources.ColorResources;
import pl.pronux.sokker.ui.resources.Fonts;

public class ConfigBean {

	private static final String DECREASE_TABLE = "color.decreaseTable";

	private static final String DECREASE_DESCRIPTION = "color.decreaseDescription";

	private static final String ERROR = "color.error";

	private static final String INCREASE_TABLE = "color.increaseTable";

	private static final String INCREASE_DESCRIPTION = "color.increaseDescription";

	private static final String INJURY_BG = "color.injuryBg";

	private static final String INJURY_FG = "color.injuryFg";

	private static final String NEW_TABLE_ITEM = "color.newTableItem";

	private static final String NEW_TREE_ITEM = "color.newTreeItem";

	private static final String TRAINED_JUNIOR = "color.trainedJunior";

	private static final String UNTRAINED_FG = "color.untrainedFg";

	private static final String UNTRAINED_BG = "color.untrainedBg";

	private static final String FONT_TABLE = "font.table";

	private static final String FONT_DESCRIPTION = "font.description";

	private static final String FONT_MAIN = "font.main";

	/** Look page colour keys, in its order */
	public static final String[] COLORS = {
		DECREASE_TABLE, DECREASE_DESCRIPTION, ERROR, INCREASE_TABLE, INCREASE_DESCRIPTION, INJURY_BG, INJURY_FG,
		NEW_TABLE_ITEM, NEW_TREE_ITEM, TRAINED_JUNIOR, UNTRAINED_FG, UNTRAINED_BG
	};

	/** Look page font keys, in its order */
	public static final String[] FONTS = {FONT_TABLE, FONT_DESCRIPTION, FONT_MAIN};

	private static final Map<String, Color> colors = new HashMap<String, Color>();

	private static final Map<String, Font> fonts = new HashMap<String, Font>();

	/** Reads the look from user.properties, default.properties for missing keys */
	public static void load() throws IOException {
		Properties properties = new Properties(SettingsHandler.getDefaultProperties());
		File file = userFile();
		if (file.exists()) {
			InputStream in = new FileInputStream(file);
			try {
				properties.load(in);
			} finally {
				in.close();
			}
		}
		for (String key : COLORS) {
			String[] rgb = properties.getProperty(key).split(",");
			colors.put(key, ColorResources.getColor(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2])));
		}
		for (String key : FONTS) {
			fonts.put(key, Fonts.getFont(DisplayHandler.getDisplay(), new FontData[] {new FontData(properties.getProperty(key))}));
		}
	}

	/** Writes the look to user.properties and loads it */
	public static void save(Properties properties) throws IOException {
		OutputStream out = new FileOutputStream(userFile());
		try {
			properties.store(out, "");
		} finally {
			out.close();
		}
		load();
	}

	private static File userFile() {
		return new File(SettingsHandler.getSokkerViewerSettings().getBaseDirectory() + File.separator + "settings" + File.separator + "user.properties");
	}

	public static Color getColor(String key) {
		return colors.get(key);
	}

	public static Font getFont(String key) {
		return fonts.get(key);
	}

	public static Color getColorDecrease() {
		return colors.get(DECREASE_TABLE);
	}

	public static Color getColorDecreaseDescription() {
		return colors.get(DECREASE_DESCRIPTION);
	}

	public static Color getColorError() {
		return colors.get(ERROR);
	}

	public static Color getColorIncrease() {
		return colors.get(INCREASE_TABLE);
	}

	public static Color getColorIncreaseDescription() {
		return colors.get(INCREASE_DESCRIPTION);
	}

	public static Color getColorInjuryBg() {
		return colors.get(INJURY_BG);
	}

	public static Color getColorInjuryFg() {
		return colors.get(INJURY_FG);
	}

	public static Color getColorNewTableObject() {
		return colors.get(NEW_TABLE_ITEM);
	}

	public static Color getColorNewTreeObject() {
		return colors.get(NEW_TREE_ITEM);
	}

	public static Color getColorTrainedJunior() {
		return colors.get(TRAINED_JUNIOR);
	}

	public static Color getColorUntrainedFg() {
		return colors.get(UNTRAINED_FG);
	}

	public static Color getColorUntrainedBg() {
		return colors.get(UNTRAINED_BG);
	}

	/** Name background of a player on the transfer list */
	public static Color getColorTransferList() {
		return ColorResources.getColor(221, 255, 255);
	}

	public static Font getFontTable() {
		return fonts.get(FONT_TABLE);
	}

	public static Font getFontDescription() {
		return fonts.get(FONT_DESCRIPTION);
	}

	public static Font getFontMain() {
		return fonts.get(FONT_MAIN);
	}
}
