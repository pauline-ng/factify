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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;

import at.knowcenter.code.api.pdf.Document;
import at.knowcenter.code.api.pdf.Font;
import at.knowcenter.code.api.pdf.Page;
import at.knowcenter.code.api.pdf.PdfParser;

/**
 * Implementation of the PDF document parser based on the PDFBox library.
 * 
 * @author Roman Kern <rkern@know-center.at>
 */
public class PdfBoxDocumentParser implements PdfParser {
    private static final String DEFAULT_PDF_PASSWORD = "";
    
    /*
    @SuppressWarnings("unchecked")
    @Override
    public Document parse(InputStream in) throws PdfParserException {
    	FileOutputStream out = null;
    	File tmpFile = null;
    	try {
			tmpFile = File.createTempFile("tmppdf", ".pdf");
			out = new FileOutputStream(tmpFile);
			IOUtils.copy(in, out);
			return parse(tmpFile);
		} catch (IOException e) {
			throw new PdfParserException("Couldn't parse PDF", e);
		} finally {
			if(out != null) IOUtils.closeQuietly(out);
			if(tmpFile != null) tmpFile.delete();
		}
    }
    */
    
    @Override
    public Document parse(File file) throws PdfParserException {
        Document pdfDocument;
               
        try {
	        //PDDocument document = PDDocument.load(in);
	        PDDocument document = PDDocument.loadNonSeq(file, null, "");
	        if (document.isEncrypted()) {
	            document.decrypt(DEFAULT_PDF_PASSWORD);
	        }
	        
	        pdfDocument = new Document();
	        
	        final Map<Object, Integer> fonts = new HashMap<Object, Integer>();
	        final Map<Integer, Font> idToFont = new HashMap<Integer, Font>();
	        
	        PDDocumentCatalog documentCatalog = document.getDocumentCatalog();
	        List<PDPage> allPages = documentCatalog.getAllPages();
	        int pageNumber = 1;
	        
//	        int counter = 0;
//	        if(Debug.get(DEBUG_CONFIG.debug_textpieces)) {
//            	String debug_output_text = PdfExtractionPipeline.global_debug_dir + PdfExtractionPipeline.global_id + "_textpieces_text.txt";
//            	String debug_output_location = PdfExtractionPipeline.global_debug_dir + PdfExtractionPipeline.global_id + "_textpieces_location.txt";
//
//            	utility util = new utility();
//            	util.writeFile(debug_output_text, "", false);
//            	util.writeFile(debug_output_location, "", false);
//            }
	        for (PDPage page : allPages) {
	            int pageRotation = page.findRotation();	            
	            float pageWidth = page.findMediaBox().getWidth();
	            float pageHeight = page.findMediaBox().getHeight();
	            float offsetUpperRightX = page.findMediaBox().getUpperRightX() - page.findCropBox().getUpperRightX();
	            float offsetUpperRightY = page.findMediaBox().getUpperRightY() - page.findCropBox().getUpperRightY();
	            // System.out.println(String.format("XXX - offsets(%.3f, %.3f)", offsetUpperRightX, offsetUpperRightY));
	            
	            final Page pdfPage = new Page();
	            pdfPage.setNumber(pageNumber++);
	            pdfPage.setRotation(pageRotation);
	            pdfPage.setHeight(pageHeight);
	            pdfPage.setWidth(pageWidth);
	            pdfPage.setOffsetUpperRightX(offsetUpperRightX);
	            pdfPage.setOffsetUpperRightY(offsetUpperRightY);
	            
	            PDStream contents = page.getContents();
	            if (contents != null) {
	                if (true) {
    	                PdfTextFragmentCollector collector = new PdfTextFragmentCollector(pdfPage, idToFont, fonts);
    	                collector.processStream(page, page.findResources(), contents.getStream());
    	                pdfPage.setLines(collector.getLines());
	                } else {
	                    CustomPageDrawer drawer = new CustomPageDrawer(pdfPage, pageNumber, 
	                            pageWidth, pageHeight, pageRotation, 
	                            idToFont, fonts);
	                    BufferedImage image = convertToImage(page, BufferedImage.TYPE_INT_RGB, 72, drawer);
	                    pdfPage.setCharacters(drawer.getFragments());
	                }
//	                if(Debug.get(DEBUG_CONFIG.debug_textpieces)) {
//	                	String debug_output_location = PdfExtractionPipeline.global_debug_dir + PdfExtractionPipeline.global_id + "_textpieces_location.txt";
//	                	String debug_output_text = PdfExtractionPipeline.global_debug_dir + PdfExtractionPipeline.global_id + "_textpieces_text.txt";
//	           	     
//	                	utility util = new utility();
//	                	
//	                	for(int i = 0; i < pdfPage.getLines().size(); i++) {
//	                		util.writeFile(debug_output_location, counter + "\t" + pdfPage.getLines().get(i).toString() + "\r\n", true);
//	                		util.writeFile(debug_output_text, counter + "\t" + pdfPage.getFragments().get(i).getText() + "\r\n", true);
//	    	                counter++;
//	                	}
//	                }
	            }
	            pdfDocument.getPages().add(pdfPage);
	        }
	        
	        for (Entry<Integer, Font> e : idToFont.entrySet()) {
	            pdfDocument.getFonts().add(e.getValue());
	        }
	        document.close();
        } catch(Exception e) {
        	throw new PdfParserException("Couldn't parse PDF", e);
        }
        
        return pdfDocument;
    }
    
    public BufferedImage convertToImage(PDPage page, int imageType, int resolution, 
            CustomPageDrawer drawer) throws IOException
    {
        PDRectangle cropBox = page.findCropBox();
        float widthPt = cropBox.getWidth();
        float heightPt = cropBox.getHeight();
        float scaling = resolution / (float)72; // DEFAULT_USER_SPACE_UNIT_DPI;
        int widthPx = Math.round(widthPt * scaling);
        int heightPx = Math.round(heightPt * scaling);
        //TODO The following reduces accuracy. It should really be a Dimension2D.Float.
        Dimension pageDimension = new Dimension( (int)widthPt, (int)heightPt );
        int rotationAngle = page.findRotation();
        // normalize the rotation angle
        if (rotationAngle < 0)
        {
            rotationAngle += 360;
        }
        else if (rotationAngle >= 360)
        {
            rotationAngle -= 360;
        }
        // swap width and height
        BufferedImage retval;
        if (rotationAngle == 90 || rotationAngle == 270)
        {
            retval = new BufferedImage( heightPx, widthPx, imageType );
        }
        else
        {
            retval = new BufferedImage( widthPx, heightPx, imageType );
        }
        Graphics2D graphics = (Graphics2D)retval.getGraphics();
        graphics.setBackground( new Color( 255, 255, 255, 0 )/*TRANSPARENT_WHITE*/ );
        graphics.clearRect( 0, 0, retval.getWidth(), retval.getHeight() );
        if (rotationAngle != 0)
        {
            int translateX = 0;
            int translateY = 0;
            switch(rotationAngle) 
            {
                case 90:
                    translateX = retval.getWidth();
                    break;
                case 270:
                    translateY = retval.getHeight();
                    break;
                case 180:
                    translateX = retval.getWidth();
                    translateY = retval.getHeight();
                    break;
                default:
                    break;
            }
            graphics.translate(translateX,translateY);
            graphics.rotate((float)Math.toRadians(rotationAngle));
        }
        graphics.scale( scaling, scaling );
        drawer.drawPage( graphics, page, pageDimension );
        drawer.dispose();
        graphics.dispose();
        return retval;
    }

    
    public static void main(String[] args) throws PdfParserException, IOException {
//    	long start = System.nanoTime();
//		Document document = new PdfBoxDocumentParser().parse(new File("C:/Users/mzechner/Desktop/code/DOW CODE (296150) 2012-01-30.pdf"));
//		int i = 1;
//		for(Page page: document.getPages()) {
//			System.out.println("page " + i++);
//			int j = 0;
//			for(Image image: page.getImages()) {
//				System.out.println("writting image file");
//				FileUtils.writeByteArrayToFile(new File("d:/temp/image" + i + "-" + j + ".png"), image.getByteArray());
//			}
//		}
//		System.out.println("took: " + (System.nanoTime()-start)/1000000000.0f + " secs");
	}
}
