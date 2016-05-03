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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import nlp.Sequence;
import nlp.StanfordNLPLight;

public class NGrams {
	public List<Sequence> getFreqSequences(List<Sequence> sentences_tokens) {
		List<Sequence> sequences = extractNGrams(sentences_tokens);
		//check validity individually
		List<Sequence> validSeq = new ArrayList<Sequence>();
		for(Sequence s : sequences) {
			//filter1: absoluteFreq >=3 
			if(s.getAbsoluteFreq() <= 1)	continue;
			//filter2: is Valid
			if(StanfordNLPLight.getInstance().stopwords == null) StanfordNLPLight.getInstance().importStopWords();
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
				if(sup.isSupersequenceOf(s)  && sup.getAbsoluteFreq() == s.getAbsoluteFreq()) isClosed = false;
			}
			if(!isClosed) continue; 
			result.add(s);
		}
		for(int i = 0; i < result.size(); i++) {//sort by size
			for(int j = i + 1; j < result.size(); j++) {
				if(result.get(j).size() > result.get(i).size()) {
					Sequence temp = result.get(i);
					result.set(i, result.get(j)); result.set(j, temp);
				}
			}
		
		}
		return result;
			
	}
	public List<Sequence> extractNGrams(List<Sequence> sentences) {
		HashMap<Sequence, Integer> ngramsToFreq = new HashMap<Sequence, Integer>();
		for(Sequence sentence : sentences) {
			for(int n = 1; n < sentence.size(); n++) {
				for(int i = 0; i <= sentence.size() - n; i++) {
					Sequence ngram = sentence.getSubsequence(i, i + n);
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
		Map<Sequence, Integer> sorted_ngramsToFreq = sortByValue(ngramsToFreq);
		ArrayList<Sequence> keys = new ArrayList<Sequence>();
		keys.addAll(sorted_ngramsToFreq.keySet());
		for(Sequence ngram : keys) {
			ngram.setAbsoluteFreq(sorted_ngramsToFreq.get(ngram));
		}
		return keys;
	}
	
	private Map sortByValue(Map map) {
	     List list = new LinkedList(map.entrySet());
	     Collections.sort(list, new Comparator() {
	          public int compare(Object o1, Object o2) {
	               return ((Comparable) ((Map.Entry) (o1)).getValue())
	              .compareTo(((Map.Entry) (o2)).getValue());
	          }
	     });

	    Map result = new LinkedHashMap();
	    for (Iterator it = list.iterator(); it.hasNext();) {
	        Map.Entry entry = (Map.Entry)it.next();
	        result.put(entry.getKey(), entry.getValue());
	    }
	    return result;
	} 
	
	
	/**
	 * [begin,end)
	 * @param tokens
	 * @param begin
	 * @param end
	 * @return
	 */
	/*private String concatenate (ArrayList<String> tokens, int begin, int end) {
		String result = "";
		for(int i = begin; i < tokens.size() && i < end; i++) {
			result = result + " " + tokens.get(i);
		}
		
		return result.trim();
	}*/
	
	public boolean isStopWords(Sequence s) {
		for(int i = 0; i < s.stems.size(); i++) {
			if(!StanfordNLPLight.getInstance().stopwords.contains(s.stems.get(i))) return false;
		}
		return true;
	}
	public boolean isValid(Sequence s) {
		{
			String beginning = s.stems.get(0);
			beginning = beginning.trim();
			if(beginning.length() == 1) return false;
		}
		{
			String end = s.stems.get(s.stems.size() - 1);
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
