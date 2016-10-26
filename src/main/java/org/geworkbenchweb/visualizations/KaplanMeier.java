package org.geworkbenchweb.visualizations;

import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;

@com.vaadin.ui.ClientWidget(org.geworkbenchweb.visualizations.client.ui.VKaplanMeier.class)
public class KaplanMeier extends AbstractComponent {
    private static Log log = LogFactory.getLog(KaplanMeier.class);

    private static final long serialVersionUID = 2073659992756579843L;
    private int subtypes = 0, months = 0;
    private String[] y = new String[0];
    private String seriesName[] = new String[0];

    private String title = "", xtitle = "", ytitle = "";

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);

        target.addAttribute("subtypes", subtypes);
        target.addAttribute("months", months);
        target.addAttribute("y", y);
        target.addAttribute("series_name", seriesName);
        target.addAttribute("title", title);
        target.addAttribute("xtitle", xtitle);
        target.addAttribute("ytitle", ytitle);
    }

    @Override
    public void setData(Object data) {
        if (!(data instanceof KaplanMeierData)) {
            log.error("wrong data type");
            return;
        }
        super.setData(data);

        KaplanMeierData d = (KaplanMeierData) data;
        subtypes = d.subtypeNumber;
        months = d.monthNumber;
        y = new String[subtypes*months];
        int index = 0;
        for (int i = 0; i < subtypes; i++) {
            for (int j = 0; j < months; j++) {
                y[index++] = "" + d.points[i][j];
            }
        }

        this.seriesName = d.seriesName;

        this.title = d.title;
        this.xtitle = d.xtitle;
        this.ytitle = d.ytitle;
    }

    public static class KaplanMeierData {
        final int subtypeNumber, monthNumber;
        final double[][] points;
        final String title, xtitle, ytitle;
        final String[] seriesName;

        public KaplanMeierData(String title, String xtitle, String ytitle, double[][] points, String seriesName[]) {
            this.title = title;
            this.xtitle = xtitle;
            this.ytitle = ytitle;
            this.points = points;
            this.seriesName = seriesName;

            subtypeNumber = points.length;
            monthNumber = points[0].length;
        }
    }

    public static KaplanMeier createInstance() { // test data
        KaplanMeier chart = new KaplanMeier();
        chart.setWidth("100%");
        chart.setHeight("100%");

        Random random = new Random();

        int subtypeNumber = 4;
        int monthNumber = 5;
        double[][] points = new double[subtypeNumber][monthNumber];
        for (int i = 0; i < subtypeNumber; i++) {
            for (int j = 0; j < monthNumber; j++) {
                points[i][j] = random.nextDouble();
            }
        }

        KaplanMeierData data = new KaplanMeierData("TODO - tumor type here", "Month", "Percent Survival", points,
                new String[] { "subtype1", "subtype2", "subtype3", "subtype4" });
        chart.setData(data);
        return chart;
    }
}
