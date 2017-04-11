package com.santrao.nlp;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NameFinder {

    private NameFinderME nameFinder;
    private SentenceTokenizer sentenceTokenizer;

    public NameFinder() {
        try {
            InputStream is = this.getClass().getResourceAsStream("/en-ner-person.bin");

            TokenNameFinderModel model = new TokenNameFinderModel(is);
            is.close();

            nameFinder = new NameFinderME(model);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        sentenceTokenizer = new SentenceTokenizer();

    }

    public List<String> getNames(String sentence) {

        List<String> tokens = sentenceTokenizer.getTokens(sentence);

        List<Span> spans = Arrays.asList(nameFinder.find((String[]) tokens.toArray()));
        
        List<String> names = new ArrayList<String>();

        for (Span span : spans) {
            names.add(span.toString());
        }

        return names;
    }
}
