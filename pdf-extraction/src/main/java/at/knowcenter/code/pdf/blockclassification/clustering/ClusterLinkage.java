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

/**
 * Provides a method for calculating the distance between two clusters
 * 
 * @author sklampfl
 * @deprecated
 */
public interface ClusterLinkage {
	
	/**
	 * the single link distance (i.e. the minimum distance between any pair of instances of
	 * different clusters)
	 */
	public final static ClusterLinkage SINGLE_LINK = new ClusterLinkage() {
		@Override
		public float distance(BlockCluster c1, BlockCluster c2,	DistanceMeasure dm) {
			float minDistance = Float.MAX_VALUE;
			List<BlockInstance> instances1 = c1.getBlockInstances();
			List<BlockInstance> instances2 = c2.getBlockInstances();
			for (BlockInstance instance1 : instances1) {
				for (BlockInstance instance2 : instances2) {
					float distance = dm.distance(instance1, instance2);
					if (distance<minDistance) {
						minDistance = distance;
					}
				}
			}
			return minDistance;
		}
	};
	
	/**
	 * calculates the distance between two {@link BlockCluster}s for the given {@link DistanceMeasure}.
	 * @param c1 the first cluster
	 * @param c2 the second cluster
	 * @param dm the distance measure
	 * @return the distance
	 */
	public float distance(BlockCluster c1, BlockCluster c2, DistanceMeasure dm);

}
