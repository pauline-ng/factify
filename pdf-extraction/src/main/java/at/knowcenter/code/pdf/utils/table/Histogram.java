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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


class Histogram {
	private final int[] histogram;
	private final int minPos;
	
	public static abstract class Extremum implements Comparable<Extremum> {
		protected final int pos;
		protected final int width;
		protected final int depth;
		protected final int value;
		protected Extremum(int pos, int width, int depth, int value) {
			this.pos = pos;
			this.width = width;
			this.depth = depth;
			this.value = value;
		}
		@Override
		public int compareTo(Extremum o) {
			return Float.compare(this.pos, o.pos);
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + depth;
			result = prime * result + pos;
			result = prime * result + value;
			result = prime * result + width;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Extremum other = (Extremum) obj;
			if (depth != other.depth)
				return false;
			if (pos != other.pos)
				return false;
			if (value != other.value)
				return false;
			if (width != other.width)
				return false;
			return true;
		}
		@Override
		public String toString() {
			return String.format("%s: %d", getClass().getSimpleName(), pos);
		}
	}
	
	public static class Minimum extends Extremum {
		public Minimum(int pos, int width, int depth, int value) {
			super(pos, width, depth, value);
		}
	}
	
	public static class Maximum extends Extremum {
		public Maximum(int pos, int width, int height, int value) {
			super(pos, width, height, value);
		}
	}

	public Histogram(int[] histogram) {
		this(histogram, 0);
	}
	
	public Histogram(int[] histogram, int minPos) {
		this.histogram = histogram;
		this.minPos = minPos;
	}
	
	public void medianFilter(int kernelWidth) {
		int[] output = new int[histogram.length];
		int[] values = new int[kernelWidth];
		for (int i = 0; i < histogram.length; i++) {
			if (i < kernelWidth/2 || i >= histogram.length-kernelWidth/2) {
				//output[i] = histogram[i];
				output[i] = 0;
			} else {
				System.arraycopy(histogram, i-kernelWidth/2, values, 0, kernelWidth);
				Arrays.sort(values);
				output[i] = values[kernelWidth/2];
			}
		}
		System.arraycopy(output, 0, histogram, 0, histogram.length);;
	}
	
	public List<Minimum> findMinima() {
		return findMinima(Integer.MAX_VALUE, 1, 1);
	}
	
	public List<Minimum> findMinima(int maxValue, int minDepth, int minWidth) {
		List<Minimum> result = new ArrayList<Minimum>();
		int lastDiff = 0;
		int lastIdx = -1;
		for (int i=0; i<histogram.length-1; i++) {
			int diff = histogram[i] - histogram[i+1];
			if (diff != 0) {
				if (lastDiff > 0 && diff < 0) {
					int depth = (lastDiff - diff) / 2;
					int width = i - lastIdx + 1;
					if (histogram[i]<=maxValue && depth>=minDepth && width>=minWidth) {
						//System.out.println(String.format("%d-%d: %d (d=%d, w=%d)", 
						//	lastIdx, i, histogram[i], depth, width));
						result.add(new Minimum(minPos + lastIdx + width - 1, width, depth, histogram[i]));
					}
				}
				lastDiff = diff;
				lastIdx = i+1;
			}
		}
		return result;
	}
	
	public List<Maximum> findMaxima() {
		return findMaxima(0, 1, 1);
	}

	public List<Maximum> findMaxima(int minValue, int minHeight, int minWidth) {
		List<Maximum> result = new ArrayList<Maximum>();
		int lastDiff = 0;
		int lastIdx = -1;
		for (int i=0; i<histogram.length-1; i++) {
			int diff = histogram[i] - histogram[i+1];
			if (diff != 0) {
				if (lastDiff < 0 && diff > 0) {
					int height = (diff - lastDiff) / 2;
					int width = i - lastIdx + 1;
					if (histogram[i]>=minValue && height>=minHeight && width>=minWidth) {
						//System.out.println(String.format("%d-%d: %d (d=%d, w=%d)", 
						//	lastIdx, i, histogram[i], depth, width));
						result.add(new Maximum(minPos + lastIdx, width, height, histogram[i]));
					}
				}
				lastDiff = diff;
				lastIdx = i+1;
			}
		}
		return result;
	}
	
	public List<Extremum> findExtrema() {
		List<Extremum> result = new ArrayList<Extremum>();
		List<Minimum> minima = findMinima();
		List<Maximum> maxima = findMaxima();
		for (int i = 0; i < maxima.size()-1; i++) {
			Maximum max1 = maxima.get(i);
			Maximum max2 = maxima.get(i+1);
			Minimum min = minima.get(i);
			int width = max2.pos-max1.pos-max1.width;
			int depth = Math.max(max1.value - min.value, max2.value - min.value);
			result.add(new Minimum(min.pos, width, depth, min.value));
		}
		for (int i = 0; i < minima.size()-1; i++) {
			Minimum min1 = minima.get(i);
			Minimum min2 = minima.get(i+1);
			Maximum max = maxima.get(i+1);
			int width = min2.pos-min1.pos-min1.width;
			int height = Math.max(max.value - min1.value, max.value - min2.value);
			result.add(new Maximum(max.pos, width, height, max.value));
		}
		if (minima.size() > 0) {
			Maximum max = maxima.get(0);
			Minimum min = minima.get(0);
			result.add(new Maximum(max.pos, min.pos-minPos, max.value, max.value));
			max = maxima.get(maxima.size()-1);
			min = minima.get(minima.size()-1);
			result.add(new Maximum(max.pos, minPos+histogram.length-min.pos, max.value, max.value));
		}
		
		Collections.sort(result);
		return result;
	}
	
	public List<Integer> findSelectedMinima() {
		List<Integer> result = new ArrayList<Integer>();
		List<Integer> minima = new ArrayList<Integer>();
		List<Integer> maxima = new ArrayList<Integer>();
		int lastDiff = 0;
		int lastIdx = -1;
		int totalMax = 0;
		for (int i=0; i<histogram.length-1; i++) {
			int diff = histogram[i] - histogram[i+1];
			if (diff != 0) {
				if (lastDiff > 0 && diff < 0) {
					minima.add(minPos + lastIdx);
				}
				if (lastDiff < 0 && diff > 0) {
					maxima.add(minPos + lastIdx);
				}
				lastDiff = diff;
				lastIdx = i+1;
			}
			if (histogram[i] > totalMax) {
				totalMax = histogram[i];
			}
		}
		
		List<Minimum> minimaList = new ArrayList<Minimum>();
		for (int i = 0; i < maxima.size()-1; i++) {
			int max1 = maxima.get(i);
			int max2 = maxima.get(i+1);
			int min = minima.get(i);
			int maxValue1 = histogram[max1-minPos];
			int maxValue2 = histogram[max2-minPos];
			int minValue = histogram[min-minPos];
			int top = Math.max(maxValue1, maxValue2);
			int area = 0;
			int depth = Math.min(maxValue1, maxValue2) - minValue;
			for (int j = max1-minPos; j < max2-minPos; j++) {
				int value = histogram[j];
				if (value == maxValue1) { continue; }
				area += Math.max(0, top - value);
			}
			System.out.println(String.format("Max %d-%d (%d), Min %d (%d): Area %d; Depth: %d", max1, max2, totalMax, min, minValue, area, depth));
			minimaList.add(new Minimum(min, area, depth, minValue));
		}
		
		
		for (Minimum minimum : minimaList) {
			int conditionCount = 0;
			conditionCount += minimum.width>1000 ? 1 : 0; // for now, this is area!
			conditionCount += minimum.value<20 ? 1 : 0;
			conditionCount += minimum.depth>20 || minimum.depth>0.5*totalMax ? 1 : 0;
			if (conditionCount > 1) {
				result.add(minimum.pos);
			}
		}
		return result;
	}
	
	public int findOptimalThreshold() {
		List<Extremum> extrema = new ArrayList<Extremum>();
		extrema.addAll(findMinima());
		extrema.addAll(findMaxima());
		Collections.sort(extrema, new Comparator<Extremum>() {
			@Override
			public int compare(Extremum o1, Extremum o2) {
				return Float.compare(o1.value, o2.value);
			}
		});
		
		int threshold = 0;
		int numCuts = 1;
		int maxNumCuts = 1;
		for (Extremum extremum : extrema) {
			if (extremum instanceof Minimum) {
				numCuts++;
			} else if (extremum instanceof Maximum) {
				numCuts--;
			}
			if (numCuts > maxNumCuts) {
				maxNumCuts = numCuts;
				threshold = extremum.value + 1;
			}
		}
		
		return threshold;
	}
	
	public int getMinPos() {
		return minPos;
	}
	
	public int getMinValue() {
		int minValue = Integer.MAX_VALUE;
		for (int pos = 0; pos < histogram.length; pos++) {
			if (histogram[pos] < minValue) {
				minValue = histogram[pos];
			}
		}
		return minValue;
	}
	
	public int getMaxPos() {
		return minPos + histogram.length;
	}
	
	public int getMaxValue() {
		int maxValue = 0;
		for (int pos = 0; pos < histogram.length; pos++) {
			if (histogram[pos] > maxValue) {
				maxValue = histogram[pos];
			}
		}
		return maxValue;
	}

	public Maximum findMaximum(int fromPos, int toPos) {
		int maxValue = 0;
		int maxPos = 0;
		for (int pos = fromPos-minPos; pos < toPos-minPos; pos++) {
			if (pos>0 && pos<histogram.length) {
				if (histogram[pos] > maxValue) {
					maxValue = histogram[pos];
					maxPos = pos+minPos;
				}
			}
		}
		int width = toPos - fromPos;
		int height = maxValue - Math.max(histogram[fromPos-minPos], histogram[toPos-minPos]);
		return new Maximum(maxPos, width, height, maxValue);
	}

	public Minimum findMinimum(int fromPos, int toPos) {
		int minValue = Integer.MAX_VALUE;
		int minPosition = 0;
		for (int pos = fromPos-minPos; pos < toPos-minPos; pos++) {
			if (pos>0 && pos<histogram.length) {
				if (histogram[pos] < minValue) {
					minValue = histogram[pos];
					minPosition = pos+minPos;
				}
			}
		}
		int width = toPos - fromPos;
		int height = Math.min(histogram[fromPos-minPos], histogram[toPos-minPos])-minValue;
		return new Minimum(minPosition, width, height, minValue);
	}

	public int getArea(int fromPos, int toPos) {
		int area = 0;
		for (int pos = fromPos-minPos; pos < toPos-minPos; pos++) {
			if (pos>0 && pos<histogram.length) {
				area += histogram[pos];
			}
		}
		return area;
	}
	
	public int getEmptyArea(int fromPos, int toPos, int maxValue) {
		int area = 0;
		for (int pos = fromPos-minPos; pos < toPos-minPos; pos++) {
			if (pos>0 && pos<histogram.length) {
				area += Math.max(0, maxValue - histogram[pos]);
			}
		}
		return area;
	}

	public String toString() {
		return String.format("%d %s", minPos, Arrays.toString(histogram));
	}
}