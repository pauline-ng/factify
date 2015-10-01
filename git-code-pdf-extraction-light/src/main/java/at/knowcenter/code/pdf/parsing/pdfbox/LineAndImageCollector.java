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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdfviewer.PageDrawer;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectForm;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.PDFOperator;
import org.apache.pdfbox.util.PDFStreamEngine;
import org.apache.pdfbox.util.operator.OperatorProcessor;

import at.knowcenter.code.api.pdf.Page;

/**
 * @author sklampfl
 *
 */
public class LineAndImageCollector {
    private static Point2D transformPoint(double x, double y, PDFStreamEngine pdfStreamEngine, Page page, boolean fixY) {
        double[] position = {x, y}; 
        pdfStreamEngine.getGraphicsState().getCurrentTransformationMatrix().createAffineTransform().transform(
                position, 0, position, 0, 1);
        
        Point2D pos = new Point2D.Double(position[0], fixY ? page.getHeight()-position[1] : position[1]);
        return pos;
    }
    
    private static Point2D transformPoint(COSNumber x, COSNumber y, PDFStreamEngine pdfStreamEngine, Page page) {
        return transformPoint(x.doubleValue(), y.doubleValue(), pdfStreamEngine, page, true);
    }

    public static class LineTo extends OperatorProcessor {

		/**
	     * process : l : Append straight line segment to path.
	     * @param operator The operator that is being executed.
	     * @param arguments List
	     */
		@Override
	    public void process(PDFOperator operator, List<COSBase> arguments) {
	        PdfTextFragmentCollector collector = (PdfTextFragmentCollector)context;
	        Page page = collector.getPdfPage();

	        COSNumber x = (COSNumber)arguments.get( 0 );
	        COSNumber y = (COSNumber)arguments.get( 1 );
	        
	        Point2D pos = transformPoint(x, y, getContext(), page);
	        collector.addLineTo(pos);
	    }
	}
	
	public static class MoveTo extends OperatorProcessor {
		/**
	     * process : l : Append straight line segment to path.
	     * @param operator The operator that is being executed.
	     * @param arguments List
	     */
		@Override
	    public void process(PDFOperator operator, List<COSBase> arguments) {
	        PdfTextFragmentCollector collector = (PdfTextFragmentCollector)context;
	        Page page = collector.getPdfPage();

	        COSNumber x = (COSNumber)arguments.get( 0 );
	        COSNumber y = (COSNumber)arguments.get( 1 );
	        
            Point2D pos = transformPoint(x, y, getContext(), page);
            collector.addMoveTo(pos);
	    }
	}
	
	public static class CloseAndStrokePath extends OperatorProcessor {
	    /**
	     * process : l : Append straight line segment to path.
	     * @param operator The operator that is being executed.
	     * @param arguments List
	     */
	    @Override
	    public void process(PDFOperator operator, List<COSBase> arguments) {
            PdfTextFragmentCollector collector = (PdfTextFragmentCollector)context;
            Page page = collector.getPdfPage();
	        collector.finishLine();
	    }
	}
	
    public static class CurveTo extends OperatorProcessor {
        /**
         * process : c : Append curved segment to path.
         * 
         * @param operator
         *            The operator that is being executed.
         * @param arguments
         *            List
         */
        public void process(PDFOperator operator, List<COSBase> arguments) {
            PdfTextFragmentCollector collector = (PdfTextFragmentCollector)context;
            Page page = collector.getPdfPage();

            COSNumber x1 = (COSNumber)arguments.get(0);
            COSNumber y1 = (COSNumber)arguments.get(1);
            COSNumber x2 = (COSNumber)arguments.get(2);
            COSNumber y2 = (COSNumber)arguments.get(3);
            COSNumber x3 = (COSNumber)arguments.get(4);
            COSNumber y3 = (COSNumber)arguments.get(5);

            Point2D point1 = transformPoint(x1, y1, getContext(), page);
            Point2D point2 = transformPoint(x2, y2, getContext(), page);
            Point2D point3 = transformPoint(x3, y3, getContext(), page);
            
            collector.addLineTo(point1);
            collector.addLineTo(point2);
            collector.addLineTo(point3);
        }
    }
	
    public static class Rectangle extends OperatorProcessor {
        /**
         * process : c : Append curved segment to path.
         * 
         * @param operator
         *            The operator that is being executed.
         * @param arguments
         *            List
         */
        public void process(PDFOperator operator, List<COSBase> arguments) {
            PdfTextFragmentCollector collector = (PdfTextFragmentCollector)context;
            Page page = collector.getPdfPage();

            COSNumber x = (COSNumber)arguments.get(0);
            COSNumber y = (COSNumber)arguments.get(1);
            COSNumber w = (COSNumber)arguments.get(2);
            COSNumber h = (COSNumber)arguments.get(3);

            Point2D point = transformPoint(x, y, getContext(), page);
            Point2D wh = transformPoint(x.doubleValue() + w.doubleValue(), y.doubleValue() + h.doubleValue(), getContext(), page, true);
            
            collector.addMoveTo(point);
            collector.addLineTo(new Point2D.Double(wh.getX(), point.getY()));
            collector.addLineTo(new Point2D.Double(wh.getX(), wh.getY()));
            collector.addLineTo(new Point2D.Double(point.getX(), wh.getY()));
            collector.finishLine();
        }
    }
    
	public static class BeginInlineImage extends OperatorProcessor {

	    /**
	     * process : BI : begin inline image.
	     * @param operator The operator that is being executed.
	     * @param arguments List
	     * @throws IOException If there is an error displaying the inline image.
	     */
	    public void process(PDFOperator operator, List<COSBase> arguments)  throws IOException {
	        PdfTextFragmentCollector collector = (PdfTextFragmentCollector)context;
	        Page page = collector.getPdfPage();
//	        PDInlinedImage image = new PDInlinedImage();
//	        image.setImageParameters( operator.getImageParameters() );
//	        image.setImageData( operator.getImageData() );
//	        BufferedImage awtImage = image.createImage( context.getColorSpaces() );
	    }
	}
	
	public static class Invoke extends OperatorProcessor {
	    /**
	     * process : Do : Paint the specified XObject (section 4.7).
	     * @param operator The operator that is being executed.
	     * @param arguments List
	     * @throws IOException If there is an error invoking the sub object.
	     */
	    public void process(PDFOperator operator, List<COSBase> arguments) throws IOException {
	        if (true) return;
  
	    	PdfTextFragmentCollector collector = (PdfTextFragmentCollector)context;
            COSName objectName = (COSName)arguments.get( 0 );
            Map<String, PDXObject> xobjects = context.getResources().getXObjects();
            PDXObject xobject = (PDXObject)xobjects.get( objectName.getName() );
            if( xobject instanceof PDXObjectImage )
            {
                PDXObjectImage image = (PDXObjectImage)xobject;
                PDPage page = context.getCurrentPage();
                double pageHeight = page.findMediaBox().getHeight();                                
                Matrix ctmNew = context.getGraphicsState().getCurrentTransformationMatrix();
                float yScaling = ctmNew.getYScale();
                float angle = (float)Math.acos(ctmNew.getValue(0, 0)/ctmNew.getXScale());
                if (ctmNew.getValue(0, 1) < 0 && ctmNew.getValue(1, 0) > 0) {
                    angle = (-1)*angle;
                }
                ctmNew.setValue(2, 1, (float)(pageHeight - ctmNew.getYPosition() - Math.cos(angle)*yScaling));
                ctmNew.setValue(2, 0, (float)(ctmNew.getXPosition() - Math.sin(angle)*yScaling));
                ctmNew.setValue(0, 1, (-1)*ctmNew.getValue(0, 1));
                ctmNew.setValue(1, 0, (-1)*ctmNew.getValue(1, 0));

                float imageXScale = ctmNew.getXScale();
                float imageYScale = ctmNew.getYScale();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                image.write2OutputStream(outputStream);
                collector.getPdfPage().addImage(outputStream.toByteArray(), ctmNew.getXPosition(), ctmNew.getYPosition(), imageXScale, imageYScale, image.getSuffix());
            }
            else if(xobject instanceof PDXObjectForm)
            {
                // save the graphics state
                context.getGraphicsStack().push( (PDGraphicsState)context.getGraphicsState().clone() );
                PDPage page = context.getCurrentPage();
                
                PDXObjectForm form = (PDXObjectForm)xobject;
                COSStream invoke = (COSStream)form.getCOSObject();
                PDResources pdResources = form.getResources();
                if(pdResources == null) {
                    pdResources = page.findResources();
                }
                Matrix matrix = form.getMatrix();
                if (matrix != null) {
                    Matrix xobjectCTM = matrix.multiply( context.getGraphicsState().getCurrentTransformationMatrix());
                    context.getGraphicsState().setCurrentTransformationMatrix(xobjectCTM);
                }
                context.processSubStream( page, pdResources, invoke );
                context.setGraphicsState( (PDGraphicsState)context.getGraphicsStack().pop() );
            }                       
	    }
	}
}
