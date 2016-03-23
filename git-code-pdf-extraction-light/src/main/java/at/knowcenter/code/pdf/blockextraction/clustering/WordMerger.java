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

package at.knowcenter.code.pdf.blockextraction.clustering;

import java.util.List;

import at.knowcenter.code.api.pdf.Page;
import at.knowcenter.code.api.pdf.TextFragment;

/**
 * 
 * 
 * @author Roman Kern <rkern@tugraz.at>
 */
public class WordMerger extends HorizontalFragmentMerger {

    /** double addWidthToDistance */
    private static final double addWidthToDistance = 0.1; //0.25;
    private static final boolean useWordSplitting = false;
    
    /**
     * Creates a new instance of this class.
     * @param fragments
     */
    public WordMerger(Page page, List<TextFragment> fragments) {
        super(page, fragments, 1.7, 0.35);
    }
    
    @Override
    protected double getDistance(Cluster a, Cluster b) {
        boolean debug = false;
//        if (a.toString().equals("INTRODUCTION")/* && b.toString().equals("t")*/) {
//            debug = true;
//        }
//      if (a.fragments.get(0).getSequence() == 0 && b.fragments.get(0).getSequence() == 1) {
//          System.out.println("");
//      }
 
        int idDistance = a.getDistanceId(b); 
        if (idDistance > 1) { 
            return Double.NaN; 
        }
        
        TextFragment leftFrag = a.fragments.get(a.fragments.size()-1);
        TextFragment rightFrag = b.fragments.get(0);
        if (rightFrag.isWordBreakHint()) {
            return Double.NaN;
        }
        
//        if (!b.fontIds.contains(a.majorityFontId) && !a.fontIds.contains(b.majorityFontId)) { 
//            return Double.NaN;
//        }
        if ((b.maxY-(b.maxY-b.minY)/3) < a.centroidY) {
            return Double.NaN;
        }
        if ((b.minY+(b.maxY-b.minY)/3) > a.centroidY) {
            return Double.NaN;
        }
//        double d = Math.abs(a.centroidY-b.centroidY) / Math.min(a.maxHeight, b.maxHeight);
//        if (d > 0.2) {
//            // return Double.NaN;
//        }
        
        if (a.fragementCount >= 2 || b.fragementCount >= 2) {
            double maxExpectedDistanceX = a.fragementCount >= b.fragementCount ? 
                    a.meanDistanceX+/*addWidthToDistance*/0.25*a.meanWidth : b.meanDistanceX+0.25*b.meanWidth;
            double dis = b.minX - a.maxX;
            if (dis > maxExpectedDistanceX) {
                return Double.NaN;
            }
        }  

        if (isSeparatorChar(leftFrag) || isSeparatorChar(rightFrag)) {
            return Double.NaN;
        }
        /*
        if (endsWithLetter(leftFrag) && startsWithDigit(rightFrag)) {
            if (b.meanHeight < a.meanHeight*0.9) {
                return Double.NaN;
            }
        }
        */
        
//        if (endsWithDigit(leftFrag) && startsWithLetter(rightFrag)) {
//            if (a.meanHeight < b.meanHeight*0.9) {
//                return Double.NaN;
//            }
//            if (a.getDistanceX(b, false) > mergeThresholdX/4) {
//                return Double.NaN;
//            }
//        }
        
        double dy = a.getDistanceY(b) / mergeThresholdY;
        if (dy < 1) {
            boolean o = a.minId < b.minId;
            Cluster c1 = o ? a : b, c2 = o ? b : a;
            double distanceX = c1.getDistanceX(c2, false);
            double dx = distanceX / mergeThresholdX;
            if (dx < -1) {
                return Double.NaN;
            }
            if (dx < 1 && dy < 1) {
                double distance = distanceX;
                if (debug) {
                    System.out.println();
                }
                return distance;
            }
        }
        return Double.NaN;
    }

//    private boolean endsWithLetter(TextFragment frag) {
//        int length = frag.getText().length();
//        if (length == 0) { return false; }
//        
//        boolean result = false;
//        char c = frag.getText().charAt(length-1);
//        if (Character.isLetter(c)) {
//            result = true;
//        }
//        return result;
//    }
    
    private boolean startsWithDigit(TextFragment frag) {
        boolean isDigit = false;
        char c = frag.getText().charAt(0);
        if (Character.isDigit(c)) {
            isDigit = true;
        }
        return isDigit;
    }
    
//    private boolean endsWithDigit(TextFragment frag) {
//        int length = frag.getText().length();
//        if (length == 0) { return false; }
//        
//        boolean result = false;
//        char c = frag.getText().charAt(length-1);
//        if (Character.isDigit(c)) {
//            result = true;
//        }
//        return result;
//    }
    
    private boolean startsWithLetter(TextFragment frag) {
        boolean isDigit = false;
        char c = frag.getText().charAt(0);
        if (Character.isLetter(c)) {
            isDigit = true;
        }
        return isDigit;
    }
    
//    private int getType(TextFragment frag) {
//        int result = 0;
//        String text = frag.getText();
//        for (int i = 0; i < text.length(); i++) {
//            char c = text.charAt(i);
//            if (Character.isLetter(c)) {
//                if (result == 0 || result == 1) { result = 1; }
//                else { result = -1; }
//            } else if (Character.isDigit(c)) {
//                if (result == 0 || result == 2) { result = 2; }
//                else { result = -1; }
//            } else {
//                if (result == 0 || result == 3) { result = 3; }
//                else { result = -1; }
//            }
//        }
//        return result;
//    }
    
    private boolean isSeparatorChar(TextFragment frag) {
        boolean isSeparatorChar = false;
        if (useWordSplitting) {
            for (int i = 0; i < frag.getText().length(); i++) {
                char c = frag.getText().charAt(i);
                if (c == '*' || c == ',' || c == ';' || (c >= 0x2000 && c <= 0x206F) || c == '(' || c == ')') {
                    isSeparatorChar = true;
                    break;
                }
            }
        }
        return isSeparatorChar;
    }

}
