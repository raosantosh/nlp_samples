package com.yahoo.presto.nlp;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import com.yahoo.presto.ilp.core.classifier.ILPClassifier;
import com.yahoo.presto.ilp.core.classifier.IntentMatch;
import com.yahoo.presto.ilp.core.classifier.ModelOutput;
import com.yahoo.presto.standalone.v2.ModelTrainer;

import edu.stanford.nlp.hcoref.CorefCoreAnnotations;
import edu.stanford.nlp.hcoref.data.CorefChain;
import edu.stanford.nlp.hcoref.data.Mention;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class TextParser {
	private StanfordCoreNLP pipeline;

	public TextParser() {
		Properties tokenizationProps = new Properties();
		tokenizationProps.put("annotators", "tokenize, ssplit, pos, lemma, parse, ner");
		// tokenizationProps.put("annotators", "tokenize, ssplit, pos, lemma,
		// parse, ner, mention, coref");
		tokenizationProps.put("ner.model",
				"edu/stanford/nlp/models/ner/english.muc.7class.caseless.distsim.crf.ser.gz");
		tokenizationProps.put("coref.algorithm", "neural");
		pipeline = new StanfordCoreNLP(tokenizationProps);
	}

	public void annotate(String text) {
		Annotation annotation = new Annotation(text);
		List<String> lemmas = new LinkedList<>();
		pipeline.annotate(annotation);
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		sentences.forEach(sentence -> sentence.get(CoreAnnotations.TokensAnnotation.class)
				.forEach(token -> lemmas.add(token.get(CoreAnnotations.LemmaAnnotation.class))));
		System.out.println("The lemmas are " + lemmas);
	}

	private void coref(String text) {
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

	public void parse(String text) {
		System.out.println("Parsing the sentence : " + text);
		Annotation annotation = new Annotation(text);
		pipeline.annotate(annotation);
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		sentences.forEach(sentence -> {
			Tree tree = sentence.get(TreeAnnotation.class);
			printTree(tree, tree, null, 0);

			tree.pennPrint();
			SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
			// dependencies.prettyPrint();
		});
	}

	private String printTree(Tree root, Tree tree, Tree parent, int index) {
		// System.out.println("Start:" + tree.label());
		// tree.printLocalTree();
		// System.out.println("+++++++++");
		String result = "";
		for (int i = 0; i < tree.getChildrenAsList().size(); ++i) {
			// System.out.println(tree.getChild(i).label().value());
			if (tree.getChild(i).label().value().equals("CC")) {
				String topLeft = printTopLeft(root, tree, index);
				String bottomLeft = printLeft(root, tree, i);
				String bottomRight = printRight(tree, i);
				String topRight = printRight(parent, index);
				// System.out.println("Top Left: " + topLeft);
				// System.out.println("Top Right: " + topRight);
				// System.out.println("Bottom Left: " + bottomLeft);
				// System.out.println("Bottom Right: " + bottomRight);
				// System.out.println("Result is " + result);
				System.out.println("Sentences 1: " + topLeft + bottomLeft + topRight);
				System.out.println("Sentences 2: " + topLeft + bottomRight + topRight);
			} else if (tree.getChild(i).isLeaf()) {
				result += " " + tree.getChild(i).value();
			}

			result += " " + printTree(root, tree.getChild(i), tree, i);
		}

		return result;
		// System.out.println("End:" + tree.label());
	}

	private String printTopLeft(Tree root, Tree node, int index) {
		// System.out.println("LEFT");
		String result = "";
		for (int i = 0; i < root.getChildrenAsList().size(); i++) {
			if (node.equals(root.getChild(i)))
				break;
			result += " " + printTreeNodeWithStop(root.getChild(i), node);
		}
		return result;
	}

	private String printLeft(Tree root, Tree node, int index) {
		// System.out.println("LEFT");
		String result = "";
		for (int i = 0; i < index && node != null; i++) {
			result += " " + printTreeNode(node.getChild(i));
		}
		return result;
	}

	private String printRight(Tree node, int index) {
		// System.out.println("RIGHT");
		String result = "";
		for (int i = index + 1; node != null && i < node.getChildrenAsList().size(); i++) {
			result += " " + printTreeNode(node.getChild(i));
		}

		return result;
	}

	private String printTreeNodeWithStop(Tree tree, Tree stopNode) {
		String result = "";
		if (tree.isLeaf()) {
			result += " " + tree.label().value();
		}
		// tree.printLocalTree();
		// System.out.println("+++++++++");
		for (int i = 0; i < tree.getChildrenAsList().size(); ++i) {
			if (tree.getChild(i).equals(stopNode))
				break;
			result += printTreeNodeWithStop(tree.getChild(i), stopNode);
		}

		return result;
	}

	private String printTreeNode(Tree tree) {
		String result = "";
		if (tree.isLeaf()) {
			result += " " + tree.label().value();
		}
		// tree.printLocalTree();
		// System.out.println("+++++++++");
		for (int i = 0; i < tree.getChildrenAsList().size(); ++i) {
			result += printTreeNode(tree.getChild(i));
		}

		return result;
	}

	public static void main(String args[]) throws Exception {
		
//		System.out.println("Total is " + getCount(23));
//		System.out.println("Total is " + getCount(41));
//		System.out.println("Total is " + getCount(100));
//		System.out.println("Total is " + getCount(120));

		NewSentenceSplitter.useAdditionalRules = true;
		testSplitter(getSentences(false));
//		NewSentenceSplitter.useAdditionalRules = false;
//		testSplitterWithClassify(getSentences(true));
		// testEntropy();
		// testCorefence();

		// parser.annotate("lebron and curry last game stats");
		// parser.coref("Barack Obama was born in Hawaii. He is the president.
		// Obama was elected in 2008.");
	}

	public static void testCorefence() {
		CorefResolver resolver = new CorefResolver();
		resolver.printCoref("Barack Obama was born in Hawaii. He is the president. Obama was elected in 2008.");
	}

	private static void testSplitter(List<String> sentences) throws Exception {
		NewSentenceSplitter splitter = new NewSentenceSplitter();
		for (String sentence : sentences) {
			System.out.println("===================== Start of parse");
			System.out.println("Sentence : " + sentence);
			List<String> result = splitter.breakIndependent(sentence);
			result.stream().forEach(splitSentence -> {
				System.out.println("+++ Split Sentence:" + splitSentence);
			});
		}
	}
	
	private static void testSplitterWithClassify(List<String> sentences) throws Exception {
		ModelTrainer modelTrainer = new ModelTrainer();
//		ILPClassifier weatherClassifier = modelTrainer.trainWeather(false);;
		ILPClassifier nbaClassifier =  modelTrainer.trainNBAWeatherFinance(false);

		NewSentenceSplitter splitter = new NewSentenceSplitter();
		for (String sentence : sentences) {
			System.out.println("===================== Start of parse");
			clasifyAndPrint(nbaClassifier, sentence);
			List<String> result = splitter.breakIndependent(sentence);
			result.stream().forEach(splitSentence -> {
				System.out.println("+++Split Sentence:");
				try {
					clasifyAndPrint(nbaClassifier, splitSentence);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
		}
	}

	public static void clasifyAndPrint(ILPClassifier classifier, String sentence) throws Exception {
		System.out.println("Sentence : " + sentence);
		if (classifier != null) {
			ModelOutput op = classifier.classify(sentence);
			System.out.println("Model Output: " + op.getBestMatch());
			System.out.println("Model Entroy: " + TextParser.calculateEntropy(op));
		}
	}

	private static void testEntropy() {
		List<Double> distrs = new ArrayList<>();
		distrs.add(0.25);
		distrs.add(0.25);
		distrs.add(0.25);
		distrs.add(0.25);
		System.out.println("Uniform data entroy :" + getEntropy(distrs));
		distrs.clear();
		distrs.add(0.75);
		distrs.add(0.15);
		distrs.add(0.05);
		distrs.add(0.05);
		System.out.println("un-uniform data entroy :" + getEntropy(distrs));
		distrs.clear();
		distrs.add(0.90);
		distrs.add(0.025);
		distrs.add(0.025);
		distrs.add(0.025);
		System.out.println("un-uniform data entroy :" + getEntropy(distrs));
		distrs.clear();
		distrs.add(0.5);
		distrs.add(0.5);
		System.out.println("un-uniform data entroy :" + getEntropy(distrs));
	}

	private static double calculateEntropy(ModelOutput output) {
		List<Double> distrs = new ArrayList<>();
		for (IntentMatch match : output.getIntents()) {
			distrs.add(match.getScore());
		}
		return getEntropy(distrs);
	}

	private static Double getEntropy(List<Double> distrs) {
		double entropy = 0;
		for (double dist : distrs) {
			double plogp = -dist * Math.log(dist);
//			System.out.println("Plog p for " + dist + " is " + plogp + " log p is " + Math.log(dist));
			entropy += (plogp);
		}

		return entropy;
	}
	
	private static long getCount(int pending) {
		if(pending == 0 )
			return 0;
		if(pending < 0) {
			return Integer.MAX_VALUE;
		}
	
		return Math.min(1 + getCount(pending - 20), 1 + getCount(pending - 1));
	}

	private static List<String> getSentences(boolean nba) {
		String s1 = "Compare lebron and curry";
		String s2 = "Show curry stats and compare him with lebron";
		String s3 = "Warriors last game and overall stats.";
		String s4 = "Warriors and curry last game stats";
		String s5 = "Show me last game stats of curry and green.";
		String s6 = "Give me curry’s news and stats.";
		String s7 = "How is weather in sfo right now and is it going to snow there tomorrow ?";
		String s8 = "How is market today and price of AAPL.";
		String s9 = "lebron and curry last game stats";
		String s14 = "APPL share price and weather in sfo";
		String s15 = "weather in sfo for warriors match today";
		String s16 = "weather in sfo and warriors last game stats";
		String s17 = "weather in sfo and los angeles";
		String s22 = "What should I know about LBJ and his most recent performance?";
		String s23 = "What did LBJ end up with for all of his stats after the last game that he played in? ";
		String s24 = "Was LBJ's play any good or did he fail to deliver in his last game? ";
		String s25 = "Tell me which player has made more three pointers, Curry or Kobe. ";
		String s26 = "which player had better overall isolation in 2014-2015, curry or kobe";

		List<String> sentences = new ArrayList<>();
		sentences.add(s1);
		sentences.add(s2);
		sentences.add(s3);
		sentences.add(s4);
		sentences.add(s5);
		sentences.add(s6);
		sentences.add(s7); 
		sentences.add(s8);
		sentences.add(s9);
		sentences.add(s14);
		sentences.add(s15);
		sentences.add(s16);
		sentences.add(s17);
		sentences.add(s22);
		sentences.add(s23);
		sentences.add(s24);
		sentences.add(s25);
		sentences.add(s26);
		
		List<String> sentencesSimple = new ArrayList<>();
		String s10 = "i want to have food near my brothers place after the match today";
		String s11 = "i want to have food and watch cricket near my brothers place after the match today";
		String s12 = "Call it Jenny Bongo. Do they accept credit cards? ";
		String s13 = "I don ' t want raw and dressed with vinegar and horseradish. I want Appalachian Greasy beans";
		String s18 = "I want to buy a cheap bottle of red wine on my way to my sister's house tomorrow";
		String s19 = "I want to buy a cheap bottle of wine that goes well with lasagna on my way to my sister's house tomorrow";
		String s20 = "I want to have dinner after my workout, with amit and his familiy near his house";
		String s21 = "I don ' t want raw and dressed but with vinegar and horseradish. I want Appalachian Greasy beans";
		String s27 = "I want to buy a flight ticket for knicks next game";
		String s28 = "Can you give me a well reviewed restaurant in the Upper East Side that serves tacos that me and my cousin Frank can go to tonight after work?";
		String s29 = "Hi can you tell me where the closes Five Guys is located? Wanted to go tonight with my friend Mike.";
		String s30 = "My boyfriend Chuck and I are looking for a restaurant to have dinner at 9PM close to Pier 39 in San Francisco. Can you suggest a place?";
		String s31 = "Can you give me a well reviewed restaurant in the Upper East Side that serves tacos that me and my cousin Frank can go together tonight after work?";
		String s32 = "i want to have dinner after my workout near my home for her birthday";

		sentencesSimple.add(s31);
		sentencesSimple.add(s10);
		sentencesSimple.add(s11);
		sentencesSimple.add(s12);
		sentencesSimple.add(s13);
		sentencesSimple.add(s15);
		sentencesSimple.add(s18);
		sentencesSimple.add(s19);
		sentencesSimple.add(s20);
		sentencesSimple.add(s21);
		sentencesSimple.add(s27);
		sentencesSimple.add(s28);
		sentencesSimple.add(s29);
		sentencesSimple.add(s30);
		sentencesSimple.add(s32);

		
		if(nba)
			 return sentences;

		return sentencesSimple;
	}
}
