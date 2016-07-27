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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import utility.Debug;
import utility.Utility;
import utility.Span;
import utility.Debug.DEBUG_CONFIG;
/**
 * Facts at the server side, i.e. fulltext-free
 * (Post-process of client side facts).
 * 
 * An object of S_Facts corresponds to a paragraph in the fulltext
 * 
 * 
 */
public class S_Facts {
	final private C_Facts cfacts;
	
	/**
	 * final facts: each element corresponds to a sentence
	 */
	final private ArrayList<ArrayList<String>> merge_facts;

	public S_Facts(C_Facts cfacts) {
		this.cfacts = cfacts;
		this.merge_facts = new ArrayList<ArrayList<String>>();
	}

	public void mergeFacts() {
		boolean toPrint = false;
		if(toPrint) {
			Iterator<Integer> itr = this.cfacts.spanOnChar.get(0).keySet().iterator();
			for(int t = 0; t < cfacts.getFactOfOneSentence(0).size(); t++) {//for each sentence
				int q = itr.next();
				Debug.println(cfacts.getFactOfOneSentence(0).get(t) + "\t" + cfacts.getSpanOnToken(0).get(t) + "\t" + q + ".." + this.cfacts.spanOnChar.get(0).get(q) ,DEBUG_CONFIG.debug_S_Facts);
			}
		}
		for(int t = 0; t < cfacts.getSize(); t++) {//for each sentence
			ArrayList<String> final_fact = new ArrayList<String>();
			ArrayList<Span> index = cfacts.getSpanOnToken(t);
			LinkedHashMap<Integer, Integer> physicalIndex = cfacts.spanOnChar.get(t);
			ArrayList<String> fact = cfacts.getFactOfOneSentence(t);
			
			int curIndex = -1;//endIndex
			int curPhysicalIndex = - 1;//physicalIndex
			String curFact = "";
			if(index.size() != fact.size()) {
				Debug.println("--" + index.size() + "\t" + fact.size() + "\t" + t,DEBUG_CONFIG.debug_S_Facts);
				final_fact.addAll(fact);
				this.merge_facts.add(final_fact);
				continue;
			}
			Iterator<Integer> itr = physicalIndex.keySet().iterator();
			for(int i = 0; i < fact.size(); i++) {//merge facts
				int start_index = (int) index.get(i).getStart();
				int end_index = (int) index.get(i).getEnd();
				int start_physical_index = itr.next();
				int end_physical_index = physicalIndex.get(start_physical_index);
				if(curIndex == -1) {
					if(fact.get(i).equals(",") //e.g. Yet, we could not do it.
							|| fact.get(i).equals(".")) {
						continue;
					}
					curIndex = end_index;
					curPhysicalIndex = end_physical_index;
					curFact = fact.get(i);
					if(i == fact.size() - 1) {
						curFact = curFact.trim();
						final_fact.add(curFact);
					}
				}else {
					if(curIndex + 1 == start_index) {//ajacent facts, merge them
						curIndex = end_index;
						if(curPhysicalIndex >= start_physical_index - 1) {//in the same word
							curFact += fact.get(i);
						}else
							curFact += " " + fact.get(i);
						curPhysicalIndex = end_physical_index;
						if(i == fact.size() - 1) {
							curFact = curFact.trim();
							final_fact.add(curFact);
						}
					}else {
						curFact = curFact.trim();
						final_fact.add(curFact);
						curIndex = end_index;
						curPhysicalIndex = end_physical_index;
						curFact = fact.get(i);
						if(i == fact.size() - 1) {
							curFact = curFact.trim();
							final_fact.add(curFact);
						}
					}
				}
			}
			this.merge_facts.add(final_fact);
		}
		for(int t = 0; t < this.merge_facts.size(); t++) {
			for(int m = 0; m < this.merge_facts.get(t).size(); m++) {
				this.merge_facts.get(t).set(m, merge_facts.get(t).get(m).replace("Fig-", "Fig.").replace("fig-", "fig."));
				this.merge_facts.get(t).set(m, merge_facts.get(t).get(m).replace("ref-", "ref.").replace("Ref-", "Ref."));
				this.merge_facts.get(t).set(m, merge_facts.get(t).get(m).replace("eq-", "eq.").replace("Eq-", "Eq."));
			}
		}
	}
	
	public void printFacts() {
		Debug.println("--------------------",DEBUG_CONFIG.debug_S_Facts);
		for(int i = 0; i < merge_facts.size(); i++) {
			for(int j = 0; j < merge_facts.get(i).size(); j++) {
				Debug.print(merge_facts.get(i).get(j) + "\t",DEBUG_CONFIG.debug_S_Facts);
			}
			Debug.println(DEBUG_CONFIG.debug_S_Facts);
		}
	}
	public String getFirstFact() {
		return merge_facts.get(0).toString();
	}
	public void writeFacts(String path) {
		for(int i = 0; i < merge_facts.size(); i++) {
			String s = "* ";
			for(int j = 0; j < merge_facts.get(i).size(); j++) {
				s += merge_facts.get(i).get(j);
				if(j != merge_facts.get(i).size() - 1) s += ", ";
			}
			Utility.writeFile(path, 
					s + "\r\n\r\n",
					true);
			Utility.writeFile(path, "\r\n",true);
		}
	}
	
	public String toString(boolean xml, boolean withOriginalText, int startingIndex, boolean withDetails) {
		String output = "";
		for(int i = 0; i < merge_facts.size(); i++) {
			String s = "* ";
			for(int j = 0; j < merge_facts.get(i).size(); j++) {
				s += merge_facts.get(i).get(j);
				if(j != merge_facts.get(i).size() - 1) s += "    ";
			}
			if(startingIndex != -1 && withOriginalText) 
				output += "sent" + startingIndex + "\t";
			
			if(withOriginalText && !xml) output += cfacts.getSentence(i) + "\r\n";
			if(withOriginalText && xml) output += cfacts.getSentence(i);
			if(startingIndex != -1) output += "fact" + startingIndex + "\t";
			output += s + "\r\n";
			if(withDetails) output += cfacts.matchingDetial_description.get(i) + "\r\n";
			if(!xml) output += "\r\n";
			if(startingIndex != -1) startingIndex++;
		}
		output = output.replace("-LRB-", "(").replace("-RRB-", ")").replace("-RSB-", "]").replace("-LSB-", "[");
		if(xml) output += "----------------------------------------------------------------\r\n";
		return output;
	}
	
	@SuppressWarnings("unchecked")
	public JSONArray toJSON(int startingIndex) {
		JSONArray objs = new JSONArray();
		for(int i = 0; i < merge_facts.size(); i++) {
			StringBuilder s = new StringBuilder("* ");
			for(int j = 0; j < merge_facts.get(i).size(); j++) {
				s.append(merge_facts.get(i).get(j));
				if(j != merge_facts.get(i).size() - 1) s.append("    ");
			}
			if(startingIndex < 0) 
				Debug.println("ERROR in toJASON! startingIndex==" + startingIndex,DEBUG_CONFIG.debug_error);
			JSONObject obj = new JSONObject();
			obj.put("type", "Sentence");
			obj.put("senID", startingIndex);
			obj.put("paragraphPageRange", cfacts.getPageRange().toString());
			obj.put("fact", s.toString().replace("-LRB-", "(").replace("-RRB-", ")").replace("-RSB-", "]").replace("-LSB-", "["));
			obj.put("details", cfacts.matchingDetial_description.get(i));
			startingIndex++;
			objs.add(obj);
		}
		return objs;
	}
	
	public int getSize() {
		return this.merge_facts.size();
	}
}
