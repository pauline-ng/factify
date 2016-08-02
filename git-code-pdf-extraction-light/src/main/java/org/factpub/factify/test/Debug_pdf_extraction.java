package org.factpub.factify.test;

import java.io.File;

import at.knowcenter.code.pdf.PdfExtractionPipeline;
import at.knowcenter.code.pdf.PdfExtractionPipeline.PdfExtractionResult;
import pdfStructure.PDF;
import utility.Debug;
import utility.Debug.DEBUG_CONFIG;
/**
 * cav6qhz.pdf: pdf-extraction works terribly on this pdf.
 */
public class Debug_pdf_extraction {

//	/* sample pdf file 1 */
//	private static String input_folder = "pdf\\";
//	private static String pdf_file = "DOI10.1093nargkg509_SIFT.pdf";
//	
//	/* sample pdf file 2 */
//	private static String input_folder = "pdf\\";
//	private static String pdf_file = "DOI10.1146annurev.genom.7.080505.115630_PredictingTheEffects.pdf";
//

	/* sample pdf file 3 */
	private static String input_folder = "pdf\\incorrectDOI\\";
	private static String pdf_file = "DOI(10.1126science.1240729)_BetaCaMKII_wrong_is_(10.1126science.1236501).pdf";
	
//	/* sample pdf file 4 */
//	private static String input_folder = "pdf\\incorrectDOI\\";
//	private static String pdf_file = "DOI(10.1053j.gastro.2009.04.032)_EvidenceForTheRole_wrong_is_(10.1053j.gastro.2009.04.032).pdf";

	public static void main(String[] args) {

		String pdfpath = input_folder + pdf_file;
		String outputPath = input_folder;
		String debugPath = input_folder;
		
		PdfExtractionPipeline  pipeline = new PdfExtractionPipeline();
		pipeline.setParameter((new File(pdfpath)).getName(), outputPath, debugPath);
		Debug.set(DEBUG_CONFIG.debug_textpieces, true);
		
		
		PdfExtractionResult result = pipeline.runPipeline(new File(pdfpath));
		PDF pdf = pipeline.getPDF();

		System.out.println(pdf);
		System.out.println(result);
	}
			
	
}
