package pattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import utility.Debug;
import utility.Debug.DEBUG_CONFIG;
/**
 * This is from https://ph.answers.yahoo.com/question/index?qid=20100820042716AAcemIX (http://pastebin.com/CbaRz14A;http://pastebin.com/cGSFUSBp;http://pastebin.com/fZLypaBn)
 * 
 * Convert text to non-negative integers (long): cannot deal with decimal or negatives
 * --cannot deal with "half an hour" or "a hundred"
 * 
 * @author huangxc
 *
 */
public class TextToNum {

	
	  public static void main(String[] args) throws SpellException {
		  TextToNum textToNum = new TextToNum();
		  Debug.println(textToNum.parse("test"),DEBUG_CONFIG.debug_temp);
//		  
//          long value = 0L;
//
//          for (long i = 0; i <= 10L; i++) {
//                  value = value * 10 + i;
//                  readOutAgain(value);
//          }
//
//          readOut(100001);
//
//          value = 0L;
//
//          for (long i = 10; i >= 0L; i--) {
//                  value = value * 10 + i;
//                  readOutAgain(value);
//          }
//
//          Scanner in = new Scanner(System.in);
//          Debug.println("Value : "
//                  + WithSeparator(parse("fifty five")));
//          System.exit(0);
//          for (;;) {
//                  try {
//                          Debug.print("Number in words : ");
//                          String numberWordsText = in.nextLine();
//
//                          if (numberWordsText.equals("."))
//                                  break;
//                          else if (numberWordsText.equals(""))
//                                  continue;
//
//                          Debug.println("Value : "
//                                          + WithSeparator(parse(numberWordsText)));
//
//                  } catch (Exception e) {
//                          System.err.println(e.getMessage());
//
//                  }
//          }
//
  }

  private  void readOut(long value) {
          String text = "Error";
          try {
                  text = spell(value);

          } catch (SpellException e) {
                  text = e.getMessage();

          } catch (Exception e) {
                  text = e.getMessage();
          }

          Debug.println(WithSeparator(value) + " : " + text,DEBUG_CONFIG.debug_temp);
  }

  private  void readOutAgain(long value) throws SpellException {
          String text = "Error";
          long readValue = 0;
          try {
                  text = spell(value);
                  readValue = parse(text);

          } catch (SpellException e) {
                  text = e.getMessage();

          } catch (Exception e) {
                  text = e.getMessage();

          }

          Debug.println(WithSeparator(value) + " : " + text
                          + "  (" + readValue + ")",DEBUG_CONFIG.debug_temp);
  }
  private  String spell(long number) throws SpellException {
		String text;
		if (number < 0L) {
			text = "Minus " + spell(-number, 1);
		} else {
			text = spell(number, 1);
		}

		int index_amp, index_perc;

		index_amp = text.lastIndexOf("$");
		index_perc = text.lastIndexOf("%");

		if (index_amp >= 0) {
			if (index_perc < 0 || index_amp > index_perc) {

				String text1 = text.substring(0, index_amp);
				String text2 = text.substring(index_amp + 1, text.length());

				text = text1 + " and " + text2;
			}
		}

		text = text.replaceAll("\\$", ", ");
		text = text.replaceAll("%", " and ");

		return text;
	}

	// WithSeparator () function:
	// It converts a number to string using 1000's separator.
	// It uses a simple recursive algorithm.
	private  String WithSeparator(long number) {
		if (number < 0) {
			return "-" + WithSeparator(-number);
		}

		if (number / 1000L > 0) {
			return WithSeparator(number / 1000L) + ","
					+ String.format("%1$03d", number % 1000L);
		} else {
			return String.format("%1$d", number);
		}
	}

	private  String mySuffixText[] = {
		"", // Dummy! no level 0
		"", // Nothing for level 1
		" Thousand", " Million", " Billion", " Trillion",
		" (Thousand Trillion)", " (Million Trillion)",
		" (Billion Trillion)", };

	private  String myTeenText[] = { "Zero", "One", "Two", "Three",
		"Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven",
		"Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
		"Seventeen", "Eighteen", "Ninteen", };

	// used appropriately for under-cent values:
	private  String myCentText[] = { "Twenty", "Thirty", "Forty",
		"Fifty", "Sixty", "Seventy", "Eighty", "Ninety" };

	// used appropriately for under-mil values.
	private  String myMilText[] = { "One Hundred", "Two Hundred",
		"Three Hundred", "Four Hundred", "Five Hundred", "Six Hundred",
		"Seven Hundred", "Eight Hundred", "Nine Hundred" };

	private  String SpellBelow1000(long number) throws SpellException {
		if (number < 0 || number >= 1000)
			throw new SpellException("Expecting a number between 0 and 999: "
					+ number);

		if (number < 20L) {
			return myTeenText[(int) number];
		} else if (number < 100L) {
			int div = (int) number / 10;
			int rem = (int) number % 10;

			if (rem == 0) {
				return myCentText[div - 2];
			} else {
				return myCentText[div - 2] + " " + SpellBelow1000(rem);
			}
		} else {
			int div = (int) number / 100;
			int rem = (int) number % 100;

			if (rem == 0) {
				return myMilText[div - 1];
			} else {
				return myMilText[div - 1] + "%" + SpellBelow1000(rem);
			}
		}
	}

	private  String spell(long number, int level) throws SpellException {
		long div = number / 1000L;
		long rem = number % 1000L;

		if (div == 0) {
			return SpellBelow1000(rem) + mySuffixText[level];
		} else {
			if (rem == 0) {
				return spell(div, level + 1);
			} else {
				return spell(div, level + 1) + "$" + SpellBelow1000(rem)
						+ mySuffixText[level];
			}
		}
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

//}

class SpellException extends Exception {

	private static final long serialVersionUID = 1L;

	public SpellException(String message) {
		super(message);
	}
}