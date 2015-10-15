package parser;

import java.io.File;
//import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import utility.Debug;
import utility.Debug.DEBUG_CONFIG;
import utility.utility;

public class ParseJSON {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Object obj=JSONValue.parse("test");
		JSONArray array = (JSONArray) obj;
		ParseJSON parser = new ParseJSON();
//		parser.parseToMediaWiki("","");
		parser.parseFromAFolder();
	}
	
	public boolean parseToTxt(String inputJson, String outputPath) {
		utility util = new utility();
		String filePath = "D:\\GitHub\\n-projects-ws-repo-nonegit\\n-projects-ws-repo-nonegit\\nonegit-BEL_extractor-test\\output_BEL_extractor\\cbdgmlu_.pdf_facts.jason";
		String str = util.readFromFile(filePath);
		String output = "D:\\crowdsourcingPlatform\\cbdgmlu_.pdf_facts.txt";
		Object obj=JSONValue.parse(str);
		JSONArray array=(JSONArray)obj;
		if(array == null) {
			Debug.println("Invalid JSON file!", DEBUG_CONFIG.debug_error);
			return false;
		}
		util.writeFile(output,"",false);
		String content = "";
		for(int i = 0; i < array.size(); i++) {
			JSONObject fact = (JSONObject) array.get(i);
//			if(fact.get("type").equals("paper")){
//				String paperPath = (String) fact.get("path");
//				int index1 = paperPath.indexOf("\\");
//				if(index1 >= 0) paperPath = paperPath.substring(index1 + "\\".length());
//				else if( paperPath.indexOf("/") >= 0) 
//					paperPath = paperPath.substring(index1 + "/".length());;
//				content = "{{Publication Information\r\n |" + "id=" + paperPath + "}}";
//				content += "{{Publication Content";
//				content += "|Acronyms=" + fact.get("acronyms");
//				content += "|Frequent NGrams=" + fact.get("freq ngrams");
//			}
			
			if(fact.get("type").equals("Sentence")) {
//				System.out.println(fact.get("sentence"));
//				System.out.println(fact.get("fact"));
//				content += fact.get("sentence");
//				content += fact.get("fact") + "\r\n";
				
				util.writeFile(output, "***" + fact.get("sentence") + "\r\n", true);
				util.writeFile(output, fact.get("fact") + "\r\n\r\n", true);
				
				//System.out.println(fact.get("details"));
			}
			if(fact.get("type").equals("SectionTitle")) {
//				content += "|" + fact.get("sectionTitle") + "=";
				util.writeFile(output, fact.get("sectionTitle") + "\r\n", true);
			}
			if(fact.get("type").equals("Paragraph Break")) {
				util.writeFile(output, "---new paragraph--------------------------\r\n", true);
			}
		}
//		content += "}}";
		util.writeFile(output,content,true);
		return true;

		//		  JSONObject obj2=(JSONObject)array.get(1);
		//		  System.out.println("======field \"1\"==========");
		//		  System.out.println(obj2.get("1"));    
	}
	public void parseToMediaWiki(String inputJson, String outputPath) {
		utility util = new utility();
		String filePath = inputJson;
//		String filePath = "D:\\GitHub\\n-projects-ws-repo-nonegit\\n-projects-ws-repo-nonegit\\nonegit-BEL_extractor-test\\output\\resultOfTestingPDFs3\\1hgs79.pdf_facts.jason";
		String str = util.readFromFile(filePath);
//		String output = "D:\\crowdsourcingPlatform\\test_facts.txt";
		String output = outputPath;
		Object obj=JSONValue.parse(str);
		JSONArray array=(JSONArray)obj;
		util.writeFile(output,"",false);
		String content = "{{Publication Content\r\n";
		for(int i = 0; i < array.size(); i++) {
			JSONObject fact = (JSONObject) array.get(i);
			if(fact.get("type").equals("paper")){
				String paperPath = (String) fact.get("path");
				int index1 = paperPath.lastIndexOf("\\");
				if(index1 >= 0) paperPath = paperPath.substring(index1 + "\\".length());
				else if( paperPath.lastIndexOf("/") >= 0) 
					paperPath = paperPath.substring(index1 + "/".length());;
				content += "|Background Concepts= " + "\r\n id: " + paperPath + "\r\n";
//				content += "{{Publication Content";
//				content += "Acronyms:\r\n" + fact.get("acronyms") + "\r\n";
//				content += "Frequent NGrams: \r\n" + fact.get("freq ngrams") + "\r\n";
			}
		}
		content += "|Methods=\r\n";
		for(int i = 0; i < array.size(); i++) {
			JSONObject fact = (JSONObject) array.get(i);
			
			if(fact.get("type").equals("Sentence")) {
//				content += fact.get("sentence");
				content += fact.get("fact") + "\r\n";
			}
			if(fact.get("type").equals("SectionTitle")) {
				content += "== " + fact.get("sectionTitle") + " == \r\n";
			}
			if(fact.get("type").equals("Paragraph Break")) {
				content += "---------------------------------------------------------------------\r\n";
			}
		}
		content += "}}";
		util.writeFile(output,content,true);

		//		  JSONObject obj2=(JSONObject)array.get(1);
		//		  System.out.println("======field \"1\"==========");
		//		  System.out.println(obj2.get("1"));    
	}

	public void parseFromAFolder() {
		String input = "D:\\GitHub\\n-projects-ws-repo-nonegit\\n-projects-ws-repo-nonegit\\nonegit-BEL_extractor-test\\output\\resultOfTestingPDFs3\\";
		String output = "D:\\GitHub\\n-projects-ws-repo-nonegit\\n-projects-ws-repo-nonegit\\nonegit-BEL_extractor-test\\output\\resultOfTestingPDFs3\\mediawiki\\";
		File[] files = (new File(input)).listFiles();
		for(File file : files) {
			if(file.getName().endsWith(".jason")) {
				parseToMediaWiki(file.getAbsolutePath(), output + file.getName() + ".txt");
			}
		}
	}
}
