package org.geworkbenchweb.visualizations;

import java.util.LinkedHashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;

@com.vaadin.ui.ClientWidget(org.geworkbenchweb.visualizations.client.ui.VVolcanoPlot.class)
public class VolcanoPlot extends AbstractComponent {
	private static Log log = LogFactory.getLog(VolcanoPlot.class);

	private static final long serialVersionUID = 2073659992756579843L;
	private String[] x = new String[0];
	private String[] y = new String[0];
	private String[] name = new String[0];
	private String seriesName = "";

	private String title = "", xtitle = "", ytitle = "";

	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);

		target.addAttribute("x", x);
		target.addAttribute("y", y);
		target.addAttribute("name", name);
		target.addAttribute("series_name", seriesName);
		target.addAttribute("title", title);
		target.addAttribute("xtitle", xtitle);
		target.addAttribute("ytitle", ytitle);
	}

	@Override
	public void setData(Object data) {
		if (!(data instanceof VolcanoPlotData)) {
			log.error("wrong data type");
			return;
		}
		super.setData(data);

		VolcanoPlotData d = (VolcanoPlotData) data;
		LinkedHashSet<Point> points = d.points;
		int n = points.size();
		String[] x = new String[n];
		String[] y = new String[n];
		String[] name = new String[n];
		for (int i = 0; i < n; i++) {
			Point a = (Point) (points.toArray())[i];
			x[i] = "" + a.x;
			y[i] = "" + a.y;
			name[i] = a.name;
		}
		this.x = x;
		this.y = y;
		this.name = name;
		this.seriesName = d.seriesName;

		this.title = d.title;
		this.xtitle = d.xtitle;
		this.ytitle = d.ytitle;
	}

	public static class Point {
		public final double x, y;
		public final String name;

		public Point(double xVal, double yVal, String mark) {
			x = xVal;
			y = yVal;
			name = mark;
		}
	}

	public static class VolcanoPlotData {
		final LinkedHashSet<Point> points;
		final String title, xtitle, ytitle;
		final String seriesName;

		public VolcanoPlotData(String title, String xtitle, String ytitle, LinkedHashSet<Point> points,
				String seriesName) {
			this.title = title;
			this.xtitle = xtitle;
			this.ytitle = ytitle;
			this.points = points;
			this.seriesName = seriesName;
		}
	}
}
