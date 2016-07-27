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
/**
 * This class servers as the ONLY interface with package git-code-pdf-extraction-light
 */
package org.factpub.factify.pdfconverter;

import java.io.File;

import pdfStructure.PDF;
import utility.Debug;
import utility.Debug.DEBUG_CONFIG;
import at.knowcenter.code.pdf.PdfExtractionPipeline;
import at.knowcenter.code.pdf.PdfExtractionPipeline.PdfExtractionResult;

/**
 * Main function to interact with {@link at.knowcenter.code} to parse the structure of a PDF file
 *
 */
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
		Debug.println("****Start Parsing PDF****", DEBUG_CONFIG.debug_timeline);
		if(!file.exists()) {
			Debug.println("File " + file.getAbsolutePath() + " does not exist!", DEBUG_CONFIG.debug_error);
			return null;
		}else if(!file.isFile()) {
			Debug.println("File " + file.getAbsolutePath() + " is not a file!", DEBUG_CONFIG.debug_error);
			return null;
		}
		PdfExtractionPipeline pipeline = new PdfExtractionPipeline();
		if(!pipeline.setParameter(args))
			return null;
		PdfExtractionResult result = pipeline.runPipeline(file);
		if(result == null) return null;
		return pipeline.getPDF();
	}
	
}
 