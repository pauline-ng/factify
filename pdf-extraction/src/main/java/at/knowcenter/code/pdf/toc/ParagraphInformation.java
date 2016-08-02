/* Copyright (C) 2010 
"Kompetenzzentrum fuer wissensbasierte Anwendungen Forschungs- und EntwicklungsgmbH" 
(Know-Center), Graz, Austria, office@know-center.at.

Licensees holding valid Know-Center Commercial licenses may use this file in
accordance with the Know-Center Commercial License Agreement provided with 
the Software or, alternatively, in accordance with the terms contained in
a written agreement between Licensees and Know-Center.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package at.knowcenter.code.pdf.toc;

import java.util.Map;
import java.util.Set;

import at.knowcenter.code.api.pdf.Block;

/**
 * Information regarding paragraphs, for merging and splitting blocks.
 * 
 * @author rkern@know-center.at
 */
public class ParagraphInformation {
    private final Map<Block, Block> mergeSourceToTargetMap;
    private final Set<Block> mergeTargetSet;
    private final Map<Block, int[]> blocksToLeftOutlierLines;

    /**
     * Creates a new instance of this class.
     * @param mergeSourceToTargetMap 
     * @param mergeTargetSet 
     * @param blocksToLeftOutlierLines 
     */
    public ParagraphInformation(Map<Block, Block> mergeSourceToTargetMap, Set<Block> mergeTargetSet, 
            Map<Block, int[]> blocksToLeftOutlierLines) {
        this.mergeSourceToTargetMap = mergeSourceToTargetMap;
        this.mergeTargetSet = mergeTargetSet;
        this.blocksToLeftOutlierLines = blocksToLeftOutlierLines;
    }
    
    /**
     * @param block
     * @return
     */
    public int[] getLinesToSplit(Block block) {
        return blocksToLeftOutlierLines.get(block);
    }
    
    public Block getBlockToMerge(Block block) {
        return mergeSourceToTargetMap.get(block);
    }
    
    public boolean isMergeTarget(Block block) {
        return mergeTargetSet.contains(block);
    }

	public Map<Block, Block> getMergeSourceToTargetMap() {
		return mergeSourceToTargetMap;
	}

	public Set<Block> getMergeTargetSet() {
		return mergeTargetSet;
	}

	public Map<Block, int[]> getBlocksToLeftOutlierLines() {
		return blocksToLeftOutlierLines;
	}  
    
}
