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
package at.knowcenter.code.pdf.utils.table;

import java.util.Collections;
import java.util.List;

import at.knowcenter.code.api.pdf.Block;
import at.knowcenter.code.api.pdf.BoundingBox;
import at.knowcenter.code.pdf.utils.PdfExtractionUtils;

public class TableCell {
	
	private final List<Block> words;
	private int rowSpan;
	private int colSpan;
	
	public TableCell() {
		this(Collections.<Block>emptyList());
	}
	
	public TableCell(List<Block> words) {
		this(words, 1, 1);
	}
	
	public TableCell(List<Block> words, int rowSpan, int colSpan) {
		this.words = words;
		this.rowSpan = rowSpan;
		this.colSpan = colSpan;
	}
	
	public List<Block> getWords() {
		return words;
	}
	
	public List<Block> getOrderedWords() {
		PdfExtractionUtils.sortBlocksBySequenceId(words);
		return words;
	}
	
	public BoundingBox getBoundingBox() {
		BoundingBox bbox = null;
		if (words.size()>0) {
			bbox = words.get(0).getBoundingBox();
			for (Block word : words) {
				bbox = bbox.union(word.getBoundingBox());
			}
		}
		return bbox;
	}
	
	public int getRowSpan() {
		return rowSpan;
	}
	
	public void setRowSpan(int rowSpan) {
		this.rowSpan = rowSpan;
	}
	
	public int getColSpan() {
		return colSpan;
	}
	
	public void setColSpan(int colSpan) {
		this.colSpan = colSpan;
	}
	
	public boolean isEmpty() {
		return words.isEmpty();
	}
	
	public void increaseColSpan() {
		colSpan++;
	}

	public void decreaseColSpan() {
		colSpan = Math.max(1, colSpan - 1);
	}

	public void increaseRowSpan() {
		rowSpan++;
	}

	public void decreaseRowSpan() {
		rowSpan = Math.max(1, rowSpan - 1);
	}

	@Override
	public String toString() {
		PdfExtractionUtils.sortBlocksBySequenceId(words);
		return TableUtils.getText(words);
	}
}