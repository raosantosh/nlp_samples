package com.santrao.nlp;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class SentenceDetector {

    private SentenceDetectorME sdetector;

    public SentenceDetector() {
        try {
            InputStream is = this.getClass().getResourceAsStream("/en-sent.bin");
            SentenceModel model = new SentenceModel(is);
            sdetector = new SentenceDetectorME(model);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> detectSentnce(String paragraph) {
        return Arrays.asList(sdetector.sentDetect(paragraph));
    }
}
