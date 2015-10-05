package extractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
//import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
//import java.io.OutputStreamWriter;
//import java.io.PrintWriter;
//import java.io.UnsupportedEncodingException;
//import java.io.Writer;
//import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
//import java.util.StringTokenizer;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;







import nlp.Sequence;
import nlp.StanfordNLPLight;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;







import pattern.Acronym;
import pattern.NGrams;
import pattern.PatternMatcher;
//import at.knowcenter.code.api.pdf.PdfParser.PdfParserException;
import pdfStructure.PDF;
import pdfStructure.Paragraph;
import debug.Debug;
import debug.Debug.DEBUG_CONFIG;
import utility.utility;
import knowledge_model.C_Facts;
import knowledge_model.S_Facts;
import PDFconverter.PDFConverter;
import pdfStructure.*;
//import reddit_anaysis.utility;
//import reddit_anaysis.wordFrequency;
//import PDFconverter.Refinement;
//import PDFconverter.Refinement;
//import model.PMC_PDF;
//import model.PMC_Section;

public class ExtractorBELExtractor {
	static StanfordNLPLight nlp;
	public static void main(String[]  args) {
		boolean test = true;
		if(!test) {
		String pdfPath = null;
		String xmlPath = null;
		String outputPath = null;
		if(args.length == 0) {
			Debug.println("Please enter a valid parameter (-pdf/-txt inputFilePath -o outputFilePath)",DEBUG_CONFIG.debug_error);
			return;
		}
		for(int i = 0; i < args.length;) {
			switch(args[i].toLowerCase().trim()) {
			case "-pdf":
			{
				i++;
				if(i < args.length) {
					pdfPath = args[i];
					i++;
				}else {
					Debug.println("ERROR: Please specify input file path!",DEBUG_CONFIG.debug_error);
					return;
				}
				
			}
			break;
			case "-xml":
				Debug.println("-xml is not supported yet",DEBUG_CONFIG.debug_error);
				System.exit(0);
				i++;
				if(i < args.length) {
					xmlPath = args[i];
					i++;
				}else {
					Debug.println("ERROR: Please specify input file path!",DEBUG_CONFIG.debug_error);
					return;
				}
				break;
			case "-o":
				i++;
				if(i < args.length) {
					outputPath = args[i];
					i++;
				}else {
					Debug.println("ERROR: Please specify output file path!",DEBUG_CONFIG.debug_error);
					return;
				}
				break;
			default:
				Debug.println("Please enter a valid parameter (-pdf/-txt inputFilePath -o outputFilePath)",DEBUG_CONFIG.debug_error);
				 Debug.println("**" + args[i] + "\t",DEBUG_CONFIG.debug_error);
				for(String t : args) Debug.println(t + "\t",DEBUG_CONFIG.debug_error);
				return;
			}
		}
		if(pdfPath != null && xmlPath != null) {
			Debug.println("Please specify if the input file is PDF or Txt",DEBUG_CONFIG.debug_error);
			return;
		}
		if(outputPath == null) {
			Debug.println("Please specify output file path",DEBUG_CONFIG.debug_error);
			return;
		}
		if(pdfPath != null && outputPath != null) {
//			examplePDFExtractor(pdfPath, outputPath);
//			examplePDFExtractor_JSON(pdfPath, outputPath);
		}
		if(xmlPath != null && outputPath != null) {
//			exampleXMLExtractor(xmlPath, outputPath);
		}
		return;
		}
		
//		anaylizeSectionTitle();
//		test();
//		test_forLawyer();
//		if(args.length != 2) {
//			Debug.println("Please specify ONE parameter!");
//			return;
//		}
//		exampleExtractor("", args);
//		System.exit(0);
//		examplePDFExtractor("test\\PMC1513515\\PMC1513515.pdf1");
//		prevalidation_1stRound();
//		String path = "test\\for lawyer\\cbdgmlu\\cbdgmlu_text.xml";
		String path = "test\\cbautw9_\\cbautw9_.pdf";
//		examplePDFExtractor(path, path + "_.fact");
//		exampleXMLExtractor(path,path + "_");
		ExtractorBELExtractor extractor = new ExtractorBELExtractor();
		extractor.examplePDFExtractor_JSON(path, path + "_fact.json");
//		examplePDFExtractor("withingroup\\huan1.pdf", "withingroup\\huan1.pdf.fact");
//		utility util = new utility();
//		util.writeFile("d:/zotero.txt", "I love you", false);
//		File file = util.getResourceFile();
//		Debug.println(util.getResourceAsString("stopwords.txt"));
//		Debug.println(util.readFromFile(file));
//		exampleExtractingResultSection();
//		test_few();
//		try {
//			test___();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	private static String reg_Number = "\\d+";
	
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
	public static void exampleExtractor(String outputPath, String[] test) {
		//String path = "test\\for lawyer2\\lunar_paper\\machine\\cbautw9.pdf_resultSection_clean.txt";
//		String path = "C:\\Users\\huangxc\\Desktop\\jayanthi\\2nd_paper\\result_disc.txt";
//		String path = "test/PMC1513515/Methods_section.txt";
		utility util = new utility();
		outputPath = "d:/test.fact";
		
		String path = "~/";
//		String para = util.readFromFile(path);
		String para = "it revealed an apple";
		StanfordNLPLight nlp = new StanfordNLPLight( "tokenize, ssplit, pos, lemma");
		List<Sequence> sentences = nlp.textToSequence(para, true);
		NGrams ngram = new NGrams();
		NGrams.nlp = nlp;
//		ngrams.wn = nlp.wn;
//		Map<String, Sequence> acronyms = Refinement.findAcronyms(para);
//		List<Sequence> freSeq = ngram.getFreqSequences(sentences);
//		HashSet<Sequence> freSeq_ = new HashSet<Sequence>(); freSeq_.addAll(freSeq);
		
//		C_Facts cFact = patterns.parsePara(sentences, freSeq_, acronyms);
		PatternMatcher pat = new PatternMatcher();
		C_Facts cFact = pat.parsePara(sentences, null, null);
		S_Facts sfact = new S_Facts(cFact);
		sfact.mergeFacts();
		
		sfact.printFacts();
		util.writeFile(outputPath, "", false);
//		for(String s : test)
//		util.writeFile(outputPath, s + "\r\n", true);
		util.writeFile(outputPath, sfact.toString(false, true, 0,false), true);
//		sfact.writeFacts(path + ".fact");
		
			
//			String s = sfact.toString();
			
	}

	private static void exampleXMLExtractor(String path, String outputPath) {
//		String path = "withingroup\\panlu1.xml";
//		path = "validation_round1/joelyons123/joelyons123.xml";
		outputPath = path + "_.xml";
		System.out.println("process " + path);
		utility util = new utility();
		String xmlContent = util.readFromFile(path);
		xmlContent = xmlContent.replace("\n", " ").replace("\r", "");
		Document xmlDoc = utility.stringToDomWithoutDtd(xmlContent);
		if(xmlDoc == null) return ;
//		util.writeFile(path + "_", "", false);
//		writeChildren(xmlDoc, path + "_");;
		
//		util.writeFile(outputPath, xmlContent, false);
		StanfordNLPLight nlp = new StanfordNLPLight( "tokenize, ssplit, pos");
	
		HashMap<Node, C_Facts> NodeToFacts = new HashMap<Node, C_Facts>();
		HashMap<Node, List<Sequence>> NodeToSequence = new HashMap<Node, List<Sequence>>();
//		String fullTextWithoutSectionTitle = "";
		List<Sequence> allSequences = new ArrayList<Sequence>();
		{
			NodeList paras = xmlDoc.getElementsByTagName("p");
			for(int i = 0; i < paras.getLength(); i++) {
				Node para = paras.item(i);
				String para_text = para.getTextContent().replace("&lt;", "<").replace("&amp;", "&").replace("&gt;", ">");
				List<Sequence> seqOfPara =  nlp.textToSequence(para_text, true);
				NodeToSequence.put(para, seqOfPara);
//				fullTextWithoutSectionTitle += para_text + "\r\n";
				allSequences.addAll(seqOfPara);
			}
		}
		
		NGrams ngram = new NGrams();
		NGrams.nlp = nlp;
//		ngrams.wn = nlp.wn;
		Map<String, Sequence> acronyms = Acronym.findAcronyms(allSequences, nlp);
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
			PatternMatcher pat = new PatternMatcher();
			for(int i = 0; i < paras.getLength(); i++) {
				C_Facts cFact = pat.parsePara(NodeToSequence.get(paras.item(i)), freSeq_, acronyms);
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
	 * output as JSON file
	 * @param path
	 * @param output
	 */
	
	public  void examplePDFExtractor_JSON(String path, String output) {
		JSONArray factsToOutput = new JSONArray();
		PDFConverter converter = new PDFConverter();
		
		PDF pdf =  converter.run(new File(path));
		if(pdf == null) {
			Debug.println("PDF Converter Failed!",DEBUG_CONFIG.debug_error);
			Debug.println("File Path: " + path,DEBUG_CONFIG.debug_error);
			return;
		}
		StanfordNLPLight nlp = new StanfordNLPLight( "tokenize, ssplit, pos, lemma");
		HashMap<Paragraph, S_Facts> paraToFacts = new HashMap<Paragraph, S_Facts>();
		HashMap<Paragraph, List<Sequence>> paraToSequence = new HashMap<Paragraph, List<Sequence>>();
		List<Sequence> allSequences = new ArrayList<Sequence>();
		{
			for(Paragraph para : pdf.body_and_heading) {
				//					Debug.println("**Section " + i);
				if(para.isHeading()) continue;
				List<Sequence> para_seq = nlp.textToSequence(para.text, -1, -1, -1, true);
				paraToSequence.put(para, para_seq);
				allSequences.addAll(para_seq);
			}
			
		}
		NGrams ngram = new NGrams();
		NGrams.nlp = nlp;
//		ngrams.wn = nlp.wn;
		Map<String, Sequence> acronyms = Acronym.findAcronyms(allSequences, nlp);
		{
//			for(String s : acronyms.keySet()) {
//				Debug.println(s + "\t" + acronyms.get(s).sourceString);
//			}
//			System.exit(0);
		}
		List<Sequence> freSeq = ngram.getFreqSequences(allSequences);
		HashSet<Sequence> freSeq_ = new HashSet<Sequence>(); freSeq_.addAll(freSeq);
		PatternMatcher pat = new PatternMatcher();
		for(int i = 0; i < pdf.body_and_heading.size(); i++) {
			Paragraph para = pdf.body_and_heading.get(i);
			if(para.isHeading()) continue;
			Debug.println("Section " + i,DEBUG_CONFIG.debug_temp);
				C_Facts cFact = pat.parsePara(paraToSequence.get(para), freSeq_, acronyms);
				S_Facts sFact = new S_Facts(cFact);
				sFact.mergeFacts();
				paraToFacts.put(para, sFact);
//				paras.item(i).setTextContent(sFact.toString());
		}
		{
			 JSONObject obj=new JSONObject();
			 obj.put("type", "paper");
			 obj.put("path", path);
//			 obj.put("sectionTitle", sec.getTitle(pdf));
			 factsToOutput.add(obj);
			}
		utility util = new utility();
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
				factsToOutput.addAll(paraToFacts.get(para).toJSON(counter_facts));
				counter_facts += paraToFacts.get(para).getSize();
//			}
			
		}
		util.writeFile(output, factsToOutput.toJSONString(), false);
//		if(nlp == null) nlp = new StanfordNLP("tokenize, ssplit, pos");
//		{
//			for(String s : pdf.acronyms_.keySet()) {
//				pdf.acronyms.put(s, nlp.textToSequence(pdf.acronyms_.get(s), true).get(0));
//			}
//			Debug.println("-------------acronyms--------------------");
//			for(String s : pdf.acronyms_.keySet()) Debug.println(s + "\t" + pdf.acronyms.get(s).sourceString);
//		}
	}
	
}
