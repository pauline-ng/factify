package PDFconverter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import pdfStructure.PDF;
//import at.knowcenter.code.api.pdf.PdfParser.PdfParserException;
import at.knowcenter.code.pdf.PdfExtractionPipeline;
import at.knowcenter.code.pdf.PdfExtractionPipeline.PdfExtractionResult;

public class PDFConverter {
	public static void main(String[] args) throws Exception {
		File file = new File("test\\1756-9966-27-85.pdf");
		PDFConverter converter = new PDFConverter();
		converter.run(file);
	}
	public PDF run(File file) {
		System.out.println("File "+file.getAbsolutePath());
		PdfExtractionPipeline pipeline = new PdfExtractionPipeline();
		String filePath = file.getName();
		pipeline.global_path = filePath.endsWith(".pdf") ? filePath.substring(0, filePath.length() - 4) : filePath;
		PdfExtractionResult result = pipeline.runPipeline("", file );
		if(result == null) return null;
		return pipeline.pdf;
	}

}
 