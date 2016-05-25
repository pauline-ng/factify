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
		ParseJSON parser = new ParseJSON();
//		parser.parseFromAFolderToMediaWiki();
//		parser.parseToMediaWiki(
//"D:\\GitHub\\n-projects-ws-repo-nonegit\\n-projects-ws-repo-nonegit\\nonegit-BEL_extractor-test\\output\\ieSurvey.pdf_facts.json",
//"D:\\GitHub\\n-projects-ws-repo-nonegit\\n-projects-ws-repo-nonegit\\nonegit-BEL_extractor-test\\output\\ieSurvey.pdf_facts.jsonToMediaWikiTxt"
//);
//		parser.parseFromAFolderToTxt();
//		parser.parseFromAFolderToMediaWiki();
		parser.analyze();
//		if(false){
//			try {
//				utility util = new utility();
//				JSONParser parser = new JSONParser();
//				String str = util.readFromFile("D:\\GitHub\\n-projects-ws-repo-nonegit\\n-projects-ws-repo-nonegit\\nonegit-BEL_extractor-test\\output_BEL_extractor\\cbdgmlu_.pdf_facts_test.json");
//				JSONArray array = (JSONArray) parser.parse(str);
//				for(int i = 0; i < array.size(); i++) {
//					JSONObject obj = (JSONObject) array.get(i);
//					if(obj.get("type").equals("acronyms")) {
//					HashMap<String, String> acronyms = (HashMap<String, String>) obj;
//					for(String s : acronyms.keySet()) {
//						Debug.set(DEBUG_CONFIG.debug_acronym, true);
//						Debug.println(s + "\t" + acronyms.get(s), DEBUG_CONFIG.debug_acronym);
//					}
//					}
//				}
//			} catch (ParseException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//		}
	
	}
	
	public boolean parseToTxt(String inputJson, String outputPath) {
//		String filePath = "D:\\GitHub\\n-projects-ws-repo-nonegit\\n-projects-ws-repo-nonegit\\nonegit-BEL_extractor-test\\output_BEL_extractor\\cbdgmlu_.pdf_facts.jason";
		String str = utility.readFromFile(inputJson);
//		String output = "D:\\crowdsourcingPlatform\\cbdgmlu_.pdf_facts.txt";
		Object obj=JSONValue.parse(str);
		JSONArray array=(JSONArray)obj;
		if(array == null) {
			Debug.println("Invalid JSON file!", DEBUG_CONFIG.debug_error);
			return false;
		}
		utility.writeFile(outputPath,"",false);
		String content = "";
		for(int i = 0; i < array.size(); i++) {
			JSONObject fact = (JSONObject) array.get(i);
			if(fact.get("type").equals("Sentence")) {
				utility.writeFile(outputPath, "***" + fact.get("sentence") + "\r\n", true);
				utility.writeFile(outputPath, fact.get("fact") + "\r\n\r\n", true);
			}
			if(fact.get("type").equals("SectionTitle")) {
				utility.writeFile(outputPath, fact.get("sectionTitle") + "\r\n", true);
			}
			if(fact.get("type").equals("Paragraph Break")) {
				utility.writeFile(outputPath, "---new paragraph--------------------------\r\n", true);
			}
		}
		utility.writeFile(outputPath,content,true);
		return true;
	}
	public void parseToMediaWiki(String inputJson, String outputPath) {
		String filePath = inputJson;
//		String filePath = "D:\\GitHub\\n-projects-ws-repo-nonegit\\n-projects-ws-repo-nonegit\\nonegit-BEL_extractor-test\\output\\resultOfTestingPDFs3\\1hgs79.pdf_facts.jason";
		String str = utility.readFromFile(filePath);
//		String output = "D:\\crowdsourcingPlatform\\test_facts.txt";
		String output = outputPath;
		Object obj=JSONValue.parse(str);
		JSONArray array=(JSONArray)obj;
		utility.writeFile(output,"",false);
		String content = "== Meta Info == \r\n";
		for(int i = 0; i < array.size(); i++) {
			JSONObject fact = (JSONObject) array.get(i);
			if(fact.get("type").equals("paper")){
				String paperPath = (String) fact.get("path");
				int index1 = paperPath.lastIndexOf("\\");
				if(index1 >= 0) paperPath = paperPath.substring(index1 + "\\".length());
				else if( paperPath.lastIndexOf("/") >= 0) 
					paperPath = paperPath.substring(index1 + "/".length());
				String doi = (String) fact.get("doi");
//				content += "{{Publication Content";
//				content += "Acronyms:\r\n" + fact.get("acronyms") + "\r\n";
//				content += "Frequent NGrams: \r\n" + fact.get("freq ngrams") + "\r\n";
				content += "\r\nDOI: " + doi + "\r\n";
				content += "\r\nPath: " + paperPath + "\r\n";
			}
//			if(fact.get("type").equals("acronyms")) {
//				HashMap<String, String> acronyms = (HashMap<String, String>) fact;
//				if(acronyms.keySet().size() > 0) {
//					content += "\r\nAcronyms:\r\n";
//				}
//				for(String s : acronyms.keySet()) {
//					content += "* " + s + " => " + acronyms.get(s) + "\r\n";
//				}
//			}
//			if(fact.get("type").equals("freq ngrams")) {
//				JSONArray values = (JSONArray) fact.get("values");
//				if(values.size() > 0) {
//					content += "\r\n == freqent ngrams: ==  \r\n";
//				}
//				for(int j = 0; j < values.size(); j++) {
//					content += values.get(j).toString() + "  \r\n";
//				}
//			}
 		}
//		content += "== Methods ==   \r\n";
		for(int i = 0; i < array.size(); i++) {
			JSONObject fact = (JSONObject) array.get(i);
			
			if(fact.get("type").equals("Sentence")) {
//				content += fact.get("sentence");
				content += fact.get("fact") + "\r\n";
			}
			if(fact.get("type").equals("SectionTitle")) {
				content += "== " + fact.get("sectionTitle") + " ==  \r\n";
			}
			if(fact.get("type").equals("Paragraph Break")) {
				content += "---------------------------------------------------------------------\r\n";
			}
		}
		utility.writeFile(output,content,true);

		//		  JSONObject obj2=(JSONObject)array.get(1);
		//		  System.out.println("======field \"1\"==========");
		//		  System.out.println(obj2.get("1"));    
	}
	public boolean analyze_onefile(String inputJson) {
		String filePath = inputJson;
		String str = utility.readFromFile(filePath);
		Object obj=JSONValue.parse(str);
		JSONArray array=(JSONArray)obj;
		for(int i = 0; i < array.size(); i++) {
			JSONObject fact = (JSONObject) array.get(i);
			if(fact.get("type").equals("paper")){
				String doi = (String) fact.get("doi");
				if(doi.equals("NULL")) return false;
				return true;
			}
				
 		}
		return false;
	}
	public void parseFromAFolderToMediaWiki() {
		String input = "D:\\GitHub\\n-projects-ws-repo-nonegit\\n-projects-ws-repo-nonegit\\nonegit-BEL_extractor-test\\output\\resultOfTestingPDFs5\\";
		String output = "D:\\GitHub\\n-projects-ws-repo-nonegit\\n-projects-ws-repo-nonegit\\nonegit-BEL_extractor-test\\output\\resultOfTestingPDFs5\\mediawiki\\";
		File[] files = (new File(input)).listFiles();
		for(File file : files) {
			if(file.getName().endsWith(".json")) {
				parseToMediaWiki(file.getAbsolutePath(), output + file.getName() + ".txt");
			}
		}
	}
	public void parseFromAFolderToTxt() {
		String input = "D:\\GitHub\\n-projects-ws-repo-nonegit\\n-projects-ws-repo-nonegit\\nonegit-BEL_extractor-test\\output\\resultOfTestingPDFs5\\";
		String output = "D:\\GitHub\\n-projects-ws-repo-nonegit\\n-projects-ws-repo-nonegit\\nonegit-BEL_extractor-test\\output\\resultOfTestingPDFs5\\txt\\";
		File[] files = (new File(input)).listFiles();
		for(File file : files) {
			if(file.getName().endsWith(".json")) {
				parseToTxt(file.getAbsolutePath(), output + file.getName() + ".txt");
			}
		}
	}
	public void analyze() {
		String input = "D:\\GitHub\\n-projects-ws-repo-nonegit\\n-projects-ws-repo-nonegit\\nonegit-BEL_extractor-test\\output\\resultOfTestingPDFs5\\";
		File[] files = (new File(input)).listFiles();
		int total = 0;
		int withDOI = 0;
		for(File file : files) {
			if(file.getName().endsWith(".json")) {
				total++;
				if(analyze_onefile(file.getAbsolutePath())) {
					withDOI++;
				}else {
					String prefix = "D:\\huangxcwd\\Data\\reddit\\odesk\\allpapers\\";
					String fileName = file.getName();
					fileName = fileName.substring(0, fileName.length() - "_facts.json".length());
					utility.copyFile(prefix + fileName, input + "//withoutdoi//" + fileName);
				}
			}
		}
		System.out.println("total: " + total + "; withDOI: " + withDOI);
	}
}
