package extractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import nlp.Sequence;
import nlp.StanfordNLPLight;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pattern.Acronym;
import pattern.NGrams;
import pattern.RootMatcher;
import pdfStructure.PDF;
import pdfStructure.Paragraph;
import utility.Debug;
import utility.Span;
import utility.utility;
import utility.Debug.DEBUG_CONFIG;
import knowledge_model.C_Facts;
import knowledge_model.S_Facts;
import PDFconverter.PDFConverter;

public class ExtractorBELExtractor {
	
	public static void main(String[]  args) {
		int error = examplePDFExtractor_JSON(args);
		Debug.println("Finished with errorcode " + error, DEBUG_CONFIG.debug_error);
		//System.exit(error);//The system has to exit with code 0, otherwise zotero would see it as failed.
	}

//	private static String reg_Number = "\\d+";
	
//	public static void test() {
//		String path = "test\\cbdgmlu_\\cbdgmlu_.pdf";//test\\papers_20 top journals\\cav6twh_.pdf
//		PDFConverterState pdf = Refinement.extractStructrueFromPDF(path);
////		Refinement.writePDF(pdf, path + ".txt");
//		utility util = new utility();
////		System.exit(0);
////		util.writeFile("PMC_FILES\\BELExtractor/PMC1513127_allblocks.txt", "", false);
////		pdf.writeTextBlocks("PMC_FILES\\BELExtractor/PMC1513127_allblocks.txt", pdf.getAllTextBlocks());
//		Debug.println("----------------------------------------------------------------------------------------------------");
////		nlp = new StanfordNLP("tokenize, ssplit, pos");
//		
//		extract(pdf);
//		for(S_Facts fact : pdf.sfacts) {
////			fact.printFacts();
//			Debug.print("--Facts----------\r\n");
//			fact.printFacts();
////			util.writeFile(output_fact, "\r\n", true);
//		}
//		
//	}
	
//	public static void test_few() {
//		String path = "test\\papers_20 top journals\\";
//		String [] papers = {"cav6twh_","cbvw8z3_","cbsbghc_","cdj05e2_","cev3lne","cfifh46","cg9ces5_","ciimt4v"};//
//		for(String s : papers ) {
//			PDFConverterState pdf = Refinement.extractStructrueFromPDF(path + s + ".pdf");
//			//		Refinement.writePDF(pdf, "PMC_FILES\\BELExtractor/PMC1513127_pdf.txt");
//			utility util = new utility();
//			//		util.writeFile("PMC_FILES\\BELExtractor/PMC1513127_allblocks.txt", "", false);
//			//		pdf.writeTextBlocks("PMC_FILES\\BELExtractor/PMC1513127_allblocks.txt", pdf.getAllTextBlocks());
//			Debug.println(s+"----------------------------------------------------------------------------------------------------");
//			extract(pdf);
//			
//			String output_text = path + "result_round4/facts/" +  s + "_sfacts_text.txt";
//			String output_fact = path + "result_round4/facts/" +  s + "_sfacts.txt";
//			util.writeFile(output_fact,
//					"", false);
//			util.writeFile(output_text,
//					"", false);
//			for(S_Facts fact : pdf.sfacts) {
//			util.writeFile(output_text,
//					"---------Section " + fact.getSecNum() + "\t " + pdf.sections.get(fact.getSecNum()).getTitle(pdf) + "-----\r\n\r\n", true);
//			util.writeFile(output_text,
//					"--Para " + fact.getParaNum() + "\t " + pdf.sections.get(fact.getSecNum()).paragraphs.get(fact.getParaNum()).getText(pdf) + "-----\r\n\r\n", true);
//			}
//			for(S_Facts fact : pdf.sfacts) {
////				fact.printFacts();
//								util.writeFile(output_fact, "--Facts----------\r\n", true);
//				fact.writeFacts(output_fact);
//				util.writeFile(output_fact, "\r\n", true);
//			}
//			Debug.println(s + "------------------end of last one----------------------------------------------------------------------------------");
//
//		}
//		
//	}
	
//	public static void test_forLawyer() {
//			
//		String path = "test\\";
////		String [] papers = {"cav6twh_","cbvw8z3_","cbsbghc_","cdj05e2_","cev3lne","cfifh46","cg9ces5_","ciimt4v"};//
//		utility util = new utility();
//		List<File> allPDF = util.getAllValidPDFs(path, false, ".pdf");
////		for(String s : papers ) {
//		for(File file : allPDF) {
//			String s = file.getName().replace(".pdf", "");
//			if(!s.equals("cbautw9_")) continue;
//			PDFConverterState pdf = Refinement.extractStructrueFromPDF(path + s + ".pdf");
////			Refinement.writeTextPieces(pdf.getWordsByPage(), path + "info\\" + s + "_wordsByPage.txt" );
////			System.exit(0);
////			Refinement.writeTextPieces(pdf.getLinesByPage_Horizontal(), path + "info\\" + s +  "_linesByPage.txt" );
////			pdf.writeTextBlocks_showTextPieces(path + s + "_textPieces_inBlocks.txt");
////			pdf.writeTextBlocks(path + s + "_Blocks.txt");
////			pdf.writeTextBlocks_showTextPieces(path + s + "_textPieces_inBlocks.txt" , pdf.getAllTextBlocks());
////			System.exit(0);
////					Refinement.writePDF(pdf, path + "info\\" + s + "_pdf.txt");
////					util.writeFile( path + "info\\" + s + "_alltextblocks.txt", "", false);
////					pdf.writeTextBlocks( path + "info\\" + s + "_alltextblocks.txt", pdf.getAllTextBlocks());
////			Debug.println(pdf.sections.get(0).paragraphs.get(1).getText(pdf));
////			Debug.println(s+"----------------------------------------------------------------------------------------------------");
////			nlp = new StanfordNLP("tokenize, ssplit, pos");
//			
//			ArrayList<TextBlock> allTBs = pdf.getAllTextBlocks();
//			ArrayList<S_Facts> sfacts = extract_fromTextBlocks(pdf);
//			String output_text = path  +  s + "_sfacts_text.txt";
//			String output_fact = path  +  s + "_sfacts.txt";
//			util.writeFile(output_fact,
//					"", false);
//			util.writeFile(output_text,
//					"", false);
//			int secNum = -1;
////			for(S_Facts fact : pdf.sfacts) {
////			
////				if(secNum != fact.getSecNum()) {
////					secNum = fact.getSecNum();
////					util.writeFile(output_text,
////							"---------Section " + (fact.getSecNum() +1) + "\t " + pdf.sections.get(fact.getSecNum()).getTitle(pdf) + "-----\r\n\r\n", true);
////				}else 							util.writeFile(output_text,
////						"--Para " + (fact.getParaNum()+1) + "\t " + pdf.sections.get(fact.getSecNum()).paragraphs.get(fact.getParaNum()).getText(pdf) + "-----\r\n\r\n", true);
////			}
//			int i = 1;
////			pdf.writeTextBlocks_showTextPieces(output_text, allTBs);
//			i = 1;
//			for(S_Facts fact : sfacts) {
//				util.writeFile(output_fact, "-" + i + "-----------\r\n", true);
//				fact.writeFacts(output_fact);
//				util.writeFile(output_fact, "\r\n", true);
//				i++;
//			}
////			 secNum = -1;
////			for(S_Facts fact : pdf.sfacts) {
//////				fact.printFacts();
//////				util.writeFile(output_fact, "--Facts----------\r\n", true);
////				if(secNum != fact.getSecNum()) {
////					secNum = fact.getSecNum();
////					util.writeFile(output_fact, "---[Section: " + (fact.getSecNum()+1) + "]\t" + pdf.sections.get(secNum).getTitle(pdf) +  "----\r\n" , true);
////				}
////				util.writeFile(output_fact, "--[Para:" +  (fact.getParaNum() +1) + "" + "]----\r\n" , true);
////				fact.writeFacts(output_fact);
////				util.writeFile(output_fact, "\r\n", true);
////			}
//			Debug.println(s + "------------------end of last one----------------------------------------------------------------------------------");
////			System.exit(0);
//		}
//		
//	}
//	public static void extract(PDFConverterState pdf) {
//		for(int i = 0; i < pdf.sections.size(); i++) {
//			Section sec = pdf.sections.get(i);
////			if(isMethodSection(sec, pdf) || isResultSection(sec, pdf)) {//
////				Debug.println(sec.text);
////				Debug.println(sec.getTitle(pdf));
//				extractEXP(i,sec, pdf);
////			}
//		}
//	}
//	
	/**TODO not finished
	 * 1. convert all paragraphs to List<Sequence>
	 * 2. identify ngrams
	 * @param pdf
	 */
//	public static void preprocess(PDFConverterState pdf) {
//		if(nlp == null) nlp = new StanfordNLP("tokenize, ssplit, pos");
//		for(Section sec : pdf.sections) {
//			for(Paragraph para : sec.paragraphs) {
//				para.sequences = (ArrayList<Sequence>) nlp.textToSequence(para.getText(pdf), -1, pdf.sections.indexOf(sec), sec.paragraphs.indexOf(para), true);
//			}
//		}
//		
//	}
	
//	public static ArrayList<S_Facts> extract_fromTextBlocks(PDFConverterState pdf) {
//		ArrayList<TextBlock> textBlocks = pdf.getAllTextBlocks();
//		ArrayList<C_Facts> cfacts = new ArrayList<>();
//		ArrayList<S_Facts> sfacts = new ArrayList<S_Facts>();
//		
//		for(int i = 0; i < textBlocks.size(); i++) {
//			TextBlock tb = textBlocks.get(i);
//			C_Facts cfact = patterns.parsePara(tb.getText(pdf), true);
//			S_Facts sfact = new S_Facts(cfact);
//			sfact.mergeFacts();
//			sfacts.add(sfact);
//		}
//		return sfacts;
//	}
	public static void test___() throws IOException {
//		String path = "test\\for lawyer\\cbautw9\\test.txt";
//		String path = "test\\for lawyer\\cbautw9\\Fully_formated_1st_paper_Facts.txt";
		String path = "test\\for lawyer\\cbdgmlu\\Facts_2nd_paper_formatted.txt";
		utility util = new utility();
//		String content = util.readFromFile(path);
//		StringTokenizer st = new StringTokenizer(content, "\r\n");
		util.writeFile(path + "clean.txt", "", false);
//		while(st.hasMoreTokens()) {
//			String line = st.nextToken();
////			Debug.println(line);
//			if(line.startsWith("[") && line.endsWith("]")) {
//				Debug.println(line);
//				util.writeFile(path + "clean.txt", "*" + line.substring(1, line.length() - 1) + "\r\n\r\n", true);
//			}
//		}
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "ISO-8859-1"));
		String line;
		while ((line = br.readLine()) != null) {
		   // process the line.
			line = line.trim();
			if(line.startsWith("[") && line.endsWith("]")) {
				Debug.println(line,DEBUG_CONFIG.debug_temp);
				util.writeFile(path + "clean.txt", "* " + line.substring(1, line.length() - 1) + "\r\n", true);
			}else {
				if(line.equals("")) util.writeFile(path + "clean.txt", "\r\n", true);
				else util.writeFile(path + "clean.txt", line + "\r\n", true);
			}
			
		}
		br.close();
//		String s = "20–31";
//		Debug.println(s);
//		File fileDir = new File(path);
//		 
//		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileDir), "ISO-8859-1"));
//		Writer out = new BufferedWriter(new OutputStreamWriter(
//				new FileOutputStream(new File(path+"clean.txt")), "ISO-8859-1"));
//		String line;
//		while ((line = br.readLine()) != null) {
//		   // process the line.
//			line = line.trim();
//			Debug.println(line);
//			s= line;
//			for(int i = 0; i < s.length(); i++) {
//				char c = s.charAt(i);
//				if(c == '5') {
//					Debug.println("debuyg");
//				}
//			}
//			
//			out.append(line);
//			out.flush();
//		//	
//		}
//		Debug.println("test" + util.readFromFile(path));
//		out.append("test" + util.readFromFile(path));
//		out.flush();
		
//		
//		Debug.println();
	}
	
//	private static void extractEXP(int secNum, Section sec, PDFConverterState pdf) {
//		for(int i = 0; i < sec.paragraphs.size(); i++) {
//			Paragraph para = sec.paragraphs.get(i);
//			String text = para.getText(pdf);
//			{
////				text = text.replace(text.charAt(189), 'u');
////				Debug.println(Charset.defaultCharset());
////				Debug.println(text.charAt(189) + "\t" + text.charAt(190));
////				Debug.println(String.format("%04x", (int)Character.getNumericValue(text.charAt(189))) + "\t" + String.format("%04x", (int)Character.getNumericValue(text.charAt(190))));
////				for(int t = 0; t < text.length(); t++) {//t=189
////					char c = text.charAt(t);
////					Debug.println(t + "\t" + c);
////				}
//			}
////			Debug.println(text);
////			utility util = new utility();
//			
//			C_Facts c_facts = patterns.parsePara(text, true);
//			S_Facts s_Facts = new S_Facts(c_facts);
//			s_Facts.mergeFacts();
//			//				s_Facts.printFacts();
//			pdf.cfacts.add(c_facts);
//			pdf.sfacts.add(s_Facts);
//			
//		
//		}
//	}
	

	/**
	 * tested on 112 pmc open access files with accuracy 95/112
	 * @param sec
	 * @param pdf
	 * @return
	 */
//	public static boolean isMethodSection(Section sec, PDFConverterState pdf) {
//		String secTitle = sec.getTitle(pdf).toLowerCase();
//		if(secTitle.contains("method") || secTitle.contains("material") || secTitle.contains("experiment") || secTitle.contains("data")
//				|| secTitle.contains("treatment")) 
//			return true;
//		return false;
//	}
//	public static boolean isResultSection(Section sec, PDFConverterState pdf) {
//		String secTitle = sec.getTitle(pdf).toLowerCase();
//		if(secTitle.contains("result")) 
//			return true;
//		return false;
//	}

//	private static void anaylizeSectionTitle() {
//		String path = "PMC_FILES/";
//		String xmlDir = path + "XML/";
//		String pdfDir = path + "PDF/";
//		utility util = new utility();
//		String output = path + "BELExtractor\\sectionTitle-standard.txt";
//		//		util.writeFile(output, "PMCID\t isValid? \t total\t correct \t wrong\r\n", false);
//		//		File file = new File("PMC_FILES\\PDF\\PMC2000650.pdf1");
//		//		File xmlFile = new File("PMC_FILES\\XML\\PMC2000650\\Br_J_Clin_Pharmacol_2007_Sep_10_64(3)_317-327\\bcp0064-0317.nxml");
//
//		List<File> allFiles = util.getAllValidPDFs(pdfDir, false, ".pdf1");
//		int count_exp = 0;
//		int count_result = 0;
//		for(File file : allFiles) {
//			String PMCid = file.getName().substring(0, file.getName().indexOf("."));
//			//			if(!PMCid.equals("PMC1413578")) continue;
//			//			if(PMCid.equals("PMC1894742") || PMCid.equals("PMC1974771") || PMCid.contains("PMC1974835") || PMCid.contains("PMC2039851")) continue;
////			Debug.println("process " + PMCid);
//			File xmlFile = PDFconverter.Comparison_PMC.findFildEndWith(".nxml", xmlDir + PMCid + "/").get(0);
//			PMC_PDF pmc_pdf = PDFconverter.Comparison_PMC.convertXMLtoStructure(xmlFile.getAbsolutePath(), PMCid);
//			if(!pmc_pdf.isValid) continue;
//			
//			for(int i = 0; i < pmc_pdf.sections.size(); i++) {
//				PMC_Section sec = pmc_pdf.sections.get(i);
//				if(sec.sectionTitle != null) {
//				Debug.print(PMCid + "\t" + (i+1) + "\t" + pmc_pdf.sections.get(i).sectionTitle + "\t");
//				String secTitle = sec.sectionTitle.toLowerCase();
//				if(secTitle.contains("method") || secTitle.contains("material") || secTitle.contains("experiment") || secTitle.contains("data")) {
//					Debug.print("EXP \t");
//					count_exp++;
//				}
//				if(secTitle.contains("result")) {
//					Debug.print("RESULT \t");
//					count_result++;
//				}
//				Debug.println("");
//				}
//			}
//		}
//		Debug.println("total: " + allFiles.size() + "\t exp: " + count_exp + "\t result: " + count_result);
//	}

//	private static void exampleExtractingResultSection() {
//		String path = "test\\for lawyer\\cbautw9\\machine\\cbautw9.pdf";
//		utility util = new utility();
//		PDFConverterState pdf = Refinement.extractStructrueFromPDF(path);
//		util.writeFile(path + "_resultSection.txt", "", false);
//		for(Section sec : pdf.sections) {
//			if(sec.getTitle(pdf).contains("Result")) {
//				for(Paragraph para : sec.paragraphs)
//				util.writeFile(path + "_resultSection.txt", para.getText(pdf), true);
//			}
//		}
//	}
//	public static void exampleExtractor(String outputPath, String[] test) {
//		//String path = "test\\for lawyer2\\lunar_paper\\machine\\cbautw9.pdf_resultSection_clean.txt";
////		String path = "C:\\Users\\huangxc\\Desktop\\jayanthi\\2nd_paper\\result_disc.txt";
////		String path = "test/PMC1513515/Methods_section.txt";
//		utility util = new utility();
////		outputPath = "d:/test.fact";
//		
////		String path = "~/";
////		String para = util.readFromFile(path);
//		String para = "it revealed an apple";
//		StanfordNLPLight nlp = new StanfordNLPLight( "tokenize, ssplit, pos, lemma");
//		List<Sequence> sentences = nlp.textToSequence(para, true);
////		Map<String, Sequence> acronyms = Refinement.findAcronyms(para);
////		List<Sequence> freSeq = ngram.getFreqSequences(sentences);
////		HashSet<Sequence> freSeq_ = new HashSet<Sequence>(); freSeq_.addAll(freSeq);
//		
////		C_Facts cFact = patterns.parsePara(sentences, freSeq_, acronyms);
//		PatternMatcher pat = new PatternMatcher();
//		C_Facts cFact = pat.parsePara(sentences, null, null, new Span(-1,-1));
//		S_Facts sfact = new S_Facts(cFact);
//		sfact.mergeFacts();
//		
//		sfact.printFacts();
//		util.writeFile(outputPath, "", false);
////		for(String s : test)
////		util.writeFile(outputPath, s + "\r\n", true);
//		util.writeFile(outputPath, sfact.toString(false, true, 0,false), true);
////		sfact.writeFacts(path + ".fact");
////			String s = sfact.toString();
//			
//	}

	private static void exampleXMLExtractor(String path, String outputPath) {
//		String path = "withingroup\\panlu1.xml";
//		path = "validation_round1/joelyons123/joelyons123.xml";
		outputPath = path + "_.xml";
		System.out.println("process " + path);
		RootMatcher pat = new RootMatcher();
		if(!pat.readMacher("RuleMatcher.jason")) return;
		utility util = new utility();
		String xmlContent = util.readFromFile(path);
		xmlContent = xmlContent.replace("\n", " ").replace("\r", "");
		Document xmlDoc = utility.stringToDomWithoutDtd(xmlContent);
		if(xmlDoc == null) return ;
//		util.writeFile(path + "_", "", false);
//		writeChildren(xmlDoc, path + "_");;
		
//		util.writeFile(outputPath, xmlContent, false);
//		StanfordNLPLight nlp = new StanfordNLPLight( "tokenize, ssplit, pos, lemma");
		if(StanfordNLPLight.nlp == null) 
			StanfordNLPLight.nlp = new StanfordNLPLight( "tokenize, ssplit, pos, lemma");
		HashMap<Node, C_Facts> NodeToFacts = new HashMap<Node, C_Facts>();
		HashMap<Node, List<Sequence>> NodeToSequence = new HashMap<Node, List<Sequence>>();
//		String fullTextWithoutSectionTitle = "";
		List<Sequence> allSequences = new ArrayList<Sequence>();
		{
			NodeList paras = xmlDoc.getElementsByTagName("p");
			for(int i = 0; i < paras.getLength(); i++) {
				Node para = paras.item(i);
				String para_text = para.getTextContent().replace("&lt;", "<").replace("&amp;", "&").replace("&gt;", ">");
				List<Sequence> seqOfPara =  StanfordNLPLight.nlp.textToSequence(para_text, true);
				NodeToSequence.put(para, seqOfPara);
//				fullTextWithoutSectionTitle += para_text + "\r\n";
				allSequences.addAll(seqOfPara);
			}
		}
		
		NGrams ngram = new NGrams();
		Map<String, Sequence> acronyms = Acronym.findAcronyms(allSequences, StanfordNLPLight.nlp);
		{
			util.writeFile(outputPath + "_synonyms","" , false);
			for(String s : acronyms.keySet()) {
//				Debug.println(s + "\t" + acronyms.get(s).sourceString);
				util.writeFile(outputPath + "_synonyms",s + "\t" + acronyms.get(s).sourceString + "\r\n" , true);
			}
//			System.exit(0);
		}
		List<Sequence> freSeq = ngram.getFreqSequences(allSequences);
		HashSet<Sequence> freSeq_ = new HashSet<Sequence>(); freSeq_.addAll(freSeq);
		int counter_fact = 1;
		{
			NodeList paras = xmlDoc.getElementsByTagName("p");
			
			for(int i = 0; i < paras.getLength(); i++) {
				C_Facts cFact = pat.parsePara(NodeToSequence.get(paras.item(i)), freSeq_, acronyms, new Span(-1,-1));
				NodeToFacts.put(paras.item(i), cFact);
				S_Facts sFact = new S_Facts(cFact);
				sFact.mergeFacts();
//				paras.item(i).setTextContent(sFact.toString(true,true,counter_fact));
				paras.item(i).setTextContent(sFact.toString(true,true,-1,true));
				counter_fact += sFact.getSize();
			}
		}
		{
			NodeList paras = xmlDoc.getElementsByTagName("section_title");
			for(int i = 0; i < paras.getLength(); i++) {
				Node para = paras.item(i);
				String para_text = para.getTextContent();
				para.setTextContent("** " + para_text);
			}
		}
		util.writeFile(outputPath, util.xmlNodeToString(xmlDoc).replace("&#13;", "\r\n").replace("&lt;", "<").replace("&amp;", "&"), false);
		
	}
	/*
	private static void examplePDFExtractor(String path, String output) {
		PDFConverterState pdf = Refinement.extractStructrueFromPDF(path);
		if(pdf == null) {
			Debug.println("PDF Converter Failed!",DEBUG_CONFIG.debug_error);
			Debug.println("File Path: " + path,DEBUG_CONFIG.debug_error);
			return;
		}
		StanfordNLP nlp = new StanfordNLP( "tokenize, ssplit, pos");
		boolean write = true;
		HashMap<Paragraph, S_Facts> paraToFacts = new HashMap<Paragraph, S_Facts>();
		HashMap<Paragraph, List<Sequence>> paraToSequence = new HashMap<Paragraph, List<Sequence>>();
		List<Sequence> allSequences = new ArrayList<Sequence>();
		String fullTextWithoutSectionTitle = "";
		{
			for(int i = 0; i < pdf.sections.size(); i++) {
				Section sec = pdf.sections.get(i);
				for(Paragraph para : sec.paragraphs) {
//					Debug.println("**Section " + i);
					List<Sequence> para_seq = nlp.textToSequence(para.getText(pdf), -1, i, -1, true);
					paraToSequence.put(para, para_seq);
					allSequences.addAll(para_seq);
					fullTextWithoutSectionTitle += para.getText(pdf) + "\r\n";
				}
			}
		}
		ngrams ngram = new ngrams();
		ngrams.nlp = nlp;
		ngrams.wn = nlp.wn;
		Map<String, Sequence> acronyms = Refinement.findAcronyms(fullTextWithoutSectionTitle, nlp);
		{
//			for(String s : acronyms.keySet()) {
//				Debug.println(s + "\t" + acronyms.get(s).sourceString);
//			}
//			System.exit(0);
		}
		List<Sequence> freSeq = ngram.getFreqSequences(allSequences);
		HashSet<Sequence> freSeq_ = new HashSet<Sequence>(); freSeq_.addAll(freSeq);
		for(int i = 0; i < pdf.sections.size(); i++) {
			Section sec = pdf.sections.get(i);
			Debug.println("Section " + i,DEBUG_CONFIG.debug_temp);
			for(Paragraph para : sec.paragraphs) {
				C_Facts cFact = patterns.parsePara(paraToSequence.get(para), freSeq_, acronyms);
				S_Facts sFact = new S_Facts(cFact);
				sFact.mergeFacts();
				paraToFacts.put(para, sFact);
//				paras.item(i).setTextContent(sFact.toString());
			}
		}
		utility util = new utility();
		if(write) util.writeFile(output, "", false);
		if(write) util.writeFile(output + "_details", "", false);
		int counter_facts = 1;
		int counter_para = 1;
		int counter_sec =1;
		for(int i = 0; i < pdf.sections.size(); i++) {
			Section sec = pdf.sections.get(i);
			Debug.println(sec.getTitle(pdf),DEBUG_CONFIG.debug_temp);
			if(write) util.writeFile(output + "_details", "sec" + counter_sec++ + "\t** " + sec.getTitle(pdf) + "\r\n\r\n", true);
			if(write) util.writeFile(output, "** " + sec.getTitle(pdf) + "\r\n\r\n", true);
			for(int j = 0; j < sec.paragraphs.size(); j++) {
				Debug.println(sec.paragraphs.get(j).getText(pdf),DEBUG_CONFIG.debug_temp);
				if(write) util.writeFile(output + "_details", "para" + counter_para++ + "\t ##" + sec.paragraphs.get(j).getText(pdf) + "\r\n\r\n", true);
				if(write) util.writeFile(output + "_details", paraToFacts.get(sec.paragraphs.get(j)).toString(false, true,counter_facts), true);
				Debug.println(paraToFacts.get(sec.paragraphs.get(j)).toString(false, false, -1),DEBUG_CONFIG.debug_temp);
				if(write) util.writeFile(output, paraToFacts.get(sec.paragraphs.get(j)).toString(false, false, -1), true);
				if(write) util.writeFile(output, "\r\n", true);
				counter_facts += paraToFacts.get(sec.paragraphs.get(j)).getSize();
			}
			
		}
//		if(nlp == null) nlp = new StanfordNLP("tokenize, ssplit, pos");
//		{
//			for(String s : pdf.acronyms_.keySet()) {
//				pdf.acronyms.put(s, nlp.textToSequence(pdf.acronyms_.get(s), true).get(0));
//			}
//			Debug.println("-------------acronyms--------------------");
//			for(String s : pdf.acronyms_.keySet()) Debug.println(s + "\t" + pdf.acronyms.get(s).sourceString);
//		}
	}
	*/
	/**
	 * 
	 * @param args
	 * 0: path
	 * 1: output_dir
	 * 2: debug_dir
	 * 3: matcher file (by default: RuleMatcher.json)
	 * 4: output_log
	 * 5: output_facts file path
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
		utility util = new utility();
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
//				System.out.println(dateFormat.format(cal.getTime())); //2014/08/06 16:00:22
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
			fact_file = args[5];
		}
		JSONArray factsToOutput = new JSONArray();
		PDFConverter converter = new PDFConverter();
		PDF pdf =  converter.run(file, file.getName(), output_dir, debug_dir);
		if(pdf == null) {
			Debug.println("PDF Converter Failed!",DEBUG_CONFIG.debug_error);
			Debug.println("File Path: " + path,DEBUG_CONFIG.debug_error);
			return 2;
		}
		if(StanfordNLPLight.nlp == null) 
			StanfordNLPLight.nlp = new StanfordNLPLight( "tokenize, ssplit, pos, lemma");
		HashMap<Paragraph, S_Facts> paraToFacts = new HashMap<Paragraph, S_Facts>();
		HashMap<Paragraph, List<Sequence>> paraToSequence = new HashMap<Paragraph, List<Sequence>>();
		List<Sequence> allSequences = new ArrayList<Sequence>();
		{
			for(Paragraph para : pdf.body_and_heading) {
				//					Debug.println("**Section " + i);
				if(para.isHeading()) continue;
				List<Sequence> para_seq = StanfordNLPLight.nlp.textToSequence(para.text, -1, -1, -1, true);
				paraToSequence.put(para, para_seq);
				allSequences.addAll(para_seq);
			}
			
		}
		NGrams ngram = new NGrams();
		Map<String, Sequence> acronyms = Acronym.findAcronyms(allSequences, StanfordNLPLight.nlp);
		{
//			for(String s : acronyms.keySet()) {
//				Debug.println(s + "\t" + acronyms.get(s).sourceString);
//			}
//			System.exit(0);
		}
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
//			 obj.put("sectionTitle", sec.getTitle(pdf));
//			 obj.put("freq ngrams", freSeq.toString());
//			 obj.put("acronyms", acronyms.toString()); 
			 
//			 Debug.println("frequent ngrams are:\r\n" + freSeq.toString(), DEBUG_CONFIG.debug_error);
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
		int counter_facts = 1;
		int counter_para = 1;
		int counter_sec =1;
		for(int i = 0; i < pdf.body_and_heading.size(); i++) {
			Paragraph para = pdf.body_and_heading.get(i);
//			Debug.println(sec.getTitle(pdf),DEBUG_CONFIG.debug_temp);
//			if(write) util.writeFile(output + "_details", "sec" + counter_sec++ + "\t** " + sec.getTitle(pdf) + "\r\n\r\n", true);
//			if(write) util.writeFile(output, "** " + sec.getTitle(pdf) + "\r\n\r\n", true);
			if(para.isHeading()){
			 JSONObject obj=new JSONObject();
			 obj.put("type", "SectionTitle");
//			 obj.put("secID", i + 1);
			 obj.put("sectionTitle", para.getHeadingText());
			 obj.put("PageRange", para.pages.toString());
			 factsToOutput.add(obj);
			 continue;
			}
//			 for(int j = 0; j < sec.paragraphs.size(); j++) {
//				Debug.println(sec.paragraphs.get(j).getText(pdf),DEBUG_CONFIG.debug_temp);
//				if(write) util.writeFile(output + "_details", "para" + counter_para++ + "\t ##" + sec.paragraphs.get(j).getText(pdf) + "\r\n\r\n", true);
//				if(write) util.writeFile(output + "_details", paraToFacts.get(sec.paragraphs.get(j)).toString(false, true,counter_facts), true);
//				Debug.println(paraToFacts.get(sec.paragraphs.get(j)).toString(false, false, -1));
//				if(write) util.writeFile(output, paraToFacts.get(sec.paragraphs.get(j)).toString(false, false, -1), true);
//				if(write) util.writeFile(output, "\r\n", true);
//			if(i == 22) {
//				Debug.print("debug", DEBUG_CONFIG.debug_timeline);
//				System.out.println(para.text);
//				System.out.println(paraToSequence.get(para));
//			}
			if(paraToFacts.get(para) == null) {
				
			}else {
				factsToOutput.addAll(paraToFacts.get(para).toJSON(counter_facts));
				 JSONObject obj=new JSONObject();
				 obj.put("type", "Paragraph Break");
				 factsToOutput.add(obj);
				counter_facts += paraToFacts.get(para).getSize();
			}
//			}
			
		}
		{//write tables
			for(String s : pdf.getTables()) {
				 JSONObject obj=new JSONObject();
				 obj.put("type", "Table");
//				 obj.put("secID", i + 1);
				 obj.put("htmlTable", s);
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
		util.writeFile(fact_file, factsToOutput.toJSONString(), false);
//		if(nlp == null) nlp = new StanfordNLP("tokenize, ssplit, pos");
//		{
//			for(String s : pdf.acronyms_.keySet()) {
//				pdf.acronyms.put(s, nlp.textToSequence(pdf.acronyms_.get(s), true).get(0));
//			}
//			Debug.println("-------------acronyms--------------------");
//			for(String s : pdf.acronyms_.keySet()) Debug.println(s + "\t" + pdf.acronyms.get(s).sourceString);
//		}
		if(pdf.body_and_heading.size() == 0) return 3;
		return 1;
	}
	
}
