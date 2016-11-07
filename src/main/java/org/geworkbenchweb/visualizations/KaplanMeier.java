package org.geworkbenchweb.visualizations;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;

@com.vaadin.ui.ClientWidget(org.geworkbenchweb.visualizations.client.ui.VKaplanMeier.class)
public class KaplanMeier extends AbstractComponent {
    private static Log log = LogFactory.getLog(KaplanMeier.class);

    private static final long serialVersionUID = 2073659992756579843L;
    private int subtypes = 0;
    private String[] y = new String[0];
    private Integer[] seriesName = new Integer[0];
    private Integer[] seriesCount = new Integer[0];

    private String title = "", xtitle = "", ytitle = "";

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);

        target.addAttribute("subtypes", subtypes);
        target.addAttribute("y", y);
        target.addAttribute("series_name", seriesName);
        target.addAttribute("series_count", seriesCount);
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
        seriesCount = new Integer[subtypes];
        List<String> allCoordinates = new ArrayList<String>();
        for (int i=0; i<d.points.length; i++) {
            List<int[]> list =  d.points[i];
            for (int[] coor : list) {
                allCoordinates.add( "" + coor[0] );
                allCoordinates.add( "" + coor[1] );
            }
            seriesCount[i] = list.size();
        }
        y = allCoordinates.toArray(new String[0]);

        this.seriesName = d.seriesName;

        this.title = d.title;
        this.xtitle = d.xtitle;
        this.ytitle = d.ytitle;
    }

    public static class KaplanMeierData {
        final int subtypeNumber;
        final List<int[]>[] points;
        final String title, xtitle, ytitle;
        final Integer[] seriesName;

        public KaplanMeierData(String title, String xtitle, String ytitle, List<int[]>[] points, Integer[] seriesName) {
            this.title = title;
            this.xtitle = xtitle;
            this.ytitle = ytitle;
            this.points = points;
            this.seriesName = seriesName;

            subtypeNumber = points.length;
        }
    }

    public static KaplanMeier createInstance(String tumorType, List<int[]>[] points, Set<Integer> subtypes) {
        KaplanMeier chart = new KaplanMeier();
        chart.setWidth("100%");
        chart.setHeight("100%");

        KaplanMeierData data = new KaplanMeierData(tumorType, "Month", "Percent Survival", points,
                subtypes.toArray(new Integer[0]));
        chart.setData(data);
        return chart;
    }
}
