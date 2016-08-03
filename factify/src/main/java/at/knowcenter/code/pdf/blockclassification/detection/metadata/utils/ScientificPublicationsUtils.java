/* Copyright (C) 2010 
"Kompetenzzentrum fuer wissensbasierte Anwendungen Forschungs- und EntwicklungsgmbH" 
(Know-Center), Graz, Austria, office@know-center.at.

Licensees holding valid Know-Center Commercial licenses may use this file in
accordance with the Know-Center Commercial License Agreement provided with 
the Software or, alternatively, in accordance with the terms contained in
a written agreement between Licensees and Know-Center.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package at.knowcenter.code.pdf.blockclassification.detection.metadata.utils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.factpub.factify.utility.Span;

//import opennlp.tools.util.Span;

/**
 * @author rkern@know-center.at
 */
public class ScientificPublicationsUtils {
    public static final Pattern ISSN_PATTERN = Pattern.compile("\\d\\d\\d\\d-\\d\\d\\d\\d");
    public static final Pattern ISBN_13_PATTERN = Pattern.compile("(?=.{17}$)97(?:8|9)([ -])\\d{1,5}\\1\\d{1,7}\\1\\d{1,6}\\1\\d$");
    public static final Pattern ISBN_10_PATTERN = Pattern.compile("(?=.{13}$)\\d{1,5}([- ])\\d{1,7}\\1\\d{1,6}\\1(\\d|X)$");
    /**
     * Based on the <a href="http://www.doi.org/doi_handbook/2_Numbering.html">DOI Number specification</a> document
     * and is different from <a href="http://stackoverflow.com/questions/27910/finding-a-doi-in-a-document-or-page">this attempt</a>, which is not correct
     * and would even fail <a href="http://www.doi.org/doi_handbook/2_Numbering.html#htmlencoding">the example</a> given in the <a href="http://www.doi.org/doi_handbook/">DOI Handbook</a>
     */
    public static final Pattern DOI_PATTERN = Pattern.compile("(10\\.[0-9]+(?:\\.[0-9]+)*/[\\p{Graph}]+)");

    
    /**
     * Creates a new instance of this class.
     */
    public ScientificPublicationsUtils() {
    }

    public boolean matchPattern(String text, Pattern pattern) {
        return (pattern.matcher(text).matches());
    }

    public boolean matchPattern(TextSpan token, Pattern pattern) {
    	return matchPattern(token.getText(), pattern);
    }

    public Set<String> matchPattern(List<TextSpan> tokens, Pattern pattern) {
        final Set<String> result = new LinkedHashSet<String>();
        
        for (TextSpan textSpan : tokens) {
            if (matchPattern(textSpan, pattern)) {
                result.add(textSpan.getText());
            }
        }
        return result;
    }
    
    public boolean findPattern(String text, Pattern pattern) {
        return pattern.matcher(text).find();
    }

    public boolean findPattern(TextSpan token, Pattern pattern) {
    	return findPattern(token.getText(), pattern);
    }

    public Set<String> findPattern(List<TextSpan> tokens, Pattern pattern) {
        final Set<String> result = new LinkedHashSet<String>();
        
        for (TextSpan textSpan : tokens) {
            if (findPattern(textSpan, pattern)) {
                result.add(textSpan.getText());
            }
        }
        return result;
    }

    public String extractPattern(String text, Pattern pattern) {
        Matcher m = pattern.matcher(text);
        if (m.find()) {
        	return m.group(0);
        }
        return null;
    }

    public TextSpan extractPattern(TextSpan token, Pattern pattern) {
    	String s = extractPattern(token.getText(), pattern);
    	if (s != null) {
    		return new TextSpan(s);
    	}
    	return null;
    }
    
    public ArrayList<Span> findAllUsingPattern(TextSpan token, Pattern pattern) {
        final ArrayList<Span> result = new ArrayList<Span>();
        
        Matcher m = pattern.matcher(token.getText());
        if (m.find()) {
            result.add(new Span(m.start(), m.end()));
        }
        return result;
    }

    /**
     * Returns the set of ISSN found as tokens. 
     * @param tokens the tokens
     * @return empty set, if none are found
     */
    public Set<String> matchIssn(List<TextSpan> tokens) {
        return matchPattern(tokens, ISSN_PATTERN);
    }
    
    /**
     * Returns the set of ISBN10 found as tokens. 
     * @param tokens the tokens
     * @return empty set, if none are found
     */
    public Set<String> matchIsbn10(List<TextSpan> tokens) {
        return matchPattern(tokens, ISBN_10_PATTERN);
    }
    
    /**
     * Returns the set of ISBN13 found as tokens. 
     * @param tokens the tokens
     * @return empty set, if none are found
     */
    public Set<String> matchIsbn13(List<TextSpan> tokens) {
        return matchPattern(tokens, ISBN_13_PATTERN);
    }
    
    
    /**
     * Returns the set of DOI found as tokens. 
     * @param tokens the tokens
     * @return empty set, if none are found
     */
    public Set<String> matchDoi(List<TextSpan> tokens) {
        return matchPattern(tokens, DOI_PATTERN);
    }
    
    public static String cleanMetadata(String text) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length()-1; i++) {
            char c = text.charAt(i);
            if (!Character.isWhitespace(c) && !Character.isDigit(c) && Character.isLetter(c)) {
                builder.append(text.substring(i));
                break;
            }
        }
        for (int i = builder.length()-1; i >= 0; i--) {
            char c = builder.charAt(i);
            if (Character.isWhitespace(c) || Character.isDigit(c) || !Character.isLetter(c)) {
                builder.setLength(i);
            } else {
                break;
            }
        }
        return builder.toString();
    }
    
    public static String cleanMetadataKeepDigits(String text) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length()-1; i++) {
            char c = text.charAt(i);
            if (!Character.isWhitespace(c) && (Character.isDigit(c) || Character.isLetter(c))) {
                builder.append(text.substring(i));
                break;
            }
        }
        for (int i = builder.length()-1; i >= 0; i--) {
            char c = builder.charAt(i);
            if (Character.isWhitespace(c) || (!Character.isDigit(c) && !Character.isLetter(c))) {
                builder.setLength(i);
            } else {
                break;
            }
        }
        return builder.toString();
    }
    
    public static void main(String[] args) {
        List<TextSpan> list = new ArrayList<TextSpan>();
        list.add(new TextSpan("foo"));
        list.add(new TextSpan("bar"));
        list.add(new TextSpan("1468-6708"));
        list.add(new TextSpan("1468-6708"));
        list.add(new TextSpan("978-1-4028-9462-6"));
        list.add(new TextSpan("99921-58-10-7"));
        list.add(new TextSpan("10.1088/0031-9155/45/11/404"));
        list.add(new TextSpan("foo"));
        System.out.println("ISSN: "+new ScientificPublicationsUtils().matchIssn(list));
        System.out.println("ISBN10: "+ new ScientificPublicationsUtils().matchIsbn10(list));
        System.out.println("ISBN13: "+ new ScientificPublicationsUtils().matchIsbn13(list));
        System.out.println("DOI: "+ new ScientificPublicationsUtils().matchDoi(list));
    }
}
