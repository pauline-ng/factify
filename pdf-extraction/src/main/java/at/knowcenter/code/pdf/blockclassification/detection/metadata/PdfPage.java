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
package at.knowcenter.code.pdf.blockclassification.detection.metadata;

import java.util.List;

import at.knowcenter.code.api.pdf.Block;
import at.knowcenter.code.api.pdf.Page;

/**
 * 
 * 
 * @author Roman Kern <rkern@know-center.at>
 */
public class PdfPage {
	private final Page pdfPage;
	private final List<Block> pageBlocks;
    private int pageNumber;

	/**
	 * Creates a new instance of this class.
	 * 
	 * @param pdfPage
	 * @param pageBlocks
	 * @param pageNumber 
	 */
	public PdfPage(Page pdfPage, List<Block> pageBlocks, int pageNumber) {
		this.pdfPage = pdfPage;
		this.pageBlocks = pageBlocks;
        this.pageNumber = pageNumber;
	}
	
	/**
	 * Returns the pdfPage.
	 * 
	 * @return the pdfPage
	 */
	public Page getPdfPage() {
		return pdfPage;
	}

	/**
	 * Returns the pageBlocks.
	 * 
	 * @return the pageBlocks
	 */
	public List<Block> getPageBlocks() {
		return pageBlocks;
	}

    /**
     * @return
     */
    public int getPageNumber() {
        return pageNumber;
    }
}
