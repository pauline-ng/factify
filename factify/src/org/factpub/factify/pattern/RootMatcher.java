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
package org.factpub.factify.pattern;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nlp.Sequence;

import org.factpub.factify.knowledge_model.C_Facts;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import utility.Debug;
import utility.Span;
import utility.Utility;
import utility.Debug.DEBUG_CONFIG;

/**
 * <pre>
 * Main entrance of rule matching. All machters implement the same interface {@link Matcher#Match(Sequence) Matcher}. 
 * Each pattern is extracted independently. At the end, conflicts are resolved in {@link #resolveSpans(List) resolveSpans}; 
 * </pre>
 * 
 * <pre>
 * Three types of Matchers (implementing Matcher interface) are supported:
 * 1. {@link POSTagMatcher}: match pos tags;
 * 2. {@link RegularExpressionMatcher}: match regular expressions;
 * 3. {@link PreBuiltWordListMatcher}: match pre-built word list
 * The matchers are read from an input file. 
 * For more information, please visit <a href="https://github.com/happybelly/fact-extractor-multiple-java-projects/wiki/Rule-Specification-Files">Rule Specification</a>.
 * It is flexible to change matchers by modifying the input file without modifying the source code.
 * </pre>
 * <pre>
 * Another five rules are hard coded: Units, TextToNumbers, Acronyms, P-values, Frequent NGrams on {@link PatternMatcher}
 * </pre>
 * 
 *
 */
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
			@SuppressWarnings("unchecked")
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
						preBuiltWordMatcher = new PreBuiltWordListMatcher(wordList, type, fileVersion_, inputFilePath, inputFileVersionFromRoot, match);
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
				}else if(line.startsWith("//")) continue;//skip comments
				else if(!line.trim().isEmpty()) postags.add(line);
			}
			br.close();
		} catch (IOException e) {
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
			List<List<Span>> matchingDetail = new ArrayList<List<Span>>();
			StringBuilder detail = new StringBuilder();
			for(Object matcher : matchers)
			{
				if(matcher instanceof POSTagMatcher) {
					POSTagMatcher posTagMatcher = (POSTagMatcher) matcher;
					List<Span> result = posTagMatcher.Match(s);
					if(printDetail)	
						for(Span span : result) 
							Debug.println("POSTagMatcher (" + posTagMatcher.getInputFileName() + "): " + span.getCoveredText(s.getSourceString()), DEBUG_CONFIG.debug_pattern);
					matchingDetail.add(result);
					detail.append("{POSTagMatcher: ");
					detail.append(posTagMatcher.getInputFileName());
					detail.append("\t" + posTagMatcher.getinputFileVersion());
					detail.append("\r\n");
					detail.append("Spans: \r\n"); 
					for(Span span : result) {
						String postag = s.getPOSTagOfWord(s.getWordIndexOfSpan(span));
						detail.append(span.toString());
						detail.append("\t");
						detail.append(span.getCoveredText(s.getSourceString()));
						detail.append("\t");
						detail.append(postag + "\r\n");
					}
					detail.append("}\r\n");
				}
				if(matcher instanceof RegularExpressionMatcher) {
					RegularExpressionMatcher regExpMatcher = (RegularExpressionMatcher) matcher;
					List<Span> result = regExpMatcher.Match(s);
					if(printDetail)	
						for(Span span : result) 
							Debug.println("RegularExpressionMatcher (" + regExpMatcher.getInputFileName() + "): " + span.getCoveredText(s.getSourceString()), DEBUG_CONFIG.debug_pattern);
					matchingDetail.add(result);
					detail.append("{RegularExpressionMatcher: ");
					detail.append(regExpMatcher.getInputFileName());
					detail.append("\t");
					detail.append(regExpMatcher.getInputFileVersion() + "\r\n");
					detail.append("Spans: \r\n"); 
					for(Span span : result) {
						detail.append(span.toString());
						detail.append("\t");
						detail.append(span.getCoveredText(s.getSourceString()));
						detail.append("\r\n");
					}
					detail.append("}\r\n");
				}
				if(matcher instanceof PreBuiltWordListMatcher) {
					PreBuiltWordListMatcher preBuiltWordMatcher = (PreBuiltWordListMatcher) matcher;
					List<Span> result = preBuiltWordMatcher.Match(s);
					if(printDetail)	
						for(Span span : result) 
							Debug.println("PreBuiltWordListMatcher (" + preBuiltWordMatcher.getInputFileName() + "): " + span.getCoveredText(s.getSourceString()), DEBUG_CONFIG.debug_pattern);
					matchingDetail.add(result);
					detail.append("{PreBuiltWordListMatcher: ");
					detail.append(preBuiltWordMatcher.getInputFileName());
					detail.append("\t");
					detail.append(preBuiltWordMatcher.getInputFileVersion() + "\r\n");
					detail.append("Spans: \r\n"); 
					for(Span span : result) {
						detail.append(span.toString());
						detail.append("\t");
						detail.append(span.getCoveredText(s.getSourceString()));
						detail.append("\r\n");
					}
					detail.append("}\r\n");
				}
			}
			{
				if(freSeq_ != null){
					List<Span> ngrams_s = pm.extractNGrams(s, freSeq_);
					if(printDetail)				for(Span span : ngrams_s) Debug.println("ngrams:" + span.getCoveredText(s.getSourceString()),DEBUG_CONFIG.debug_pattern);
					matchingDetail.add(ngrams_s);
					detail.append("freq ngrams: \r\n");
					detail.append("Spans: \r\n"); 
					for(Span span : ngrams_s) {
						detail.append(span.toString());
						detail.append("\t");
						detail.append(span.getCoveredText(s.getSourceString()));
						detail.append("\r\n");
					}
					detail.append("}\r\n");
				}
			}
			{

				List<Span> units = pm.extractUnits(s);
				if(printDetail) for(Span span : units) Debug.println("units:" + span.getCoveredText(s.getSourceString()),DEBUG_CONFIG.debug_pattern);
				matchingDetail.add(units);
				detail.append("Units: \r\n");
				detail.append("Spans: \r\n"); 
				for(Span span : units) {
					detail.append(span.toString());
					detail.append("\t");
					detail.append(span.getCoveredText(s.getSourceString()));
					detail.append("\r\n");
				}
				detail.append("}\r\n");

			}
			{
				List<Span> textToNum = pm.extractTextToNum(s);
				if(printDetail)				for(Span span : textToNum) Debug.println("textToNum:" + span.getCoveredText(s.getSourceString()),DEBUG_CONFIG.debug_pattern);
				matchingDetail.add(textToNum);
				detail.append("textToNum: \r\n");
				detail.append("Spans: \r\n"); 
				for(Span span : textToNum) {
					detail.append(span.toString());
					detail.append("\t");
					detail.append(span.getCoveredText(s.getSourceString()));
					detail.append("\r\n");
				}
				detail.append("}\r\n");
			}
			if(acronyms!= null){
				List<Span> acros = pm.extractAcronyms(s, acronyms);
				matchingDetail.add(acros);
				detail.append("Acronyms: \r\n");
				detail.append("Spans: \r\n"); 
				for(Span span : acros) {
					detail.append(span.toString());
					detail.append("\t");
					detail.append(span.getCoveredText(s.getSourceString()));
					detail.append("\r\n");
				}
				detail.append("}\r\n");
			}
			{
				List<Span> pvalues = pm.extractP_Value(s);
				matchingDetail.add(pvalues);
				detail.append("PValue: \r\n");
				detail.append("Spans: \r\n"); 
				for(Span span : pvalues) {
					detail.append(span.toString());
					detail.append("\t");
					detail.append(span.getCoveredText(s.getSourceString()));
					detail.append("\r\n");
				}
				detail.append("}\r\n");
			}
			List<Span> after = resolveSpans(matchingDetail);
			allFacts.add(after);
			matchingDetail_description.add(detail.toString());
			Debug.print(detail.toString(), DEBUG_CONFIG.debug_C_Facts);
		}
		C_Facts cFact = formFacts(allFacts, sentences, matchingDetail_description, pageRange);
		return cFact;
	}
	/**
	 * merge overlapping spans
	 * Merge sort
	 * @param all
	 * @return
	 */
	public  List<Span> resolveSpans(List<List<Span>> all) {
		List<Span> all_ = new ArrayList<Span>();
		for(List<Span> spans : all) all_.addAll(spans);
        Collections.sort(all_, new Comparator<Span>(){public int compare(Span i, Span j){ 
            if(i.getStart() == j.getStart()) return i.getEnd()-j.getEnd();
            else return i.getStart()-j.getStart();
        } });
        List<Span> ret = new ArrayList<Span>();
        if(all_.size() < 1) return ret;
        ret.add(new Span(all_.get(0).getStart(), all_.get(0).getEnd()));
        for(int i = 1; i < all_.size(); i++) {
        	Span last = ret.get(ret.size()-1);
        	Span cur = all_.get(i);
            if(last.getEnd() > cur.getStart()) last.setEnd(Math.max(last.getEnd(), cur.getEnd()));
            else ret.add(new Span(cur.getStart(), cur.getEnd()));
        }
        return ret;
	}
	
	/**
	 * Form the facts from all rules
	 * <pre>
	 * Any token that crosses the span of the extracted facts would be counted in.
	 * </pre>
	 * @param all_facts The output of {@link RootMatcher#resolveSpans(List) resolveSpan}
	 * @param sentens The source sentence
	 * @param details Record which rule matches which substring
	 * @param pageRange The pages the facts cover
	 * @return
	 */
	public  C_Facts formFacts(List<List<Span>> all_facts, List<Sequence> sentens, List<String> details, Span pageRange) {
		if(sentens.size() == 0) return null; 
		C_Facts cfacts = new C_Facts(pageRange.getStart(), pageRange.getEnd());
		for(int senIndex = 0; senIndex < sentens.size(); senIndex++) {
			List<Span> facts_per_sen = all_facts.get(senIndex);
			Sequence senten = sentens.get(senIndex);
			Utility.sortByStart(facts_per_sen);

			// Any token that crosses the span of all would be counted in.
			HashSet<Integer> crossToken = new HashSet<Integer>();//the relative order of tokens that the span cross
			for(int i = 0; i < facts_per_sen.size(); i++) {
				Span cur = facts_per_sen.get(i);
				for(int j = 0; j < senten.getWordCount(); j++) {
					Span token = senten.getSpanOfWord(j);
					if(token.intersects(cur)) crossToken.add(j);
				}
			}
			List<Integer> crossToken_ = new ArrayList<Integer>(); crossToken_.addAll(crossToken);
			Collections.sort(crossToken_);
			ArrayList<String> facts = new ArrayList<>();
			ArrayList<Span> relativeOrders = new ArrayList<Span>();
			LinkedHashMap<Integer, Integer> spans = new LinkedHashMap<>();
			for(int i = 0; i < crossToken_.size(); i++) {
				int relativeOrder = crossToken_.get(i);
				facts.add(senten.getWord(relativeOrder));
				relativeOrders.add(new Span(relativeOrder,relativeOrder));
				spans.put(senten.getSpanOfWord(relativeOrder).getStart(), senten.getSpanOfWord(relativeOrder).getEnd() - 1);
			}
			cfacts.addFact(facts, senIndex, relativeOrders, spans, details.get(senIndex));
			cfacts.addSentence(sentens.get(senIndex).getSourceString());
		}
		return cfacts;
	}
}
