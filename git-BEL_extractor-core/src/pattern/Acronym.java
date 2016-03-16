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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import utility.Debug;
import utility.Debug.DEBUG_CONFIG;
import nlp.Sequence;
import nlp.StanfordNLPLight;


/**
 * this class is from a paper
 * @author huangxc
 *
 */
public class Acronym {

	/** 
	       Method findBestLongForm takes as input a short-form and a long-  
	       form candidate (a list of words) and returns the best long-form 
	       that matches the short-form, or null if no match is found. 
	 **/   
	public static String findBestLongForm(String shortForm, String longForm) { 
		int sIndex;     // The index on the short form 
		int lIndex;     // The index on the long form   
		char currChar;  // The current character to match 

		sIndex = shortForm.length() - 1;  // Set sIndex at the end of the short form 
		lIndex = longForm.length() - 1;   // Set lIndex at the end of the long form 
		for ( ; sIndex >= 0; sIndex--) {  // Scan the short form starting from end to start 
			// Store the next character to match. Ignore case 
			currChar = Character.toLowerCase(shortForm.charAt(sIndex)); 
			// ignore non alphanumeric characters 
			if (!Character.isLetterOrDigit(currChar))
				continue; 
			// Decrease lIndex while current character in the long form 
			// does not match the current character in the short form. 
			// If the current character is the first character in the 
			// short form, decrement lIndex until a matching character  
			// is found at the beginning of a word in the long form. 
			while ( 
					((lIndex >= 0) && (Character.toLowerCase(longForm.charAt(lIndex)) != currChar)) 
					|| 
					((sIndex == 0) && (lIndex > 0) && (Character.isLetterOrDigit(longForm.charAt(lIndex - 1))))) 
				lIndex--; 
			// If no match was found in the long form for the current character, return null (no match). 
			if (lIndex < 0) 
				return null; 
			// A match was found for the current character. Move to the next character in the long form. 
			lIndex--; 
		} 

		// Find the beginning of the first word (in case the first character matches the beginning of a hyphenated word).  
		lIndex = longForm.lastIndexOf(" ", lIndex) + 1; 
		// Return the best long form, the substring of the original long form, starting from lIndex up to the end of the original long form. 
		return longForm.substring(lIndex); 

	}
//	public static Map<String, Sequence> findAcronyms(String text, StanfordNLPLight nlp) {
//		Acronym acronym = new Acronym();
////		Properties props = new Properties();
////		props.put("annotators", "tokenize, ssplit, pos");
////		StanfordNLP nlp = new StanfordNLP(props);
////		if(nlp.stopwords == null) nlp.importStopWords();
//
//		Map<String, Sequence> acronyms_all = new HashMap<String, Sequence>();
//		String s = text;
//		Annotation annotation = new Annotation(s);
//		nlp.pipeline.annotate(annotation);
//		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
//		for(CoreMap sen_ : sentences) {
//			String sen = sen_.get(CoreAnnotations.TextAnnotation.class);
//			Stack<Integer> st = new Stack<Integer>();//stack for parenthesis
//			ArrayList<Integer> start_index_parenthesis = new ArrayList<Integer>();
//			ArrayList<Integer> end_index_parenthesis = new ArrayList<Integer>();
//			for(int i = 0; i < sen.length(); i++) {
//				char c = sen.charAt(i);
//				switch(c) {
//				case '(' :
//					st.push(i);
//					break;
//				case ')':
//					if(st.empty()) {//too many ')'
//						Debug.println("WARNING: there are unpaired parenthesis: " + sen,DEBUG_CONFIG.debug_warning);
//						break;
//					}
//					start_index_parenthesis.add((int)st.pop());
//					end_index_parenthesis.add(i);
//					//							Debug.println(sen);
//					break;
//				default:
//				}
//
//			}
//			if(!st.empty()) {//too many '('
//				Debug.println("WARNING: there are unpaired parenthesis: " + sen,DEBUG_CONFIG.debug_warning);
//			}
//			for(int i = 0; i < start_index_parenthesis.size(); i++) {
//				String acronyms = sen.substring(start_index_parenthesis.get(i) + 1, end_index_parenthesis.get(i));
//				String candidate = sen.substring(0, start_index_parenthesis.get(i));
//				candidate.trim();
//				candidate = acronym.findBestLongForm(acronyms, candidate);
//				//							Debug.println(acronyms + "\t" + candidate);
//				if(candidate != null && candidate.length() > 0) {
//					if(acronyms_all.containsKey(acronyms)) {
//						if(!candidate.equals(acronyms_all.get(acronyms))) {
//							Debug.println("WARNING: acronym \"" + acronyms + "\" has more than one long forms: \"" + candidate + "\" and " + acronyms_all.get(acronyms),DEBUG_CONFIG.debug_warning  );
//						}
//						continue;
//					}
//					if(candidate.length() > 20 * acronyms.length()) {//invalid
//						Debug.println("WARNING: INVALID acronym \"" + acronyms + "\" : \"" + candidate + "\" ",DEBUG_CONFIG.debug_warning );
//						continue;
//					}
//					acronyms_all.put(acronyms, nlp.textToSequence(candidate,false).get(0));
//				}
//			}
//
//		}
//		return acronyms_all;
//		
//	}
	public static Map<String, Sequence> findAcronyms(List<Sequence> sentences, StanfordNLPLight nlp) {
		Acronym acronym = new Acronym();
		Map<String, Sequence> acronyms_all = new HashMap<String, Sequence>();
		for(Sequence sen_ : sentences) {
			String sen = sen_.sourceString;
			Stack<Integer> st = new Stack<Integer>();//stack for parenthesis
			ArrayList<Integer> start_index_parenthesis = new ArrayList<Integer>();
			ArrayList<Integer> end_index_parenthesis = new ArrayList<Integer>();
			for(int i = 0; i < sen.length(); i++) {
				char c = sen.charAt(i);
				switch(c) {
				case '(' :
					st.push(i);
					break;
				case ')':
					if(st.empty()) {//too many ')'
						Debug.println("WARNING: there are unpaired parenthesis: " + sen,DEBUG_CONFIG.debug_warning);
						break;
					}
					start_index_parenthesis.add((int)st.pop());
					end_index_parenthesis.add(i);
					//							Debug.println(sen);
					break;
				default:
				}

			}
			if(!st.empty()) {//too many '('
				Debug.println("WARNING: there are unpaired parenthesis: " + sen,DEBUG_CONFIG.debug_warning);
			}
			for(int i = 0; i < start_index_parenthesis.size(); i++) {
				String acronyms = sen.substring(start_index_parenthesis.get(i) + 1, end_index_parenthesis.get(i));
				String candidate = sen.substring(0, start_index_parenthesis.get(i));
				candidate.trim();
				candidate = acronym.findBestLongForm(acronyms, candidate);
				//							Debug.println(acronyms + "\t" + candidate);
				if(candidate != null && candidate.length() > 0) {
					if(acronyms_all.containsKey(acronyms)) {
						if(!candidate.equals(acronyms_all.get(acronyms))) {
							Debug.println("WARNING: acronym \"" + acronyms + "\" has more than one long forms: \"" + candidate + "\" and " + acronyms_all.get(acronyms),DEBUG_CONFIG.debug_warning  );
						}
						continue;
					}
					if(candidate.length() > 20 * acronyms.length()) {//invalid
						Debug.println("WARNING: INVALID acronym \"" + acronyms + "\" : \"" + candidate + "\" ",DEBUG_CONFIG.debug_warning );
						continue;
					}
					acronyms_all.put(acronyms, nlp.textToSequence(candidate,false).get(0));
				}
			}

		}
		return acronyms_all;
		
	}
	public static Map<String, String> findAcronyms(String text) {
		Acronym acronym = new Acronym();
//		Properties props = new Properties();
//		props.put("annotators", "tokenize, ssplit, pos");
//		StanfordNLP nlp = new StanfordNLP(props);
//		if(nlp.stopwords == null) nlp.importStopWords();

//		Map<String, Sequence> acronyms_all = new HashMap<String, Sequence>();
		Map<String, String> acronyms_all = new HashMap<String, String>();
		String s = text;
			Stack<Integer> st = new Stack<Integer>();//stack for parenthesis
			ArrayList<Integer> start_index_parenthesis = new ArrayList<Integer>();
			ArrayList<Integer> end_index_parenthesis = new ArrayList<Integer>();
			for(int i = 0; i < s.length(); i++) {
				char c = s.charAt(i);
				switch(c) {
				case '(' :
					st.push(i);
					break;
				case ')':
					if(st.empty()) {//too many ')'
						Debug.println("WARNING: there are unpaired parenthesis: " + s,DEBUG_CONFIG.debug_warning);
						break;
					}
					start_index_parenthesis.add((int)st.pop());
					end_index_parenthesis.add(i);
					//							Debug.println(sen);
					break;
				default:
				}

			}
			if(!st.empty()) {//too many '('
				Debug.println("WARNING: there are unpaired parenthesis: " + s,DEBUG_CONFIG.debug_warning);
			}
			for(int i = 0; i < start_index_parenthesis.size(); i++) {
				String acronyms = s.substring(start_index_parenthesis.get(i) + 1, end_index_parenthesis.get(i));
				if(acronyms.length() < 2) continue;
				String candidate = s.substring(0, start_index_parenthesis.get(i));
				candidate.trim();
				candidate = acronym.findBestLongForm(acronyms, candidate);
				//							Debug.println(acronyms + "\t" + candidate);
				if(candidate != null && candidate.length() > 1) {
					if(acronyms_all.containsKey(acronyms)) {
						if(!candidate.equals(acronyms_all.get(acronyms))) {
							Debug.println("WARNING: acronym \"" + acronyms + "\" has more than one long forms: \"" + candidate + "\" and " + acronyms_all.get(acronyms) ,DEBUG_CONFIG.debug_warning );
						}
						continue;
					}
					if(candidate.length() > 20 * acronyms.length()) {//invalid
						Debug.println("WARNING: INVALID acronym \"" + acronyms + "\" : \"" + candidate + "\" ",DEBUG_CONFIG.debug_warning);
						continue;
					}
					if(candidate.length() < acronyms.length() + 4) continue; 
					acronyms_all.put(acronyms, candidate);
				}
			}
		return acronyms_all;
		
	}

}


