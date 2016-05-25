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
package nlp;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import utility.Span;


public class Sequence {
	private List<String> stems;//lemmas of string tokens/words
	private List<String> POSTags;
	private int senID;
	
	
	private List<String> words;//words/tokens without stemming
	private List<Span> spans; // [)
	
	private int absoluteFreq;
	
	private String sourceString;
	
	public Sequence(List<String> stems) {
		if(stems == null) {
			this.stems = null;
		}else {
			this.stems = new ArrayList<String>();
			this.stems.addAll(stems);
		}
	}
	public Sequence(String stem) {
		this.stems = new ArrayList<String>();
		if(stems == null) this.stems = null;
		else this.stems.add(stem);
	}
	public Sequence(List<String> stems, List<String> POSTags, List<Span> spans, String sourceString) {
		this.stems = new ArrayList<String>();
		if(stems == null) this.stems = null;
		else this.stems.addAll(stems);
		this.POSTags = new ArrayList<String>();
		if(POSTags == null) {
			this.POSTags = null;
		}
		else this.POSTags.addAll(POSTags);
		this.spans = new ArrayList<Span>();
		if(spans == null) this.spans = null;
		else this.spans.addAll(spans);
		this.setSourceString(sourceString);
	}

	public Sequence(List<String> words, List<String> stems, List<String> POSTags, List<Span> spans, String sourceString) {
		this.stems = new ArrayList<String>();
		if(stems == null) this.stems = null;
		else this.stems.addAll(stems);
		this.POSTags = new ArrayList<String>();
		if(POSTags == null) {
			this.POSTags = null;
		}
		else this.POSTags.addAll(POSTags);
		this.spans = new ArrayList<Span>();
		if(spans == null) this.spans = null;
		else this.spans.addAll(spans);
		this.setSourceString(sourceString);
		this.words = new ArrayList<String>();
		if(words == null)  this.words = null;
		else this.words.addAll(words);
	}
	public Sequence(List<String> words, List<String> stems, List<String> POSTags, List<Span> spans, String sourceString, int pageNum, int secNum, int paraNum, int senID) {
		this.stems = new ArrayList<String>();
		if(stems == null) this.stems = null;
		else this.stems.addAll(stems);
		this.POSTags = new ArrayList<String>();
		if(POSTags == null) {
			this.POSTags = null;
		}
		else this.POSTags.addAll(POSTags);
		this.spans = new ArrayList<Span>();
		if(spans == null) this.spans = null;
		else this.spans.addAll(spans);
		this.setSourceString(sourceString);
		this.words = new ArrayList<String>();
		if(words == null)  this.words = null;
		else this.words.addAll(words);
		this.senID = senID;
	}
	public Sequence(String[] stems) {
		this.stems = new ArrayList<String>();
		if(stems == null) this.stems = null;
		else for(int i = 0; i < stems.length; i++) this.stems.add(stems[i]);
	}
	
	public int hashCode() {
		
		return stems.toString().hashCode();
	}
	public boolean equals(Object obj) {
		return this.hashCode() == obj.hashCode();
	}
	
	public boolean isSubsequenceOrSelfOf(Sequence superS) {
		for(int i = 0; i < superS.getWordCount() - this.getWordCount() + 1; i++) {
			Sequence subseq = superS.getSubsequence(i, i + this.getWordCount());
			if(subseq.equals(this)) return true;
		}
		return false;
	}
	public int indexOfSequence(Sequence subS) {
		Sequence superS = this;
		for(int i = 0; i < superS.getWordCount() - subS.getWordCount() + 1; i++) {
			Sequence subseq = superS.getSubsequence(i, i + subS.getWordCount());
			if(subseq.equals(subS)) return i;
		}
		return -1;
	}
	
	public boolean isSupersequenceOrSelfOf(Sequence subS) {
		return subS.isSubsequenceOrSelfOf(this);
	}
	public boolean isSupersequenceOf(Sequence subS) {
		return subS.getWordCount() < this.getWordCount() && subS.isSubsequenceOrSelfOf(this);
	}
	public boolean isSupbsequenceOf(Sequence sup) {
		return sup.isSupersequenceOf(this);
	}
	
	//[begin, end)
	public Sequence getSubsequence(int begin, int end) {
		try{
		if(this.POSTags == null) return  new Sequence(this.stems.subList(begin, end));
		else {
			//update span to be relative pos
			List<Span> subSpans = new ArrayList<Span>(); subSpans.addAll(this.spans.subList(begin, end));
			int offset = subSpans.get(0).getStart();
			for(int i = 0; i < subSpans.size(); i++) subSpans.set(i, new Span(subSpans.get(i).getStart() - offset, subSpans.get(i).getEnd() - offset));
			Sequence newS = new Sequence(this.words.subList(begin, end), this.stems.subList(begin, end), 
					this.POSTags.subList(begin, end), subSpans,
					this.getSourceString().substring(this.spans.get(begin).getStart(), this.spans.get(end -1).getEnd())
					);
			return newS;
		}
				
		}
		catch(Exception e) {
			System.out.println(this.getSourceString() + "\t " + begin + "\t" + end);
			System.exit(0);
			return null;
		}
		}

	public String toString() {
		String s = "";
		for(int i = 0; i < this.stems.size(); i++) {
			s += this.getPrettyStem(i) + " ";
		}
		if(this.stems.size() > 0) s = s.substring(0, s.length() -1);//remove the space added to the end
		return s;
	}
	public String details() {
		String s = "";
		for(int i = 0; i < this.stems.size(); i++) {
			if(this.POSTags != null)
			s += this.stems.get(i) + "["+ this.POSTags.get(i) + "]" + " ";
			else 
				s += this.stems.get(i)  + " ";
		}
		if(this.stems.size() > 0) s = s.substring(0, s.length() -1);//remove the space added to the end
		return s;
	}
	public int getAbsoluteFreq() {
		return absoluteFreq;
	}
	public void setAbsoluteFreq(int absoluteFreq) {
		this.absoluteFreq = absoluteFreq;
	}
	public boolean containsNouns() {
		HashSet<String> nouns = new HashSet<String>();
		nouns.add("NN");
		nouns.add("NNS");
		nouns.add("NNP");
		nouns.add("NNPS");
		for(String tag : this.POSTags) {
			if(nouns.contains(tag)) return true;
		}
		return false;
	}
	/**
	 * check if stem as itself is equal to (not as a substring of) any existing stem
	 * @param stem
	 * @return
	 */
	public boolean containsIndivStem(String stem) {
		for(String ss : this.stems) {
			if(ss.trim().equals(stem.trim())) return true;
			if((ss.toLowerCase().equals("-lsb-") || ss.equals("[")) &&
					(stem.toLowerCase().equals("-lsb-") || stem.equals("["))) 
				return true;
			if((ss.toLowerCase().equals("-rsb-") || ss.equals("]")) &&
					(stem.toLowerCase().equals("-rsb-") || stem.equals("]"))) 
				return true;
			if((ss.toLowerCase().equals("-lrb-") || ss.equals("(")) &&
					(stem.toLowerCase().equals("-lrb-") || stem.equals("("))) 
				return true;
			if((ss.toLowerCase().equals("-rrb-") || ss.equals(")")) &&
					(stem.toLowerCase().equals("-lrrb-") || stem.equals(")"))) 
				return true;
		}
		return false;
	}
	public boolean endsWithIndivStem(String stem) {
		String last = this.stems.get(this.stems.size()-1);
		if(last.trim().equals(stem.trim())) return true;
		if((last.toLowerCase().equals("-lsb-") || last.equals("[")) &&
				(stem.toLowerCase().equals("-lsb-") || stem.equals("["))) 
			return true;
		if((last.toLowerCase().equals("-rsb-") || last.equals("]")) &&
				(stem.toLowerCase().equals("-rsb-") || stem.equals("]"))) 
			return true;
		if((last.toLowerCase().equals("-lrb-") || last.equals("(")) &&
				(stem.toLowerCase().equals("-lrb-") || stem.equals("("))) 
			return true;
		if((last.toLowerCase().equals("-rrb-") || last.equals(")")) &&
				(stem.toLowerCase().equals("-lrrb-") || stem.equals(")"))) 
			return true;
		return false;
	}
	public boolean startsWithIndivStem(String stem) {
		String start = this.stems.get(0);
		if(start.trim().equals(stem.trim())) return true;
		if((start.toLowerCase().equals("-lsb-") || start.equals("[")) &&
				(stem.toLowerCase().equals("-lsb-") || stem.equals("["))) 
			return true;
		if((start.toLowerCase().equals("-rsb-") || start.equals("]")) &&
				(stem.toLowerCase().equals("-rsb-") || stem.equals("]"))) 
			return true;
		if((start.toLowerCase().equals("-lrb-") || start.equals("(")) &&
				(stem.toLowerCase().equals("-lrb-") || stem.equals("("))) 
			return true;
		if((start.toLowerCase().equals("-rrb-") || start.equals(")")) &&
				(stem.toLowerCase().equals("-lrrb-") || stem.equals(")"))) 
			return true;
		return false;
	}
	
	public String getPrettyStem(int index) {
		String token = this.stems.get(index);
		if(token.toLowerCase().equals("-lsb-")) {
			return "[";
		}else if(token.toLowerCase().equals("-lrb-")) {
			return "(";
		}else if(token.toLowerCase().equals("-rsb-")) {
			return "]";
		}else if(token.toLowerCase().equals("-rrb-")) {
			return ")";
		}else return token;
	}
	
	public boolean containUnpairedParen() {
		Stack<String> stk = new Stack<>();
		for(int i = 0; i < this.getWordCount(); i++) {
			String token = this.getPrettyStem(i);
			switch(token) {
			case "(":
			case "[":
				stk.push(token);
				break;
			case ")":
				if(stk.isEmpty()) return true;
				String top = stk.pop();
				if(!top.equals(")")) return true;
				break;
			case "]":
				if(stk.isEmpty()) return true;
				top = stk.pop();
				if(!top.equals("]")) return true;
				break;
			}
		}
		if(!stk.empty()) return true;
		return false;
	}
	public boolean hasCommonIndivStem(Sequence s) {
		HashSet<String> these = new HashSet<String>();
		HashSet<String> those = new HashSet<String>();
		for(int i = 0; i < this.stems.size(); i++) these.add(this.getPrettyStem(i));
		for(int i = 0; i < s.stems.size(); i++) those.add(s.getPrettyStem(i));
		these.retainAll(those);
		if(these.size() > 0) return true;
		return false;
	}
	public String getWord(int index) {
		return this.words.get(index);
	}
	public String getStemOfWord(int index) {
		return this.stems.get(index);
	}
	public Span getSpanOfWord(int index) {
		return this.spans.get(index);
	}
	public String getPOSTagOfWord(int index) {
		return this.POSTags.get(index);
	}
	public String getSourceString() {
		return sourceString;
	}
	public void setSourceString(String sourceString) {
		this.sourceString = sourceString;
	}
	public int getWordCount() {
		if(this.words == null) return 0;
		return this.words.size();
	}
	public int getWordIndexOfSpan(Span s) {//expensive
		return this.spans.indexOf(s);
	}
	public void setPOSTagOfWord(int index, String tag) {
		this.POSTags.set(index, tag);
	}
}
