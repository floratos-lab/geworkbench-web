package org.geworkbenchweb.plugins.pbqdi;

import java.util.Random;

public class ResultData {
    String[] dataQualityImages;

    DrugResult oncology;
    DrugResult nononcology;
    DrugResult investigational;

    public ResultData(String[] dataQualityImages, DrugResult oncology, DrugResult nononcology,
            DrugResult investigational) {
        this.dataQualityImages = dataQualityImages;
        this.oncology = oncology;
        this.nononcology = nononcology;
        this.investigational = investigational;
    }

    static ResultData randomTestData() {
        Random random = new Random();

        String[] dataQualityImages = new String[1 + random.nextInt(5)];
        for (int i = 0; i < dataQualityImages.length; i++) {
            dataQualityImages[i] = IndividualDrugInfo.randomWord();
        }

        ResultData x = new ResultData(dataQualityImages, DrugResult.randomTestData(), DrugResult.randomTestData(),
                DrugResult.randomTestData());

        return x;
    }
}
