package pl.pronux.sokker.ui.widgets.composites;

import java.awt.Color;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;

import pl.pronux.sokker.ui.beans.Colors;
import pl.pronux.sokker.ui.resources.Theme;

/** Chart colours for the dark theme; the light theme keeps JFreeChart's own */
public final class ChartStyle {

	private ChartStyle() {
	}

	public static void apply(JFreeChart chart) {
		if (!Theme.isDark()) {
			return;
		}
		Color text = awt(Colors.getLightText());
		chart.setBackgroundPaint(awt(Colors.getPanelBackground()));
		XYPlot plot = chart.getXYPlot();
		plot.setBackgroundPaint(awt(Colors.getListBackground()));
		plot.setDomainGridlinePaint(awt(Colors.getGridLine()));
		plot.setRangeGridlinePaint(awt(Colors.getGridLine()));
		paintAxis(plot.getDomainAxis(), text);
		paintAxis(plot.getRangeAxis(), text);
		if (chart.getTitle() != null) {
			chart.getTitle().setPaint(text);
		}
		if (chart.getLegend() != null) {
			chart.getLegend().setBackgroundPaint(awt(Colors.getPanelBackground()));
			chart.getLegend().setItemPaint(text);
		}
	}

	/** Colour of the selected row's markers */
	public static Color marker() {
		return awt(Colors.getBlueText());
	}

	private static void paintAxis(ValueAxis axis, Color text) {
		axis.setAxisLinePaint(text);
		axis.setLabelPaint(text);
		axis.setTickLabelPaint(text);
		axis.setTickMarkPaint(text);
	}

	private static Color awt(org.eclipse.swt.graphics.Color color) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue());
	}
}
