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
package at.knowcenter.code.pdf.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import at.knowcenter.code.api.pdf.BoundingBox;

/**
 * 
 * @author sklampfl
 *
 */
public class GeometricUtils {
	
	private static class WhitespaceData implements Comparable<WhitespaceData> {
		BoundingBox parent;
		BoundingBox bound;
		List<BoundingBox> obstacles;
		public WhitespaceData(BoundingBox parent, BoundingBox bound, List<BoundingBox> obstacles) {
			this.parent = parent;
			this.bound = bound;
			this.obstacles = obstacles;
		}
		public float quality() {
			return bound.area();
		}
		@Override
		public int compareTo(WhitespaceData o) {
			// order descending!
			return -Float.compare(this.quality(), o.quality());
		}
		@Override
		public String toString() {
			return bound.toString();
		}
		
	}

	public static BoundingBox findLargestWhitespace(BoundingBox bound, List<BoundingBox> obstacles) {
		return findLargestWhitespaces(1, bound, obstacles).get(0);
	}
	
	public static List<BoundingBox> findLargestWhitespaces(int n, BoundingBox bound, List<BoundingBox> obstacles) {
		List<BoundingBox> result = new ArrayList<BoundingBox>();
		PriorityQueue<WhitespaceData> queue = new PriorityQueue<WhitespaceData>();
		queue.add(new WhitespaceData(bound, bound, obstacles));
		while (!queue.isEmpty()) {
			WhitespaceData data = queue.poll();
			if (data.obstacles.isEmpty()) {
				if (true) {
					result.add(data.bound);
					for (WhitespaceData d : queue) {
						if (d.bound.intersects(data.bound, 0)) {
							d.obstacles.add(data.bound);
						}
					}
				}
				if (result.size()==n) {
					break;
				} else {
					continue;
				}
			}
			int random = new Random().nextInt(data.obstacles.size());
			BoundingBox pivot = data.obstacles.get(random);
			BoundingBox r0 = new BoundingBox(pivot.maxx, data.bound.maxx, data.bound.miny, data.bound.maxy);
			BoundingBox r1 = new BoundingBox(data.bound.minx, pivot.minx, data.bound.miny, data.bound.maxy);
			BoundingBox r2 = new BoundingBox(data.bound.minx, data.bound.maxx, pivot.maxy, data.bound.maxy);
			BoundingBox r3 = new BoundingBox(data.bound.minx, data.bound.maxx, data.bound.miny, pivot.miny);
			for (BoundingBox bbox : new BoundingBox[]{r0,r1,r2,r3}) {
				List<BoundingBox> obst = new ArrayList<BoundingBox>(data.obstacles.size());
				for (BoundingBox o : data.obstacles) {
					if (o.intersects(bbox, 0)) {
						obst.add(o);
					}
				}
				queue.add(new WhitespaceData(data.bound, bbox, obst));
			}
		}
		return result;
	}
	
	

}
