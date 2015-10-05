package pattern;

import de.lodde.jnumwu.formula.Expression;

/**
 * Library from http://sourceforge.net/p/jnumwu/wiki/markdown_syntax/
 * @author huangxc
 *
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
	 * if a token is following a number and is recognized as Constant or Div then it is a unit
	 * --the conditions are to filter out false positive such as 
	 * --1. "all" is a variable expression that has units "l"
	 * --2. "w/v" is a Div expression that has units
	 * --Remaining problem:
	 * --3. "Drugs were prepared as suspensions in 1% methylcellulose (w/v in distilled water)" : unit w/v is not with numbers
	 * --"Additional significant SNPs were identified in GABRA4 as well" : "as" is identified as unit
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
//					System.out.println(parseExpression.toString());
					if(followingNumber) return true;
				}
			}catch(StackOverflowError | NumberFormatException e) {
//				System.out.println("ERROR");
				return false;
			}
		return false;
	}
}
