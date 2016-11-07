package org.geworkbenchweb.plugins.pbqdi;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geworkbenchweb.GeworkbenchRoot;

public class SurvivalData {

    static String SURVIVAL_DATA = GeworkbenchRoot.getAppProperty("survival.data");

    private Map<String, Map<Integer, List<int[]>>> data = new HashMap<String, Map<Integer, List<int[]>>>();
    private Map<String, Map<Integer, Integer>> subtypeCount = new HashMap<String, Map<Integer, Integer>>();

    public List<int[]> getOneSubtype(String tumorType, int subtype) {
        return data.get(tumorType).get(subtype);
    }

    public int getOneSubtypeCount(String tumorType, int subtype) {
        return subtypeCount.get(tumorType).get(subtype);
    }

    public SurvivalData() throws IOException {
        this(SURVIVAL_DATA);
    }

    public SurvivalData(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line = br.readLine();
        String tumorType = null;
        Map<Integer, List<int[]>> subtypeMap = null;
        Map<Integer, Integer> countMap = null;
        while (line != null && line.trim().length() > 0) {
            if (line.startsWith("TUMOR_TYPE")) {
                if (tumorType != null) {
                    data.put(tumorType, subtypeMap);
                    subtypeCount.put(tumorType, countMap);
                }
                tumorType = line.split("\\s")[1];
                subtypeMap = new HashMap<Integer, List<int[]>>();
                countMap = new HashMap<Integer, Integer>();
            } else {
                int subtype = Integer.parseInt(line.substring(0, line.indexOf(" ")));
                String survival = line.substring(line.indexOf("[") + 2, line.indexOf("]") - 1);
                List<int[]> list = new ArrayList<int[]>();
                String[] xy = survival.split("\\),\\s\\(");
                for (String pair : xy) {
                    String[] s = pair.split(",\\s");
                    int[] x = new int[2];
                    x[0] = Integer.parseInt(s[0]);
                    x[1] = Integer.parseInt(s[1]);
                    list.add(x);
                }
                int total = Integer.parseInt(line.substring(line.indexOf("]") + 1).trim());
                subtypeMap.put(subtype, list);
                countMap.put(subtype, total);
                if (total != list.size()) {
                    System.out.println(total + " not equal to" + list.size());
                    br.close();
                    throw new IOException("survial dat incorrect");
                }
            }
            line = br.readLine();
        }
        if (tumorType != null) {
            data.put(tumorType, subtypeMap);
            subtypeCount.put(tumorType, countMap);
        }

        br.close();
    }

    public static void main(String[] args) throws IOException {
        SurvivalData test = new SurvivalData("F:/cptac_project/survival data/survival.output.txt");
        int count = test.getOneSubtypeCount("ucec", 10);
        System.out.println("count=" + count);
        List<int[]> x = test.getOneSubtype("ucec", 10);
        for (int[] a : x) {
            System.out.println(a[0] + "," + a[1]);
        }
    }
}
