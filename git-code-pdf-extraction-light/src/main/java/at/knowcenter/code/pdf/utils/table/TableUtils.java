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

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;

import at.knowcenter.code.api.pdf.Block;
import at.knowcenter.code.api.pdf.BoundingBox;
import at.knowcenter.code.api.pdf.TableRegion;
import at.knowcenter.code.pdf.utils.GeometricUtils;

/**
 * 
 * @author sklampfl
 *
 */
public class TableUtils {
	
	private final static String INT_REGEX = "[0-9]+";
	private final static String DOUBLE_REGEX = "[-+]?[0-9]*[\\.,]?[0-9]+([eE][-+]?[0-9]+)?";
	private final static String BIG_NUMBER = "[0-9]{1,3}(\\s*[,\\.\\s]\\s*[0-9]{3})+(\\s*[,\\.\\s]\\s*[0-9]{1,3})*";
	private final static String RANGE_REGEX = "(?:"+INT_REGEX+"\\s*[-–]\\s*"+INT_REGEX+"|"
		+DOUBLE_REGEX+"\\s*-\\s*"+DOUBLE_REGEX+"|"+BIG_NUMBER+"\\s*-\\s*"+BIG_NUMBER+")";
	private final static String NUMBER_REGEX = "(?:"+INT_REGEX+"|"+BIG_NUMBER+"|"+DOUBLE_REGEX+")";

	private final static Pattern NUMBER_PATTERN = Pattern.compile(NUMBER_REGEX);
	
	public static String getText(List<Block> words) {
		StringBuilder buffer = new StringBuilder();
		for (Block word : words) {
			String text = word.getText();
			text = Normalizer.normalize(text, Normalizer.Form.NFKC);
			buffer.append(text).append(" ");
		}
		return buffer.toString();
	}
	
	public static int getNumCols(TableCell[][] cells) {
		int numCols = 0;
		for (int rowId = 0; rowId < cells.length; rowId++) {
			numCols = Math.max(numCols, cells[rowId].length);
		}
		return numCols;
	}

	public static String createHtmlTable(String title, TableCell[][] cells) {
		if (cells==null || cells.length==0) {
			return "";
		}
		
		int numCols = getNumCols(cells);
		StringBuilder cellBuffer = new StringBuilder();
		BoundingBox tableBBox = null;
		int pageNumber = -1;
		int[] remainingRowsToSpan = new int[numCols];
		for (int i=0; i<cells.length; i++) {
			cellBuffer.append("<tr>");
			int remainingColsToSpan = 0;
			if (cells[i]!=null) {
				for (int j=0; j<cells[i].length; j++) {
					if (remainingColsToSpan > 0) {
						remainingColsToSpan--;
						continue;
					}
					if (remainingRowsToSpan[j] > 0) {
						remainingRowsToSpan[j]--;
						continue;
					}
					TableCell cell = cells[i][j];
					BoundingBox bbox = cell.getBoundingBox();
					if (pageNumber < 0 && cell.getWords().size()>0) {
						pageNumber = cell.getWords().get(0).getPage().getNumber();
					}
					if (tableBBox==null) {
						tableBBox = bbox;
					} else if (bbox!=null) {
						tableBBox = tableBBox.union(bbox);
					}
					cellBuffer.append("<td data-bbox=\""+bbox+"\"");
					int colSpan = cell.getColSpan();
					remainingColsToSpan = colSpan - 1;
					int rowSpan = cell.getRowSpan();
					remainingRowsToSpan[j] = rowSpan - 1;
					if (colSpan > 1) {
						cellBuffer.append(" colspan=\""+colSpan+"\"");
					}
					if (rowSpan > 1) {
						cellBuffer.append(" rowspan=\""+rowSpan+"\"");
					}
					cellBuffer.append(">");
					cellBuffer.append(StringEscapeUtils.escapeHtml(cell.toString())).append("</td>");
				}
			}
			cellBuffer.append("</tr>");
		}
		StringBuilder tableBuffer = new StringBuilder();
		tableBuffer.append("<table border=\"1\" summary=\""+title+"\"");
		tableBuffer.append(" data-page=\""+pageNumber+"\"");
		tableBuffer.append(" data-bbox=\""+tableBBox+"\"");
		tableBuffer.append(">");
		tableBuffer.append(cellBuffer);
		tableBuffer.append("</table>");
		return tableBuffer.toString();		
	}
	
	public static String createHtmlTable(TableCell[][] cells) {
		return createHtmlTable("", cells);		
	}
		
	public static String createCsvTable(TableCell[][] cells, char separator) {
		if (cells==null || cells.length==0) {
			return "";
		}
		
		StringBuilder cellBuffer = new StringBuilder();
		for (int i=0; i<cells.length; i++) {
			if (cells[i]!=null) {
				for (int j=0; j<cells[i].length; j++) {
					TableCell cell = cells[i][j];
					String content = cell.toString();
					if (content.contains(String.valueOf(separator))) {
						cellBuffer.append('"').append(content).append('"');
					} else {
						cellBuffer.append(content);
					}
					cellBuffer.append(separator);
				}
			}
			cellBuffer.append("\n");
		}
		return cellBuffer.toString();		
	}

	public static List<BoundingBox> findLargestWhitespaces(int n, TableRegion tableRegion) {
		List<BoundingBox> obstacles = new ArrayList<BoundingBox>(tableRegion.words.size());
		for (Block word : tableRegion.words) {
			obstacles.add(word.getBoundingBox());
		}
		return GeometricUtils.findLargestWhitespaces(n, tableRegion.tableBBox, obstacles);
	}
	
	public static boolean isNumber(String s) {
		return NUMBER_PATTERN.matcher(s).matches();
	}
	
	public static class Column {
		final List<Block> words;
		final float leftXPos;
		final float rightXPos;
		public Column(List<Block> words, float leftXPos, float rightXPos) {
			this.words = words;
			this.leftXPos = leftXPos;
			this.rightXPos = rightXPos;
		}
		public float getMinWordPos() {
			float min = Float.POSITIVE_INFINITY;
			for (Block word : words) {
				float minx = word.getBoundingBox().minx;
				if (minx < min) {
					min = minx;
				}
			}
			return min;
		}
		public float getMaxWordPos() {
			float max = 0f;
			for (Block word : words) {
				float maxx = word.getBoundingBox().maxy;
				if (maxx > max) {
					max = maxx;
				}
			}
			return max;
		}
		public List<Block> getWords() {
			return words;
		}
	}
	
	public static class Row {
		final List<Block> words;
		final float topYPos;
		final float bottomYPos;
		public Row(List<Block> words, float topYPos, float bottomYPos) {
			this.words = words;
			this.topYPos = topYPos;
			this.bottomYPos = bottomYPos;
		}
		public float getMinWordPos() {
			float min = Float.POSITIVE_INFINITY;
			for (Block word : words) {
				float miny = word.getBoundingBox().miny;
				if (miny < min) {
					min = miny;
				}
			}
			return min;
		}
		public float getMaxWordPos() {
			float max = 0f;
			for (Block word : words) {
				float maxy = word.getBoundingBox().maxy;
				if (maxy > max) {
					max = maxy;
				}
			}
			return max;
		}
		public List<Block> getWords() {
			return words;
		}
	}

}
