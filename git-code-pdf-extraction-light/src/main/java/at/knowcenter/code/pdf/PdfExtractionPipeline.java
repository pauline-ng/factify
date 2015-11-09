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
package at.knowcenter.code.pdf;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.analysis.compound.hyphenation.HyphenationException;

import pdfStructure.PDF;
import pdfStructure.PDFtoStructure;
import pdfStructure.Paragraph;
import utility.Debug;
import utility.Debug.DEBUG_CONFIG;
import utility.utility;
import at.knowcenter.code.api.pdf.Block;
import at.knowcenter.code.api.pdf.BlockLabel;
import at.knowcenter.code.api.pdf.Document;
import at.knowcenter.code.api.pdf.PdfParser.PdfParserException;
import at.knowcenter.code.api.pdf.ReadingOrder;
import at.knowcenter.code.api.pdf.TableRegion;
import at.knowcenter.code.pdf.blockclassification.BlockLabeling;
import at.knowcenter.code.pdf.blockclassification.detection.CaptionDetector;
import at.knowcenter.code.pdf.blockclassification.detection.DecorationDetector;
import at.knowcenter.code.pdf.blockclassification.detection.Detector;
import at.knowcenter.code.pdf.blockclassification.detection.DetectorPipeline;
import at.knowcenter.code.pdf.blockclassification.detection.FigureDetector;
import at.knowcenter.code.pdf.blockclassification.detection.HeadingDetector;
import at.knowcenter.code.pdf.blockclassification.detection.MainTextDetector;
import at.knowcenter.code.pdf.blockclassification.detection.TableDetector;
import at.knowcenter.code.pdf.blockclassification.detection.metadata.ArticleMetadataCollector;
import at.knowcenter.code.pdf.blockextraction.clustering.ClusteringPdfBlockExtractor;
import at.knowcenter.code.pdf.blockrelation.geometric.BlockNeighborhood;
import at.knowcenter.code.pdf.blockrelation.geometric.DefaultBlockNeighborhood;
import at.knowcenter.code.pdf.blockrelation.readingorder.ReadingOrderExtractor;
import at.knowcenter.code.pdf.parsing.pdfbox.PdfBoxDocumentParser;
import at.knowcenter.code.pdf.toc.DocumentStructureExtractor;
import at.knowcenter.code.pdf.utils.table.PartitioningTableParser;
import at.knowcenter.code.pdf.utils.table.TableCell;
import at.knowcenter.code.pdf.utils.table.TableException;
import at.knowcenter.code.pdf.utils.table.TableParser;
import at.knowcenter.code.pdf.utils.table.TableUtils;
import at.knowcenter.code.pdf.utils.text.Dehyphenator;
import at.knowcenter.ie.AnnotatedDocument;
import at.knowcenter.ie.Language;
import at.knowcenter.ie.pipelines.AnnotatorPipeline;

/**
 * a basic class for wrapping the pdf extraction workflow.
 * 
 * @author sklampfl
 * 
 */
public class PdfExtractionPipeline {
//	private static Logger log = Logger.getLogger(PdfExtractionPipeline.class.getSimpleName());
	//by huangxc: the following 3 strings are used to share among classes (e.g. DetectorPipeline)
	public static String global_debug_dir = null;
	public static String global_output_dir = null;
	public static String global_id = null;
	
	private  String debug_dir = null;
	private String output_dir = null;
	private String id = null;
	private PDF pdf;
	
	private String doi;
	
	public enum PdfExtractionBackend {
	    PDFBox 
//	    ,
//	    IText,
//	    Poppler,
//	    Mootools,
//	    JPod
	}
	 
	private static final PdfExtractionBackend BACKEND = PdfExtractionBackend.PDFBox;
	
	/**
	 * class holding the pdf extraction result.
	 * 
	 * @author sklampfl
	 *
	 */
	public static class PdfExtractionResult {
		/** the extracted document */
		public final Document doc;
		/** a list of blocks, one for each page */
		public final List<Block> pageBlocks;
		/** the labeling of the blocks */
		public final BlockLabeling labeling;
		/** the reading order including all blocks */
		public final ReadingOrder readingOrder;
		/** the reading order of only those blocks part of the main text */
		public final ReadingOrder postprocessedReadingOrder;
		/**	the block neighborhood */
		public final BlockNeighborhood neighborhood;
		/** the extracted document */
		public final String documentText;
		public final String rawDocumentText;
		/** an annotated document with e.g., token annotations */
		public final AnnotatedDocument annotatedDocument;
		/** The dehypenator */
        public final Dehyphenator dehyphenator;
		
		/**
		 * creates a new pdf extraction result with the given parameters
		 * @param doc
		 * @param pageBlocks
		 * @param labeling
		 * @param readingOrder
		 * @param postprocessedReadingOrder
		 * @param neighborhood
		 * @param dehyphenator 
		 * @param documentText
		 * @param annotatedDocument
		 */
		public PdfExtractionResult(Document doc, List<Block> pageBlocks, BlockLabeling labeling, 
				ReadingOrder readingOrder, ReadingOrder postprocessedReadingOrder,
				BlockNeighborhood neighborhood, Dehyphenator dehyphenator, String documentText, String rawDocumentText, 
				AnnotatedDocument annotatedDocument) {
			this.doc = doc;
			this.pageBlocks = pageBlocks;
			this.labeling = labeling;
			this.readingOrder = readingOrder;
			this.postprocessedReadingOrder = postprocessedReadingOrder;
			this.neighborhood = neighborhood;
            this.dehyphenator = dehyphenator;
			this.documentText = documentText;
			this.rawDocumentText = rawDocumentText;
			this.annotatedDocument = annotatedDocument;
		}

		public Document getDoc() {
			return doc;
		}

		public List<Block> getPageBlocks() {
			return pageBlocks;
		}

		public BlockLabeling getLabeling() {
			return labeling;
		}

		public ReadingOrder getReadingOrder() {
			return readingOrder;
		}
		
        public Dehyphenator getDehyphenator() {
            return dehyphenator;
        }

		public ReadingOrder getPostprocessedReadingOrder() {
			return postprocessedReadingOrder;
		}

		public String getDocumentText() {
			return documentText;
		}

//		public AnnotatedDocument getAnnotatedDocument() {
//			return annotatedDocument;
//		}
	}
	
	protected final DetectorPipeline pipeline;
	
//	private static final String MODEL_DIR = "data/pubmed-10k/";
//	private static final String MODEL_DIR = "data/pubmed-1k-test/";
//  private static final String MODEL_DIR = "data/pubmed-1k-jpod-4/";
//	private static final String MODEL_DIR = "data/pubmed-1k-pdfbox-centroid/";
//	private static final String DEFAULT_BLOCK_MODEL = MODEL_DIR + "block-type-classifier-model.bin";
//	private static final String DEFAULT_FEATURES =  MODEL_DIR + "block-features.arff";
//	private static final String DEFAULT_LANG_MODEL =  MODEL_DIR + "language-model";
//	private static final String DEFAULT_TOKEN_MODEL = MODEL_DIR + "token-classifier-model.bin";
//    private static final String DEFAULT_REFERENCE_MODEL = MODEL_DIR + "references-classifier-model.bin";

    /**
	 * creates a new PDF extraction pipeline
	 * @throws IOException 
	 */
//	public PdfExtractionPipeline() {
//		this(DEFAULT_BLOCK_MODEL, DEFAULT_FEATURES, DEFAULT_TOKEN_MODEL, DEFAULT_LANG_MODEL, DEFAULT_REFERENCE_MODEL);
//		this(DEFAULT_BLOCK_MODEL, DEFAULT_FEATURES, DEFAULT_TOKEN_MODEL, DEFAULT_LANG_MODEL);
//	}
	
	public PdfExtractionPipeline() {
		pipeline = new DetectorPipeline(new DecorationDetector(), 
				   new MainTextDetector(),
				   new HeadingDetector(),
				   new CaptionDetector(),
				   new TableDetector(),
				   new FigureDetector()//,
//				   new BlockMetadataDetector(blockModelFile, languageModelDir, featuresFile),
//				   new TokenMetadataDetector(tokenModelFile, languageModelDir)
		        );
	}

	/**
	 * creates a new pdf extraction pipeline with the given model files
	 * @param blockModelFile
	 * @param featuresFile
	 * @param tokenModelFile
	 * @param languageModelDir
	 */
	public PdfExtractionPipeline(String blockModelFile, String featuresFile, String tokenModelFile,
			String languageModelDir, String referencesModelFile) {
		pipeline = new DetectorPipeline(
				   new DecorationDetector(), 
				   new MainTextDetector(),
				   // new AlgorithmDetector(),
				   new HeadingDetector(),
				   new CaptionDetector(),
				   new TableDetector(),
				   new FigureDetector()//,
//				   new BlockMetadataDetector(blockModelFile, languageModelDir, featuresFile),
//				   new TokenMetadataDetector(tokenModelFile, languageModelDir),
//				   new DoiDetector(),
//				   new ReferenceDetector(referencesModelFile, getDehyphenator())
		        );
	}
	
	/**
	 * Constructor that creates a pipeline without block and token metadata annotation
	 * @param useSimple
	 */
	public PdfExtractionPipeline(boolean useSimple) {
		pipeline = new DetectorPipeline(
				   new DecorationDetector(), 
				   new MainTextDetector(),
				   // new AlgorithmDetector(),
				   new HeadingDetector(),
				   new CaptionDetector(),
				   new TableDetector()
//				   new DoiDetector());
				   );
	}
	
	/**
	 * creates a new pipeline with the given {@link DetectorPipeline}
	 * @param pipeline
	 */
	public PdfExtractionPipeline(DetectorPipeline pipeline) {
		this.pipeline = pipeline;
	}

	/**
	 * parses the document (fonts, fragments) from a file
	 * @param file the input file
	 * @return the parsed document
	 * @throws PdfParserException
	 */
	private Document parseDocument(File file) throws PdfParserException {
		FileInputStream in = null;
		Document document;
		try {
		    switch (BACKEND) {
		    case PDFBox:
		        document = new PdfBoxDocumentParser().parse(file);
		        break;
//		    case IText:
//		        document = new ItextParser().parse(file);
//		        break;
//	        case JPod:
//		        document = new JPodParser().parse(file);
//		        break;
	        default:
	            throw new UnsupportedOperationException("Sorry, currently the pdf backend '" + BACKEND + "' is not supported.");
		    }
			return document;
		} catch(Exception e) {
			throw new PdfParserException("Couldn't parse file '" + file.getPath() + "'", e);
		} finally {
			if(in != null) try { in.close(); } catch(IOException e) { };
		}
	}
	
	/**
	 * parses the document (fonts, fragments) from an input stream
	 * @param in the input stream
	 * @return the parsed document
	 * @throws PdfParserException
	private Document parseDocument(InputStream in) throws PdfParserException {
		return new PdfBoxDocumentParser().parse(in);		
	}
	 */
	
	/**
	 * extracts the text blocks from the given document
	 * @param document the input document
	 * @param id the id (used for logging)
	 * @return a list of blocks corresponding to pages
	 * @throws PdfParserException
	 */
	private List<Block> extractBlocks(Document document, String id) throws PdfParserException {
		ClusteringPdfBlockExtractor clusteringPdfBlockExtractor = new ClusteringPdfBlockExtractor();
		List<Block> blocks = clusteringPdfBlockExtractor.extractBlocks(document, id);
		this.doi = clusteringPdfBlockExtractor.doi;
		return blocks;
	}	

	/**
	 * extracts the neighborhood relation of the blocks on the pages
	 * @param pageBlocks the list of blocks, one for each page
	 * @return the extracted block neighborhood
	 */
	private BlockNeighborhood extractBlockNeighborhood(List<Block> pageBlocks) {
		return new DefaultBlockNeighborhood(pageBlocks, 5f);
	}

	/**
	 * extracts the reading order of all blocks on the pages
	 * @param pageBlocks the list of blocks, one for each page
	 * @return the extracted reading order
	 */
	private ReadingOrder extractReadingOrder(List<Block> pageBlocks) {		
		return new ReadingOrderExtractor(5).evaluateReadingOrder(pageBlocks);
	}

	/**
	 * runs a list of {@link Detector}s that label the blocks
	 * @param doc the {@link Document} the blocks come from.
	 * @param pageBlocks the list of blocks, one for each page in the document
	 * @param labeling the current block labeling, which is modified by this method
	 * @param readingOrder the {@link ReadingOrder} of the blocks
	 * @param articleMetadata 
	 * @param neighbourHood the {@link BlockNeighborhood}
	 */
	protected void runDetectorPipeline(Document document, List<Block> pageBlocks, BlockLabeling labeling, 
			ReadingOrder readingOrder, BlockNeighborhood neighborhood, ArticleMetadataCollector articleMetadata) {	
		pipeline.detect(document, pageBlocks, labeling, readingOrder, neighborhood, articleMetadata);
	}

	/**
	 * deletes blocks from the reading order that are not either main text or headings or abstract.
	 * This method does not change the given reading order, but returns a modified copy.
	 * @param pageBlocks the list of blocks, one for each page in the document
	 * @param labeling the current block labeling, which is modified by this method
	 * @param readingOrder the {@link ReadingOrder} of the blocks
	 * @return the postprocessed reading order
	 */
	private ReadingOrder postprocessReadingOrder(List<Block> pageBlocks, BlockLabeling labeling,
			ReadingOrder readingOrder) {		
		BlockLabel[] blockLabelsToIgnore = new BlockLabel[] { BlockLabel.Decoration };
		BlockLabel[] blockLabelsToInclude = new BlockLabel[] { BlockLabel.Main, BlockLabel.Abstract, BlockLabel.Heading, BlockLabel.Title };
		
		List<List<Integer>> postprocessedReadingOrder = new ArrayList<List<Integer>>(pageBlocks.size());
		for (int i = 0; i < pageBlocks.size(); i++) {
			Block[] blocksOnPage = pageBlocks.get(i).getSubBlocks().toArray(new Block[0]);
			List<Integer> readingOrderOnPage = new ArrayList<Integer>(readingOrder.getReadingOrder(i));
			List<Integer> blocksToRemove = new ArrayList<Integer>(readingOrderOnPage.size());
			for (int j = 0; j < blocksOnPage.length; j++) {
				if (labeling.hasLabel(blocksOnPage[j], blockLabelsToIgnore)) {
					blocksToRemove.add(j);
				}
			}
			List<Integer> blocksToRetain = new ArrayList<Integer>(readingOrderOnPage.size());
			for (int j = 0; j < blocksOnPage.length; j++) {
				if (labeling.hasLabel(blocksOnPage[j], blockLabelsToInclude)) {
					blocksToRetain.add(j);
				}
			}
			readingOrderOnPage.removeAll(blocksToRemove);
			readingOrderOnPage.retainAll(blocksToRetain);
			postprocessedReadingOrder.add(readingOrderOnPage);
		}
		
		boolean insideExcludedHeading = false;
		for (int i = 0; i < pageBlocks.size(); i++) {
			Block[] blocksOnPage = pageBlocks.get(i).getSubBlocks().toArray(new Block[0]);
			List<Integer> readingOrderOnPage = postprocessedReadingOrder.get(i);
			List<Integer> blocksToRemove = new ArrayList<Integer>(readingOrderOnPage.size());
			for (Integer j : readingOrderOnPage) {
				Block block = blocksOnPage[j];
				if (labeling.hasLabel(block, BlockLabel.Heading)) {
					boolean foundExcludedHeading = false;
					for (String heading : DocumentStructureExtractor.headingsToExclude) {
						String text = block.getText();
						text = Normalizer.normalize(text, Normalizer.Form.NFKC);
						if (text.equalsIgnoreCase(heading)) {
							foundExcludedHeading = true;
							insideExcludedHeading = true;
							blocksToRemove.add(j);
							break;
						}
					}
					if (!foundExcludedHeading) {
						insideExcludedHeading = false;
					}
				} else if(insideExcludedHeading) {
					blocksToRemove.add(j);
				}
			}
			readingOrderOnPage.removeAll(blocksToRemove);
		}
		
		
		return new ReadingOrder(postprocessedReadingOrder);
	}

	/**
	 * by huangxc: this function has a bug when clearHyphenations=true
	 * extracts the document text using information from the extracted blocks and reading order
	 * @param pageBlocks the list of blocks, one for each page
	 * @param labeling the block labeling
	 * @param readingOrder the (postprocessed) reading order
	 * @return the extracted document text
	 */
	private String extractDocumentText(List<Block> pageBlocks, BlockLabeling labeling, ReadingOrder readingOrder,
			boolean clearHyphenations) {
		
		List<Block> mainTextLines = new LinkedList<Block>();		

		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < pageBlocks.size(); i++) {
			Block[] blocksOnPage = pageBlocks.get(i).getSubBlocks().toArray(new Block[0]);
			List<Integer> readingOrderOnPage = readingOrder.getReadingOrder(i);
			for (Integer blockId : readingOrderOnPage) {
				Block currentBlock = blocksOnPage[blockId];
				String text = currentBlock.getText();
				text = Normalizer.normalize(text, Normalizer.Form.NFKC);
				
				BlockLabel label = labeling.getLabel(currentBlock);
				if (label!=null) {
					if (label==BlockLabel.Main) {
						mainTextLines.addAll(currentBlock.getSubBlocks());
						if (!clearHyphenations) {
							buffer.append("\n").append(text).append("\n");
						}
					} else if (label==BlockLabel.Heading) {
						if (clearHyphenations && mainTextLines.size()>0) {
							buffer.append(clearHyphenations(mainTextLines)).append("\n");
						}
						mainTextLines.clear();
						buffer.append("\n").append(text).append("\n");
					}
				}
			}
		}
		if (clearHyphenations && mainTextLines.size()>0) {
			buffer.append(clearHyphenations(mainTextLines)).append("\n");
		}
		return buffer.toString();
	}
	/**
	 * by huangxc
	 * @param pageBlocks
	 * @param labeling
	 * @param readingOrder
	 * @param clearHyphenations
	 * @return
	 */
	private List<Block> extractDocumentBody(List<Block> pageBlocks, BlockLabeling labeling, ReadingOrder readingOrder,
			boolean clearHyphenations) {
		
		List<Block> paragrahs_and_heading = new ArrayList<Block>();
		for (int i = 0; i < pageBlocks.size(); i++) {
			Block[] blocksOnPage = pageBlocks.get(i).getSubBlocks().toArray(new Block[0]);
			List<Integer> readingOrderOnPage = readingOrder.getReadingOrder(i);
			for (Integer blockId : readingOrderOnPage) {
				Block currentBlock = blocksOnPage[blockId];
				String text = currentBlock.getText();
				text = Normalizer.normalize(text, Normalizer.Form.NFKC);
				BlockLabel label = labeling.getLabel(currentBlock);
				//by huangxc
				if(label == BlockLabel.Main || label == BlockLabel.Heading) paragrahs_and_heading.add(currentBlock);
			}
		}
		return paragrahs_and_heading;
	}

	public String clearHyphenations(List<Block> mainTextLines) {
		Dehyphenator dehyphenator = getDehyphenator();
		
		if (mainTextLines.size() == 0) {
			return "";
		}
		
		StringBuilder buffer = new StringBuilder();
		Iterator<Block> iterator = mainTextLines.iterator();
		Block currentLineBlock = iterator.next();
		Block tempBlock = null;
		while (iterator.hasNext()) {
			Block nextLineBlock = iterator.next();
			String currentLineText = currentLineBlock.getText();
			String nextLineText = nextLineBlock.getText();
			currentLineText = Normalizer.normalize(currentLineText, Normalizer.Form.NFKC);
			nextLineText = Normalizer.normalize(nextLineText, Normalizer.Form.NFKC);
			buffer.append(currentLineText);
			if (currentLineText.endsWith(Character.toString(Dehyphenator.HYPHENATION_CHAR))) {
				String[] currentTokens = currentLineText.split("\\W");
				String[] nextTokens = nextLineText.split("\\W");
				if (currentTokens.length>0 && nextTokens.length>0) {
					String part1 = currentTokens[currentTokens.length-1];
					String part2 = nextTokens[0];
					if (dehyphenator.checkHyphenation(part1, part2)) {
						buffer.deleteCharAt(buffer.length()-1);
						while(!buffer.toString().endsWith(part1)) {
							buffer.deleteCharAt(buffer.length() - 1);
						}
					}
				}
			} else {
				buffer.append(' ');
			}
			tempBlock = currentLineBlock;
			currentLineBlock = nextLineBlock;
			nextLineBlock = tempBlock;
		}
		buffer.append(currentLineBlock.getText());
		return buffer.toString();
	}

	/**
	 * utility method for getting the dehyphenated text content of a block
	 * @param block the block
	 * @return the text of the given block with hyphenations removed
	 */
//	private String clearHyphenations(Block block) {
//		List<Block> mainTextLines = new ArrayList<Block>();
//		mainTextLines.addAll(block.getLineBlocks());
//		return clearHyphenations(mainTextLines);
//	}


	/**
	 * runs the pipeline on a pdf file and returns a pdf extraction result
	 * @param id the id (used for logging)
	 * @param file the input pdf file
	 * @return the pdf extraction result
	 * @throws PdfParserException
	 * @throws  
	 */
	public PdfExtractionResult runPipeline(File file)  {
		try{
			if(file.exists()) {
				if(checkParameterSetting())
				
					return runPipeline(parseDocument(file));
				return null;
			}else{ 
				Debug.print("File " + file.getAbsolutePath() + " does not exist!", DEBUG_CONFIG.debug_error);
				return null;
			}
		}
		catch (PdfParserException e) {
			e.printStackTrace();
			return null;
		}
	}
	

	/**
	 * runs the pipeline on an input stream and returns a pdf extraction result
	 * @param id the id (used for logging)
	 * @param in the input stream
	 * @return the pdf extraction result
	 * @throws PdfParserException
	public PdfExtractionResult runPipeline(String id, InputStream in) throws PdfParserException {
		return runPipeline(id, parseDocument(in));
	}
	 * @throws TableException 
	 */
	

	private PdfExtractionResult runPipeline( Document document)  {		
		try{
//		log.info("Extracting blocks...");
		List<Block> pageBlocks = extractBlocks(document, id);
		{
            if(Debug.get(DEBUG_CONFIG.debug_textpieces)) {
//        	String debug_output_location = PdfExtractionPipeline.global_debug_dir + PdfExtractionPipeline.global_id + "_textpieces_location.txt";
        	String debug_output_text = PdfExtractionPipeline.global_debug_dir + PdfExtractionPipeline.global_id + "_textpieces_text.txt";
        	utility util = new utility();
        	util.writeFile(debug_output_text, "", false);
//        	util.writeFile(debug_output_location, "", false);
        	int counter = 0;
        	for(int i = 0; i < pageBlocks.size(); i++) {
        		for(int j = 0; j < pageBlocks.get(i).getLineBlocks().size(); j++) {
        			util.writeFile(debug_output_text, counter + "\t" + pageBlocks.get(i).getLineBlocks().get(j).getText() + "\r\n", true);
        			//        		util.writeFile(debug_output_text, counter + "\t" + pdfPage.getFragments().get(i).getText() + "\r\n", true);
        			counter++;
        		}
        	}
            }
		}
//		log.info("Extracting reading order...");
		ReadingOrder readingOrder = extractReadingOrder(pageBlocks);
		BlockNeighborhood neighborhood = extractBlockNeighborhood(pageBlocks);
		BlockLabeling labeling = new BlockLabeling();
		ArticleMetadataCollector articleMetadata = new ArticleMetadataCollector();
		
//		log.info("Running detector pipeline...");
		runDetectorPipeline(document, pageBlocks, labeling, readingOrder, neighborhood, articleMetadata);
		
//		log.info("Extracting tables...");
		extractTables(document, articleMetadata);
		
		List<Paragraph> paragraphs = new ArrayList<Paragraph>();
		AnnotatedDocument annotatedDocument = new AnnotatedDocumentBuilder(getAnnotatorPipeline(), getDehyphenator()).
				build(document, pageBlocks, labeling, readingOrder, neighborhood, articleMetadata, paragraphs);
		
		{
//			List<Paragraph> post_paragraphs = new ArrayList<Paragraph>();
//			path = "data/960_PR2_linecollectors.txt";writeFile(path, "", false);
//			for(Paragraph ad : paragraphs) {
//				LineCollector lc = ad.t;
//				if(lc != null && lc.lines != null)
//				for(Block line : lc.lines) {//now break to paragraphs
//					
//					
//					writeFile(path, line.getText()+ "--\r\n", true);
//				}
//			}
		}
		{
//			String path = global_path + "_new_paragraphs_1.txt";
//			PDFtoStructure converter = new PDFtoStructure();
//			List<Paragraph> new_paragraphs = converter.convert(paragraphs);
//			writeFile(path, "", false);
//			for(Paragraph para : new_paragraphs) {
//				writeFile(path, "----" + para.label+ "--Page " + para.pages + "--" + para.remark + "----------------\r\n", true);
//				writeFile(path, para.toString() + "---------------------------\r\n", true);
//			}
		}
		
		{
//			String path = global_path + "_new_paragraphs_0.txt";
//			path = global_path + "_news_paragraphs.txt";
//			writeFile(path, "", false);
//			for(Paragraph ad : paragraphs) {
//				writeFile(path, "----" + ad.label+ "--------------------\r\n", true);
//				writeFile(path, ad.text + "---------------------------\r\n", true);
//			}

		}
		
//		extractCitations(annotatedDocument, articleMetadata);
		
//		log.info("Extracting document text...");
		ReadingOrder postprocessedReadingOrder = postprocessReadingOrder(pageBlocks, labeling, readingOrder);
		String text = extractDocumentText(pageBlocks, labeling, postprocessedReadingOrder, true);		
		String rawText = extractDocumentText(pageBlocks, labeling, postprocessedReadingOrder, false);		
		{
			writeFile(debug_dir + id + "_body_standard.txt", text, false);
			//writeFile(debug_dir + id + "_body_standard_rawtext.txt", rawText, false);
		}
		{
			String path = debug_dir + id + "_new_paragraphs_3.txt";
			List<Block> blocks_body_and_heading = extractDocumentBody(pageBlocks, labeling, postprocessedReadingOrder, true);
			PDFtoStructure pdftoStructure = new PDFtoStructure();
			List<Paragraph> body_and_heading = pdftoStructure.convert(blocks_body_and_heading, labeling, this);
			//writeFile(path, "", false);
			//for(Paragraph para : body_and_heading) {
				//writeFile(path, "----" + para.label+ "--Page " + para.pages + "--" + para.remark + "----------------\r\n", true);
				//writeFile(path, para.text + "---------------------------\r\n", true);
			//}
			
		
		this.pdf = new PDF();
		pdf.body_and_heading = body_and_heading;
		pdf.htmlTables = articleMetadata.getHtmlTables();
		pdf.doi = this.doi;
//		return pdf;
		}
		return new PdfExtractionResult(document, pageBlocks, labeling, readingOrder, 
				postprocessedReadingOrder, neighborhood, getDehyphenator(), text, rawText, annotatedDocument);
}
catch(PdfParserException e) {
	
	return null;}
	}

    private AnnotatorPipeline getAnnotatorPipeline() throws PdfParserException {
		AnnotatorPipeline annotatorPipeline = null;
    	try {
//    		annotatorPipeline = new AnnotatorPipeline();
    		annotatorPipeline = new AnnotatorPipeline(Language.English);
//    		annotatorPipeline.addAnnotator(Language.English, new SentenceAnnotator(Language.English));
    	} catch (IOException e) {
    		throw new PdfParserException("Couldn't create AnnotatorPipeline", e);
    	}
		return annotatorPipeline;
	}

	private Dehyphenator getDehyphenator() {
		Dehyphenator dehyphenator = null;
		try {
			dehyphenator = new Dehyphenator(1, 1);
		} catch (HyphenationException e) {
			throw new RuntimeException("Couldn't create Dehyphenator", e);
		}
		return dehyphenator;
	}

	private void extractTables(Document document, ArticleMetadataCollector articleMetadata) {
		TableParser parser = new PartitioningTableParser();
		for (TableRegion table : document.getTables()) {
			try {
				TableCell[][] cells = parser.parseTable(table);
				String htmlTable = TableUtils.createHtmlTable(cells);
				articleMetadata.addHtmlTable(table, htmlTable);
			} catch (TableException e) {
				throw new RuntimeException("Error parsing table "+table.captionBlock.getText(), e);
			}
		}
	}
	
    /**
     * @param annotatedDocument
     * @param articleMetadata
     * @throws PdfParserException 
     */
//    private void extractCitations(AnnotatedDocument annotatedDocument,
//            ArticleMetadataCollector articleMetadata) throws PdfParserException {
//        try {
//            CitationAnnotator citationExtractor = new CitationAnnotator(articleMetadata);
//            citationExtractor.annotate(annotatedDocument);
//        } catch (IOException e) {
//            throw new PdfParserException("Caused by IOException", e);
//        }
//    }

    public static void main(String[] args) throws PdfParserException {
//    	if(args.length != 1) {
//    		System.out.println("Please specify 1 parameter!");
//    		return;
//    	}
//    	String filePath = args[0];
//    	if(!new File(filePath).exists()) {
//    		System.out.println("Path: \"" + new File(filePath).getAbsolutePath()+ "\" does not exist!");
//    		return;
//    	}
//    	try {
    		PdfExtractionPipeline pipeline = new PdfExtractionPipeline();
//    		String filePath = "../git-BEL_extractor-test/data/960_PR2.pdf";
    		String filePath = args[0];
    		File file = new File(filePath);
    		String fileName = file.getName();
    		if(!pipeline.setParameter(fileName.endsWith(".pdf") ? fileName.substring(0, fileName.length() - 4) : fileName,
    				file.getParent() == null ?  "output/" : file.getParent() + "/output/",
    				file.getParent() == null ?  "debug_output/" : file.getParent() + "/debug_output/"))
    			return ;
    		pipeline.runPipeline(file);
	}

//	@Override
//	public PdfExtractorResult extract(File pdf)
//			throws PdfParserException {
//		final PdfExtractionResult result = runPipeline(pdf);
//		return new PdfExtractorResult(result.doc, result.annotatedDocument);
//	}
//	
	
//	public static void print(Document doc, List<Block> pageBlocks, BlockLabeling labeling,
//			ReadingOrder readingOrder, String output) {
//		writeFile(output,"",false);
//		for (int i = 0; i < pageBlocks.size(); i++) {
//			Block pageBlock = pageBlocks.get(i);
//			List<Integer> ro = readingOrder.getReadingOrder(i);
//			List<Block> blocks = new ArrayList<Block>(pageBlock.getSubBlocks());
//			for (int j = 0; j < ro.size(); j++) {            	
//				Block currentBlock = blocks.get(ro.get(j));
//				BlockLabel label = labeling.getLabel(currentBlock);
//				writeFile(output, "-------" + (label == null? "NULL" : label.getLabel()) + "----------\r\n",true);
//				writeFile(output, currentBlock.getText() + "\r\n\r\n",true);
//
//			}
//		}
//	}
	public static void writeFile(String path, String s, boolean append) {
		File log_f;
		log_f = new File(path);
		Writer out; 
		try {
			
			if(!log_f.exists()) {
				Path pathToFile = Paths.get(path);
				Path parent = Files.createDirectories(pathToFile.getParent());
				Path current = Files.createFile(pathToFile);
				if(parent == null || current== null) {
					Debug.print("Failed to create file " + path, DEBUG_CONFIG.debug_error);
					return;
				}
			}
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(new File(path), append), "UTF-8"));
//			out = new BufferedWriter(new FileWriter(new File(path), append));
			out.append(s);
			out.flush();
			out.close();
			
		}
		catch(Exception e) {
			Debug.print("Failed to create file " + path, DEBUG_CONFIG.debug_error);
			e.printStackTrace();
			
		}
		
//		try {
//			PrintWriter writer;
//			writer = new PrintWriter(path, "UTF-8");
//			writer.println(s);
////			writer.println("BEL");
//			writer.close();
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (UnsupportedEncodingException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
	}
	private boolean checkParameterSetting() {
		if(this.id == null || this.debug_dir == null || this.output_dir == null) {
			Debug.print("Please specify pipeline's parameters: id, debug_dir, and output_dir", DEBUG_CONFIG.debug_error);
			return false;
		}
		{
			File file = new File(this.debug_dir);
			if(!file.exists()) {
				Path pathToFile = Paths.get(file.getAbsolutePath());
				try {
					if(Files.createDirectories(pathToFile) == null ){
						Debug.print("Failed to create folder " + file.getAbsolutePath(), DEBUG_CONFIG.debug_error);
						return false;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Debug.print("Failed to create folder " + file.getAbsolutePath(), DEBUG_CONFIG.debug_error);
					return false;
				}	
			}
		}
		{
			File file = new File(this.output_dir);
			if(!file.exists()) {
				Path pathToFile = Paths.get(file.getAbsolutePath());
				try {
					if(Files.createDirectories(pathToFile) == null ){
						Debug.print("Failed to create folder " + file.getAbsolutePath(), DEBUG_CONFIG.debug_error);
						return false;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Debug.print("Failed to create folder " + file.getAbsolutePath(), DEBUG_CONFIG.debug_error);
					return false;
				}	
			}
		}
		global_debug_dir = debug_dir;
		global_id = id;
		global_output_dir = output_dir;
		return true;
	}
	/**
	 * 
	 * @param args
	 * 0: id
	 * 1: output_dir
	 * 2: debug_dir
	 * @return
	 */
	public boolean setParameter(String...args) {
		if(args.length < 2) {
			Debug.print("Please set 3 parameters for PDFExtractionPipeline: id, debug_dir, and output_dir." , DEBUG_CONFIG.debug_error);
			return false;
		}
		
		this.id = args[0];
		this.output_dir = args[1];
		if(args.length > 2) this.debug_dir = args[2];
		else this.debug_dir = this.output_dir;
		if(checkParameterSetting())
			return true;
		return false;
	}
	
	public PDF getPDF(){
		return this.pdf;
	}
 }
