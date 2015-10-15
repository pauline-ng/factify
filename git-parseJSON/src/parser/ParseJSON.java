package parser;

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
		String filePath = "D:\\GitHub\\n-projects-ws-repo-nonegit\\n-projects-ws-repo-nonegit\\nonegit-BEL_extractor-test\\output_BEL_extractor\\cbdgmlu_.pdf_facts.jason";
		String str = util.readFromFile(filePath);
		String output = "D:\\crowdsourcingPlatform\\cbdgmlu_.pdf_facts.txt";
		Object obj=JSONValue.parse(str);
		JSONArray array=(JSONArray)obj;
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

		//		  JSONObject obj2=(JSONObject)array.get(1);
		//		  System.out.println("======field \"1\"==========");
		//		  System.out.println(obj2.get("1"));    
	}

}
