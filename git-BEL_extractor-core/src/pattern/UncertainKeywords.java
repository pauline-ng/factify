package pattern;

/**
 * Not in use
 */
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;

import utility.utility;

public class UncertainKeywords {
	public static HashSet<String> uncertainties;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		UncertainKeywords uk = new UncertainKeywords();
		uk.initialize();
		System.out.println(uncertainties);
	}
	public  void initialize() {
//		String path = "CORENLP_INPUT/signal_1.txt";
		if(uncertainties != null) return;
		uncertainties = new HashSet<String>();
		
		HashSet<String> current = uncertainties;
		utility util = new utility();
//		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
		try(BufferedReader br = getResourceAsBufReader("uncertainty-words.txt")) {
		    String line;
		    while ((line = br.readLine()) != null) {
		       if(line.trim().startsWith("//")) continue;
		       else current.add(line.trim());
		    }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public  HashSet<String> getAllKeywords() {
		if(uncertainties == null) initialize();
		return uncertainties;
	}
	public  BufferedReader getResourceAsBufReader(String fileName) {
//		ClassLoader classLoader = getClass().getClassLoader();
		//				File file = new File(classLoader.getResource("stopwords.txt").getFile());
		BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(fileName )));
		return br;
	}
	
}
