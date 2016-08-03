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
package at.knowcenter.code.pdf.blockrelation.geometric;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import at.knowcenter.code.api.pdf.Block;
import at.knowcenter.code.api.pdf.BoundingBox;

/**
 * 
 * This class defines the closest blocks in either of the four main directions 
 * (North, East, South, West) as neighbors. It also defines overlapping blocks
 * as neighbors.
 * 
 * @author sklampfl
 * 
 */
public class DefaultBlockNeighborhood implements BlockNeighborhood, Serializable {

	private List<Block> pageBlocks;
	private Map<Block, Integer> blockToPageId;
	private List<Map<Block, Map<Direction, Set<Block>>>> neighbors;
	
	private final float tol;

	public enum Direction {
		North, South, East, West, All
	}
	
	public DefaultBlockNeighborhood(List<Block> pageBlocks, float tol) {
		this.pageBlocks = pageBlocks;
		this.tol = tol;
		this.blockToPageId = new HashMap<Block, Integer>();
		this.neighbors = new ArrayList<Map<Block, Map<Direction, Set<Block>>>>(pageBlocks.size());
		buildNeighborhood();
		makeSymmetric();
	}
	
	private void makeSymmetric() {
		for (Map<Block, Map<Direction, Set<Block>>> nbmap : neighbors) {
			for (Block block : nbmap.keySet()) {
				Map<Direction, Set<Block>> map = nbmap.get(block);
				for (Direction dir : map.keySet()) {
					Set<Block> set = map.get(dir);
					for (Block other : set) {
						switch (dir) {
						case North:
							nbmap.get(other).get(Direction.South).add(block);
							break;
						case South:
							nbmap.get(other).get(Direction.North).add(block);
							break;
						case East:
							nbmap.get(other).get(Direction.West).add(block);
							break;
						case West:
							nbmap.get(other).get(Direction.East).add(block);
							break;
						case All:
							nbmap.get(other).get(Direction.All).add(block);
							break;
						default:
							break;
						}
					}
				}
			}
		}
	}

	private void buildNeighborhood() {
		for (int i = 0; i < pageBlocks.size(); i++) {
			Block pageBlock = pageBlocks.get(i);
			SortedSet<Block> blocksOnPage = pageBlock.getSubBlocks();
			Map<Block, Map<Direction, Set<Block>>> neighborMap = new HashMap<Block, Map<Direction, Set<Block>>>(blocksOnPage.size());
			for (Block block : blocksOnPage) {
				blockToPageId.put(block, i);
				BoundingBox bbox = block.getBoundingBox();
				float minLeftDistance = Float.MAX_VALUE;
				float minRightDistance = Float.MAX_VALUE;
				float minTopDistance = Float.MAX_VALUE;
				float minBottomDistance = Float.MAX_VALUE;
				Set<Block> topNeighbors = new HashSet<Block>();
				Set<Block> bottomNeighbors = new HashSet<Block>();
				Set<Block> leftNeighbors = new HashSet<Block>();
				Set<Block> rightNeighbors = new HashSet<Block>();
				Set<Block> overlappingNeighbors = new HashSet<Block>();
				for (Block otherBlock : blocksOnPage) {
					if (block == otherBlock) {
						continue;
					}
					BoundingBox bbox2 = otherBlock.getBoundingBox();
					if (hasXOverlap(bbox, bbox2, tol)) {
						float topDistance = bbox.miny - bbox2.maxy;
						float bottomDistance = bbox2.miny - bbox.maxy;
						if (isAbove(bbox2, bbox, tol)) {
							if (topDistance < minTopDistance) {
								minTopDistance = topDistance;
								topNeighbors.clear();
								topNeighbors.add(otherBlock);
							} else if (topDistance == minTopDistance) {
								topNeighbors.add(otherBlock);
							}
						} else if (isAbove(bbox, bbox2, tol)) {
							if (bottomDistance < minBottomDistance) {
								minBottomDistance = bottomDistance;
								bottomNeighbors.clear();
								bottomNeighbors.add(otherBlock);
							} else if (bottomDistance == minBottomDistance) {
								bottomNeighbors.add(otherBlock);
							}
						} else {
							overlappingNeighbors.add(otherBlock);
						}
					}
					if (hasYOverlap(bbox, bbox2, tol)) {
						float leftDistance = bbox.minx - bbox2.maxx;
						float rightDistance = bbox2.minx - bbox.maxx;
						if (isLeft(bbox2, bbox, tol)) {
							if (leftDistance < minLeftDistance) {
								minLeftDistance = leftDistance;
								leftNeighbors.clear();
								leftNeighbors.add(otherBlock);
							} else if (leftDistance == minLeftDistance) {
								leftNeighbors.add(otherBlock);
							}
						} else if (isLeft(bbox, bbox2, tol)) {
							if (rightDistance < minRightDistance) {
								minRightDistance = rightDistance;
								rightNeighbors.clear();
								rightNeighbors.add(otherBlock);
							} else if (rightDistance == minRightDistance) {
								rightNeighbors.add(otherBlock);
							}
						} else {
							overlappingNeighbors.add(otherBlock);
						}
					}
				}
				Map<Direction, Set<Block>> directionMap = new HashMap<Direction, Set<Block>>(Direction.values().length);
				Set<Block> allNeighbors = new HashSet<Block>();
				allNeighbors.addAll(leftNeighbors);
				allNeighbors.addAll(rightNeighbors);
				allNeighbors.addAll(topNeighbors);
				allNeighbors.addAll(bottomNeighbors);
				allNeighbors.addAll(overlappingNeighbors);
				directionMap.put(Direction.All, allNeighbors);
				directionMap.put(Direction.North, topNeighbors);
				directionMap.put(Direction.South, bottomNeighbors);
				directionMap.put(Direction.West, leftNeighbors);
				directionMap.put(Direction.East, rightNeighbors);
				neighborMap.put(block, directionMap);
			}
			neighbors.add(neighborMap);
		}
	}

	private static boolean hasXOverlap(BoundingBox bbox1, BoundingBox bbox2, float tol) {
		boolean noOverlap = false;
		noOverlap = noOverlap || bbox1.precedesX(bbox2, tol)
				|| bbox2.precedesX(bbox1, tol);
		noOverlap = noOverlap || bbox1.meetsX(bbox2, tol)
				|| bbox2.meetsX(bbox1, tol);
		return !noOverlap;
	}

	private static boolean hasYOverlap(BoundingBox bbox1, BoundingBox bbox2, float tol) {
		boolean noOverlap = false;
		noOverlap = noOverlap || bbox1.precedesY(bbox2, tol)
				|| bbox2.precedesY(bbox1, tol);
		noOverlap = noOverlap || bbox1.meetsY(bbox2, tol)
				|| bbox2.meetsY(bbox1, tol);
		return !noOverlap;
	}

	private static boolean isAbove(BoundingBox bbox1, BoundingBox bbox2, float tol) {
		return bbox1.precedesY(bbox2, tol) || bbox1.meetsY(bbox2, tol) || bbox1.overlapsY(bbox2, tol);
	}

	private static boolean isLeft(BoundingBox bbox1, BoundingBox bbox2, float tol) {
		return bbox1.precedesX(bbox2, tol) || bbox1.meetsX(bbox2, tol) || bbox1.overlapsX(bbox2, tol);
	}

	@Override
	public boolean isNeighbor(Block block1, Block block2) {
		if (blockToPageId.get(block1)!=blockToPageId.get(block2)) {
			return false;
		}
		Set<Block> blockNeighbors = neighbors.get(blockToPageId.get(block1)).get(block1).get(Direction.All);
		return blockNeighbors.contains(block2);
	}

	@Override
	public Set<Block> getNeighbors(Block block) {
		return neighbors.get(blockToPageId.get(block)).get(block).get(Direction.All);
	}
	
	/**
	 * returns the neighbors for a given direction. Overlapping blocks are only
	 * contained in the set returned by the {@link #getNeighbors(Block)} method.
	 * @param block the block for which the neighbors should be returned
	 * @param direction East, West, North, South, or All
	 * @return the set of neighbors
	 */
	public Set<Block> getNeighbors(Block block, Direction direction) {
		return neighbors.get(blockToPageId.get(block)).get(block).get(direction);
	}

}
