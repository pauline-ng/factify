package PDFconverter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import pdfStructure.PDF;
import utility.Debug;
import utility.Debug.DEBUG_CONFIG;
//import at.knowcenter.code.api.pdf.PdfParser.PdfParserException;
import at.knowcenter.code.pdf.PdfExtractionPipeline;
import at.knowcenter.code.pdf.PdfExtractionPipeline.PdfExtractionResult;

public class PDFConverter {
	public static void main(String[] args) throws Exception {
		File file = new File("..\\git-BEL-extractor-test\\1756-9966-27-85.pdf");
		PDFConverter converter = new PDFConverter();
		converter.run(file);
	}
	public PDF run(File file) {
		if(!file.exists()) {
			Debug.print("File " + file.getAbsolutePath() + " does not exist!", DEBUG_CONFIG.debug_error);
			return null;
		}else if(!file.isFile()) {
			Debug.print("File " + file.getAbsolutePath() + " is not a file!", DEBUG_CONFIG.debug_error);
			return null;
		}
//		System.out.println("File "+file.getAbsolutePath());
		PdfExtractionPipeline pipeline = new PdfExtractionPipeline();
		String fileName = file.getName();
		if(!pipeline.setParameter(fileName.endsWith(".pdf") ? fileName.substring(0, fileName.length() - 4) : fileName,
				file.getParent() == null ?  "output/" : file.getParent() + "/output/",
				file.getParent() == null ?  "debug_output/" : file.getParent() + "/debug_output/"))
			return null;
		PdfExtractionResult result = pipeline.runPipeline(file);
		if(result == null) return null;
		return pipeline.getPDF();
	}
	public PDF run(File file, String...args) {
		Debug.print("****Start Parsing PDF****", DEBUG_CONFIG.debug_timeline);
		if(!file.exists()) {
			Debug.print("File " + file.getAbsolutePath() + " does not exist!", DEBUG_CONFIG.debug_error);
			return null;
		}else if(!file.isFile()) {
			Debug.print("File " + file.getAbsolutePath() + " is not a file!", DEBUG_CONFIG.debug_error);
			return null;
		}
//		System.out.println("File "+file.getAbsolutePath());
		PdfExtractionPipeline pipeline = new PdfExtractionPipeline();
		if(!pipeline.setParameter(args))
			return null;
		PdfExtractionResult result = pipeline.runPipeline(file);
		if(result == null) return null;
		return pipeline.getPDF();
	}
	
}
 