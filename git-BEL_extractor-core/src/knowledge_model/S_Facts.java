package knowledge_model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;



import java.util.Map.Entry;






import org.json.simple.JSONArray;
import org.json.simple.JSONObject;




import utility.Debug;
//import opennlp.tools.util.Span;
//import reddit_anaysis.utility;
import utility.utility;
import utility.Span;
import utility.Debug.DEBUG_CONFIG;
/**
 * Facts at the server level
 * @author huangxc
 *
 */
public class S_Facts {
	C_Facts cfacts;
	
	//facts///
		ArrayList<ArrayList<String>> merge_facts = null;//each element corresponds to a sentence

	public S_Facts(C_Facts cfacts) {
		this.cfacts = cfacts;
		this.merge_facts = new ArrayList<>();
	}

	public void mergeFacts() {
		if(this.merge_facts == null) this.merge_facts = new ArrayList<ArrayList<String>>();
		boolean toPrint = false;
		if(toPrint) {
			Iterator<Integer> itr = this.cfacts.spans.get(0).keySet().iterator();
			for(int t = 0; t < cfacts.facts.get(0).size(); t++) {//for each sentence
				int q = itr.next();
				Debug.println(cfacts.facts.get(0).get(t) + "\t" + cfacts.relativeOrder.get(0).get(t) + "\t" + q + ".." + this.cfacts.spans.get(0).get(q) ,DEBUG_CONFIG.debug_S_Facts);
			}
		}
		for(int t = 0; t < cfacts.facts.size(); t++) {//for each sentence
			ArrayList<String> final_fact = new ArrayList<String>();
			ArrayList<Span> index = cfacts.relativeOrder.get(t);
			LinkedHashMap<Integer, Integer> physicalIndex = cfacts.spans.get(t);
			ArrayList<String> fact = cfacts.facts.get(t);
			
			int curIndex = -1;//endIndex
			int curPhysicalIndex = - 1;//physicalIndex
//			Iterator itr = index.entrySet().iterator();
			String curFact = "";
			if(index.size() != fact.size()) {
				Debug.println("--" + index.size() + "\t" + fact.size() + "\t" + t,DEBUG_CONFIG.debug_S_Facts);
				final_fact.addAll(fact);
				this.merge_facts.add(final_fact);
				continue;
			}
			Iterator<Integer> itr = physicalIndex.keySet().iterator();
			for(int i = 0; i < fact.size(); i++) {//merge facts
//				Map.Entry me = (Map.Entry)itr.next();
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
		cleanFacts();
	}
	public void cleanFacts() {
		//1. empty parenthesis
		//2. empty between two punctuations: hard to decide. e.g. a , , , b. can we delete ,?
		
		//delete it if the first fact is , e.g. Yet, we could not identify it.
//		for(ArrayList<String> senten : merge_facts) {
//			int i = 0;//index of the first meaningful fact
//			while(i < senten.size()) {
//				if(senten.get(i).startsWith(",") 
//				|| senten.get(i).startsWith(".") ) i++;
//				else break;
//			}
//			merge_facts.set(merge_facts.indexOf(senten),new ArrayList<String>( senten.subList(i, senten.size())));
//			
//		}
		
		
	}
	public void printFacts() {
		Debug.println("--------------------",DEBUG_CONFIG.debug_S_Facts);
		for(int i = 0; i < merge_facts.size(); i++) {
//			Debug.print("[ Page " + (cfacts.pageNum + 1) + ";" + cfacts.paraNum + "th paragraph of section " +  cfacts.secNum + ";" +  i + "th sentence: ");
			for(int j = 0; j < merge_facts.get(i).size(); j++) {
				Debug.print(merge_facts.get(i).get(j) + "\t",DEBUG_CONFIG.debug_S_Facts);
			}
//			Debug.println(merge_facts.get(i));
			Debug.println(DEBUG_CONFIG.debug_S_Facts);
		}
	}
	public String getFirstFact() {
		return merge_facts.get(0).toString();
	}
	public void writeFacts(String path) {
		utility util = new utility();
		for(int i = 0; i < merge_facts.size(); i++) {
			String s = "* ";
//			if(merge_facts.get(i).size() < 2) {
//				util.writeFile(path, 
////						"[ Page " + (cfacts.pageNum + 1) + ";" + cfacts.paraNum + "th paragraph of section " +  cfacts.secNum + ";" +  i + "th sentence: " +
//			"* " + "\r\n",
//						true);
//			}
			for(int j = 0; j < merge_facts.get(i).size(); j++) {
				s += merge_facts.get(i).get(j);
				if(j != merge_facts.get(i).size() - 1) s += ", ";
//				Debug.print();
			}
			util.writeFile(path, 
//					"[ Page " + (cfacts.pageNum + 1) + ";" + cfacts.paraNum + "th paragraph of section " +  cfacts.secNum + ";" +  i + "th sentence: " +
//		"* " +	merge_facts.get(i).toString().replace("[", "").replace("]", "") + "\r\n",
					s + "\r\n\r\n",
					true);
			util.writeFile(path, "\r\n",true);
		}
	}
	
	public String toString(boolean xml, boolean withOriginalText, int startingIndex, boolean withDetails) {
		utility util = new utility();
		String output = "";
		for(int i = 0; i < merge_facts.size(); i++) {
			String s = "* ";
//			if(merge_facts.get(i).size() < 2) {
//				util.writeFile(path, 
////						"[ Page " + (cfacts.pageNum + 1) + ";" + cfacts.paraNum + "th paragraph of section " +  cfacts.secNum + ";" +  i + "th sentence: " +
//			"* " + "\r\n",
//						true);
//			}
			for(int j = 0; j < merge_facts.get(i).size(); j++) {
				s += merge_facts.get(i).get(j);
				if(j != merge_facts.get(i).size() - 1) s += "    ";
//				Debug.print();
			}
//			util.writeFile(path, 
//					"[ Page " + (cfacts.pageNum + 1) + ";" + cfacts.paraNum + "th paragraph of section " +  cfacts.secNum + ";" +  i + "th sentence: " +
//		"* " +	merge_facts.get(i).toString().replace("[", "").replace("]", "") + "\r\n",
//					s + "\r\n\r\n",
//					true);
			if(startingIndex != -1 && withOriginalText) 
				output += "sent" + startingIndex + "\t";
			
			if(withOriginalText && !xml) output += cfacts.sentences.get(i) + "\r\n";
			if(withOriginalText && xml) output += cfacts.sentences.get(i);
			if(startingIndex != -1) output += "fact" + startingIndex + "\t";
			output += s + "\r\n";
			if(withDetails) output += cfacts.details.get(i) + "\r\n";
			if(!xml) output += "\r\n";
			if(startingIndex != -1) startingIndex++;
			
//			util.writeFile(path, "\r\n",true);
		}
		output = output.replace("-LRB-", "(").replace("-RRB-", ")").replace("-RSB-", "]").replace("-LSB-", "[");
		if(xml) output += "----------------------------------------------------------------\r\n";
		return output;
	}
	
	public JSONArray toJSON(int startingIndex) {
		utility util = new utility();
		String output = "";
		JSONArray objs = new JSONArray();
		for(int i = 0; i < merge_facts.size(); i++) {
			String s = "* ";
//			if(merge_facts.get(i).size() < 2) {
//				util.writeFile(path, 
////						"[ Page " + (cfacts.pageNum + 1) + ";" + cfacts.paraNum + "th paragraph of section " +  cfacts.secNum + ";" +  i + "th sentence: " +
//			"* " + "\r\n",
//						true);
//			}
			for(int j = 0; j < merge_facts.get(i).size(); j++) {
				s += merge_facts.get(i).get(j);
				if(j != merge_facts.get(i).size() - 1) s += "    ";
//				Debug.print();
			}
			if(startingIndex < 0) 
				Debug.println("ERROR in toJASON! startingIndex==" + startingIndex,DEBUG_CONFIG.debug_error);
			JSONObject obj = new JSONObject();
			obj.put("type", "Sentence");
			obj.put("senID", startingIndex);
			obj.put("sentence", cfacts.sentences.get(i));
			obj.put("fact", s.replace("-LRB-", "(").replace("-RRB-", ")").replace("-RSB-", "]").replace("-LSB-", "["));
			startingIndex++;
			objs.add(obj);
//			util.writeFile(path, "\r\n",true);
		}
		output = output.replace("-LRB-", "(").replace("-RRB-", ")").replace("-RSB-", "]").replace("-LSB-", "[");
		return objs;
	}
	
	public int getSecNum() {
		return cfacts.secNum;
	}
	public int getParaNum() {
		return cfacts.paraNum;
	}
	public int getSize() {
		return this.merge_facts.size();
	}
}
