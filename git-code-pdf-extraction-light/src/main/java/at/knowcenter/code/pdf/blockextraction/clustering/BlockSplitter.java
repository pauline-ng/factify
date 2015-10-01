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

package at.knowcenter.code.pdf.blockextraction.clustering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import at.knowcenter.code.api.pdf.Block;
import at.knowcenter.code.api.pdf.Page;
import at.knowcenter.code.api.pdf.ReadingOrder;
import at.knowcenter.code.api.pdf.TextFragment;
import at.knowcenter.code.pdf.toc.ParagraphExtractor;
import at.knowcenter.code.pdf.toc.ParagraphInformation;

/**
 * 
 * 
 * @author Roman Kern <rkern@tugraz.at>
 */
public class BlockSplitter extends AbstractSplitter {

    private final Block blocks;
    private final Page page;

    /**
     * Creates a new instance of this class.
     * @param blockCollection 
     */
    public BlockSplitter(Page page, Block blockCollection) {
    	this.page = page;
        this.blocks = blockCollection;
    }

    /**
     * @return
     */
    public Block split() {
        SortedSet<Block> currentBlocks = new TreeSet<Block>(Block.VERTICAL_FRAGMENTS);
        boolean debug = false;

        for (Block block : blocks.getSubBlocks()) {
            SortedSet<Block> lines = block.getSubBlocks();
            double maxDistance = getCutpoint(lines, false);
            if (!Double.isNaN(maxDistance)) {
                double lastPos = Double.NaN;
                SortedSet<Block> currentBlock = new TreeSet<Block>(Block.VERTICAL_FRAGMENTS);
                if (debug) { System.out.print("Splitting block [" + block + "] into: "); };
                for (Block line : lines) {
                    List<TextFragment> frags = line.getFragments();
                    if (!Double.isNaN(lastPos)) {
                        if ((getDelta(frags, lastPos)) >= maxDistance) {
                            Block c = new Block(page, currentBlock);
                            currentBlocks.add(c);
                            if (debug) { System.out.print("["+c+"]"); };
                            currentBlock = new TreeSet<Block>(Block.VERTICAL_FRAGMENTS);
                        }
                    }
                    currentBlock.add(line);
                    lastPos = getEnd(frags);
                }
                if (!currentBlock.isEmpty()) {
                    Block c = new Block(page, currentBlock);
                    currentBlocks.add(c);
                    if (debug) { System.out.print("["+c+"]"); };
                }
                if (debug) { System.out.println(); };
            } else {
                currentBlocks.add(new Block(page, lines));
            }
        }
        
        SortedSet<Block> result = new TreeSet<Block>(Block.VERTICAL_FRAGMENTS);
        for (Block block : currentBlocks) {
            ParagraphExtractor pe = new ParagraphExtractor();
            SortedSet<Block> pageBlocks = new TreeSet<Block>(Block.VERTICAL_FRAGMENTS);
            pageBlocks.add(block);
            ParagraphInformation pi = pe.extract(Arrays.asList(new Block(page, pageBlocks)), null, new ReadingOrder(Arrays.asList(Arrays.asList(0))));
            int[] linesToSplit = pi.getLinesToSplit(block);
            if (linesToSplit != null && linesToSplit.length > 0) {
                List<Block> lineBlocks = new ArrayList<Block>(block.getLineBlocks());
                int to = lineBlocks.size();
                for (int i = linesToSplit.length-1; i >= -1; i--) {
                    int index = i >= 0 ? linesToSplit[i] : 0;
                    List<Block> subList = lineBlocks.subList(index, to);
                    SortedSet<Block> lines = new TreeSet<Block>(Block.VERTICAL_FRAGMENTS);
                    lines.addAll(subList);
                    result.add(new Block(page, lines));
                    to = index;
                    if (index == 0) { break; }
                }
                // result.remove(block);
            } else { 
                result.add(block);
            }
        }
        
        return new Block(page, result);
    }
    
    @Override
    protected double getDelta(List<TextFragment> current, double lastPos) {
        return doGetDelta(current, lastPos);
    }

    static double doGetDelta(List<TextFragment> current, double lastPos) {
        return getMinY(current)-lastPos;
    }
    
    static double getAvgHeight(List<TextFragment> current) {
        double m = 0;
        for (TextFragment f : current) {
            float d = f.getHeight();
            m += d;
        }
        return (float)(m / current.size());
    }
    
    @Override
    protected float getEnd(List<TextFragment> current) {
        return doGetEnd(current);
    }

    static float doGetEnd(List<TextFragment> current) {
        double m = 0;
        for (TextFragment f : current) {
            float d = f.getY()+(f.getHeight()/2.0f);
            m += d;
        }
        return (float)(m / current.size());
//        float m = 0;
//        for (TextFragment f : current) {
//            float d = f.getY()+f.getHeight();
//            if (d > m) {
//                m = d;
//            }
//        }
//        return m;
    }

    private static double getMinY(List<TextFragment> current) {
        double m = 0;
        for (TextFragment f : current) {
            float d = f.getY()+(f.getHeight()/2.0f);
            m += d;
        }
        return (float)(m / current.size());
//        double m = Double.NaN;
//        for (TextFragment f : current) {
//            double d = f.getY();
//            if (d < m || Double.isNaN(m)) {
//                m = d;
//            }
//        }
//        return m;
    }

    
}
