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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import at.knowcenter.code.api.pdf.Block;
import at.knowcenter.code.api.pdf.BlockLabel;
import at.knowcenter.code.api.pdf.BoundingBox;
import at.knowcenter.code.api.pdf.Document;
import at.knowcenter.code.api.pdf.ReadingOrder;
import at.knowcenter.code.pdf.blockclassification.BlockLabeling;
import at.knowcenter.code.pdf.blockclassification.detection.metadata.ArticleMetadataCollector;
import at.knowcenter.code.pdf.blockrelation.geometric.BlockNeighborhood;
import at.knowcenter.code.pdf.utils.StringMatching;

/**
 * implements a detector for labelling decorations (page numbers, headers, footers, etc.).
 * 
 * Lin, X. (2002). Header and Footer Extraction by Page-Association.
 * Proceedings of SPIE, 5010, 164–171. doi:10.1117/12.472833
 * 
 * It looks for blocks at the top or bottom of a page that are similar on neighboring pages.
 * Similarity is given by both the geometric similarity (overlapping bounding boxes) and
 * the content similarity (edit distance between the String content of the blocks).
 * 
 * @author sklampfl
 *
 */
public class DecorationDetector implements Detector {

	private final int numLines;
	private final float scoreThreshold;
	private final float areaThreshold;
	private final List<Comparator<Block>> comparators;	
	private int significance = 10;
	
	/**
	 * creates a new instance.
	 * 
	 * @param numLines the number of blocks to consider at the top and bottom of each page
	 * (blocks are sorted by their minimum and maximum y-coordinates). A typical value is 5 to 10.
	 * @param scoreThreshold the threshold above which a block is classified as a decoration.
	 * The score is between 0 and 1. To allow for a certain flexibility a value between 0.25 and 0.5 is recommended
	 */
	public DecorationDetector(int numLines, float scoreThreshold, float areaThreshold) {
		this.numLines = numLines;
		this.scoreThreshold = scoreThreshold;		
		this.areaThreshold = areaThreshold;
		this.comparators = new ArrayList<Comparator<Block>>();
		this.comparators.add(new Comparator<Block>() {
			@Override
			public int compare(Block o1, Block o2) {
				float diff = significance*(o1.getBoundingBox().miny-o2.getBoundingBox().miny);
				return (int)Math.signum(diff);
			}
		});
		this.comparators.add(new Comparator<Block>() {
			@Override
			public int compare(Block o1, Block o2) {
				float diff = significance*(o2.getBoundingBox().maxy-o1.getBoundingBox().maxy);
				return (int)Math.signum(diff);
			}
		});
		// 2504315/2504315.pdf did yield a "Comparison method violates its general contract!"
		this.comparators.add(new Comparator<Block>() {
			@Override
			public int compare(Block o1, Block o2) {
				float diff = significance*(o1.getBoundingBox().minx-o2.getBoundingBox().minx);
				return (int)Math.signum(diff);
			}
		});
		this.comparators.add(new Comparator<Block>() {
			@Override
			public int compare(Block o1, Block o2) {
				float diff = significance*(o2.getBoundingBox().maxx-o1.getBoundingBox().maxx);
				return (int)Math.signum(diff);
			}
		});
	}
	
	/**
	 * creates a default decoration detector
	 */
	public DecorationDetector() {
		this(10, 0.25f, 10000f);
	}
	
	private static float baseSimilarity(Block block1, Block block2) {
		String text1 = block1.getText();
		String text2 = block2.getText();
		text1 = text1.replaceAll("[0-9]+", "@");
		text2 = text2.replaceAll("[0-9]+", "@");
		int editDistance = StringMatching.computeEditDistance(text1, text2);
		return 1.0f - (float)editDistance/(float)Math.max(text1.length(), text2.length());
	}
	
	private static float geometricSimilarity(Block block1, Block block2) {
		BoundingBox bbox1 = block1.getBoundingBox();
		BoundingBox bbox2 = block2.getBoundingBox();
		float maxArea = Math.max(bbox1.area(), bbox2.area());
		float intersectArea = Math.max(0f,bbox1.intersect(bbox2).area());
		return intersectArea/maxArea;
	}
	
	private static float similarity(Block block1, Block block2) {
		float baseSimilarity = baseSimilarity(block1,block2);
		float geometricSimilarity = geometricSimilarity(block1,block2);
		return baseSimilarity*geometricSimilarity;
	}
	
	private static int previousPageId(int pageId, int numPages) {
		int result = pageId - 2;
		if (numPages==2) {
			result = 1 - pageId;
		} else if (numPages==3 && pageId==1) {
			result = 0;
		} else if (pageId==0 && numPages%2==0) {
			result = numPages - 2;
		} else if (pageId==0 && numPages%2!=0) {
			result = numPages - 1;
		} else if (pageId==1 && numPages%2==0) {
			result = numPages - 1;
		} else if (pageId==1 && numPages%2!=0) {
			result = numPages - 2;
		}
		return result;
	}
	
	private static int nextPageId(int pageId, int numPages) {
		int result = pageId + 2;
		if (numPages==2) {
			result = 1 - pageId;
		} else if (numPages==3 && pageId==1) {
			result = 2;
		} else if (pageId==numPages-1 && numPages%2==0) {
			result = 1;
		} else if (pageId==numPages-1 && numPages%2!=0) {
			result = 0;
		} else if (pageId==numPages-2 && numPages%2==0) {
			result = 0;
		} else if (pageId==numPages-2 && numPages%2!=0) {
			result = 1;
		}
		return result;
	}
	
	private void detectCurrentDecorations(List<List<Block>> orderedBlocks, List<List<Block>> foundDecorations,
			BlockLabeling labeling) {
		int numPages = orderedBlocks.size();
		if (foundDecorations.size()==0) {
			for (int pageId=0; pageId<numPages; pageId++) {
				foundDecorations.add(new ArrayList<Block>());
			}
		}
		for (int pageId=0; pageId<numPages; pageId++) {
			List<Block> blocksOnPage = orderedBlocks.get(pageId);
			List<Block> decorationsOnPage = foundDecorations.get(pageId);
			for (int blockId=0; blockId<numLines; blockId++) {
				if (blockId>=blocksOnPage.size()) { continue; }
				Block block = blocksOnPage.get(blockId);
				float maxScore = 0.0f;
				for (int otherBlockId=0; otherBlockId<numLines; otherBlockId++) {
					float score = 0.0f;
					int count = 0;
					int previousPageId = previousPageId(pageId, numPages);
					List<Block> previousPage = orderedBlocks.get(previousPageId);
					if (previousPageId!=pageId && otherBlockId<previousPage.size()) {
						score += similarity(block, previousPage.get(otherBlockId));
						count++;
					}
					int nextPageId = nextPageId(pageId, numPages);
					List<Block> nextPage = orderedBlocks.get(nextPageId);
					if (nextPageId!=pageId && otherBlockId<nextPage.size()) {
						score += similarity(block, nextPage.get(otherBlockId));
						count++;
					}
					score /= count;
					if (score > maxScore) {
						maxScore = score;
					}
				}
				if (maxScore>=scoreThreshold && block.getBoundingBox().area()<areaThreshold) {
					labeling.setLabel(block, BlockLabel.Decoration);
					decorationsOnPage.add(block);
				}
			}
		}
	}
	
	@Override
	public void detect(Document doc, List<Block> pageBlocks, BlockLabeling labeling, 
			ReadingOrder readingOrder, BlockNeighborhood neighborHood, ArticleMetadataCollector articleMetadata) {
		List<List<Block>> foundDecorations = new ArrayList<List<Block>>();

		for (Comparator<Block> comparator : comparators) {
			List<List<Block>> orderedBlocks = new ArrayList<List<Block>>(pageBlocks.size());
			for (Block pageBlock : pageBlocks) {
				List<Block> textBlocks = new ArrayList<Block>(pageBlock.getSubBlocks());
				Collections.sort(textBlocks, comparator);
				orderedBlocks.add(textBlocks);
			}
			detectCurrentDecorations(orderedBlocks, foundDecorations, labeling);
		}
	}
	
	
}
