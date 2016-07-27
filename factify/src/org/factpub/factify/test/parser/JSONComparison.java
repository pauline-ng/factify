package org.factpub.factify.test.parser;

import java.io.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import utility.Utility;

/**
 * 
 * For debug
 *
 */
public class JSONComparison {

	public static void main(String[] args) {
		JSONComparison c = new JSONComparison();
		String folder1 = "D:\\GitHub\\n-projects-ws-repo-nonegit\\n-projects-ws-repo-nonegit\\nonegit-BEL_extractor-test\\bash\\0617\\test_output\\";
		String folder2 = 			
				"D:\\GitHub\\n-projects-ws-repo-nonegit\\n-projects-ws-repo-nonegit\\nonegit-BEL_extractor-test\\bash\\factExtractor_15\\test_output\\";
			File[] allFiles = new File(folder1).listFiles();
		for(File src : allFiles) {
			String dest = folder2 + src.getName();
			if(new File(dest).exists()) {
				System.out.println(src.getName() + "\t" + c.compare(src.getAbsolutePath(), dest));
			}
		}
		
			}
	public boolean compare(String file1, String file2) {
		JSONArray array1 = null;
		JSONArray array2 = null;
		{
			String str = Utility.readFromFile(file1);
			Object obj=JSONValue.parse(str);
			array1 =(JSONArray)obj;
		}{
			String str = Utility.readFromFile(file2);
			Object obj=JSONValue.parse(str);
			array2 =(JSONArray)obj;
		}
		for(int i = 0; i < Math.max(array1.size(), array2.size()); i++) {
			JSONObject s1 = (JSONObject) array1.get(i);
			JSONObject s2 = (JSONObject) array2.get(i);
			if(!s1.equals(s2)) {
				if(s1.get("type").equals("freq ngrams") && s2.get("type").equals("freq ngrams")) continue;
				if(s1.keySet().contains("path")) continue;
				System.out.println("ERROR!" + file2);
				System.out.println(s1.toString());
				System.out.println("====================================");
				System.out.println(s2.toString());
				System.exit(0);
				return false;
			}
		}
		return true;
	}

}
