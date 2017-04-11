package com.yahoo.presto.nlp;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class SentenceSplitter {

	private StanfordCoreNLP pipeline;

	public SentenceSplitter() {
		Properties tokenizationProps = new Properties();
		tokenizationProps.put("annotators", "tokenize, ssplit, pos, lemma, parse, ner");
		pipeline = new StanfordCoreNLP(tokenizationProps);
	}

	public List<String> breakIndependent(String text) {
		List<String> result = new ArrayList<>();
		Annotation annotation = new Annotation(text);
		pipeline.annotate(annotation);
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		sentences.forEach(sentence -> {
			Tree tree = sentence.get(TreeAnnotation.class);
			tree.pennPrint();
			printTree(tree, tree, null, 0, result);
			
		});
		return result;
	}

	private String printTree(Tree root, Tree tree, Tree parent, int index, List<String> resultList) {
		String result = "";
		for (int i = 0; i < tree.getChildrenAsList().size(); ++i) {
			if (tree.getChild(i).label().value().equals("CC") || (tree.getChild(i).label().value().equals("PP") && tree.getChild(i).getChildrenAsList().size() > 1)) {
				String topLeft = printTopLeft(root, tree, index);
				String bottomLeft = printLeft(root, tree, i);
				String bottomRight = printRight(tree, i);
				String topRight = printRight(parent, index);
				resultList.add(topLeft + bottomLeft + topRight);
				resultList.add(topLeft + bottomRight + topRight);
			} else if (tree.getChild(i).isLeaf()) {
				result += " " + tree.getChild(i).value();
			}

			result += " " + printTree(root, tree.getChild(i), tree, i, resultList);
		}

		return result;
	}

	private String printTopLeft(Tree root, Tree node, int index) {
		String result = "";
		for (int i = 0; i < root.getChildrenAsList().size(); i++) {
			if (node.equals(root.getChild(i)))
				break;
			result += " " + printTreeNodeWithStop(root.getChild(i), node);
		}
		return result;
	}

	private String printLeft(Tree root, Tree node, int index) {
		String result = "";
		for (int i = 0; i < index && node != null; i++) {
			result += " " + printTreeNode(node.getChild(i));
		}
		return result;
	}

	private String printRight(Tree node, int index) {
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
		for (int i = 0; i < tree.getChildrenAsList().size(); ++i) {
			result += printTreeNode(tree.getChild(i));
		}

		return result;
	}

}
