/**
    Copyright (C) 2016, Genome Institute of Singapore, A*STAR  

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package pattern;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import knowledge_model.C_Facts;
import nlp.Sequence;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import utility.Debug;
import utility.Span;
import utility.Debug.DEBUG_CONFIG;

public class RootMatcher {
	private List<Object> matchers = new ArrayList<Object>();
	public boolean readMacher(String path) {
		JSONParser parser = new JSONParser();
		try {     
			Object obj = parser.parse(new FileReader(path));
			String parentFolder = new File(path).getParent();
			JSONObject jsonObject =  (JSONObject) obj;
			// loop array
			JSONArray rules = (JSONArray) jsonObject.get("Rules");
			Iterator<JSONObject> iterator = rules.iterator();
			while (iterator.hasNext()) {
				JSONObject rule = iterator.next();
				String type = (String) rule.get("type");
				String inputFileVersionFromRoot = (String) rule.get("inputFileVersion");
				String inputFilePath = (String) rule.get("inputFilePath");
				inputFilePath = parentFolder == null ? inputFilePath : parentFolder + File.separator + inputFilePath;
				String match = (String) rule.get("match");//"contain" or null
				Debug.println(rule.toString(), DEBUG_CONFIG.debug_temp);
				Debug.println(inputFileVersionFromRoot, DEBUG_CONFIG.debug_temp);
				Debug.println(inputFilePath, DEBUG_CONFIG.debug_temp);
				Debug.println(type, DEBUG_CONFIG.debug_temp);
				if(type == null 
						|| inputFileVersionFromRoot == null 
						|| inputFilePath == null 
						|| (inputFilePath != null && !(new File(inputFilePath)).exists())
						|| (inputFilePath != null && (new File(inputFilePath)).exists() && !(new File(inputFilePath).isFile()))
						) {
					Debug.println("Parameter error for inputFilePath: " + inputFilePath, DEBUG_CONFIG.debug_warning);
					continue;
				}
				switch(type) {
				case "postag":
				{
					StringBuilder fileVersion = new StringBuilder();
					HashSet<String> posTags = readList(inputFilePath, inputFileVersionFromRoot, fileVersion);
					String fileVersion_ = fileVersion.toString().trim();
					if(!fileVersion_.equals(inputFileVersionFromRoot)) {
						Debug.println("Warning: inputFile has a version: " + fileVersion_ + " while the root file says " + inputFileVersionFromRoot, DEBUG_CONFIG.debug_warning);
					}
					POSTagMatcher postTagMatcher = null;
					if(posTags != null)
						postTagMatcher = new POSTagMatcher(posTags, type, fileVersion_, inputFilePath, inputFileVersionFromRoot);
					if(postTagMatcher != null) matchers.add(postTagMatcher);
					break;
				}
				case "regExp": 
				{
					StringBuilder fileVersion = new StringBuilder();
					HashSet<String> regExps = readList(inputFilePath, inputFileVersionFromRoot, fileVersion);
					String fileVersion_ = fileVersion.toString().trim();
					if(!fileVersion_.equals(inputFileVersionFromRoot)) {
						Debug.println("Warning: inputFile has a version: " + fileVersion_ + " while the root file says " + inputFileVersionFromRoot, DEBUG_CONFIG.debug_warning);
					}
					RegularExpressionMatcher regExpMatcher = null;
					if(regExps != null)
						regExpMatcher = new RegularExpressionMatcher(regExps, type, fileVersion_, inputFilePath, inputFileVersionFromRoot);
					if(regExpMatcher != null) matchers.add(regExpMatcher);
					break;
				}
				case "preBuiltWordList":
				{
					StringBuilder fileVersion = new StringBuilder();
					HashSet<String> wordList = readList(inputFilePath, inputFileVersionFromRoot, fileVersion);
					String fileVersion_ = fileVersion.toString().trim();
					if(!fileVersion_.equals(inputFileVersionFromRoot)) {
						Debug.println("Warning: inputFile has a version: " + fileVersion_ + " while the root file says " + inputFileVersionFromRoot, DEBUG_CONFIG.debug_warning);
					}
					PreBuiltWordListMatcher preBuiltWordMatcher = null;
					if(match != null) {
						for(String s : wordList) {
							if(s.trim().contains("\\s")) {
								match = null;
								Debug.print("WARNING:" + inputFilePath + " is a containing match while it has a word with space \"" + s + "\"", DEBUG_CONFIG.debug_warning);
								break;
							}
						}
					}
					if(wordList != null)
						preBuiltWordMatcher = new PreBuiltWordListMatcher(wordList, type, fileVersion_, inputFileVersionFromRoot, inputFilePath,match);
					if(preBuiltWordMatcher != null) matchers.add(preBuiltWordMatcher);
					break;
				}
				default:
					
				}
				
			}
		} catch (FileNotFoundException e) {
			Debug.print("Error: File " + path + " does not exist!", DEBUG_CONFIG.debug_error);
			return false;
		} catch (IOException e) {
			Debug.print("Error: " + e.getMessage() + " when accessing file " + path, DEBUG_CONFIG.debug_error);
			return false;
		} catch (ParseException e) {
			Debug.print("Error: " + e.getMessage() + " when parsing jason file " + path, DEBUG_CONFIG.debug_error);
			return false;
		}
		if(this.matchers.size() == 0) {
			Debug.println("Fatal Error: no matcher is found!", DEBUG_CONFIG.debug_error);
			return false;
		}
		return true;
	}
	
	private HashSet<String> readList(String inputPath, String inputFileVersionFromRoot, StringBuilder sb) {
		File inputFile = new File(inputPath);
		if(!inputFile.exists() || !inputFile.isFile()) {
			Debug.print("Input rule file does not exist or is not a file! Path: " + inputFile.getAbsolutePath(), DEBUG_CONFIG.debug_warning);
			return null;
		}
		HashSet<String> postags = new HashSet<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(inputFile));
			String line = null;
			while((line = br.readLine()) != null) {
				line = line.trim();
				if(line.startsWith("--")) {
					if(line.startsWith("--version:")) {
						String version = line.substring("--version:".length());
						sb.append(version);
						
					}
				}else
					if(!line.trim().isEmpty()) postags.add(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return postags.size() > 0 ? postags : null;
	}
	
	public  C_Facts parsePara(List<Sequence> sentences, HashSet<Sequence> freSeq_, Map<String, Sequence> acronyms, Span pageRange) {
		if(this.matchers.size() == 0) {
			Debug.print("No matcher is found!", DEBUG_CONFIG.debug_error);
			return null;
		}
		List<List<Span>> allFacts = new ArrayList<List<Span>>();
		List<String> matchingDetail_description = new ArrayList<String>();
		boolean printDetail = true;
		PatternMatcher pm = new PatternMatcher();
		for(Sequence s : sentences) {
			Debug.println(s.POSTags, DEBUG_CONFIG.debug_pattern);
			List<List<Span>> matchingDetail = new ArrayList<List<Span>>();
			String detail = "";
			for(Object matcher : matchers)
			{
				if(matcher instanceof POSTagMatcher) {
					POSTagMatcher posTagMatcher = (POSTagMatcher) matcher;
					List<Span> result = posTagMatcher.Match(s);
					if(printDetail)	
						for(Span span : result) 
							Debug.println("POSTagMatcher (" + posTagMatcher.getInputFileName() + "): " + span.getCoveredText(s.sourceString), DEBUG_CONFIG.debug_pattern);
					matchingDetail.add(result);
					detail += "{POSTagMatcher: " + posTagMatcher.getInputFileName() + "\t" + posTagMatcher.getinputFileVersion() + "\r\n";
					detail += "Spans: \r\n"; 
					for(Span span : result) {
						String postag = s.POSTags.get(s.spans.indexOf(span));
						detail += span.toString() + "\t" + span.getCoveredText(s.sourceString) + "\t" + postag + "\r\n";
					}
					detail += "}\r\n";
				}
				if(matcher instanceof RegularExpressionMatcher) {
					RegularExpressionMatcher regExpMatcher = (RegularExpressionMatcher) matcher;
					List<Span> result = regExpMatcher.Match(s);
					if(printDetail)	
						for(Span span : result) 
							Debug.println("RegularExpressionMatcher (" + regExpMatcher.getInputFileName() + "): " + span.getCoveredText(s.sourceString), DEBUG_CONFIG.debug_pattern);
					matchingDetail.add(result);
					detail += "{RegularExpressionMatcher: " + regExpMatcher.getInputFileName()+ "\t"  + regExpMatcher.getInputFileVersion() + "\r\n";
					detail += "Spans: \r\n"; 
					for(Span span : result) {
						detail += span.toString()+ "\t" + span.getCoveredText(s.sourceString) + "\r\n";
					}
					detail += "}\r\n";
				}
				if(matcher instanceof PreBuiltWordListMatcher) {
					PreBuiltWordListMatcher preBuiltWordMatcher = (PreBuiltWordListMatcher) matcher;
					List<Span> result = preBuiltWordMatcher.Match(s);
					if(printDetail)	
						for(Span span : result) 
							Debug.println("PreBuiltWordListMatcher (" + preBuiltWordMatcher.getInputFileName() + "): " + span.getCoveredText(s.sourceString), DEBUG_CONFIG.debug_pattern);
					matchingDetail.add(result);
					detail += "{PreBuiltWordListMatcher: " + preBuiltWordMatcher.getInputFileName()+ "\t"  + preBuiltWordMatcher.getInputFileVersion() + "\r\n";
					detail += "Spans: \r\n"; 
					for(Span span : result) {
						detail += span.toString()+ "\t" + span.getCoveredText(s.sourceString) + "\r\n";
					}
					detail += "}\r\n";
				}
			}
			{
				if(freSeq_ != null){
					List<Span> ngrams_s = pm.extractNGrams(s, freSeq_);
					if(printDetail)				for(Span span : ngrams_s) Debug.println("ngrams:" + span.getCoveredText(s.sourceString),DEBUG_CONFIG.debug_pattern);
					matchingDetail.add(ngrams_s);
					detail += "freq ngrams: \r\n";
					detail += "Spans: \r\n"; 
					for(Span span : ngrams_s) {
						detail += span.toString() + "\t" + span.getCoveredText(s.sourceString)+ "\r\n";
					}
					detail += "}\r\n";
				}
			}
			{

				List<Span> units = pm.extractUnits(s);
				if(printDetail) for(Span span : units) Debug.println("units:" + span.getCoveredText(s.sourceString),DEBUG_CONFIG.debug_pattern);
				matchingDetail.add(units);
				detail += "Units: \r\n";
				detail += "Spans: \r\n"; 
				for(Span span : units) {
					detail += span.toString() + "\t" + span.getCoveredText(s.sourceString)+ "\r\n";
				}
				detail += "}\r\n";

			}
			{
				List<Span> textToNum = pm.extractTextToNum(s);
				if(printDetail)				for(Span span : textToNum) Debug.println("textToNum:" + span.getCoveredText(s.sourceString),DEBUG_CONFIG.debug_pattern);
				matchingDetail.add(textToNum);
				detail += "textToNum: \r\n";
				detail += "Spans: \r\n"; 
				for(Span span : textToNum) {
					detail += span.toString() + "\t" + span.getCoveredText(s.sourceString)+ "\r\n";
				}
				detail += "}\r\n";
			}
			if(acronyms!= null){
				List<Span> acros = pm.extractAcronyms(s, acronyms);
				matchingDetail.add(acros);
				detail += "Acronyms: \r\n";
				detail += "Spans: \r\n"; 
				for(Span span : acros) {
					detail += span.toString() + "\t" + span.getCoveredText(s.sourceString)+ "\r\n";
				}
				detail += "}\r\n";
			}
			{
				List<Span> pvalues = pm.extractP_Value(s);
				matchingDetail.add(pvalues);
				detail += "PValue: \r\n";
				detail += "Spans: \r\n"; 
				for(Span span : pvalues) {
					detail += span.toString() + "\t" + span.getCoveredText(s.sourceString)+ "\r\n";
				}
				detail += "}\r\n";
			}
			List<Span> after = pm.resolveSpans(matchingDetail, s);
			allFacts.add(after);
			matchingDetail_description.add(detail);
			Debug.print(detail, DEBUG_CONFIG.debug_C_Facts);
		}
		C_Facts cFact = pm.formFacts(allFacts, sentences, matchingDetail_description, pageRange);
		return cFact;
	}
}
