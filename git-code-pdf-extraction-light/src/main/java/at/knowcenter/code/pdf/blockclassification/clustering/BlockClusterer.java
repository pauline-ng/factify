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

import java.util.List;
import java.util.Set;

import at.knowcenter.code.api.pdf.Block;


/**
 * Interface for clusterers that operate on text blocks. This is used for main text detection
 * 
 * @author sklampfl
 *
 */
public abstract class BlockClusterer {
	
	/**
	 * the current set of clusters (modified during operation)
	 */
	protected Set<BlockCluster> clusters;
		
	/**
	 * this should be overridden by subclasses
	 * @param blocks the list of blocks to cluster
	 * @param dm the distance measure used
	 * @return the final set of clusters
	 */
	public abstract Set<BlockCluster> execute(List<Block> blocks, DistanceMeasure dm);
	
	/**
	 * utility method for calculating the total size of the given data set in lines
	 * @return the number of total lines
	 */
	public int getTotalLines() {
		int result = 0;
		if (clusters!=null) {
			for (BlockCluster cluster : clusters) {
				result += cluster.sizeInLines();
			}
		}
		return result;
	}

}
