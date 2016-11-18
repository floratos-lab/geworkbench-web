package org.geworkbenchweb.plugins.pbqdi;

import java.util.Random;

public class ResultData {
    String[] dataQualityImages;

    DrugResult fdaApproved;
    DrugResult investigational;

    static ResultData randomTestData() {
        Random random = new Random();

        String[] dataQualityImages = new String[1 + random.nextInt(5)];
        for (int i = 0; i < dataQualityImages.length; i++) {
            dataQualityImages[i] = IndividualDrugInfo.randomWord();
        }

        ResultData x = new ResultData();
        x.dataQualityImages = dataQualityImages;
        x.fdaApproved = DrugResult.randomTestData();
        x.investigational = DrugResult.randomTestData();

        return x;
    }
}
