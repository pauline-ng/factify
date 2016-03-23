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
package at.knowcenter.code.pdf.utils.table;

import java.util.ArrayList;
import java.util.List;

import at.knowcenter.clustering.Cluster;
import at.knowcenter.clustering.Clustering;
import at.knowcenter.clustering.hierarchical.agglomerative.ClusterSimilarity;
import at.knowcenter.clustering.hierarchical.agglomerative.HacMetadata;
import at.knowcenter.clustering.hierarchical.agglomerative.InMemoryHac;
import at.knowcenter.code.api.pdf.Block;
import at.knowcenter.code.api.pdf.BoundingBox;
import at.knowcenter.code.api.pdf.TableRegion;
import at.knowcenter.featureengineering.DefaultInstanceSet;
import at.knowcenter.featureengineering.Instance;
import at.knowcenter.featureengineering.InstanceSet;
import at.knowcenter.featureengineering.functions.DistanceFunction;
import at.knowcenter.storage.utils.InMemoryStorage;
import at.knowcenter.vectorization.Vector;
import at.knowcenter.vectorization.vectors.DenseVector;

/**
 * @author sklampfl
 *
 */
public class DualClusteringTableParser implements TableParser {

	
	private static class Span {
		public final Block word1;
		public final Block word2;
		public final float min;
		public final float max;
		
		public Span(float min, float max, Block word1, Block word2) {
			this.min = min;
			this.max = max;
			this.word1 = word1;
			this.word2 = word2;
		}
		
//		public float getSize() { 
//			return max - min; 
//		}
		
		@Override
		public String toString() {
			return String.format("%d-%d (%s - %s)", (int)min, (int)max, word1.getText(), word2.getText());
		}
	}
	
	private static class GapDistanceFunction implements DistanceFunction<Vector> {

//		private final double maxValue;
//		
//		public GapDistanceFunction(double maxValue) {
//			this.maxValue = maxValue;
//		}
		
		@Override
		public double calculate(Vector a, Vector b) {
			double min1 = a.get(0);
			double max1 = a.get(1);
			double min2 = b.get(0);
			double max2 = b.get(1);
			
			// no overlap
			if (min1 > max2 || min2 > max1) {
				//return Double.POSITIVE_INFINITY;
			}
			
			// gaps are overlapping
			double overlap = Math.min(max2-min1, max1-min2);
			double totalSpan = Math.max(max1, max2) - Math.min(min1, min2);
			
			return 1d - overlap/totalSpan;
		}
		
	};
	
	private static boolean intersectsY(BoundingBox bbox1, BoundingBox bbox2) {
		return !bbox1.precedesY(bbox2, 0) && !bbox1.meetsY(bbox2, 0) && !bbox2.precedesY(bbox1, 0) && !bbox2.meetsY(bbox2, 0);
	}
	
	private InstanceSet<Vector> generateInstanceSet(List<Span> gapSpans) throws Exception {
		DefaultInstanceSet<Vector> vectorSpace = new DefaultInstanceSet<Vector>(Vector.class, new InMemoryStorage<String, Vector>());		
		
		int i = 0;
		for(Span span : gapSpans) {
			String id = i + ": " + span.toString();
			Vector v = new DenseVector(new double[] { span.min, span.max });
			vectorSpace.addInstance(new Instance<Vector>(id, v));
			i++;
		}
		
		return vectorSpace;
	}


	private List<Span> generateHorizontalGapSpans(List<Block> words) {
		List<Span> result = new ArrayList<Span>();
		//PdfExtractionUtils.sortBlocksByY(words);
		BoundingBox currentBBox = null;
		BoundingBox leftBBox = null;
		BoundingBox rightBBox = null;
		BoundingBox tempBBox = null;
		float leftDistance = Float.MAX_VALUE;
		float rightDistance = Float.MAX_VALUE;
		float leftDiff = 0;
		float rightDiff = 0;
		int leftIndex = -1;
		int rightIndex = -1;
		for (int currentIndex = 0; currentIndex < words.size(); currentIndex++) {
			currentBBox = words.get(currentIndex).getBoundingBox();
			leftDistance = Float.MAX_VALUE;
			rightDistance = Float.MAX_VALUE;
			leftIndex = -1;
			rightIndex = -1;
			for (int index = 0; index < words.size(); index++) {
				if (index == currentIndex) {
					continue;
				}
				tempBBox = words.get(index).getBoundingBox();
				if (intersectsY(tempBBox, currentBBox)) {
					leftDiff = currentBBox.minx - tempBBox.maxx;
					rightDiff = tempBBox.minx - currentBBox.maxx;
					if (leftDiff > 0 && leftDiff < leftDistance) {
						leftDistance = leftDiff;
						leftBBox = tempBBox;
						leftIndex = index;
					}
					if (rightDiff > 0 && rightDiff < rightDistance) {
						rightDistance = rightDiff;
						rightBBox = tempBBox;
						rightIndex = index;
					}
				}
			}
			if (leftIndex >= 0) {
				result.add(new Span(leftBBox.maxx, currentBBox.minx, words.get(leftIndex), words.get(currentIndex)));
			}
			if (rightIndex >= 0) {
				result.add(new Span(currentBBox.maxx, rightBBox.minx, words.get(currentIndex), words.get(rightIndex)));
			}
		}
		return result;
	}
	
	@Override
	public TableCell[][] parseTable(TableRegion tableRegion) throws TableException {
		
		List<Span> horizontalGapSpans = generateHorizontalGapSpans(tableRegion.words);
		try {
			InstanceSet<Vector> horizontalInstanceSet = generateInstanceSet(horizontalGapSpans);
			
			InMemoryHac<Vector> hac = new InMemoryHac<Vector>(2, 1, new GapDistanceFunction(), ClusterSimilarity.AVERAGE);
			Clustering<HacMetadata> clustering = hac.cluster(horizontalInstanceSet);
			List<Cluster<HacMetadata>> clusters = clustering.getClusters();
			
			for (Cluster<HacMetadata> cluster : clusters) {
				System.out.println(cluster.getAllInstanceIds());
			}
			
			System.out.println();
		} catch (Exception e) {
			throw new TableException("Could not parse table " + e);
		}
		
		return null;
	}

}
