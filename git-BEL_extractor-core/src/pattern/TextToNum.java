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
package pattern;

import java.util.ArrayList;
import java.util.Arrays;

import utility.Debug;
import utility.Debug.DEBUG_CONFIG;
/**
 * This is from {@link https://ph.answers.yahoo.com/question/index?qid=20100820042716AAcemIX} ({@link http://pastebin.com/CbaRz14A;http://pastebin.com/cGSFUSBp;http://pastebin.com/fZLypaBn})
 * 
 * Convert text to non-negative integers (long): cannot deal with decimal or negatives
 * --cannot deal with "half an hour" or "a hundred"
 * 
 *
 */
public class TextToNum {

	
	  public static void main(String[] args) throws SpellException {
		  TextToNum textToNum = new TextToNum();
		  Debug.println(textToNum.parse("test"),DEBUG_CONFIG.debug_temp);
  }

  private  String[] myBelowThousandWords = { "zero", "one", "two",
		"three", "four", "five", "six", "seven", "eight", "nine", "ten",
		"eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen",
		"seventeen", "eighteen", "ninteen", "twenty", "thirty", "forty",
		"fifty", "sixty", "seventy", "eighty", "ninety", "hundred" };

	private  long[] myBelowThousandValuess = { 0, 1, 2, 3, 4, 5, 6, 7, 8,
		9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 30, 40, 50, 60, 70,
		80, 90, 100 };

	private  ArrayList<String> myBelowThousandWordList = new ArrayList<String>(
			Arrays.asList(myBelowThousandWords));

	private  Long parseBelow1000(String text) throws SpellException {

		long value = 0;
		String[] words = text.replaceAll(" and ", " ").split("\\s");

		for (String word : words) {
			if (!myBelowThousandWordList.contains(word)) {
//				throw new SpellException("Unknown token : " + word);
				return null;
			}

			long subval = getValueOf(word);

			if (subval == 100) {
				if (value == 0)
					value = 100;
				else
					value *= 100;
			} else
				value += subval;

		}

		return value;
	}

	private  long getValueOf(String word) {

		return myBelowThousandValuess[myBelowThousandWordList.indexOf(word)];
	}

	private  String[] mySuffixWords = { "trillion", "billion", "million",
	"thousand" };

	private  long[] mySuffixValues = { 1000000000000L, 1000000000L,
		1000000L, 1000L };

	public  Long parse(String text) {
		try {
		text = text.toLowerCase().replaceAll("[\\-,]", " ").replaceAll(" and ",
				" ");

		long totalValue = 0;

		boolean processed = false;

		for (int n = 0; n < mySuffixWords.length; n++) {

			int index = text.indexOf(mySuffixWords[n]);

			if (index >= 0) {
				String text1 = text.substring(0, index).trim();
				String text2 = text
						.substring(index + mySuffixWords[n].length()).trim();

				if (text1.equals(""))
					text1 = "one";

				if (text2.equals(""))
					text2 = "zero";

				Long t1 = parseBelow1000(text1);
				Long t2 = parse(text2);
				if(t1 == null || t2 == null) return null;
				totalValue =  t1 * mySuffixValues[n]
						+ t2;
				processed = true;
				break;

			}
		}

		if (processed)
			return totalValue;
		else
			return parseBelow1000(text);
	}
	
	catch(Exception e) {
		e.printStackTrace();
		return null;
	}
		
	}
}

class SpellException extends Exception {

	private static final long serialVersionUID = 1L;

	public SpellException(String message) {
		super(message);
	}
}
