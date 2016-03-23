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

package at.knowcenter.code.pdf.parsing.pdfbox;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.common.PDMatrix;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.ResourceLoader;
import org.apache.pdfbox.util.TextNormalize;
import org.apache.pdfbox.util.TextPosition;

import at.knowcenter.code.api.annotations.PdfPosition;
import at.knowcenter.code.api.pdf.BoundingBox;
import at.knowcenter.code.api.pdf.Font;
import at.knowcenter.code.api.pdf.Page;
import at.knowcenter.code.api.pdf.TextFragment;

/**
 * 
 * 
 * @author Roman Kern <rkern@tugraz.at>
 */
final class PdfTextFragmentCollector extends PDFTextStripper {
    private static final byte[] CHARACTER_FOR_HEIGHT = new byte[] { 'H' };
    
    /** XmlPdfPage pdfPage */
    private final Page                       pdfPage;
    /** Map<Integer,XmlPdfFont> idToFont */
    private final Map<Integer, Font>         idToFont;
    /** IdentityHashMap<PDFont,Integer> fonts */
    private final Map<Object, Integer> fonts;
    
    private int fragmentCounter;
    private boolean wordBreakHint;
    private TextPosition previousTextPosition;
    private TextFragment previousFragment;
    
    private final StringBuilder debugBuffer = new StringBuilder();
    private TextNormalize normalize;
    private Point2D startPosition;
    private Point2D currentPosition;
    private List<Page.Line> lines = new ArrayList<Page.Line>();
    
//    private StringBuilder debug = new StringBuilder();

    /**
     * Creates a new instance of this class.
     * @param pdfPage
     * @param idToFont
     * @param fonts
     */
    PdfTextFragmentCollector(Page pdfPage, Map<Integer, Font> idToFont,
                             Map<Object, Integer> fonts) throws IOException {
    	super(ResourceLoader.loadProperties("pdfbox.properties", true ));
        this.pdfPage = pdfPage;
        this.idToFont = idToFont;
        this.fonts = fonts;
        this.wordBreakHint = true;
        this.normalize = new TextNormalize(/*this.outputEncoding*/"UTF-8");
    }
    
    @Override
    public void processEncodedText(byte[] string) throws IOException {
        super.processEncodedText(string);
        // wordBreakHint = true;
    }

    @Override
    protected void processTextPosition(TextPosition tp) {
        List<TextFragment> fragments = pdfPage.getFragments();
        
        if (true) {
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
            
            String fontSpec = font.getBaseFont();
            Object fontRef;
            /*
            if (fontSpec != null) {
                PDFontDescriptor fd = font.getFontDescriptor();
                if (fd != null) {
                    String fontName = fd.getFontName();
                    boolean isBold = fd.isForceBold();
                    boolean isItalic = fd.isItalic();
                    float xHeight = fd.getXHeight();
                    fontSpec = String.format("%s-%s-%s-%.2f",
                        fontName, isBold ? "bold" : "d", isItalic ? "italic" : "d", xHeight);
//                } else {
//                    try {
//                        float fontHeight = font.getFontHeight(CHARACTER_FOR_HEIGHT, 0, 1) / 1000.0f;
//                        fontSpec = String.format("%s-%.2f", fontSpec, fontHeight);
//                    } catch (IOException e) {
//                        throw new RuntimeException("Caused by IOException", e);
//                    }
                    fontRef = fontSpec;
                } else {
                    fontRef = font;
                }
            } else {
                fontRef = font;
            }
            */
            fontRef = font;
            
            Integer id = fonts.get(fontRef);
            int currentFontId = id != null ? id.intValue() : -1;
            if (currentFontId < 0) {
                currentFontId = fonts.size();
                float averageFontWidth = 0; //font.getAverageFontWidth();
                Font pdfFont;
                PDFontDescriptor fd = font.getFontDescriptor();
                try {
                    if (fd != null) {
                        String fontName = fontSpec != null? fontSpec : fd.getFontName();
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
                        pdfFont.setFontName(fontSpec);
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
        } else {
    
            
            PDFont font = tp.getFont();
            
            String text = tp.getCharacter();
            boolean ignoreFragment = false;
            if (previousTextPosition != null) {
                if (tp.isDiacritic() && previousTextPosition.contains(tp)) {
                    previousTextPosition.mergeDiacritic(tp, this.normalize);
    //                previousFragment.text = previousTextPosition.getCharacter();
                    if (previousFragment != null) { 
                        previousFragment.setText(Normalizer.normalize(previousTextPosition.getCharacter(), Form.NFC));
                    }
                    ignoreFragment = true;
                }
                if (previousTextPosition.isDiacritic() && tp.contains(previousTextPosition)) {
                    tp.mergeDiacritic(previousTextPosition, this.normalize);
                    text = Normalizer.normalize(tp.getCharacter(), Form.NFC);
                }
            } 
            if (tp.isDiacritic()) {
                ignoreFragment = true;
            }
            
            Integer fontId = fonts.get(font);
            float fontXHeight = 0;
            float capHeight = 0;
            if (fontId == null) {
                fontId = fonts.size();
                
                tp.contains(tp);
                
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
                        pdfFont.setId(fontId);
                        pdfFont.setFontName(fontName);
                        pdfFont.setAverageFontWidth(fd.getAverageWidth());
                        pdfFont.setXHeight(fd.getXHeight());
                        pdfFont.setIsBold(isBold);
                        pdfFont.setIsItalic(isItalic);
                        capHeight = fd.getCapHeight();
                        fontXHeight = fd.getXHeight();
                    } else {
                        pdfFont = new Font();
                        pdfFont.setId(fontId);
                        pdfFont.setFontName(baseFont);
                        pdfFont.setAverageFontWidth(averageFontWidth);
                        //fontWithoutDescriptionCounter++;
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Caused by IOException", e);
                }
                
                fonts.put(font, fontId);
                idToFont.put(fontId, pdfFont);
            } else {
                Font f = idToFont.get(fontId);
                fontXHeight = f.getXHeight();
            }
            
            float rawWidth = tp.getIndividualWidths()[0];
            float rawHeight = tp.getYScale();
            Matrix textPos = tp.getTextPos().copy();
            float x = tp.getX(); // + 2f; //textPos.getXPosition() + .2f*rawWidth;
            float y = pdfPage.getHeight() - textPos.getYPosition(); // + .2f*rawHeight;
            x += pdfPage.getOffsetUpperRightX();
            y -= pdfPage.getOffsetUpperRightY();
            float height = .865f*rawHeight; 
            float width = /*.865f**/rawWidth;
            
            if (tp.getDir() == 90) {
                float f = width; width = height; height = f;
                x -= width;
            } else if (tp.getDir() == 180) {
                y -= height;
            } else if (tp.getDir() == 270) {
                float f = width; width = height; height = f;
                y += width;
            }
            float fontSize = tp.getFontSize();
            float fontSizeInPt = tp.getFontSizeInPt();
            
    //        if (height < 0 || width < 0 || x < 0 || y < 0 || fontSize < 0 || fontSizeInPt < 0) {
    //            // System.out.println();
    //        }
            
            if (height < -1e-3) {
                y -= height;
                height = -height;
            } else if (height <= 1e-3) { 
                if (fontSizeInPt > 0) {
                    height = fontSizeInPt;
                } else {
                    height = 12; //1e-3f
                }
    //            ignoreFragment = true;
    //            wordBreakHint = true;
            }
    //        if (fontXHeight > 0) {
    //            fontSize = fontSizeInPt = height = fontXHeight;
    //        } else {
                fontSize = fontSizeInPt = height;
    //        }
            
            if (width < -1e-3) {
                x -= width;
                width -= width;
            } else if (width <= 1e-3) { 
                width = height*0.5f; //1e-3f 
    //            ignoreFragment = true;
    //            wordBreakHint = true;
            }
            
            text = text.replace((char)160, (char)32);
            if (text.length() == 0 || text.trim().length() == 0) {
                ignoreFragment = true;
                if (previousTextPosition == null || !previousTextPosition.contains(tp))
                    wordBreakHint = true;
            } else if (previousFragment != null) {
                if (previousFragment.getPosition().getBoundingBox().getX() == x && previousFragment.getPosition().getBoundingBox().getY() == y) {
                    ignoreFragment = true;
                }
            }
            
            if (!ignoreFragment) {
                TextFragment fragment = new TextFragment();
                fragment.setSequence(fragmentCounter);
                fragment.getPosition().setBoundingBox(new BoundingBox(x, x + width, y - height, y));
                fragment.setFontId(fontId);
                fragment.setFontSize(Math.abs(fontSize));
                fragment.setFontSizePt(Math.abs(fontSizeInPt));
                fragment.setxScale(tp.getXScale()); 
                fragment.setWidthOfSpace(tp.getWidthOfSpace());
                fragment.setText(text);
                fragment.setWordBreakHint(wordBreakHint);                    
                
                fragments.add(fragment);
                previousFragment = fragment;
                
                wordBreakHint = false;
                fragmentCounter++;
            }
            previousTextPosition = tp;
            
            debugBuffer.append(text);
        }
    }

    public Page getPdfPage() {
    	return pdfPage;
    }

    /**
     * @param pos
     */
    public void addMoveTo(Point2D pos) {
        startPosition = pos;
        currentPosition = null;
    }

    /**
     * @param pos
     */
    public void addLineTo(Point2D pos) {
        if (currentPosition != null || startPosition != null) {
            Page.Line line = new Page.Line(currentPosition != null ? currentPosition : startPosition, pos);
            lines.add(line);
            currentPosition = pos;
        } else {
            System.err.println("Got a line end without a start");
        }
    }
    
    public void finishLine() {
        if (startPosition != null && currentPosition != null) {
            Page.Line line = new Page.Line(currentPosition, startPosition);
            lines.add(line);
        }
        startPosition = currentPosition = null;
    }
    
    /**
     * Returns the lines.
     * @return the lines
     */
    public List<Page.Line> getLines() {
        return lines;
    }
}