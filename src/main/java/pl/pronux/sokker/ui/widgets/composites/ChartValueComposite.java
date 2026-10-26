package pl.pronux.sokker.ui.widgets.composites;

import java.awt.Frame;
import java.awt.GridLayout;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import pl.pronux.sokker.model.Date;

public class ChartValueComposite extends Composite {
	private Frame chartFrame;

	public ChartValueComposite(Composite parent, int style) {
		super(parent, style);

		this.setLayout(new FormLayout());

		FormData canvasFormFill = new FormData();
		canvasFormFill.top = new FormAttachment(0, 0);
		canvasFormFill.left = new FormAttachment(0, 0);
		canvasFormFill.right = new FormAttachment(100, 0);
		canvasFormFill.bottom = new FormAttachment(100, 0);

		Composite comp = new Composite(this, SWT.EMBEDDED);
		comp.setLayoutData(canvasFormFill);

		chartFrame = SWT_AWT.new_Frame(comp);
		chartFrame.setLayout(new GridLayout());

	}

	public void fillGraph(List<Number> values1, List<Number> values2, List<Date> dates, final int trainingDay, final boolean zero, final double maxValue, boolean shapes, String title, String legend1, String legend2) {
		if (values1.size() <= 0 || values2.size() <= 0) {
			return;
		}

		final XYSeries series1 = new XYSeries(legend1);
		final XYSeries series2 = new XYSeries(legend2);

		for (int i = values1.size() - 1; i >= 0; i--) {
			series1.add(dates.get(i).getSokkerDate().getWeek(), (Number) values1.get(i));
			series2.add(dates.get(i).getSokkerDate().getWeek(), (Number) values2.get(i));
		}
		final XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(series1);
		dataset.addSeries(series2);

		setSeries(dataset, zero, maxValue, shapes, title, true);
	}

	public void fillGraph(List<Number> values, List<Date> dates, final int trainingDay, final boolean zero, final double maxValue, boolean shapes, String title) {
		if (values.size() <= 0) {
			return;
		}

		final XYSeries series = new XYSeries("");

		for (int i = values.size() - 1; i >= 0; i--) {
			series.add(dates.get(i).getSokkerDate().getWeek(), (Number) values.get(i));
		}

		setSeries(new XYSeriesCollection(series), zero, maxValue, shapes, title, false);
	}

	private void setSeries(final XYDataset dataset, final boolean zero, final double maxValue, boolean shapes, String title, boolean legend) {
		chartFrame.removeAll();

		JFreeChart chart = ChartFactory.createXYLineChart(title, "", "", dataset, PlotOrientation.VERTICAL, legend, true, false);

		final XYPlot plot = chart.getXYPlot();
		final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		for(int i = 0 ; i < dataset.getSeriesCount(); i++) {
			renderer.setSeriesLinesVisible(i, true);
			renderer.setSeriesShapesVisible(i, shapes);
		}
		plot.setRenderer(renderer);
		
		// change the auto tick unit selection to integer units only...
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		rangeAxis.setAutoRangeIncludesZero(zero);

		if (maxValue != -1) {
			rangeAxis.setRange(-1, maxValue);
			rangeAxis.setAutoTickUnitSelection(false);
			java.awt.Font font = rangeAxis.getLabelFont();
			rangeAxis.setTickLabelFont(font.deriveFont(font.getStyle(), font.getSize() - 4));
		}
		
		ValueAxis domainAxis = plot.getDomainAxis();
		domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		domainAxis.setAutoRangeMinimumSize(1.0);
		
		plot.setDomainAxis(domainAxis);
		
		drawChart(chart);

	}

	private void drawChart(JFreeChart chart) {
		ChartStyle.apply(chart);
		chartFrame.add(new ChartPanel(chart));
		chartFrame.doLayout();
	}
}
