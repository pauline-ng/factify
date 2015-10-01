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
import java.util.SortedSet;

import at.knowcenter.code.api.pdf.Block;
import at.knowcenter.code.api.pdf.TextFragment;

/**
 * 
 * 
 * @author Roman Kern <rkern@tugraz.at>
 */
public class AbstractSplitter {
    /**
     * @param collections
     * @param defaultDebug 
     * @return
     */
    protected double getCutpoint(SortedSet<Block> collections, boolean defaultDebug) {
        boolean debug = defaultDebug;
//        if (fragments.get(0).seq == 476) {
//            debug = true;
//        }
        
        double min = Double.NaN, max = Double.NaN, mean = 0, meanDistance = 0;
        TextFragment maxFrag = null, minFrag = null;
        {
            double lastPos = Double.NaN;
            double distance = 0;
            for (Block word : collections) {
                List<TextFragment> frags = word.getFragments();
                boolean hasLastPos = !Double.isNaN(lastPos);
                if (hasLastPos) {
                	TextFragment first = frags.get(0);
                    double d = getDelta(frags, lastPos);
                    if (!Double.isNaN(min)) {
                        if (d < min) { min = d; minFrag = first; }
                        else if (d > max) { max = d; maxFrag = first; }
                    } else {
                        min = max = d; minFrag = maxFrag = first;
                    }
                    mean += d;
                    if (debug) { System.out.println(String.format("%.3f - %d - %s", d, frags.get(0).getSequence(), word.toString().replace('\n', ' '))); }
                } else {
                    if (debug) { System.out.println(String.format("%.3f - %d - %s", 0.0, frags.get(0).getSequence(), word.toString().replace('\n', ' '))); }
                }
                double old = lastPos;
                lastPos = getEnd(frags);
                if (hasLastPos) {
                    distance += (lastPos-old);
                }
            }
            mean /= collections.size()-1;
            meanDistance = distance / (collections.size()-2); 
        }
        if (max <= 0) { return Double.NaN; }
        if (max < min+Math.abs(mean)) { return Double.NaN; }

        double c1 = min, c2 = max, s1 = c1, s2 = c2; 
        double maxc2 = min;
        double minc2 = max;
        int n1 = 1, n2 = 1;
        //double md1 = 0;
        {
            double lastPos = Double.NaN;
            for (Block collection : collections) {
                List<TextFragment> frags = collection.getFragments();
                if (!Double.isNaN(lastPos)) {
                	TextFragment first = frags.get(0);
                    if (first != minFrag && first != maxFrag) {
                        double d = getDelta(frags, lastPos);
                        double d1 = Math.abs(c1 - d), d2 = Math.abs(c2 - d);
                        if (debug) { System.out.println(String.format("%.3f - %.3f, %.3f - %.3f (%d), %.3f (%d)", 
                                d, d1, d2, c1, n1, c2, n2)); }
                        if (d1 <= d2) {
                            s1 += d; n1++; c1 = s1 / n1;
                            // md1 += Math.abs(d1);
                            if (Double.isNaN(maxc2) || d > maxc2) { maxc2 = d; }
                        } else {
                            s2 += d; n2++; c2 = s2 / n2;
                            if (Double.isNaN(minc2) || d < minc2) { minc2 = d; }
                        }
                    }
                }
//                XmlPdfTextFragment last = frags.get(frags.size()-1);
                lastPos = getEnd(frags);
            }
        }

        //md1 = md1 / n1;
        double cutpoint = Double.NaN; 
//        boolean old = n1 >= n2/**2*/ && c2 > (c1+md1*4) && c2 > (c1+mean/2) && n2 <= 2 && c2 > 0;
        boolean isCutpoint = n1 >= n2 && c2 > (c1+meanDistance*0.01) && n2 <= 2 && c2 > 0;
        if (isCutpoint) {
            cutpoint = minc2;
        } 
        
        return cutpoint;  
    }
    
    protected double getCutpoint(List<TextFragment> fragments) {
        boolean debug = false;
        
        double min = Double.NaN, max = Double.NaN, mean = 0;
        TextFragment maxFrag = null, minFrag = null;
        {
            double lastPos = Double.NaN, lastX = Double.NaN;;
            for (TextFragment f : fragments) {
                if (!Double.isNaN(lastPos)) {
                    double d = f.getX()-lastPos;
                    if (!Double.isNaN(min)) {
                        if (d < min) { min = d; minFrag = f; }
                        else if (d > max) { max = d; maxFrag = f; }
                    } else {
                        min = max = d; minFrag = maxFrag = f;
                    }
                    mean += f.getX()-lastX;
                    if (debug) { System.out.println(String.format("%.3f - %s - %d", d, f.getText(), f.getSequence())); }
                } else {
                    if (debug) { System.out.println(String.format("%.3f - %s - %d", 0.0, f.getText(), f.getSequence())); }
                }
                lastPos = f.getX()+f.getWidth();
                lastX = f.getX();
            }
            mean /= fragments.size()-1;
        }
        if (max <= 0) { return Double.NaN; }

        double c1 = min, c2 = max, s1 = c1, s2 = c2;
        double minc2 = Double.NaN;
        int n1 = 1, n2 = 1;
        {
            double lastPos = Double.NaN;
            for (TextFragment f : fragments) {
                if (!Double.isNaN(lastPos)) {
                    if (f != minFrag && f != maxFrag) {
                        double d = f.getX()-lastPos;
                        double d1 = Math.abs(c1 - d), d2 = Math.abs(c2 - d);
                        if (debug) { System.out.println(String.format("%.3f - %.3f, %.3f - %.3f (%d), %.3f (%d)", 
                                d, d1, d2, c1, n1, c2, n2)); }
                        if (d1 <= d2) {
                            s1 += d; n1++; c1 = s1 / n1;
                        } else {
                            s2 += d; n2++; c2 = s2 / n2;
                            if (Double.isNaN(minc2) || d < minc2) { minc2 = d; }
                        }
                    }
                }
                lastPos = f.getX()+f.getWidth();
            }
        }

        double cutpoint = Double.NaN;
        if (n1 > n2*2 && c2 > (mean/4)) {
            cutpoint = minc2;
        }
        
        return cutpoint;
    }

    /**
     * @param current
     * @return
     */
    protected float getEnd(List<TextFragment> current) {
    	TextFragment last = current.get(current.size()-1);
        return last.getX()+last.getWidth();
    }

    /**
     * @param frags
     * @param lastPos
     * @return
     */
    protected double getDelta(List<TextFragment> frags, double lastPos) {
        return frags.get(0).getX()-lastPos;
    }
    

}
