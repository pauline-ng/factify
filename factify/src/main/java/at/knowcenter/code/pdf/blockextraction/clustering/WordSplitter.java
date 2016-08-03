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
import at.knowcenter.code.api.pdf.TextFragment;

/**
 * 
 * 
 * @author Roman Kern <rkern@tugraz.at>
 */
public class WordSplitter extends AbstractSplitter {

    private final Block wordCollection;
    private final Page page;

    /**
     * Creates a new instance of this class.
     * @param wordCollection 
     */
    public WordSplitter(Page page, Block wordCollection) {
    	this.page = page;
        this.wordCollection = wordCollection;
    }

    /**
     * @return
     */
    public Block split() {
        SortedSet<Block> result = new TreeSet<Block>(Block.HORIZONTAL_FRAGMENTS);
        boolean debug = false;
        
        for (Block collection : wordCollection.getSubBlocks()) {
            List<TextFragment> fragments = collection.getFragments();
            
            if (fragments.size() < 6) {
                result.add(new Block(page, fragments));
            } else {
                double maxDistance = getCutpoint(fragments);
                if (!Double.isNaN(maxDistance)) {
                    double lastPos = Double.NaN;
                    List<TextFragment> currentWord = new ArrayList<TextFragment>();
                    if (debug) { System.out.print("Splitting word [" + collection + "] into: "); };
                    for (TextFragment f : fragments) {
                        if (!Double.isNaN(lastPos)) {
                            if ((getDelta(Arrays.asList(f), lastPos)) >= maxDistance) {
                                Block c = new Block(page, currentWord);
                                result.add(c);
                                if (debug) { System.out.print("["+c+"]"); };
                                currentWord = new ArrayList<TextFragment>();
                            }
                        }
                        currentWord.add(f);
                        lastPos = getEnd(Arrays.asList(f)); 
                    }
                    if (!currentWord.isEmpty()) {
                        Block c = new Block(page, currentWord);
                        result.add(c);
                        if (debug) { System.out.print("["+c+"]"); };
                    }
                    if (debug) { System.out.println(); };
                } else {
                    result.add(new Block(page, fragments));
                }
            }
        }
        
        return new Block(page, result);
    }

}
