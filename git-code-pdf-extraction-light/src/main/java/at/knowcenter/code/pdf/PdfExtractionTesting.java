/**
 * Copyright (C) 2010
 * "Kompetenzzentrum fuer wissensbasierte Anwendungen Forschungs- und EntwicklungsgmbH"
 * (Know-Center), Graz, Austria, office@know-center.at.
 *
 * Licensees holding valid Know-Center Commercial licenses may use this file in
 * accordance with the Know-Center Commercial License Agreement provided with
 * the Software or, alternatively, in accordance with the terms contained in
 * a written agreement between Licensees and Know-Center.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package at.knowcenter.code.pdf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//import javax.swing.JFileChooser;
//import javax.swing.filechooser.FileNameExtensionFilter;


import at.knowcenter.code.api.pdf.PdfParser.PdfParserException;
import at.knowcenter.code.pdf.PdfExtractionPipeline.PdfExtractionResult;
import at.knowcenter.code.pdf.utils.PdfExtractionUtils;
//import at.knowcenter.code.pdf.utils.rendering.PdfBlockRenderer;
//import at.knowcenter.code.pdf.utils.rendering.PdfViewer;
import at.knowcenter.code.pdf.utils.table.PartitioningTableParser;
import at.knowcenter.code.pdf.utils.table.TableException;
import at.knowcenter.code.pdf.utils.table.TableParser;
//import at.knowcenter.code.pdf.utils.text.InformationExtractionPipeline;
//import at.knowcenter.code.pdf.utils.text.StatisticalInformationExtractor;
//import at.knowcenter.ie.documentviewer.AnnotatedDocumentViewer;


/**
 * 
 * A simple testing program for pdf extraction.
 * 
 * @author sklampfl
 *
 */
public class PdfExtractionTesting {
		
//	private static String getKeyText(String documentText) throws IOException {
//		StatisticalInformationExtractor extractor = new StatisticalInformationExtractor(
//				new InformationExtractionPipeline(), documentText);
//		List<String> keyPhrases = extractor.getKeyPhrases();
//		List<String> keySentences = extractor.getKeySentences();
//		
//		StringBuilder buffer = new StringBuilder();
//		buffer.append("\nKey Phrases:\n");
//		for (String keyPhrase : keyPhrases) {
//			buffer.append(keyPhrase).append("\n");
//		}
//		buffer.append("\nKey Sentences:\n");
//		for (String keySentence : keySentences) {
//			buffer.append(keySentence).append("\n");
//		}
//		return buffer.toString();
//	}
	

	private static void run(File file) throws PdfParserException {
		System.out.println("File "+file.getAbsolutePath());
		
		PdfExtractionPipeline pipeline = new PdfExtractionPipeline();
		PdfExtractionResult result = pipeline.runPipeline(file.getName(), file);			
		
		String documentText = result.documentText;

		try {
			String outputFileName = "data/doc.txt";
			PdfExtractionUtils.writeTextToFile(documentText, outputFileName, false);
//			if (documentText.length()>0) {
//				String keyText = getKeyText(documentText);
//				PdfExtractionUtils.writeTextToFile(keyText, outputFileName, true);
//			}

			outputFileName = "data/tables.html";
			//TableParser tableParser = new ClusteringTableParser();
			TableParser tableParser = new PartitioningTableParser();
			PdfExtractionUtils.writeTablesToFile(result, tableParser, outputFileName);
			
//			int i = 0;
//			for(Page page: result.doc.getPages()) {
//				for(Image img: page.getImages()) {
//					FileOutputStream out = new FileOutputStream(new File("data", "img-" + i++ + "." + img.getSuffix()));
//					IOUtils.write(img.getByteArray(), out);
//					IOUtils.closeQuietly(out);
//				}
//			}
			
		} catch (IOException e) {
			throw new PdfParserException("Could not write text to file ", e);
		} catch (TableException e) {
			throw new PdfParserException("Could not parse table ", e);
		}

//		new AnnotatedDocumentViewer(result.annotatedDocument);
//		new PdfViewer(file, new PdfBlockRenderer(result));		
	}
	
//	private static File selectFileFromChooser(String path) {
//		File file = null;
//		JFileChooser fc = new JFileChooser(path);
//		fc.setFileFilter(new FileNameExtensionFilter("PDF files", "pdf"));
//		if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
//	    	file = fc.getSelectedFile();
//        }
//		return file;
//	}
	
    private static List<File> loadPubMedIds(String pubMedLocation, String idsFile) throws IOException {
        List<File> result = new ArrayList<File>();
        String[] lines = PdfExtractionUtils.readLinesFromFile(idsFile);
        for (String line : lines) {
            result.add(new File(pubMedLocation+line+"/"+line+".pdf"));
        }
        return result;
    }
	
	@SuppressWarnings("unchecked")
	private static File selectRandomFile() throws IOException {
//		String[] dirs = new String[] {
//				"/home/sklampfl/workspace/GROTOAP/pdfs",
//				"/home/sklampfl/Dokumente/know/data/eprints/pdfs,bio",
//				"/home/sklampfl/Dokumente/know/data/pubmed-0.1/data-set"
//		};
//		String[] extensions = new String[] {"pdf"};
//		List<File> files = new ArrayList<File>();
//		for (String dir : dirs) {
//			files.addAll(FileUtils.listFiles(new File(dir), extensions, true));
//		}
		List<File> files = loadPubMedIds("/home/sklampfl/Dokumente/know/data/pubmed-0.1/data-set/",
				"data/pubmed-ids/pubmed-1k-test/testing-ids.txt");
		int index = new Random().nextInt(files.size());
		File selectedFile = files.get(index);
		//System.out.println(selectedFile);
		return selectedFile;
	}
	
	public static void main(String[] args) throws IOException {
		
		File file = null;
		boolean random = false;
		String path = "data";
		if (args.length > 0) {
			path = args[0];
		}
		
		if (!random) {
//			file = selectFileFromChooser(path);
		} else {
			file = selectRandomFile();
		}
		
		if (file != null) {
			try {
				run(file);
			} catch (PdfParserException e) {
				e.printStackTrace();
			}
		}
		

	}

}
