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
package at.knowcenter.code.pdf.blockrelation.readingorder;

import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import at.knowcenter.code.api.pdf.Block;
import at.knowcenter.code.api.pdf.BoundingBox;
import at.knowcenter.code.api.pdf.ReadingOrder;
import at.knowcenter.code.pdf.blockrelation.readingorder.Graph.Node;

/**
 * extracts the reading order from a list of blocks extracted from a PDF document
 * 
 * @author sklampfl
 *
 */
public class ReadingOrderExtractor {
	
	/**
	 * implements the BeforeInReading relation from Aiello et al., 2002
	 * 
	 * @param bbox1
	 * @param bbox2
	 * @param tol the tolerance in bounding box coordinates
	 * @return true, if bbox1 occurs sometimes before bbox2 in the reading order
	 */
	public static boolean beforeInReading(BoundingBox bbox1, BoundingBox bbox2, float tol) {
		boolean result = false;
		result = result || bbox1.precedesX(bbox2, tol);
		result = result || bbox1.meetsX(bbox2, tol);
		result = result || (bbox1.overlapsX(bbox2, tol) && 
				(bbox1.precedesY(bbox2, tol) || bbox1.meetsY(bbox2, tol) || bbox1.overlapsY(bbox2, tol)));
		result = result || ((bbox1.precedesY(bbox2, tol) || bbox1.meetsY(bbox2, tol) || bbox1.overlapsY(bbox2, tol)) &&
				(bbox1.precedesX(bbox2, tol) || bbox1.meetsX(bbox2, tol) || bbox1.overlapsX(bbox2, tol) || 
				 bbox1.startsX(bbox2, tol) || bbox2.finishesX(bbox1, tol) || bbox1.equalsX(bbox2, tol) ||
				 bbox1.duringX(bbox2, tol) || bbox2.duringX(bbox1, tol) || bbox1.finishesX(bbox2, tol) ||
				 bbox2.startsX(bbox1, tol) || bbox2.overlapsX(bbox1, tol)));
		return result;
	}
	
	/**
	 * calculates whether a block is rendered before another block in the rendering order of the PDF
	 * 
	 * @param block1
	 * @param block2
	 * @return true, if block1 is rendered before block2
	 */
	public static boolean beforeInRendering(Block block1, Block block2) {
		int maxSequence1 = block1.getMaximumSequenceId();
		int minSequence2 = block2.getMinimumSequenceId();
		return minSequence2 > maxSequence1;
	}
	
	private final float tol;

	/**
	 * creates a new extractor with the given tolerance
	 * @param tol the tolerance
	 */
	public ReadingOrderExtractor(float tol) {
		this.tol = tol;
	}
	
	/**
	 * creates a new extractor with tolerance 0.
	 */
	public ReadingOrderExtractor() {
		this.tol = 0f;
	}

	private Graph buildGraph(Block[] blocks) {
		Graph graph = new Graph(blocks.length);
		for (int i=0; i<blocks.length; i++) {
		    graph.setPriority(i, (int)blocks[i].getBoundingBox().getY());
			for (int j=0; j<blocks.length; j++) {
				boolean beforeInReading = beforeInReading(blocks[i].getBoundingBox(), blocks[j].getBoundingBox(), tol);
				boolean beforeInRendering = beforeInRendering(blocks[i], blocks[j]);
				if (beforeInRendering || beforeInReading) {
					graph.addEdge(i,j);
				}
			}
		}
		graph.removeSelfLoops();
		return graph;
	}
	
	private List<Integer> performTopologicalSort(Graph graph) {
		List<Node> nodeList = graph.getNodeList();
		int size = graph.getNumNodes();
        List<Integer> result = new ArrayList<Integer>(size);
		boolean failed = false;
		boolean[] foundNodes = new boolean[size];
		while (nodeList.size()>0 && !failed) {
			Node node = nodeList.get(0);
//			if (node.fanout!=nodeList.size()-1) {
//				failed = true;
//			} else {
				result.add(node.node);
				if (nodeList.size()==2) {
					Node node2 = nodeList.get(1);
                    result.add(node2.node);
	                foundNodes[node2.node] = true;
				}
				graph.removeNode(node.node);
				foundNodes[node.node] = true;
				nodeList = graph.getNodeList();
//			}
		}
		for (int i = 0; i < foundNodes.length; i++) {
            if (!foundNodes[i]) {
                result.add(i);
            }
        }
		assert size == result.size() : "The number of block indices (" + size + ") must match the number of blocks (" + result.size() + ")";
		return result;
	}
	
	private List<Integer> evaluateReadingOrder(Block pageBlock) {
		Set<Block> blocksToExpand = new THashSet<Block>();
		List<Block> blocksToSort = collapseBlocks(pageBlock, blocksToExpand);
		Block[] blockArray = blocksToSort.toArray(new Block[blocksToSort.size()]);
		
		Graph graph = buildGraph(blockArray);
		List<Integer> readingOrder = performTopologicalSort(graph);
		
		expandReadingOrder(readingOrder, blockArray, blocksToExpand);
		
		return readingOrder;
	}
	
	private List<Block> collapseBlocks(Block pageBlock,
			 Set<Block> blocksToExpand) {
		SortedSet<Block> blocks = pageBlock.getSubBlocks();
		List<Block> blocksToSort = new ArrayList<Block>(blocks.size()); 
		SortedSet<Block> garbageBlocks = new TreeSet<Block>(blocks.comparator());
		for(Block block: blocks) {
			if(isGarbage(block)) {
				garbageBlocks.add(block);
			} else  {
				if(!garbageBlocks.isEmpty()) {
					if(garbageBlocks.size() > 1) {
						Block combinedBlock = new Block(pageBlock.getPage(), garbageBlocks);
						blocksToSort.add(combinedBlock);
						blocksToExpand.add(combinedBlock);
						garbageBlocks = new TreeSet<Block>(blocks.comparator());
					} else {
						blocksToSort.add(garbageBlocks.iterator().next());
						garbageBlocks.clear();
					}
				}
				blocksToSort.add(block);
			}
		}
		
		if(!garbageBlocks.isEmpty()) {
			if(garbageBlocks.size() > 1) {
				Block combinedBlock = new Block(pageBlock.getPage(), garbageBlocks);
				blocksToSort.add(combinedBlock);
				blocksToExpand.add(combinedBlock);
			} else {
				blocksToSort.add(garbageBlocks.iterator().next());
			}
		}	
		
		return blocksToSort;
	}

	private void expandReadingOrder(List<Integer> readingOrder, Block[] blockArray,
			Set<Block> blocksToExpand) {
		for(int i = 0; i < blockArray.length; i++) {
			if(blocksToExpand.contains(blockArray[i])) {
				int iIndex = readingOrder.get(i);
				SortedSet<Block> subBlocks = blockArray[i].getSubBlocks();
				int adjust = subBlocks.size() - 1;
				int length = readingOrder.size();
				for(int j = 0; j < length; j++) {
					int jIndex = readingOrder.get(j);
					if(jIndex > iIndex) {
						readingOrder.set(j, jIndex + adjust);
					}
				}
				
				for(int j = 0; j < adjust; j++) {
					readingOrder.add(i + j + 1, iIndex + j + 1);
				}
			}
		}
	}

	private boolean isGarbage(Block block) {
		return block.getFragments().size() < 2;
	}

	/**
	 * evaluates the reading order for the given blocks
	 * @param pageBlocks a list of blocks corresponding to the pages
	 * @return the extracted reading order
	 */
	public ReadingOrder evaluateReadingOrder(List<Block> pageBlocks) {
		List<List<Integer>> readingOrders = new ArrayList<List<Integer>>(pageBlocks.size());
		for (Block page : pageBlocks) {
			readingOrders.add(evaluateReadingOrder(page));
		}
		return new ReadingOrder(readingOrders);
	}
	
//	public static void main(String[] args) throws PdfException, IOException {
//		File file = new File("data/1756-9966-27-85.pdf");
//        PdfParseResult parseResult = PdfTools.getPdfPages(file);
//        List<PdfPage> parsedPages = parseResult.getPages();
//        ReadingOrderDetector rod = new ReadingOrderDetector(5);
//        for (PdfPage page : parsedPages) {
//        	System.out.println("Page "+page.getPdfPage().number);
//        	List<Integer> readingOrder = rod.evaluateReadingOrder(page);
//        	System.out.println(readingOrder);
//        }
//	}

	
}
