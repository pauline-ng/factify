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
package at.knowcenter.code.pdf.utils.text;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.fontbox.util.ResourceLoader;
import org.apache.lucene.analysis.compound.hyphenation.Hyphenation;
import org.apache.lucene.analysis.compound.hyphenation.HyphenationException;
import org.xml.sax.InputSource;

//import org.apache.lucene.analysis.compound.hyphenation.HyphenationTree;

/**
 * 
 * @author sklampfl
 * 
 */
public class Dehyphenator {
	private static Logger log = Logger.getLogger(Dehyphenator.class.getSimpleName());

	private class HyphenationTree extends
			org.apache.lucene.analysis.compound.hyphenation.HyphenationTree {
		private static final long serialVersionUID = 1L;

		public Hyphenation hyphenate(char[] w, int offset, int len,
				int remainCharCount, int pushCharCount) {
			int i;
			char[] word = new char[len + 3];

			// normalize word
			char[] c = new char[2];
			int iIgnoreAtBeginning = 0;
			int iLength = len;
			boolean bEndOfLetters = false;
			for (i = 1; i <= len; i++) {
				// c[0] = w[offset + i - 1];
				// int nc = classmap.find(c, 0);
				// if (nc < 0) { // found a non-letter character ...
				// if (i == (1 + iIgnoreAtBeginning)) {
				// // ... before any letter character
				// iIgnoreAtBeginning++;
				// } else {
				// // ... after a letter character
				// bEndOfLetters = true;
				// }
				// iLength--;
				// } else {
				if (!bEndOfLetters) {
					word[i - iIgnoreAtBeginning] = w[offset + i - 1];
				} else {
					return null;
				}
				// }
			}
			len = iLength;
			if (len < (remainCharCount + pushCharCount)) {
				// word is too short to be hyphenated
				return null;
			}
			int[] result = new int[len + 1];
			int k = 0;

			// check exception list first
			String sw = new String(word, 1, len);
			if (stoplist.containsKey(sw)) {
				// assume only simple hyphens (Hyphen.pre="-", Hyphen.post =
				// Hyphen.no =
				// null)
				ArrayList<Object> hw = stoplist.get(sw);
				int j = 0;
				for (i = 0; i < hw.size(); i++) {
					Object o = hw.get(i);
					// j = index(sw) = letterindex(word)?
					// result[k] = corresponding index(w)
					if (o instanceof String) {
						j += ((String) o).length();
						if (j >= remainCharCount && j < (len - pushCharCount)) {
							result[k++] = j + iIgnoreAtBeginning;
						}
					}
				}
			} else {
				// use algorithm to get hyphenation points
				word[0] = '.'; // word start marker
				word[len + 1] = '.'; // word end marker
				word[len + 2] = 0; // null terminated
				byte[] il = new byte[len + 3]; // initialized to zero
				for (i = 0; i < len + 1; i++) {
					searchPatterns(word, i, il);
				}

				// hyphenation points are located where interletter value is odd
				// i is letterindex(word),
				// i + 1 is index(word),
				// result[k] = corresponding index(w)
				for (i = 0; i < len; i++) {
					if (((il[i + 1] & 1) == 1) && i >= remainCharCount
							&& i <= (len - pushCharCount)) {
						result[k++] = i + iIgnoreAtBeginning;
					}
				}
			}

			if (k > 0) {
				// trim result array
				int[] res = new int[k + 2];
				System.arraycopy(result, 0, res, 1, k);
				// We add the synthetical hyphenation points
				// at the beginning and end of the word
				res[0] = 0;
				res[k + 1] = len;
				hyphenationPoints = res;
				return null;
			} else {
				return null;
			}
		}

	}

	private final static String DEFAULT_HYPHENATION_FILENAME = "en.xml";
	public final static char HYPHENATION_CHAR = '-';
	private final HyphenationTree hyphenationTree;
	private int[] hyphenationPoints;
	private int remainCharCount;
	private int pushCharCount;

	public Dehyphenator(int remainCharCount, int pushCharCount) throws HyphenationException {
		InputStream in = Dehyphenator.class.getResourceAsStream(DEFAULT_HYPHENATION_FILENAME);
		hyphenationTree = new HyphenationTree();
		hyphenationTree.loadPatterns(new InputSource(in));
		//hyphenationTree.printStats();
		this.remainCharCount = remainCharCount;
		this.pushCharCount = pushCharCount;
		try { in.close(); } catch(Exception e) { };
	}

	public boolean checkHyphenation(String part1, String part2) {
		hyphenationPoints = null;
		String word = part1 + part2;
		Hyphenation h = hyphenationTree.hyphenate(word, remainCharCount, pushCharCount);
		if (h != null) {
			hyphenationPoints = h.getHyphenationPoints();
		}
		boolean result = false;
		if (hyphenationPoints != null) {
			for (int p : hyphenationPoints) {
				if (p == part1.length()) {
					result = true;
					break;
				}
			}
		}
		return result;
	}

	public int[] getHyphenationPoints(String word) {
		hyphenationPoints = null;
		Hyphenation h = hyphenationTree.hyphenate(word, remainCharCount, pushCharCount);
		if (h != null) {
			hyphenationPoints = h.getHyphenationPoints();
		}
		return hyphenationPoints;
	}
	
	public String clearHyphenations(String text) {
		StringBuilder buffer = new StringBuilder();
		String regex = "\\w+\\s*"+HYPHENATION_CHAR+"\\s*\\w+";
		Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        int lastIndex = 0;
        while (matcher.find()) {
        	int startIndex = matcher.start();
        	int endIndex = matcher.end();
        	String hyphenatedWord = matcher.group().replaceAll("\\s+", "");
        	int hyphIndex = hyphenatedWord.indexOf(HYPHENATION_CHAR);
        	String part1 = hyphenatedWord.substring(0, hyphIndex);
        	String part2 = hyphenatedWord.substring(hyphIndex+1);
        	buffer.append(text.substring(lastIndex,startIndex));
        	if (checkHyphenation(part1, part2)) {
        		buffer.append(part1).append(part2);
        	} else {
        		buffer.append(matcher.group());
        	}
        	lastIndex = endIndex;
        }
        buffer.append(text.substring(lastIndex));
		return buffer.toString();
	}
	
	private static String readLine() {
		String s = "";
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			s = in.readLine();
		} catch (Exception e) {
			log.log(Level.SEVERE, "Error while reading line", e); 
		}
		return s;
	}

	public static void main(String[] args) {

		Dehyphenator dh = null;
		try {
			dh = new Dehyphenator(2,3);
		} catch (HyphenationException e) {
			e.printStackTrace();
			System.exit(-1);
		}
//		System.out.println(Arrays.toString(dh.getHyphenationPoints("label")));
//		String part1 = "represe";
//		String part2 = "ntatives";
//		System.out.println(dh.checkHyphenation(part1, part2));
		
		while(true) {
			System.out.println("Enter a word:");
			String word = readLine();
			int[] hp = dh.getHyphenationPoints(word);
			StringBuilder buffer = new StringBuilder();
			int last = 0;
			if (hp==null) {
				buffer.append(word);
			} else {
				for (int p : hp) {
					if (p>0) {
						buffer.append(word.substring(last,p));
						if (p<word.length()) {
							buffer.append(HYPHENATION_CHAR);
						}
					}
					last = p;
				}
			}
			System.out.println("Hyphenated word: "+buffer.toString());
			System.out.println();
		}
	}

}
