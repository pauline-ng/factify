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
package at.knowcenter.code.pdf.blockrelation.geometric.delaunay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.knowcenter.code.api.pdf.Block;
import at.knowcenter.code.api.pdf.BoundingBox;
import at.knowcenter.code.pdf.blockrelation.geometric.BlockNeighborhood;

/**
 * @author sklampfl
 *
 */
public class BlockCenterDelaunayTriangulation implements BlockNeighborhood {
	
	private List<Triangulation> dts;
	private List<Block> pageBlocks;
	private List<Map<Pnt,Block>> centerToBlock;
	private Map<Block,Integer> blockToPageId;
	
	public BlockCenterDelaunayTriangulation(List<Block> pageBlocks) {
		this.pageBlocks = pageBlocks;
		this.centerToBlock = new ArrayList<Map<Pnt,Block>>(pageBlocks.size());
		this.dts = new ArrayList<Triangulation>(pageBlocks.size());
		this.blockToPageId = new HashMap<Block,Integer>();
		buildTriangulations();
	}
	
	private static Pnt getCenter(Block block) {
		BoundingBox bbox = block.getBoundingBox();
		Pnt center = new Pnt(0.5*(bbox.maxx+bbox.minx),0.5*(bbox.maxy+bbox.miny));
		return center;
	}
	
	private static Triangle getEnclosingTriangle(Block pageBlock) {
		BoundingBox bbox = pageBlock.getBoundingBox();
		float height = bbox.getHeight()+bbox.miny;
		float width = bbox.getWidth()+bbox.minx;
		Pnt p1 = new Pnt(-width, -height);
		Pnt p2 = new Pnt(-width, 3*height);
		Pnt p3 = new Pnt(3*width, -height);
		return new Triangle(p1, p2, p3);
	}
	
	private void buildTriangulations() {
		for (int pageId = 0; pageId<pageBlocks.size(); pageId++) {
			Block pageBlock = pageBlocks.get(pageId);
			Map<Pnt,Block> centerMap = new HashMap<Pnt,Block>(pageBlocks.size());
            Triangulation dt = new Triangulation(getEnclosingTriangle(pageBlock));
            for (Block block : pageBlock.getSubBlocks()) {
				Pnt p = getCenter(block);
            	centerMap.put(p, block);
            	dt.delaunayPlace(p);
            	blockToPageId.put(block, pageId);
            }
            centerToBlock.add(centerMap);
            dts.add(dt);
		}
	}
	
	@Override
	public boolean isNeighbor(Block block1, Block block2) {
		if (blockToPageId.get(block1)!=blockToPageId.get(block2)) {
			return false;
		}
		for (Triangle tri : dts.get(blockToPageId.get(block1))) {
			if (tri.contains(getCenter(block1)) && tri.contains(getCenter(block2))) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public Set<Block> getNeighbors(Block block) {
		int pageId = blockToPageId.get(block);
		Set<Block> result = new HashSet<Block>(pageBlocks.size());
		Pnt center = getCenter(block);
		for (Triangle tri : dts.get(pageId)) {
			if (tri.contains(center)) {
				for (Pnt p : tri) {
					if (!p.equals(center)) {
						result.add(centerToBlock.get(pageId).get(p));
					}
				}
			}
		}
		return result;
	}
}
