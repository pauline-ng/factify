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
package at.knowcenter.code.pdf.blockclassification.detection;

import java.util.List;

import at.knowcenter.code.api.pdf.Block;
import at.knowcenter.code.api.pdf.Document;
import at.knowcenter.code.api.pdf.ReadingOrder;
import at.knowcenter.code.pdf.blockclassification.BlockLabeling;
import at.knowcenter.code.pdf.blockclassification.detection.metadata.ArticleMetadataCollector;
import at.knowcenter.code.pdf.blockrelation.geometric.BlockNeighborhood;

/**
 * interface for classes that assign a label to text blocks
 * 
 * @author sklampfl
 *
 */
public interface Detector {
	/**
	 * the detect method may assign a certain label to some of the given text blocks
	 * @param doc the {@link Document} the blocks come from.
	 * @param pageBlocks the list of blocks, one for each page in the document
	 * @param labeling the current block labeling, which is modified by this method
	 * @param readingOrder the {@link ReadingOrder} of the blocks
	 * @param articleMetadata TODO
	 * @param neighbourHood the {@link BlockNeighborhood}
	 */
	public void detect(Document doc, List<Block> pageBlocks, BlockLabeling labeling, 
			ReadingOrder readingOrder, BlockNeighborhood neighbourhood, ArticleMetadataCollector articleMetadata);
}
