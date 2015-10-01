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

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import at.knowcenter.code.api.pdf.Block;
import at.knowcenter.code.api.pdf.BoundingBox;
import at.knowcenter.code.api.pdf.Page;
import at.knowcenter.code.api.pdf.TextFragment;
import at.knowcenter.code.pdf.toc.DocumentStructureExtractor;

/**
 * 
 * 
 * @author Roman Kern <rkern@tugraz.at>
 */
public class HeadingLineCleaner extends AbstractSplitter {

    private final Block lines;
    private final Page page;

    /**
     * Creates a new instance of this class.
     * @param merge
     */
    public HeadingLineCleaner(Page page, Block lineCollection) {
        this.lines = lineCollection;
        this.page = page;
    }

    /**
     * @return
     */
    public Block clean() {
        SortedSet<Block> result = new TreeSet<Block>(Block.VERTICAL_FRAGMENTS);
        
        Block segmentLine = null;
        for (Block line : lines.getSubBlocks()) {
            SortedSet<Block> words = line.getSubBlocks();
            
            if (segmentLine == null) {
                if (words.size() == 1) {
                    Block firstWord = words.iterator().next();
                    String text = firstWord.toString().trim();
                    int segmentCounter = DocumentStructureExtractor.getSegmentCounter(text);
                    if (segmentCounter > 0) {
                        segmentLine = line;
                    }
                }
            } else {
                BoundingBox pbb = segmentLine.getBoundingBox();
                BoundingBox cbb = line.getBoundingBox();
                boolean isFound = false;
                if (pbb.getX() < cbb.getX()) {
                    float pmid = pbb.getY()+pbb.getHeight()/2;
                    if (pmid > cbb.getY() && pmid < cbb.getY()+cbb.getHeight()) {
                        isFound = true;
                    }
                }
                if (isFound) {
                    float pfs = segmentLine.getMeanFontSize();
                    float cfs = line.getMeanFontSize();
                    float rfs = page.getMeanFontSize();
                    rfs += rfs*.1f; // add 10% to minimum font size
                    if (pfs > rfs && cfs > rfs) {
                        words.addAll(segmentLine.getSubBlocks());
                    }
                } else {
                    result.add(segmentLine);
                }
                segmentLine = null;
            }
            
            if (segmentLine == null) {
                result.add(line);
            }
        }
        
        return new Block(page, result);
    }

}
