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
import java.util.TreeSet;

import at.knowcenter.code.api.pdf.Block;
import at.knowcenter.code.api.pdf.Page;
import at.knowcenter.code.api.pdf.TextFragment;

/**
 * 
 * 
 * @author Roman Kern <rkern@tugraz.at>
 */
public class LineSplitter extends AbstractSplitter {

    private final Block lines;
    private final Page page;

    /**
     * Creates a new instance of this class.
     * @param merge
     */
    public LineSplitter(Page page, Block lineCollection) {
        this.lines = lineCollection;
        this.page = page;
    }

    /**
     * @return
     */
    public Block split() {
        SortedSet<Block> result = new TreeSet<Block>(Block.VERTICAL_FRAGMENTS);
        boolean debug = true;

        for (Block line : lines.getSubBlocks()) {
            SortedSet<Block> words = line.getSubBlocks();
//            if (line.getFragments().get(0).seq == 1446) {
//                System.out.println();
//            }
            
            if (words.size() < 16) {
                result.add(new Block(page, words));
            } else {
//                if (words.get(0).getFragments().get(0).seq == 21) {
//                    System.out.println();
//                }
                double maxDistance = getCutpoint(words, false);
                 if (!Double.isNaN(maxDistance)) {
                    double lastPos = Double.NaN;
                    SortedSet<Block> currentLine = new TreeSet<Block>(Block.HORIZONTAL_FRAGMENTS);
                    if (debug) { System.out.print("Splitting line [" + line + "] into: "); };
                    for (Block word : words) {
                        List<TextFragment> frags = word.getFragments();
                        if (!Double.isNaN(lastPos)) {
                        	TextFragment first = frags.get(0);
                            double d = first.getX()-lastPos;
                            if ((!Double.isNaN(maxDistance) && d >= maxDistance) || d < -10) {
                                Block c = new Block(page, currentLine);
                                result.add(c);
                                if (debug) { System.out.print("["+c+"]"); };
                                currentLine = new TreeSet<Block>(Block.HORIZONTAL_FRAGMENTS);
                            }
                        }
                        currentLine.add(word);
                        TextFragment last = frags.get(frags.size()-1);
                        lastPos = last.getX()+last.getWidth();
                    }
                    if (!currentLine.isEmpty()) {
                        Block c = new Block(page, currentLine);
                        result.add(c);
                        if (debug) { System.out.print("["+c+"]"); };
                    }
                    if (debug) { System.out.println(); };
                } else {
                    result.add(new Block(page, words));
                }
            }
        }
        
        return new Block(page, result);
    }

    /*
    private double getCutpoint(List<Block> words, boolean defaultDebug) {
        boolean debug = defaultDebug;
//        if (fragments.get(0).seq == 476) {
//            debug = true;
//        }
        
        double min = Double.NaN, max = Double.NaN, mean = 0;
        XmlPdfTextFragment maxFrag = null, minFrag = null;
        {
            double lastPos = Double.NaN;
            for (Block word : words) {
                List<XmlPdfTextFragment> frags = word.getFragments();
                if (!Double.isNaN(lastPos)) {
                    XmlPdfTextFragment first = frags.get(0);
                    double d = first.x-lastPos;
                    if (!Double.isNaN(min)) {
                        if (d < min) { min = d; minFrag = first; }
                        else if (d > max) { max = d; maxFrag = first; }
                    } else {
                        min = max = d; minFrag = maxFrag = first;
                    }
                    mean += d;
                    if (debug) { System.out.println(String.format("%.3f - %s - %d", d, word, frags.get(0).seq)); }
                } else {
                    if (debug) { System.out.println(String.format("%.3f - %s - %d", 0.0, word, frags.get(0).seq)); }
                }
                XmlPdfTextFragment last = frags.get(frags.size()-1);
                lastPos = last.x+last.width;
            }
            mean /= words.size()-1;
        }
        if (max <= 0) { return Double.NaN; }

        double c1 = min, c2 = max, s1 = c1, s2 = c2;
        double maxc2 = min;
        double minc2 = max;
        int n1 = 1, n2 = 1;
        {
            double lastPos = Double.NaN;
            for (Block word : words) {
                List<XmlPdfTextFragment> frags = word.getFragments();
                if (!Double.isNaN(lastPos)) {
                    XmlPdfTextFragment first = frags.get(0);
                    if (first != minFrag && first != maxFrag) {
                        double d = first.x-lastPos;
                        double d1 = Math.abs(c1 - d), d2 = Math.abs(c2 - d);
                        if (debug) { System.out.println(String.format("%.3f - %.3f, %.3f - %.3f (%d), %.3f (%d)", 
                                d, d1, d2, c1, n1, c2, n2)); }
                        if (d1 <= d2) {
                            s1 += d; n1++; c1 = s1 / n1;
                            if (Double.isNaN(maxc2) || d > maxc2) { maxc2 = d; }
                        } else {
                            s2 += d; n2++; c2 = s2 / n2;
                            if (Double.isNaN(minc2) || d < minc2) { minc2 = d; }
                        }
                    }
                }
                XmlPdfTextFragment last = frags.get(frags.size()-1);
                lastPos = last.x+last.width;
            }
        }

        double cutpoint = Double.NaN; 
        if (n1 > n2*2 && c2 > (c1+mean/2) && n2 <= 2) {
            cutpoint = minc2;
        } 
        
        return cutpoint;  
    }
     */
    
}
