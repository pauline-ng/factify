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
package pdfStructure;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import utility.Span;
import at.knowcenter.code.api.pdf.Block;
import at.knowcenter.code.api.pdf.BlockLabel;
import at.knowcenter.code.pdf.AnnotatedDocumentBuilder.LineCollector;
import at.knowcenter.code.pdf.PdfExtractionPipeline;
import at.knowcenter.code.pdf.blockclassification.BlockLabeling;

/**
 * Utility class to bridge the output of {@link at.knowcenter.code} and {@link PDF}
 * 
 * <pre>
 * Non-instantiable
 * </pre>
 *
 */
public class PDFtoStructure {
	private PDFtoStructure(){}

	public static List<Paragraph> convert(List<Block> blocks, BlockLabeling labeling,PdfExtractionPipeline pipline) {

		List<Paragraph> paragraphs = new ArrayList<Paragraph>();
		for(Block old : blocks) {//assume all lines are adjusted aligned
			if(labeling.getLabel(old) == BlockLabel.Heading) {
				String text = old.getText();
				text = Normalizer.normalize(text, Normalizer.Form.NFKC);
				Paragraph para = new Paragraph(text);
				para.label = BlockLabel.Heading;
				para.headingBlock = old;
				HashSet<Integer> pages = new HashSet<Integer>();
				for(Block each : old.getSubBlocks()) {
					pages.add(each.getPage().getNumber());
				}
				para.pages = new Span(Collections.min(pages), Collections.max(pages) + 1);
				paragraphs.add(para);
				continue;
			}
			//now labeling.getLabel(old) == BlockLabel.Main || else
			List<Block> old_lines = new ArrayList<Block>();
			old_lines.addAll(old.getSubBlocks());//old.getLineBlocks(); 
			ArrayList<Block> new_para = new ArrayList<Block>();
			new_para.addAll(old_lines);
			String text = "";
			if (new_para.size()>0) {
				text = pipline.clearHyphenations(new_para);
			}
			Paragraph newPara = new Paragraph(text);
			newPara.bodySubBlocks = new ArrayList<Block>();
			newPara.bodySubBlocks.addAll(new_para);
			if(new_para.size() != old_lines.size()) {
				newPara.remark += "new paragraph;";
			}
			HashSet<Integer> pages = new HashSet<Integer>();
			for(Block each : old.getSubBlocks()) {
				pages.add(each.getPage().getNumber());
			}
			newPara.pages = new Span(Collections.min(pages), Collections.max(pages) + 1);
			paragraphs.add(newPara);
		}
		return paragraphs;
	}
}
