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

import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import at.knowcenter.code.api.pdf.Block;
import at.knowcenter.code.api.pdf.BoundingBox;
import at.knowcenter.code.api.pdf.TextFragment;

/**
 * This class holds a block and additional features of this block used for clustering.
 * 
 * @author sklampfl
 *
 */
public class BlockInstance {

	private final Block block;
	private final Map<String,Object> features;
	
	/**
	 * creates a new instance for the given block.
	 * This constructor already extracts all features.
	 * @param block the block
	 */
	public BlockInstance(Block block) {
		this.block = block;
		this.features = new HashMap<String,Object>();
		extractFeatures();
	}
	
	private void extractFeatures() {
		BoundingBox bbox = block.getBoundingBox();
		features.put("minx", bbox.minx);
		features.put("width", bbox.maxx-bbox.minx);
		
		int maxFontSize = -1;
		TIntIntHashMap fontIdFrequency = new TIntIntHashMap();
		TIntIntHashMap fontSizeFrequency = new TIntIntHashMap();
		for (TextFragment fragment : block.getFragments()) {
			fontIdFrequency.adjustOrPutValue(fragment.getFontId(), 1, 1);
			int fontSize = (int) fragment.getFontSizePt();
			fontSizeFrequency.adjustOrPutValue(fontSize, 1, 1);
			if (fontSize > maxFontSize) {
				maxFontSize = fontSize;
			}
		}
		int majorityFontId = -1;
		int majorityFontSize = -1;
		int maxFreq = 0;
		int freq = 0;
        for (TIntIntIterator it = fontIdFrequency.iterator(); it.hasNext(); ) {
        	it.advance();
        	freq = it.value();
        	if (freq > maxFreq) {
        		maxFreq = freq;
        		majorityFontId = it.key();
        	}
        }
		maxFreq = 0;
		freq = 0;
        for (TIntIntIterator it = fontSizeFrequency.iterator(); it.hasNext(); ) {
        	it.advance();
        	freq = it.value();
        	if (freq > maxFreq) {
        		maxFreq = freq;
        		majorityFontSize = it.key();
        	}
        }
        features.put("majorityFontId", majorityFontId);
        features.put("majorityFontSize", majorityFontSize);
        features.put("maxFontSize", maxFontSize);
        
        String firstWord = null;
        for (Block word : block.getWordBlocks()) {
       		firstWord = word.getText();
       		break;
        }
        features.put("firstWord", firstWord);

	}
	
	/**
	 * gets the set of features that this instance holds
	 * @return a set of feature names
	 */
	public Set<String> getFeatures() {
		return features.keySet();
	}
	
	/**
	 * gets the value for the specified feature
	 * @param feature the feature
	 * @return the value for this feature
	 */
	public Object get(String feature) {
		if (!features.containsKey(feature)) {
			throw new IllegalArgumentException("Feature '"+feature+"' is not defined");
		}
		return features.get(feature);
	}
	
//	public void set(String feature, Object value) {
//		features.put(feature, value);
//	}
	
	/**
	 * gets the block for this block instance
	 * @return the block
	 */
	public Block getBlock() {
		return block;
	}
	
	@Override
	public String toString() {
		return features.toString();
	}
}
