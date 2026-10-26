package pl.pronux.sokker.ui.beans;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

import pl.pronux.sokker.ui.resources.ColorResources;
import pl.pronux.sokker.ui.resources.Theme;

public class Colors {
	/** The light or the dark colour, by the running theme */
	private static Color pick(int red, int green, int blue, int darkRed, int darkGreen, int darkBlue) {
		return Theme.isDark() ? ColorResources.getColor(darkRed, darkGreen, darkBlue) : ColorResources.getColor(red, green, blue);
	}

	/** Background of lists, tables and text fields: SV's own where it paints the widgets, else the platform's */
	public static Color getListBackground() {
		return Theme.isSkinned() ? pick(255, 255, 255, 43, 43, 43) : ColorResources.getListBackground();
	}

	/** Background of windows and panels: SV's own where it paints the widgets, else the platform's */
	public static Color getPanelBackground() {
		return Theme.isSkinned() ? pick(240, 240, 240, 60, 60, 60) : ColorResources.getWidgetBackground();
	}

	/** Hover popup background: the panel grey where SV paints dark widgets, else the system tooltip colour */
	public static Color getPopupBackground() {
		return Theme.isSkinned() && Theme.isDark() ? getPanelBackground() : ColorResources.getSystemColor(SWT.COLOR_INFO_BACKGROUND);
	}

	/** Hover popup text: off-white where SV paints dark widgets, else the system tooltip colour */
	public static Color getPopupText() {
		return Theme.isSkinned() && Theme.isDark() ? getLightText() : ColorResources.getSystemColor(SWT.COLOR_INFO_FOREGROUND);
	}

	/** Off-white text on dark backgrounds */
	public static Color getLightText() {
		return ColorResources.getColor(205, 203, 198);
	}

	/** Grid lines on dark backgrounds */
	public static Color getGridLine() {
		return ColorResources.getColor(72, 72, 72);
	}

	/** Frames and separators on dark backgrounds, as grey as Windows' flat dark borders */
	public static Color getBorderLine() {
		return ColorResources.getColor(100, 100, 100);
	}

	public static Color getArenaField() {
		return ColorResources.getColor(93, 176, 4);
	}

	public static Color getArenaBenches() {
		return ColorResources.getColor(183, 240, 245);
	}

	public static Color getArenaSeats() {
		return ColorResources.getColor(253, 68, 59);
	}

	public static Color getTrainerGeneralSkill() {
		return pick(128, 64, 0, 222, 150, 90);
	}

	public static Color getArenaStanding() {
		return ColorResources.getColor(242, 242, 242);
	}

	public static Color getArenaTerraces() {
		return ColorResources.getColor(192, 192, 192);
	}

	/** BBCode editors' paper, the forum's own background in every theme */
	public static Color getForumPaper() {
		return ColorResources.getColor(243, 247, 237);
	}

	public static Color getTrainerSkill() {
		return ColorResources.getColor(188, 151, 126);
	}

	public static Color getMatchDraw() {
		return ColorResources.getColor(163, 163, 163);
	}

	public static Color getMatchLost() {
		return ColorResources.getColor(255, 79, 79);
	}

	public static Color getMatchWin() {
		return pick(0, 157, 0, 95, 200, 95);
	}

	public static Color getLeagueMatch() {
		return pick(243, 247, 237, 44, 50, 38);
	}

	public static Color getPlayoff() {
		return pick(226, 234, 208, 54, 62, 36);
	}

	public static Color getCup() {
		return pick(246, 246, 206, 66, 62, 28);
	}

	public static Color getFriendlyMatch() {
		return pick(252, 242, 242, 66, 40, 40);
	}

	public static Color getJuniorsLeague() {
		return pick(234, 242, 250, 34, 46, 62);
	}

	public static Color getFriendlyLeague() {
		return pick(242, 252, 250, 30, 58, 54);
	}

	public static Color getBlueDescription() {
		return pick(0, 70, 213, 110, 160, 255);
	}

	public static Color getPositionATT() {
		return pick(226, 255, 208, 40, 70, 30);
	}

	public static Color getPositionMID() {
		return pick(255, 255, 208, 70, 66, 28);
	}

	public static Color getPositionDEF() {
		return pick(255, 230, 214, 78, 52, 38);
	}

	public static Color getPositionGK() {
		return pick(221, 255, 255, 28, 64, 68);
	}

	public static Color getReportCost() {
		return pick(252, 242, 242, 66, 40, 40);
	}

	public static Color getReportIncome() {
		return pick(239, 250, 236, 36, 60, 36);
	}

	public static Color getReportInfo() {
		return pick(229, 232, 249, 40, 46, 76);
	}

	/** Name background of a player on the transfer list */
	public static Color getTransferList() {
		return pick(221, 255, 255, 28, 64, 68);
	}

	public static Color getStripe() {
		return pick(192, 192, 192, 50, 50, 50);
	}

	/** Date cell of an even season */
	public static Color getSeasonShade() {
		return pick(240, 240, 240, 52, 52, 52);
	}

	/** Preferences and wizard header */
	public static Color getDialogHeader() {
		return pick(248, 247, 243, 75, 75, 75);
	}

	/** TrainingTree column-title row */
	public static Color getTreeHeader() {
		return pick(128, 128, 128, 80, 80, 80);
	}

	public static Color getCoachHead() {
		return pick(236, 213, 202, 78, 54, 44);
	}

	public static Color getCoachJuniors() {
		return pick(237, 240, 225, 56, 62, 44);
	}

	public static Color getCoachAssistant() {
		return pick(252, 250, 245, 54, 54, 48);
	}

	public static Color getTrainingType() {
		return pick(48, 121, 182, 100, 170, 230);
	}

	public static Color getJuniorAccent() {
		return pick(142, 3, 121, 232, 120, 212);
	}

	public static Color getMutedText() {
		return pick(128, 128, 128, 150, 150, 150);
	}

	public static Color getDisabledText() {
		return pick(192, 192, 192, 110, 110, 110);
	}

	public static Color getGreenText() {
		return pick(0, 128, 0, 95, 200, 95);
	}

	public static Color getRedText() {
		return pick(255, 0, 0, 255, 105, 97);
	}

	public static Color getInfoText() {
		return pick(59, 91, 253, 125, 155, 255);
	}

	public static Color getBlueText() {
		return pick(0, 0, 255, 100, 150, 255);
	}

	public static Color getNavyText() {
		return pick(0, 0, 128, 125, 150, 255);
	}

	public static Color getYellowText() {
		return pick(255, 255, 0, 255, 214, 10);
	}

	/** TrainingConfigurationTable title row */
	public static Color getConfigHeader() {
		return pick(0, 128, 128, 80, 200, 200);
	}

	/** TrainingConfigurationTable first column */
	public static Color getConfigRowLabel() {
		return pick(8, 61, 132, 125, 170, 240);
	}
}
