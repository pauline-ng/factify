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
package org.factpub.factify.knowledge_model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.factpub.factify.utility.Debug;
import org.factpub.factify.utility.Span;
import org.factpub.factify.utility.Debug.DEBUG_CONFIG;

/**
 * Facts at the client side.
 * 
 * An object of C_Facts corresponds to a paragraph in the fulltext
 * 
 */
//designed for Experiments: nouns and numbers
public class C_Facts {
	
	/**
	 * the pages the paragraph covers.
	 * [) ; start with 1.
	 * In correspondence with Paragraph.pages
	 */
	final private Span pageRange;

	///////////////////////////////////
	
	/**
	 * Each element corresponds to the facts of a sentence
	 */
	final private ArrayList<ArrayList<String>> facts;
	
	/**
	 * Each element is the start-end index of facts of a sentence e.g. [1,1] corresponds to the first TOKEN/WORD of the sentence.
	 * Note: don't mess up with the formal definition of Span, which is [,).
	 * 
	 */
	final private ArrayList<ArrayList<Span>> spanOnToken;
	
	/**
	 * Each element is the start-end index of facts of a sentence e.g. [1,1] corresponds to the first CHAR of the sentence.
	 * 
	 * spanOnToken is for token matches, such as postag or prebuilt word list, which treats the sentence as a list of tokens.
	 * spanOnChar is for regular expressions, which treats the sentence as a string.
	 */
	final ArrayList<LinkedHashMap<Integer, Integer>> spanOnChar;
	
	/////////////////////////////
	
	//intermediate data//////////////////////////
	/**
	 * List of sentences in the paragraph
	 */
	final private ArrayList<String> sentences;
	
	
	//for the flexibility of removing some rule////////////
	public List<List<List<Span>>> matchingDetail;
	public List<String> matchingDetial_description;
	/////////////
	
	public C_Facts(int startPage, int endPage) {
		this.pageRange = new Span(startPage, endPage);
		this.facts = new ArrayList<ArrayList<String>>();
		this.spanOnToken = new ArrayList<>();
		this.spanOnChar = new ArrayList<>();
		this.sentences = new ArrayList<String>();
		this.matchingDetail = new ArrayList<>();
		this.matchingDetial_description = new ArrayList<>();
	}
	
	/**
	 * for each sentence
	 * @param fact
	 * @param senID
	 * @param relativeOrder
	 * @param span
	 */
	public void addFact(ArrayList<String> fact, int senID, ArrayList<Span> relativeOrder,LinkedHashMap<Integer, Integer> span, String detail) {
		facts.add(fact);
		this.spanOnToken.add(relativeOrder);
		this.spanOnChar.add(span);
		this.matchingDetial_description.add(detail);
	}
	
	public void printFacts(boolean showPos) {
		for(int i = 0; i < facts.size(); i++) {
			Iterator<Entry<Integer, Integer>> itr_span = spanOnChar.get(i).entrySet().iterator();
			for(int j = 0; j < facts.get(i).size(); j++) {//facts of a sentence
				Debug.print("*" + facts.get(i).get(j) + "*",DEBUG_CONFIG.debug_C_Facts);
				if(showPos) Debug.print("[span: " + itr_span.next() + "] [order: " + spanOnToken.get(i).get(j).toString().replace(')', ']') + "] \t",DEBUG_CONFIG.debug_C_Facts);
			}
			Debug.println(DEBUG_CONFIG.debug_C_Facts);
		
		}
	}
	public void printFacts(boolean showPos, int senIndex) {
		int i = senIndex;
		Iterator<Entry<Integer, Integer>> itr_span = spanOnChar.get(i).entrySet().iterator();
		for(int j = 0; j < facts.get(i).size(); j++) {//facts of a sentence
			Debug.print("*" + facts.get(i).get(j) + "*",DEBUG_CONFIG.debug_C_Facts);
			if(showPos) Debug.print("[span: " + itr_span.next() + "] [order: " + spanOnToken.get(i).get(j).toString().replace(')', ']') + "] \t",DEBUG_CONFIG.debug_C_Facts);
		}
		Debug.println(DEBUG_CONFIG.debug_C_Facts);
	}
	
	public ArrayList<String> getFactsBySenIndex(int senIndex) {
		if(this.facts.size() -1 < senIndex ) return null;
		return this.facts.get(senIndex);
	}

	public ArrayList<Span> getRelativeOrder(int senIndex) {
		if(this.facts.size() -1 < senIndex ) return null;
		return this.spanOnToken.get(senIndex);
	}
	public Span getRelativeOrder(int senIndex, int factIndex) {
		if(this.facts.size() -1 < senIndex ) return null;
		if(this.facts.get(senIndex).size() -1 < factIndex) return null;
		return this.spanOnToken.get(senIndex).get(factIndex);
	}
	public boolean hasFacts() {
		return this.facts.size() > 0 ? true : false;
	}
	public boolean hasFacts(int senNum) {
		if(this.facts.size() -1 < senNum ) return false;
		return this.facts.get(senNum).size() > 0 ? true : false;
	}

	public Span getSpanOnChar(int senIndex, int factIndex) {
		int i = -1; 
		Iterator<Entry<Integer, Integer>> itr = this.spanOnChar.get(senIndex).entrySet().iterator();
		while(itr.hasNext()) {
			i++;
			if(i == factIndex) {
				Entry<Integer,Integer> entry = itr.next();
				return new Span(entry.getKey(), entry.getValue());
			}else {
				itr.next();
			}
		}
		return null;
	}

	public String toString() {
		return this.facts.toString();
	}
	public String getFactDetail(int id) {
		if(this.matchingDetial_description.size() > id) return this.matchingDetial_description.get(id);
		return null;
	}

	public Span getPageRange() {
		return pageRange;
	}

	public String getSentence(int i) {
		return sentences.get(i);
	}
	public boolean addSentence(String s) {
		return this.sentences.add(s);
	}
	public ArrayList<String> getFactOfOneSentence(int senIndex) {
		return this.facts.get(senIndex);
	}
	
	public int getSize(){
		return this.facts.size();
	}
	public ArrayList<Span> getSpanOnToken(int senIndex) {
		return this.spanOnToken.get(senIndex);
	}
 
}
