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

import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.procedure.TObjectDoubleProcedure;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import at.knowcenter.code.api.pdf.Block;
import at.knowcenter.code.api.pdf.BoundingBox;
import at.knowcenter.code.api.pdf.Font;
import at.knowcenter.code.api.pdf.Page;
import at.knowcenter.code.api.pdf.TextFragment;

/**
 * 
 * 
 * @author Roman Kern <rkern@tugraz.at>
 */
public class BlockMerger extends VerticalFragmentMerger {
    /** double MAX_REL_FONT_SIZE_DIFF */
    private static final double MAX_REL_FONT_SIZE_DIFF = 0.12;
    /** double MAX_GLYPH_REL_DIFFERENCE */
    private static final double MAX_GLYPH_REL_DIFFERENCE = 0.17;
    private static final double REL_MEAN_HEIGHT_LINE_GAP = 0.2;
    private static final int EXPECTED_SCALE_LINE_GAP = 2;
    
    private final double lineSpacing;

    /**
     * Creates a new instance of this class.
     * @param lineFragments
     * @param lineSpacing 
     * @param idToFont
     */
    public BlockMerger(Page page, Block lineFragments,
                       double lineSpacing, Map<Integer, Font> idToFont) {
        super(page, lineFragments, idToFont, 0.7, lineSpacing * 0.7);
        this.lineSpacing = lineSpacing;
    }
    
    @Override
    protected boolean useExhaustiveSearch() {
        return true;
    }
    
    @Override
    protected Comparator<Block> getResultCollectionOrder() {
//        return Block.ID_FRAGMENTS;
        return Block.VERTICAL_FRAGMENTS;
    }
    
    @Override
    protected void resetCycleStats(Cluster a) {
        double[] minDistanceY = new double[2];
        minDistanceY[0] = minDistanceY[1] = Double.NaN;
        a.cycleStats = minDistanceY;
    }
    
    @Override
    protected void updateCycleStats(Cluster a, Cluster b) {
        double overlapX = a.getOverlapX(b);
        if (overlapX > 0) {
            double distanceY = a.getDistanceY(b);
            {
                double[] minDistanceY = (double[])a.cycleStats;
                if (Double.isNaN(minDistanceY[0]) || distanceY < minDistanceY[0]) {
                    minDistanceY[0] = distanceY;
                }
            }
            {
                double[] minDistanceY = (double[])b.cycleStats;
                if (Double.isNaN(minDistanceY[0]) || distanceY < minDistanceY[0]) {
                    minDistanceY[1] = distanceY;
                }
            }
        }
    }
    
//    static int counter = 0;

    @Override
    protected double getDistance(Cluster a, Cluster b) {
//        if (a.fragments.get(0).getSequence() == 469 && b.fragments.get(0).getSequence() == 489) {
//            System.out.println(b.fragments.get(0).getSequence()+" -> "+getOverlapX(a, b));
//        }
        double overlapX = a.getOverlapX(b);
        if (overlapX <= 0) {
            return Double.NaN;
        }
        double distanceY = a.getDistanceY(b);
        
        
        {
            double[] minDistanceY = (double[])a.cycleStats;
            if (minDistanceY != null && distanceY*0.98 > minDistanceY[0] && distanceY > mergeThresholdY/2) {
                return Double.NaN;
            }
        }
        {
            double[] minDistanceY = (double[])b.cycleStats;
            if (minDistanceY != null && distanceY*0.98 > minDistanceY[0] && distanceY > mergeThresholdY/2) {
                return Double.NaN;
            }
        }
        
        double meanHeight = Math.min(a.meanHeight, b.meanHeight);
        double relHeightDiff = Math.abs(a.meanHeight-b.meanHeight) / meanHeight;
        double meanFontSize = Math.min(a.meanFontSize, b.meanFontSize);
        double relFontSizeDiff = Math.abs(a.meanFontSize-b.meanFontSize) / meanFontSize;
        boolean containsFont = containsFontsWhole(a, b);
        if (!containsFont) {
            return Double.NaN;
        }
//        containsFont = containsFontAdjacentLines(a, b);
//        if (!containsFont) {
//            return Double.NaN;
//        }
        if (relFontSizeDiff >= MAX_REL_FONT_SIZE_DIFF) {
            return Double.NaN;
        }
        if (a.isUpperCase != b.isUpperCase && a.letterCount > 4 && b.letterCount > 4) {
            return Double.NaN;
        }
        if (!a.fontIds.contains(b.majorityFontId) && !b.fontIds.contains(a.majorityFontId)) {
            return Double.NaN;
        }
        if (b.majorityFontId != a.majorityFontId) {
            return Double.NaN;
        }
        double lineGap = getLineGap(a, b);
        if (a.collections.size() >= 3) {
            Stats agaps = getMeanLineGaps(a);
            if (agaps.getN() >= 2) {
                double maxExpected = agaps.getMaxExpected(EXPECTED_SCALE_LINE_GAP)+REL_MEAN_HEIGHT_LINE_GAP*meanHeight;
                if (lineGap > maxExpected) {
                    return Double.NaN;
                }
            }
        }
        if (b.collections.size() >= 3) {
            Stats bgaps = getMeanLineGaps(b);
            if (bgaps.getN() >= 2) {
                double maxExpected = bgaps.getMaxExpected(EXPECTED_SCALE_LINE_GAP)+REL_MEAN_HEIGHT_LINE_GAP*meanHeight;
                if (lineGap > maxExpected) {
                    return Double.NaN;
                }
            }
        }
//        if (a.collections.size() >= 3) {
//            if (!isWithinX(a, b.collections.first(), true)) {
//                return Double.NaN;
//            }
////            if (!isWithinX(a, b, false)) {
////                return Double.NaN;
////            }
//        }
//        if (b.collections.size() >= 3) {
////            if (!isWithinX(b, a, true)) {
////                return Double.NaN;
////            }
//            if (!isWithinX(b, a.collections.last(), false)) {
//                return Double.NaN;
//            }
//        }
        
//        boolean isUpperCaseA = isUpperCase(a), isUpperCaseB = isUpperCase(b);
//        if ((isUpperCaseA && !isUpperCaseB) || (isUpperCaseB && !isUpperCaseA)) {
//            return Double.NaN;
//        }
        
//        if (relHeightDiff > 0.5) {
//            return Double.NaN;
//        }
//        if (relWidthDiff > 0.5) {
//            return Double.NaN;
//        }
        
//        Stats m = getGlyphDimensionDifference(a, b, true);
//        if (m.getN() > 3 && Math.abs(m.getMean()) > MAX_GLYPH_REL_DIFFERENCE) {
//            return Double.NaN;
//        }
//        m = getGlyphDimensionDifference(a, b, false);
//        if (m.getN() > 3 && Math.abs(m.getMean()) > MAX_GLYPH_REL_DIFFERENCE) {
//            return Double.NaN;
//        }
        
        double dx = overlapX;
        dx = 1 - (dx-mergeThresholdX);
        double dy = distanceY / mergeThresholdY;
        if (dx < 1 && dy < 1) {
            double distance = dy;
            distance += 0.2*(relHeightDiff + relFontSizeDiff);
            distance += 1 / getOverlapX(a, b);
            return distance;
        }
        return Double.NaN;
    }

    private double getOverlapX(Cluster a, Cluster b) {
        Block last = a.collections.last();
        Block first = b.collections.first();
        
        BoundingBox bb1 = last.getBoundingBox();
        BoundingBox bb2 = first.getBoundingBox();
        
        double bx = Math.max(bb1.minx, bb2.minx), cx = Math.min(bb1.maxx, bb2.maxx),
               dx = Math.min(bb1.minx, bb2.minx), ex = Math.max(bb1.maxx, bb2.maxx);
        double i = cx-bx;
        return i >= 0 ? i / (ex-dx) : 0;
    }

//    private boolean isWithinX(Cluster a, Block line, boolean isMinX) {
//        boolean result = true;
//        Stats aminx = getMeanX(a, isMinX);
//        BoundingBox boundingBox = line.getBoundingBox();
//        double x = isMinX ? boundingBox.minx : boundingBox.maxx;
//        if (!withinX(aminx, x)) {
//            result = false;
//        }
//        return result;
//    }
    
//    private boolean withinX(Stats stats, double x) {
//        double standardDeviation = stats.getStandardDeviation();
//        double mean = stats.getMean();
//        double minx = mean - 2*standardDeviation - 0.1;
//        if (x < minx) { 
//            return false;
//        }
//        double maxx = mean + 2*standardDeviation + 0.1;
//        if (x > maxx) { 
//            return false;
//        }
//        return true;
//    }

//    private Stats getMeanX(Cluster c, boolean isMinX) {
//        Stats m = new Stats();
//        
//        int size = c.collections.size();
//        int i = 0;
//        for (Block line : c.collections) {
//            if (isMinX) { if (i == 0) { continue; } }
//            else { if (i == size-1) { continue; } } 
//            BoundingBox boundingBox = line.getBoundingBox();
//            double x = isMinX ? boundingBox.minx : boundingBox.maxx;
//            m.increment(x);
//            i++;
//        }
//        return m;
//    }

//    private boolean isUpperCase(Cluster a) {
//        boolean result = false;
//        
//        int charCounter = 0;
//        for (TextFragment f : a.fragments) {
//            String text = f.getText();
//            for (int i = 0; i < text.length(); i++) {
//                if (!Character.isUpperCase(text.charAt(i))) {
//                    return false;
//                }
//                charCounter++;
//            }
//            if (charCounter >= 5) {
//                result = true;
//            }
//        }
//        return result;
//    }

    private Stats getMeanLineGaps(Cluster c) {
        Stats m = new Stats();
        
        double end = Double.NaN;
        for (Block line : c.collections) {
            List<TextFragment> fragments = line.getFragments();
            if (!Double.isNaN(end)) {
                double delta = BlockSplitter.doGetDelta(fragments, end);
                m.increment(delta);
            }
            end = BlockSplitter.doGetEnd(fragments);
        }
        return m;
    }
    
    private double getLineGap(Cluster a, Cluster b) {
        Block lastLine = a.collections.last();
        Block firstLine = b.collections.first();

        return BlockSplitter.doGetDelta(firstLine.getFragments(), BlockSplitter.doGetEnd(lastLine.getFragments()));
    }
    
//    private boolean containsFontAdjacentLines(Cluster a, Cluster b) {
//        boolean containsFont;
//        Block lastLine = a.collections.last();
//        Block firstLine = b.collections.first();
//        TIntHashSet lastLineFontIds = new TIntHashSet();
//        TIntHashSet firstLineFontIds = new TIntHashSet();
//        for (TextFragment f : lastLine.getFragments()) { lastLineFontIds.add(f.getFontId()); }
//        for (TextFragment f : firstLine.getFragments()) { firstLineFontIds.add(f.getFontId()); }
//        containsFont = false;
//        for (int id : lastLineFontIds.toArray()) {
//            if (firstLineFontIds.contains(id)) { 
//                containsFont = true;
//                break;
//            }
//        }
//        return containsFont;
//    }

    private boolean containsFontsWhole(Cluster a, Cluster b) {
        boolean containsFont = false;
        for (int id : a.fontIds.keys()) {
            if (b.fontIds.containsKey(id)) { 
                containsFont = true;
                break;
            }
        }
        return containsFont;
    }

//    private Stats getGlyphDimensionDifference(Cluster a, Cluster b, boolean isWidth) {
//        final TObjectDoubleHashMap<String> aw = getFragmentDimension(a.fragments, isWidth);
//        final TObjectDoubleHashMap<String> bw = getFragmentDimension(b.fragments, isWidth);
//        final Stats m = new Stats(); 
//        aw.forEachEntry(new TObjectDoubleProcedure<String>() {
//            @Override
//            public boolean execute(String k, double v1) {
//                double v2 = bw.get(k);
//                if (v2 > 0) {
//                    m.increment((v1-v2)/Math.max(v1, v2));
//                }
//                return true;
//            }
//        });
//        return m;
//    }

//    private TObjectDoubleHashMap<String> getFragmentDimension(List<TextFragment> fragments, boolean isWidth) {
//        Map<String, Stats> map = new HashMap<String, Stats>();
//        for (TextFragment f : fragments) {
//            float dim = isWidth ? f.getWidth() : f.getHeight();
//            Stats mean = map.get(f.getText());
//            if (mean == null) { map.put(f.getText(), mean = new Stats()); }
//            mean.increment(dim);
//        }
//        TObjectDoubleHashMap<String> result = new TObjectDoubleHashMap<String>(1024, 2, -1);
//        for (Entry<String, Stats> e : map.entrySet()) {
//            result.put(e.getKey(), e.getValue().getMean());
//        }
//        return result;
//    }
}
