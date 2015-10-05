package pattern;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;

import utility.utility;

public class SignalKeywords {
	public static HashSet<String> negatives;
	public static HashSet<String> others;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SignalKeywords sk = new SignalKeywords();
		sk.initialize();
		System.out.println("Negative signal words: " + negatives);
		System.out.println("other signal words: " + others);

	}
	public  void initialize() {
//		String path = "CORENLP_INPUT/signal_1.txt";
		if(negatives != null) return;
		negatives = new HashSet<String>();
		others = new HashSet<String>();
		HashSet<String> current = negatives;
		utility util = new utility();
//		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
		try(BufferedReader br = getResourceAsBufReader("signal_1.txt")) {
		    String line;
		    while ((line = br.readLine()) != null) {
		       if(line.trim().startsWith("//")) {
		    	   line = line.substring(2);
		    	   switch(line) {
		    	   case "negative":
		    		   current = negatives;
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
		negatives.addAll(getNegativeKeywordsFromFile());
	}
	public  boolean contain(String s) {
		if(negatives == null) initialize();
		return negatives.contains(s.toLowerCase().trim()) || others.contains(s.toLowerCase().trim());
	}
	public  HashSet<String> getAllKeywords() {
		HashSet<String> result = new HashSet<String>();
		if(negatives == null) initialize();
		result.addAll(negatives); result.addAll(others);
		return result;
	}
	
	public  HashSet<String> getNegativeKeywordsFromFile() {
		utility util = new utility();
		HashSet<String> all = new HashSet<String>();
		try(BufferedReader br = getResourceAsBufReader("negative-words.txt")) {
			  String line;
			    while ((line = br.readLine()) != null) {
			    	if(line.trim().startsWith(";")) continue;
			    	if(line.trim().isEmpty()) continue;
			    	all.add(line.trim());
			    }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return all;
	}
	public  BufferedReader getResourceAsBufReader(String fileName) {
//		ClassLoader classLoader = getClass().getClassLoader();
		//				File file = new File(classLoader.getResource("stopwords.txt").getFile());
		BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(fileName )));
		return br;
	}
}
