package pattern;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;

import utility.utility;

public class RelationKeywords {
	public static HashSet<String> verbs;
	public static HashSet<String> others;
	public  void initialize() {
//		String path = "CORENLP_INPUT/rel_1.txt";
		if(verbs != null) return;
		verbs = new HashSet<String>();
		others = new HashSet<String>();
		HashSet<String> current = verbs;
		utility util = new utility();
//		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
		try (BufferedReader br = getResourceAsBufReader("rel_1.txt")) {
		    String line;
		    while ((line = br.readLine()) != null) {
		       if(line.trim().startsWith("//")) {
		    	   line = line.substring(2);
		    	   switch(line) {
		    	   case "verbs":
		    		   current = verbs;
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
		if(verbs == null) initialize();
		return verbs.contains(s.toLowerCase().trim());
	}
	public  HashSet<String> getAllKeywords() {
		HashSet<String> all = new HashSet<>();
		if(verbs == null) initialize();
		if(verbs!= null) all.addAll(verbs);
		if(others!=null) all.addAll(others);
		return all;
	}
	public  BufferedReader getResourceAsBufReader(String fileName) {
//		ClassLoader classLoader = getClass().getClassLoader();
		//				File file = new File(classLoader.getResource("stopwords.txt").getFile());
		BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(fileName )));
		return br;
	}
}
