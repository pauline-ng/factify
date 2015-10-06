package pattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nlp.StanfordNLPLight;
import knowledge_model.C_Facts;
import knowledge_model.S_Facts;
import utility.Debug;
//import edu.stanford.nlp.ling.CoreAnnotations;
//import edu.stanford.nlp.pipeline.Annotation;
//import edu.stanford.nlp.util.CoreMap;
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
 * @author huangxc
 *
 */

public class PatternMatcher {
//	static String NLP_INPUT_PROPERTIES = "NLP_INPUT/properties.xml";
//	static String NLP_INPUT_EN_PARSER_CHUNKING = "NLP_INPUT/en-parser-chunking.bin";
//	static String NLP_INPUT_EN_SENT = "NLP_INPUT/en-sent.bin";
//	static String NLP_INPUT_EN_POS_MAXENT = "NLP_INPUT/en-pos-maxent.bin";
//	static String NLP_INPUT_EN_TOKEN = "NLP_INPUT/en-token.bin";
	//	private static  openNLP nlp;
//	static StanfordNLP nlp;
//	private static wordnet wn;

	public static final String reg_rational =  "(?:(?i)(?:[+-]?)(?:(?=[.]?[0123456789])(?:[0123456789]*)(?:(?:[.])(?:[0123456789]{0,}))?)(?:(?:[E])(?:(?:[+-]?)(?:[0123456789]+))|))";
	public static final String reg_pvalue = "\\(?\\s*(p|P)\\s*(=|>|<|(>\\s*=)|(<\\s*=)\\s*)\\s*"+reg_rational + "\\s*\\)?";
	 //4 out of 6; 4 of/in the 6; 4 of/in 6
//	public static final String reg_outof =	reg_rational + "\\s((out of)|(of)|(in)|(of the)|(in the))\\s" + reg_rational;
	
	private RelationKeywords rk = new RelationKeywords();
	private SignalKeywords sk = new SignalKeywords();
	private UncertainKeywords uk = new UncertainKeywords();
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
	private final static String[] puncs = {
		",",
		";",
	};
//	private final static char[] operators = {'+'};
	
	
	public static void main(String[] args) {
		char c = '\u2265';
//		Debug.println(Character.toString((char )'\u2265'));
//		System.err.print("test");
//		test();
		System.exit(0);
		PatternMatcher pat = new PatternMatcher();
		{
			Pattern pattern = Pattern.compile(pat.reg_rational);
			Matcher matcher = pattern.matcher("I have 100.");
			while (matcher.find()) {
				Debug.println("rational:" + matcher.group(),DEBUG_CONFIG.debug_pattern);
			}
		}
		{
			Pattern pattern = Pattern.compile(pat.reg_pvalue);
			Matcher matcher = pattern.matcher(" p =0.0053 ");
			while (matcher.find()) {
				Debug.println("pavalue:" + matcher.group(),DEBUG_CONFIG.debug_pattern);
			}
		}
		{
			pat.formReg_Equation();
			//be careful "1/9 0.1" will become " 1" "/9" and " 0.1" through matcher.find();
			Pattern pattern = Pattern.compile(pat.reg_equation);
			Matcher matcher = pattern.matcher("aa 1/9 0.1 ");
			while (matcher.find()) {
				String matchString = matcher.group();
				if(!matchString.trim().equals("")) {
				Debug.println("equation:" + matchString,DEBUG_CONFIG.debug_pattern);
				int trimB = 0; int trimE = 0;
				for(trimB = 0; trimB < matchString.length(); trimB++) {
					if(matchString.charAt(trimB) != ' ') break;
				}
				for(trimE = matchString.length() -1; trimE > -1; trimE--) {
					if(matchString.charAt(trimE) != ' ') break;
				}
				trimE++; //[trimB, trimE) of matchString is the trimed string
				Debug.println(new Span(matcher.start() + trimB, matcher.start() + trimE),DEBUG_CONFIG.debug_pattern);
				}
			}
			Debug.println("matches equation?" + pat.matchReg_Equation("ab"),DEBUG_CONFIG.debug_pattern);
		}
		{
			//negation: ^(?!.*DontMatchThis).*$ //not helpful
			//and: (?:match this expression)(?:match this too)(?:oh, and this)//not helpful
			Pattern pattern = Pattern.compile(reg_equation);//(?:a*)
			Matcher matcher = pattern.matcher("10%");
			while (matcher.find()) {
				Debug.println("test:" + matcher.group(),DEBUG_CONFIG.debug_pattern);
			}
		}
		{
//			List<Sequence> sentences = nlp.textToSequence("It has been improved by 10% and resulted in a good performance", true);
//			for(Sequence s : sentences) {
//				Debug.println(s.sourceString);
//			List<Span> rels = extractRelations(s);
//			for(Span rel : rels) Debug.println("rel:" + rel.getCoveredText(s.sourceString));
//			}
			
		}
		{
			TextToNum textToNum = new TextToNum();
			Debug.println("text to num: " + textToNum.parse("less than 0.3"),DEBUG_CONFIG.debug_pattern);
		}
	}
	
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
//	public static boolean TextToEquation(Sequence s) {
//		return false;
//	}
	
	public  void extract(Sequence senten) {
		List<Span>	fact_spans = new ArrayList<Span>();
		
	}
	/**
	 * NOT IN USE
	 * find the equation/rational in a sentence (represented by sequence)
	 * the result is a list of non-overlapping spans of characters in senten (trimmed)
	 * Note: performing over tokens instead of strings, because of sentences like "I have 100."
	 * @param senten
	 * @return
	 */
	private  List<Span> extractReg_Equa(Sequence senten) {
		HashSet<Span>	results = new HashSet<Span>();//[)
		formReg_Equation();
		Pattern pattern = Pattern.compile(reg_equation);
		Matcher matcher = pattern.matcher(senten.sourceString);
		while (matcher.find()) {
			String matchString = matcher.group();
			if(!matchString.trim().equals("")) {
//				Debug.println("equation:" + matchString);
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
		}
		List<Span> results_ = new ArrayList<Span>(); results_.addAll(results);
		return results_;
	}
	
	private  List<Span> extractReg_Rational(Sequence senten) {
		HashSet<Span>	results = new HashSet<Span>();//[)
		Pattern pattern = Pattern.compile(reg_rational);
		for(int i = 0; i < senten.size(); i++) {
			String word = senten.words.get(i);
			Matcher matcher = pattern.matcher(word);//word has been trimmed
			if (matcher.find()) {
					results.add(new Span(senten.spans.get(i).getStart(), senten.spans.get(i).getEnd()));
			}
		}
		List<Span> results_ = new ArrayList<Span>(); results_.addAll(results);
		return results_;
	}
	
	private  List<Span> extractNouns(Sequence senten) {
		HashSet<Span>	results = new HashSet<Span>();//[)
		for(int i = 0; i < senten.size(); i++) {
			String tag = senten.POSTags.get(i);
			if (nlp.StanfordNLPLight.isNoun(tag)) {
					results.add(new Span(senten.spans.get(i).getStart(), senten.spans.get(i).getEnd()));
			}
		}
		List<Span> results_ = new ArrayList<Span>(); results_.addAll(results);
		return results_;
	}
	
	private  List<Span> extractP_Value(Sequence senten) {
		HashSet<Span>	results = new HashSet<Span>();//[)
		Pattern pattern = Pattern.compile(reg_pvalue);
		Matcher matcher = pattern.matcher(senten.sourceString);
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
	
	private  List<Span> extractTextToNum(Sequence senten) {//need to do it by token
		HashSet<Span>	results = new HashSet<Span>();//[)
		TextToNum textToNum = new TextToNum();
		String curInteger = "";
		int integerRelativeOrder_start = -1;
		for(int i = 0; i < senten.POSTags.size(); i++) {
			String word = senten.words.get(i);//stem
			String tag = senten.POSTags.get(i);
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
						results.add(new Span(senten.spans.get(integerRelativeOrder_start).getStart(), senten.spans.get(i - 1).getEnd()));
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
	
	/**
	 * Currently support one-token comparatives
	 * @param senten
	 * @return
	 */
	private  List<Span> extractComparatives(Sequence senten) {//need to do it by token
		HashSet<Span>	results = new HashSet<Span>();//[)
		String preTag = "";
		ComparativeKeywords ck = new ComparativeKeywords();
		for(int i = 0; i < senten.size(); i++) {
			String tag = senten.POSTags.get(i);
			if(ck.containPOS(tag)||
					ck.contain(senten.stems.get(i))) results.add(new Span(senten.spans.get(i).getStart(), senten.spans.get(i).getEnd()));
			if((preTag.equals("RBR") || preTag.equals("JJR")) && senten.words.get(i).equals("than")) {//e.g. less than
				results.add(new Span(senten.spans.get(i).getStart(), senten.spans.get(i).getEnd()));
			}
			preTag = tag;
		}
		
		List<Span> results_ = new ArrayList<Span>(); results_.addAll(results);
		return results_;
	}
	
	private  List<Span> extractAcronyms(Sequence senten, Map<String, Sequence> acronyms) {//need to do it by token
		HashSet<Span>	results = new HashSet<Span>();//[)
		HashSet<Sequence> longforms = new HashSet<Sequence>();
		longforms.addAll(acronyms.values());
		for(int i = 0; i < senten.size(); i++) {
			if(acronyms.keySet().contains(senten.words.get(i))) results.add(new Span(senten.spans.get(i).getStart(), senten.spans.get(i).getEnd()));
			results.addAll(findMatches(senten, longforms));
		}
		
		List<Span> results_ = new ArrayList<Span>(); results_.addAll(results);
		return results_;
	}
	/**
	 * A relation keyword may contain multiple tokens e.g. result in / result from
	 * @param senten
	 * @return
	 */
	private  List<Span> extractRelations(Sequence senten) {
		HashSet<String> keywords_ = rk.getAllKeywords();
		HashSet<Sequence> keywords = new HashSet<Sequence>();
 		for(String s : keywords_) {
			StringTokenizer st = new StringTokenizer(s, " ");
			List<String> ele = new ArrayList<String>();
			while(st.hasMoreTokens()) ele.add(st.nextToken());
			keywords.add(new Sequence(null, ele, null, null, s));//sequence equality is based on stems
		}
		return findMatches(senten, keywords);
	}
	/**
	 * A signal keyword may contain multiple tokens e.g. a few
	 * @param senten
	 * @return
	 */
	private  List<Span> extractSingalWords(Sequence senten) {
		HashSet<String> keywords_ = sk.getAllKeywords();
		HashSet<Sequence> keywords = new HashSet<Sequence>();
 		for(String s : keywords_) {
			StringTokenizer st = new StringTokenizer(s, " ");
			List<String> ele = new ArrayList<String>();
			while(st.hasMoreTokens()) ele.add(st.nextToken());
			keywords.add(new Sequence(null, ele, null, null, s));//sequence equality is based on stems
		}
		return findMatches(senten, keywords);
	}
	/**
	 * NOT IN USE
	 * A signal keyword may contain multiple tokens e.g. a few
	 * @param senten
	 * @return
	 */
	private  List<Span> extractUncertaintyWords(Sequence senten) {
		HashSet<String> keywords_ = uk.getAllKeywords();
		HashSet<Sequence> keywords = new HashSet<Sequence>();
 		for(String s : keywords_) {
			StringTokenizer st = new StringTokenizer(s, " ");
			List<String> ele = new ArrayList<String>();
			while(st.hasMoreTokens()) ele.add(st.nextToken());
			keywords.add(new Sequence(null, ele, null, null, s));//sequence equality is based on stems
		}
		return findMatches(senten, keywords);
	}
	private  List<Span> extractUnits(Sequence senten) {
		HashSet<Span>	results = new HashSet<Span>();//[)
		UnitsRecognizer unitsRecognizer = new UnitsRecognizer();
		for(int i = 1; i < senten.size(); i++) {
			boolean followingNumber = false;
			if(extractReg_Rational(senten.getSubsequence(i -1, i)).size() > 0) followingNumber = true;
			if(unitsRecognizer.isUnit(senten.words.get(i), followingNumber)) {
				results.add(new Span(senten.spans.get(i).getStart(), senten.spans.get(i).getEnd()));
			}
		}
		List<Span> results_ = new ArrayList<Span>(); results_.addAll(results);
		return results_;
	}
	private  List<Span> extractNGrams(Sequence senten, HashSet<Sequence> freNGrams ) {
		return findMatches(senten, freNGrams);
	}
	
	/**
	 * 4 out of 6; 3 to 4; twenty to fifty
	 * number to/out of number
	 * @param senten
	 * @return
	 */
//	private static List<Span> extractReg_OutOf(Sequence senten) {
//			HashSet<Span>	results = new HashSet<Span>();//[)
//			Pattern pattern = Pattern.compile(reg_outof);
//			Matcher matcher = pattern.matcher(senten.sourceString);
//			while (matcher.find()) {
//				String matchString = matcher.group();
//				int trimB = 0; int trimE = 0;
//				for(trimB = 0; trimB < matchString.length(); trimB++) {
//					if(matchString.charAt(trimB) != ' ') break;
//				}
//				for(trimE = matchString.length() -1; trimE > -1; trimE--) {
//					if(matchString.charAt(trimE) != ' ') break;
//				}
//				trimE++; //[trimB, trimE) of matchString is the trimmed string
//				results.add(new Span(matcher.start() + trimB, matcher.start() + trimE));
//			}
//			List<Span> results_ = new ArrayList<Span>(); results_.addAll(results);
//			return results_;
//		
//	}
	
	/**
	 * find matches based on stem
	 * @param senten
	 * @param freNGrams
	 * @return
	 */
	private  List<Span> findMatches(Sequence senten, HashSet<Sequence> freNGrams ) {
		HashSet<Span>	results = new HashSet<Span>();//[)
		HashSet<Integer> freSeqLen = new HashSet<Integer>();
		for(Sequence s : freNGrams) freSeqLen.add(s.size());
		ArrayList<Integer> enty_indices = new ArrayList<Integer>(); //[)
		//match from longest to shortest
		HashSet<Integer> matchedToken = new HashSet<Integer>();
		int k = senten.size();//length of pattern to be matched
		while(k > 0) {
			if(!freSeqLen.contains(k)) {k--; continue;}
			boolean coarseMatch = false;
			for(Sequence s : freNGrams) {
//				if(s.toString().contains("faint") && k == 1) {
//					Debug.println("debug");
//				}
				if(s.size() == k && s.isSubsequenceOrSelfOf(senten)) coarseMatch = true;
				if(coarseMatch) break;
			}
			if(!coarseMatch) {k--; continue;}
			int b = 0; // start/beginning of the substring
			while(b < senten.size() - k + 1) {
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
			results.add(new Span(senten.spans.get(start).getStart(), senten.spans.get(end - 1).getEnd()));
			i = i + 2;
		}
		List<Span> results_ = new ArrayList<Span>(); results_.addAll(results);
		return results_;
	}
	private  List<Span> extractParenthesis(Sequence senten) {
		HashSet<Span>	results = new HashSet<Span>();//[)
		for(int i = 0; i < senten.size(); i++) {
			String word = senten.words.get(i).trim();
			if(word.equals("(")||word.equals(")")||word.equals("[") || word.equals("]")) {
				results.add(new Span(senten.spans.get(i).getStart(), senten.spans.get(i).getEnd()));
			}
		}
		List<Span> results_ = new ArrayList<Span>(); results_.addAll(results);
		return results_;
	}
	
	
	private  List<Span> extractOperators(Sequence senten) {
//		//http://www.utf8-chartable.de/unicode-utf8-table.pl?utf8=dec 
		HashSet<String> operators = new HashSet<String>(); 
		for(String s : PatternMatcher.operators) operators.add(s);
		HashSet<Span>	results = new HashSet<Span>();//[)
		for(int i = 0; i < senten.size(); i++) {
			String word = senten.words.get(i).trim();
//			if(word.length() != 1) continue;
			boolean found = false;
			for(String s : operators) 
				if(word.contains(s)) {
					found = true; break;
				}
			
			if(found)
					results.add(new Span(senten.spans.get(i).getStart(), senten.spans.get(i).getEnd()));
		}
		List<Span> results_ = new ArrayList<Span>(); results_.addAll(results);
		return results_;
	}
	private  List<Span> extractPunc(Sequence senten) {

		HashSet<String> puncs = new HashSet<String>(); 
		for(String s : PatternMatcher.puncs) puncs.add(s);
		HashSet<Span>	results = new HashSet<Span>();//[)
		for(int i = 0; i < senten.size(); i++) {
			String word = senten.words.get(i).trim();
			if(word.length() != 1) continue;
			
			if(puncs.contains(word))
					results.add(new Span(senten.spans.get(i).getStart(), senten.spans.get(i).getEnd()));
		}
		List<Span> results_ = new ArrayList<Span>(); results_.addAll(results);
		return results_;
	}
	public  void test() {
		utility util = new utility();
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
		StanfordNLPLight nlp = new StanfordNLPLight("tokenize, ssplit, pos");
//		ngrams ngram = new ngrams();
		NGrams.nlp = nlp;
//		ngrams.wn = nlp.wn;
//		List<Sequence> freSeq = ngram.getFreqSequences(sentences);
//		freSeq_ = new HashSet<Sequence>(); freSeq_.addAll(freSeq);
		
		C_Facts cFact = parsePara(nlp.textToSequence(para, true), null, null);
		S_Facts sfact = new S_Facts(cFact);
		sfact.mergeFacts();
		sfact.printFacts();
//		sfact.writeFacts("test/PMC1513515/Methods_section_ngram.fact");
	}
	public  C_Facts parsePara(List<Sequence> sentences, HashSet<Sequence> freSeq_, Map<String, Sequence> acronyms) {
//		String para = "This bag costs 100. It is a million dollars. It's improved by 10 % with p <= 0.001.Mine is better than his.";
		utility util = new utility();
//		para = util.readFromFile("test/PMC1513515/Results_section.txt");
//		List<Sequence> sentences = nlp.textToSequence(para, true);
//		HashSet<Sequence> freSeq_ = null;
//		if(withNGrams) {
//			ngrams ngram = new ngrams();
//			ngrams.nlp = nlp;
//			ngrams.wn = nlp.wn;
//			List<Sequence> freSeq = ngram.getFreqSequences(sentences);
//			freSeq_ = new HashSet<Sequence>(); freSeq_.addAll(freSeq);
//		}
		List<List<Span>> allFacts = new ArrayList<List<Span>>();
		List<String> details = new ArrayList<String>();
		boolean printDetail = false;
		String detail = "";
		for(Sequence s : sentences) {
//			Debug.println(s.sourceString);
			List<List<Span>> all = new ArrayList<List<Span>>();
			{
				List<Span> rational = extractReg_Rational(s);
				if(printDetail)	for(Span span : rational) Debug.println("rational:" + span.getCoveredText(s.sourceString),DEBUG_CONFIG.debug_pattern);
				all.add(rational);
				for(Span span : rational) {
					detail += "Rule Rational: " + span.getCoveredText(s.sourceString) + "\r\n"; 
				}
			}
			{
//				List<Span> outOf = extractReg_OutOf(s);
//				if(printDetail)	for(Span span : outOf) Debug.println("Out of:" + span.getCoveredText(s.sourceString),DEBUG_CONFIG.debug_pattern);
//				all.add(outOf);
			}
			{
				List<Span> textToNum = extractTextToNum(s);
				if(printDetail)				for(Span span : textToNum) Debug.println("textToNum:" + span.getCoveredText(s.sourceString),DEBUG_CONFIG.debug_pattern);
				all.add(textToNum);
				for(Span span : textToNum) {
					detail += "Rule textToNum: " + span.getCoveredText(s.sourceString) + "\r\n"; 
				}
			}
			{
				List<Span> nouns = extractNouns(s);
				if(printDetail)				for(Span span : nouns) Debug.println("nouns:" + span.getCoveredText(s.sourceString),DEBUG_CONFIG.debug_pattern);
				all.add(nouns);
				for(Span span : nouns) {
					detail += "Rule Nouns: " + span.getCoveredText(s.sourceString) + "\r\n"; 
				}
			}
			{
				List<Span> pvalues = extractP_Value(s);
				if(printDetail)				for(Span span : pvalues) Debug.println("pvalues:" + span.getCoveredText(s.sourceString),DEBUG_CONFIG.debug_pattern);
				all.add(pvalues);
				for(Span span : pvalues) {
					detail += "Rule P Value: " + span.getCoveredText(s.sourceString) + "\r\n"; 
				}
			}
			{
				List<Span> com = extractComparatives(s);
				if(printDetail)				for(Span span : com) Debug.println("comparatives:" + span.getCoveredText(s.sourceString),DEBUG_CONFIG.debug_pattern);
				all.add(com);
				for(Span span : com) {
					detail += "Rule Comparatives: " + span.getCoveredText(s.sourceString) + "\r\n"; 
				}
			}
			{
//				List<Span> uncertain = extractUncertaintyWords(s);
//				if(printDetail)				for(Span span : uncertain) Debug.println("uncertainties:" + span.getCoveredText(s.sourceString));
//				all.add(uncertain);
			}
			{
				List<Span> units = extractUnits(s);
				if(printDetail) for(Span span : units) Debug.println("units:" + span.getCoveredText(s.sourceString),DEBUG_CONFIG.debug_pattern);
				all.add(units);
				for(Span span : units) {
					detail += "Rule Units: " + span.getCoveredText(s.sourceString) + "\r\n"; 
				}
			}
			if(freSeq_ != null){
				
				List<Span> ngrams_s = extractNGrams(s, freSeq_);
				if(printDetail)				for(Span span : ngrams_s) Debug.println("ngrams:" + span.getCoveredText(s.sourceString),DEBUG_CONFIG.debug_pattern);
				all.add(ngrams_s);
				for(Span span : ngrams_s) {
					detail += "Rule Freq NGrams: " + span.getCoveredText(s.sourceString) + "\r\n"; 
				}
			}
			{
				List<Span> rels = extractRelations(s);
				if(printDetail)				
					for(Span rel : rels) Debug.println("rel:" + rel.getCoveredText(s.sourceString),DEBUG_CONFIG.debug_pattern);
				all.add(rels);
				for(Span span : rels) {
					detail += "Rule Relations: " + span.getCoveredText(s.sourceString) + "\r\n"; 
				}
			}
			{
				List<Span> signals = extractSingalWords(s);
				if(printDetail)				
					for(Span signal : signals) Debug.println("signals:" + signal.getCoveredText(s.sourceString),DEBUG_CONFIG.debug_pattern);
				all.add(signals);
				for(Span span : signals) {
					detail += "Rule Signals: " + span.getCoveredText(s.sourceString) + "\r\n"; 
				}
			}
			if(acronyms!= null){
//				acronyms.add("subunit");
				 List<Span> acros = extractAcronyms(s, acronyms);
				if(printDetail)				for(Span acro : acros) Debug.println("acronyms:" + acro.getCoveredText(s.sourceString),DEBUG_CONFIG.debug_pattern);
				all.add(acros);
				for(Span span : acros) {
					detail += "Rule Acronyms: " + span.getCoveredText(s.sourceString) + "\r\n"; 
				}
			}
			{
				List<Span> parenthesis = extractParenthesis(s);
				if(printDetail)				for(Span p : parenthesis) Debug.println("parenthesis:" + p.getCoveredText(s.sourceString),DEBUG_CONFIG.debug_pattern);
				all.add(parenthesis);
				
			}
			{
				List<Span> operators = extractOperators(s);
				if(printDetail) for(Span o : operators) Debug.println("operators:" + o.getCoveredText(s.sourceString),DEBUG_CONFIG.debug_pattern);
				all.add(operators);
				for(Span span : operators) {
					detail += "Rule Operators: " + span.getCoveredText(s.sourceString) + "\r\n"; 
				}
			}
			{
				List<Span> puncs = extractPunc(s);
				if(printDetail) for(Span o : puncs) Debug.println("puncs:" + o.getCoveredText(s.sourceString),DEBUG_CONFIG.debug_pattern);
				all.add(puncs);
			}
//			for(List<Span> l : all) for(Span span : l) if(span.getCoveredText(s.sourceString).toString().contains("studies")) Debug.print("here" + all.indexOf(l) + " " + span.toString() );
//			for(List<Span> l : all) for(Span span : l) if(span.getCoveredText(s.sourceString).toString().contains("70")) Debug.print("lala" + all.indexOf(l)+ " " + span.toString() );
			
			List<Span> after = resolveSpans(all, s);
//			Debug.print("--\t");
//			for(String word : s.words) Debug.print(word + "\t");
//			Debug.println("--");
//			Debug.print("**\t");
//			for(Span span : after) 
//				Debug.print(span.getCoveredText(s.sourceString) + "\t");
//			Debug.println("**");
			allFacts.add(after);
			details.add(detail);
//			System.exit(0);
		}
		C_Facts cFact = formFacts(allFacts, sentences, details);
//		S_Facts sfact = new S_Facts(cFact);
//		sfact.mergeFacts();
//		sfact.printFacts();
		return cFact;
		
//		for(int i = 0; i < sentences.size(); i++) {
//			Debug.println(sentences.get(i).sourceString);
//			Debug.print("**\t");
//			for(Span span : allFacts.get(i)) Debug.print(span.getCoveredText(sentences.get(i).sourceString) + "\t");
//			Debug.println("**");
//		}
		
		
		
	}
	
	

	
	private  List<Span> resolveSpans(List<List<Span>> all, Sequence seq) {
		HashSet<Span> hsAll = new HashSet<Span>(); 
		for(List<Span> spans : all) for(Span span : spans) 
//			hsAll.add(getFullSpan(span,seq));
			hsAll.add(span);
		List<Span> all_ = new ArrayList<Span>(); all_.addAll(hsAll);
		boolean done = false;
		while(!done) {
			all_ = removeSubspans(all_);
			if(!mergeTwoOverlappingSpans(all_)) 	done = true;
			else {System.err.println("Conflict Spans Detected!");}
		}
		return all_;
	}
	
	/**
	 * remove spans in @all whose superspan is in @all
	 * @param all
	 * @return
	 */
	private  List<Span> removeSubspans(List<Span> all) {
		HashSet<Span> hsAll = new HashSet<Span>(); for(Span span : all) hsAll.add(span);
		List<Span> all_ = new ArrayList<Span>(); all_.addAll(hsAll);
//		Debug.println(all_);
		Collections.sort(all_);
		List<Span> temp = new ArrayList<Span>();
		for(int i = 0; i < all_.size(); i++) {
			int j;
			for(j = 0; j < all_.size(); j++) {
				if(i == j) continue;
				if(all_.get(j).contains(all_.get(i))) break;
				if(all_.get(j).crosses(all_.get(i))) {
				}
			}
			if(j == all_.size()) temp.add(all_.get(i));
		}
		return temp;
	}
	
	/**
	 * Find a pair of overlapping spans in @all and merge the two spans
	 * @param all
	 * @return
	 */
	private  boolean mergeTwoOverlappingSpans(List<Span> all) {
		Collections.sort(all);
		int i;
		int j = -1;
		boolean foundOne = false;
		for(i = 0; i < all.size(); i++) {
			for(j = 0; j < all.size(); j++) {
				if(i == j) continue;
				if(all.get(j).crosses(all.get(i))) {
					foundOne = true; break;
				}
			}
			if(foundOne) break;
		}
		if(!foundOne) return false;
		Span newSpan = new Span(Math.min(all.get(i).getStart(), all.get(j).getStart()),Math.max(all.get(i).getEnd(), all.get(j).getEnd()));
		all.set(i, newSpan);
		all.remove(j);
		Collections.sort(all);
		return true;
	}
	
	/**
	 * Any token that crosses the span of all would be counted in.
	 * TODO: could be optimized using merge join instead of nested-loop join
	 * 
	 * @param all: the output of resolveSpans()
	 * @param senten: the source sentence
	 * @return
	 */
	private  C_Facts formFacts(List<List<Span>> all_facts, List<Sequence> sentens, List<String> details) {
		if(sentens.size() == 0) return null; 
		C_Facts cfacts = new C_Facts(sentens.get(0).pageNum, sentens.get(0).secNum, sentens.get(0).paraNum);
		utility util = new utility();
		for(int senIndex = 0; senIndex < sentens.size(); senIndex++) {
			List<Span> facts_per_sen = all_facts.get(senIndex);
			Sequence senten = sentens.get(senIndex);
			util.sortByStart(facts_per_sen);
			//		List<Span> relativeOrder = new ArrayList<Span>(); //[]

			// Any token that crosses the span of all would be counted in.
			HashSet<Integer> crossToken = new HashSet<Integer>();//the relative order of tokens that the span cross
			for(int i = 0; i < facts_per_sen.size(); i++) {
				Span cur = facts_per_sen.get(i);
				for(int j = 0; j < senten.size(); j++) {
					Span token = senten.spans.get(j);
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
				facts.add(senten.words.get(relativeOrder));
				relativeOrders.add(new Span(relativeOrder,relativeOrder));
				spans.put(senten.spans.get(relativeOrder).getStart(), senten.spans.get(relativeOrder).getEnd() - 1);
			}
			cfacts.addFact(facts, senIndex, relativeOrders, spans, details.get(senIndex));
			cfacts.sentences.add(sentens.get(senIndex).sourceString);
		}
		
		return cfacts;
	}
	
	/**
	 * 
	 * @param s [)
	 * @param seq [)
	 * @return
	 */
	private  Span getFullSpan(Span s, Sequence seq) {
		List<Span> continuousSeq = new ArrayList<Span>();
		Span prvSpan = null;
		for(int i = 0; i < seq.size(); i++) {
			Span curSpan = seq.spans.get(i);
			if(prvSpan == null) prvSpan = new Span(curSpan.getStart(), curSpan.getEnd());
			else{
				if(prvSpan.getEnd() == curSpan.getStart()) {
					prvSpan = new Span(prvSpan.getStart(), curSpan.getEnd());
				}else {
					continuousSeq.add(prvSpan);
					prvSpan = curSpan;
				}
			}
			if(i == seq.size() - 1) continuousSeq.add(prvSpan);
		}
		Debug.println(continuousSeq,DEBUG_CONFIG.debug_pattern);
		Debug.println(s,DEBUG_CONFIG.debug_pattern);
		for(int i = 0; i < continuousSeq.size(); i++) {
			if(continuousSeq.get(i).crosses(s)) return continuousSeq.get(i);
		}
		
		return null;
	}

}
