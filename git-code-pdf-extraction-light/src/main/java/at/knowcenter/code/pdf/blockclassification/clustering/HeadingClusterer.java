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
package at.knowcenter.code.pdf.blockclassification.clustering;

import gnu.trove.TIntHashSet;
import gnu.trove.iterator.TCharIntIterator;
import gnu.trove.map.hash.TCharIntHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import at.knowcenter.code.api.pdf.Block;
import at.knowcenter.code.api.pdf.BlockLabel;
import at.knowcenter.code.api.pdf.BoundingBox;
import at.knowcenter.code.api.pdf.Font;
import at.knowcenter.code.api.pdf.ReadingOrder;
import at.knowcenter.code.pdf.blockclassification.BlockLabeling;
import at.knowcenter.code.pdf.toc.DocumentStructureExtractor;

/**
 * 
 * @author sklampfl
 *
 */
public class HeadingClusterer extends DocumentStructureExtractor {

    private static final double MAX_DIGIT_RATIO = 0.3;
    private static final double MIN_TOP_DISTANCE = 5;
    private static final double MAX_BOTTOM_DISTANCE = 10;
    
    protected class HeadingBlockWithDistance extends HeadingBlock {

    	final Block previousBlock;
    	final Block nextBlock;
    	final float topDistance;
    	final float bottomDistance;
    	
		public HeadingBlockWithDistance(Block block, int blockIndex, Block previousBlock,
				Block nextBlock, float topDistance, float bottomDistance) {
			super(block, blockIndex);
			this.previousBlock = previousBlock;
			this.nextBlock = nextBlock;
			this.topDistance = topDistance;
			this.bottomDistance = bottomDistance;
		}
		
        @Override
        public String toString() {
            return String.format("HeadingBlockWithDistance [block=%s]", block);
        }
    	
    }
    
    protected class ClusterWithDistance extends Cluster {

    	float topDistance;
    	float bottomDistance;
    	
		public ClusterWithDistance(HeadingBlock block, int id,
				Map<Integer, Font> fonts) {
			super(block, id, fonts);
		}
		
		@Override
		protected void updateStats() {
			super.updateStats();
			int numBlocks = 0;
			for (HeadingBlock b : blocks) {
				if (b instanceof HeadingBlockWithDistance) {
					HeadingBlockWithDistance hb = (HeadingBlockWithDistance) b;
					if (hb.topDistance > 0 && hb.bottomDistance > 0) {
						topDistance += hb.topDistance;
						bottomDistance += hb.bottomDistance;
						numBlocks++;
					}
				}
			}
			if (numBlocks > 0) {
				topDistance /= numBlocks;
				bottomDistance /= numBlocks;
			}
		}
    	
        @Override
        public String toString() {
            return String.format(
                    "ClusterWithDistance [id=%s, ucStdevHeight=%s, ucMeanHeight=%s, isUpperCase=%s, isBold=%s, isItalic=%s, segmentCount=%s, blocks=%s]",
                    id, ucStdevHeight, ucMeanHeight, isUpperCase, isBold, isItalic, segmentCount, blocks);
        }
    }
    
	private static float verticalDistance(Block block1, Block block2) {
		if (block1==null || block2==null) {
			return -1;
		}
		BoundingBox bbox1 = block1.getBoundingBox();
		BoundingBox bbox2 = block2.getBoundingBox();
		return bbox2.miny - bbox1.maxy;
	}
	
	@Override
    protected List<Cluster> initializeClusters(List<HeadingBlock> headingBlocks, Map<Integer, Font> fonts) {
        List<Cluster> clusters = super.initializeClusters(headingBlocks, fonts);

        List<Cluster> result = new ArrayList<Cluster>(clusters.size());
        for (Cluster cluster : clusters) {
        	result.add(new ClusterWithDistance(cluster.blocks.iterator().next(), cluster.id, fonts));
        }
        
        return result;
    }

    @Override
    protected boolean isMergeCandidate(List<Cluster> clusters, Cluster a, Cluster b, double d) {
        boolean upperCaseClusterIsOutlier = isUpperCaseOutlier(clusters);
        boolean isMergeCandidate = true;
        if (a.segmentCount == b.segmentCount && a.segmentCount > 0 && b.segmentCount > 0) {
            // segments overrule everything else
        } else {
            if (d > Math.max(a.ucStdevHeight, b.ucStdevHeight)+0.01) {
                isMergeCandidate = false;
            } else if (a.isUpperCase != b.isUpperCase && !upperCaseClusterIsOutlier) {
                isMergeCandidate = false;
            } else if (a.segmentCount != b.segmentCount) {
                isMergeCandidate = false;
            } else if (a.isAdjacentTo(b)) {
                isMergeCandidate = false;
            } else if (a.hasDifferentFontTypes(b)) {
                isMergeCandidate = false;
            } else if (a.hasDifferentFragemtsSizes(b)) {
                isMergeCandidate = false;
//                    } else if (Math.abs(a.fragmentCount - b.fragmentCount) > Math.max(a.fragmentCount, b.fragmentCount)/2.0) {
//                        isMergeCandidate = false;
//                    } else if (!a.hasCommonFonts(b)) {
//                        isMergeCandidate = false;
            } else if (a instanceof ClusterWithDistance && b instanceof ClusterWithDistance) {
            	ClusterWithDistance ad = (ClusterWithDistance)a;
            	ClusterWithDistance bd = (ClusterWithDistance)b;
            	if (false) { // rkern: not sure, whether this is always the case
                    if (ad.topDistance > 0 && bd.topDistance > 0 && Math.abs(ad.topDistance - bd.topDistance) > 1) {
                        isMergeCandidate = false;
                    }
                }
                if (ad.bottomDistance > 0 && bd.bottomDistance > 0 && Math.abs(ad.bottomDistance - bd.bottomDistance) > 1) {
            		isMergeCandidate = false;
            	}
            }
        }
        return isMergeCandidate;
    }

    private List<HeadingBlock> getHeadingBlocks(List<Block> pageBlocks, BlockLabeling labeling, 
    		ReadingOrder readingOrder, Collection<Block> candidateHeadings) {
        List<HeadingBlock> headingBlocks = new ArrayList<HeadingBlock>();
        Block titleBlock = null;
        TIntHashSet segmentCounterSet = new TIntHashSet();
        int blockIndex = 0;
        for (int i = 0; i < pageBlocks.size(); i++) {
            Block pageBlock = pageBlocks.get(i);
            List<Integer> ro = readingOrder.getReadingOrder(i);
            List<Block> blocks = new ArrayList<Block>(pageBlock.getSubBlocks());
            for (int j = 0; j < ro.size(); j++) {
                Block currentBlock = blocks.get(ro.get(j));
                Block lastBlock = j==0 ? null : blocks.get(ro.get(j-1));
                Block nextBlock = j==ro.size()-1 ? null : blocks.get(ro.get(j+1));
                BlockLabel label = labeling.getLabel(currentBlock);
                
                if (label == BlockLabel.Title) {
                    titleBlock = currentBlock;
                } else if (candidateHeadings.contains(currentBlock)) {
                    boolean ignoreBlock = false;
                    
                    String blockText = currentBlock.toString();
                    for (String eh : headingsToExclude) {
                        if (eh.equalsIgnoreCase(blockText)) {
                            ignoreBlock = true;
                            break;
                        }
                    }
                    
                    if (!ignoreBlock) {
                    	float topDistance = verticalDistance(lastBlock, currentBlock);
                    	float bottomDistance = verticalDistance(currentBlock, nextBlock);
                        HeadingBlock headingBlock = new HeadingBlockWithDistance(currentBlock, blockIndex,
                        		lastBlock, nextBlock, topDistance, bottomDistance);
                        if (headingBlock.segmentCount > 0) {
                            segmentCounterSet.add(headingBlock.segmentCount);
                        }
                        headingBlocks.add(headingBlock);
                    }
                } 
                blockIndex++;
            }
        }
        return headingBlocks;
    }
    
	private Map<Integer, Font> getFontMap(List<Font> fonts) {
		Map<Integer, Font> fontMap = new HashMap<Integer, Font>();
        if (fonts != null) {
            for (Font font : fonts) {
                fontMap.put(font.getId(), font);
            }
        }
		return fontMap;
	}

	public List<Block> getSelectedHeadings(List<Block> pageBlocks, BlockLabeling labeling, 
    		ReadingOrder readingOrder, List<Font> fonts, Collection<Block> candidateHeadings) {
        List<HeadingBlock> headingBlocks = getHeadingBlocks(pageBlocks, labeling, readingOrder, candidateHeadings);
        Map<Integer, Font> fontMap = getFontMap(fonts);
        List<Cluster> clusters = initializeClusters(headingBlocks, fontMap);
        mergeClusters(clusters);
        SortedSet<Cluster> sortedClusters = sortClusters(clusters);
        Map<Block, Integer> headingLevelMap = new HashMap<Block, Integer>();
        
        int headingLevel = 1;
        for (Cluster c : sortedClusters) {
            for (HeadingBlock b : c.blocks) {
                System.out.println(String.format("* %s (%d, %.3f, %.3f, %.2f, %.2f, %s, %s)", b.block.getText(), b.segmentCount,
                        b.ucMeanHeight, b.ucStdevHeight, b.ucRatio, b.digitRatio, b.blockIndex, Arrays.toString(b.fontIds.keys())));
                headingLevelMap.put(b.block, headingLevel);
            }
            if (c instanceof ClusterWithDistance) {
            	ClusterWithDistance cd = (ClusterWithDistance)c;
            	System.out.println(String.format("top: %f, bottom: %f", cd.topDistance, cd.bottomDistance));
            }
            headingLevel++;
            System.out.println();
        }
        
        Set<Block> result = new HashSet<Block>();

        int numNonTrivialClusters = 0;
        int maxClusterSize = 0;
        for (Cluster c : sortedClusters) {
        	if (c.blocks.size() > 1) {
        		numNonTrivialClusters++;
        	}
        	if (c.blocks.size() > maxClusterSize) {
        		maxClusterSize = c.blocks.size();
        	}
        }
        
        if (numNonTrivialClusters==1) {
        	
        	for (Cluster c : sortedClusters) {
        		if (c.blocks.size() > 1) {
	        		for (HeadingBlock b : c.blocks) {
	        			result.add(b.block);
	        		}
        		}
        	}
        	
        } else {
        
        	Set<Cluster> ignoreClusters = new HashSet<Cluster>();
	        for (Cluster c : sortedClusters) {
//	        	if (c.blocks.size() <= 1) {
//	        		continue;
//	        	}
	        	if (c instanceof ClusterWithDistance) {
	        		ClusterWithDistance cd = (ClusterWithDistance)c;
	        		if (cd.topDistance < MIN_TOP_DISTANCE && cd.bottomDistance > MAX_BOTTOM_DISTANCE) {
	        			continue;
	        		}
	        	}
//	        	TCharIntHashMap endCharCount = new TCharIntHashMap();
//	            for (HeadingBlock b : c.blocks) {
////	                System.out.println(String.format("* %s (%d, %.3f, %.3f, %.2f, %.2f, %s, %s)", 
////	                		b.block.getText(), b.segmentCount, b.ucMeanHeight, b.ucStdevHeight,
////	                		b.ucRatio, b.digitRatio, b.blockIndex, Arrays.toString(b.fontIds.keys())));
//	                char lastChar = getLastChar(b.block.getText());
//	                endCharCount.adjustOrPutValue(lastChar, 1, 1);
//	            }
//	            char majorityEndChar = getMajorityChar(endCharCount);
	            //avgDigitRatio /= c.blocks.size();
	            //System.out.println(endChars);
//	            if (maxDigitRatio > MAX_DIGIT_RATIO || endChars.size() > 1) {
//	            	continue;
//	            }
	        	int numPotentiallyInvalidHeadings = 0;
	            for (HeadingBlock b : c.blocks) {
	                String text = b.block.getText();
	                char lastChar = getLastChar(text);
					boolean validBlock = b.digitRatio < MAX_DIGIT_RATIO;
	                validBlock = validBlock && !text.startsWith("Theorem");
	                validBlock = validBlock && !text.startsWith("Proof");
	                validBlock = validBlock && (Character.isUpperCase(text.charAt(0)) || lastChar==' ');
	                validBlock = validBlock && (lastChar!=',' && lastChar!=';');
	                if (b instanceof HeadingBlockWithDistance) {
	                	HeadingBlockWithDistance hb = (HeadingBlockWithDistance)b;
	                	if (labeling.hasLabel(hb.previousBlock, BlockLabel.Main) && hb.topDistance > 0) {
	                		validBlock = validBlock && hb.topDistance > MIN_TOP_DISTANCE;
	                	}
	                	if (hb.nextBlock != null) {
	                		char nextChar = hb.nextBlock.getText().charAt(0);
	                		numPotentiallyInvalidHeadings += Character.isLowerCase(nextChar) ? 1 : 0;
	                	}
	                }
					if (validBlock) {
	                	result.add(b.block);
	                } else if (c.blocks.size() < maxClusterSize/2) {
	                	ignoreClusters.add(c);
	                }
	            }
	            if (numPotentiallyInvalidHeadings == c.blocks.size()) {
	            	ignoreClusters.add(c);
	            }
	        }
	        
	        
	        for (Cluster c : ignoreClusters) {
	        	for (HeadingBlock b : c.blocks) {
	        		result.remove(b.block);
	        	}
	        }
	        
        }
        
        postprocessWithReadingOrder(result, headingLevelMap, pageBlocks, readingOrder, labeling);
        
        return new ArrayList<Block>(result);
    }
	
	private void postprocessWithReadingOrder(Set<Block> candidates, Map<Block, Integer> headingLevelMap, 
			List<Block> pageBlocks, ReadingOrder readingOrder, BlockLabeling labeling) {
		// here we look at adjacent headings and if their heading level is not strictly descending remove
		// all but the last heading
		Set<Block> headingsToRemove = new HashSet<Block>();
		List<Block> adjacentHeadings = new ArrayList<Block>();
		for (int pageId=0; pageId<pageBlocks.size(); pageId++) {
			Block page = pageBlocks.get(pageId);
			Block[] textBlocks = page.getSubBlocks().toArray(new Block[0]);
			List<Integer> order = readingOrder.getReadingOrder(pageId);
			for (int blockId = 0; blockId < order.size(); blockId++) {
				Block currentBlock = textBlocks[order.get(blockId)];
				if (labeling.hasLabel(currentBlock, BlockLabel.Main)) {
					if (adjacentHeadings.size() > 1) {
						for (int headingId = 1; headingId < adjacentHeadings.size(); headingId++) {
							int lastLevel = headingLevelMap.get(adjacentHeadings.get(headingId-1));
							int level = headingLevelMap.get(adjacentHeadings.get(headingId));
							if (lastLevel >= level) {
								headingsToRemove.addAll(adjacentHeadings.subList(0, adjacentHeadings.size()-1));
								break;
							}
						}
					}
					adjacentHeadings.clear();
				}
				if (candidates.contains(currentBlock)) {
					adjacentHeadings.add(currentBlock);
				}
			}
		}
		headingsToRemove.addAll(adjacentHeadings);
		candidates.removeAll(headingsToRemove);
	}
	
	private char getLastChar(String text) {
		char lastChar = text.charAt(text.length()-1);
        if (Character.isLetterOrDigit(lastChar) || 
        		lastChar==')' || lastChar=='?' || lastChar==':') {
        	lastChar = ' ';
        }
        return lastChar;
	}
	
	private char getMajorityChar(TCharIntHashMap charCount) {
        int maxFreq = 0;
        char majorityChar = ' ';
        for (TCharIntIterator it = charCount.iterator(); it.hasNext(); ) {
        	it.advance();
        	if (it.value() > maxFreq) {
        		maxFreq = it.value();
        		majorityChar = it.key();
        	}
        }
        return majorityChar;
	}


}
