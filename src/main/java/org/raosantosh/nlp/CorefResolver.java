package com.yahoo.presto.nlp;

import java.util.Properties;

import edu.stanford.nlp.hcoref.CorefCoreAnnotations;
import edu.stanford.nlp.hcoref.data.CorefChain;
import edu.stanford.nlp.hcoref.data.Mention;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class CorefResolver {
	private StanfordCoreNLP pipeline;

	public CorefResolver() {
		Properties tokenizationProps = new Properties();
		tokenizationProps.put("annotators", "tokenize, ssplit, pos, lemma,parse, ner, mention, coref");
		pipeline = new StanfordCoreNLP(tokenizationProps);
	}

	public void printCoref(String text) {
		Annotation document = new Annotation(text);
		pipeline.annotate(document);
		System.out.println("---");
		System.out.println("coref chains");
		for (CorefChain cc : document.get(CorefCoreAnnotations.CorefChainAnnotation.class).values()) {
			System.out.println("\t" + cc);
		}
		for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
			System.out.println("---");
			System.out.println("mentions");
			for (Mention m : sentence.get(CorefCoreAnnotations.CorefMentionsAnnotation.class)) {
				System.out.println("\t" + m);
			}
		}
	}
}