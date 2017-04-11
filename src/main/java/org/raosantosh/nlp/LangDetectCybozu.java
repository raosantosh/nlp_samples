package com.santrao.nlp;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

class LangDetectCybozu {
	
	private final String profiles[] = new String[] { "af","ar","bg","bn","cs","da","de","el", 
	        "en","es","et","fa","fi","fr","gu","he","hi","hr","hu","id","it","ja", 
	        "kn","ko","lt","lv","mk","ml","mr","ne","nl","no","pa","pl","pt","ro", 
	        "ru","sk","sl","so","sq","sv","sw","ta","te","th","tl","tr","uk","ur", 
	        "vi","zh-cn","zh-tw"}; 
	
	
	public LangDetectCybozu() throws Exception{
		// TODO Auto-generated constructor stub
		
		String[] models = new String[profiles.length]; 
        for (int i = 0; i < profiles.length; i++) { 
            InputStream s = getClass().getClassLoader(). 
                    getResourceAsStream("profiles/" + profiles[i]); 
            try { 
                models[i] = IOUtils.toString(s, "UTF-8"); 
            } catch (IOException ex) { 
            	ex.printStackTrace();
            } 
        } 
        DetectorFactory.loadProfile(Arrays.asList(models)); 
	}

	public String detect(String text) throws LangDetectException {
		Detector detector = DetectorFactory.create();
		detector.append(text);
		return detector.detect();
	}

	public ArrayList detectLangs(String text) throws LangDetectException {
		Detector detector = DetectorFactory.create();
		detector.append(text);
		return detector.getProbabilities();
	}
}