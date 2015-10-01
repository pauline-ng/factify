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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import at.knowcenter.code.api.pdf.Block;

/**
 * This class implements a very naive Hierarchical Agglomerative Clusterer with bad performance.
 * 
 * @author sklampfl
 * @deprecated
 */
public class SimpleHAC extends BlockClusterer {
	
	private final float distanceThreshold;
	private ClusterLinkage linkage;
	
	public SimpleHAC(float distanceThreshold, ClusterLinkage linkage) {
		this.distanceThreshold = distanceThreshold;
		this.linkage = linkage;
	}
	
	@Override
	public Set<BlockCluster> execute(List<Block> blocks, DistanceMeasure dm) {
		clusters = new HashSet<BlockCluster>();
		for (Block block : blocks) {
			clusters.add(new BlockCluster(new BlockInstance(block)));
		}

		float minDistance = 0.0f, distance = 0.0f;
		BlockCluster cluster1 = null, cluster2 = null;
		while (minDistance < distanceThreshold) {
			minDistance = Float.MAX_VALUE;
			cluster1 = null;
			cluster2 = null;
			for (BlockCluster c1 : clusters) {
				for (BlockCluster c2 : clusters) {
					if (c1 != c2) {
						distance = linkage.distance(c1, c2, dm);
						if (distance < minDistance) {
							cluster1 = c1;
							cluster2 = c2;
							minDistance = distance;
						}
					}
				}
			}
			clusters.remove(cluster2);
			cluster1.merge(cluster2);
		}
		
		return clusters;	
	}
	
}
