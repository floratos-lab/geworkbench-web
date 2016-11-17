package org.geworkbenchweb.plugins.pbqdi;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DrugResult {

    List<List<String>> images;
    List<List<IndividualDrugInfo>> drugs;

    static DrugResult randomTestData() {
        Random random = new Random();

        List<List<String>> images = new ArrayList<List<String>>();
        List<List<IndividualDrugInfo>> drugs = new ArrayList<List<IndividualDrugInfo>>();
        int numberOfCards = random.nextInt(10);
        for (int i = 0; i < numberOfCards; i++) {
            List<String> img = new ArrayList<String>();
            for (int j = 0; j < random.nextInt(3); j++) {
                img.add(IndividualDrugInfo.randomString(10));
            }
            images.add(img);

            List<IndividualDrugInfo> drg = new ArrayList<IndividualDrugInfo>();
            for (int j = 0; j < random.nextInt(3); j++) {
                drg.add(IndividualDrugInfo.randomTestData());
            }
            drugs.add(drg);
        }
        DrugResult x = new DrugResult();
        x.images = images;
        x.drugs = drugs;
        return x;
    }
}
