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
package knowledge_model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import utility.Debug;
import utility.Span;
import utility.Debug.DEBUG_CONFIG;

/**
 * Facts at the client side.
 * 
 * designed for Experiments: nouns and numbers
 * 
 * An object of Facts corresponds to a paragraph in the fulltext
 * 
 * 
 *
 */

public class C_Facts {
	
	//location info// the following 3 are not in use
	public int pageNum;
	public int secNum;
	public int paraNum;
	
	//
	public Span pageRange;

	///////////////////////////////////
	
	//facts///
	ArrayList<ArrayList<String>> facts;//each element corresponds to a sentence
	
	//Note: don't mess up with the formal definition of Span
	ArrayList<ArrayList<Span>> relativeOrder;// each element is the start-end index of facts of a sentence e.g. <1,1> corresponds to the first TOKEN/WORD of the sentence.
	
	ArrayList<LinkedHashMap<Integer, Integer>> spans;// each element is the start-end index of facts of a sentence e.g. <1,1> corresponds to the first CHAR of the sentence.
	
	/////////////////////////////
	
	//intermediate data//////////////////////////
	public ArrayList<String> sentences;
	public ArrayList<Span[]> sentences_spans;
	////////////////////////////
	
	
	//for the flexibility of removing some rule////////////
	public List<List<List<Span>>> matchingDetail;
	public List<String> matchingDetial_description;
	/////////////
	
	
	public C_Facts(int pageNum, int secNum, int paraNum) {
		this.pageNum = pageNum;
		this.secNum = secNum;
		this.paraNum = paraNum;
		this.facts = new ArrayList<ArrayList<String>>();
		this.relativeOrder = new ArrayList<>();
		this.spans = new ArrayList<>();
		this.sentences = new ArrayList<String>();
		this.sentences_spans = new ArrayList<Span[]>();
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
		this.relativeOrder.add(relativeOrder);
		this.spans.add(span);
		this.matchingDetial_description.add(detail);
	}
	
	public void printFacts(boolean showPos) {
		for(int i = 0; i < facts.size(); i++) {
			Iterator<Entry<Integer, Integer>> itr_span = spans.get(i).entrySet().iterator();
			for(int j = 0; j < facts.get(i).size(); j++) {//facts of a sentence
				Debug.print("*" + facts.get(i).get(j) + "*",DEBUG_CONFIG.debug_C_Facts);
				if(showPos) Debug.print("[span: " + itr_span.next() + "] [order: " + relativeOrder.get(i).get(j).toString().replace(')', ']') + "] \t",DEBUG_CONFIG.debug_C_Facts);
			}
			Debug.println(DEBUG_CONFIG.debug_C_Facts);
		
		}
	}
	public void printFacts(boolean showPos, int senIndex) {
		int i = senIndex;
		Iterator<Entry<Integer, Integer>> itr_span = spans.get(i).entrySet().iterator();
		for(int j = 0; j < facts.get(i).size(); j++) {//facts of a sentence
			Debug.print("*" + facts.get(i).get(j) + "*",DEBUG_CONFIG.debug_C_Facts);
			if(showPos) Debug.print("[span: " + itr_span.next() + "] [order: " + relativeOrder.get(i).get(j).toString().replace(')', ']') + "] \t",DEBUG_CONFIG.debug_C_Facts);
		}
		Debug.println(DEBUG_CONFIG.debug_C_Facts);
	}
	
	public ArrayList<String> getFactsBySenIndex(int senIndex) {
		if(this.facts.size() -1 < senIndex ) return null;
		return this.facts.get(senIndex);
	}

	public ArrayList<Span> getRelativeOrder(int senIndex) {
		if(this.facts.size() -1 < senIndex ) return null;
		return this.relativeOrder.get(senIndex);
	}
	public Span getRelativeOrder(int senIndex, int factIndex) {
		if(this.facts.size() -1 < senIndex ) return null;
		if(this.facts.get(senIndex).size() -1 < factIndex) return null;
		return this.relativeOrder.get(senIndex).get(factIndex);
	}
	public boolean hasFacts() {
		return this.facts.size() > 0 ? true : false;
	}
	public boolean hasFacts(int senNum) {
		if(this.facts.size() -1 < senNum ) return false;
		return this.facts.get(senNum).size() > 0 ? true : false;
	}

	public Span getSpan(int senIndex, int factIndex) {
		int i = -1; 
		Iterator<Entry<Integer, Integer>> itr = this.spans.get(senIndex).entrySet().iterator();
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
 
}
