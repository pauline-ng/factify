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

/**
 * This class servers as the ONLY interface for NLP processing
 */
package nlp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import utility.Span;
import utility.Utility;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

/**
 * All functions involves NLP.
 * 
 * It is expensive to load StanfordNLP models, so this class is designed to be singleton using Enum. 
 *
 */
public enum StanfordNLPLight {
	
	INSTANCE("tokenize, ssplit, pos, lemma");
	final private StanfordCoreNLP pipeline;
	final private HashSet<String> stopwords;
	
	private StanfordNLPLight( String props_str) {
		Properties prop = new Properties();
		prop.put("annotators",props_str);
		pipeline = new StanfordCoreNLP(prop);
		stopwords = importStopWords();

	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//		StanfordNLPLight nlp = new StanfordNLPLight()
		testStanfordNLPLight(args);
	}

	public static void testStanfordNLPLight(String[] args) {
		// TODO Auto-generated method stub
		//		String text = "The precision is less than 0.1";
		//		String text = "However, both diphenhydramine and desloratadine+verapamil treated animals performed significantly less well on the rotarod than the MC treated animals (p<0.0001).";
//		String text = "Studies of genetic variation in African-American autism families are rare. Analysis of 557 "+
//				"Caucasian and an independent population of 54 African-"+
//				"American families with 35 SNPs within GABRB1 and "+
//				"GABRA4 strengthened the evidence for involvement of "+
//				"GABRA4 in autism risk in Caucasians (rs17599165, "+
//				"p=0.0015; rs1912960, p=0.0073; and rs17599416, "+
//				"p=0.0040) and gave evidence of significant association in "+
//				"African-Americans (rs2280073, p=0.0287 and rs168"+ 
//				"59788, p=0.0253). ";
		String text = "This allows otherwise poorly invasive bacteria to exploit lipid raft-mediated transcytotic pathways to cross the intestinal mucosa.";
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma");
		StanfordNLPLight nlp = StanfordNLPLight.INSTANCE;
		Annotation annotation = new Annotation(text);
		nlp.pipeline.annotate(annotation);
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

		for(CoreMap sentence : sentences) {
			System.out.println(sentence.keySet());
			System.out.println("---token text next---");
			for (CoreMap token : sentence.get(CoreAnnotations.TokensAnnotation.class)) 
				System.out.print(token.get(CoreAnnotations.TextAnnotation.class) + "\t");
			System.out.println("---POSTage next---");
			for (CoreMap token : sentence.get(CoreAnnotations.TokensAnnotation.class)) 
				System.out.print(token.get(CoreAnnotations.PartOfSpeechAnnotation.class) + "\t");
			System.out.println();
			System.out.println("---Lemma next---");
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
	 * @param para Input string (typically a paragraph with many sentences)
	 * @param refineNouns Since postagger is not good at parsing scientific text, we refine the tag of a word to be noun if there exists an occurrence of the word that has beed tagged Noun.
	 * @return A list sequences where each sequence represents a sentence
	 */
	public  List<Sequence> textToSequence(String para, boolean refineNouns) {
		para = para.replace("fig.", "fig-").replace("Fig.", "Fig-");//to avoid broken sentences 
		para = para.replace("ref.", "ref-").replace("Ref.", "Ref-");//to avoid broken sentences 
		para = para.replace("eq.", "eq-").replace("Eq.", "Eq-");//to avoid broken sentences 
		List<Sequence> sentences_ = new ArrayList<Sequence>();
		Annotation annotation = new Annotation(para);
		pipeline.annotate(annotation);
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		HashSet<String> knowNouns = new HashSet<String>();
		if(stopwords == null) importStopWords();
		int senIndex = 0;
		for(CoreMap sentence : sentences) {
			List<String> words = new ArrayList<String>();
			List<String> stems = new ArrayList<String>();
			List<String> POSTag = new ArrayList<String>();
			List<Span> spans = new ArrayList<Span>();
			for(CoreMap token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
				String stem = token.get(CoreAnnotations.LemmaAnnotation.class);
				words.add(token.get(CoreAnnotations.TextAnnotation.class));
				stems.add(stem);
				POSTag.add(token.get(CoreAnnotations.PartOfSpeechAnnotation.class));
				spans.add(new Span(token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class) - sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class),
						token.get(CoreAnnotations.CharacterOffsetEndAnnotation.class)- sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class)));
				if(isNoun(POSTag.get(POSTag.size() - 1)) && stem!= null) knowNouns.add(stem);
			}
			sentences_.add(new Sequence(words, stems, POSTag, spans, para.substring(sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class), sentence.get(CoreAnnotations.CharacterOffsetEndAnnotation.class)),
					 senIndex));
			senIndex++;
		}
		//refine pos tag: because current taggger may not work for scientific content
		if(refineNouns) {
			for(Sequence s : sentences_) {
				for(int i = 0; i < s.getWordCount(); i++) {
					String token = s.getStemOfWord(i);
					if(knowNouns.contains(token)) s.setPOSTagOfWord(i, "NN");
				}
			}
		}
		return sentences_;
	}

	private HashSet<String> importStopWords() {
		try{
			String s = getResourceAsString("stopwords.txt");
			StringTokenizer st = new StringTokenizer(s, "\r\n");
			HashSet<String> t = new HashSet<String>();
			while(st.hasMoreTokens()) t.add(st.nextToken().trim());
			return t;
		}
		catch(Exception e) {
			e.printStackTrace();
			System.out.println("error");
			return null;
		}
	}
	
	public static boolean isNoun(String pos) {
		if(pos.equals("NN") || pos.equals("NNS") || pos.equals("NNP") || pos.equals("NNPS"))
			return true;
		return false;
	}

	public String getResourceAsString(String fileName) {
		try{	 
			BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(fileName )));
			return Utility.toString(br);
		}
		catch(Exception e) {
			ClassLoader classLoader = getClass().getClassLoader();
			System.out.println(classLoader.getClass().getName().toString());
			e.printStackTrace();
			return null;
		}

	}

	public boolean containsStopWord(String word) {
		return this.stopwords != null && this.stopwords.contains(word); 
	}

}
