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
import at.knowcenter.code.api.pdf.Page;
import at.knowcenter.code.api.pdf.TextFragment;

/**
 * 
 * 
 * @author Roman Kern <rkern@tugraz.at>
 */
public class LineCleaner extends AbstractSplitter {

    private final Block lines;
    private final Page page;

    /**
     * Creates a new instance of this class.
     * @param merge
     */
    public LineCleaner(Page page, Block lineCollection) {
        this.lines = lineCollection;
        this.page = page;
    }

    /**
     * @return
     */
    public Block clean() {
        SortedSet<Block> result = new TreeSet<Block>(Block.VERTICAL_FRAGMENTS);
        
        for (Block line : lines.getSubBlocks()) {
            SortedSet<Block> words = line.getSubBlocks();
            double lastPos = Double.NaN;
            TextFragment prev = null;
            SortedSet<Block> currentLine = new TreeSet<Block>(Block.HORIZONTAL_FRAGMENTS);
            for (Block word : words) {
                List<TextFragment> frags = word.getFragments();
                TextFragment last = frags.get(frags.size()-1);
                if (!Double.isNaN(lastPos)) {
                	TextFragment first = frags.get(0);
                    if (lastPos - first.getX()  >= 50 && overlapY(first, prev) > 0.5) {
                        currentLine.clear();
                    }
                }
                currentLine.add(word);
                lastPos = last.getX()/*+last.width*/;
                prev = last;
            }
            if (!currentLine.isEmpty()) {
                Block c = new Block(page, currentLine);
                result.add(c);
            }
        }
        
        return new Block(page, result);
    }

    /**
     * @param first
     * @param prev
     * @return
     */
    private double overlapY(TextFragment first, TextFragment prev) {
        double by = Math.max(first.getY(), prev.getY()), cy = Math.min(first.getY()+first.getHeight(), prev.getY()+prev.getHeight()),
                s = Math.min(first.getHeight(), prev.getHeight());
         double i = cy-by;
         return i >= 0 ? i / s : 0;
    }

}
