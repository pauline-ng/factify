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
package at.knowcenter.code.pdf.blockclassification.detection;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
//import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.factpub.factify.utility.Debug;
import org.factpub.factify.utility.Debug.DEBUG_CONFIG;

import at.knowcenter.code.api.pdf.Block;
import at.knowcenter.code.api.pdf.BlockLabel;
import at.knowcenter.code.api.pdf.Document;
import at.knowcenter.code.api.pdf.ReadingOrder;
import at.knowcenter.code.pdf.PdfExtractionPipeline;
import at.knowcenter.code.pdf.blockclassification.BlockLabeling;
import at.knowcenter.code.pdf.blockclassification.detection.metadata.ArticleMetadataCollector;
import at.knowcenter.code.pdf.blockrelation.geometric.BlockNeighborhood;

/**
 * This class holds a number of {@link Detector}s and allows to invoke them sequentially.
 * 
 * @author sklampfl
 *
 */
public class DetectorPipeline implements Detector {
    private static final Logger logger = Logger.getLogger(Detector.class.getName());

	private final Detector[] detectors;
	
	/**
	 * initializes a new pipeline with a number of detectors
	 * @param detectors the detectors
	 */
	public DetectorPipeline(Detector ... detectors) {
		this.detectors = detectors;
	}
	
	/**
//	 * invokes the {@link Detector#detect(List)} method for each detector sequentially
	 * @param doc the {@link Document} the blocks come from
	 * @param pageBlocks the list of page blocks
	 * @param labeling the block labeling, which is modified by individual detectors
	 * @param readingOrder the {@link ReadingOrder} of the blocks
	 * @param neighborhood the {@link BlockNeighborhood}
	 */
	@Override
	public void detect(Document doc, List<Block> pageBlocks, BlockLabeling labeling, 
			ReadingOrder readingOrder, BlockNeighborhood neighborhood, ArticleMetadataCollector articleMetadata) {
		if (detectors==null) {
			return;
		}
		String debug_file = PdfExtractionPipeline.global_debug_dir + "detectorPipeline";
		{
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			DateFormat dateFormat1 = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
//			System.out.println(dateFormat.format(date)); //2014/08/06 15:59:48
			debug_file = PdfExtractionPipeline.global_debug_dir + PdfExtractionPipeline.global_id + "_detectorPipeline.txt" + dateFormat1.format(date);
			if(Debug.get(DEBUG_CONFIG.debug_detectorpipeline)) writeFile(debug_file, dateFormat.format(date) + "\r\n", false);
		}
		Map<Block, String> afterFigDet = new HashMap<Block, String>();
		 for (Detector detector : detectors) {
			try {
				Class<? extends Detector> c = detector.getClass();
				logger.fine("Detecting: " + c.getName().replace(c.getPackage().getName() + ".","").replace("Detector", "").toUpperCase());
                detector.detect(doc, pageBlocks, labeling, readingOrder, neighborhood, articleMetadata);
            } catch (Throwable e) {
                logger.log(Level.SEVERE, "Cannot exectute detector '" + detector + "' on document '" + doc + "'", e);
            }
			{
				if(Debug.get(DEBUG_CONFIG.debug_detectorpipeline))writeFile(debug_file, "*****AFTER DETECTOR " + detector.getClass() + "\r\n", true);
				
				if(detector.getClass() ==  at.knowcenter.code.pdf.blockclassification.detection.FigureDetector.class) {
					if(Debug.get(DEBUG_CONFIG.debug_detectorpipeline))writeFile(debug_file, "*****AFTER DETECTOR " + detector.getClass() + "\r\n", true);
					for (int i = 0; i < pageBlocks.size(); i++) {
//			        	Page page = doc.getPages().get(i);
			            Block pageBlock = pageBlocks.get(i);
			            List<Integer> ro = readingOrder.getReadingOrder(i);
			            List<Block> blocks = new ArrayList<Block>(pageBlock.getSubBlocks());
			            for (int j = 0; j < ro.size(); j++) {            	
			                Block currentBlock = blocks.get(ro.get(j));
			                BlockLabel label = labeling.getLabel(currentBlock);
			                if(Debug.get(DEBUG_CONFIG.debug_detectorpipeline))writeFile(debug_file, "----" + (label == null ? "null" : label.getLabel()) + "----\r\n", true);
			                if(Debug.get(DEBUG_CONFIG.debug_detectorpipeline))writeFile(debug_file,currentBlock.getText() + "\r\n" , true);
			                afterFigDet.put(currentBlock, (label == null? "null" : label.getLabel()));
			            }
			        }
				}
			/*	if(detector.getClass() ==  at.knowcenter.code.pdf.blockclassification.detection.references.ReferenceDetector.class) {
					writeFile("debug_huangxc.txt", "*****FROM afterFigDetector TO end**************\r\n", true);
					HashSet<String> allLabels = new HashSet<String>();
					HashSet<String> allLabels_old = new HashSet<String>();
					for (int i = 0; i < pageBlocks.size(); i++) {
//			        	Page page = doc.getPages().get(i);
			            Block pageBlock = pageBlocks.get(i);
			            List<Integer> ro = readingOrder.getReadingOrder(i);
			            List<Block> blocks = new ArrayList<Block>(pageBlock.getSubBlocks());
			      
			            for (int j = 0; j < ro.size(); j++) {            	
			                Block currentBlock = blocks.get(ro.get(j));
			                BlockLabel label = labeling.getLabel(currentBlock);
//			                if(!afterFigDet.containsKey(currentBlock)) {
//			                	writeFile("debug_huangxc.txt", "***NEW***" + currentBlock.getText() + "\r\n" , true);
//			                	continue;
//			                }
			                if((label == null && !afterFigDet.get(currentBlock).equals("null")) || 
			                		(label != null && !afterFigDet.get(currentBlock).equals(label.getLabel()))) {
			                	writeFile("debug_huangxc.txt", "----from " + afterFigDet.get(currentBlock) + " to " + label.getLabel() + "\r\n", true);
			                	writeFile("debug_huangxc.txt",currentBlock.getText() + "\r\n" , true);
			                	allLabels.add(label.getLabel());
			                	allLabels_old.add(afterFigDet.get(currentBlock));
			                }
			            }
					}
					allLabels.remove(allLabels_old);
					System.out.println("new labels are " + allLabels);
				}*/
				 
			}
		}
		
	}

	public void writeFile(String path, String s, boolean append) {
		File log_f;
		log_f = new File(path);
		Writer out; 
		try {
			
			if(!log_f.exists()) {
				Path pathToFile = Paths.get(path);
				if(Files.createDirectories(pathToFile.getParent()) == null || Files.createFile(pathToFile) == null) {
					Debug.print("Failed to create file " + path, DEBUG_CONFIG.debug_error);
					return;
				}
			}
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(new File(path), append), "UTF-8"));
//			out = new BufferedWriter(new FileWriter(new File(path), append));
			out.append(s);
			out.flush();
			out.close();
			
		}
		catch(Exception e) {
			Debug.print("Failed to create file " + path, DEBUG_CONFIG.debug_error);
			e.printStackTrace();
			
		}
	}
}
