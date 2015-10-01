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

package at.knowcenter.code.pdf.blockextraction;

import java.util.List;

import at.knowcenter.code.api.pdf.Block;
import at.knowcenter.code.api.pdf.Document;
import at.knowcenter.code.pdf.PdfException;

/**
 *  Extractor of structure blocks on the pages of a PDF document.
 * 
 * @author Roman Kern <rkern@tugraz.at>
 */
public interface PdfPageBlockExtractor {
	/**
	 * Exception thrown by {@link PdfPageBlockExtractor} implementations.
	 * @author mzechner
	 *
	 */
	public static class PdfPageBlockExtractorException extends PdfException {
		private static final long serialVersionUID = -100671252958700723L;

		public PdfPageBlockExtractorException() {
			super();
		}

		public PdfPageBlockExtractorException(String message, Throwable cause) {
			super(message, cause);
		}

		public PdfPageBlockExtractorException(String message) {
			super(message);
		}

		public PdfPageBlockExtractorException(Throwable cause) {
			super(cause);
		}
		
	}
	
    /**
     * Extracts the blocks out of a parsed PDF document.
     * @param pdfDocument the document
     * @param id the id of the document, used for logging
     * @return the list of pages and their blocks
     */
    List<Block> extractBlocks(Document pdfDocument, String id);

}