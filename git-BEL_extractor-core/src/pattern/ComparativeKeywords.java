package pattern;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;

import utility.utility;

public class ComparativeKeywords {
	public static HashSet<String> n_verbs;
	public static HashSet<String> n_nouns;
	public static HashSet<String> p_verbs;
	public static HashSet<String> p_nouns;
	public static HashSet<String> adjs;
	public static HashSet<String> advs;
	public static HashSet<String> others;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ComparativeKeywords ck = new ComparativeKeywords();
		ck.initialize();
		System.out.println("Negative Verbs: " + n_verbs);
		System.out.println("Negative Nouns: " + n_nouns);
		System.out.println("Positive Verbs: " + p_verbs);
		System.out.println("Positive Nouns: " + p_nouns);
		System.out.println("Ajectives     : " + adjs);
		System.out.println("Adverbs       : " + advs);
		System.out.println("others        : " + others);

	}
	public  void initialize() {
//		String path = "CORENLP_INPUT/comparative_1.txt";
		if(n_verbs != null) return;
		n_verbs = new HashSet<String>();
		n_nouns = new HashSet<String>();
		p_verbs = new HashSet<String>();
		p_nouns = new HashSet<String>();
		adjs = new HashSet<String>();
		advs = new HashSet<String>();
		others = new HashSet<String>();
		HashSet<String> current = n_verbs;
		utility util = new utility();
//		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
		try (BufferedReader br = getResourceAsBufReader("comparative_1.txt")) {
		    String line;
		    while ((line = br.readLine()) != null) {
		       if(line.trim().startsWith("//")) {
		    	   line = line.substring(2);
		    	   switch(line) {
		    	   case "negative--verb":
		    		   current = n_verbs;
		    		   break;
		    	   case "negative--noun":
		    		   current = n_nouns;
		    		   break;
		    	   case "positive--verb":
		    		   current = p_verbs;
		    		   break;
		    	   case "positive--noun":
		    		   current = p_nouns;
		    		   break;
		    	   case "adjective":
		    		   current = adjs;
		    		   break;
		    	   case "adverb":
		    		   current = advs;
		    		   break;
		    	   case "other":
		    		   current = others;
		    		   break;
		    	   }
		       }else current.add(line.trim());
		    }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public  boolean contain(String s) {
		if(n_verbs == null) initialize();
		if(n_verbs.contains(s) ||
				n_nouns.contains(s) ||
				p_verbs.contains(s) ||
				p_nouns.contains(s) ||
				adjs.contains(s) || 
				advs.contains(s) ||
				others.contains(s))
			return true;
		return false;
	}
	public  boolean contain_NN_V(String s) {
		if(n_verbs == null) initialize();
		if(n_verbs.contains(s) ||
				n_nouns.contains(s) ||
				p_verbs.contains(s) ||
				p_nouns.contains(s))
			return true;
		return false;
	}
	
	public static boolean containPOS(String pos) {
		return pos.equals("RBR") || pos.equals("RBS") || pos.equals("JJR") || pos.equals("JJS"); 
	}
	public  BufferedReader getResourceAsBufReader(String fileName) {
//		ClassLoader classLoader = getClass().getClassLoader();
		//				File file = new File(classLoader.getResource("stopwords.txt").getFile());
		BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(fileName )));
		return br;
	}
}
