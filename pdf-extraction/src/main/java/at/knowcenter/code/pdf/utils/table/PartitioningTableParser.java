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

import gnu.trove.map.hash.TIntIntHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import at.knowcenter.clustering.Cluster;
import at.knowcenter.clustering.Clustering;
import at.knowcenter.clustering.ClusteringException;
import at.knowcenter.clustering.kmeans.batch.KMeans;
import at.knowcenter.clustering.kmeans.batch.KMeansMetadata;
import at.knowcenter.clustering.kmeans.seedselection.VectorSeedSelector;
import at.knowcenter.code.api.pdf.Block;
import at.knowcenter.code.api.pdf.BoundingBox;
import at.knowcenter.code.api.pdf.TableRegion;
import at.knowcenter.code.api.pdf.TextFragment;
import at.knowcenter.code.pdf.utils.PdfExtractionUtils;
import at.knowcenter.code.pdf.utils.table.Histogram.Extremum;
import at.knowcenter.code.pdf.utils.table.Histogram.Maximum;
import at.knowcenter.code.pdf.utils.table.Histogram.Minimum;
import at.knowcenter.code.pdf.utils.table.TableUtils.Column;
import at.knowcenter.code.pdf.utils.table.TableUtils.Row;
import at.knowcenter.featureengineering.DefaultInstanceSet;
import at.knowcenter.featureengineering.Instance;
import at.knowcenter.featureengineering.InstanceSetException;
import at.knowcenter.storage.utils.InMemoryStorage;
import at.knowcenter.vectorization.Vector;
import at.knowcenter.vectorization.relatedness.DistanceFunctions;
import at.knowcenter.vectorization.vectors.DenseVector;

/**
 * @author sklampfl
 *
 */
public class PartitioningTableParser implements TableParser {
	
	private static final int MEDIAN_FILTER_SIZE = 5;
	private static final int MIN_MINIMUM_WIDTH = 5;
	private static final int MIN_NUM_ROWS_FOR_MERGING = 5;
	private static final int MIN_NUM_COLS_FOR_MERGING = 3;
	private static final float DEPTH_FRACTION_THRESHOLD = 0.2f;

	public PartitioningTableParser() {
	}
	
	private Histogram calculateVerticalProjection(List<Block> words) {
		TIntIntHashMap verticalProjection = new TIntIntHashMap();
		int minPos = Integer.MAX_VALUE;
		int maxPos = Integer.MIN_VALUE;
		for (Block word : words) {
			BoundingBox bbox = word.getBoundingBox();
			int minx = (int)bbox.minx;
			if (minx < minPos) minPos = minx;
			int maxx = (int)bbox.maxx;
			if (maxx > maxPos) maxPos = maxx;
			int height = (int)bbox.getHeight();
			for (int xpos = minx; xpos <= maxx; xpos++) {
				verticalProjection.adjustOrPutValue(xpos, height, height);
			}
		}
		int[] histogram = new int[maxPos-minPos+1];
		for (int i=0; i<histogram.length; i++) {
			histogram[i] = verticalProjection.get(minPos+i);
		}
		return new Histogram(histogram, minPos);
	}

	private Histogram calculateHorizontalProjection(List<Block> words) {
		TIntIntHashMap horizontalProjection = new TIntIntHashMap();
		int minPos = Integer.MAX_VALUE;
		int maxPos = Integer.MIN_VALUE;
		for (Block word : words) {
			BoundingBox bbox = word.getBoundingBox();
			int miny = (int)bbox.miny;
			if (miny < minPos) minPos = miny;
			int maxy = (int)bbox.maxy;
			if (maxy > maxPos) maxPos = maxy;
			int width = (int)bbox.getWidth();
			for (int ypos = miny; ypos <= maxy; ypos++) {
				horizontalProjection.adjustOrPutValue(ypos, width, width);
			}
		}
		int[] histogram = new int[maxPos-minPos+1];
		for (int i=0; i<histogram.length; i++) {
			histogram[i] = horizontalProjection.get(minPos+i);
		}
		return new Histogram(histogram, minPos);
	}
	
	private List<Extremum> preprocessExtrema(List<Extremum> extrema, Histogram projection) {
		List<Extremum> result = new ArrayList<Extremum>(extrema);
		Set<Extremum> extremaToRemove = new HashSet<Extremum>();
		int maxValue = projection.getMaxValue();
		for (int i = 1; i < extrema.size()-1; i++) {
			Extremum prevExt = extrema.get(i-1);
			Extremum ext = extrema.get(i);
			Extremum nextExt = extrema.get(i+1);
			int diff1 = Math.abs(prevExt.value - ext.value);
			int diff2 = Math.abs(nextExt.value - ext.value);
			if (diff1 < maxValue*DEPTH_FRACTION_THRESHOLD && diff2 < maxValue*DEPTH_FRACTION_THRESHOLD) {
				extremaToRemove.add(ext);
			}
		}
		result.removeAll(extremaToRemove);

		Set<Extremum> extremaToAdd = new HashSet<Extremum>();
		extremaToRemove.clear();
		Extremum lastExtremum = null;
		for (Extremum extremum : result) {
			if (lastExtremum == null && extremum instanceof Minimum) {
				int minPos = projection.getMinPos();
				extremaToAdd.add(projection.findMaximum(minPos, extremum.pos));
			} else if (lastExtremum instanceof Minimum && extremum instanceof Minimum) {
				Maximum maximum = projection.findMaximum(lastExtremum.pos, extremum.pos);
				Extremum largerExtremum = lastExtremum.value > extremum.value ? lastExtremum : extremum;
				int diff = maximum.value - largerExtremum.value;
				if (diff > maxValue*DEPTH_FRACTION_THRESHOLD) {
					extremaToAdd.add(maximum);
				} else {
					extremaToRemove.add(largerExtremum);
				}
			} else if (lastExtremum instanceof Maximum && extremum instanceof Maximum)  {
				Minimum minimum = projection.findMinimum(lastExtremum.pos, extremum.pos);
				Extremum smallerExtremum = lastExtremum.value < extremum.value ? lastExtremum : extremum;
				int diff = smallerExtremum.value - minimum.value;
				if (diff > maxValue*DEPTH_FRACTION_THRESHOLD) {
					extremaToAdd.add(minimum);
				} else {
					extremaToRemove.add(smallerExtremum);
				}
			}
			lastExtremum = extremum;
		}
		if (lastExtremum instanceof Minimum) {
			extremaToAdd.add(projection.findMaximum(lastExtremum.pos, projection.getMaxPos()));
		}
		
		result.addAll(extremaToAdd);
		result.removeAll(extremaToRemove);
		Collections.sort(result);
		
		return result;
	}
	
	private DefaultInstanceSet<Vector> createMinimumVectorSpace(List<Extremum> extrema, 
			Map<String, Extremum> idToExtremum) throws InstanceSetException {
		DefaultInstanceSet<Vector> minVectorSpace = new DefaultInstanceSet<Vector>(Vector.class, new InMemoryStorage<String, Vector>());
		for(Extremum extremum : extrema) {
			String id = "-" + extremum.pos;
			if (extremum instanceof Minimum) {
				id = "min" + id;
				Vector v = new DenseVector(new double[] { extremum.value });
				minVectorSpace.addInstance(new Instance<Vector>(id, v));
				idToExtremum.put(id, extremum);
			}
		}
		return minVectorSpace;
	}
	
	private DefaultInstanceSet<Vector> createMaximumVectorSpace(List<Extremum> extrema, 
			Map<String, Extremum> idToExtremum) throws InstanceSetException {
		DefaultInstanceSet<Vector> maxVectorSpace = new DefaultInstanceSet<Vector>(Vector.class, new InMemoryStorage<String, Vector>());
		for(Extremum extremum : extrema) {
			String id = "-" + extremum.pos;
			if (extremum instanceof Maximum) {
				id = "max" + id;
				Vector v = new DenseVector(new double[] { extremum.value });
				maxVectorSpace.addInstance(new Instance<Vector>(id, v));
				idToExtremum.put(id, extremum);
			}
		}
		return maxVectorSpace;
	}
	
	private int[] calculateExtremumStatistics(List<Extremum> extrema) {
		int largestMinimum = 0;
		int smallestMinimum = Integer.MAX_VALUE;
		int largestMaximum = 0;
		int smallestMaximum = Integer.MAX_VALUE;
		for(Extremum extremum : extrema) {
			if (extremum instanceof Minimum) {
				if (extremum.value > largestMinimum) {
					largestMinimum = extremum.value;
				}
				if (extremum.value < smallestMinimum) {
					smallestMinimum = extremum.value;
				}
			} else if (extremum instanceof Maximum) {
				if (extremum.value < smallestMaximum) {
					smallestMaximum = extremum.value;
				}
				if (extremum.value > largestMaximum) {
					largestMaximum = extremum.value;
				}
			}
		}
		return new int[] {largestMinimum, smallestMinimum, largestMaximum, smallestMaximum};
	}
	
	private List<Minimum> filterMinimaByClustering(DefaultInstanceSet<Vector> minVectorSpace, 
			Map<String, Extremum> idToExtremum, int maxValue, int largestMinimum, int smallestMinimum)
					throws ClusteringException, InstanceSetException {
		List<Vector> minSeeds = new ArrayList<Vector>(2);
		minSeeds.add(new DenseVector(new double[] { 0  }));
		minSeeds.add(new DenseVector(new double[] { maxValue}));

		KMeans kmeans = new KMeans(2, 1, DistanceFunctions.EUCLIDEAN_DISTANCE,
				new VectorSeedSelector(minSeeds));
		Clustering<KMeansMetadata> clustering = kmeans.cluster(minVectorSpace);
		List<Cluster<KMeansMetadata>> clusters = clustering.getClusters();
		Cluster<KMeansMetadata> lowerMinCluster = clusters.get(0);
		
		Set<String> instanceIds = (largestMinimum-smallestMinimum < maxValue*DEPTH_FRACTION_THRESHOLD) ? 
				minVectorSpace.getInstanceIds() : lowerMinCluster.getInstanceIds();
		//System.out.println(instanceIds);
		List<Minimum> filteredMinima = new ArrayList<Minimum>();
		for (String id : instanceIds) {
			Extremum extremum = idToExtremum.get(id);
			if (extremum instanceof Minimum) {
				filteredMinima.add((Minimum)extremum);
			}
		}
		Collections.sort(filteredMinima);
		return filteredMinima;
	}
	
	private List<Maximum> filterMaximaByClustering(DefaultInstanceSet<Vector> maxVectorSpace, 
			Map<String, Extremum> idToExtremum, int maxValue, int largestMaximum, int smallestMaximum)
					throws ClusteringException, InstanceSetException {
		List<Vector> maxSeeds = new ArrayList<Vector>(2);
		maxSeeds.add(new DenseVector(new double[] { maxValue}));
		maxSeeds.add(new DenseVector(new double[] { 0 }));

		KMeans kmeans = new KMeans(2, 1, DistanceFunctions.EUCLIDEAN_DISTANCE,
				new VectorSeedSelector(maxSeeds));
		Clustering<KMeansMetadata> clustering = kmeans.cluster(maxVectorSpace);
		List<Cluster<KMeansMetadata>> clusters = clustering.getClusters();
		Cluster<KMeansMetadata> upperMaxCluster = clusters.get(0);
		
		Set<String> instanceIds = (largestMaximum - smallestMaximum < maxValue*DEPTH_FRACTION_THRESHOLD) ? 
				maxVectorSpace.getInstanceIds() : upperMaxCluster.getInstanceIds();
		//System.out.println(instanceIds);
		List<Maximum> filteredMaxima = new ArrayList<Maximum>();
		for (String id : instanceIds) {
			Extremum extremum = idToExtremum.get(id);
			if (extremum instanceof Maximum) {
				filteredMaxima.add((Maximum)extremum);
			}
		}
		Collections.sort(filteredMaxima);
		return filteredMaxima;
	}
	
	private Set<Minimum> getSelectedMinima(List<Minimum> filteredMinima, List<Maximum> filteredMaxima,
			Histogram projection) {
		Set<Minimum> selectedMinima = new HashSet<Minimum>();
		if (filteredMinima.size()==0 || filteredMaxima.size()==0) {
			return selectedMinima;
		}
		if (filteredMaxima.size()==1 && filteredMinima.size()==1) {
			selectedMinima.add(filteredMinima.get(0));
		}
		int j = 0;
		for (int i=0; i<filteredMaxima.size()-1; i++) {
			Maximum currentMaximum = filteredMaxima.get(i);
			Maximum nextMaximum = filteredMaxima.get(i+1);
			Minimum minimum = null;
			while (j<filteredMinima.size() && filteredMinima.get(j).pos < currentMaximum.pos) {
				j++;
			}
			while (j<filteredMinima.size() && filteredMinima.get(j).pos < nextMaximum.pos) {
				if (minimum == null) {
					minimum = filteredMinima.get(j);
				} else if (filteredMinima.get(j).value < minimum.value) {
					minimum = filteredMinima.get(j);
				}
				j++;
			}
			if (minimum != null) {
				//System.out.println(minimum);
				selectedMinima.add(minimum);
			}
		}
		return selectedMinima;
	}
	
	private List<Minimum> findSelectedMinima(Histogram projection) {
		List<Minimum> result = new ArrayList<Minimum>();
		try {
			List<Extremum> extrema = projection.findExtrema();
			
			extrema = preprocessExtrema(extrema, projection);
			
			Map<String, Extremum> idToExtremum = new HashMap<String, Extremum>();
			DefaultInstanceSet<Vector> minVectorSpace = createMinimumVectorSpace(extrema, idToExtremum);
			DefaultInstanceSet<Vector> maxVectorSpace = createMaximumVectorSpace(extrema, idToExtremum);
			int[] statistics = calculateExtremumStatistics(extrema);
			int largestMinimum = statistics[0];
			int smallestMinimum = statistics[1];
			int largestMaximum = statistics[2];
			int smallestMaximum = statistics[3];
			int maxValue = largestMaximum;
			
			List<Minimum> filteredMinima = filterMinimaByClustering(minVectorSpace, idToExtremum, maxValue,
					largestMinimum, smallestMinimum);
			List<Maximum> filteredMaxima = filterMaximaByClustering(maxVectorSpace, idToExtremum, maxValue,
					largestMaximum, smallestMaximum);
			
			Set<Minimum> selectedMinima = getSelectedMinima(filteredMinima, filteredMaxima, projection);
			
			for (Extremum extremum : extrema) {
				if (extremum instanceof Minimum && extremum.value==0 && extremum.width > MIN_MINIMUM_WIDTH) {
					selectedMinima.add((Minimum)extremum);
					//System.out.println(extremum);
				}
			}
			
			result.addAll(selectedMinima);
			Collections.sort(result);
			//System.out.println(result);
			
		} catch (InstanceSetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClusteringException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;

	}
	
	private List<Column> findColumns(TableRegion tableRegion) {
		List<Block> wordsByX = new ArrayList<Block>(tableRegion.words);
		PdfExtractionUtils.sortBlocksByX(wordsByX);
		
		Histogram verticalProjection = calculateVerticalProjection(tableRegion.words);
		verticalProjection.medianFilter(MEDIAN_FILTER_SIZE);
//		System.out.println(verticalProjection);
		
		List<Minimum> verticalMinima = findSelectedMinima(verticalProjection);
//		System.out.println(verticalMinima);
		List<Column> columns = new ArrayList<Column>();
		int currentIndex = 0;
		int currentMinimum = verticalProjection.getMinPos();
		int nextMinimum = verticalMinima.size() > 0 ? verticalMinima.get(currentIndex).pos : Integer.MAX_VALUE;
		List<Block> currentColumn = new ArrayList<Block>();
		for (Block word : wordsByX) {
			BoundingBox bbox = word.getBoundingBox();
			if (bbox.minx > nextMinimum) {
				columns.add(new Column(new ArrayList<Block>(currentColumn),currentMinimum,nextMinimum));
				currentColumn.clear();
				currentIndex++;
				currentMinimum = nextMinimum;
				if (verticalMinima.size() > currentIndex) {
					nextMinimum = verticalMinima.get(currentIndex).pos;
				} else {
					nextMinimum = verticalProjection.getMaxPos();
				}
			}
			currentColumn.add(word);
		}
		columns.add(new Column(new ArrayList<Block>(currentColumn),currentMinimum,nextMinimum));
		return columns;
	}
	
	private List<Row> findRows(TableRegion tableRegion) {
		List<Block> wordsByY = new ArrayList<Block>(tableRegion.words);
		PdfExtractionUtils.sortBlocksByY(wordsByY);
		
		Histogram horizontalProjection = calculateHorizontalProjection(tableRegion.words);
//		List<Minimum> horizontalMinima = horizontalProjection.findMinima(maxHorizontalValue, minHorizontalDepth, minHorizontalWidth);
		List<Minimum> horizontalMinima = horizontalProjection.findMinima();
//		System.out.println(horizontalProjection);
//		System.out.println(horizontalMinima);
//		List<Minimum> horizontalMinima = findSelectedMinima(horizontalProjection);
		List<Row> rows = new ArrayList<Row>();
		int currentIndex = 0;
		int currentMinimum = horizontalProjection.getMinPos();
		int nextMinimum = horizontalMinima.size() > 0 ? horizontalMinima.get(currentIndex).pos : Integer.MAX_VALUE;
		List<Block> currentRow = new ArrayList<Block>();
		for (Block word : wordsByY) {
			if (word.getBoundingBox().miny > nextMinimum) {
				rows.add(new Row(new ArrayList<Block>(currentRow),currentMinimum,nextMinimum));
				currentRow.clear();
				currentIndex++;
				currentMinimum = nextMinimum;
				if (horizontalMinima.size() > currentIndex) {
					nextMinimum = horizontalMinima.get(currentIndex).pos;
				} else {
					nextMinimum = horizontalProjection.getMaxPos();
				}
			}
			currentRow.add(word);
		}
		rows.add(new Row(new ArrayList<Block>(currentRow),currentMinimum,nextMinimum));
		return rows;
	}
	
	private boolean haveSameMajorityFont(TableCell cell1, TableCell cell2) {
		
		List<TextFragment> fragments = new ArrayList<TextFragment>();
		for (Block word : cell1.getWords()) {
			fragments.addAll(word.getFragments());
		}
		int majorityFontId1 = PdfExtractionUtils.getMajorityFontId(fragments);
		int majorityFontSize1 = PdfExtractionUtils.getMajorityFontSize(fragments);
		fragments.clear();
		for (Block word : cell2.getWords()) {
			fragments.addAll(word.getFragments());
		}
		int majorityFontId2 = PdfExtractionUtils.getMajorityFontId(fragments);
		int majorityFontSize2 = PdfExtractionUtils.getMajorityFontSize(fragments);
		
		return majorityFontId1==majorityFontId2 && majorityFontSize1==majorityFontSize2;
	}
	
	private float verticalDistance(TableCell cell1, TableCell cell2) {
		float maxYPos1 = 0;
		if (cell1 != null) {
			for (Block word : cell1.getWords()) {
				BoundingBox bbox = word.getBoundingBox();
				if (bbox.maxy > maxYPos1) {
					maxYPos1 = bbox.maxy;
				}
			}
		}
		float minYPos2 = Float.POSITIVE_INFINITY;
		if (cell2 != null) {
			for (Block word : cell2.getWords()) {
				BoundingBox bbox = word.getBoundingBox();
				if (bbox.miny < minYPos2) {
					minYPos2 = bbox.miny;
				}
			}
		}
		return minYPos2 - maxYPos1;
	}
	
	
	private TableCell[][] mergeRows(TableCell[][] rawTable) {
		
		if (rawTable.length < MIN_NUM_ROWS_FOR_MERGING) {
			return rawTable;
		}
		
		float avgDistance = 0;
		float[] minDistances = new float[rawTable.length];
		for (int rowId = 0; rowId < rawTable.length-1; rowId++) {
			float minDistance = Float.MAX_VALUE;
			for (int colId = 0; colId < rawTable[rowId].length; colId++) {
				float distance = verticalDistance(rawTable[rowId][colId], rawTable[rowId+1][colId]);
				if (distance < minDistance) {
					minDistance = distance;
				}
			}
			minDistance = Math.max(0, minDistance);
			avgDistance += minDistance;
			minDistances[rowId+1] = minDistance;
		}
		avgDistance /= rawTable.length - 1;
		
		List<List<TableCell>> postprocessedTable = new ArrayList<List<TableCell>>(rawTable.length);
		for (int rowId = 0; rowId < rawTable.length; rowId++) {
			int numEmptyCells = 0;
			boolean mergeCandidate = rowId > 0;
			List<TableCell> row = new ArrayList<TableCell>(rawTable[rowId].length);
			for (int colId = 0; colId < rawTable[rowId].length; colId++) {
				TableCell cell = rawTable[rowId][colId];
				row.add(cell);
				if (cell.isEmpty()) {
					numEmptyCells++;
				} else if (rowId > 0) {
					TableCell upperCell = rawTable[rowId-1][colId];
					mergeCandidate = mergeCandidate && haveSameMajorityFont(cell, upperCell);
					mergeCandidate = mergeCandidate && 
							(cell.getWords().size() <= 1 || cell.toString().length() < upperCell.toString().length());
				}
			}
			mergeCandidate = mergeCandidate && minDistances[rowId] <= avgDistance;
			if (numEmptyCells > rawTable[rowId].length/2 && mergeCandidate) {
				for (int colId = 0; colId < rawTable[rowId].length; colId++) {
					TableCell cell = rawTable[rowId][colId];
					TableCell upperCell = postprocessedTable.get(postprocessedTable.size()-1).get(colId);
					upperCell.decreaseRowSpan();
					if (!cell.isEmpty()) {
						upperCell.getWords().addAll(cell.getWords());
					}
				}
			} else {
				postprocessedTable.add(row);
			}
		}
		
		TableCell[][] result = new TableCell[postprocessedTable.size()][];
		int i=0;
		for (List<TableCell> row : postprocessedTable) {
			result[i++] = row.toArray(new TableCell[row.size()]);
		}
		
		return result;
	}
		
	private TableCell[][] mergeColumns(TableCell[][] rawTable) {
		
		List<List<TableCell>> postprocessedTable = new ArrayList<List<TableCell>>(rawTable.length);
		for (int rowId = 0; rowId < rawTable.length; rowId++) {
			postprocessedTable.add(new ArrayList<TableCell>());
		}
		int numCols = TableUtils.getNumCols(rawTable);
		if (numCols < MIN_NUM_COLS_FOR_MERGING) {
			return rawTable;
		}
		for (int colId = 0; colId < numCols; colId++) {
			int numEmptyCells = 0;
			int numCells = 0;
			boolean mergeCandidate = colId > 0;
			for (int rowId = 0; rowId < rawTable.length; rowId++) {
				if (colId >= rawTable[rowId].length) {
					continue;
				}
				TableCell cell = rawTable[rowId][colId];
				numCells++;
				if (cell.isEmpty()) {
					numEmptyCells++;
				} else if (colId > 0) {
					TableCell leftCell = rawTable[rowId][colId-1];
					mergeCandidate = mergeCandidate && haveSameMajorityFont(cell, leftCell);
					mergeCandidate = mergeCandidate && 
							(cell.getWords().size() <= 1 || cell.toString().length() < leftCell.toString().length());
				}
			}
			for (int rowId = 0; rowId < rawTable.length; rowId++) {
				if (colId >= rawTable[rowId].length) {
					continue;
				}
				TableCell cell = rawTable[rowId][colId];
				List<TableCell> rowList = postprocessedTable.get(rowId);
				if (numEmptyCells > numCells/2 && mergeCandidate) {
					TableCell leftCell = rowList.get(rowList.size()-1);
					leftCell.decreaseColSpan();
					if (!cell.isEmpty()) {
						leftCell.getWords().addAll(cell.getWords());
					}
				} else {
					rowList.add(cell);
				}
			}
		}
		
		TableCell[][] result = new TableCell[postprocessedTable.size()][];
		int i=0;
		for (List<TableCell> row : postprocessedTable) {
			result[i++] = row.toArray(new TableCell[row.size()]);
		}
		
		return result;
	}
	
	private void mergeCloseCells(TableCell[][] cells) {
		DescriptiveStatistics gapStatistics = new DescriptiveStatistics();
		for (TableCell[] row : cells) {
			for (TableCell cell : row) {
				List<Block> words = cell.getWords();
				for (int i = 0; i < words.size()-1; i++) {
					float gap = words.get(i+1).getBoundingBox().minx - words.get(i).getBoundingBox().maxx;
					gapStatistics.addValue(gap);
				}
			}
		}
		double avgWordGap = gapStatistics.getMean();
		double stdWordGap = gapStatistics.getStandardDeviation();
		
		for (int rowId = 0; rowId < cells.length; rowId++) {
			for (int colId = 0; colId < cells[rowId].length; colId++) {
				TableCell cell = cells[rowId][colId];
				if (cell.isEmpty()) {
					continue;
				}
				int nextColId = colId+1;
				
				if (nextColId < cells[rowId].length) {
					TableCell nextCell = cells[rowId][nextColId];
					if (nextCell.isEmpty()) {
						continue;
					}
					List<Block> orderedWords = cell.getOrderedWords();
					Block lastWord = orderedWords.get(orderedWords.size()-1);
					orderedWords = nextCell.getOrderedWords();
					Block firstWord = orderedWords.get(0);
					float gap = firstWord.getBoundingBox().minx - lastWord.getBoundingBox().maxx;
					if (gap <= avgWordGap + 1.5*stdWordGap) {
						cell.setColSpan(2);
					}
				}
				
			}
		}
	}
	
	private void cleanUpSpans(TableCell[][] cells) {
		int numCols = TableUtils.getNumCols(cells);
		int[] remainingRowSpans = new int[numCols];
		int[] currentRowSpans = new int[numCols];
		TableCell[] currentRowCells = new TableCell[numCols];
		for (int rowId = 0; rowId < cells.length; rowId++) {
			int remainingColSpan = 0;
			int currentColSpan = 0;
			TableCell currentColCell = null;
			for (int colId = 0; colId < cells[rowId].length; colId++) {
				TableCell cell = cells[rowId][colId];
				if (remainingColSpan > 0) {
					currentColCell.getWords().addAll(cell.getWords());
					cells[rowId][colId] = new TableCell();
					remainingColSpan--;
					currentColSpan++;
				} else {
					if (currentColCell != null) currentColCell.setColSpan(currentColSpan);
					currentColCell = cell;
					currentColSpan = 1;
				}
				if (remainingRowSpans[colId] > 0) {
					currentRowCells[colId].getWords().addAll(cell.getWords());
					cells[rowId][colId] = new TableCell();
					remainingRowSpans[colId]--;
					currentRowSpans[colId]++;
				} else {
					if (currentRowCells[colId] != null) currentRowCells[colId].setRowSpan(currentRowSpans[colId]);
					currentRowCells[colId] = cell;
					currentRowSpans[colId] = 1;
				}
				remainingColSpan += cell.getColSpan() - 1;
				remainingRowSpans[colId] += cell.getRowSpan() - 1;
			}
			currentColCell.setColSpan(currentColSpan);
		}
		for (int colId = 0; colId < numCols; colId++) {
			currentRowCells[colId].setRowSpan(currentRowSpans[colId]);
		}
	}
		
	@Override
	public TableCell[][] parseTable(TableRegion tableRegion)
			throws TableException {
		
		System.out.println();
		if (tableRegion.captionBlock != null) {
			System.out.println(tableRegion.captionBlock.getText());
		}
		List<Column> columns = findColumns(tableRegion);
		List<Row> rows = findRows(tableRegion);
		
//		for (List<Block> column : columns) {
//			System.out.println(column);
//		}
		
		TableCell[][] cells = new TableCell[rows.size()][columns.size()];
		for (int rowId = 0; rowId < rows.size(); rowId++) {
			Row row = rows.get(rowId);
			for (int colId = 0; colId < columns.size(); colId++) {
				Column column = columns.get(colId);
				List<Block> words = new ArrayList<Block>(column.words);
				words.retainAll(row.words);
				
				// colspan those columns where a word spans across the column border
				boolean merge = false;
				int nextColId = colId+1;
				int numCols = 1;
				while (nextColId < columns.size()) {
					Column nextColumn = columns.get(nextColId);
					float minWordPos = nextColumn.getMinWordPos();
					for (Block word : words) {
						if (word.getBoundingBox().maxx > minWordPos) {
							merge = true;
							numCols++;
							break;
						}
					}
					nextColId++;
					if (!merge) {
						break;
					}
				}
				
				//merge = false;
				if (merge) {
					cells[rowId][colId] = new TableCell(words, 1, numCols);
				} else {
					cells[rowId][colId] = new TableCell(words);
				}
			}
		}
		
		// merge those adjacent cells with a small horizontal gap (space)
		mergeCloseCells(cells);

		// clean up spans
		cleanUpSpans(cells);
		
		// merge rows and columns with a large number of empty cells
		cells = mergeRows(cells);
		cells = mergeColumns(cells);

		return cells;
		
	}

}
