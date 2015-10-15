package test;

import java.io.File;

import knowledge_model.C_Facts;
import knowledge_model.S_Facts;
import nlp.StanfordNLPLight;
import pattern.RootMatcher;
import pattern.NGrams;
import utility.Debug;
import utility.Span;
import utility.utility;
import utility.Debug.DEBUG_CONFIG;
import extractor.ExtractorBELExtractor;

public class Debug_BEL_extractor {

	public static void main(String[] args) {
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
				String para = "A few apples are proved to be better compared with lexus 1 + - / * 1. ; it is 2(.)";
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
				if(StanfordNLPLight.nlp == null) 
					StanfordNLPLight.nlp = new StanfordNLPLight("tokenize, ssplit, pos, lemma");
				C_Facts cFact = matcher.parsePara(StanfordNLPLight.nlp.textToSequence(para, true), null, null, new Span(0,0));
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
			ExtractorBELExtractor.main(args);
//		}else if(fileOrFolder == false) {
//			testFromFolder(args[0], args[1], args[2]);
//		}
		
		
	}

}
