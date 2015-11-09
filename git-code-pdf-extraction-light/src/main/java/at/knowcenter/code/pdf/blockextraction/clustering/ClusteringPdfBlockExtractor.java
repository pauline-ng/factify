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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.knowcenter.code.api.pdf.Block;
import at.knowcenter.code.api.pdf.Document;
import at.knowcenter.code.api.pdf.Font;
import at.knowcenter.code.api.pdf.Page;
import at.knowcenter.code.api.pdf.TextFragment;
import at.knowcenter.code.pdf.blockextraction.PdfPageBlockExtractor;

/**
 * 
 * 
 * @author Roman Kern <rkern@tugraz.at>
 */
public class ClusteringPdfBlockExtractor implements PdfPageBlockExtractor {
    private static final Logger logger = Logger.getLogger(ClusteringPdfBlockExtractor.class.getName());
    
    public static boolean debug;
    
    public String doi = null;
    
    /**
     * Creates a new instance of this class.
     */
    public ClusteringPdfBlockExtractor() {
    }
    
    @Override
    public List<Block> extractBlocks(Document pdfDocument, String id) {
        List<BlocksEntry> pageBlocks = new ArrayList<BlocksEntry>();
        
        final Map<Integer, Font> idToFont = new HashMap<Integer, Font>();
        for (Page pdfPage : pdfDocument.getPages()) {
            Block mergedWords = new WordMerger(pdfPage, pdfPage.getFragments()).merge();
            Block splittedWords = new WordSplitter(pdfPage, mergedWords).split();
            Block mergedLines = new LineMerger(pdfPage, splittedWords).merge();
            Block splitedLines = mergedLines; //new LineSplitter(pdfPage, mergedLines).split();
            Block cleanedLines = new LineCleaner(pdfPage, splitedLines).clean();
            Block cleanedLines2 = cleanedLines; // new HeadingLineCleaner(pdfPage, cleanedLines).clean();
            double lineSpacing = new LineSpacingDetector(cleanedLines2).getLineSpacing();
            Block blocksFragments = new BlockMerger(pdfPage, cleanedLines2, lineSpacing, idToFont).merge();
            Block splitedBlocks =  new BlockSplitter(pdfPage, blocksFragments).split();
            
            for(int i = 0; i < splitedBlocks.getLineBlocks().size() && doi == null; i++) {
            	String line2 = splitedBlocks.getLineBlocks().toString();
            	String regEx = "10\\.[0-9]{4,}/[^\\s]*[^\\s\\.,]";// /10\.[0-9]{4,}\/[^\s]*[^\s\.,]/
            	Pattern pattern = Pattern.compile(regEx);
            	Matcher matcher = pattern.matcher(line2);
            	while (matcher.find()) {
            		doi = matcher.group();
            		System.out.println("doi is " + doi);
            		break;
            	}
            }
            
            pageBlocks.add(new BlocksEntry(pdfPage, splitedBlocks));
            
            if (debug) {
                debugOutput(splitedBlocks, id);
            }
        }
        return getPages(pageBlocks);
    }
    
    /**
     * @param fragments 
     * @param pageWidth 
     * @param pageHeight 
     * 
     */
    private void testLayout(List<TextFragment> fragments, float pageWidth, float pageHeight) {
        int dimX = 500, dimY = (int)((double)dimX*pageHeight/pageWidth);
        byte[][] matrix = new byte[dimX][];
        for (int i = 0; i < dimX; i++) {
            matrix[i] = new byte[dimY];
        }
        
        for (TextFragment fragment : fragments) {
            int xs = (int)Math.floor(dimX*fragment.getX()/pageWidth), xe = (int)Math.ceil(dimX*(fragment.getX()+fragment.getWidth())/pageWidth);
            int ys = (int)Math.floor(dimY*fragment.getY()/pageHeight), ye = (int)Math.ceil(dimY*(fragment.getY()+fragment.getHeight())/pageHeight);
            
            for (int y = ys; y <= Math.min(dimY-1, ye); y++) {
                for (int x = xs; x <= Math.min(dimX-1, xe); x++) {
                    matrix[x][y] = 1;
                }
            }
        }

//        VectorContainer data = createDataSet2(pageWidth, pageHeight, matrix);
//        
//        long start = System.currentTimeMillis();
//        
//        GrowingNeuralGas gng = new GrowingNeuralGas( );
//        
//        gng.addIterationListener( new NeuralGasVisualization( data, null, 1000 ) );
//        NeuralGasResult<GrowingNeuralGasNode, GrowingNeuralGasEdge> result = gng.grow( 
//                data, 0.05, 0.005, 0.7, 0.0005, data.size() / 500, 10, 250, (int)(data.size()*3));
//        System.out.println(String.format("GNG took %.3f seconds", (System.currentTimeMillis()-start)/1000.0));
//        
//        Log.println( result.graph.getNodes().size() );
    }

//    private VectorContainer createDataSet(int dimX, int dimY, byte[][] matrix) {
//        Random random = new Random(0);
//        VectorContainer data = new VectorContainer();
//        for (int i = 0; i < 50000; i++) {
//            boolean isFinished = false;
//            double x = random.nextDouble();
//            double y = random.nextDouble();
//            
//            if (matrix[(int)(x*dimX)][(int)(y*dimY)] == 0) {
//                data.add(new DenseVector((x*dimX), (y*dimY)));
//                isFinished = true;
//            }
//        }
//        return data;
//    }
    
//    private VectorContainer createDataSet2(double pageWidth, double pageHeight, byte[][] matrix) {
//        final int cx = 5, cy = 5;
//        
//        VectorContainer data = new VectorContainer();
//        double dx = pageWidth / matrix.length;
//        double ddx = dx / cx;
//        for (int x = 0; x < matrix.length; x++) {
//            byte[] row = matrix[x];
//            double dy = pageHeight / row.length;
//            double ddy = dy / cy;
//            double ox = pageWidth*x / matrix.length;
//            
//            for (int y = 0; y < row.length; y++) {
//                if (row[y] == 0) {
//                    double rx = ox + ddx/2;
//                    double oy = pageHeight*y / row.length;
//                    
//                    for (int i = 0; i < cx; i++) {
//                        double ry = oy + ddy/2;
//                        for (int j = 0; j < cy; j++) {
//                            data.add(new DenseVector(rx, ry));
//                            ry += ddy;
//                        }
//                        rx += ddx;
//                    }
//                }
//            }
//        }
//        return data;
//    }

    /**
     * @param blockEntries 
     * @return
     */
    private List<Block> getPages(List<BlocksEntry> blockEntries) {
        List<Block> result = new ArrayList<Block>();
        for (BlocksEntry pageEntry : blockEntries) {        	
        	result.add(pageEntry.blocks);
        }
        return result;
        
//        not commented by huangxc
//        for (BlocksEntry pageEntry : blockEntries) {
//            Block page = pageEntry.blocks;
//            for (Block blocks : page.getSubBlocks()) {
//                StringBuilder block = new StringBuilder();
//                List<List<String>> lineList = new ArrayList<List<String>>();                
//                List<String> wordList = new ArrayList<String>();
//                
//                for (Block line : blocks.getSubBlocks()) {
//                    StringBuilder lineBuilder = new StringBuilder();
//                    List<String> lineWordList = new ArrayList<String>();
//                    
//                    removeHypen(block);
//                    
//                    int oldLen = 0;
////                    double currentX = Double.NaN;
//                    for (Block words : line.getSubBlocks()) {
//                        if (lineBuilder.length() != oldLen) { lineBuilder.append(' '); }
//                        oldLen = lineBuilder.length();
//                        
//                        StringBuilder word = new StringBuilder();
//                        List<TextFragment> fragments = words.getFragments();
////                        if (fragments.size() > 0) {
////                            float x = fragments.get(0).x;
////                            if (x < currentX) {
////                                lineBuilder.setLength(0);
////                                oldLen = 0;
////                            }
////                            currentX = fragments.get(fragments.size()-1).x;
////                        }
//                        for (TextFragment f : fragments) {
//                            word.append(f.getText());
//                        }
//                        String w = word.toString().trim();
//                        lineBuilder.append(w);
//                        lineWordList.add(w);
//                    }
//                    
//                    lineList.add(lineWordList);
//                    wordList.addAll(lineWordList);
//                    
//                    String lineText = lineBuilder.toString();
//                    if (lineText.length() > 1 && Character.isLowerCase(lineText.charAt(0))) {
//                        removeHypen(block);
//                    }
//                    block.append(lineText);
//                }
//                pageBlocks.add(new Block(blocks));
//            }
//            Collections.sort(pageBlocks, new Comparator<PdfTextBlock>() {
//                @Override
//                public int compare(PdfTextBlock arg0, PdfTextBlock arg1) {
//                    double s0 = getStart(arg0.getFragments());
//                    double s1 = getStart(arg1.getFragments());
//                    return Double.compare(s0, s1);
//                }
//                private double getStart(List<TextFragment> list) {
//                    if (list == null || list.isEmpty()) { return -1; }
//                    double sum = 0;
//                    for (TextFragment f : list) {
//                        sum += f.y; // f.seq;
//                    }
//                    return sum / list.size();
//                }
//            });
//            result.add(new PageBlocks(pageEntry.pdfPage, pageBlocks));
//        }
//        return result;
    }
    
    /**
     * @param block
     */
    private void removeHypen(StringBuilder block) {
        if (block.length() > 0) {
            if (block.charAt(block.length()-1) == '-') {
                block.setLength(block.length()-1);
            } else {
                block.append(' ');
            }
        }
    }
    
    /*
    public void write(File xpdfFile) throws IOException {
        OutputStream outputStream = new FileOutputStream(xpdfFile);
        XmlHelper.toXml(pdfDocument, outputStream);
        IOUtils.closeQuietly(outputStream);
    }
    */

    /**
     * @param blocksFragments 
     * 
     */
    private void debugOutput(Block blocksFragments, String id) {
        //                int lineCounter = 1;
        System.out.println(id);
        for (Block blocks : blocksFragments.getSubBlocks()) {
            System.out.println("--");
            for (Block line : blocks.getSubBlocks()) {
                StringBuilder b = new StringBuilder();
                
                int oldLen = 0;
                for (Block words : line.getSubBlocks()) {
                    if (b.length() != oldLen) { b.append(' '); }
                    oldLen = b.length();
                    
                    StringBuilder word = new StringBuilder();
                    for (TextFragment f : words.getFragments()) {
                        //if (b.length() == 0) { b.append(f.seq).append(": "); }
                        word.append(f.getText());
                        //word.append(" (" + f.seq + ")");
                    }
                    b.append(word.toString().trim());
                    
//                            if (l != null) {
//                                b.append(String.format("    <%.2f, %.2f, %.2f, %2f>",
//                                        l.x, l.y, l.widthOfSpace, l.height));
//                            }
                }
                TextFragment f = line.getFragments().get(0);
                b.append("      |"+f.getSequence()+"|"+line.getBoundingBox());
//                for (XmlPdfTextFragment x : line.getFragments()) {
//                    b.append(""+x.seq+", ");
//                }
                System.out.println(b.toString());
//                    System.out.println(String.format("%s <%.2f, %.2f, %s>", 
//                            b.toString(), f.x, f.y, f.text));
            }
//                    System.out.println("\\----------------------------/");
        }
    }

//    /**
//     * 
//     * 
//     * @author Roman Kern <rkern@tugraz.at>
//     */
//    private final class CustomVector extends DenseVector {
//        
//        
//        
//        /**
//         * Creates a new instance of this class.
//         * @param vals
//         */
//        private CustomVector(double... vals) {
//            super(vals);
//        }
//        
//        /**
//         * Creates a new instance of this class.
//         * @param d
//         */
//        public CustomVector(Vector d) {
//            super(d);
//        }
//
//
//
//        @Override
//        public double dist(Vector b) {
//            return dist(b);
////            return distL1(b);
//            //return 1 / super.dist(b);
//        }
//        
//        @Override
//        public Vector cpy() {
//            return new CustomVector(this);
//        }
//    }
    
    private static class BlocksEntry {
        final Page pdfPage;
        final Block blocks;

        /**
         * Creates a new instance of this class.
         * @param pdfPage
         * @param blocksFragments
         */
        public BlocksEntry(Page pdfPage, Block blocksFragments) {
            this.pdfPage = pdfPage;
            this.blocks = blocksFragments;
        }
        
    }

}
