package nlp;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import utility.utility;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.dictionary.Dictionary;

public class wordnet {
	
	public Dictionary dict;
	public wordnet() {
		configureJWordNet();
	}
	public static void main(String[] args) throws JWNLException {
		wordnet wn = new wordnet();
		wn.configureJWordNet();
//		Set<String> synonyms = new HashSet<String>();
//		wn.lookupSynonyms("improve",synonyms);
//		System.out.println(synonyms);
		System.out.println(wn.StemWordWithWordNet("rs111"));
		
	}
	 public  Set<String> lookupSynonyms(String lexicalForm, Set<String> synonyms) throws JWNLException
	 {
//		 Set<String> synonyms = new HashSet<String>();
		 configureJWordNet();
		 IndexWord indexWord = dict.getIndexWord(POS.VERB, lexicalForm);
		 if (indexWord == null)
			 return synonyms;
		 Synset[] synSets = indexWord.getSenses();
		 for (Synset synset : synSets)
		 {
			 Word[] words = synset.getWords();
			 HashSet<String> newS = new HashSet<String>();
			 for (Word word : words)
			 {
				if(synonyms.add(word.getLemma())) {
					newS.add(word.getLemma());
				}
			 }
			 System.out.println(lexicalForm + "\t" + synonyms);
			 for(String s : newS) {
				 lookupSynonyms(s, synonyms);
			 }
		 }
//		 synonyms.remove(lexicalForm);
		 return synonyms;
	 }
	 public  void configureJWordNet() {
         // WARNING: This still does not work in Java 5!!!
         try {
                 // initialize JWNL (this must be done before JWNL can be used)
                 // See the JWordnet documentation for details on the properties file
        	 if(dict == null) {
        		 utility util = new utility();
//                 FileInputStream oFileInputStream = new FileInputStream("NLP_INPUT/properties.xml");
        		 InputStream is = getResourceAsInputStream("properties.xml");
//        		 System.out.println("configureJWordNet " + (is == null));
                 JWNL.initialize(is);
                 dict = Dictionary.getInstance();
        	 }
         } catch (Exception ex) {
                 ex.printStackTrace();
                 utility util = new utility();
                 FileWriter fw = null;
                 PrintWriter pw = new PrintWriter (fw);
                 ex.printStackTrace(pw);
                 pw.close();
//                 util.writeFile("d:/zotero.txt","\r\n path: "+ wordnet.class.getProtectionDomain().getCodeSource().getLocation() + "\r\n", true);
 				
//                 util.writeFile("d:/zotero.txt","\r\n"+ ex.getStackTrace().toString() + "\r\n", true);
                 System.exit(-1);
         }
 }
	 
		
		/*
		 * stems a word with wordnet
		 * @param word word to stem
		 * @return the stemmed word or null if it was not found in WordNet
		 */
		public String StemWordWithWordNet ( String word )
		{
//			if(word.equals("al.")) {
//				System.out.println("debug");
//			}
			if ( word == null ) return word;
			if (containNumber(word)) return word;
			if (word.contains("-") || word.contains(Character.toString((char) '\u2013'))) {//without this, wn will stem "off-target" to "off"
				StringTokenizer st = new StringTokenizer(word, "-");
				String newWord = null;
				while(st.hasMoreTokens()) {
					if(newWord == null) newWord = StemWordWithWordNet(st.nextToken());
					else newWord = newWord + "-" + StemWordWithWordNet(st.nextToken());
				}
				if(word.endsWith("-")) newWord = newWord + "-";
				if(word.startsWith("-")) newWord = "-" + newWord;
				if(word.endsWith(Character.toString((char) '\u2013'))) newWord = newWord + Character.toString((char) '\u2013');
				if(word.startsWith(Character.toString((char) '\u2013'))) newWord = Character.toString((char) '\u2013') + newWord;
				return newWord;
			}
			if (word.contains(".")) {//without this, wn will stem "al." to "al" (et al.)
				StringTokenizer st = new StringTokenizer(word, ".");
				String newWord = null;
				while(st.hasMoreTokens()) {
					if(newWord == null) newWord = StemWordWithWordNet(st.nextToken());
					else newWord = newWord + "." + StemWordWithWordNet(st.nextToken());
				}
				if(word.endsWith(".")) newWord = newWord + ".";
				if(word.startsWith(".")) newWord = "." + newWord;
				return newWord;
			}
			IndexWord w;
			Dictionary dictionary = dict;
			try
			{
				w = dictionary.lookupIndexWord(POS.VERB, word);
				//				System.out.println(token + "=>" + indexWord.getLemma());
				if ( w != null )
					return w.getLemma().toString ();
				w = dictionary.lookupIndexWord( POS.NOUN, word );
				if ( w != null )
					return w.getLemma().toString();
				w = dictionary.lookupIndexWord( POS.ADJECTIVE, word );
				if ( w != null )
					return w.getLemma().toString();
				w = dictionary.lookupIndexWord( POS.ADVERB, word );
				if ( w != null )
					return w.getLemma().toString();
			} 
			catch ( JWNLException e )
			{
			}
			return word;
		}
		
		private boolean containNumber(String word) {
			if(word.contains("0")
					|| word.contains("1")
					|| word.contains("2")
					|| word.contains("3")
					|| word.contains("4")
					|| word.contains("5")
					|| word.contains("6")
					|| word.contains("7")
					|| word.contains("8")
					|| word.contains("9")
					) return true;
			return false;
		}
		 public InputStream getResourceAsInputStream(String fileName) {
			return getClass().getClassLoader().getResourceAsStream(fileName);
		 }
		 

}

