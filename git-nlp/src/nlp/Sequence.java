package nlp;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

//import org.apache.commons.math3.analysis.function.Subtract;

import utility.Span;


public class Sequence {
	public List<String> stems;//lemmas of string tokens/words
	public List<String> POSTags;
	public int pageNum;
	public int secNum;
	public int paraNum;
	public int senID;
	
	
	public List<String> words;//words/tokens without stemming
	public List<Span> spans; // [)
	
	private int absoluteFreq;
	
	private List<Sequence> supperSequencesONE;// for super sequences of size = this.size() + 1;
	private List<Sequence> subSequencesONE;// for sub-sequences of size = this.size() + 1;
	public String sourceString;
	
	public int isFrequent;//not in use
	
	//1 itself is frequent and its supper is not frequent;
	//2 itself is a subsequence of a frequent sequence; 
	//3 itself is the end of some maximally frequent sequence (i.e. *s is frequent (* may be empty)
	
	public Sequence(List<String> stems) {
		// TODO Auto-generated constructor stub
		this.stems = new ArrayList<String>();
		if(stems == null) this.stems = null;
		else this.stems.addAll(stems);
	}
	public Sequence(String stem) {
		// TODO Auto-generated constructor stub
		this.stems = new ArrayList<String>();
		if(stems == null) this.stems = null;
		else this.stems.add(stem);
	}
	public Sequence(List<String> stems, List<String> POSTags, List<Span> spans, String sourceString) {
		// TODO Auto-generated constructor stub
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
		this.sourceString = sourceString;
	}

	public Sequence(List<String> words, List<String> stems, List<String> POSTags, List<Span> spans, String sourceString) {
		// TODO Auto-generated constructor stub
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
		this.sourceString = sourceString;
		this.words = new ArrayList<String>();
		if(words == null)  this.words = null;
		else this.words.addAll(words);
	}
	public Sequence(List<String> words, List<String> stems, List<String> POSTags, List<Span> spans, String sourceString, int pageNum, int secNum, int paraNum, int senID) {
		// TODO Auto-generated constructor stub
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
		this.sourceString = sourceString;
		this.words = new ArrayList<String>();
		if(words == null)  this.words = null;
		else this.words.addAll(words);
		this.pageNum = pageNum;
		this.secNum = secNum;
		this.paraNum = paraNum;
		this.senID = senID;
	}
	public Sequence(String[] stems) {
		// TODO Auto-generated constructor stub
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
	
	public int size() {
		return this.stems.size();
	}
	
	public boolean isSubsequenceOrSelfOf(Sequence superS) {
		for(int i = 0; i < superS.size() - this.size() + 1; i++) {
			Sequence subseq = superS.getSubsequence(i, i + this.size());
			if(subseq.equals(this)) return true;
		}
		return false;
	}
	public int indexOfSequence(Sequence subS) {
		Sequence superS = this;
		for(int i = 0; i < superS.size() - subS.size() + 1; i++) {
			Sequence subseq = superS.getSubsequence(i, i + subS.size());
			if(subseq.equals(subS)) return i;
		}
		return -1;
	}
	
	public boolean isSupersequenceOrSelfOf(Sequence subS) {
		return subS.isSubsequenceOrSelfOf(this);
	}
	public boolean isSupersequenceOf(Sequence subS) {
		return subS.size() < this.size() && subS.isSubsequenceOrSelfOf(this);
	}
	public boolean isSupbsequenceOf(Sequence sup) {
		return sup.isSupersequenceOf(this);
	}
	
	//[begin, end)
	public Sequence getSubsequence(int begin, int end) {
//		if(this.sourceString.substring(this.spans.get(begin).getStart(), this.spans.get(end -1).getEnd()).equals("In")) {
//			System.out.println("debug");
//		}
		try{
		if(this.POSTags == null) return  new Sequence(this.stems.subList(begin, end));
		else {
			//update span to be relative pos
			List<Span> subSpans = new ArrayList<Span>(); subSpans.addAll(this.spans.subList(begin, end));
			int offset = subSpans.get(0).getStart();
			for(int i = 0; i < subSpans.size(); i++) subSpans.set(i, new Span(subSpans.get(i).getStart() - offset, subSpans.get(i).getEnd() - offset));
			Sequence newS = new Sequence(this.words.subList(begin, end), this.stems.subList(begin, end), 
					this.POSTags.subList(begin, end), subSpans,
					this.sourceString.substring(this.spans.get(begin).getStart(), this.spans.get(end -1).getEnd())
					);
			return newS;
		}
				
		}
		catch(Exception e) {
			System.out.println(this.sourceString + "\t " + begin + "\t" + end);
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
	public List<Sequence> getSupperSequences() {
		return supperSequencesONE;
	}
	public void setSupperSequences(List<Sequence> supperSequences) {
		this.supperSequencesONE = supperSequences;
	}
	public void addSupperSequences(Sequence supperSequences) {
		if(this.supperSequencesONE == null) this.supperSequencesONE = new ArrayList<Sequence>();
		this.supperSequencesONE.add(supperSequences);
	}
	
	public double getAvgSequenceOfSupperOne() {
		int total = 0;
		for(Sequence s : this.supperSequencesONE) {
			total += s.absoluteFreq;
		}
		return total / (double) this.supperSequencesONE.size();
		
	}
	public double[] getNormalDistributionOfSupperOne() {
		if(this.supperSequencesONE == null) return new double[] {-1,-1};
		double avg = this.getAvgSequenceOfSupperOne();
		double var = 0;
		for(Sequence sup : this.supperSequencesONE) {
			var += (sup.absoluteFreq - avg) * (sup.absoluteFreq - avg);
		}
		return new double[] {avg, var};
		
	}
	public List<Sequence> getSubSequencesONE() {
		return subSequencesONE;
	}
	public void setSubSequencesONE(List<Sequence> subSequencesONE) {
		this.subSequencesONE = subSequencesONE;
	}
	public void addsubSequences(Sequence subSequences) {
		if(this.subSequencesONE == null) this.subSequencesONE = new ArrayList<Sequence>();
		this.subSequencesONE.add(subSequences);
	}
	public double getAvgSequenceOfSubOne() {
		int total = 0;
		for(Sequence s : this.subSequencesONE) {
			total += s.absoluteFreq;
		}
		return total / (double) this.subSequencesONE.size();
		
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
//		if(this.tokens == null) {
//			System.out.println("ERROR");
//		}
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
//		if(this.tokens == null) {
//			System.out.println("ERROR");
//		}
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
//		if(this.tokens == null) {
//			System.out.println("ERROR");
//		}
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
		for(int i = 0; i < this.size(); i++) {
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
	
}
