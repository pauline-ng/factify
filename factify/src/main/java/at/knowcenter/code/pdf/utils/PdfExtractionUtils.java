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
package at.knowcenter.code.pdf.utils;

import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.map.hash.TIntIntHashMap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import at.knowcenter.code.api.pdf.Block;
import at.knowcenter.code.api.pdf.TableRegion;
import at.knowcenter.code.api.pdf.TextFragment;
import at.knowcenter.code.pdf.PdfExtractionPipeline.PdfExtractionResult;
import at.knowcenter.code.pdf.utils.table.TableCell;
import at.knowcenter.code.pdf.utils.table.TableException;
import at.knowcenter.code.pdf.utils.table.TableParser;
import at.knowcenter.code.pdf.utils.table.TableUtils;

/**
 * @author sklampfl
 *
 */
public class PdfExtractionUtils {

	private PdfExtractionUtils() {}
	
	public static void writeTextToFile(String text, String fileName, boolean append) throws IOException {
		File file = new File(fileName);
		Writer output = new BufferedWriter(new FileWriter(file, append));
		output.write(text);
		output.close();
	}
	
	public static String[] readLinesFromFile(String fileName) throws IOException {
	    BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
	    StringBuilder buf = new StringBuilder();
	    String str = null;
	    while ((str = in.readLine()) != null) {
	        buf.append(str);
	        buf.append("\n");
	    }
	    in.close();
		return buf.toString().split("\n");
	}

	public static double calculateMedian(double[] values) {
		Arrays.sort(values);
		int midIndex = values.length / 2;
		if (values == null || values.length == 0) {
			return 0.0;
		}
		if (values.length % 2 == 1) {
			return values[midIndex];
		} else {
			return 0.5 * (values[midIndex] + values[midIndex - 1]);
		}
	}
	
	public static double calculateMedian(Double[] values) {
		Arrays.sort(values);
		int midIndex = values.length / 2;
		if (values == null || values.length == 0) {
			return 0.0;
		}
		if (values.length % 2 == 1) {
			return values[midIndex];
		} else {
			return 0.5 * (values[midIndex] + values[midIndex - 1]);
		}
	}
	
	public static int getMajorityFontId(Block block) {
		return getMajorityFontId(block.getFragments());
	}
	
	public static int getMajorityFontId(List<TextFragment> fragments) {
		TIntIntHashMap fontIdFrequency = new TIntIntHashMap();
		for (TextFragment fragment : fragments) {
			fontIdFrequency.adjustOrPutValue(fragment.getFontId(), 1, 1);
		}
		int majorityFontId = -1;
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
        return majorityFontId;
	}
	
	public static int getMajorityFontSize(Block block) {
		return getMajorityFontSize(block.getFragments());
	}

	public static int getMajorityFontSize(List<TextFragment> fragments) {
		TIntIntHashMap fontSizeFrequency = new TIntIntHashMap();
		for (TextFragment fragment : fragments) {
			int fontSize = (int) fragment.getFontSizePt();
			fontSizeFrequency.adjustOrPutValue(fontSize, 1, 1);
		}
		int majorityFontSize = -1;
		int maxFreq = 0;
		int freq = 0;
        for (TIntIntIterator it = fontSizeFrequency.iterator(); it.hasNext(); ) {
        	it.advance();
        	freq = it.value();
        	if (freq > maxFreq) {
        		maxFreq = freq;
        		majorityFontSize = it.key();
        	}
        }
        return majorityFontSize;
	}
	
	public static void sortBlocksByX(List<Block> words) {
		Collections.sort(words, new Comparator<Block>() {
			@Override
			public int compare(Block o1, Block o2) {
				return Double.compare(o1.getBoundingBox().minx, o2.getBoundingBox().minx);
			}	
		});
	}
	
	public static void sortBlocksByY(List<Block> words) {
		Collections.sort(words, new Comparator<Block>() {
			@Override
			public int compare(Block o1, Block o2) {
				return Double.compare(o1.getBoundingBox().miny, o2.getBoundingBox().miny);
			}	
		});
	}
	
	public static void sortBlocksBySequenceId(List<Block> words) {
		Collections.sort(words, new Comparator<Block>() {
			@Override
			public int compare(Block o1, Block o2) {
				return Double.compare(o1.getMinimumSequenceId(), o2.getMinimumSequenceId());
			}	
		});
	}

	public static void writeTableToFile(TableParser tableParser, TableRegion tableRegion, String outputFileName, boolean append) 
			throws TableException, IOException {
		TableCell[][] cells = tableParser.parseTable(tableRegion);
		String captionText = tableRegion.captionBlock == null ? "" : tableRegion.captionBlock.getText();
		captionText = captionText.replaceAll(">", "&gt;");
		captionText = captionText.replaceAll("<", "&lt;");
		String htmlTable = "<div>"+captionText+"</div>"+TableUtils.createHtmlTable(cells)+"<br/>";
		writeTextToFile(htmlTable, outputFileName, append);
	}

	public static void writeTablesToFile(PdfExtractionResult result, TableParser tableParser, String outputFileName) 
			throws TableException, IOException {
		writeTextToFile("<!doctype html><html><head><meta charset=\"UTF-8\"></head><body>", outputFileName, false);
		for (TableRegion table : result.doc.getTables()) {
			writeTableToFile(tableParser, table, outputFileName, true);
		}
		writeTextToFile("</body></html>", outputFileName, true);
	}

	public static <T> List<T> getRandomSubList(List<T> input, int subsetSize) {
		List<T> result = new ArrayList<T>(subsetSize);
	    Random r = new Random();
	    int inputSize = input.size();
	    for (int i = 0; i < subsetSize; i++) {
	        int index = i + r.nextInt(inputSize - i);
	        result.add(input.get(index));
	    }
	    return result;
	}

	
}
