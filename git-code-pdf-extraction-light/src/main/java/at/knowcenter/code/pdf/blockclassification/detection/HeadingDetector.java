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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import at.knowcenter.code.api.pdf.Block;
import at.knowcenter.code.api.pdf.BlockLabel;
import at.knowcenter.code.api.pdf.BoundingBox;
import at.knowcenter.code.api.pdf.Document;
import at.knowcenter.code.api.pdf.Font;
import at.knowcenter.code.api.pdf.ReadingOrder;
import at.knowcenter.code.pdf.blockclassification.BlockLabeling;
import at.knowcenter.code.pdf.blockclassification.clustering.HeadingClusterer;
import at.knowcenter.code.pdf.blockclassification.detection.metadata.ArticleMetadataCollector;
import at.knowcenter.code.pdf.blockrelation.geometric.BlockNeighborhood;
import at.knowcenter.code.pdf.blockrelation.geometric.DefaultBlockNeighborhood;
import at.knowcenter.code.pdf.utils.PdfExtractionUtils;

/**
 * implements a detector for labelling headings (of sections, subsections etc).
 *
 * This detector uses information from blocks already labelled as main text,
 * thus this detector should be called after {@link MainTextDetector}.
 * 
 * A heading is found if it is 
 * i)   before a main text block or another heading*, either in terms of {@link ReadingOrder}
 *      or in terms of {@link BlockNeighborhood},
 * ii)  aligned to this text block, either at the left edge or at the horizontal center,
 * iii) close enough to this text block,
 * iv)  a font at least as large as the majority font of this text block,
 * v)   has a max number of lines,
 * vi)  has a minimum amount of text,
 * vii) starts with a number or capital letter.
 * 
 * *) the algorithm makes several passes through the set of blocks
 * 
 * @author sklampfl
 *
 */
public class HeadingDetector implements Detector {
    
    private static final String[] HEADINGS_TO_INCLUDE = new String[] { 
        "Introduction",
        "Discussion",
        "Results",
        "Results and Discussion",
        "Methods",
        "Materials",
        "Materials and Methods",
        "Conclusion",
        "Conclusions"
    };
	private final static int NUM_PASSES = 3;
	
	private final float tol;
	private final int maxNumLines;
	private final int maxDistanceInLines;
	private final double minDensity;
	
	private static boolean isCenterAligned(BoundingBox bbox1, BoundingBox bbox2, float tol) {
		double centerX1 = 0.5*(bbox1.maxx + bbox1.minx);
		double centerX2 = 0.5*(bbox2.maxx + bbox2.minx);
		return Math.abs(centerX1 - centerX2) < tol; 
	}
	
	private static boolean isAligned(BoundingBox bbox1, BoundingBox bbox2, float tol) {
		boolean isLeftAligned = bbox1.startsX(bbox2, tol) || bbox2.startsX(bbox1, tol) || bbox1.equalsX(bbox2, tol);
		boolean isCenterAligned = bbox1.duringX(bbox2, tol) && isCenterAligned(bbox1, bbox2, 2*tol);
		return isLeftAligned || isCenterAligned;
	}
	
	private static double verticalDistance(BoundingBox bbox1, BoundingBox bbox2) {
		return bbox2.miny - bbox1.maxy;
	}
	
	private static int getFontSize(Block block) {
		return PdfExtractionUtils.getMajorityFontSize(block);
	}
	
	private static int getFontId(Block block) {
		return PdfExtractionUtils.getMajorityFontId(block);
	}

	private static boolean isLabelAfterHeading(BlockLabel label) {
		return label!=null && (label==BlockLabel.Main || label==BlockLabel.Heading);
	}
	
    private static double getDensity(Block currentBlock) {
        double sum = 0;
        int counter = 0;
        for (Block line : currentBlock.getLineBlocks()) {
            for (Block word : line.getWordBlocks()) {
                sum += word.getFragments().size();
                counter++;
            }
        }
        double density = sum / counter;
        return density;
    }

	
	/**
	 * creates a new instance
	 * @param document the document (used for font information)
	 * @param readingOrder the reading order (used for sequential alignment to main text)
	 * @param blockNeighborhood the block neighborhood (used for sequential alignment to main text)
	 * @param tol the tolerance (used for geometrical alignment to main text)
	 * @param maxNumLines the max number of lines allowed for headings
	 * @param maxDistanceInLines the max distance to the following block measured in lines
	 */
	public HeadingDetector(float tol, int maxNumLines, int maxDistanceInLines, double minDensity) {		
		this.tol = tol;
		this.maxNumLines = maxNumLines;
		this.maxDistanceInLines = maxDistanceInLines;
		this.minDensity = minDensity;
	}
	
	/**
	 * creates a default heading detector
	 */
	public HeadingDetector() {
		this(10f, 3, 5, 3);
	}
	
	@Override
	public void detect(Document document, List<Block> pageBlocks, BlockLabeling labeling,
			ReadingOrder readingOrder, BlockNeighborhood blockNeighborhood, ArticleMetadataCollector articleMetadata) {
		
		Set<Block> candidateHeadings = detectCandidateHeadings(document, pageBlocks,
				labeling, readingOrder, blockNeighborhood);
		Collection<Block> postprocessedHeadings = postprocessHeadings(document, pageBlocks, labeling, 
				readingOrder, candidateHeadings);
		labelHeadings(postprocessedHeadings, labeling);
		
	}
	
	private void labelHeadings(Collection<Block> candidateHeadings, BlockLabeling labeling) {
		for (Block block : candidateHeadings) {
			labeling.setLabel(block, BlockLabel.Heading);
		}
	}
	
	private Collection<Block> postprocessHeadings(Document document, List<Block> pageBlocks, BlockLabeling labeling,
			ReadingOrder readingOrder, Collection<Block> candidateHeadings) {
		HeadingClusterer clusterer = new HeadingClusterer();
		List<Block> selectedHeadings = clusterer.getSelectedHeadings(pageBlocks, 
				labeling, readingOrder, document.getFonts(), candidateHeadings);
		return selectedHeadings;
		//return candidateHeadings;
	}

	private Set<Block> detectCandidateHeadings(Document document, List<Block> pageBlocks, BlockLabeling labeling,
			ReadingOrder readingOrder, BlockNeighborhood blockNeighborhood) {
		
		Set<Block> candidateHeadings = new HashSet<Block>();
		
		Block previousBlock = null;
		Block currentBlock = null;
		Block nextBlock = null;
		BoundingBox previousBBox = null;
		BoundingBox currentBBox = null;
		BoundingBox nextBBox = null;
		BlockLabel label = null;
		String text = null;
		Font majorFont = null;
		Block topBlock = null;
		Block bottomBlock = null;
		Block afterBlock = null;
		Block beforeBlock = null;
		BoundingBox topBBox = null;
		BoundingBox bottomBBox = null;
		int numLines = -1;
		int fontSize = -1;
		int afterFontSize = -1;
		int fontId = -1;
		int afterFontId = -1;
		int columnWidth = 0;
		int pageWidth = 0;
		
		DefaultBlockNeighborhood dbn = null;
		if (blockNeighborhood instanceof DefaultBlockNeighborhood) {
			dbn = (DefaultBlockNeighborhood)blockNeighborhood;
		}
		
		int oldSize = -1;
		int pass = 0;
		//for (int pass=0; pass<NUM_PASSES; pass++) {
		while (oldSize != candidateHeadings.size() && pass < NUM_PASSES) {
			pass++;
			oldSize = candidateHeadings.size();
			for (int pageId=0; pageId<pageBlocks.size(); pageId++) {
				Block page = pageBlocks.get(pageId);
				pageWidth = (int)page.getBoundingBox().getWidth();
				Block[] textBlocks = page.getSubBlocks().toArray(new Block[0]);
				List<Integer> order = readingOrder.getReadingOrder(pageId);
				for (int i=0; i<order.size(); i++) {
					previousBlock = (i>0)?textBlocks[order.get(i-1)]:null;
					currentBlock = textBlocks[order.get(i)];
					nextBlock = (i<order.size()-1)?textBlocks[order.get(i+1)]:null;
					previousBBox = (previousBlock!=null)?previousBlock.getBoundingBox():null;
					currentBBox = currentBlock.getBoundingBox();
					nextBBox = (nextBlock!=null)?nextBlock.getBoundingBox():null;
					fontId = getFontId(currentBlock);
					majorFont = document.getFonts().get(fontId);
					fontSize = getFontSize(currentBlock);
					numLines = currentBlock.getSubBlocks().size();
					text = currentBlock.getText();
					afterBlock = null;
					beforeBlock = null;
					
					if (labeling.hasLabel(currentBlock, BlockLabel.Main)) {
						columnWidth = (int)currentBlock.getBoundingBox().getWidth();
					}
		        	for (String ih : HEADINGS_TO_INCLUDE) {
		        		if (ih.replaceAll("\\s", "").equalsIgnoreCase(text.replaceAll("\\s", ""))) {
		        			candidateHeadings.add(currentBlock);
		        		}
		        	}
			        
					
					topBlock = null;
					bottomBlock = null;
					if (dbn != null) {
						Set<Block> topBlocks = 
								dbn.getNeighbors(currentBlock, DefaultBlockNeighborhood.Direction.North);
						Set<Block> bottomBlocks = 
								dbn.getNeighbors(currentBlock, DefaultBlockNeighborhood.Direction.South);
						if (topBlocks.size()==1) {
							topBlock = topBlocks.iterator().next();
							topBBox = topBlock.getBoundingBox();
						}
						if (bottomBlocks.size()==1) {
							bottomBlock = bottomBlocks.iterator().next();
							bottomBBox = bottomBlock.getBoundingBox();
						}
					}
					
					boolean afterMainBlock = false;
					boolean alignedToPreviousBlock = false;
					if (previousBlock!=null) {
						label = labeling.getLabel(previousBlock);
						afterMainBlock = isLabelAfterHeading(label);
						alignedToPreviousBlock = isAligned(currentBBox, previousBBox, tol);
					}
					boolean belowMainBlock = false;
					boolean alignedToTopBlock = false;
					if (topBlock!=null) {
						label = labeling.getLabel(topBlock);
						belowMainBlock = isLabelAfterHeading(label);
						alignedToTopBlock = isAligned(currentBBox, topBBox, tol);
					}
					boolean beforeMainBlock = false;
					boolean alignedToNextBlock = false;
					if (nextBlock!=null) {
						label = labeling.getLabel(nextBlock);
						beforeMainBlock = isLabelAfterHeading(label);
						beforeMainBlock = beforeMainBlock || candidateHeadings.contains(nextBlock);
						alignedToNextBlock = isAligned(currentBBox, nextBBox, tol);
						alignedToNextBlock = alignedToNextBlock || nextBBox.getWidth() > pageWidth/2;
					}
					boolean aboveMainBlock = false;
					boolean alignedToBottomBlock = false;
					if (bottomBlock!=null) {
						label = labeling.getLabel(bottomBlock);
						aboveMainBlock = isLabelAfterHeading(label);
						aboveMainBlock = aboveMainBlock || candidateHeadings.contains(bottomBlock);
						alignedToBottomBlock = isAligned(currentBBox, bottomBBox, tol);
						alignedToBottomBlock = alignedToBottomBlock || bottomBBox.getWidth() > pageWidth/2;
					}
					
					if (beforeMainBlock && alignedToNextBlock) {
						afterBlock = nextBlock;
					} else if (aboveMainBlock && alignedToBottomBlock) {
						afterBlock = bottomBlock;
					}
					if (afterMainBlock && alignedToPreviousBlock) {
						beforeBlock = previousBlock;
					} else if (belowMainBlock && alignedToTopBlock) {
						beforeBlock = bottomBlock;
					}
					if (afterBlock != null) {
						afterFontSize = getFontSize(afterBlock);
						afterFontId = getFontId(afterBlock);
					}
					
					char firstChar = text.charAt(0);
					boolean hasStartChar = Character.isDigit(firstChar) || Character.isLetter(firstChar);
					boolean hasText = text.replaceAll("\\d(\\.\\d)*", " ").trim().length()>1;
					boolean linesCorrect = numLines <= maxNumLines;
					//boolean fontSizeCorrect = fontSize >= afterFontSize;
					boolean distanceCorrect = false;
					boolean widthCorrect = currentBBox.getWidth() <= columnWidth+1;
					boolean densityCorrect = getDensity(currentBlock) >= minDensity;
					
					//if ((afterMainBlock && alignedToPreviousBlock) || (belowMainBlock && alignedToTopBlock) ||
					if (afterBlock != null) {
						distanceCorrect = verticalDistance(currentBBox, afterBlock.getBoundingBox()) < maxDistanceInLines*fontSize;
						if (hasStartChar && hasText && linesCorrect && //fontSizeCorrect && 
								distanceCorrect && widthCorrect && densityCorrect) {
							if (labeling.hasLabelOrNull(currentBlock, BlockLabel.Sparse)) {
								candidateHeadings.add(currentBlock);
							} else if (labeling.hasLabel(currentBlock, BlockLabel.Main)) {
								if (labeling.hasLabel(afterBlock, BlockLabel.Main)) {
//									if ((fontId>=0 && otherFontId>=0 && fontId!=otherFontId) &&
//											(majorFont!=null && majorFont.getIsBold()!=null && majorFont.getIsItalic()!=null)) {
//										if (majorFont.getIsBold() || majorFont.getIsItalic()) {
//											labeling.setLabel(currentBlock, BlockLabel.Heading);
//										}
//									}
									if (fontId>=0 && afterFontId>=0 && fontId!=afterFontId) {
										//if (majorFont!=null && majorFont.getIsBold()!=null && majorFont.getIsItalic()!=null &&
										//		(majorFont.getIsBold() || majorFont.getIsItalic())) {
											candidateHeadings.add(currentBlock);
										//}
									}
								} else if (candidateHeadings.contains(afterBlock)) {
									if (fontId>=0 && afterFontId>=0 && fontId==afterFontId) {
										candidateHeadings.add(currentBlock);
									}
								}
							}
						}
					}
				}
			}
		}
		
		return candidateHeadings;
	}

}
