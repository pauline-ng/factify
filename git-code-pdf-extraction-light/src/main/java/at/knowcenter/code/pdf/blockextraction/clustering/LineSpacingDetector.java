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
import java.util.TreeSet;

import at.knowcenter.code.api.pdf.Block;
import at.knowcenter.code.api.pdf.TextFragment;

/**
 * Helper to compute the average line spacing of all lines.
 * 
 * @author Roman Kern <rkern@tugraz.at>
 */
public class LineSpacingDetector {
    private double lineSpacing;

    /**
     * Creates a new instance of this class.
     * @param lines
     */
    public LineSpacingDetector(Block lines) {
        TreeSet<Block> set = new TreeSet<Block>(
                Block.VERTICAL_FRAGMENTS);
        set.addAll(lines.getSubBlocks());
        
        double end = Double.NaN;
        Stats stats = new Stats();
        for (Block c : set) {
            List<TextFragment> fragments = c.getFragments();
            double delta;
            if (!Double.isNaN(end)) {
                delta = BlockSplitter.doGetDelta(fragments, end) / BlockSplitter.getAvgHeight(fragments);
                if (delta > 1 && delta < 5) {
                    stats.increment(delta);
                }
            } else {
                delta = 0;
            }
            end = BlockSplitter.doGetEnd(fragments);
        }
        lineSpacing = stats.getMaxExpected(0.2); // mean + 0.2*stdev
        if (Double.isNaN(lineSpacing)) {
            lineSpacing = 1.5;
        }
    }

    /**
     * The expected line spacing
     * @return
     */
    public double getLineSpacing() {
        return lineSpacing;
    }

}
