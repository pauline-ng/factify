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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import knowledge_model.C_Facts;
import utility.Debug;
import utility.Span;
import utility.utility;
import utility.Debug.DEBUG_CONFIG;
import nlp.Sequence;

/**
 * Patterns:
 *Each pattern is extracted independently. At the end, we will resolve conflicts in resolveSpans();
 * 1. numbers
 * a. rational numbers: subset of reg_equation
 *     reg_rational = "(?:(?i)(?:[+-]?)(?:(?=[.]?[0123456789])(?:[0123456789]*)(?:(?:[.])(?:[0123456789]{0,}))?)(?:(?:[E])(?:(?:[+-]?)(?:[0123456789]+))|))";
 *     one token
 * b. operators with numbers e.g. 10%, 1/10 (be careful with empty string) // NOT IN USE
 *     reg_equation = reg_rational?\\s*operators?\\s*reg_rational? :i.e. super set of reg_rational
 *     multiple tokens
 * 2. textToNum (e.g. two, three)
 *    find text that express numbers and then textToNum.parse()
 *     multiple tokens
 * 3. p < 0.00 i.e. p value
 *    reg_pvalue = "\\(?\\s*p\\s*(=|>|<|(>\\s*=)|(<\\s*=)\\s*)\\s*"+reg_rational + "\\s*\\)?"
 *    multiple tokens
 * 4. comparative than /superlative e.g. less than 0.3
 *    single token: POS tag (JJR, RBR) + keyword "than"; POS tag (JJS, RBS) 
 * 5. units (e.g. kg, g/ml)
 *    unitsRecognizer.isUnit();
 *    two tokens (current and previous token)
 * 7. acronyms 
 * 8. ngrams + nouns -- for result section
 * 9. signal words -- for result section: significant, increase, decrease etc.
 * 10. nouns -- for methods section
 * 11. signal words -- for method section: not yet
 * 12. signal words in common: e.g. not, no, rare, seldom etc.
 * 
 * 13. 4 out of 6; 4 of/in the 6; 4 of/in 6
 * 	reg_rational + "\\s((out of)|(of)|(in)|(of the)|(in the))\\s" + reg_rational;
 * 
 * 14. 3 to 5; 3' to 5' //NOT IN USE
 * --problem: 3' to 5': 2 to 3; 
 * 
 * 
 *ATTENTION:
 *The NLP tokenizer outputs 3 tokens for "N=9", i.e. "N", "=", "9". This has not been captured by our approach. We simply captured = as operators
 *1. Ma et al. as author names are not identified.
 *2.
 * 
 *
 */

public class PatternMatcher {

	public static final String reg_rational =  "(?:(?i)(?:[+-]?)(?:(?=[.]?[0123456789])(?:[0123456789]*)(?:(?:[.])(?:[0123456789]{0,}))?)(?:(?:[E])(?:(?:[+-]?)(?:[0123456789]+))|))";
	public static final String reg_pvalue = "\\(?\\s*(p|P)\\s*(=|>|<|(>\\s*=)|(<\\s*=)\\s*)\\s*"+reg_rational + "\\s*\\)?";
	 //4 out of 6; 4 of/in the 6; 4 of/in 6
//	public static final String reg_outof =	reg_rational + "\\s((out of)|(of)|(in)|(of the)|(in the))\\s" + reg_rational;
	
	public static String reg_equation; //contains epsilon, be careful
	//http://www.utf8-chartable.de/unicode-utf8-table.pl?utf8=dec //TODO
	private final static String[] operators = {"\\+", "-", "/", "%", "\\*", ">", "<","=",
		Character.toString((char )181),//\mu
		Character.toString((char )'\u2215'),//division sign
		Character.toString((char) 176),//design sign
		Character.toString((char) 177), //plus minus
		Character.toString((char) '\u2228'),//logical or
		Character.toString((char) '\u2227'),//logical and
		Character.toString((char) '\u2229'),//intersection
		Character.toString((char) '\u222A'),//union
		Character.toString((char) '\u2264'),//<=
		Character.toString((char) '\u2265'),//>=
		Character.toString((char) '\u2013'),//end dash; \u002D is minus sign

		};
	
	public  void formReg_Equation() {
		if(reg_equation != null) return;
		reg_equation = reg_rational;
		for(int i = 0; i < operators.length; i++) {
			String c = operators[i];
			if(i == 0) reg_equation = "(" + reg_equation + ")?\\s*(" + c;
			else
				reg_equation = reg_equation + "|" + c;
			if( i == operators.length -1) reg_equation = reg_equation + ")?\\s*(" + reg_rational + ")?";
		}
		Debug.println(reg_equation,DEBUG_CONFIG.debug_pattern);
	}
	public  boolean matchReg_Equation(String s) {
		formReg_Equation();
		boolean result = false;
		Pattern pattern = Pattern.compile(reg_equation);
		Matcher matcher = pattern.matcher(s);
		while (matcher.find()) {
			String matchString = matcher.group();
			if(!matchString.trim().equals("")) result = true;
		}
		return result;
	}

	private  List<Span> extractReg_Rational(Sequence senten) {
		HashSet<Span>	results = new HashSet<Span>();//[)
		Pattern pattern = Pattern.compile(reg_rational);
		for(int i = 0; i < senten.getWordCount(); i++) {
			String word = senten.getWord(i);
			Matcher matcher = pattern.matcher(word);//word has been trimmed
			if (matcher.find()) {
					results.add(new Span(senten.getSpanOfWord(i).getStart(), senten.getSpanOfWord(i).getEnd()));
			}
		}
		List<Span> results_ = new ArrayList<Span>(); results_.addAll(results);
		return results_;
	}
	
	public  List<Span> extractP_Value(Sequence senten) {
		HashSet<Span>	results = new HashSet<Span>();//[)
		Pattern pattern = Pattern.compile(reg_pvalue);
		Matcher matcher = pattern.matcher(senten.getSourceString());
		while (matcher.find()) {
			String matchString = matcher.group();
			int trimB = 0; int trimE = 0;
			for(trimB = 0; trimB < matchString.length(); trimB++) {
				if(matchString.charAt(trimB) != ' ') break;
			}
			for(trimE = matchString.length() -1; trimE > -1; trimE--) {
				if(matchString.charAt(trimE) != ' ') break;
			}
			trimE++; //[trimB, trimE) of matchString is the trimmed string
			results.add(new Span(matcher.start() + trimB, matcher.start() + trimE));
		}
		List<Span> results_ = new ArrayList<Span>(); results_.addAll(results);
		return results_;
	}
	
	public  List<Span> extractTextToNum(Sequence senten) {//need to do it by token
		HashSet<Span>	results = new HashSet<Span>();//[)
		TextToNum textToNum = new TextToNum();
		String curInteger = "";
		int integerRelativeOrder_start = -1;
		for(int i = 0; i < senten.getWordCount(); i++) {
			String word = senten.getWord(i);//stem
			String tag = senten.getPOSTagOfWord(i);
			//check if it is rational
			Pattern pattern = Pattern.compile(reg_rational);
			Matcher matcher = pattern.matcher(word);
			boolean isRational = false;
			while (matcher.find()) {//because pos tag "CD" may be for numbers, e.g. 0.1
				isRational = true;
			}
			//check the facts in the stack
			boolean found = false;
			if(isRational && integerRelativeOrder_start != -1) { //there were words that express numbers
				found = true;
			}
			if(!isRational) {
				if(tag.equals("CD") || word.equals("and") ) {//indentify words expressing numbers
					if(integerRelativeOrder_start == -1) {
						if(!tag.equals("and")) {// the first word cannot be "and"
							integerRelativeOrder_start = i;
							curInteger += " " +  word;
						}
					}else {
						curInteger += " " +  word;
					}
				}else {//if there are words that express numbers, this is the end of the words
					if(integerRelativeOrder_start != -1) found = true;
				}
			}
			if(found) {
				//found a word sequence expressing numbers
				curInteger = curInteger.trim(); 
				//last word cannot be "and"
				if(curInteger.endsWith("and")) curInteger = curInteger.length() == 3 ? null : curInteger.substring(0, curInteger.length() - 3);
				if(curInteger !=null && (!curInteger.trim().equals(""))) {//valid word sequence expressing numbers
					Long value = textToNum.parse(curInteger);
					if(value != null) {//if our program is not able to deal with it, leave it.
						results.add(new Span(senten.getSpanOfWord(integerRelativeOrder_start).getStart(), senten.getSpanOfWord(i - 1).getEnd()));
					}else {
						Debug.println("WARNING: Failed to convert " + curInteger + " to integer",DEBUG_CONFIG.debug_pattern);
					}
				}
				curInteger = "";
				integerRelativeOrder_start = -1;
			}
		}
		List<Span> results_ = new ArrayList<Span>(); results_.addAll(results);
		return results_;
	}
	
	public  List<Span> extractAcronyms(Sequence senten, Map<String, Sequence> acronyms) {//need to do it by token
		HashSet<Span>	results = new HashSet<Span>();//[)
		HashSet<Sequence> longforms = new HashSet<Sequence>();
		longforms.addAll(acronyms.values());
		for(int i = 0; i < senten.getWordCount(); i++) {
			if(acronyms.keySet().contains(senten.getWord(i))) results.add(new Span(senten.getSpanOfWord(i).getStart(), senten.getSpanOfWord(i).getEnd()));
			results.addAll(findMatches(senten, longforms));
		}
		
		List<Span> results_ = new ArrayList<Span>(); results_.addAll(results);
		return results_;
	}

	public  List<Span> extractUnits(Sequence senten) {
		HashSet<Span>	results = new HashSet<Span>();//[)
		UnitsRecognizer unitsRecognizer = new UnitsRecognizer();
		for(int i = 1; i < senten.getWordCount(); i++) {
			boolean followingNumber = false;
			if(extractReg_Rational(senten.getSubsequence(i -1, i)).size() > 0) followingNumber = true;
			if(unitsRecognizer.isUnit(senten.getWord(i), followingNumber)) {
				results.add(new Span(senten.getSpanOfWord(i).getStart(), senten.getSpanOfWord(i).getEnd()));
			}
		}
		List<Span> results_ = new ArrayList<Span>(); results_.addAll(results);
		return results_;
	}
	public  List<Span> extractNGrams(Sequence senten, HashSet<Sequence> freNGrams ) {
		return findMatches(senten, freNGrams);
	}
	
	/**
	 * find matches based on stem
	 * @param senten
	 * @param freNGrams
	 * @return
	 */
	public  List<Span> findMatches(Sequence senten, HashSet<Sequence> freNGrams ) {
		HashSet<Span>	results = new HashSet<Span>();//[)
		HashSet<Integer> freSeqLen = new HashSet<Integer>();
		for(Sequence s : freNGrams) freSeqLen.add(s.getWordCount());
		ArrayList<Integer> enty_indices = new ArrayList<Integer>(); //[)
		//match from longest to shortest
		HashSet<Integer> matchedToken = new HashSet<Integer>();
		int k = senten.getWordCount();//length of pattern to be matched
		while(k > 0) {
			if(!freSeqLen.contains(k)) {k--; continue;}
			boolean coarseMatch = false;
			for(Sequence s : freNGrams) {
				if(s.getWordCount() == k && s.isSubsequenceOrSelfOf(senten)) coarseMatch = true;
				if(coarseMatch) break;
			}
			if(!coarseMatch) {k--; continue;}
			int b = 0; // start/beginning of the substring
			while(b < senten.getWordCount() - k + 1) {
				boolean foundOne = true;
				for(int m = b; m < b + k; m++) if(matchedToken.contains(m)) {//matched token in the middle, start from next token
					foundOne = false; break;
				}
				if(foundOne && freNGrams.contains(senten.getSubsequence(b, b+k))) {//find one and it's matched to a freq pattern
					enty_indices.add(b); enty_indices.add(b+k); 
					for(int j = b; j < b+k; j++) matchedToken.add(j);
					b = b + k; 
				}else b++;
			}
			k--;
		}
		//now sort indices
		Collections.sort(enty_indices);
		for(int i = 0; i < enty_indices.size();) {
			int start = enty_indices.get(i);
			int end = enty_indices.get(i + 1);
			results.add(new Span(senten.getSpanOfWord(start).getStart(), senten.getSpanOfWord(end - 1).getEnd()));
			i = i + 2;
		}
		List<Span> results_ = new ArrayList<Span>(); results_.addAll(results);
		return results_;
	}
	/**
	 * find matches based on stem
	 * @param senten
	 * @param freNGrams: one token only
	 * @return
	 */
	public  List<Span> findContainingMatches(Sequence senten, HashSet<Sequence> freNGrams ) {
		HashSet<Span>	results = new HashSet<Span>();//[)
		for(int i = 0; i < senten.getWordCount(); i++) {
			String word = senten.getWord(i);
			boolean contains = false;
			for(Sequence s : freNGrams) {
				if(word.contains(s.getSourceString())) {
					contains = true; break;
				}
			}
			if(contains) results.add(new Span(senten.getSpanOfWord(i).getStart(), senten.getSpanOfWord(i).getEnd()));
		}
		List<Span> results_ = new ArrayList<Span>(); results_.addAll(results);
		return results_;
	}
	public  List<Span> extractParenthesis(Sequence senten) {
		HashSet<Span>	results = new HashSet<Span>();//[)
		for(int i = 0; i < senten.getWordCount(); i++) {
			String word = senten.getWord(i).trim();
			if(word.equals("(")||word.equals(")")||word.equals("[") || word.equals("]")) {
				results.add(new Span(senten.getSpanOfWord(i).getStart(), senten.getSpanOfWord(i).getEnd()));
			}
		}
		List<Span> results_ = new ArrayList<Span>(); results_.addAll(results);
		return results_;
	}
	
	
	public  void test() {
//		utility util = new utility();
//		String para = util.readFromFile("test/PMC1513515/Methods_section.txt");
//		String para = "The specificity of CRISPR/Cas9 is largely dictated by PAM and the 17–20 nt sequence at the 5' end of gRNAs (Cong et al., 2013; Hsu et al., 2013; Mali et al., 2013a; Mali et al., 2013c; Pattanayak et al., 2013; Wu et al., 2014a). ";
//		String para = "Additional significant SNPs were identified in GABRA4 as well, rs17599165 (p=0.0015) and rs1759 9416 (p=0.0040)";
//		String para = "it failed.";
//		String para = " Two candidate off-target sites within exons were found, where lower concentration of the Cas9 mRNA and gRNA had been used (sample A and C, Fig. 3B), and further confirmed through the T7E1 assay (Fig. S5).";
//		String para = "CRISPR/Cas9 targeting of the ß-globin locus was previously reported to have substantially high off-target activity in cultured human cells (Cradick et al., 2013). ";
//		String para = "Widespread seasonal gene expression in the immune system. Strikingly, we found ~23% of the genome (5,136 unique genes out of 22,822 genes tested) to show significant seasonal differences in expression in the BABYDIET data set (Fig. 2a and Supplementary Table 3).";
//		String para = "Because the sequences of HBB and HBD are very similar, HBD may also be used as a template to repair HBB";
//		String para = "he faint";
//		String para = "First, although mixed-model analysis is effective in correcting for many forms of confounding, performing careful data quality control remains critical to avoid false positives.";
//		String para = "We further verified that type I error was properly controlled (Online Methods and Supplementary Table 5).";
//		String para = "The mean χ2 statistics for GCTA-LOCO and BOLT-LMM-inf at SNPs of standardized effect were essentially identical and slightly exceeded the statistics from PCA, consistent with theory12.";
//		String para = "Interestingly, early-stage inflammation in rheumatoid arthritis (a disease treated with anti-IL-6 receptor reagents46) has been shown to either resolve or progress to erosive disease, and a predictor of this outcome is the season when disease symptoms first present47.";
//		String para = "Said et al showed25 that Ag1 is both bacteriostatic and bactericidal in growth mediumat comparable silver concentrations.";
//		String para = "Financial incentives have been shown to promote a variety of health behaviors. For example, in a randomized, clinical trial involving 878 General Electric employees, a bundle of incentives worth $750 for smoking cessation nearly tripled quit rates, from 5.0% to 14.7%,8 and led to a program adapted by General Electric for its U.S. employees.9 Although incentive programs are increasingly used by governments, employers, and insurers to motivate changes in health behavior,10,11 their design is usually based on the traditional economic assumption that the size of the incentive determines its effectiveness.";
String para = "In contrast, behavioral economic theory suggests that incentives of similar size may have very different effects depending on how they are designed.12";
		//		Debug.println("\\u" + Integer.toHexString('-' | 0x10000).substring(1));
//		for(int i = 0; i < para.length(); i++) Debug.println(para.charAt(i) +  "\t" + "\\u" + Integer.toHexString(para.charAt(i) | 0x10000).substring(1) + "\t" + Character.getType(para.charAt(i)));
//		System.exit(0);
		Debug.println(para,DEBUG_CONFIG.debug_pattern);
//		StanfordNLPLight nlp = new StanfordNLPLight("tokenize, ssplit, pos, lemma");
//		ngrams ngram = new ngrams();
//		List<Sequence> freSeq = ngram.getFreqSequences(sentences);
//		freSeq_ = new HashSet<Sequence>(); freSeq_.addAll(freSeq);
		
//		C_Facts cFact = parsePara(nlp.textToSequence(para, true), null, null, new Span(-1,-1));
//		S_Facts sfact = new S_Facts(cFact);
//		sfact.mergeFacts();
//		sfact.printFacts();
//		sfact.writeFacts("test/PMC1513515/Methods_section_ngram.fact");
	}
	
	/**
	 * merge overlapping spans
	 * Merge sort
	 * @param all
	 * @return
	 */
	public  List<Span> resolveSpans(List<List<Span>> all) {
		List<Span> all_ = new ArrayList<Span>();
		for(List<Span> spans : all) all_.addAll(spans);
        Collections.sort(all_, new Comparator<Span>(){public int compare(Span i, Span j){ 
            if(i.getStart() == j.getStart()) return i.getEnd()-j.getEnd();
            else return i.getStart()-j.getStart();
        } });
        List<Span> ret = new ArrayList<Span>();
        if(all_.size() < 1) return ret;
        ret.add(new Span(all_.get(0).getStart(), all_.get(0).getEnd()));
        for(int i = 1; i < all_.size(); i++) {
        	Span last = ret.get(ret.size()-1);
        	Span cur = all_.get(i);
            if(last.getEnd() > cur.getStart()) last.setEnd(Math.max(last.getEnd(), cur.getEnd()));
            else ret.add(new Span(cur.getStart(), cur.getEnd()));
        }
        return ret;
	}
	public List<Span> merge(List<Span> intervals) {
        if(intervals == null || intervals.size() == 0) return intervals;
        
        return merge(intervals, 0, intervals.size());
    }
    public List<Span> merge(List<Span> intervals, int i, int j) {//[i,j);
        if(j - i < 2) {
            List<Span> tmp = new ArrayList<Span>();
            tmp.add(intervals.get(i));
            return tmp;
        }
        int mid = i + (j-i)/2;//higher mid
        List<Span> l = merge(intervals, i, mid);
        List<Span> r = merge(intervals, mid, j);
        List<Span> result = new ArrayList<Span>();
        int k = 0;
        int p = 0;
        while(k < l.size() && p < r.size()) {
        	Span left = l.get(k);
        	Span right = r.get(p);
            if(left.getEnd() <= right.getStart()) {
                result.add(left); k++;
            }else if(right.getEnd() <= left.getStart()) {
                result.add(right); p++;
            }else {
                if(left.getStart() <= right.getStart()) {
                	Span newleft = new Span(left.getStart(), left.getEnd());
                	newleft.setEnd(Math.max(newleft.getEnd(), right.getEnd()));
                    //make sure l is disjoint
                    while(k < l.size() -1 && l.get(k+1).getStart()< newleft.getEnd()) newleft.setEnd(Math.max(newleft.getEnd(), l.get(++k).getEnd()));
                    l.set(k, newleft);
                    p++;
                }else {
                	Span newRight = new Span(right.getStart(), right.getEnd());
                	newRight.setEnd(Math.max(left.getEnd(), newRight.getEnd()));
                    while(p < r.size()-1 && r.get(p+1).getStart()< newRight.getEnd()) {
                    	newRight.setEnd(Math.max(newRight.getEnd(), r.get(++p).getEnd()));
                    }
                    r.set(p, newRight);
                    k++;
                }
            }
        }
        while(k < l.size()) {
        	Span cur = l.get(k);
            if(result.size() == 0) {
                result.add(cur);
                k++;
                continue;
            }
            Span pre = result.get(result.size() - 1);
            if(pre.getEnd() <= cur.getStart()) {
                result.add(cur);
            }else {
                pre.setEnd(Math.max(cur.getEnd(), pre.getEnd()));
            }
            k++;
        }
        while(p < r.size()) {
        	Span cur = r.get(p);
            if(result.size() == 0) {
                result.add(cur);
                p++;
                continue;
            }
            Span pre = result.get(result.size() - 1);
            if(pre.getEnd() <= cur.getStart()) {
                result.add(cur);
            }else {
                pre.setEnd(Math.max(cur.getEnd(), pre.getEnd()));
            }
            p++;
        }
        
        return result;
    }
	
	/**
	 * Any token that crosses the span of all would be counted in.
	 * TODO: could be optimized using merge join instead of nested-loop join
	 * 
	 * @param all: the output of resolveSpans()
	 * @param senten: the source sentence
	 * @return
	 */
	public  C_Facts formFacts(List<List<Span>> all_facts, List<Sequence> sentens, List<String> details, Span pageRange) {
		if(sentens.size() == 0) return null; 
		C_Facts cfacts = new C_Facts(pageRange.getStart(), pageRange.getEnd());
		for(int senIndex = 0; senIndex < sentens.size(); senIndex++) {
			List<Span> facts_per_sen = all_facts.get(senIndex);
			Sequence senten = sentens.get(senIndex);
			utility.sortByStart(facts_per_sen);

			// Any token that crosses the span of all would be counted in.
			HashSet<Integer> crossToken = new HashSet<Integer>();//the relative order of tokens that the span cross
			for(int i = 0; i < facts_per_sen.size(); i++) {
				Span cur = facts_per_sen.get(i);
				for(int j = 0; j < senten.getWordCount(); j++) {
					Span token = senten.getSpanOfWord(j);
					if(token.intersects(cur)) crossToken.add(j);
				}
			}
			List<Integer> crossToken_ = new ArrayList<Integer>(); crossToken_.addAll(crossToken);
			Collections.sort(crossToken_);
			ArrayList<String> facts = new ArrayList<>();
			ArrayList<Span> relativeOrders = new ArrayList<Span>();
			LinkedHashMap<Integer, Integer> spans = new LinkedHashMap<>();
			for(int i = 0; i < crossToken_.size(); i++) {
				int relativeOrder = crossToken_.get(i);
				facts.add(senten.getWord(relativeOrder));
				relativeOrders.add(new Span(relativeOrder,relativeOrder));
				spans.put(senten.getSpanOfWord(relativeOrder).getStart(), senten.getSpanOfWord(relativeOrder).getEnd() - 1);
			}
			cfacts.addFact(facts, senIndex, relativeOrders, spans, details.get(senIndex));
			cfacts.addSentence(sentens.get(senIndex).getSourceString());
		}
		return cfacts;
	}
	
}
