package pattern;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;


import java.util.Stack;

import utility.Debug;
import utility.Span;
import utility.utility;
import utility.Debug.DEBUG_CONFIG;
//import reddit_anaysis.utility;
//import edu.stanford.nlp.ling.CoreAnnotations;
//import edu.stanford.nlp.ling.CoreAnnotations.IsURLAnnotation;
//import edu.stanford.nlp.pipeline.Annotation;
//import edu.stanford.nlp.util.CoreMap;
//import net.didion.jwnl.JWNLException;
//import net.didion.jwnl.data.IndexWord;
//import net.didion.jwnl.data.POS;
//import net.didion.jwnl.dictionary.Dictionary;
import nlp.Sequence;
import nlp.StanfordNLPLight;
//import nlp.wordnet;
//import model.PMC_PDF;
//import PDFconverter.Comparison_PMC;

public class NGrams {
//	static String NLP_INPUT_PROPERTIES = "NLP_INPUT/properties.xml";
//	static String NLP_INPUT_EN_PARSER_CHUNKING = "NLP_INPUT/en-parser-chunking.bin";
//	static String NLP_INPUT_EN_SENT = "NLP_INPUT/en-sent.bin";
//	static String NLP_INPUT_EN_POS_MAXENT = "NLP_INPUT/en-pos-maxent.bin";
//	static String NLP_INPUT_EN_TOKEN = "NLP_INPUT/en-token.bin";
	//	private static  openNLP nlp;
	public static StanfordNLPLight nlp;
//	public static wordnet wn;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		ArrayList<String> seq1 = new ArrayList<String>(); seq1.add("string");
//		ArrayList<String> seq2 = new ArrayList<String>(); seq2.add("string");
//		Sequence s1 = new Sequence(seq1);
//		Sequence s2 = new Sequence(seq2);
//		Debug.println(s1.equals(s2));
		
//		anaylze();
		Debug.println("[aa]".hashCode() + "\t" + "[aa]".hashCode(),DEBUG_CONFIG.debug_temp);
//		System.exit(0);
		anaylze_String("", "test/PMC1513515/facts.txt");
		NGrams ngram = new NGrams();
//		ngram.stemNounWithWordNet("rs17599416");
	}
	

	public static List<Integer> anaylze_String(String para, String facts_outfile) {
//		 para = "";
		utility util = new utility();
		para = util.readFromFile("test/PMC1513515/Results_section.txt");
//		Debug.println(paras.size());
		List<Integer> stat = new ArrayList<Integer>();
		if(nlp == null) nlp = new StanfordNLPLight();
		 ArrayList<Sequence> sentences_tokens = (ArrayList<Sequence>) nlp.textToSequence(para, true);//each sentence is a token sequence
//			String[] sentences= nlp.sdetector.sentDetect(para);
//			for(String senten : sentences) {//missing: as <word> as
//				String tokens[] = nlp.tokenizer.tokenize(senten);
//				Debug.println(senten);
//				for(String token : tokens) Debug.print(token + "\t");
//				for(int i = 0; i < tokens.length; i++) tokens[i] = stemNounWithWordNet(tokens[i]);
//				Debug.println();
//				for(String token : tokens) Debug.print(token + "\t");
//				Debug.println();
//				sentences_tokens.add(new Sequence(tokens));
//			}
//		 Properties props = new Properties();
//	     props.put("annotators", "tokenize, ssplit, pos");
//	     nlp = new StanfordNLP(props);
//	     Annotation annotation = new Annotation(para);
//	     nlp.pipeline.annotate(annotation);
//	     
//	     List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
//	    if(nlp.stopwords == null) nlp.importStopWords();
//	    wn = new wordnet();
//	    HashSet<String> knowNouns = new HashSet<String>();
//	     for(CoreMap sentence : sentences) {
////	    	 Debug.println(sentence.toShorterString());
//	    	 List<String> tokens = new ArrayList<String>();
//	    	 List<String> POSTag = new ArrayList<String>();
//	    	 List<Span> spans = new ArrayList<Span>();
// 	    	 for(CoreMap token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
////	    		 String token_s = token.get(CoreAnnotations.LemmaAnnotation.class);
// 	    		 String token_s = wn.StemWordWithWordNet(token.get(CoreAnnotations.TextAnnotation.class));
////	    		 if(!nlp.stopwords.contains(token_s)) {
//	    			 tokens.add(token_s);
//	    			 POSTag.add(token.get(CoreAnnotations.PartOfSpeechAnnotation.class));
//	    			 spans.add(new Span(token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class) - sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class),
//	    					 token.get(CoreAnnotations.CharacterOffsetEndAnnotation.class)- sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class)));
//	    			 if(isNoun(POSTag.get(POSTag.size() - 1))) knowNouns.add(token_s);
////	    		 }
//	    	 }
// 	    	 {//debug
// 	    		String text = para.substring(sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class), sentence.get(CoreAnnotations.CharacterOffsetEndAnnotation.class));
// 	    		Debug.println(text);
//// 	    		Debug.println(sentence.get(CoreAnnotations.))
// 	    		for(Span span : spans) Debug.print(text.substring(span.getStart(), span.getEnd()) + "**");
// 	    		Debug.println();
//// 	    		System.exit(0); 
// 	    	 }
//	    	 sentences_tokens.add(new Sequence(tokens, POSTag, spans, para.substring(sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class), sentence.get(CoreAnnotations.CharacterOffsetEndAnnotation.class))));
//	     }
	     //refine pos tag: because current taggger may not work for scientific content
//	     for(Sequence s : sentences_tokens) {
//	    	 for(int i = 0; i < s.stems.size(); i++) {
//	    		 String token = s.stems.get(i);
//	    		 if(knowNouns.contains(token)) s.POSTags.set(i, "NN");
//	    	 }
////	    	 Debug.println(s.details());
//	     }
		NGrams ngram = new NGrams();
		List<Sequence> freSeq = ngram.getFreqSequences(sentences_tokens);
		HashSet<Sequence> freSeq_ = new HashSet<Sequence>(); freSeq_.addAll(freSeq);
		HashSet<Integer> freSeqLen = new HashSet<Integer>();
		for(Sequence s : freSeq_) freSeqLen.add(s.size());
		if(facts_outfile != null) util.writeFile(facts_outfile, "", false);
		for(Sequence senten : sentences_tokens) {//find matching frequent pattern within each sentence
			ArrayList<Integer> enty_indices = new ArrayList<Integer>();
			//match from longest to shortest
			HashSet<Integer> matchedToken = new HashSet<Integer>();
			int k = senten.size();//length of pattern to be matched

			while(k > 0) {
				if(!freSeqLen.contains(k)) {k--; continue;}
				boolean coarseMatch = false;
				for(Sequence s : freSeq_) {
					if(s.size() == k && s.isSubsequenceOrSelfOf(senten)) coarseMatch = true;
					if(coarseMatch) break;
				}
				if(!coarseMatch) {k--; continue;}
				int b = 0; // start/beginning of the substring
				while(b < senten.size() - k) {
					boolean foundOne = true;
					for(int m = b; m < b + k; m++) if(matchedToken.contains(m)) {//matched token in the middle, start from next token
						foundOne = false; break;
					}
					if(foundOne && freSeq_.contains(senten.getSubsequence(b, b+k))) {//find one and it's matched to a freq pattern
						enty_indices.add(b); enty_indices.add(b+k); 
						for(int j = b; j < b+k; j++) matchedToken.add(j);
						b = b + k; 
					}else b++;
				}
				k--;
			}
			//now sort indices
			Collections.sort(enty_indices);
			//now process relations
			List<Integer> indices_rel = new ArrayList<Integer>();
			ComparativeKeywords ck = new ComparativeKeywords();
			for(int i = 0; i < senten.size(); i++) {
				if(ck.contain_NN_V(senten.getPrettyStem(i)) ||
						ComparativeKeywords.containPOS(senten.POSTags.get(i)))
					indices_rel.add(i);
			}
			stat.add(enty_indices.size()/2); stat.add(indices_rel.size());
			boolean toPrint = true;
			if(!toPrint) continue;
			if(facts_outfile != null) util.writeFile(facts_outfile, "**", true);
			if(enty_indices.size() > 0) {//print assuming indicies are ordered
				if(toPrint) Debug.print("[EN=" + enty_indices.size()/2 + " REL=" + indices_rel.size() + "]", DEBUG_CONFIG.debug_temp);
				int p = 0;//pointer of indices
				
				for(int i = 0; i < senten.stems.size(); i++) {
					if(p < enty_indices.size() && i == enty_indices.get(p)) {
						if(toPrint) Debug.print("[",DEBUG_CONFIG.debug_temp);
//						for(int j = i; j < enty_indices.get(p + 1); j++) {
//							Debug.print(" " + senten.getPrettyToken(j));
//						}
						if(toPrint) Debug.print(senten.getSubsequence(i, enty_indices.get(p+1)).sourceString,DEBUG_CONFIG.debug_temp);
						if(facts_outfile != null) util.writeFile(facts_outfile,senten.getSubsequence(i, enty_indices.get(p+1)).sourceString + "\t" , true);
						if(toPrint) Debug.print("] ",DEBUG_CONFIG.debug_temp);
						i = enty_indices.get(p + 1) - 1;
						p = p + 2;
					}else {
						if(toPrint) Debug.print(senten.getPrettyStem(i) + " ",DEBUG_CONFIG.debug_temp);
					}
				}
				if(facts_outfile != null) util.writeFile(facts_outfile,"**\r\n", true);
				if(toPrint) Debug.println(DEBUG_CONFIG.debug_temp);
				if(toPrint) Debug.println(senten.sourceString,DEBUG_CONFIG.debug_temp);
			}else {
				if(toPrint) Debug.println("[N]" + senten.sourceString,DEBUG_CONFIG.debug_temp);
			}
			if(facts_outfile != null) util.writeFile(facts_outfile, "**", true);
			
		}
		return stat;
	}
	
	public List<Sequence> getFreqSequences(List<Sequence> sentences_tokens) {
//		ArrayList<Sequence> sentences_tokens_ = ArrayList<Sequence>();
		
		List<Sequence> sequences = extractNGrams(sentences_tokens);
//		for(Sequence s : sequences) {
//			for(Sequence ss : sequences) {
//				if(ss.size() == s.size() + 1 
//						&& ss.isSupersequenceOrSelfOf(s)) {
//					s.addSupperSequences(ss);
//					ss.addsubSequences(s);
//				}
//			}
//		}
		//check validity individually
		List<Sequence> validSeq = new ArrayList<Sequence>();
		for(Sequence s : sequences) {
			//filter1: absoluteFreq >=3 
			if(s.getAbsoluteFreq() <= 1)	continue;
			//filter2: is Valid
			if(nlp.stopwords == null) nlp.importStopWords();
			if(!isValid(s)) continue;
			//filter3: clean the parenthesis --require paired parenthesis
			if(s.containUnpairedParen()) continue;
			validSeq.add(s);
			
		}
		//check validity: cross-examination
		List<Sequence> result = new ArrayList<Sequence>();
		for(Sequence s : validSeq) {
			
			//filter1: is closed (see Data mining textbook), i.e. there does not exist a super sequence that has the same freq as it
			boolean isClosed = true;
			for(Sequence sup : validSeq) {
				if(sup.isSupersequenceOf(s)  && sup.getAbsoluteFreq() == s.getAbsoluteFreq()) isClosed = false;
			}
			if(!isClosed) continue; 
			result.add(s);
		}
		for(int i = 0; i < result.size(); i++) {//sort by size
			for(int j = i + 1; j < result.size(); j++) {
				if(result.get(j).size() > result.get(i).size()) {
					Sequence temp = result.get(i);
					result.set(i, result.get(j)); result.set(j, temp);
				}
			}
		
		}
//		for(Sequence s : result) 	Debug.println(s.toString() + "\t" + s.getAbsoluteFreq());
		return result;
			
	}
	public List<Sequence> extractNGrams(List<Sequence> sentences) {
		HashMap<Sequence, Integer> ngramsToFreq = new HashMap<Sequence, Integer>();
		for(Sequence sentence : sentences) {
//			Debug.println(sentence.sourceString);
			for(int n = 1; n < sentence.size(); n++) {
				
				for(int i = 0; i <= sentence.size() - n; i++) {
					Sequence ngram = sentence.getSubsequence(i, i + n);
//					Debug.print(ngram.sourceString);
					if(ngram.containsIndivStem(",") || ngram.containsIndivStem(".") || ngram.containsIndivStem(";") 
							|| ngram.containsIndivStem("-lrb-") || ngram.containsIndivStem("-rrb-")) continue;
					if(!ngram.containsNouns()) continue;
					if(ngramsToFreq.containsKey(ngram)) {
						ngramsToFreq.put(ngram, ngramsToFreq.get(ngram) + 1);
					}
					else ngramsToFreq.put(ngram, 1);
//					Debug.println();
				}
			}
		}
		Map<Sequence, Integer> sorted_ngramsToFreq = sortByValue(ngramsToFreq);
		ArrayList<Sequence> keys = new ArrayList<Sequence>();
		keys.addAll(sorted_ngramsToFreq.keySet());
		for(Sequence ngram : keys) {
			ngram.setAbsoluteFreq(sorted_ngramsToFreq.get(ngram));
//			if(ngramsToFreq.get(ngram) > 1)
//			Debug.println(ngram + "\t" + ngramsToFreq.get(ngram));
		}
		return keys;
	}
	
	private Map sortByValue(Map map) {
	     List list = new LinkedList(map.entrySet());
	     Collections.sort(list, new Comparator() {
	          public int compare(Object o1, Object o2) {
	               return ((Comparable) ((Map.Entry) (o1)).getValue())
	              .compareTo(((Map.Entry) (o2)).getValue());
	          }
	     });

	    Map result = new LinkedHashMap();
	    for (Iterator it = list.iterator(); it.hasNext();) {
	        Map.Entry entry = (Map.Entry)it.next();
	        result.put(entry.getKey(), entry.getValue());
	    }
	    return result;
	} 
	
	
	/**
	 * [begin,end)
	 * @param tokens
	 * @param begin
	 * @param end
	 * @return
	 */
	private String concatenate (ArrayList<String> tokens, int begin, int end) {
		String result = "";
		for(int i = begin; i < tokens.size() && i < end; i++) {
			result = result + " " + tokens.get(i);
		}
		
		return result.trim();
	}
	
	public static ArrayList<File> findFildEndWith(String suffix, String path) {
//		path = "PMC_FILES\\XML\\PMC2000650\\";
		File dir = new File(path);
		File [] fl = dir.listFiles();
		ArrayList<File> result = new ArrayList<File>();
		for(File file : fl) {
			if(file.getName().endsWith(suffix)) result.add(file);
			if(file.isDirectory()) {
				result.addAll(findFildEndWith(suffix, file.getPath()));
			}
		}
		return result;
	}
	public boolean isStopWords(Sequence s) {
		for(int i = 0; i < s.stems.size(); i++) {
			if(!nlp.stopwords.contains(s.stems.get(i))) return false;
		}
		return true;
	}
	public boolean isValid(Sequence s) {
		{
			String beginning = s.stems.get(0);
			beginning = beginning.trim();
			if(beginning.length() == 1) return false;
		}
		{
			String end = s.stems.get(s.stems.size() - 1);
			end = end.trim();
			if(end.length() == 1) return false;
		}
		if(isStopWords(s)) return false;
		if(s.endsWithIndivStem("of") 
				|| s.endsWithIndivStem("in") 
				|| s.endsWithIndivStem("within") 
				||s.endsWithIndivStem("also") 
				|| s.endsWithIndivStem("to") 
				|| s.endsWithIndivStem("be")
				||s.endsWithIndivStem("between")
				||s.endsWithIndivStem("for")
				|| s.equals(new Sequence("table")) 
				|| s.equals(new Sequence("study")) 
				|| s.equals(new Sequence("analysis"))) return false;
		if(s.startsWithIndivStem("and") 
				|| s.startsWithIndivStem("in") 
				|| s.startsWithIndivStem("of") 
				|| s.startsWithIndivStem("the")
				||s.startsWithIndivStem("between") 
				|| s.startsWithIndivStem("with") 
				||s.startsWithIndivStem("for")
				) return false;
//		if(s.endsWithToken("(")  || s.endsWithToken("[") || s.endsWithToken("]") || s.endsWithToken("=")) return false;
		  TextToNum textToNum = new TextToNum();
		  if(textToNum.parse(s.toString()) != null) return false;//remove sole numbers
		return true;
	}
	public static boolean isNoun(String pos) {
		if(pos.equals("NN") || pos.equals("NNS") || pos.equals("NNP") || pos.equals("NNPS"))
		return true;
		return false;
	}
	
	
}
