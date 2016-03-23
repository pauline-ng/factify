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
package at.knowcenter.code.pdf.blockclassification.detection.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.knowcenter.code.api.pdf.Block;
import at.knowcenter.code.api.pdf.BlockLabel;
import at.knowcenter.code.api.pdf.Document;
import at.knowcenter.code.api.pdf.Page;
import at.knowcenter.code.api.pdf.ReadingOrder;
import at.knowcenter.code.api.pdf.TextFragment;
import at.knowcenter.code.pdf.blockclassification.BlockLabeling;
import at.knowcenter.code.pdf.blockrelation.geometric.DefaultBlockNeighborhood;

public class PdfArticle {
	public static final Pattern EMAIL_PATTERN = Pattern.compile("[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})");
    public static final Pattern EMAIL_PATTERN_SIMPLE = Pattern.compile("@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})");

    private final String id;
    private final List<PdfPage> pages;
    private final DefaultBlockNeighborhood neighborhood;
    private final ReadingOrder readingOrder;
    private final BlockLabeling labeling;

    /**
     * Creates a new instance of this class.
     * @param id 
     * @param pages
     * @param parsingTime 
     * @param clusteringTime 
     */
    public PdfArticle(String id, List<PdfPage> pages, ReadingOrder readingOrder, BlockLabeling labeling, DefaultBlockNeighborhood neighborhood) {
        this.id = id;
        this.readingOrder = readingOrder;
        this.labeling = labeling;
        this.neighborhood = neighborhood;
        if (readingOrder != null) {
            List<PdfPage> reorderedPages = new ArrayList<PdfPage>(pages.size());
            for(PdfPage page : pages) {
                List<Integer> ro = readingOrder.getReadingOrder(page.getPageNumber());
                List<Block> subBlocks = new ArrayList<Block>(page.getPageBlocks());
                List<Block> blocksInSeq = new ArrayList<Block>(subBlocks.size());
                for (int j = 0; j < subBlocks.size(); j++) {
                    blocksInSeq.add(subBlocks.get(ro.get(j)));
                }
                PdfPage pdfPage = new PdfPage(page.getPdfPage(), blocksInSeq, page.getPageNumber());
                reorderedPages.add(pdfPage);
            }
            this.pages = pages;
        } else {
            this.pages = pages;
        }
    }
    
    public PdfArticle(String id, Document doc, List<Block> pageBlocks, ReadingOrder readingOrder, BlockLabeling labeling, DefaultBlockNeighborhood neighborhood) {
    	this.id = id;
        this.readingOrder = readingOrder;
        this.labeling = labeling;
        this.neighborhood = neighborhood;
    	this.pages = new ArrayList<PdfPage>();
    	int i = 0;
    	for(Page page: doc.getPages()) {
    	    List<Integer> ro = readingOrder.getReadingOrder(i);
            List<Block> subBlocks = new ArrayList<Block>(pageBlocks.get(i).getSubBlocks());
            List<Block> blocksInSeq = new ArrayList<Block>(subBlocks.size());
            for (int j = 0; j < subBlocks.size(); j++) {
                blocksInSeq.add(subBlocks.get(ro.get(j)));
            }
    		PdfPage pdfPage = new PdfPage(page, blocksInSeq, i);
    		pages.add(pdfPage);
    		i++;
    	}
    }
    
    /**
     * Returns the pageBlocks.
     * @return the pageBlocks
     */
    public List<PdfPage> getPages() {
        return pages;
    }         

    /**
     * Returns the page that contains the metadata information.
     * @return the page
     */
    public PdfPage getMetadataPage() {
//        return pages.get(0); 
        
        PdfPage result = null;
        for (PdfPage pdfPage : pages) {
            //PdfPage pdfPage = pages.get(0);
            int fragmentCounter = 0;
            for (Block block : pdfPage.getPageBlocks()) {
                fragmentCounter += block.getFragments().size();
            }
            if (fragmentCounter > 500) {
                result = pdfPage;
                break;
            }
            if (result == null) { 
                result = pdfPage; 
            }
        }
        return result;
    }

    /**
     * @return
     */
    public String getId() {
        return id;
    }
    
    /**
     * @return
     */
    public List<String> extractEmails() {
        List<String> result = new ArrayList<String>();
        for (Block block : pages.get(0).getPageBlocks()) {
            String plainText = block.getText();
            Matcher matcher = EMAIL_PATTERN.matcher(plainText);
            while (matcher.find()) {
                String email = plainText.substring(matcher.start(), matcher.end());
                result.add(email);
            }
        }
        return result;
    }

    /**
     * Return all blocks from a given hierarchy with the given label
     * @return all blocks with given label
     */
    private List<Block> getBlocksLabeled(Block b, BlockLabel l) {
    	List<Block> result = new ArrayList<Block>();
    	boolean hasLabel = labeling.getLabel(b, BlockLabel.Unknown).equals(l);
    	if (hasLabel) {
    		result.add(b);
    	}
    	if (b.depth() > 1) {
    		for (Block s : b.getSubBlocks()) {
    			result.addAll(getBlocksLabeled(s, l));
    		}
    	}
    	return result;
    }
    
    /**
     * This will find the first block labeled as a Doi in the detection phase
     * @return doi text
     */
    public String extractDoi() {
        String result = null;
        for (PdfPage page : pages) {
            for (Block block : page.getPageBlocks()) {
            	List<Block> labeledBlocks = getBlocksLabeled(block, BlockLabel.Doi);
            	if (labeledBlocks.size() > 0) {
            		return labeledBlocks.get(0).getText();
            	}
            }        	
        }
        return result;
    }    
    
    /**
     * @return
     */
    public String extractTitle() {
        double maxMeanHeight = Double.NaN;
        Block maxBlock = null;
        
        for (Block block : pages.get(0).getPageBlocks()) {
            List<TextFragment> fragments = block.getFragments();
            if (fragments.size() <= 3) { continue; }
            if (fragments.get(0).getY() < 80) { continue; } 
            
            double height = 0;
            for (TextFragment fragment : fragments) {
                height += fragment.getHeight();
            }
            height /= fragments.size();
            
            if (Double.isNaN(maxMeanHeight) || height > maxMeanHeight) {
                maxMeanHeight = height;
                maxBlock = block;
            }
        }
        
        String result = null;
        if (maxBlock != null) {
            result = maxBlock.getText();
        }
        return result;
    }

    /**
     * @return
     */
    public String extractText() {
        StringBuilder builder = new StringBuilder();
        for (PdfPage p : pages) {
            List<Block> b = p.getPageBlocks();
            if (builder.length() > 0) { builder.append("\n\n"); }
            for (Block block : b) {
                if (builder.length() > 0) { builder.append("\n"); }
                builder.append(block.getText());
            }
        }
        return builder.toString();
    }

    /**
     * Returns the neighborhood.
     * @return the neighborhood
     */
    public DefaultBlockNeighborhood getNeighborhood() {
        return neighborhood;
    }
    
    /**
     * Returns the labeling.
     * @return the labeling
     */
    public BlockLabeling getBlockLabeling() {
        return labeling;
    }
    
    /**
     * Returns the readingOrder.
     * @return the readingOrder
     */
    public ReadingOrder getReadingOrder() {
        return readingOrder;
    }
}
