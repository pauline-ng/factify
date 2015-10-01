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

/**
 * The distance measure used for clustering
 * 
 * @author sklampfl
 *
 */
public interface DistanceMeasure {

	/**
	 * standard Euclidean distance
	 */
	public static final DistanceMeasure EUCLIDEAN_DISTANCE = new DistanceMeasure() {
		@Override
		public float distance(BlockInstance instance1, BlockInstance instance2) {
			float sum = 0.0f;
			for (String feature : instance1.getFeatures()) {
				if (instance1.get(feature) instanceof Float) {
					float value1 = (Float) instance1.get(feature);
					float value2 = (Float) instance2.get(feature);
					float diff = value1 - value2;
					sum += diff*diff;
				}
			}
			return (float) Math.sqrt(sum);
		}
	};
	
	/**
	 * calculates the distance between two {@link BlockInstance}s.
	 * A {@link BlockInstance} holds the features of the text block.
	 * Note that while distance should be symmetric,
	 * implementations are not required to have this property 
	 * 
	 * @param instance1 the first instance
	 * @param instance2 the second instance
	 * @return the distance
	 */
	public float distance(BlockInstance instance1, BlockInstance instance2);
	
}
