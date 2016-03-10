package pdfStructure;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import at.knowcenter.code.api.pdf.Block;
import at.knowcenter.code.api.pdf.BlockLabel;
import at.knowcenter.code.pdf.AnnotatedDocumentBuilder.LineCollector;
import utility.Span;

/**
 * this is not exactly a paragraph, but a huge block of texts across columns, pages
 * @author huangxc
 *
 */
public class Paragraph {

//	public enum TYPE {Paragraph, Heading};
	
	public String text;
	public Span pages; //[)
	public BlockLabel label;
	public LineCollector t;
	public String remark = "";
	public Block headingBlock;
	public List<Block> bodySubBlocks;
	
	public Paragraph(String text) {
		// TODO Auto-generated constructor stub
		this.text = text;
	}
	public Paragraph(LineCollector lc, BlockLabel label) {
		// TODO Auto-generated constructor stub
		t = lc;
		this.label = label;
		HashSet<Integer> pages_ = new HashSet<Integer>();
		for(Block line : lc.lineToPage.keySet()) {
			pages_.add(lc.lineToPage.get(line).getNumber());
		}
		pages = new Span(Collections.min(pages_), Collections.max(pages_) + 1);
//		this.text = lc.
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
