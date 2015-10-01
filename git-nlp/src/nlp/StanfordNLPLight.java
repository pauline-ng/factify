package nlp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;








//import org.apache.commons.io.IOUtils;

//import net.didion.jwnl.data.Word;
import utility.Span;
import utility.utility;
//import knowledge_model.C_Facts;
//import knowledge_model.S_Facts;
//import reddit_anaysis.utility;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
//import extractor.wordnet;

public class StanfordNLPLight {
	 public StanfordCoreNLP pipeline;
	 public HashSet<String> stopwords;
	public String sourceFolder;
	public  wordnet wn;
	public StanfordNLPLight() {
	     Properties props = new Properties();
//	     props.put("annotators", "tokenize, ssplit, pos, lemma, ner, depparse");
	     props.put("annotators", "tokenize, ssplit, pos");
//	     props.put("ner.model", "edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz");
//	     props.put("ner.applyNumericClassifiers", "false");
	     
	}
	public StanfordNLPLight( Properties props) {
	      pipeline = new StanfordCoreNLP(props);
	}
	public StanfordNLPLight( String props_str) {
		 Properties prop = new Properties();
	     prop.put("annotators",props_str);
	      pipeline = new StanfordCoreNLP(prop);
	      
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String text = "The precision is less than 0.1";
		
		 Properties props = new Properties();
	     props.put("annotators", "tokenize, ssplit, pos, lemma");
	     StanfordNLPLight nlp = new StanfordNLPLight(props);
	     Annotation annotation = new Annotation(text);
	     nlp.pipeline.annotate(annotation);
	     List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
	     
	     for(CoreMap sentence : sentences) {
	    	 System.out.println(sentence.keySet());
	    	 
	    	  for (CoreMap token : sentence.get(CoreAnnotations.TokensAnnotation.class)) 
	    	        System.out.print(token.get(CoreAnnotations.TextAnnotation.class) + "\t");
	    	  System.out.println();
	    	  for (CoreMap token : sentence.get(CoreAnnotations.TokensAnnotation.class)) 
	    	        System.out.print(token.get(CoreAnnotations.PartOfSpeechAnnotation.class) + "\t");
	    	   System.out.println();
	    	   for (CoreMap token : sentence.get(CoreAnnotations.TokensAnnotation.class)) 
	    	        System.out.print(token.get(CoreAnnotations.LemmaAnnotation.class) + "\t");
	    	   System.out.println();
	    	   for (CoreMap token : sentence.get(CoreAnnotations.TokensAnnotation.class)) 
	    		   System.out.print("[" + token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class) + "\t" 
	    	   + token.get(CoreAnnotations.CharacterOffsetEndAnnotation.class) + ") ");
//	    	   for (CoreMap lemma : sentence.get(CoreAnnotations.LemmaAnnotation.class))
//	    	        System.out.print(lemma.toShorterString() + "\t");
	    	   System.out.println();
	     }
	}
	
	
	public List<Span> splitSentences(String s) {
		Annotation annotation = new Annotation(s);
	     pipeline.annotate(annotation);
	     List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
	     List<Span> spans = new ArrayList<Span>();
	     for(CoreMap sentence : sentences) {
	    	 int start = sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
	    	 int end = sentence.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
	    	 spans.add(new Span(start,end));
	     }
	     return spans;
	}
	
	/**
	 * Since postagger is not good at parsing scientific text, we refine the tag of a word to be noun if there exists an occurrence of the word that has beed tagged Noun.
	 * @param para
	 * @return
	 */
	public List<Sequence> textToSequence(String para, boolean refindNouns) {
		para = para.replace("fig.", "fig-").replace("Fig.", "Fig-");//to avoid broken sentences 
		para = para.replace("ref.", "ref-").replace("Ref.", "Ref-");//to avoid broken sentences 
		para = para.replace("eq.", "eq-").replace("Eq.", "Eq-");//to avoid broken sentences 
		List<Sequence> sentences_ = new ArrayList<Sequence>();
//		Properties props = new Properties();
//		props.put("annotators", "tokenize, ssplit, pos");
//		nlp = new StanfordNLP(props);
		Annotation annotation = new Annotation(para);
		pipeline.annotate(annotation);
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		HashSet<String> knowNouns = new HashSet<String>();
		if(this.stopwords == null) importStopWords();
		if(wn == null) 
			wn = new wordnet();
		for(CoreMap sentence : sentences) {
				    	 System.out.println(sentence.toShorterString());
			List<String> words = new ArrayList<String>();
			List<String> stems = new ArrayList<String>();
			List<String> POSTag = new ArrayList<String>();
			List<Span> spans = new ArrayList<Span>();
			for(CoreMap token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
				//	    		 String token_s = token.get(CoreAnnotations.LemmaAnnotation.class);
				String stem = wn.StemWordWithWordNet(token.get(CoreAnnotations.TextAnnotation.class));
				//	    		 if(!nlp.stopwords.contains(token_s)) {
				words.add(token.get(CoreAnnotations.OriginalTextAnnotation.class));
				stems.add(stem);
				POSTag.add(token.get(CoreAnnotations.PartOfSpeechAnnotation.class));
				spans.add(new Span(token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class) - sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class),
						token.get(CoreAnnotations.CharacterOffsetEndAnnotation.class)- sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class)));
				 if(isNoun(POSTag.get(POSTag.size() - 1))) knowNouns.add(stem);
			}
			System.out.println(words);
			System.out.println(stems);
			sentences_.add(new Sequence(words, stems, POSTag, spans, para.substring(sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class), sentence.get(CoreAnnotations.CharacterOffsetEndAnnotation.class))));
		}
		  //refine pos tag: because current taggger may not work for scientific content
	   if(refindNouns) {
		   for(Sequence s : sentences_) {
			   for(int i = 0; i < s.stems.size(); i++) {
				   String token = s.stems.get(i);
				   if(knowNouns.contains(token)) s.POSTags.set(i, "NN");
//				   System.out.println(s.words.get(i));
			   }
			   //	    	 System.out.println(s.details());
	     }
	   }
		return sentences_;
	}
	public  List<Sequence> textToSequence(String para, int pageNum, int secNum, int paraNum, boolean refineNouns) {
		para = para.replace("fig.", "fig-").replace("Fig.", "Fig-");//to avoid broken sentences 
		para = para.replace("ref.", "ref-").replace("Ref.", "Ref-");//to avoid broken sentences 
		para = para.replace("eq.", "eq-").replace("Eq.", "Eq-");//to avoid broken sentences 
		List<Sequence> sentences_ = new ArrayList<Sequence>();
//		Properties props = new Properties();
//		props.put("annotators", "tokenize, ssplit, pos");
//		nlp = new StanfordNLP(props);
		Annotation annotation = new Annotation(para);
		pipeline.annotate(annotation);
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		HashSet<String> knowNouns = new HashSet<String>();
		if(stopwords == null) importStopWords();
		wn = new wordnet();
		int senIndex = 0;
		for(CoreMap sentence : sentences) {
			//	    	 System.out.println(sentence.toShorterString());
			List<String> words = new ArrayList<String>();
			List<String> stems = new ArrayList<String>();
			List<String> POSTag = new ArrayList<String>();
			List<Span> spans = new ArrayList<Span>();
			for(CoreMap token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
				//	    		 String token_s = token.get(CoreAnnotations.LemmaAnnotation.class);
				String stem = wn.StemWordWithWordNet(token.get(CoreAnnotations.TextAnnotation.class));
				//	    		 if(!nlp.stopwords.contains(token_s)) {
				words.add(token.get(CoreAnnotations.TextAnnotation.class));
				stems.add(stem);
				POSTag.add(token.get(CoreAnnotations.PartOfSpeechAnnotation.class));
				spans.add(new Span(token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class) - sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class),
						token.get(CoreAnnotations.CharacterOffsetEndAnnotation.class)- sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class)));
				 if(isNoun(POSTag.get(POSTag.size() - 1))) knowNouns.add(stem);
			}
			sentences_.add(new Sequence(words, stems, POSTag, spans, para.substring(sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class), sentence.get(CoreAnnotations.CharacterOffsetEndAnnotation.class)),
					                   pageNum, secNum, pageNum, senIndex));
			senIndex++;
		}
		  //refine pos tag: because current taggger may not work for scientific content
	    if(refineNouns) {
	    	for(Sequence s : sentences_) {
	    		for(int i = 0; i < s.stems.size(); i++) {
	    			String token = s.stems.get(i);
	    			if(knowNouns.contains(token)) s.POSTags.set(i, "NN");
	    		}
	    		//	    	 System.out.println(s.details());
	    	}
	    }
		return sentences_;
	}
	
	public void importStopWords() {
		utility util = new utility();
		//	String path = "CORENLP_INPUT/stopwords.txt";
		//	String s = util.readFromFile(path);
		String s = getResourceAsString("stopwords.txt");
		StringTokenizer st = new StringTokenizer(s, "\r\n");
		stopwords = new HashSet<String>();
		while(st.hasMoreTokens()) stopwords.add(st.nextToken().trim());
	}
	public static boolean isNoun(String pos) {
		if(pos.equals("NN") || pos.equals("NNS") || pos.equals("NNP") || pos.equals("NNPS"))
		return true;
		return false;
	}
	
	public String getResourceAsString(String fileName) {
//		 ClassLoader classLoader = getClass().getClassLoader();
		 //				File file = new File(classLoader.getResource("stopwords.txt").getFile());
			 utility util = new utility();
			 BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(fileName )));
			 return util.toString(br);
		 
	 }
	

}
