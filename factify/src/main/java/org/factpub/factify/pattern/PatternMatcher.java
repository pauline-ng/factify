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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.factpub.factify.knowledge_model.C_Facts;
import org.factpub.factify.nlp.Sequence;
import org.factpub.factify.utility.Debug;
import org.factpub.factify.utility.Span;
import org.factpub.factify.utility.Utility;
import org.factpub.factify.utility.Debug.DEBUG_CONFIG;

/**
 * <pre>
 * Pattern Matcher for matching Units, TextToNumbers, Acronyms, P-values, Frequent NGrams
 * </pre>
 * 
 *<pre>
 *NOT SUPPORTED 
 *1. Equation: N = 3
 *2. 4 out of 6; 4 of/in the 6; 4 of/in 6 
 *3. 3 to 5; 3' to 5'
 *4. Ma et al. as author names are not identified.
 *</pre>
 *
 */

public class PatternMatcher {

	public static final String reg_rational =  "(?:(?i)(?:[+-]?)(?:(?=[.]?[0123456789])(?:[0123456789]*)(?:(?:[.])(?:[0123456789]{0,}))?)(?:(?:[E])(?:(?:[+-]?)(?:[0123456789]+))|))";
	public static final String reg_pvalue = "\\(?\\s*(p|P)\\s*(=|>|<|(>\\s*=)|(<\\s*=)\\s*)\\s*"+reg_rational + "\\s*\\)?";

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
	 * For each pattern in patterns, find matches based on stem
	 * 
	 * <pre>
	 * This method works as a general interface for matchers such as {@link PreBuiltWordListMatcher}, {@link PatternMatcher}, since every rule is represented as a {@link org.factpub.factify.nlp.Sequence Sequence}.
	 * For example, "a lot" in a {@link PreBuiltWordListMatcher} is represented as a {@link org.factpub.factify.nlp.Sequence Sequence}.
	 * </pre>
	 * @param senten Input sentence as a Sequence
	 * @param patterns Sequences to be matched against
	 * @return
	 */
	public  List<Span> findMatches(Sequence senten, HashSet<Sequence> patterns ) {
		HashSet<Span>	results = new HashSet<Span>();//[)
		HashSet<Integer> freSeqLen = new HashSet<Integer>();
		for(Sequence s : patterns) freSeqLen.add(s.getWordCount());
		ArrayList<Integer> enty_indices = new ArrayList<Integer>(); //[)
		//match from longest to shortest
		HashSet<Integer> matchedToken = new HashSet<Integer>();
		int k = senten.getWordCount();//length of pattern to be matched
		while(k > 0) {
			if(!freSeqLen.contains(k)) {k--; continue;}
			boolean coarseMatch = false;
			for(Sequence s : patterns) {
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
				if(foundOne && patterns.contains(senten.getSubsequence(b, b+k))) {//find one and it's matched to a freq pattern
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
	 * For each senten, check if it contains the string in patterns.
	 * @param senten Input sentence as a Sequence
	 * @param patterns Each Sequence has one token only
	 * @return
	 */
	public  List<Span> findContainingMatches(Sequence senten, HashSet<Sequence> patterns ) {
		HashSet<Span>	results = new HashSet<Span>();//[)
		for(int i = 0; i < senten.getWordCount(); i++) {
			String word = senten.getWord(i);
			boolean contains = false;
			for(Sequence s : patterns) {
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
	
	
	
}
