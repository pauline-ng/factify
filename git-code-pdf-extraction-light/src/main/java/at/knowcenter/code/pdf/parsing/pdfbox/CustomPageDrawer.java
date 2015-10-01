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
package at.knowcenter.code.pdf.parsing.pdfbox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdfviewer.PageDrawer;
import org.apache.pdfbox.pdmodel.common.PDMatrix;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.TextPosition;

import at.knowcenter.code.api.annotations.PdfPosition;
import at.knowcenter.code.api.pdf.BoundingBox;
import at.knowcenter.code.api.pdf.Font;
import at.knowcenter.code.api.pdf.Page;
import at.knowcenter.code.api.pdf.TextFragment;

/**
 * 
 * 
 * @author rkern@know-center.at
 */
public class CustomPageDrawer extends PageDrawer {
    /** byte[] CHARACTER_FOR_HEIGHT */
    private static final byte[] CHARACTER_FOR_HEIGHT = new byte[] { 'H' };
    private final List<TextFragment> fragments;
    private int currentFontId = -1;
//    private boolean currentWordBreakHint;
//    private int currentMissingWidth;
//    private float pageWidth;
//    private float pageHeight;
//    private float currentFontSize;
    private final Map<Integer, Font> idToFont;
    private final Map<Object, Integer> fonts;
    private final Page pdfPage;
    
    /**
     * Creates a new instance of this class.
     * @param pdfPage 
     * @param pageNumber 
     * @param width 
     * @param height 
     * @param rotation 
     * @param fonts 
     * @param idToFont 
     * @throws IOException
     */
    public CustomPageDrawer(Page pdfPage, int pageNumber, float width, float height, int rotation, 
            Map<Integer, Font> idToFont, Map<Object, Integer> fonts) throws IOException {
        super();
        this.pdfPage = pdfPage;
        
//        this.pageHeight = height;
//        this.pageWidth = width;
        this.idToFont = idToFont;
        this.fonts = fonts;
//        this.page = new Page();
//        this.page.setNumber(pageNumber);
//        this.page.setWidth(width);
//        this.page.setHseight(height);
//        this.page.setRotation(rotation);
        this.fragments = new ArrayList<TextFragment>(1000);
    }
    
    @Override
    protected void processTextPosition(TextPosition tp) {
        super.processTextPosition(tp);
        
        String chars = tp.getCharacter();
        if (chars.trim().isEmpty()) {
//            currentWordBreakHint = false;
            return;
        } else {
//            currentWordBreakHint = false;
        }
//        Matrix textPos = tp.getTextPos().copy();
//        float x = textPos.getXPosition();
//        float y = pageSize.height - textPos.getYPosition();
        float x = tp.getX();
        float y = tp.getY();
        x += pdfPage.getOffsetUpperRightX();
        y -= pdfPage.getOffsetUpperRightY();
        float width = tp.getIndividualWidths()[0]; // text.getWidth();
        float height;
//        float height = text.getHeightDir()+2;
//        y += 2;
        
        PDFont font = tp.getFont();
        PDMatrix fontMatrix = font.getFontMatrix();
        try {
            float fontSize = tp.getFontSize();
            height = fontSize * font.getFontHeight(CHARACTER_FOR_HEIGHT, 0, 1) / 1000.0f;
        } catch (IOException e) {
            throw new RuntimeException("Caused by IOException", e);
        }
        //height = Math.max(Math.max(height, tp.getHeightDir()), .865f*tp.getYScale());
        height = Math.max(height, tp.getHeightDir());
        width *= fontMatrix.getValue(0, 0) * 1000f;
        height *= fontMatrix.getValue(1, 1) * 1000f;
        height += 3;
        if (height < -1e-3) {
            y -= height;
            height = -height;
        } else if (height <= 1e-3) { 
                height = 12; //1e-3f
        }
        if (width < -1e-3) {
            x -= width;
            width -= width;
        } else if (width <= 1e-3) { 
            width = height*0.5f; //1e-3f 
        }
        y += 1;
        

        if (tp.getDir() == 90) {
            float f = width; width = height; height = f;
            x -= width;
        } else if (tp.getDir() == 180) {
            y -= height;
        } else if (tp.getDir() == 270) {
            float f = width; width = height; height = f;
            y += width;
        }
        
        
//        x += height*0.1;
//        height += height*0.3;
        
//        height *= (-1)*textPos.getValue(0, 1);
//        width *= (-1)*textPos.getValue(1, 0);
        
        Integer id = fonts.get(font);
        currentFontId = id != null ? id.intValue() : -1;
        if (currentFontId < 0) {
            currentFontId = fonts.size();
            float averageFontWidth = 0; //font.getAverageFontWidth();
            String baseFont = font.getBaseFont();
            Font pdfFont;
            PDFontDescriptor fd = font.getFontDescriptor();
            try {
                if (fd != null) {
                    String fontName = fd.getFontName();
                    boolean isBold = fd.isForceBold();
                    boolean isItalic = fd.isItalic();
                    pdfFont = new Font();
                    pdfFont.setId(currentFontId);
                    pdfFont.setFontName(fontName);
                    pdfFont.setAverageFontWidth(fd.getAverageWidth());
                    pdfFont.setXHeight(fd.getXHeight());
                    pdfFont.setIsBold(isBold);
                    pdfFont.setIsItalic(isItalic);
                } else {
                    pdfFont = new Font();
                    pdfFont.setId(currentFontId);
                    pdfFont.setFontName(baseFont);
                    pdfFont.setAverageFontWidth(averageFontWidth);
                }
            } catch (IOException e) {
                throw new RuntimeException("Caused by IOException", e);
            }
            
            fonts.put(font, currentFontId);
            idToFont.put(currentFontId, pdfFont);
        }

        PdfPosition position = new PdfPosition();
        BoundingBox boundingBox = new BoundingBox(x, x+width, y-height, y);
//        if (tp.getDir() == 90) {
//            // boundingBox = new BoundingBox(x, x+width, y-height, y);
//            boundingBox = new BoundingBox(x, x+30, y, y+30);
//        } else if (tp.getDir() == 180) {
//            boundingBox = new BoundingBox(x, x+width, y-height, y);
//            boundingBox = new BoundingBox(x-width, x, y-height, y);
//        } else if (tp.getDir() == 270) {
//            boundingBox = new BoundingBox(x, x+width, y-height, y);
//            boundingBox = new BoundingBox(x-width, x, y, y+height);
//        }
        position.setBoundingBox(boundingBox);
        position.setPageId(pdfPage.getNumber());
        TextFragment fragment = new TextFragment(chars, position, currentFontId); 
        fragment.setFontSize(height);
        fragment.setFontSizePt(height);
        fragment.setSequence(fragments.size());
        //fragment.setWidthOfSpace(currentMissingWidth);
        fragment.setxScale(1);
        // fragment.setWordBreakHint(currentWordBreakHint);
        fragments.add(fragment);
    }

    /**
     * @return
     */
    public List<TextFragment> getFragments() {
        return fragments;
    }
}
