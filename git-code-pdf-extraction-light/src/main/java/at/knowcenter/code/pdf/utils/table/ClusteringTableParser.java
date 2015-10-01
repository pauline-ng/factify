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

import gnu.trove.TIntDoubleHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import at.knowcenter.clustering.Cluster;
import at.knowcenter.clustering.Clustering;
import at.knowcenter.clustering.ClusteringException;
import at.knowcenter.clustering.hierarchical.agglomerative.ClusterSimilarity;
import at.knowcenter.clustering.hierarchical.agglomerative.HacMetadata;
import at.knowcenter.clustering.hierarchical.agglomerative.InMemoryHac;
import at.knowcenter.code.api.pdf.Block;
import at.knowcenter.code.api.pdf.BoundingBox;
import at.knowcenter.code.api.pdf.Page.Line;
import at.knowcenter.code.api.pdf.TableRegion;
import at.knowcenter.code.pdf.utils.PdfExtractionUtils;
import at.knowcenter.featureengineering.DefaultInstanceSet;
import at.knowcenter.featureengineering.Instance;
import at.knowcenter.featureengineering.InstanceSet;
import at.knowcenter.featureengineering.functions.DistanceFunction;
import at.knowcenter.storage.utils.InMemoryStorage;
import at.knowcenter.vectorization.Vector;
import at.knowcenter.vectorization.relatedness.DistanceFunctions;
import at.knowcenter.vectorization.vectors.DenseVector;

/**
 * 
 * @author sklampfl
 *
 */
public class ClusteringTableParser implements TableParser {
	
	private final Map<String,Block> idToBlock;

	private final float tol = 5f;
	private final float maxHGap;
	private final float minHGapFraction;
	private int numHGaps = 0;
	private float sumHGap = 0f;
	private final float maxVGap;
	private final float minVGapFraction;
	private int numVGaps = 0;
	private float sumVGap = 0f;
	private final boolean useLines;
	
	public ClusteringTableParser(float maxHGap, float minHGapFraction, float maxVGap,
			float minVGapFraction, boolean useLines) {
		this.maxHGap = maxHGap;
		this.maxVGap = maxVGap;
		this.minHGapFraction = minHGapFraction;
		this.minVGapFraction = minVGapFraction;
		this.useLines = useLines;
		this.idToBlock = new HashMap<String,Block>();
	}
	
	public ClusteringTableParser() {
		this(10f, 0.5f, 1f, 0.5f, false);
	}
	
	private void reset() {
		numHGaps = 0;
		sumHGap = 0f;
		numVGaps = 0;
		sumVGap = 0f;
	}

	private InstanceSet<Vector> generateHorizontalWordSpans(TableRegion tableRegion) throws Exception {
		DefaultInstanceSet<Vector> vectorSpace = new DefaultInstanceSet<Vector>(Vector.class, new InMemoryStorage<String, Vector>());		
		
		int i = 0;
		for(Block word : tableRegion.words) {
			BoundingBox bbox = word.getBoundingBox();
			String id = word.getText()+"-" + i;
			Vector v = new DenseVector(new double[] { bbox.minx, bbox.maxx });
			idToBlock.put(id, word);
			vectorSpace.addInstance(new Instance<Vector>(id, v));
			i++;
		}
		
		return vectorSpace;
	}
	
	private InstanceSet<Vector> generateVerticalWordSpans(TableRegion tableRegion) throws Exception {
		DefaultInstanceSet<Vector> vectorSpace = new DefaultInstanceSet<Vector>(Vector.class, new InMemoryStorage<String, Vector>());		
		
		int i = 0;
		for(Block word : tableRegion.words) {
			BoundingBox bbox = word.getBoundingBox();
			String id = word.getText()+"-" + i;
			Vector v = new DenseVector(new double[] { bbox.miny, bbox.maxy });
			idToBlock.put(id, word);
			vectorSpace.addInstance(new Instance<Vector>(id, v));
			i++;
		}
		
		return vectorSpace;
	}
	
	private boolean isToBeSplitHorizontally(Cluster<HacMetadata> cluster) {
		Cluster<HacMetadata> c1 = cluster.getChildren().get(0);
		Cluster<HacMetadata> c2 = cluster.getChildren().get(1);
		List<Block> words1 = getVOrderedWordList(c1);
		List<Block> words2 = getVOrderedWordList(c2);
		if (words2.size() < words1.size()) {
			List<Block> tmp = words1;
			words1 = words2;
			words2 = tmp;
		}
		
		TIntDoubleHashMap gaps = new TIntDoubleHashMap(words1.size()); 
		for(Block word1 : words1) {
			BoundingBox bbox1 = word1.getBoundingBox();
			int key = (int) bbox1.miny;
			gaps.putIfAbsent(key, Double.POSITIVE_INFINITY);
			for(Block word2 : words2) {
				BoundingBox bbox2 = word2.getBoundingBox();
				double gap = BoundingBox.horizontalDistance(bbox1, bbox2);
				if (bbox1.equalsY(bbox2, tol) && gap<gaps.get(key)) {
					gaps.put(key, gap);
				}
			}
		}
		double medianGap = PdfExtractionUtils.calculateMedian(gaps.getValues());
		boolean split = false;
		double gapFraction = 0;
		if (medianGap<Double.POSITIVE_INFINITY) {
			numHGaps++;
			sumHGap += medianGap;
			gapFraction = medianGap/(sumHGap/numHGaps);
			split = medianGap >= maxHGap || (gapFraction > minHGapFraction && gapFraction < 1.0);
		}
		return split;
	}
	
	private boolean isToBeSplitVertically(Cluster<HacMetadata> cluster) {
		Cluster<HacMetadata> c1 = cluster.getChildren().get(0);
		Cluster<HacMetadata> c2 = cluster.getChildren().get(1);
		List<Block> words1 = getHOrderedWordList(c1);
		List<Block> words2 = getHOrderedWordList(c2);
		if (words2.size() < words1.size()) {
			List<Block> tmp = words1;
			words1 = words2;
			words2 = tmp;
		}
		
		List<Double> gaps = new ArrayList<Double>(words1.size()); 
		for(Block word1 : words1) {
			BoundingBox bbox1 = word1.getBoundingBox();
			for(Block word2 : words2) {
				BoundingBox bbox2 = word2.getBoundingBox();
				double gap = BoundingBox.verticalDistance(bbox1, bbox2);
				gaps.add(gap);
			}
		}
		double medianGap = PdfExtractionUtils.calculateMedian(gaps.toArray(new Double[0]));
		boolean split = false;
		double gapFraction = 0;
		if (medianGap<Double.POSITIVE_INFINITY) {
			numVGaps++;
			sumVGap += medianGap;
			gapFraction = medianGap/(sumVGap/numVGaps);
			split = medianGap >= maxVGap || (gapFraction > minVGapFraction && gapFraction < 1.0);
		}
		return split;
	}
	
	private List<Block> getVOrderedWordList(Cluster<HacMetadata> cluster) {
		List<Block> words = new ArrayList<Block>();
		for (String id : cluster.getAllInstanceIds()) {
			words.add(idToBlock.get(id));
		}
		PdfExtractionUtils.sortBlocksByY(words);
		return words;
	}	
	
	private List<Block> getHOrderedWordList(Cluster<HacMetadata> cluster) {
		List<Block> words = new ArrayList<Block>();
		for (String id : cluster.getAllInstanceIds()) {
			words.add(idToBlock.get(id));
		}
		PdfExtractionUtils.sortBlocksByX(words);
		return words;
	}
	
	private DistanceFunction<Vector> generateHorizontalDistanceFunction(TableRegion tableRegion) {
		final double[] xPos = new double[tableRegion.lines.size()];
		int i = 0;
		for (Line line : tableRegion.lines) {
			if (line.isVertical()) {
				xPos[i] = line.getStart().getX();
				i++;
			}
		}
		final int numLines = i;
		return new DistanceFunction<Vector>() {
			@Override
			public double calculate(Vector a, Vector b) {
				double x1 = a.get(0);
				double x2 = a.get(1);
				double x3 = b.get(0);
				double x4 = b.get(1);
				if (Math.max(x1-x4, x3-x2)>0) {
					double xx1, xx2;
					if (x1<x4) { xx1 = x2; xx2 = x3; }
					else { xx1 = x4; xx2 = x1; }
					for (int i=0; i<numLines; i++) {
						if(xx1<xPos[i] && xPos[i]<xx2) {
							return Double.POSITIVE_INFINITY;
						}
					}
				}
				return DistanceFunctions.EUCLIDEAN_DISTANCE.calculate(a, b);
			}
		};
	}

	private DistanceFunction<Vector> generateVerticalDistanceFunction(TableRegion tableRegion) {
		final double[] yPos = new double[tableRegion.lines.size()];
		int i = 0;
		for (Line line : tableRegion.lines) {
			if (line.isHorizontal()) {
				yPos[i] = line.getStart().getY();
				i++;
			}
		}
		final int numLines = i;
		return new DistanceFunction<Vector>() {
			@Override
			public double calculate(Vector a, Vector b) {
				double y1 = a.get(0);
				double y2 = a.get(1);
				double y3 = b.get(0);
				double y4 = b.get(1);
				if (Math.max(y1-y4, y3-y2)>0) {
					double yy1, yy2;
					if (y1<y4) { yy1 = y2; yy2 = y3; }
					else { yy1 = y4; yy2 = y1; }
					for (int i=0; i<numLines; i++) {
						if(yy1<yPos[i] && yPos[i]<yy2) {
							return Double.POSITIVE_INFINITY;
						}
					}
				}
				return DistanceFunctions.EUCLIDEAN_DISTANCE.calculate(a, b);
			}
		};
	}

	@Override
	public TableCell[][] parseTable(TableRegion tableRegion) throws TableException {
		
		reset();
		List<List<Block>> columns = null;
		List<List<Block>> rows = null;
		try {
			columns = findColumns(tableRegion);
			rows = findRows(tableRegion);
		} catch (Exception e) {
			throw new TableException("Could not parse table:\n"+tableRegion.captionBlock, e);
		}
		if (rows==null || columns==null) {
			return null;
		}
		
		TableCell[][] cells = new TableCell[rows.size()][columns.size()];
		int rowId = 0;
		for (List<Block> row : rows) {
			int colId = 0;
			for (List<Block> column : columns) {
				List<Block> words = new ArrayList<Block>(column);
				words.retainAll(row);
				cells[rowId][colId] = new TableCell(words);
				colId++;
			}
			rowId++;
		}
		return cells;	
	}
	
	private List<List<Block>> findColumns(TableRegion tableRegion) throws Exception, ClusteringException {
		InstanceSet<Vector> instanceSet = generateHorizontalWordSpans(tableRegion);
		DistanceFunction<Vector> distanceFunction;
		if (useLines) {
			distanceFunction = generateHorizontalDistanceFunction(tableRegion);
		} else {
			distanceFunction = DistanceFunctions.EUCLIDEAN_DISTANCE;
		}
		InMemoryHac<Vector> hac = new InMemoryHac<Vector>(2, 100, distanceFunction, ClusterSimilarity.AVERAGE);
		Clustering<HacMetadata> clustering = hac.cluster(instanceSet);
		
		List<Cluster<HacMetadata>> clusters = clustering.getClusters();
		Queue<Cluster<HacMetadata>> open = new LinkedList<Cluster<HacMetadata>>();
		List<Cluster<HacMetadata>> closed = new ArrayList<Cluster<HacMetadata>>();
		open.addAll(clusters);
		while(!open.isEmpty()) {
			Cluster<HacMetadata> cluster = open.poll();
			if (cluster.isLeaf()) {
				closed.add(cluster);
				continue;
			}
			if (isToBeSplitHorizontally(cluster)) {
				open.addAll(cluster.getChildren());
			} else {
				closed.add(cluster);
			}
		}
		
		Collections.sort(closed, new Comparator<Cluster<HacMetadata>>() {
			@Override
			public int compare(Cluster<HacMetadata> o1,	Cluster<HacMetadata> o2) {
				double d1 = getVOrderedWordList(o1).get(0).getBoundingBox().minx;
				double d2 = getVOrderedWordList(o2).get(0).getBoundingBox().minx;
				return Double.compare(d1, d2);
			}
		});
		List<List<Block>> result = new ArrayList<List<Block>>(closed.size());
		for (Cluster<HacMetadata> c : closed) {
			List<Block> vOrderedWordList = getVOrderedWordList(c);
			result.add(vOrderedWordList);
		}
		return result;
	}

	private List<List<Block>> findRows(TableRegion tableRegion) throws Exception, ClusteringException {
		InstanceSet<Vector> instanceSet = generateVerticalWordSpans(tableRegion);
		DistanceFunction<Vector> distanceFunction;
		if (useLines) {
			distanceFunction = generateVerticalDistanceFunction(tableRegion);
		} else {
			distanceFunction = DistanceFunctions.EUCLIDEAN_DISTANCE;
		}
		InMemoryHac<Vector> hac = new InMemoryHac<Vector>(2, 1E10, distanceFunction, ClusterSimilarity.AVERAGE);
		Clustering<HacMetadata> clustering = hac.cluster(instanceSet);
		
		List<Cluster<HacMetadata>> clusters = clustering.getClusters();
		Queue<Cluster<HacMetadata>> open = new LinkedList<Cluster<HacMetadata>>();
		List<Cluster<HacMetadata>> closed = new ArrayList<Cluster<HacMetadata>>();
		open.addAll(clusters);
		while(!open.isEmpty()) {
			Cluster<HacMetadata> cluster = open.poll();
			if (cluster.isLeaf()) {
				closed.add(cluster);
				continue;
			}
			if (isToBeSplitVertically(cluster)) {
				open.addAll(cluster.getChildren());
			} else {
				closed.add(cluster);
			}
		}
		
		Collections.sort(closed, new Comparator<Cluster<HacMetadata>>() {
			@Override
			public int compare(Cluster<HacMetadata> o1,	Cluster<HacMetadata> o2) {
				double d1 = getHOrderedWordList(o1).get(0).getBoundingBox().miny;
				double d2 = getHOrderedWordList(o2).get(0).getBoundingBox().miny;
				return Double.compare(d1, d2);
			}
		});
		List<List<Block>> result = new ArrayList<List<Block>>(closed.size());
		for (Cluster<HacMetadata> c : closed) {
			List<Block> hOrderedWordList = getHOrderedWordList(c);
			result.add(hOrderedWordList);
		}
		return result;
	}

}
