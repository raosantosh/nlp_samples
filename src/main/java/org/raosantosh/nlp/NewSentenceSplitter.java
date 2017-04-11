package com.yahoo.presto.nlp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class NewSentenceSplitter {

	private StanfordCoreNLP pipeline;

	public static boolean useAdditionalRules = false;

	private Map<String, String> phraseEquivanlence = new HashMap<>();

	public NewSentenceSplitter() {
		Properties tokenizationProps = new Properties();
		tokenizationProps.put("annotators", "tokenize, ssplit, pos, lemma, parse, ner");
		tokenizationProps.put("parse.model", "edu/stanford/nlp/models/lexparser/englishRNN.ser.gz");
		pipeline = new StanfordCoreNLP(tokenizationProps);

		phraseEquivanlence.put("NP", "NP");
		phraseEquivanlence.put("WHNP", "NP");
		phraseEquivanlence.put("S", "NP");
		phraseEquivanlence.put("SBAR", "NP");
		phraseEquivanlence.put("NP-TMP", "NP");
		phraseEquivanlence.put("VP", "VP");
	}

	public List<String> breakIndependent(String text) {
		List<String> finalResult = new ArrayList<>();
		Annotation annotation = new Annotation(text);
		pipeline.annotate(annotation);
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		sentences.forEach(sentence -> {
			List<String> result = new ArrayList<>();
			result.add(" ");
			Tree tree = sentence.get(TreeAnnotation.class);
			tree.pennPrint();
			printTree(tree, tree, null, 0, result);
			finalResult.addAll(result);
		});
		return finalResult;
	}

	private void printTree(Tree root, Tree tree, Tree parent, int index, List<String> resultList) {
		boolean containsConjunction = tree.getChildrenAsList().stream()
				.filter(treeNode -> treeNode.value().equals("CC")).collect(Collectors.toList()).size() > 0;

		boolean vpAndContainsPPNP = false;
		boolean npAndContainsNPPP = false;

		if (useAdditionalRules) {
			vpAndContainsPPNP = tree.value().equals("VP")
					&& (tree.getChildrenAsList().stream().filter(treeNode -> getByType(treeNode.value()).equals("NP"))
							.collect(Collectors.toList()).size() > 0)
					&& tree.getChildrenAsList().stream().filter(treeNode -> treeNode.value().equals("PP"))
							.collect(Collectors.toList()).size() > 0 ;
							
			vpAndContainsPPNP = vpAndContainsPPNP || tree.value().equals("VP")
									&& (tree.getChildrenAsList().stream().filter(treeNode -> getByType(treeNode.value()).equals("PP"))
											.collect(Collectors.toList()).size() > 0)
									&& tree.getChildrenAsList().stream().filter(treeNode -> treeNode.value().equals("PP"))
											.collect(Collectors.toList()).size() > 0 ;

			npAndContainsNPPP = tree.value().equals("NP")
					&& (tree.getChildrenAsList().stream().filter(treeNode -> getByType(treeNode.value()).equals("NP"))
							.collect(Collectors.toList()).size() > 0)
					&& tree.getChildrenAsList().stream().filter(treeNode -> treeNode.value().equals("PP"))
							.collect(Collectors.toList()).size() > 0 && (tree.getChildrenAsList().size() != 2);
		}

		if (containsConjunction) {
			List<String> finalResultList = new ArrayList<>();
			List<String> newResultList = new ArrayList<>(resultList);
			for (int i = 0; i < tree.getChildrenAsList().size(); ++i) {
				if (!getByType(tree.getChild(i).label().value()).equals("CC")) {
					printTree(root, tree.getChild(i), tree, i, newResultList);
				} else {
					finalResultList.addAll(newResultList);
					newResultList = new ArrayList<>(resultList);
				}
			}
			finalResultList.addAll(newResultList);
			resultList.clear();
			resultList.addAll(finalResultList);
		} else if (vpAndContainsPPNP) {
			List<String> finalResultList = new ArrayList<>();
			List<String> newResultList = new ArrayList<>(resultList);
			for (int i = 0; i < tree.getChildrenAsList().size(); ++i) {
				if (!getByType(tree.getChild(i).label().value()).equals("NP")
						&& !getByType(tree.getChild(i).label().value()).equals("PP")) {
					printTree(root, tree.getChild(i), tree, i, newResultList);
					newResultList = new ArrayList<>(newResultList);
				} else {
					List<String> tempNewResultList = new ArrayList<>(newResultList);
					printTree(root, tree.getChild(i), tree, i, newResultList);
					finalResultList.addAll(newResultList);
					newResultList = new ArrayList<>(tempNewResultList);
				}
			}
			resultList.clear();
			resultList.addAll(finalResultList);
		} else if (npAndContainsNPPP) {
			List<String> finalResultList = new ArrayList<>();
			List<String> newResultList = new ArrayList<>(resultList);
			for (int i = 0; i < tree.getChildrenAsList().size(); ++i) {
				if (!getByType(tree.getChild(i).label().value()).equals("NP")
						&& !getByType(tree.getChild(i).label().value()).equals("PP")) {
					printTree(root, tree.getChild(i), tree, i, newResultList);
					newResultList = new ArrayList<>(newResultList);
				} else {
					List<String> tempNewResultList = new ArrayList<>(newResultList);
					printTree(root, tree.getChild(i), tree, i, newResultList);
					finalResultList.addAll(newResultList);
					newResultList = new ArrayList<>(tempNewResultList);
				}
			}
			resultList.clear();
			resultList.addAll(finalResultList);
		} else {
			for (int i = 0; i < tree.getChildrenAsList().size(); ++i) {
				if (tree.getChild(i).isLeaf()) {
					List<String> newList = new ArrayList<>();
					for (String temp : resultList) {
						temp += tree.getChild(i).value() + " ";
						newList.add(temp);
					}
					resultList.clear();
					resultList.addAll(newList);
				}
				printTree(root, tree.getChild(i), tree, index, resultList);
			}
		}
	}

	private String getByType(String input) {
		if (phraseEquivanlence.containsKey(input)) {
			return phraseEquivanlence.get(input);
		}

		return input;
	}
}
