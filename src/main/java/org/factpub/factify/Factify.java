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
package org.factpub.factify;

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

import org.factpub.factify.knowledge_model.C_Facts;
import org.factpub.factify.knowledge_model.S_Facts;
import org.factpub.factify.nlp.Sequence;
import org.factpub.factify.nlp.StanfordNLPLight;
import org.factpub.factify.pattern.Acronym;
import org.factpub.factify.pattern.NGrams;
import org.factpub.factify.pattern.RootMatcher;
import org.factpub.factify.pdf.converter.PDFConverter;
import org.factpub.factify.utility.Debug;
import org.factpub.factify.utility.Debug.DEBUG_CONFIG;
import org.factpub.factify.utility.Utility;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import at.knowcenter.code.pdf.structure.PDF;
import at.knowcenter.code.pdf.structure.Paragraph;

/**
 * This is the main class for fact extractor
 */
public class Factify {
	
	public static void main(String[]  args) {
		
		/*
		 * Use this main function for debugging purpose for the time being.
		 * Ideally, Unit test should be prepared as /src/test/java/org.factpub.factify.FactifyTest
		 */
		
//		/* sample pdf file 1 */
//		String input_folder = "pdf\\";
//		String pdf_file = "DOI10.1093nargkg509_SIFT.pdf";
	//	
//		/* sample pdf file 2 */
		String input_folder = "pdf\\";
		String pdf_file = "DOI10.1146annurev.genom.7.080505.115630_PredictingTheEffects.pdf";
	//

//		/* sample pdf file 3 */
//		String input_folder = "pdf" + File.separator + "incorrectDOI" + File.separator;
//		String pdf_file = "DOI(10.1126science.1240729)_BetaCaMKII_wrong_is_(10.1126science.1236501).pdf";
		
		/* sample pdf file 4 */
//		String input_folder = "pdf" + File.separator + "incorrectDOI" + File.separator;
//		String pdf_file = "DOI(10.1053j.gastro.2009.04.032)_EvidenceForTheRole_wrong_is_(10.1053j.gastro.2009.04.022).pdf";
		
		String[] parameters = new String[6];
		parameters[0] = input_folder + pdf_file;
		parameters[1] = input_folder;
		parameters[2] = input_folder;
		parameters[3] = "Rule_INPUT" + File.separator + "RuleMatcher.json";
		parameters[4] = "";
		parameters[5] = "MD5";
		
		System.out.println(parameters[0]);
		System.out.println(parameters[1]);
		System.out.println(parameters[2]);
		System.out.println(parameters[3]);
		System.out.println(parameters[4]);
		System.out.println(parameters[5]);
		
		int error = runFactify(parameters);
		Debug.println("Finished with errorcode " + error, DEBUG_CONFIG.debug_error);
	}
	
	/**
	 * 
	 * @param args Input parameters <br>
	 * 0: Input PDF Path <br>
	 * 1: Output directory <br>
	 * 2: Debug directory <br>
	 * 3: Matcher file path (by default: RuleMatcher.json) <br>
	 * 4: Output_log <br>
	 * 5: Output_facts file path: or "MD5" <br>
	 * @return ErrorCode<br>
	 * -1: input parameter error <br>
	 * 0: Input file does not exist<br>
	 * 1: Success<br>
	 * 2: PDF Converter failed<br>
	 * 3: PDF Converter succeeded, but no body text (or section heading)<br>
	 * 4: Facts exist<br>
	 */
	
	public static int runFactify(String...args) {
		
		/*
		 * Step0-0: Check JRE version
		 */
		{
			String[] javaVersionElements = System.getProperty("java.runtime.version").split("\\.");//1.8.0_45-b14
			try {
				int major = Integer.parseInt(javaVersionElements[1]);
				if (major < 8) System.exit(-33);
			}
			catch (Exception e ){
				
			}
		}
		
		/*
		 * Step0-1: Check if the arguments are okay
		 */
		if(args.length < 5) {
			Debug.println("Please input PDF path, output directory, debug directory, matcher file path, and debug_log file!", DEBUG_CONFIG.debug_error);
			Debug.println("*If debug_log=\"\", then debug info will print to the screen.", DEBUG_CONFIG.debug_error);
			Debug.println("*Please add slash to folder path.", DEBUG_CONFIG.debug_error);
			
			Debug.println("Parameters are :" , DEBUG_CONFIG.debug_error);
			for(String s : args) Debug.println(s, DEBUG_CONFIG.debug_error);
			return -1;
		}
		
		/*
		 * Step0-2: Configure Debug file
		 */
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
		
		
		/*
		 * Step0-3: Check each arguments.
		 */
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
				facts_name = Utility.MD5(path);
				if(facts_name != null) fact_file = output_dir + facts_name + "_facts.json";
			}
			else fact_file = args[5];
		}
		
		
		/*
		 * Step1: pdf-extraction
		 * Given PDF file is parsed and structuralized by PdfExtractionPipeline.
		 * The extracted texts are organized as a PDF instance.
		 */
		PDFConverter converter = new PDFConverter();
		PDF pdf =  converter.run(file, file.getName(), output_dir, debug_dir);
		if(pdf == null) {
			Debug.println("PDF Converter Failed!",DEBUG_CONFIG.debug_error);
			Debug.println("File Path: " + path,DEBUG_CONFIG.debug_error);
			return 2;
		}
		
		/* Write by Xuenan Pi
		 * Solve the unexpected sentence break by pages or columns problem.
		 */
		else{
			Utility.sewBrokenSentence(pdf.body_and_heading);
		}
		
		/*
		 * Step2: Apply Stanford Core NLP to Paragraphs extracted in the previous stage.
		 */
		HashMap<Paragraph, S_Facts> paraToFacts = new HashMap<Paragraph, S_Facts>();
		HashMap<Paragraph, List<Sequence>> paraToSequence = new HashMap<Paragraph, List<Sequence>>();
		List<Sequence> allSequences = new ArrayList<Sequence>();
		{
			for(Paragraph para : pdf.body_and_heading) {
				//					Debug.println("**Section " + i);
				if(para.isHeading()) continue;
				List<Sequence> para_seq = StanfordNLPLight.INSTANCE.textToSequence(para.text, true);
				paraToSequence.put(para, para_seq);
				allSequences.addAll(para_seq);
			}
			
		}
		NGrams ngram = new NGrams();
		Map<String, Sequence> acronyms = Acronym.findAcronyms(allSequences);
		List<Sequence> freSeq = ngram.getFreqSequences(allSequences);
		HashSet<Sequence> freSeq_ = new HashSet<Sequence>(); freSeq_.addAll(freSeq);
		
		
		/*
		 * Step3: Apply Rule Matching based on the Rule_INPUT files 
		 */
		
		// prepare pattern matcher
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
		
		
		/*
		 * Step4: Create JSON object as an output. 
		 */
		JSONArray factsToOutput = new JSONArray();
		{
			 JSONObject obj=new JSONObject();
			 obj.put("type", "paper");
			 obj.put("path", path);
			 			 
			 
			 /*
			  * Modified by Sun SAGONG on 04AUG2016
			  * Print ArrayList<String> doi without the brackets [ ], which are printed by default.  
			  * 
			  */
			 
			 System.out.println(pdf.doi);
			 obj.put("doi", (pdf.doi == null ? "NULL" : pdf.doi));
			 //obj.put("doi", (pdf.doi == null ? "NULL" : pdf.doi.toString().substring(1, pdf.doi.toString().length() - 1)));
			 
			 
			 factsToOutput.add(obj);
			 {
				 HashMap<String, String> acronyms_string = new HashMap<String,String>();
				 for(String s : acronyms.keySet()) {
					 acronyms_string.put(s, acronyms.get(s).getSourceString());
				 }
				 JSONObject acro_json = new JSONObject(acronyms_string);
				 acro_json.put("type", "acronyms");
				 factsToOutput.add(acro_json);
			 }
			 {
				 JSONArray freqNgrams = new JSONArray();
				 for(Sequence seq : freSeq) {
					 JSONObject oneFreq = new JSONObject();
					 oneFreq.put("value", seq.getSourceString());
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
		
		//
		Utility.writeFile(fact_file, factsToOutput.toJSONString(), false);
		if(pdf.body_and_heading.size() == 0) return 3;
		
		/*
		 * End of runFactify
		 * If everything is okay, this returns 1.
		 */
		
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
