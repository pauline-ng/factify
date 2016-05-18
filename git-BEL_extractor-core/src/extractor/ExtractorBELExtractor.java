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
package extractor;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nlp.Sequence;
import nlp.StanfordNLPLight;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import pattern.Acronym;
import pattern.NGrams;
import pattern.RootMatcher;
import pdfStructure.PDF;
import pdfStructure.Paragraph;
import utility.Debug;
import utility.utility;
import utility.Debug.DEBUG_CONFIG;
import knowledge_model.C_Facts;
import knowledge_model.S_Facts;
import PDFconverter.PDFConverter;

/**
 * This is the main class for fact extractor
 * Use ExtractorBELExtractor.examplePDFExtractor_JSON(args);
 */
public class ExtractorBELExtractor {
	
	public static void main(String[]  args) {
		int error = examplePDFExtractor_JSON(args);
		Debug.println("Finished with errorcode " + error, DEBUG_CONFIG.debug_error);
		//System.exit(error);//The system has to exit with code 0, otherwise zotero would see it as failed.
	}
	
	/**
	 * 
	 * @param args
	 * 0: path
	 * 1: output_dir
	 * 2: debug_dir
	 * 3: matcher file (by default: RuleMatcher.json)
	 * 4: output_log
	 * 5: output_facts file path: or "MD5"
	 * @param output
	 * @return ErrorCode:
	 * -1: input parameter error 
	 * 0: input file not exist; 
	 * 1: succeeded
	 * 2: PDF Converter Failed
	 * 3: PDF Converter succeeded, but no body text (or section heading)
	 * 4: Facts Exists.
	 */
	public static int examplePDFExtractor_JSON(String...args) {
		{
			String[] javaVersionElements = System.getProperty("java.runtime.version").split("\\.");//1.8.0_45-b14
			try {
				int major = Integer.parseInt(javaVersionElements[1]);
				if (major < 8) System.exit(-33);
			}
			catch (Exception e ){
				
			}
		}
		if(args.length < 5) {
			Debug.println("Please input PDF path, output directory, debug directory, matcher file path, and debug_log file!", DEBUG_CONFIG.debug_error);
			Debug.println("*If debug_log=\"\", then debug info will print to the screen.", DEBUG_CONFIG.debug_error);
			Debug.println("*Please add slash to folder path.", DEBUG_CONFIG.debug_error);
			
			Debug.println("Parameters are :" , DEBUG_CONFIG.debug_error);
			for(String s : args) Debug.println(s, DEBUG_CONFIG.debug_error);
			return -1;
		}
		{
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
				Calendar cal = Calendar.getInstance();
				Debug.debugFile = args[4];
				if(Debug.debugFile.trim().equals("")) {
					Debug.debugFile = null;
					Debug.init();
				}else {
					Debug.init();
				}
				Debug.print("========" + dateFormat.format(cal.getTime()) + "==========\r\n", DEBUG_CONFIG.debug_timeline);
		}
		String path = args[0];
		File file = new File(path);
		if (!file.exists()) {
			Debug.print("Input File " + path + " does not exist!", DEBUG_CONFIG.debug_error);
			return 0;
		}
		String output_dir = args[1];
		
		String debug_dir = args[2];
		
		String matcherFile = args[3];
		{
			File file_temp = new File(matcherFile);
			if(!file_temp.exists() || !file_temp.isFile()) {
				Debug.println("Fatal Error: No matcher file (RuleMatcher.json) is found in path " + matcherFile + "!",DEBUG_CONFIG.debug_error );
				Debug.println("Input parameters are " , DEBUG_CONFIG.debug_error);
				for(String s : args)
					Debug.println(s , DEBUG_CONFIG.debug_error);
				return -1;
			}
		}
		String fact_file = output_dir + file.getName() + "_facts.json";
		if(args.length > 5) {
			if(args[5].trim().equals("MD5")) {
				String facts_name = "";
				facts_name = utility.MD5(path);
				if(facts_name != null) fact_file = output_dir + facts_name + "_facts.json";
			}
			else fact_file = args[5];
		}
		JSONArray factsToOutput = new JSONArray();
		PDFConverter converter = new PDFConverter();
		PDF pdf =  converter.run(file, file.getName(), output_dir, debug_dir);
		if(pdf == null) {
			Debug.println("PDF Converter Failed!",DEBUG_CONFIG.debug_error);
			Debug.println("File Path: " + path,DEBUG_CONFIG.debug_error);
			return 2;
		}
		HashMap<Paragraph, S_Facts> paraToFacts = new HashMap<Paragraph, S_Facts>();
		HashMap<Paragraph, List<Sequence>> paraToSequence = new HashMap<Paragraph, List<Sequence>>();
		List<Sequence> allSequences = new ArrayList<Sequence>();
		{
			for(Paragraph para : pdf.body_and_heading) {
				//					Debug.println("**Section " + i);
				if(para.isHeading()) continue;
				List<Sequence> para_seq = StanfordNLPLight.INSTANCE.textToSequence(para.text, -1, -1, -1, true);
				paraToSequence.put(para, para_seq);
				allSequences.addAll(para_seq);
			}
			
		}
		NGrams ngram = new NGrams();
		Map<String, Sequence> acronyms = Acronym.findAcronyms(allSequences);
		List<Sequence> freSeq = ngram.getFreqSequences(allSequences);
		HashSet<Sequence> freSeq_ = new HashSet<Sequence>(); freSeq_.addAll(freSeq);
		RootMatcher pat = new RootMatcher();
		if(!pat.readMacher(matcherFile)) {
			return -1;
		}
		Debug.print("****Start pattern matching****", DEBUG_CONFIG.debug_timeline);
		for(int i = 0; i < pdf.body_and_heading.size(); i++) {
			Paragraph para = pdf.body_and_heading.get(i);
			if(para.isHeading()) continue;
			Debug.println("Section " + i,DEBUG_CONFIG.debug_temp);
			C_Facts cFact = pat.parsePara(paraToSequence.get(para), freSeq_, acronyms, para.pages);
			if(cFact != null) { 
				S_Facts sFact = new S_Facts(cFact);
				sFact.mergeFacts();
				paraToFacts.put(para, sFact);
			}
		}
		{
			 JSONObject obj=new JSONObject();
			 obj.put("type", "paper");
			 obj.put("path", path);
			 obj.put("doi", (pdf.doi == null ? "NULL" : pdf.doi));
			 factsToOutput.add(obj);
			 {
				 HashMap<String, String> acronyms_string = new HashMap<String,String>();
				 for(String s : acronyms.keySet()) {
					 acronyms_string.put(s, acronyms.get(s).sourceString);
				 }
				 JSONObject acro_json = new JSONObject(acronyms_string);
				 acro_json.put("type", "acronyms");
				 factsToOutput.add(acro_json);
			 }
			 {
				 JSONArray freqNgrams = new JSONArray();
				 for(Sequence seq : freSeq) {
					 JSONObject oneFreq = new JSONObject();
					 oneFreq.put("value", seq.sourceString);
					 oneFreq.put("freq", seq.getAbsoluteFreq());
					 freqNgrams.add(oneFreq);
				 }
				 JSONObject acro_freqngram = new JSONObject();
				 acro_freqngram.put("type", "freq ngrams");
				 acro_freqngram.put("values", freqNgrams);
				 factsToOutput.add(acro_freqngram);
			 }
			}
		{//write candidate titles:
			JSONObject candidateTitles = new JSONObject();
			
			JSONArray candidateTitle = new JSONArray();
			for(int i = 0; i < pdf.candidateTitle.size(); i++) {
				String decor = pdf.candidateTitle.get(i).text;
				if(decor.trim().length() > 0) {
					StringTokenizer st = new StringTokenizer(decor, " ");
					int count_words = 10;
					String decor_truncated = "";
					while (st.hasMoreTokens() && count_words > 0) {
						decor_truncated += st.nextToken() + " ";
						count_words--;
					}
					candidateTitle.add(decor_truncated);
				}
			}
			candidateTitles.put("type", "Titles");
			candidateTitles.put("value", candidateTitle);
			factsToOutput.add(candidateTitles);
		}
		int counter_facts = 1;
		for(int i = 0; i < pdf.body_and_heading.size(); i++) {
			Paragraph para = pdf.body_and_heading.get(i);
			if(para.isHeading()){
			 JSONObject obj=new JSONObject();
			 obj.put("type", "SectionTitle");
			 obj.put("sectionTitle", para.getHeadingText());
			 obj.put("PageRange", para.pages.toString());
			 factsToOutput.add(obj);
			 continue;
			}
			if(paraToFacts.get(para) == null) {
				
			}else {
				factsToOutput.addAll(paraToFacts.get(para).toJSON(counter_facts));
				 JSONObject obj=new JSONObject();
				 obj.put("type", "Paragraph Break");
				 factsToOutput.add(obj);
				counter_facts += paraToFacts.get(para).getSize();
			}
		}
		{//write tables
			 List<String> htmlTables_string = pdf.htmlTables_string;
			 List<String> htmlTables_caption = pdf.htmlTables_caption;
			for(int order = 0; order < htmlTables_string.size(); order++) {
				 JSONObject obj=new JSONObject();
				 obj.put("type", "Table");
				 obj.put("htmlTable", htmlTables_string.get(order));
				 obj.put("caption", getTableCaptionPrefix(htmlTables_caption.get(order)));
				 obj.put("order", order);
				 
				 factsToOutput.add(obj);
			}
			
		}
		{//write decorations:
			JSONObject decoration = new JSONObject();
			
			JSONArray decorations = new JSONArray();
			for(int i = 0; i < pdf.noneBodynorHeading.size(); i++) {
				String decor = pdf.noneBodynorHeading.get(i).text;
				if(decor.trim().length() > 0) {
					StringTokenizer st = new StringTokenizer(decor, " ");
					int count_words = 10;
					String decor_truncated = "";
					while (st.hasMoreTokens() && count_words > 0) {
						decor_truncated += st.nextToken() + " ";
						count_words--;
					}
					decorations.add(decor_truncated);
				}
			}
			decoration.put("type", "decorations");
			decoration.put("value", decorations);
			factsToOutput.add(decoration);
		}
		
		utility.writeFile(fact_file, factsToOutput.toJSONString(), false);
		if(pdf.body_and_heading.size() == 0) return 3;
		return 1;
	}
	
	private static String getTableCaptionPrefix(String caption) {
		//test cases: 
		/*
		 * 
		getTableCaptionPrefix("table 1");// "table 1"
		getTableCaptionPrefix("table");// "table"
		getTableCaptionPrefix("table 1.1.1.1");//"table 1.1.1.1"
		getTableCaptionPrefix("table 1.1");//"table 1.1"
		getTableCaptionPrefix("Table 1.1.1.");//"Table 1.1.1."
		getTableCaptionPrefix("table 1.1 2.1");//"table 1.1 2.1"
		 */
		String result = caption.trim();
		String prefix = caption.toLowerCase().trim();
		String pattern = "table((\\s|\\.)\\d+)*";
		
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(prefix);
		if(m.find()) {
			int start = m.start(); int end = m.end();
			if(start == 0) {
				result = result.substring(start, end);
			}else result = "";
		}else result = "";
		return result;
	}
}
