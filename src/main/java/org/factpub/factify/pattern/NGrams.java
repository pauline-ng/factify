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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.factpub.factify.nlp.Sequence;
import org.factpub.factify.nlp.StanfordNLPLight;

/**
 * Compute the (frequent) NGrams of a paper i.e. a list of {@link org.factpub.factify.nlp.Sequence Sequence}
 * 
 * <pre>
 * NGrams are represented as {@link org.factpub.factify.nlp.Sequence Sequence} too.
 * </pre>
 *
 */
public class NGrams {
	/**
	 * 
	 * @param sentences_tokens A list of sentences represented by {@link org.factpub.factify.nlp.Sequence Sequence}
	 * @return A list of frequent ngrams represented as {@link org.factpub.factify.nlp.Sequence Sequence}
	 * 
	 * <pre>
	 * A ngram is a frequent ngram if </br>
	 * 1. Its frequency is > 1 </br>
	 * 2. It is valid, e.g. it is not empty, not stop words, etc. See {@link #isValid(Sequence)}.</br>
	 * 3. It does not contain unpaired parenthesis. </br>
	 * 4. There does not exist another ngram of the same frequency that is a super sequence of it.
	 * </pre>
	 */
	public List<Sequence> getFreqSequences(List<Sequence> sentences_tokens) {
		List<Sequence> sequences = extractNGrams(sentences_tokens);
		//check validity individually
		List<Sequence> validSeq = new ArrayList<Sequence>();
		for(Sequence s : sequences) {
			//filter1: absoluteFreq >=3 
			if(s.getAbsoluteFreq() <= 1)	continue;
			//filter2: is Valid
			if(!isValid(s)) continue;
			//filter3: clean the parenthesis --require paired parenthesis
			if(s.containUnpairedParen()) continue;
			validSeq.add(s);
			
		}
		//check validity: cross-examination
		List<Sequence> result = new ArrayList<Sequence>();
		for(Sequence s : validSeq) {
			
			//filter1: is closed (see Data mining textbook), i.e. there does not exist a super sequence that has the same freq as it
			boolean isClosed = true;
			for(Sequence sup : validSeq) {
				if(sup.isSupersequenceOf(s)  && sup.getAbsoluteFreq() == s.getAbsoluteFreq()) {
					isClosed = false;
					break;
				}
			}
			if(!isClosed) continue; 
			result.add(s);
		}
		return result;
			
	}
	/**
	 * 
	 * @param sentences A list of sentences represented by {@link org.factpub.factify.nlp.Sequence Sequence}
	 * @return A list of ngrams represented as {@link org.factpub.factify.nlp.Sequence Sequence} with frequency
	 * 
	 * <pre>
	 * A ngram here refers to ngrams that contain at least one noun, and do not contain '.', ',', ';'. Because those delimiters indicate sentence boundary.
	 * </pre>
	 */
	public List<Sequence> extractNGrams(List<Sequence> sentences) {
		HashMap<Sequence, Integer> ngramsToFreq = new HashMap<Sequence, Integer>();
		for(Sequence sentence : sentences) {
			for(int len = 1; len < sentence.getWordCount(); len++) {
				for(int i = 0; i <= sentence.getWordCount() - len; i++) {
					Sequence ngram = sentence.getSubsequence(i, i + len);
					if(ngram.containsIndivStem(",") || ngram.containsIndivStem(".") || ngram.containsIndivStem(";") 
							|| ngram.containsIndivStem("-lrb-") || ngram.containsIndivStem("-rrb-")) continue;
					if(!ngram.containsNouns()) continue;
					if(ngramsToFreq.containsKey(ngram)) {
						ngramsToFreq.put(ngram, ngramsToFreq.get(ngram) + 1);
					}
					else ngramsToFreq.put(ngram, 1);
				}
			}
		}
		ArrayList<Sequence> keys = new ArrayList<Sequence>();
		keys.addAll(ngramsToFreq.keySet());
		for(Sequence ngram : keys) {
			ngram.setAbsoluteFreq(ngramsToFreq.get(ngram));
		}
		return keys;
	}	
	
	
	public boolean isStopWords(Sequence s) {
		for(int i = 0; i < s.getWordCount(); i++) {
			if(!StanfordNLPLight.INSTANCE.containsStopWord(s.getStemOfWord(i))) return false;
		}
		return true;
	}
	public boolean isValid(Sequence s) {
		{
			String beginning = s.getStemOfWord(0);
			beginning = beginning.trim();
			if(beginning.length() == 1) return false;
		}
		{
			String end = s.getStemOfWord(s.getWordCount() - 1);
			end = end.trim();
			if(end.length() == 1) return false;
		}
		if(isStopWords(s)) return false;
		if(s.endsWithIndivStem("of") 
				|| s.endsWithIndivStem("in") 
				|| s.endsWithIndivStem("within") 
				||s.endsWithIndivStem("also") 
				|| s.endsWithIndivStem("to") 
				|| s.endsWithIndivStem("be")
				||s.endsWithIndivStem("between")
				||s.endsWithIndivStem("for")
				|| s.equals(new Sequence("table")) 
				|| s.equals(new Sequence("study")) 
				|| s.equals(new Sequence("analysis"))) return false;
		if(s.startsWithIndivStem("and") 
				|| s.startsWithIndivStem("in") 
				|| s.startsWithIndivStem("of") 
				|| s.startsWithIndivStem("the")
				||s.startsWithIndivStem("between") 
				|| s.startsWithIndivStem("with") 
				||s.startsWithIndivStem("for")
				) return false;
		  TextToNum textToNum = new TextToNum();
		  if(textToNum.parse(s.toString()) != null) return false;//remove sole numbers
		return true;
	}
	public static boolean isNoun(String pos) {
		if(pos.equals("NN") || pos.equals("NNS") || pos.equals("NNP") || pos.equals("NNPS"))
		return true;
		return false;
	}
	
	
}
