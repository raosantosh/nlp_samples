package com.santrao.nlp;

import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class SentenceTokenizer {

    private Tokenizer tokenizer;

    public SentenceTokenizer() {
        try {
            InputStream is = this.getClass().getResourceAsStream("/en-token.bin");

            TokenizerModel model = new TokenizerModel(is);

            tokenizer = new TokenizerME(model);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public List<String> getTokens(String sentence) {
        return Arrays.asList(tokenizer.tokenize(sentence));
    }
}
