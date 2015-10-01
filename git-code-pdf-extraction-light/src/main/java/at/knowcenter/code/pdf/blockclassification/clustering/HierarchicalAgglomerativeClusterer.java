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

import gnu.trove.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.knowcenter.code.api.pdf.Block;

/**
 * implements a hierarchical agglomerative clustering of text blocks
 * 
 * @author sklampfl
 *
 */
public class HierarchicalAgglomerativeClusterer extends BlockClusterer {

	public enum ClusterSimilarity {
		SingleLink,
		CompleteLink,
		AverageLink
	}
	
	private static class InstancePair {
		private final BlockInstance instance1;
		private final BlockInstance instance2;
		public InstancePair(BlockInstance instance1, BlockInstance instance2) {
			this.instance1 = instance1;
			this.instance2 = instance2;
		}
	}
	
	private static class DistanceMatrix {
		private final double[][] distances;
		private final double[][] originalDistances;
		private final double[][] cumulativeDistances;
		private final TObjectIntHashMap<BlockInstance> instanceToIdx;
		private final List<BlockInstance> instances;
		private final int numInstances;
		private final boolean[] activeClusters;
		private final int[] clusterSizes;
		
		public DistanceMatrix(List<BlockInstance> inputInstances, DistanceMeasure distanceMeasure) {
			this.numInstances = inputInstances.size();
			this.instances = inputInstances;
			this.distances = new double[numInstances][numInstances];
			this.originalDistances = new double[numInstances][numInstances];
			this.cumulativeDistances = new double[numInstances][numInstances];
			this.activeClusters = new boolean[numInstances];
			this.clusterSizes = new int[numInstances];
			this.instanceToIdx = new TObjectIntHashMap<BlockInstance>();
			for (int i = 0; i<numInstances; i++) {
				BlockInstance instance1 = inputInstances.get(i);
				this.instanceToIdx.put(instance1, i);
				this.activeClusters[i] = true;
				this.clusterSizes[i] = 1;
				for (int j = i+1; j<numInstances; j++) {
					BlockInstance instance2 = inputInstances.get(j);
					double distance = distanceMeasure.distance(instance1, instance2);
					this.distances[i][j] = distance;
					this.originalDistances[i][j] = distance;
					this.cumulativeDistances[i][j] = distance;
				}
			}
		}
		private static double getValue(double[][] values, int i, int j) {
			if (i==j) {
				return 0.0;
			}
			if (j>i) {
				return values[i][j];
			} else {
				return values[j][i];
			}
		}
		private static void setValue(double[][] values, int i, int j, double newValue) {
			if (i==j) {
				return;
			}
			if (j>i) {
				values[i][j] = newValue;
			} else {
				values[j][i] = newValue;
			}
		}
		public InstancePair getMinimumDistancePair() {
			double minDistance = Double.POSITIVE_INFINITY;
			int minI = 0;
			int minJ = 0;
			for (int i=0; i<numInstances; i++) {
				for (int j=i+1; j<numInstances; j++) {
					if (activeClusters[i] && activeClusters[j]) {
						double distance = distances[i][j];
						if (distance<minDistance) {
							minDistance=distance;
							minI = i;
							minJ = j;
						}
					}
				}
			}
			return new InstancePair(instances.get(minI), instances.get(minJ));
		}
		public void mergeInstances(BlockInstance instance1, BlockInstance instance2, ClusterSimilarity linkage)  {
			int i = instanceToIdx.get(instance1);
			int j = instanceToIdx.get(instance2);
			for (int k = 0; k<numInstances; k++) {
				double newDistance = 0.0;
				switch(linkage) {
				case SingleLink:
					newDistance = Math.min(getValue(distances,k,i), getValue(distances,k,j));
					break;
				case CompleteLink:
					newDistance = Math.max(getValue(distances,k,i), getValue(distances,k,j));
					break;
				case AverageLink:
					if (!activeClusters[k]) continue;
					double sum = getValue(cumulativeDistances,k,i) + getValue(cumulativeDistances,k,j);
					setValue(cumulativeDistances,k,i,sum);
					newDistance = sum/(double)((clusterSizes[i]+clusterSizes[j])*clusterSizes[k]);
					break;
				default:
					throw new UnsupportedOperationException("Unknown ClusterSimilarity "+linkage.toString());
				}
				setValue(distances, k, i, newDistance);
			}
			activeClusters[j] = false;
			clusterSizes[i] += clusterSizes[j];
		}
	}
	
	private final double distanceThreshold;
	private final ClusterSimilarity linkage;
	
	public HierarchicalAgglomerativeClusterer(double distanceThreshold, ClusterSimilarity linkage) {
		this.distanceThreshold = distanceThreshold;
		this.linkage = linkage;
	}
	
	@Override
	public Set<BlockCluster> execute(List<Block> blocks, DistanceMeasure dm) {
		
		int numInstances = blocks.size();
		
		clusters = new HashSet<BlockCluster>(numInstances);
		List<BlockInstance> instances = new ArrayList<BlockInstance>(numInstances);
		Map<BlockInstance, BlockCluster> clusterAssignment = new HashMap<BlockInstance, BlockCluster>(numInstances);
		for (Block block : blocks) {
			BlockInstance instance = new BlockInstance(block);
			BlockCluster cluster = new BlockCluster(instance);
			clusters.add(cluster);
			instances.add(instance);
			clusterAssignment.put(instance, cluster);
		}
		
		if (numInstances>0) {
		
			DistanceMatrix distanceMatrix = new DistanceMatrix(instances, dm);
			
			double distance = 0.0;
			int step = 0;
			while (distance < distanceThreshold && step<numInstances-1) {
				if ((step+1)%10==0) {
				}
	
				InstancePair closestPair = distanceMatrix.getMinimumDistancePair();
				BlockInstance instance1 = closestPair.instance1;
				BlockInstance instance2 = closestPair.instance2;
				distance = dm.distance(instance1, instance2);
	
				distanceMatrix.mergeInstances(instance1, instance2, linkage);
				
				BlockCluster cluster1 = clusterAssignment.get(instance1);
				BlockCluster cluster2 = clusterAssignment.get(instance2);
				clusters.remove(cluster2);
				cluster1.merge(cluster2);
				for (BlockInstance instanceToReassign : cluster2.getBlockInstances()) {
					clusterAssignment.put(instanceToReassign, cluster1);
				}
	
				step++;
				
	//			String clusterId1 = clustering.getClusterIdOfInstance(instance1.getId());
	//			String clusterId2 = clustering.getClusterIdOfInstance(instance2.getId());
	//			clustering.mergeClusters(clusterId1, clusterId2);
			}
		}
		
		return clusters;
	}

}
