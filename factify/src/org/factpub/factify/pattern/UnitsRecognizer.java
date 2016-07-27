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
package org.factpub.factify.pattern;

import de.lodde.jnumwu.formula.Expression;

/**
 * Check if a string is a unit/measurement
 * 
 * <pre>
 * Library from <a href="http://sourceforge.net/p/jnumwu/wiki/markdown_syntax/">SourceForge</a>
 *</pre>
 */
public class UnitsRecognizer {
	public static void main(String[] args) {
		UnitsRecognizer unit = new UnitsRecognizer();
		unit.recognizeUnits("");
	}
	
	/**
	 * 
	 * @param s: a word (without whitespace)
	 */
	public void recognizeUnits(String s) {
		
		{
			Expression parseExpression = Expression.parseExpression("g/L");
			System.out.println(parseExpression.getPrecedence() + "\t" + parseExpression.getClass());
		}
		{
			Expression parseExpression = Expression.parseExpression("all");
			System.out.println(parseExpression.getPrecedence() + "\t" + parseExpression.getClass());
		}
		
	}
	
	/**
	 * Check if a word is a unit
	 * 
	 * <pre>
	 * If a token is following a number and is recognized as {@link de.lodde.jnumwu.formula.Constant} or {@link de.lodde.jnumwu.formula.Div}, then it is a unit.
	 * The conditions are to filter out false positive such as 
	 * 1. "all" is a variable expression that has units "l"
	 * 2. "w/v" is a Div expression that has units
	 * </pre>
	 * <pre>
	 * Remaining problem:
	 * 3. "Drugs were prepared as suspensions in 1% methylcellulose (w/v in distilled water)" : unit w/v is not with numbers
	 *    "Additional significant SNPs were identified in GABRA4 as well" : "as" is identified as unit
	 * </pre>
	 * @param token
	 * @param followingNumber
	 * @return
	 */
	public boolean isUnit(String token, boolean followingNumber) {
		
		if(!token.equals("(") && !token.equals(")")) 
			try{
				Expression parseExpression = Expression.parseExpression(token);
				String class_str = parseExpression.getClass().toString();
				if(parseExpression.hasUnits() && 
						(class_str.equals("class de.lodde.jnumwu.formula.Constant") || class_str.equals("de.lodde.jnumwu.formula.Div"))) {
					if(followingNumber) return true;
				}
			}catch(StackOverflowError | NumberFormatException e) {
				return false;
			}
		return false;
	}
}
