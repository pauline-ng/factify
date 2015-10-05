package knowledge_model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;



//import opennlp.tools.util.Span;
import utility.Span;

import org.omg.PortableInterceptor.INACTIVE;

import debug.Debug;
import debug.Debug.DEBUG_CONFIG;

/**
 * Facts at the client side.
 * 
 * designed for Experiments: nouns and numbers
 * 
 * An object of Facts corresponds to a paragraph in the fulltext
 * 
 * @author huangxc
 *
 */

public class C_Facts {
	
	//location info//
	public int pageNum;
	public int secNum;
	public int paraNum;

	//the following coordinates are not in use because I have not found a good way to express paragraphs across two columns.
//	float m_x;                       //the left-end X axis of the text piece
//	float m_y;                       //the top-end Y axis of the text piece
//	float m_endX;                    //the right-end X axis of the text piece
//	float m_endY;                    //the bottom-end Y axis of the text piece
//	float m_xScale;                  //the X-scale of the text piece 
//	float m_yScale;                  //the Y-scale of the text piece 
	  
	///////////////////////////////////
	
	//facts///
	ArrayList<ArrayList<String>> facts;//each element corresponds to a sentence
//	ArrayList<Integer> senIndexToID;//each element is the location of a sentence in the paragraph: senIndexToID(i) is the absolute order of the sentence within the para
	ArrayList<String> details;
	
	//Note: don't mess up with the formal definition of Span
	ArrayList<ArrayList<Span>> relativeOrder;// each element is the start-end index of facts of a sentence e.g. <1,1> corresponds to the first TOKEN/WORD of the sentence.
	
	ArrayList<LinkedHashMap<Integer, Integer>> spans;// each element is the start-end index of facts of a sentence e.g. <1,1> corresponds to the first CHAR of the sentence.
	
	/////////////////////////////
	
	//intermediate data//////////////////////////
	public ArrayList<String> sentences;
	public ArrayList<Span[]> sentences_spans;
	////////////////////////////
	
	
	public C_Facts(int pageNum, int secNum, int paraNum) {
		this.pageNum = pageNum;
		this.secNum = secNum;
		this.paraNum = paraNum;
		this.facts = new ArrayList<ArrayList<String>>();
//		this.senIndexToID = new ArrayList<Integer>();
		this.relativeOrder = new ArrayList<>();
		this.spans = new ArrayList<>();
		this.sentences = new ArrayList<String>();
		this.sentences_spans = new ArrayList<Span[]>();
		this.details = new ArrayList<String>();
		
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
//		this.senIndexToID.add(senID);
		this.relativeOrder.add(relativeOrder);
		this.spans.add(span);
		this.details.add(detail);
	}
	
	public void printFacts(boolean showPos) {
		
		
		for(int i = 0; i < facts.size(); i++) {
			Iterator<Entry<Integer, Integer>> itr_span = spans.get(i).entrySet().iterator();
//			Iterator<Entry<Integer, Integer>> itr_order = relativeOrder.get(i).entrySet().iterator();
//			Entry[] index = (Entry[]) .toArray();
//			Entry[] order = (Entry[]) relativeOrder.get(i).entrySet().toArray();
//			Debug.print("[ Page " + (pageNum + 1) + "; " + paraNum + "th paragraph of section " +  secNum + "; " +  senIndexToID.get(i) + "th sentence: ");
			for(int j = 0; j < facts.get(i).size(); j++) {//facts of a sentence
				Debug.print("*" + facts.get(i).get(j) + "*",DEBUG_CONFIG.debug_C_Facts);
				if(showPos) Debug.print("[span: " + itr_span.next() + "] [order: " + relativeOrder.get(i).get(j).toString().replace(')', ']') + "] \t",DEBUG_CONFIG.debug_C_Facts);
			}
			Debug.println(DEBUG_CONFIG.debug_C_Facts);
		
		}
	}
public void printFacts(boolean showPos, int senIndex) {
		
		
//		for(int i = 0; i < facts.size(); i++) {
	int i = senIndex;
			Iterator<Entry<Integer, Integer>> itr_span = spans.get(i).entrySet().iterator();
//			Iterator<Entry<Integer, Integer>> itr_order = relativeOrder.get(i).entrySet().iterator();
//			Entry[] index = (Entry[]) .toArray();
//			Entry[] order = (Entry[]) relativeOrder.get(i).entrySet().toArray();
//			Debug.print("[ Page " + (pageNum + 1) + "; " + paraNum + "th paragraph of section " +  secNum + "; " +  senIndexToID.get(i) + "th sentence: ");
			for(int j = 0; j < facts.get(i).size(); j++) {//facts of a sentence
				Debug.print("*" + facts.get(i).get(j) + "*",DEBUG_CONFIG.debug_C_Facts);
				if(showPos) Debug.print("[span: " + itr_span.next() + "] [order: " + relativeOrder.get(i).get(j).toString().replace(')', ']') + "] \t",DEBUG_CONFIG.debug_C_Facts);
			}
			Debug.println(DEBUG_CONFIG.debug_C_Facts);
		
//		}
	}
	
	/**
	 * NOT IN USE
	 * insert cFacts into the sentence (senNum) where the begging span of cFacts is specified by beginningspan
	 * @param cFacts
	 * @param senNum
	 * @param beginningSpan
	 * @param length: the total span of cFacts (note that it would be longer than the spans recorded in cFacts)
	 * @param tokenNum: the total number of tokens
	 * e.g. cFacts = "(10)"
	 * the length of cFacts is 4; there is only one fact in cFacts i.e. 10 with span [1,2]
	 * @return
	 */
	public boolean insertFacts(C_Facts cFacts, int senNum, int whereToInsert, int length, int tokenNum) {//part of a sentence
		if(!cFacts.hasFacts()) return false;
		if(cFacts.facts.size() > 1) {Debug.println("ERROR: you are trying to insert facts from more than one sentences",DEBUG_CONFIG.debug_C_Facts); System.exit(0); return false; }
		int senIndex = -1;
		senIndex = senNum;
//		for(senIndex = 0; senIndex < this.senIndexToID.size(); senIndex++) if(this.senIndexToID.get(senIndex) == senNum) break;
//		if(senIndex == this.senIndexToID.size()) return false;
		
//		int whereToInsert = -1;
		Iterator<Entry<Integer, Integer>> spans_itr = this.spans.get(senIndex).entrySet().iterator();
//		int endOfPrevious = -1;
//		while(spans_itr.hasNext()) {
//			whereToInsert++;
//			Entry<Integer, Integer> entry = spans_itr.next();
//			if(whereToInsert < 1) {endOfPrevious = entry.getValue(); continue;}
//			int start = entry.getKey();
//			if(beginningSpan >= endOfPrevious && beginningSpan < start) break;
//			else endOfPrevious = entry.getValue();
//		}
//		//now insert
//		if(whereToInsert == this.spans.get(senIndex).size()) {Debug.println("ERROR: we don't know where to insert the fact! "); System.exit(0); return false;}
		ArrayList<Span> new_relativeOrder = new ArrayList<Span>();
		LinkedHashMap<Integer, Integer> new_spans = new LinkedHashMap<Integer, Integer>();
		
		int i = -1;
		
		spans_itr = this.spans.get(senIndex).entrySet().iterator();
		Span lastOrder;
		if(whereToInsert == 0) lastOrder = new Span(0,0); //only in use when insert to the end, so don't worry 
		else lastOrder= this.relativeOrder.get(senIndex).get(whereToInsert -1);
		Entry<Integer, Integer> lastSpan = null;
		while(spans_itr.hasNext()) {
			i++;
			Entry<Integer, Integer> span = spans_itr.next();
			if(!spans_itr.hasNext()) {
				lastSpan = span;
			}
			if(i < whereToInsert) {
				new_relativeOrder.add(this.relativeOrder.get(senIndex).get(i));
				new_spans.put((int)span.getKey(), (int)span.getValue());
				continue;
			}
			Span curOrder = this.relativeOrder.get(senIndex).get(i);
			if(i == whereToInsert) {//insert the new facts and put it as the 
				this.facts.get(senIndex).addAll(i, cFacts.facts.get(0));
				for(int j = 0; j < cFacts.facts.get(0).size(); j++) {//assign the same relative order
					Span curFactOrder = cFacts.getRelativeOrder(0, j);
					new_relativeOrder.add(new Span(curOrder.getStart() + curFactOrder.getStart(), curOrder.getStart() + curFactOrder.getEnd()));
					Span curFact_span = cFacts.getSpan(0, j);
					new_spans.put((int)span.getKey() + curFact_span.getStart(), (int)span.getKey() + curFact_span.getEnd());
				}
			}
			new_relativeOrder.add(new Span(curOrder.getStart() + tokenNum, curOrder.getEnd() + tokenNum));
			new_spans.put((int)span.getKey() + length, (int) span.getValue() + length);
			
		}
		
		if(whereToInsert == this.spans.get(senIndex).size()) {//insert to the end
			this.facts.get(senIndex).addAll(cFacts.facts.get(0));
			if(this.spans.get(senIndex).size() == 0) {//no facts
				int lastOrder_start = 0;
				int lastOrder_end = 0;
				int lastspan_start = 0;
				int lastspan_end = 0;
				for(int j = 0; j < cFacts.facts.get(0).size(); j++) {//assign the same relative order
					Span curFactOrder = cFacts.getRelativeOrder(0, j);
					new_relativeOrder.add(new Span(lastOrder_start +  curFactOrder.getStart(), lastOrder_end + curFactOrder.getEnd()));
					Span curFact_span = cFacts.getSpan(0, j);
					new_spans.put(lastspan_start + curFact_span.getStart(), lastspan_end + curFact_span.getEnd());
//					lastOrder_start = 
				}
			}else {
				int lastOrder_start = lastOrder.getStart() + 1;
				int lastOrder_end = lastOrder.getEnd() + 1;
				int lastspan_start = lastSpan.getKey() + 1;
				int lastspan_end = lastSpan.getValue() + 1;
				for(int j = 0; j < cFacts.facts.get(0).size(); j++) {//assign the same relative order
					Span curFactOrder = cFacts.getRelativeOrder(0, j);
					new_relativeOrder.add(new Span(lastOrder_start +  curFactOrder.getStart(), lastOrder_end + curFactOrder.getEnd()));
					Span curFact_span = cFacts.getSpan(0, j);
					new_spans.put(lastspan_start + curFact_span.getStart(), lastspan_end + curFact_span.getEnd());
					//				lastOrder_start = 
				}
			}
		}
		this.relativeOrder.remove(senIndex);
		this.relativeOrder.add(senIndex, new_relativeOrder);
		this.spans.remove(senIndex);
		this.spans.add(senIndex, new_spans);
		return true;
		
		
	}
	
	/**
	 * insert one fact (facts in one parenthesis) into a sentence. More details are in  insertFactsByTokenNum(List,int, List, list, list, list)
	 * NOTE:USE IN CAUTION
	 * @param cFacts
	 * @param senNum
	 * @param whereToInsert
	 * @param beginningSpan
	 * @param length
	 * @param tokenNum
	 * @return 1 -> insert; 2 -> update
	 */
	public int insertFactsByTokenNum(C_Facts cFacts, int senNum, int whereToInsert, int beginningSpan, int length, int tokenNum) {//part of a sentence
		if(!cFacts.hasFacts()) return -1;
		if(cFacts.facts.size() > 1) {Debug.println("ERROR: you are trying to insert facts from more than one sentences",DEBUG_CONFIG.debug_C_Facts); System.exit(0); return -1; }
		int senIndex = -1;
		senIndex = senNum;
		
//		//now insert
		ArrayList<Span> new_relativeOrder = new ArrayList<Span>();
		LinkedHashMap<Integer, Integer> new_spans = new LinkedHashMap<Integer, Integer>();
		
		int i = -1;
		Iterator<Entry<Integer, Integer>> spans_itr = this.spans.get(senIndex).entrySet().iterator();
		ArrayList<Span> orders = this.relativeOrder.get(senIndex);
		boolean hasInserted = false;
		boolean hasUpdated = false;
		for( i = 0; i < orders.size(); i++) {
			Span curOrder = orders.get(i);
			Entry<Integer,Integer> curSpan = spans_itr.next();
			if(  curOrder.getStart() < whereToInsert) {
				new_relativeOrder.add(this.relativeOrder.get(senIndex).get(i));
				new_spans.put((int)curSpan.getKey(), (int)curSpan.getValue());
				continue;
			}
			if(whereToInsert <= curOrder.getStart() && !hasInserted && !hasUpdated) {
				if(beginningSpan >= curSpan.getKey() && beginningSpan <= curSpan.getValue()) {//insert into the middle of the fact; update not insert
					String newFact = this.facts.get(senIndex).get(i);
					{
						newFact = newFact.substring(0, beginningSpan - curSpan.getKey()) + cFacts.sentences.get(0) + newFact.substring(beginningSpan - curSpan.getKey()) ;
								
					}
					this.facts.get(senIndex).set(i, newFact);
					hasUpdated = true;
					new_relativeOrder.add(new Span(curOrder.getStart(), curOrder.getEnd()));
					new_spans.put((int)curSpan.getKey(), (int) curSpan.getValue() + length);
				}
				else{//insert
					this.facts.get(senIndex).addAll(i, cFacts.facts.get(0));
					for(int j = 0; j < cFacts.facts.get(0).size(); j++) {//assign the same relative order
						Span curFactOrder = cFacts.getRelativeOrder(0, j);
						new_relativeOrder.add(new Span(whereToInsert + curFactOrder.getStart(), whereToInsert + curFactOrder.getEnd()));
						Span curFact_span = cFacts.getSpan(0, j);
						new_spans.put(beginningSpan + curFact_span.getStart(), beginningSpan + curFact_span.getEnd());
					}
					hasInserted = true;
					new_relativeOrder.add(new Span(curOrder.getStart() + tokenNum, curOrder.getEnd() + tokenNum));
					new_spans.put((int)curSpan.getKey() + length, (int) curSpan.getValue() + length);
				}
			}else {
				int offset_token = 0;
				if(hasInserted) offset_token = tokenNum;
				else if(hasUpdated) offset_token = 0;
				new_relativeOrder.add(new Span(curOrder.getStart() + offset_token, curOrder.getEnd() + offset_token));
				new_spans.put((int)curSpan.getKey() + length, (int) curSpan.getValue() + length);
			}
		}
		if(!hasInserted && !hasUpdated) {//insert to the end
			this.facts.get(senIndex).addAll(cFacts.facts.get(0));
			for(int j = 0; j < cFacts.facts.get(0).size(); j++) {//assign the same relative order
				Span curFactOrder = cFacts.getRelativeOrder(0, j);
				new_relativeOrder.add(new Span(whereToInsert + curFactOrder.getStart(), whereToInsert + curFactOrder.getEnd()));
				Span curFact_span = cFacts.getSpan(0, j);
				new_spans.put(beginningSpan + curFact_span.getStart(), beginningSpan + curFact_span.getEnd());
			}
			hasInserted = true;
		}
		this.relativeOrder.remove(senIndex);
		this.relativeOrder.add(senIndex, new_relativeOrder);
		this.spans.remove(senIndex);
		this.spans.add(senIndex, new_spans);
		return hasInserted == true ? 1 : (hasUpdated == true ? 2 : 0);
		
		
	}

	/**
	 * NOT IN USE
	 * @param cFacts: presumed to be ordered
	 * @param senNum
	 * @param beginningSpan: the beggining span of cFacts in the sentence, i.e. to help identify where to insert the fact
	 * @param length
	 * @return
	 */
	public boolean insertFacts(ArrayList<C_Facts> cFacts, int  senNum, ArrayList<Integer> whereToInsert, ArrayList<Integer> length, ArrayList<Integer> tokenNum) {
		int offset_whereToInsert = 0;
		for(int i = 0; i < cFacts.size(); i++) {
			if( cFacts.get(i).getFactsBySenIndex(0).size() > 0) {
				int whereTo = whereToInsert.get(i) + offset_whereToInsert;
				insertFacts(cFacts.get(i), senNum, whereTo, length.get(i), tokenNum.get(i));
				this.printFacts(true,senNum);
				offset_whereToInsert += cFacts.get(i).getFactsBySenIndex(0).size();
			}
		}
 		
		return false;
	}
	
	/**
	 * insert a list of facts (which were located in parenthesis, each element corresponds to one parenthesis) into sentence senNum
	 * NOTE:USE IN CAUTION
	 * @param cFacts: the WHOLE list of parethensis facts of the sentence
	 * @param senNum: which sentence
	 * @param whereToInsert: insert into the whereToInsert th token
	 * @param beginningSpan: character offset of the fact within the sentence
	 * @param length: total length of the parenthesis (to offset the span of the facts that are located after a parenthesis)
	 * @param tokenNum: total number of the tokens in the parethensis (to offset the relativeOrder of the facts that are located after a parenthesis)
	 * @return
	 */
	public boolean insertFactsByTokenNum(ArrayList<C_Facts> cFacts, int  senNum, ArrayList<Integer> whereToInsert,ArrayList<Integer> beginningSpan, ArrayList<Integer> length, ArrayList<Integer> tokenNum) {
		int offset_whereToInsert = 0;
		int offset_beggin = 0;
		for(int i = 0; i < cFacts.size(); i++) {
			if( cFacts.get(i).getFactsBySenIndex(0).size() > 0) {
				Debug.println("insert facts " + cFacts.get(i).getFactsBySenIndex(0),DEBUG_CONFIG.debug_C_Facts);
				int whereTo = whereToInsert.get(i) + offset_whereToInsert;
				int beginSpan = beginningSpan.get(i) + offset_beggin;
				int status = insertFactsByTokenNum(cFacts.get(i), senNum, whereTo, beginSpan, length.get(i), tokenNum.get(i));
				this.printFacts(true,senNum);
				if(status == 1 ) offset_whereToInsert += tokenNum.get(i);//# of tokens
				offset_beggin += length.get(i);
				Debug.println("end insert facts " + cFacts.get(i).getFactsBySenIndex(0),DEBUG_CONFIG.debug_C_Facts);
			}
		}
 		
		return false;
	}
	
	/**
	 * insert before the whereToInsert th fact 
	 * @param cFacts
	 * @param senId
	 * @param beginningSpan
	 * @return
	 */
	public int[] findWhereToInsert(C_Facts cFacts, int senId, int beginningSpan) {
			if(!cFacts.hasFacts()) return null;
			if(cFacts.facts.size() > 1) {Debug.println("ERROR: you are trying to insert facts from more than one sentences",DEBUG_CONFIG.debug_C_Facts); System.exit(0); return null; }
			int senIndex = -1;
			senIndex = senId;
//			for(senIndex = 0; senIndex < this.senIndexToID.size(); senIndex++) if(this.senIndexToID.get(senIndex) == senId) break;
//			if(senIndex == this.senIndexToID.size()) return null;
			
			
			int whereToInsert = -1;//the relative order
			boolean found = false; //to differeniate inserting to the last sentence or inserting to the end of the sentence
			if(beginningSpan == 0) {
				found = true;
				whereToInsert = 0 ;
			}else {
				Iterator<Entry<Integer, Integer>> spans_itr = this.spans.get(senIndex).entrySet().iterator();
				int endOfPrevious = -1;
				
				while(spans_itr.hasNext()) {
					whereToInsert++;
					Entry<Integer, Integer> entry = spans_itr.next();
					if(whereToInsert < 1) {
						endOfPrevious = entry.getValue(); 
						if(entry.getKey() > beginningSpan) {//insert as the first one
							break;
						}

						continue;}
					int start = entry.getKey();
					if(beginningSpan >= endOfPrevious && beginningSpan < start) {
						found = true;
						break;
					}
					else endOfPrevious = entry.getValue();
				}
			}
			if(found) {//insert to some sentence
				return new int[] {senIndex, whereToInsert};
			}
			if(whereToInsert == this.spans.get(senIndex).size() - 1 ) {//insert to the end
				whereToInsert++;
			}
			return new int[] {senIndex, whereToInsert};
	}
	
	
	
	public ArrayList<String> getFactsBySenIndex(int senIndex) {
		if(this.facts.size() -1 < senIndex ) return null;
		return this.facts.get(senIndex);
	}
//	public ArrayList<String> getFactsBySenNum(int senNum) {
//		int i = 0;
//		for(i = 0; i < this.senIndexToID.size(); i++) if(this.senIndexToID.get(i) == senNum) return this.facts.get(i);
//		return null;
//	}

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
		if(this.details.size() > id) return this.details.get(id);
		return null;
	}
	
//	public int insertFactsToTheEnd(C_Facts cFacts, int senNum, int length, int tokenNum) {//part of a sentence
//		if(cFacts.getFactsBySenIndex(0).size() == 0) return 0;
//		this.facts.get(senNum).addAll(cFacts.getFactsBySenIndex(0));
////		int curTotalToken = this.facts.get(0);
//		
//	}
 
}
