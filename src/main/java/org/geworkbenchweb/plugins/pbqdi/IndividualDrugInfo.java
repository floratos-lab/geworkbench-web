package org.geworkbenchweb.plugins.pbqdi;

import java.util.Random;

public class IndividualDrugInfo {
    String name;
    String description;
    String accession;

    static IndividualDrugInfo testData() {
        IndividualDrugInfo x = new IndividualDrugInfo();
        x.name = "Teniposide";
        x.accession = "12345";
        x.description = "Type II topoisomerase inhibitor that causes dose-dependent single- and double-stranded breaks in DNA and DNA-protein cross-links.";
        return x;
    }

    private static Random random = new Random();

    static public String randomString(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append('A' + random.nextInt(54));
        }
        return sb.toString();
    }

    static IndividualDrugInfo randomTestData() {
        IndividualDrugInfo x = new IndividualDrugInfo();
        x.name = randomString(10);
        x.accession = "" + random.nextInt();
        x.description = randomString(100);
        return x;
    }
}
