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

import java.util.Comparator;

import at.knowcenter.code.api.pdf.Block;
import at.knowcenter.code.api.pdf.Page;
import at.knowcenter.code.api.pdf.TextFragment;

/**
 * Merger for fragments by using a horizontal layout assumption.
 * 
 * @author Roman Kern <rkern@tugraz.at>
 */
public abstract class HorizontalFragmentMerger extends FragmentMerger {
    /** HorizontalFragmentMerger mergeThresholdX */
    protected final double mergeThresholdX;
    /** HorizontalFragmentMerger mergeThresholdY */
    protected final double mergeThresholdY;

    /**
     * Creates a new instance of this class.
     * @param fragments 
     * @param mergeThresholdX the threshold for merging horizontally 
     * @param mergeThresholdY the threshold for merging vertically
     */
    public HorizontalFragmentMerger(Page page, Iterable<TextFragment> fragments, double mergeThresholdX, double mergeThresholdY) {
        super(page, fragments, Cluster.VERTICAL_CLUSTERS);
        this.mergeThresholdX = mergeThresholdX;
        this.mergeThresholdY = mergeThresholdY;
    }
    
    /**
     * Creates a new instance of this class.
     * @param collection 
     * @param mergeThresholdX the threshold for merging horizontally 
     * @param mergeThresholdY the threshold for merging vertically
     */
    public HorizontalFragmentMerger(Page page, Block collection, double mergeThresholdX, double mergeThresholdY) {
        super(page, collection, Cluster.VERTICAL_CLUSTERS);
        this.mergeThresholdX = mergeThresholdX;
        this.mergeThresholdY = mergeThresholdY;
    }
    
    @Override
    protected double getMaxMergeHeight(double maxFragmentHeight) {
        return maxFragmentHeight*1.2;
    }

    @Override
    protected Comparator<TextFragment> getResultFragmentOrder() {
        return Cluster.HORIZONTAL_FRAGEMENTS;
    }

    @Override
    protected Comparator<Block> getResultCollectionOrder() {
        return Block.ID_FRAGMENTS;
        //return Block.HORIZONTAL_FRAGMENTS;
    } 
}
