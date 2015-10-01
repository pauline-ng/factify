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
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import at.knowcenter.code.api.pdf.Block;
import at.knowcenter.code.api.pdf.BlockLabel;
import at.knowcenter.code.api.pdf.Document;
import at.knowcenter.code.api.pdf.ReadingOrder;
import at.knowcenter.code.pdf.blockclassification.BlockLabeling;
import at.knowcenter.code.pdf.blockclassification.clustering.BlockCluster;
import at.knowcenter.code.pdf.blockclassification.clustering.BlockClusterer;
import at.knowcenter.code.pdf.blockclassification.clustering.BlockInstance;
import at.knowcenter.code.pdf.blockclassification.clustering.DistanceMeasure;
import at.knowcenter.code.pdf.blockclassification.clustering.HierarchicalAgglomerativeClusterer;
import at.knowcenter.code.pdf.blockclassification.clustering.HierarchicalAgglomerativeClusterer.ClusterSimilarity;
import at.knowcenter.code.pdf.blockclassification.detection.metadata.ArticleMetadataCollector;
import at.knowcenter.code.pdf.blockrelation.geometric.BlockNeighborhood;

/**
 * implements a detector for labelling text blocks belonging to the main text.
 * 
 * Note that several Detectors, such as {@link HeadingDetector} or {@link SparseLineAndTableDetector}
 * make use of main text labels, so this detector should be called before.
 * 
 * This detector only labels blocks that have not yet been labelled.
 * 
 * This detector performs a clustering of text blocks through the {@link HierarchicalAgglomerativeClusterer}.
 * It uses two features of text blocks: xmin and width. It then sorts the obtained clusters by their
 * size in text lines in descending order. Blocks in the largest cluster are classified as main text.
 * The next larger clusters are also classified as main text until one of the following condition is met:
 * i)  the mean width of the blocks in the cluster changes by more than a specified amount
 * ii) the majority font size of the blocks changes AND the size of the next cluster changes by more than
 * a specified amount.
 * 
 * @author sklampfl
 *
 */
public class MainTextDetector implements Detector {
	
	/**
	 * the distance measure used for clustering.
	 * 
	 * It is the standard euclidean distance; additionally the distance for blocks with
	 * different majority font size is set to infinity, i.e., blocks with different font sizes
	 * are certainly in different clusters.
	 * 
	 * @author sklampfl
	 *
	 */
	public class MainTextDistance implements DistanceMeasure {

		@Override
		public float distance(BlockInstance instance1, BlockInstance instance2) {
			
//			int id1 = (Integer) instance1.get("majorityFontId");
//			int id2 = (Integer) instance2.get("majorityFontId");
//			Font font1 = document.getFonts().get(id1);
//			Font font2 = document.getFonts().get(id2);
			int size1 = (Integer) instance1.get(FONT_SIZE_FEATURE);
			int size2 = (Integer) instance2.get(FONT_SIZE_FEATURE);
//			String startWord1 = (String) instance1.get("firstWord");
//			String startWord2 = (String) instance2.get("firstWord");
			
//			for (String startWord : captionStartWords) {
//				if (startWord1.equalsIgnoreCase(startWord) || startWord2.equalsIgnoreCase(startWord)) {
//					return Float.POSITIVE_INFINITY;
//				}
//			}
			
			if (size1 != size2) {
				return Float.POSITIVE_INFINITY;
			}
			
			return DistanceMeasure.EUCLIDEAN_DISTANCE.distance(instance1, instance2);
		}

	}

//	private final float totalSizeThreshold;
	private final float sizeDiffThreshold;
	private final float distanceThreshold;
	private final float widthDiffThreshold;
	
	private final static String FONT_SIZE_FEATURE = "majorityFontSize";
	
	/**
	 * creates a new instance
	 * @param document the document (used for font information)
	 * @param distanceThreshold the distance threshold (stoppage criterion for HAC)
	 * @param widthDiffThreshold upper threshold for the allowed width difference
	 * @param sizeDiffThreshold upper threshold for the allowed cluster size difference
	 */
	public MainTextDetector(float distanceThreshold, float widthDiffThreshold,
			float sizeDiffThreshold) {
		this.distanceThreshold = distanceThreshold;
		this.sizeDiffThreshold = sizeDiffThreshold;
//		this.totalSizeThreshold = totalSizeThreshold;
		this.widthDiffThreshold = widthDiffThreshold;
	}
	
	/**
	 * creates a default main text detector
	 */
	public MainTextDetector() {
		this(10f, 10f, 0.1f);
	}
	
	private static void printBlocks(List<Block> blocks) {
		StringBuilder buffer = new StringBuilder();
		buffer.append('[');
		for (Block block : blocks) {
			buffer.append('[').append(block.getBoundingBox().minx).append(',');
			buffer.append(block.getBoundingBox().getWidth()).append(']').append(',');
		}
		buffer.deleteCharAt(buffer.length()-1);
		buffer.append(']');
		System.out.println(buffer.toString());		
	}
	
	protected boolean isGarbage(Block block) {
		return block.getWordBlocks().size() < 2;
	}
	
	@Override
	public void detect(Document document, List<Block> pageBlocks, BlockLabeling labeling, 
			ReadingOrder readingOrder, BlockNeighborhood neighborHood, ArticleMetadataCollector articleMetadata) {
		
		List<Block> blocks = new ArrayList<Block>();
		for (Block page : pageBlocks) {
			for (Block block : page.getSubBlocks()) {
				if (!labeling.hasLabel(block, BlockLabel.Decoration) && !isGarbage(block)) {
					blocks.add(block);
				}
			}
		}
		
		//printBlocks(blocks);
		//List<Block> mainTextBlocks = new ArrayList<Block>();
		
//		BlockClusterer clusterer = new SimpleHAC(distanceThreshold, ClusterLinkage.SINGLE_LINK);
		BlockClusterer clusterer = new HierarchicalAgglomerativeClusterer(distanceThreshold, ClusterSimilarity.SingleLink);
		Set<BlockCluster> clusters = clusterer.execute(blocks, new MainTextDistance());
		int totalLines = clusterer.getTotalLines();
		SortedSet<BlockCluster> sortedClusters = new TreeSet<BlockCluster>(clusters);
		
		float pageWidth = document.getPages().get(0).getWidth();
		
		float totalSizeFraction = 0.0f;
		float lastSizeFraction = 0.0f;
		float sizeFraction = 0.0f;
		float meanWidth = 0.0f;
		float lastMeanWidth = 0.0f;
		int lastFontSize = 0;
		int currentFontSize = 0;
		int numMainBlocks = 0;
		int numMainClusters = 0;
		for (BlockCluster cluster : sortedClusters) {
			lastSizeFraction = sizeFraction;
			sizeFraction = (float)cluster.sizeInLines()/(float)totalLines;
			lastMeanWidth = meanWidth;
			meanWidth = cluster.meanWidth();
			lastFontSize = currentFontSize;
			currentFontSize = (Integer)cluster.getBlockInstances().get(0).get(FONT_SIZE_FEATURE);
			boolean largeWidthChange = lastMeanWidth>0f && Math.abs(meanWidth - lastMeanWidth) > widthDiffThreshold;
			boolean fontSizeChange = lastFontSize>0 && Math.abs(currentFontSize - lastFontSize) > 0;
			boolean largeSizeDiff = lastSizeFraction-sizeFraction>sizeDiffThreshold;
			if (largeWidthChange || (fontSizeChange && largeSizeDiff)) {
				break;
			}
			totalSizeFraction += sizeFraction;
			numMainClusters++;
			for (BlockInstance instance : cluster.getBlockInstances()) {
				Block block = instance.getBlock();
				if (labeling.getLabel(block)==null) {
					labeling.setLabel(block, BlockLabel.Main);
					//mainTextBlocks.add(block);
					numMainBlocks++;
				}
			}
		}
		
		//printBlocks(mainTextBlocks);
	}
}
