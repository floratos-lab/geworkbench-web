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

    static public String randomWord() {
        int length = 5 + random.nextInt(5);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append((char) ('A' + random.nextInt(26)));
        }
        return sb.toString();
    }

    static public String randomParagraph() {
        int length = 10 + random.nextInt(10);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(randomWord()).append(" ");
        }
        return sb.toString();
    }

    static IndividualDrugInfo randomTestData() {
        IndividualDrugInfo x = new IndividualDrugInfo();
        x.name = randomWord();
        x.accession = "" + random.nextInt();
        x.description = randomParagraph();
        return x;
    }
}
