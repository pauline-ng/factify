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
package at.knowcenter.code.pdf.utils;

import java.util.regex.Pattern;

/**
 * 
 * 
 * @author rkern@know-center.at
 */
public class StringHelper {
    public static final Pattern EMAIL_PATTERN = Pattern.compile("[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})");
    public static final Pattern EMAIL_PATTERN_SIMPLE = Pattern.compile("@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})");
    public static final Pattern URL_PATTERN = Pattern.compile("(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
    
    /**
     * @param plainText
     * @return
     */
    public static boolean containsDigits(String plainText) {
        if (plainText.length() == 0) { return false; }
        
        boolean result = false;
        for (int i = 0; i < plainText.length(); i++) {
            if (Character.isDigit(plainText.charAt(i))) {
                result = true;
                break;
            }
        }
        return result;
    }
    
    /**
     * @param plainText
     * @return
     */
    public static boolean isNumber(String plainText) {
        if (plainText.length() == 0) { return false; }
        
        boolean result = true;
        for (int i = 0; i < plainText.length(); i++) {
            if (!Character.isDigit(plainText.charAt(i))) {
                result = false;
                break;
            }
        }
        return result;
    }

    /**
     * @param plainText
     * @return false, if the text starts with a lower-case letter
     */
    public static boolean isCapitalized(String plainText) {
        return plainText.length() > 0 ? Character.isUpperCase(plainText.charAt(0)) : false;
    }

    /**
     * @param plainText
     * @return false, if the text starts with a lower-case letter
     */
    public static boolean isFullyUpperCase(String plainText) {
        if (plainText.length() == 0) { return false; }
        boolean result = true;
        for (int i = 0; i < plainText.length(); i++) {
            if (!Character.isUpperCase(plainText.charAt(i))) {
                result = false;
                break;
            }
        }
        return result;
    }

}
