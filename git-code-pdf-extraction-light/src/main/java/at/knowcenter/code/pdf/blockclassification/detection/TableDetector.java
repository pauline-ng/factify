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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import at.knowcenter.code.api.pdf.Block;
import at.knowcenter.code.api.pdf.BlockLabel;
import at.knowcenter.code.api.pdf.BoundingBox;
import at.knowcenter.code.api.pdf.Document;
import at.knowcenter.code.api.pdf.Page;
import at.knowcenter.code.api.pdf.Page.Line;
import at.knowcenter.code.api.pdf.ReadingOrder;
import at.knowcenter.code.api.pdf.TableRegion;
import at.knowcenter.code.pdf.blockclassification.BlockLabeling;
import at.knowcenter.code.pdf.blockclassification.detection.metadata.ArticleMetadataCollector;
import at.knowcenter.code.pdf.blockrelation.geometric.BlockNeighborhood;

/**
 * implements a detector for tables via labelling sparse lines (tables, formulas, references etc.).
 * 
 * Liu, Y., Mitra, P., & Giles, C. L. (2008). 
 * Identifying table boundaries in digital documents via sparse line detection. 
 * Proceeding of the 17th ACM conference on Information and knowledge mining CIKM 08 (pp. 1311–1320).
 * ACM Press. doi:10.1145/1458082.1458255
 * 
 * This detector looks for blocks that either have a large word spacing or a short width.
 * The reference statistics are calculated from the blocks already labelled as main text.
 * Then sparse blocks in the neighborhood of a table caption are labelled as table blocks.
 * Thus this detector should be called after {@link MainTextDetector} and {@link CaptionDetector}.
 * 
 * @author sklampfl
 *
 */
public class TableDetector implements Detector {

	private final float wordSpaceFraction;
	private final float columnWidthFraction;
	private final float tol = 10f;
	
	public static class MainTextStatistics {
		public final float avgColumnWidth;
		public final float avgWordSpace;
		public final float avgLineHeight;
		public final SortedSet<Integer> mainLeftPos;
		public final int aboveOrBelowScore;
		public final float minLeftPos;
		public final float maxRightPos;
		
		public MainTextStatistics(float avgColumnWidth, float avgWordSpace, float avgLineHeight,
				SortedSet<Integer> mainLeftPos, int aboveOrBelowScore, float minLeftPos, float maxRightPos) {
			this.avgColumnWidth = avgColumnWidth;
			this.avgWordSpace = avgWordSpace;
			this.avgLineHeight = avgLineHeight;
			this.mainLeftPos = mainLeftPos;
			this.aboveOrBelowScore = aboveOrBelowScore;
			this.minLeftPos = minLeftPos;
			this.maxRightPos = maxRightPos;
		}
	}
	
	/**
	 * creates a new instance
	 * @param wordSpaceFraction the fraction/multiple of the allowed word space relative to
	 * the average word space in main text (typical 2)
	 * @param columnWidthFraction the fraction of the allowed column width relative to
	 * the avarage main column width (typical 2/3)
	 */
	public TableDetector(float wordSpaceFraction, float columnWidthFraction) {
		this.wordSpaceFraction = wordSpaceFraction;
		this.columnWidthFraction = columnWidthFraction;
	}
	
	/**
	 * creates a default table detector
	 */
	public TableDetector() {
		this(2f, 2f/3f);
	}
	
	protected MainTextStatistics detectMainTextStatistics(List<Block> pageBlocks, BlockLabeling labeling, 
			BlockNeighborhood neighborhood) {
		float sumWidth = 0.0f;
		int numMainBlocks = 0;
		float sumSpace = 0.0f;
		int numSpaces = 0;
		float sumHeight = 0.0f;
		int numLines = 0;
		SortedSet<Integer> mainLeftPos = new TreeSet<Integer>();
		float minLeftPos = Float.MAX_VALUE;
		float maxRightPos = 0;
		
		for (Block pageBlock : pageBlocks) {
			for (Block block : pageBlock.getSubBlocks()) {
		        if (labeling.hasLabel(block, BlockLabel.Main)) {
		        	numMainBlocks++;
		        	BoundingBox boundingBox = block.getBoundingBox();
					sumWidth += boundingBox.getWidth();
		        	mainLeftPos.add((int)boundingBox.minx);
		        	if (boundingBox.minx < minLeftPos) {
		        		minLeftPos = boundingBox.minx;
		        	}
		        	if (boundingBox.maxx > maxRightPos) {
		        		maxRightPos = boundingBox.maxx;
		        	}
		        	for (Block line : block.getSubBlocks()) {
		        		numLines++;
		        		sumHeight += line.getBoundingBox().getHeight();
		        		Block[] words = line.getSubBlocks().toArray(new Block[0]);
		        		for (int w=0; w<words.length-1; w++) {
		        			sumSpace += words[w+1].getBoundingBox().minx - words[w].getBoundingBox().maxx;
		        			numSpaces++;
		        		}
		        	}
		        }
			}
		}
		
		float avgColumnWidth = sumWidth/numMainBlocks;
		float avgWordSpace = sumSpace/numSpaces;
		float avgLineHeight = sumHeight/numLines;
		
		int aboveOrBelowScore = 0;
		for (Block pageBlock : pageBlocks) {
			for (Block block : pageBlock.getSubBlocks()) {
		        if (isTableCaptionBlock(block, labeling)) {
		        	Block minBlock = findStartBlock(block, labeling, neighborhood, null);
		        	if (minBlock==null) {
		        		continue;
		        	}
		        	if (isAbove(block, minBlock)) {
		        		aboveOrBelowScore++;
		        	} else if (isAbove(minBlock, block)) {
		        		aboveOrBelowScore--;
		        	}
		        }
			}
		}
//		boolean isCaptionAbove = aboveOrBelowScore>0;
		
		return new MainTextStatistics(avgColumnWidth, avgWordSpace, avgLineHeight, mainLeftPos, 
				aboveOrBelowScore, minLeftPos, maxRightPos);
	}
	
	protected boolean isAbove(Block block1, Block block2) {
		BoundingBox bbox1 = block1.getBoundingBox();
		BoundingBox bbox2 = block2.getBoundingBox();
		return bbox1.precedesY(bbox2, 0) || bbox1.meetsY(bbox2, 0) || bbox1.overlapsY(bbox2, 0);
	}
	
	private boolean hasSmallWidth(Block block, MainTextStatistics statistics) {
		return block.getBoundingBox().getWidth() < columnWidthFraction*statistics.avgColumnWidth;
	}
	
	private boolean hasLargeWordSpacing(Block block, MainTextStatistics statistics) {
		for (Block line : block.getSubBlocks()) {
    		Block[] words = line.getSubBlocks().toArray(new Block[0]);
    		for (int w=0; w<words.length-1; w++) {
    			float space = words[w+1].getBoundingBox().minx - words[w].getBoundingBox().maxx;
    			if (space > wordSpaceFraction*statistics.avgWordSpace) {
    				return true;
    			}
    		}
		}
		return false;
	}
	
	private boolean hasLargeAverageWordSpacing(Block block, MainTextStatistics statistics) {
		float sumSpace = 0;
		int count = 0;
		for (Block line : block.getSubBlocks()) {
    		Block[] words = line.getSubBlocks().toArray(new Block[0]);
    		for (int w=0; w<words.length-1; w++) {
    			float space = words[w+1].getBoundingBox().minx - words[w].getBoundingBox().maxx;
    			sumSpace += space;
    			count++;
    		}
		}
		if (sumSpace/count > wordSpaceFraction*statistics.avgWordSpace) {
			return true;
		}
		return false;
	}
	
	protected void detectSparseBlocks(List<Block> pageBlocks, BlockLabeling labeling, MainTextStatistics statistics) {
		for (Block pageBlock : pageBlocks) {
			for (Block block : pageBlock.getSubBlocks()) {
				if (labeling.getLabel(block)==null) {
					if (hasSmallWidth(block, statistics)) {
						labeling.setLabel(block, BlockLabel.Sparse);
						continue;
					}
		        	if (hasLargeWordSpacing(block, statistics)) {
		        		labeling.setLabel(block, BlockLabel.Sparse);
		        	}
				} else if (labeling.hasLabel(block, BlockLabel.Main)) {
					if (hasLargeAverageWordSpacing(block, statistics)) {
						labeling.setLabel(block, BlockLabel.Sparse);
					}
				}
			}
		}
	}
	
	protected boolean isTableCaptionBlock(Block block, BlockLabeling labeling) {
		if (labeling.hasLabel(block, BlockLabel.Caption)) {
			if (block.getText().substring(0,3).equalsIgnoreCase("tab")) {
				return true;
			}
		}
		return false;
	}
	
	protected boolean hasSmallDistance(Block block, Block neighbor, MainTextStatistics statistics,
			float currentTop, float currentBottom) {
		double verticalDistance = Double.POSITIVE_INFINITY;
		double horizontalDistance = Double.POSITIVE_INFINITY;
		BoundingBox neighborBBox = neighbor.getBoundingBox();
		if (block != null) {
			BoundingBox bbox = block.getBoundingBox();
			verticalDistance = BoundingBox.verticalDistance(bbox, neighborBBox);
			horizontalDistance = BoundingBox.horizontalDistance(bbox, neighborBBox);
		}
		verticalDistance = Math.min(verticalDistance, 
				Math.max(neighborBBox.miny - currentBottom, currentTop - neighborBBox.maxy));
		boolean isCloseX = true; //horizontalDistance < columnWidthFraction*avgColumnWidth;
		boolean isCloseY = verticalDistance < 2.5*statistics.avgLineHeight;
		return isCloseX && isCloseY;
	}
		
	protected List<TableRegion> detectTableBlocks(Document doc, List<Block> pageBlocks, BlockLabeling labeling, 
			BlockNeighborhood neighborhood, MainTextStatistics statistics) {
		List<TableRegion> tables = new LinkedList<TableRegion>();
		List<Page> pages = doc.getPages();
		for (int pageId=0; pageId<pageBlocks.size(); pageId++) {
			Block pageBlock = pageBlocks.get(pageId);
			Page page = pages.get(pageId);
			for (Block block : pageBlock.getSubBlocks()) {
				if (isTableCaptionBlock(block, labeling)) {
					TableRegion tableRegion = findTableRegion(page, pageBlock, block, labeling, neighborhood, statistics);
					if (tableRegion != null) {
                        tables.add(tableRegion);
                    }
				}
			}
		}
		return tables;
	}
	
	protected boolean isSparse(BlockLabeling labeling, Block neighbor) {
		return labeling.hasLabelOrNull(neighbor, BlockLabel.Sparse);
	}
	
	protected void processQueue(Queue<Block> queue, List<Block> words, BoundingBox tableBBox, Block captionBlock,
			int left, boolean isSingleColumn, BlockLabeling labeling, BlockNeighborhood neighborhood, 
			MainTextStatistics statistics) {
		Set<Block> sparseNeighbors = new HashSet<Block>();
		do {
			while (!queue.isEmpty()) {
				Block block = queue.poll();
				if (block == null) { continue; }
				labeling.setLabel(block, BlockLabel.Table);
				words.addAll(block.getWordBlocks());
				BoundingBox bbox = block.getBoundingBox();
				tableBBox.minx = Math.min(tableBBox.minx, bbox.minx);
				tableBBox.maxx = Math.max(tableBBox.maxx, bbox.maxx);
				tableBBox.miny = Math.min(tableBBox.miny, bbox.miny);
				tableBBox.maxy = Math.max(tableBBox.maxy, bbox.maxy);
				for (Block neighbor : neighborhood.getNeighbors(block)) {
					bbox = neighbor.getBoundingBox();
					boolean isSparse = isSparse(labeling, neighbor);
					boolean insideRegion = tableBBox.intersects(bbox, 0);
					insideRegion = insideRegion && !labeling.hasLabel(neighbor, BlockLabel.Sparse, BlockLabel.Table, BlockLabel.Caption);
					boolean isMergeCandidate = isMergeCandidate(block, neighbor, captionBlock, left, labeling, isSingleColumn, statistics, tableBBox);
					if (isSparse && isMergeCandidate) {
						queue.add(neighbor);
						labeling.setLabel(neighbor, BlockLabel.Table);
					} else if (isSparse || insideRegion) {
						sparseNeighbors.add(neighbor);
					}
				}
			}
			for (Block neighbor : sparseNeighbors) { 
				// these are sparse blocks which are not yet labelled, but which might belong to the table 
				boolean isMergeCandidate = isMergeCandidate(null, neighbor, captionBlock, left, labeling, isSingleColumn, statistics, tableBBox);
				if (isMergeCandidate && !labeling.hasLabel(neighbor, BlockLabel.Table)) {
					queue.add(neighbor);
					break;
				}
			}
			sparseNeighbors.clear();
		} while(!queue.isEmpty());

	}
	
	protected boolean isMergeCandidate(Block block, Block neighbor, Block captionBlock, int left, 
			BlockLabeling labeling, boolean isSingleColumn,	MainTextStatistics statistics, BoundingBox tableBBox) {
		BoundingBox bbox = neighbor.getBoundingBox();
		boolean isInsideColumn = bbox.minx > left-tol && bbox.maxx < left+statistics.avgColumnWidth+tol;
		boolean isOutsideColumn = bbox.maxx < left-tol || bbox.minx > left+statistics.avgColumnWidth+tol;
		boolean tableInsideColumn = tableBBox.minx > left-tol && tableBBox.maxx < left+statistics.avgColumnWidth+tol;
		tableInsideColumn = tableInsideColumn && tableBBox.getHeight() > 50;
		boolean hasSmallDistance = hasSmallDistance(block, neighbor, statistics, tableBBox.miny, tableBBox.maxy);
		boolean isColumn = Math.abs(statistics.avgColumnWidth - bbox.getWidth())<tol && 
				bbox.getHeight() > Math.max(200, tableBBox.getHeight());
		boolean fitsToCaption = fitsToCaption(captionBlock, bbox, tableBBox, statistics);
		boolean result = !(isSingleColumn && !isInsideColumn) && !(tableInsideColumn && isOutsideColumn) 
				&& hasSmallDistance && !isColumn && fitsToCaption;
		return result;

	}
	
	protected boolean fitsToCaption(Block captionBlock, BoundingBox bbox, BoundingBox tableBBox, MainTextStatistics statistics) {
		boolean fitsToCaption = true;
		if (captionBlock != null) {
			BoundingBox captionBBox = captionBlock.getBoundingBox();
			float captionCenter = captionBBox.minx + captionBBox.getWidth()/2;
			float minLeftPos = Math.max(statistics.minLeftPos, 2*captionCenter - statistics.maxRightPos);
			float maxRightPos = statistics.maxRightPos;
			minLeftPos = Math.min(minLeftPos, tableBBox.minx);
			maxRightPos = Math.max(maxRightPos, tableBBox.maxx);
			fitsToCaption = bbox.minx >= minLeftPos-tol && bbox.maxx <= maxRightPos+tol;
		}
		return fitsToCaption;
	}
		
	protected TableRegion findTableRegion(Page page, Block pageBlock, Block captionBlock, BlockLabeling labeling,
    		BlockNeighborhood neighborhood, MainTextStatistics statistics) {
    	
    	BoundingBox captionBBox = captionBlock.getBoundingBox();
		float captionLeft = captionBBox.minx;
    	float captionWidth = captionBBox.maxx-captionBBox.minx;
    	float captionCenter = captionLeft + captionWidth/2;
    	List<Block> lineBlocks = captionBlock.getLineBlocks();
		int numCaptionLines = lineBlocks.size();
    	if (lineBlocks.get(0).getText().length() < 15) { // contains only "Table X"
    		numCaptionLines--;
    	}
    	
    	// determine if table is single-column table (based on caption)
    	boolean isSingleColumn = false;
    	int left = -1;
    	for (Integer leftPos : statistics.mainLeftPos) {
   			float center = leftPos + statistics.avgColumnWidth/2;
   			if (Math.abs(leftPos - captionLeft)<tol || Math.abs(center - captionCenter)<tol) {
   				if (numCaptionLines > 1 && captionWidth <= statistics.avgColumnWidth+tol) {
   					isSingleColumn = true;
   				}
   				left = leftPos;
   				break;
   			}
   		}
    	
    	Block minBlock = findStartBlock(captionBlock, labeling, neighborhood, statistics);
    	if (minBlock == null) {
    	    // TODO improve code
    	    return null;
    	}
    	
    	List<Block> words = new LinkedList<Block>();
		Queue<Block> queue = new LinkedList<Block>();
		queue.add(minBlock);
		BoundingBox bbox = minBlock.getBoundingBox();
		BoundingBox tableBBox = new BoundingBox(Math.min(captionBBox.minx, bbox.minx),
				Math.max(captionBBox.maxx, bbox.maxx), bbox.miny, bbox.maxy);
		processQueue(queue, words, tableBBox, captionBlock, left, isSingleColumn,
				labeling, neighborhood, statistics);
		
		for (Block block : pageBlock.getSubBlocks()) {
			if (!labeling.hasLabel(block, BlockLabel.Table, BlockLabel.Caption)) {
				if (tableBBox.contains(block.getBoundingBox())) {
					labeling.setLabel(block, BlockLabel.Table);
					words.addAll(block.getWordBlocks());
				}
			}
		}
		
		List<Line> lines = new ArrayList<Line>();
		for (Line line : page.getLines()) {
			if (line.isHorizontal() || line.isVertical()) {
				if (tableBBox.contains((float)line.getStart().getX(), (float)line.getStart().getY()) &&
					tableBBox.contains((float)line.getEnd().getX(), (float)line.getEnd().getY())) {
					lines.add(line);
				}
			}
		}
		
		return new TableRegion(tableBBox, captionBBox, words, captionBlock, lines);
    }

	protected Block findStartBlock(Block captionBlock, BlockLabeling labeling, 
			BlockNeighborhood neighborhood, MainTextStatistics statistics) {
		Block minBlock = null;
		BoundingBox captionBBox = captionBlock.getBoundingBox();
    	double minDistance = Double.POSITIVE_INFINITY;
    	for (Block neighbor : neighborhood.getNeighbors(captionBlock)) {
    		BoundingBox neighborBBox = neighbor.getBoundingBox();
			double distance = BoundingBox.verticalDistance(neighborBBox, captionBBox);
    		//boolean isSparse = labeling.hasLabelOrNull(neighbor, BlockLabel.Sparse);
			boolean isSparse = !labeling.hasLabel(neighbor, BlockLabel.Decoration);
    		boolean isHorizontallyAligned = !captionBBox.precedesX(neighborBBox, tol) &&
    				!neighborBBox.precedesX(captionBBox, tol);
    		boolean hasCorrectRelativeVerticalPosition = (statistics==null) || (statistics.aboveOrBelowScore == 0) || 
    				(statistics.aboveOrBelowScore > 0 && isAbove(captionBlock, neighbor)) || 
    				(statistics.aboveOrBelowScore < 0 && isAbove(neighbor, captionBlock));
    		if (distance < minDistance && isHorizontallyAligned && hasCorrectRelativeVerticalPosition && isSparse) {
    			minDistance = distance;
    			minBlock = neighbor;
    		}
    	}
		return minBlock;
	}

	
	@Override
	public void detect(Document doc, List<Block> pageBlocks, BlockLabeling labeling, 
			ReadingOrder readingOrder, BlockNeighborhood neighborhood, ArticleMetadataCollector articleMetadata) {

		MainTextStatistics statistics = detectMainTextStatistics(pageBlocks, labeling, neighborhood);
		detectSparseBlocks(pageBlocks, labeling, statistics);
		List<TableRegion> tables = detectTableBlocks(doc, pageBlocks, labeling, neighborhood, statistics);
		doc.setTables(tables);

	}

}
