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



/**
 * 
 * @author sklampfl
 *
 */
public class StringMatching {

	private StringMatching() {}
	
	public static int computeEditDistance(String s, String t) {
		// according to http://en.wikipedia.org/wiki/Levenshtein_distance 
		
		int m = s.length();
		int n = t.length();
		int[] current = new int[n+1];
		int[] previous = new int[n+1];
		int[] temp;
		int a,b,c,newValue;

		if (n==0) {
			return m;
		} else if (m==0) {
			return n;
		}
		
		// target prefixes can be reached from empty source prefix
		// by inserting every characters
		for (int j=0; j<=n; j++) {
			previous[j] = j;
		}
		
		for (int i=1; i<=m; i++) {
			// source prefix can be transformed into empty string by
			// dropping all characters
			current[0] = i;
			for (int j=1; j<=n; j++) {
				if (s.charAt(i-1)==t.charAt(j-1)) {
					// no operation required
					current[j] = previous[j-1];
				} else {
					a = previous[j]+1; // deletion
					b = current[j-1]+1; // insertion
					c = previous[j-1]+1; // substitution
					newValue = Math.min(a, b);  
					newValue = Math.min(newValue, c);  
					current[j] = newValue;
				}
			}
			temp = previous;
			previous = current;
			current = temp;
			
		}

		return previous[n];
	}

	public static int computeLongestCommonSubstring(String s, String t) {
		if (s.isEmpty() || t.isEmpty()) {
			return 0;
		}

		int m = s.length();
		int n = t.length();
		int cost = 0;
		int maxLen = 0;
		int[] p = new int[n];
		int[] d = new int[n];

		for (int i = 0; i < m; ++i) {
			for (int j = 0; j < n; ++j) {
				// calculate cost/score
				if (s.charAt(i) != t.charAt(j)) {
					cost = 0;
				} else {
					if ((i == 0) || (j == 0)) {
						cost = 1;
					} else {
						cost = p[j - 1] + 1;
					}
				}
				d[j] = cost;

				if (cost > maxLen) {
					maxLen = cost;
				}
			} // for {}

			int[] swap = p;
			p = d;
			d = swap;
		}

		return maxLen;
	}
	
	private static final int DELETE = 0;

	private static final int INSERT = 1;

	private static final int STEPS = 2;

	
	/**
	 * Computes the number of delete and insert operations to rewrite b to a.
	 * @param a The first array.
	 * @param b The second array.
	 * @return An integer array with the first element corresponding to the
	 *         number of delete operations and the second element to the number
	 *         of insert operations.
	 */
	public static int[] computeEditDistances(char[] a, char[] b) {

		int[] result = new int[2];

		int[][] current = new int[3][b.length + 1];
		int[][] previous = new int[3][b.length + 1];

		if (a.length == 0 || b.length == 0) {
			result[DELETE] = b.length;
			result[INSERT] = a.length;
			return result;
		}

		for (int j = 0; j <= b.length; j++) {
			previous[STEPS][j] = previous[DELETE][j] = j;
			previous[INSERT][j] = 0;
		}

		for (int i = 1; i <= a.length; i++) {
			current[STEPS][0] = current[INSERT][0] = i;
			current[DELETE][0] = 0;
			for (int j = 1; j <= b.length; j++) {
				if (a[i - 1]==b[j - 1]) {
					current[STEPS][j] = previous[STEPS][j - 1];
					current[DELETE][j] = previous[DELETE][j - 1];
					current[INSERT][j] = previous[INSERT][j - 1];
				} else {

					int insertion = previous[STEPS][j] + 1;
					int deletion = current[STEPS][j - 1] + 1;
					int substitution = previous[STEPS][j - 1] + 1;
					int min = Math.min(Math.min(deletion, insertion),
							substitution);
					current[STEPS][j] = min;

					if (min == deletion) {
						current[DELETE][j] = current[DELETE][j - 1] + 1;
						current[INSERT][j] = current[INSERT][j - 1];
					} else if (min == insertion) {
						current[DELETE][j] = previous[DELETE][j];
						current[INSERT][j] = previous[INSERT][j] + 1;
					} else {
						current[DELETE][j] = previous[DELETE][j - 1] + 1;
						current[INSERT][j] = previous[INSERT][j - 1] + 1;
					}
				}
			}

			// Swap current and previous
			int[][] temp = previous;
			previous = current;
			current = temp;
		}

		result[DELETE] = previous[DELETE][b.length];
		result[INSERT] = previous[INSERT][b.length];

		return result;
	}


	public static void main(String[] args) {
		System.out.println(computeEditDistance("kitten","sitten"));
		System.out.println(computeEditDistance("sitten","sittin"));
		System.out.println(computeEditDistance("sittin","sitting"));
		System.out.println(computeEditDistance("kitten","sitting"));
		System.out.println(computeEditDistance("","12345"));
	}
	
}
