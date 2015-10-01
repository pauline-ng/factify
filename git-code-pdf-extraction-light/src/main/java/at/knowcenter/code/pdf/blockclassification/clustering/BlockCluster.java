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

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import at.knowcenter.code.api.pdf.Block;

/**
 * This class holds a single cluster of blocks.
 * 
 * Each cluster holds a list of {@link BlockInstance}s.
 * 
 * @author sklampfl
 *
 */
public class BlockCluster implements Comparable<BlockCluster> {

	private final List<BlockInstance> blocks = new ArrayList<BlockInstance>();
	private int sizeInLines;
	private float totalHeight;
	
	/**
	 * initializes a new block cluster with a single instance
	 * @param instance the instance
	 */
	public BlockCluster(BlockInstance instance) {
		blocks.add(instance);
		List<Block> lineBlocks = instance.getBlock().getLineBlocks();
		if (lineBlocks!=null) {
			sizeInLines += lineBlocks.size();
		}
		totalHeight += instance.getBlock().getBoundingBox().getHeight();
	}
	
	/**
	 * returns the current blocks of this cluster
	 * @return the current list of blocks
	 */
	public List<Block> getBlocks() {
		List<Block> result = new ArrayList<Block>(blocks.size());
		for (BlockInstance instance : blocks) {
			result.add(instance.getBlock());
		}
		return result;
	}
	
	/**
	 * returns the current block instances of this cluster
	 * @return the current list of block instances
	 */
	public List<BlockInstance> getBlockInstances() {
		return blocks;
	}
	
	/**
	 * merges this cluster with another cluster
	 * @param other the other cluster
	 */
	public void merge(BlockCluster other) {
		this.blocks.addAll(other.blocks);
		for (Block block : other.getBlocks()) {
			List<Block> lineBlocks = block.getLineBlocks();
			if (lineBlocks!=null) {
				sizeInLines += lineBlocks.size();
			}
			totalHeight += block.getBoundingBox().getHeight();
		}
	}
	
	/**
	 * returns the number of blocks in this cluster
	 * @return the number of blocks
	 */
	public int size() {
		return blocks.size();
	}
	
	/**
	 * returns the size of this cluster measured as the number of text lines
	 * @return the number of lines over all blocks in this cluster
	 */
	public int sizeInLines() {
		return sizeInLines;
	}
	
	/**
	 * returns the average width of blocks in this cluster
	 * @return the average width
	 */
	public float meanWidth() {
		if (sizeInLines==0) {
			return 0f;
		}
		float result = 0.0f;
		for (BlockInstance instance : blocks) {
			Block block = instance.getBlock();
			List<Block> lineBlocks = block.getLineBlocks();
			if (lineBlocks!=null) {
				result += block.getBoundingBox().getWidth()*lineBlocks.size();
			}
		}
		return result/sizeInLines;
	}
	
	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(String.format("Cluster (Size: %d, Lines: %d)\n",size(),sizeInLines()));
		for (Block block : getBlocks()) {
			String text = block.getText();
			int length = 30;
			text = (text.length()>length)?text.substring(0, length)+"...":text;
			List<Block> lineBlocks = block.getLineBlocks();
			if (lineBlocks!=null) {
				buffer.append(text).append(" (").append(lineBlocks.size()).append(")").append("\n");
			}
		}
		return buffer.toString();
	}

	@Override
	public int compareTo(BlockCluster o) {
		// clusters are ordered by their size in text lines
		BlockCluster other = (BlockCluster) o;
		//return other.sizeInLines - this.sizeInLines;
		return Float.compare(other.totalHeight, this.totalHeight);
	}

	
}
