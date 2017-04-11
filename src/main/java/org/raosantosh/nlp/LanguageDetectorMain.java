package com.santrao.nlp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;

import com.google.common.base.Optional;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;

public class LanguageDetectorMain {

	LanguageDetector languageDetector = null;
	
	LangDetectCybozu cubozuDetector = new LangDetectCybozu();

	public LanguageDetectorMain() throws Exception {
		List<LanguageProfile> languageProfiles = new LanguageProfileReader()
				.readAllBuiltIn();

		languageDetector = LanguageDetectorBuilder
				.create(NgramExtractors.standard())
				.withProfiles(languageProfiles).build();
	}

	public static void main(String[] args) throws Exception {
		LanguageDetectorMain languageDetectorMain = new LanguageDetectorMain();
		languageDetectorMain.readFile();
	}

	private String detectLanguage(String content) {
		try {

//			Optional<LdLocale> lang = languageDetector.detect(content);
//
//			if (lang.isPresent()) {
//				return lang.get().getLanguage();
//			} else
//				return "def-en";
			
			return cubozuDetector.detect(content);

		} catch (Throwable t) {
			System.out.println("Error for :" + content);
			t.printStackTrace();
		}

		return "def-en";

	}

	private void readFile() throws Exception {
		FileReader fr = new FileReader(
				new File(
						"/Users/santrao/work/assistance/presto_data/multi-domain/v3/rawdata/web_other_40k.uniq.txt"));
		BufferedReader br = new BufferedReader(fr);

		File outputFile = new File(
				"/Users/santrao/work/assistance/presto_data/multi-domain/v3/rawdata/english_only.uniq.txt");

		// if file doesnt exists, then create it
		if (!outputFile.exists()) {
			outputFile.createNewFile();
		}

		FileWriter fw = new FileWriter(outputFile.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);

		int i = 0;

		String line;
		while ((line = br.readLine()) != null) {
			++i;
			String content = line.split("\t")[0];
			String lang = detectLanguage(content);
			if (lang.equals("en")) {
				bw.write(content);
				bw.newLine();
			}

			if ((i % 1000) == 0) {
				bw.flush();
				System.out.println("DOne with :" + i);
			}
		}

		br.close();
		fr.close();
		bw.close();
	}
}
