/**
    Copyright (C) 2016, Genome Institute of Singapore, A*STAR  

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package at.knowcenter.code.pdf.structure;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import at.knowcenter.code.api.pdf.Block;
import at.knowcenter.code.api.pdf.BlockLabel;
import at.knowcenter.code.pdf.AnnotatedDocumentBuilder.LineCollector;
import utility.Span;

/**
 * This is not exactly a paragraph, but a huge block of texts across columns, pages
 *
 */
public class Paragraph {

	public String text;
	public Span pages; //[) ; start with 1
	public BlockLabel label;
	public LineCollector t;
	public String remark = "";
	public Block headingBlock;
	public List<Block> bodySubBlocks;
	
	public Paragraph(String text) {
		this.text = text;
	}
	public Paragraph(LineCollector lc, BlockLabel label) {
		t = lc;
		this.label = label;
		HashSet<Integer> pages_ = new HashSet<Integer>();
		for(Block line : lc.lineToPage.keySet()) {
			pages_.add(lc.lineToPage.get(line).getNumber());
		}
		pages = new Span(Collections.min(pages_), Collections.max(pages_) + 1);
	}
	public String toString() {
		String s = "";
		for(Block line : t.lines) {
			s += line.getText() + "\r\n";
		}
		if(s.endsWith("\r\n")) s = s.substring(0, s.length() - "\r\n".length());
		return s;
	}
	public boolean isHeading() {
		return headingBlock == null ? false : true;
	}
	public String getHeadingText() {
		return isHeading() ? headingBlock.getText() : "";
	}
	
	public double getBodyBlockMeanFontSize() {
		if(this.bodySubBlocks.size() == 0) return -1.0;
		double mean = 0.0;
		int i = 0;
		while(i < this.bodySubBlocks.size()) {
			mean += this.bodySubBlocks.get(i++).getMeanFontSize();
		}
		return mean/(double) this.bodySubBlocks.size();
	}
}
