/* Copyright (C) 2010 
"Kompetenzzentrum fuer wissensbasierte Anwendungen Forschungs- und EntwicklungsgmbH" 
(Know-Center), Graz, Austria, office@know-center.at.

Licensees holding valid Know-Center Commercial licenses may use this file in
accordance with the Know-Center Commercial License Agreement provided with 
the Software or, alternatively, in accordance with the terms contained in
a written agreement between Licensees and Know-Center.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package at.knowcenter.code.pdf.toc;

import gnu.trove.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import at.knowcenter.code.api.pdf.Block;
import at.knowcenter.code.api.pdf.BlockLabel;
import at.knowcenter.code.api.pdf.BoundingBox;
import at.knowcenter.code.api.pdf.ReadingOrder;
import at.knowcenter.code.pdf.blockclassification.BlockLabeling;

/**
 * Extractor for paragraphs out of blocks, where blocks are split and merged.
 * 
 * @author rkern@know-center.at
 */
public class ParagraphExtractor {
    /** float MAX_OUTLIER_TO_NORMAL_RATIO */
    private static final float MAX_OUTLIER_TO_NORMAL_RATIO = .2f;
    /** int MIN_RELATIVE_DISTANCE */
    private static final float MIN_RELATIVE_DISTANCE = 1.3f;

    /**
     * Extracts the paragraph information
     * @param pageBlocks the blocks from all pages
     * @param labeling optional labeling, defaults to main for all blocks if null
     * @param readingOrder the reading sequence
     * @return the paragraphs
     */
    public ParagraphInformation extract(List<Block> pageBlocks, BlockLabeling labeling,
    		ReadingOrder readingOrder) {
        List<Block> firstLineLeftOutlierBlocks = new ArrayList<Block>(pageBlocks.size());
        List<Block> lastLineOutlierBlocks = new ArrayList<Block>(pageBlocks.size());
        Map<Block, Block> blocksToPrecedingBlockMap = new LinkedHashMap<Block, Block>(pageBlocks.size());
        Map<Block, int[]> blocksToLeftOutlierLines = new HashMap<Block, int[]>();
        Block previousMainTextBlock = null;
        for (int i = 0; i < pageBlocks.size(); i++) {
            Block pageBlock = pageBlocks.get(i);
            List<Integer> ro = readingOrder.getReadingOrder(i);
            List<Block> blocks = new ArrayList<Block>(pageBlock.getSubBlocks());
            for (int j = 0; j < ro.size(); j++) {
                Block currentBlock = blocks.get(ro.get(j));
                BlockLabel label = labeling != null ? labeling.getLabel(currentBlock) : BlockLabel.Main;
                
                if (label == BlockLabel.Main) {
                    TFloatArrayList starts = new TFloatArrayList();
                    TFloatArrayList ends = new TFloatArrayList();
                    float minStart = Float.NaN, maxStart = Float.NaN, 
                          minEnd = Float.NaN, maxEnd = Float.NaN;
                    List<Block> lineBlocks = currentBlock.getLineBlocks();
                    float avgFragSize = 0;
                    for (Block line : lineBlocks) {
                        float minx = line.getBoundingBox().minx;
                        float maxx = line.getBoundingBox().maxx;
                        minStart = Float.isNaN(minStart) || minx < minStart ? minx : minStart;
                        maxStart = Float.isNaN(maxStart) || minx > maxStart ? minx : maxStart;
                        minEnd = Float.isNaN(minEnd) || maxx < minEnd ? maxx : minEnd;
                        maxEnd = Float.isNaN(maxEnd) || maxx > maxEnd ? maxx : maxEnd;
                        starts.add(minx);
                        ends.add(maxx);
                        avgFragSize += (maxx-minx)/line.getFragments().size();
                    }
                    avgFragSize /= lineBlocks.size();
                    int leftAlignedCounter = 0;
                    int rightAlignedCounter = 0;
                    for (int k = 0; k < starts.size(); k++) {
                        float x = starts.getQuick(k);
                        if (x <= minStart+avgFragSize) { leftAlignedCounter++; }
                    }
                    for (int k = 0; k < ends.size(); k++) {
                        float x = ends.getQuick(k);
                        if (x >= maxEnd-avgFragSize) { rightAlignedCounter++; }
                    }
                    boolean isLeftAligned = (double)leftAlignedCounter / starts.size() > 0.66;
                    boolean isRightAligned = (double)rightAlignedCounter / ends.size() > 0.66;
                    
                    boolean isFirstLineLeftOutlier;
                    OutlierInformation leftOutliers = null;
                    if (isLeftAligned) {
                        leftOutliers = detectOutliers(starts, minStart, maxStart, avgFragSize, true);
                        isFirstLineLeftOutlier = leftOutliers.hasOutliers && leftOutliers.outlierLineIndices.contains(0);
                    } else {
                        isFirstLineLeftOutlier = false;
                    }
                    boolean isLastLineOutlier;
//                    OutlierInformation rightOutliers = null;
                    if (isRightAligned) {
                        TIntArrayList rightOutlierIndices = new TIntArrayList();
                        for (int k = 0; k < ends.size(); k++) {
                            float x = ends.getQuick(k);
                            if (x <= maxEnd-(2*avgFragSize)) { rightOutlierIndices.add(k); }
                        }
//                        rightOutliers = detectOutliers(ends, minEnd, maxEnd, avgFragSize, false);
//                        boolean hasRightOutliers = rightOutliers.hasOutliers;
                        if (rightOutlierIndices.size() > 0) {
                            isLastLineOutlier = rightOutlierIndices.contains(ends.size()-1);
                            int[] indices = isLastLineOutlier ? 
                                    rightOutlierIndices.toArray(0, rightOutlierIndices.size()-1) : rightOutlierIndices.toArray();
//                            int[] indices = rightOutlierIndices.toArray(); 
                            for (int k = 0; k < indices.length; k++) { indices[k]++; }
                            blocksToLeftOutlierLines.put(currentBlock, indices);
                        } else {
                            isLastLineOutlier = false;
                        }
                    } else {
                        isLastLineOutlier = false;
                    }
                    
                    // TODO: find outliers in regard of line start for splitting of blocks
                    if (leftOutliers != null && leftOutliers.hasOutliers) {
                        int[] indices = isFirstLineLeftOutlier ? 
                                leftOutliers.outlierLineIndices.toArray(1, leftOutliers.outlierLineIndices.size()-1) : leftOutliers.outlierLineIndices.toArray();
                        blocksToLeftOutlierLines.put(currentBlock, indices);
                    }
                    
                    if (isFirstLineLeftOutlier) {
                        firstLineLeftOutlierBlocks.add(currentBlock);
                    } else {
                        if (previousMainTextBlock != null) {
                            blocksToPrecedingBlockMap.put(currentBlock, previousMainTextBlock);
                        }
                    }
                    
                    if (isLastLineOutlier) {
                        lastLineOutlierBlocks.add(currentBlock);
                        previousMainTextBlock = null;
                    } else { 
                        previousMainTextBlock = currentBlock;
                    }
                } else if (label == BlockLabel.Heading) {
                    previousMainTextBlock = null;
                }
            }
        }
        
        Map<Block, Block> mergeSourceToTargetMap = new LinkedHashMap<Block, Block>(blocksToPrecedingBlockMap.size());
        Set<Block> mergeTargetSet = new HashSet<Block>(blocksToPrecedingBlockMap.size());
        for (Entry<Block, Block> e : blocksToPrecedingBlockMap.entrySet()) {
            boolean isCandidate = false;
            if (e.getKey().getPage().getNumber() != e.getValue().getPage().getNumber()) {
                // are on different pages
                isCandidate = true;
            } else {
                // are on different columns
                BoundingBox bbox1 = e.getKey().getBoundingBox();
                BoundingBox bbox2 = e.getValue().getBoundingBox();
                float c = bbox1.minx + .5f*(bbox1.maxx - bbox1.minx);
                if (c < bbox2.minx || c > bbox2.maxx) {
                    isCandidate = true;
                }
            }
            if (isCandidate) {
                mergeSourceToTargetMap.put(e.getKey(), e.getValue());
                mergeTargetSet.add(e.getValue());
            }
        }
//        for(Block block : mergeTargetSet) {
//        	System.out.println("--------\r\n" + block.getText() + "--------\r\n");
//        }
        return new ParagraphInformation(mergeSourceToTargetMap, mergeTargetSet, blocksToLeftOutlierLines);
    }
    
    private OutlierInformation detectOutliers(TFloatArrayList values, float min, float max, float avgFragSize, boolean isHigher) {
        // TODO: implement k-means (multiple iterations)
        TIntArrayList mn = new TIntArrayList(values.size()), 
                      mx = new TIntArrayList(values.size());
        float cn = 0, cx = 0, ocn = min, ocx = max;
        int nn = 0, nx = 0;
        for (int i = 0; i < values.size(); i++)  {
            float v = values.getQuick(i);
            float dn = Math.abs(v-ocn), dx = Math.abs(ocx-v);
            if (dn < dx) {
                mn.add(i);
                cn += v;
                nn++;
            } else {
                mx.add(i);
                cx += v;
                nx++;
            }
        }
        cn /= nn;
        cx /= nx;
        
        float rel = (cx - cn) / avgFragSize;
        
        if (isHigher) {
            //return nx == 1 && rel > 1 && mx.contains(line);
            return new OutlierInformation((float)nx / (nx+nn), rel, mx);
        } else {
            //return nn == 1 && rel > 1 && mn.contains(line);
            return new OutlierInformation((float)nn / (nx+nn), rel, mn);
        }
    }
    
    static class OutlierInformation {
        final float outlierToNormaRatio;
        final float relativeDistance;
        final TIntArrayList outlierLineIndices;
        final boolean hasOutliers;

        /**
         * Creates a new instance of this class.
         * @param outlierToNormalRatio 
         * @param relativeDistance 
         * @param outlierLineIndices 
         */
        public OutlierInformation(float outlierToNormalRatio, float relativeDistance, TIntArrayList outlierLineIndices) {
            this.outlierToNormaRatio = outlierToNormalRatio;
            this.relativeDistance = relativeDistance;
            this.hasOutliers = relativeDistance > MIN_RELATIVE_DISTANCE && outlierToNormalRatio < MAX_OUTLIER_TO_NORMAL_RATIO;
            this.outlierLineIndices = outlierLineIndices;
        }
        
    }

}

