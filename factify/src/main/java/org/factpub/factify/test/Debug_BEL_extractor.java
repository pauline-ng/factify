package org.factpub.factify.test;

import java.io.File;
import java.text.NumberFormat;

import org.factpub.factify.Factify;
import org.factpub.factify.knowledge_model.C_Facts;
import org.factpub.factify.knowledge_model.S_Facts;
import org.factpub.factify.nlp.StanfordNLPLight;
import org.factpub.factify.pattern.RootMatcher;
import org.factpub.factify.utility.Debug;
import org.factpub.factify.utility.Span;
import org.factpub.factify.utility.Utility;
import org.factpub.factify.utility.Debug.DEBUG_CONFIG;

public class Debug_BEL_extractor {

	private static String samplePDF_Path = ".\\pdf\\DOI10.1093nargkg509_SIFT.pdf";
	
	public static void main(String[] args) {
		System.out.println(File.separator);
		System.out.println(Utility.MD5(samplePDF_Path));
		if(true)return;
		// TODO Auto-generated method stub
//		String path = "D:\\huangxcwd\\Data\\reddit\\odesk\\allpapers\\ccok4xz.pdf.pdf";
//		examplePDFExtractor(path, path + "_.fact");
//		exampleXMLExtractor(path,path + "_");
//		ExtractorBELExtractor extractor = new ExtractorBELExtractor();
		if(false){
			if(args.length != 1) {
				Debug.print("Please specify the matcher file!", DEBUG_CONFIG.debug_error);
				return;
			}
			Debug.set(DEBUG_CONFIG.debug_temp, true);
			RootMatcher matcher = new RootMatcher();
			if(!matcher.readMacher(args[0])) return;
//				String para = "A few apples are proved to be better compared with lexus 1 + - / * 1. ; it is 2(.)";
			String para = "This allows otherwise poorly invasive bacteria to exploit lipid raft-mediated transcytotic pathways to cross the intestinal mucosa.";
				//		Debug.println("\\u" + Integer.toHexString('-' | 0x10000).substring(1));
//				for(int i = 0; i < para.length(); i++) Debug.println(para.charAt(i) +  "\t" + "\\u" + Integer.toHexString(para.charAt(i) | 0x10000).substring(1) + "\t" + Character.getType(para.charAt(i)));
//				System.exit(0);
				Debug.set(DEBUG_CONFIG.debug_pattern, true);
				Debug.set(DEBUG_CONFIG.debug_C_Facts, true);
				Debug.set(DEBUG_CONFIG.debug_S_Facts, true);
				Debug.println(para,DEBUG_CONFIG.debug_pattern);
//				StanfordNLPLight nlp = new StanfordNLPLight("tokenize, ssplit, pos, lemma");
//				ngrams ngram = new ngrams();
//				ngrams.wn = nlp.wn;
//				List<Sequence> freSeq = ngram.getFreqSequences(sentences);
//				freSeq_ = new HashSet<Sequence>(); freSeq_.addAll(freSeq);
				C_Facts cFact = matcher.parsePara(StanfordNLPLight.INSTANCE.textToSequence(para, true), null, null, new Span(0,0));
				if(cFact != null) {
					S_Facts sfact = new S_Facts(cFact);
					sfact.mergeFacts();
					sfact.printFacts();
				}
//				sfact.writeFacts("test/PMC1513515/Methods_section_ngram.fact");
			if(true) return;
		}
//		if(args.length != 3) {
//			Debug.print("Please specify 3 parameters: filePath, output_dir, and debug_dir!", DEBUG_CONFIG.debug_error);
//			return;
//		}
//		if(!new File(args[0]).exists()) {
//			Debug.print("Please specify a valid input file or folder!", DEBUG_CONFIG.debug_error);
//			return;
//		}
//		Boolean fileOrFolder = null;//true if it is a file
//		if(new File(args[0]).isFile()) {
//			fileOrFolder = true;
//		}else if (new File(args[0]).isDirectory()) {
//			fileOrFolder = false;
//		}
//		if(fileOrFolder == true) {
		Runtime runtime = Runtime.getRuntime();

		NumberFormat format = NumberFormat.getInstance();
		{
			StringBuilder sb = new StringBuilder();
			long maxMemory = runtime.maxMemory();
			long allocatedMemory = runtime.totalMemory();
			long freeMemory = runtime.freeMemory();

			sb.append("free memory: " + format.format(freeMemory / 1024) + "\r\n");
			sb.append("allocated memory: " + format.format(allocatedMemory / 1024) + "\r\n");
			sb.append("max memory: " + format.format(maxMemory / 1024) + "\r\n");
			sb.append("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024) + "\r\n");
			System.out.println(sb);
		}
			Factify.main(args);
			
			{
			StringBuilder sb = new StringBuilder();
			long maxMemory = runtime.maxMemory();
			long allocatedMemory = runtime.totalMemory();
			long freeMemory = runtime.freeMemory();

			sb.append("free memory: " + format.format(freeMemory / 1024) + "\r\n");
			sb.append("allocated memory: " + format.format(allocatedMemory / 1024) + "\r\n");
			sb.append("max memory: " + format.format(maxMemory / 1024) + "\r\n");
			sb.append("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024) + "\r\n");
			System.out.println(sb);
			}
			//		}else if(fileOrFolder == false) {
//			testFromFolder(args[0], args[1], args[2]);
//		}
		
		
	}

}
