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

import java.io.Serializable;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import pdfStructure.Paragraph;
import nlp.StanfordNLPLight;
import utility.Span;
import at.knowcenter.code.api.pdf.Block;
import at.knowcenter.code.api.pdf.BlockLabel;
import at.knowcenter.code.api.pdf.Document;
import at.knowcenter.code.api.pdf.ImageRegion;
import at.knowcenter.code.api.pdf.Page;
import at.knowcenter.code.api.pdf.PdfAnnotations;
import at.knowcenter.code.api.pdf.PdfAnnotations.BoundingBoxList;
import at.knowcenter.code.api.pdf.PdfAnnotations.PageAnnotation;
import at.knowcenter.code.api.pdf.PdfAnnotations.ScientificArticleMetadataAnnotation;
import at.knowcenter.code.api.pdf.PdfAnnotations.ScientificArticleStructureAnnotation;
import at.knowcenter.code.api.pdf.PdfAnnotations.TokenAnnotation;
import at.knowcenter.code.api.pdf.ReadingOrder;
import at.knowcenter.code.api.pdf.TableRegion;
import at.knowcenter.code.pdf.blockclassification.BlockLabeling;
import at.knowcenter.code.pdf.blockclassification.detection.metadata.ArticleMetadataCollector;
import at.knowcenter.code.pdf.blockrelation.geometric.BlockNeighborhood;
import at.knowcenter.code.pdf.toc.DocumentStructure;
import at.knowcenter.code.pdf.toc.DocumentStructureExtractor;
import at.knowcenter.code.pdf.toc.ParagraphInformation;
import at.knowcenter.code.pdf.utils.text.Dehyphenator;
import at.knowcenter.ie.AnnotatedDocument;
import at.knowcenter.ie.BaseTypeSystem;
import at.knowcenter.ie.Language;
import at.knowcenter.ie.impl.DefaultDocument;
import at.knowcenter.ie.pipelines.AnnotatorPipeline;
import at.knowcenter.ie.tools.AnnotatedDocumentUtils;
import at.knowcenter.util.typedkey.TypedKey;

/**
 * Builder for annotated documents out of the detected blocks.
 * 
 * @author rkern@know-center.at
 */
public class AnnotatedDocumentBuilder {

	private final AnnotatorPipeline annotatorPipeline;
	private final Dehyphenator dehyphenator;
//	private final SentenceDetectorME sentenceDetector;

	/**
	 * Creates a new builder instance.
	 * 
	 * @param annotatorPipeline the annotator pipeline for text blocks
	 * @param dehyphenator the dehypenator to un-hyphen words
	 */
	public AnnotatedDocumentBuilder(AnnotatorPipeline annotatorPipeline,
			Dehyphenator dehyphenator) {
		this.annotatorPipeline = annotatorPipeline;
		this.dehyphenator = dehyphenator;
//		try {
//			InputStream in = ResourceLoader.getInputStream(SentenceAnnotator.class, "en-sent.bin" );		
//			SentenceModel sentenceModel = new SentenceModel(in);		
//			sentenceDetector = new SentenceDetectorME(sentenceModel);
//		} catch (Exception e) {
//			throw new RuntimeException("Cannot load the sentence detector", e);
//		}
	}

	/**
	 * Builds an annotated document out of a list of blocks.
	 * @param doc
	 * @param pageBlocks
	 * @param readingOrder
	 * @param neighborhood
	 * @param articleMetadata 
	 * @return the newly created annotated document
	 */
	public AnnotatedDocument build(Document doc, List<Block> pageBlocks, BlockLabeling labeling,
			ReadingOrder readingOrder, BlockNeighborhood neighborhood, ArticleMetadataCollector articleMetadata, List<Paragraph> paras) {
	    DocumentStructure documentStructure = new DocumentStructureExtractor().detectDocumentStructure(
	            pageBlocks, labeling, readingOrder, doc.getFonts());
	    ParagraphInformation paragraphInformation = documentStructure.getParagraphInformation();
	    
    	AnnotationCollector collector = new AnnotationCollector(dehyphenator, paragraphInformation, labeling);
        for (int i = 0; i < pageBlocks.size(); i++) {
        	Page page = doc.getPages().get(i);
            Block pageBlock = pageBlocks.get(i);
            List<Integer> ro = readingOrder.getReadingOrder(i);
            List<Block> blocks = new ArrayList<Block>(pageBlock.getSubBlocks());
            for (int j = 0; j < ro.size(); j++) {            	
                Block currentBlock = blocks.get(ro.get(j));
                BlockLabel label = labeling.getLabel(currentBlock);
                
                if (label == BlockLabel.Title || label == BlockLabel.Subtitle || label == BlockLabel.Journal) {
                	addMetadataBlock(currentBlock, label, documentStructure, doc, page, collector);
                } else if (label==BlockLabel.Affiliations || label==BlockLabel.Authors ||
                		label==BlockLabel.AuthorsMixed || label==BlockLabel.Emails) {
                	addMetadataBlock(currentBlock, label, documentStructure, doc, page, collector);
                } else if (label==BlockLabel.Abstract) {
                	addTextBlock(currentBlock, label, doc, page, collector);
                } else if (label==BlockLabel.Heading) {
                	addHeadingBlock(currentBlock, label, documentStructure, doc, page, collector);
                } else if (label==BlockLabel.Main) {
                	addTextBlock(currentBlock, label, doc, page, collector);
                } else if (label==BlockLabel.Caption) {
                	addCaptionBlock(currentBlock, label, doc, page, articleMetadata, collector);
//                	addCaptionBlock(currentBlock, label, doc, page, collector);
                } else if (label==BlockLabel.Sparse) {
                	addGenericBlock(currentBlock, label, documentStructure, doc, page, collector);
                } else if (label==null) {
                	addGenericBlock(currentBlock, label, documentStructure, doc, page, collector);
                } else if (label==BlockLabel.Decoration) {
                } 
            }
        }
        
//        collector.addReferences(articleMetadata.getReferences());
//        paragraphs_output.addAll(collector.getDocuments());
        paras.addAll(collector.paragraphs);
        return AnnotatedDocumentUtils.concat(collector.getDocuments());
    }
    
    private void addCaptionBlock(final Block captionBlock, final BlockLabel label, 
    		final Document doc, final Page page, final ArticleMetadataCollector articleMetadata,
//    		final Document doc, final Page page,
    		AnnotationCollector collector) {
		collector.processBlocks(captionBlock, label, page, new Callback() {
            @Override
            public void onFinished(AnnotatedDocument document) {
                Map<TypedKey<BaseTypeSystem, ? extends Serializable>, Serializable> features = new HashMap<TypedKey<BaseTypeSystem,? extends Serializable>, Serializable>();
                
                TableRegion foundTable = null;                
                for (TableRegion table : doc.getTables()) {
                	if (table.captionBlock == null) {
                		continue;
                	}
                	if (table.captionBlock.equals(captionBlock)) {
                		foundTable = table;
                		break;
                	}
                }
                
                if(foundTable != null) {
                    features.put(PdfAnnotations.Table, foundTable);
	                features.put(BaseTypeSystem.DocumentStructureFeatures.StructureType, BaseTypeSystem.DocumentStructureFeatures.StructureTypeValue.Paragraph);
	                features.put(BaseTypeSystem.DocumentStructureFeatures.StructureLevel, PdfAnnotations.HeadingLevelTableCaption);
//	                features.put(PdfAnnotations.HtmlTable, articleMetadata.getHtmlTables().get(foundTable)); //commented by huangxc
                }
//                features.put(PdfAnnotations.BoundingBoxList, new BoundingBoxList(captionBlock.getBoundingBox()));
//                features.put(PdfAnnotations.Page, new Page.PageInfo(page));                
//                document.newAnnotation(0, document.getText().length(), BaseTypeSystem.DocumentStructure, features);

                ImageRegion foundImage = null;                
                for (ImageRegion image : doc.getImages()) {
                	if (image.captionBlock.equals(captionBlock)) {
                		foundImage = image;
                		break;
                	}
                }
                
                if(foundImage != null) {
	                features.put(PdfAnnotations.Image, foundImage);
	                features.put(BaseTypeSystem.DocumentStructureFeatures.StructureType, BaseTypeSystem.DocumentStructureFeatures.StructureTypeValue.Paragraph);
	                features.put(BaseTypeSystem.DocumentStructureFeatures.StructureLevel, PdfAnnotations.HeadingLevelImageCaption);
                }
                features.put(PdfAnnotations.BoundingBoxList, new BoundingBoxList(captionBlock.getBoundingBox()));
                features.put(PdfAnnotations.Page, new Page.PageInfo(page));                
                document.newAnnotation(0, document.getText().length(), BaseTypeSystem.DocumentStructure, features);
                annotatorPipeline.annotate(document, Language.English);

            }
		});
	}

    private void addHeadingBlock(final Block headingBlock, final BlockLabel label, 
    		final DocumentStructure documentStructure,
    		final Document doc, final Page page, AnnotationCollector collector) {
		collector.processBlocks(headingBlock, label, page, new Callback() {
            @Override
            public void onFinished(AnnotatedDocument document) {
                Map<TypedKey<BaseTypeSystem, ? extends Serializable>, Serializable> features = new HashMap<TypedKey<BaseTypeSystem,? extends Serializable>, Serializable>();
                features.put(BaseTypeSystem.DocumentStructureFeatures.StructureType, BaseTypeSystem.DocumentStructureFeatures.StructureTypeValue.Paragraph);
                features.put(BaseTypeSystem.DocumentStructureFeatures.StructureLevel, 
                        getStrucutureLevel(headingBlock, documentStructure));
                features.put(PdfAnnotations.BoundingBoxList, new BoundingBoxList(headingBlock.getBoundingBox()));
                features.put(PdfAnnotations.Page, new Page.PageInfo(page));
                document.newAnnotation(0, document.getText().length(), BaseTypeSystem.DocumentStructure, features);
            }
		});
	}

	private void addMetadataBlock(final Block metadataBlock, final BlockLabel label, 
			final DocumentStructure documentStructure, 
			final Document doc, final Page page, AnnotationCollector collector) {
		collector.processBlocks(metadataBlock, label, page, new Callback() {
            @Override
            public void onFinished(AnnotatedDocument document) {
                Map<TypedKey<BaseTypeSystem, ? extends Serializable>, Serializable> features = new HashMap<TypedKey<BaseTypeSystem,? extends Serializable>, Serializable>();
                features.put(BaseTypeSystem.DocumentStructureFeatures.StructureType, BaseTypeSystem.DocumentStructureFeatures.StructureTypeValue.Paragraph);
                features.put(BaseTypeSystem.DocumentStructureFeatures.StructureLevel, 
                        getStrucutureLevel(metadataBlock, documentStructure));
                features.put(PdfAnnotations.BoundingBoxList, new BoundingBoxList(metadataBlock.getBoundingBox()));
                features.put(PdfAnnotations.Page, new Page.PageInfo(page));
                String text = document.getText();
                int start = 0, end = 0;
                int length = text.length();
                do {
                    int indexOf = text.indexOf('\n', start);
                    if (indexOf >= 0) {
                        end = indexOf;
                    } else {
                        end = length;
                    }
                    if (end > start) {
                        document.newAnnotation(start, end, BaseTypeSystem.DocumentStructure, features);
                    }
                    start = end+1;
                } while (end < length);
                
//                if (blockIsNamedEntity) {
//                    features = new HashMap<TypedKey<BaseTypeSystem,? extends Serializable>, Serializable>();
//                    features.put(BaseTypeSystem.NamedEntityFeatures.EntityClass, label==null?"null":label.getLabel());
//                    document.newAnnotation(0, document.getText().length(), BaseTypeSystem.NamedEntity, features);
//                }
            }
		});
	}

	private void addGenericBlock(final Block metadataBlock, final BlockLabel label, 
			final DocumentStructure documentStructure, 
			final Document doc, final Page page, AnnotationCollector collector) {
		collector.processBlocks(metadataBlock, label, page, new Callback() {
            @Override
            public void onFinished(AnnotatedDocument document) {
                Map<TypedKey<BaseTypeSystem, ? extends Serializable>, Serializable> features = new HashMap<TypedKey<BaseTypeSystem,? extends Serializable>, Serializable>();
                features.put(BaseTypeSystem.DocumentStructureFeatures.StructureType, BaseTypeSystem.DocumentStructureFeatures.StructureTypeValue.Paragraph);
                features.put(BaseTypeSystem.DocumentStructureFeatures.StructureLevel, 
                        getStrucutureLevel(metadataBlock, documentStructure));
                features.put(PdfAnnotations.BoundingBoxList, new BoundingBoxList(metadataBlock.getBoundingBox()));
                features.put(PdfAnnotations.Page, new Page.PageInfo(page));
                document.newAnnotation(0, document.getText().length(), BaseTypeSystem.DocumentStructure, features);
                annotatorPipeline.annotate(document, Language.English);
            }
		});
	}

    private void addTextBlock(final Block textBlock, final BlockLabel label, 
    		final Document doc, final Page page, AnnotationCollector collector) {
		collector.processBlocks(textBlock, label, page, new Callback() {
            @Override
            public void onFinished(AnnotatedDocument document) {
                Map<TypedKey<BaseTypeSystem, ? extends Serializable>, Serializable> features = new HashMap<TypedKey<BaseTypeSystem,? extends Serializable>, Serializable>();
                features.put(BaseTypeSystem.DocumentStructureFeatures.StructureType, BaseTypeSystem.DocumentStructureFeatures.StructureTypeValue.Paragraph);
                String level = "";
                if (label==BlockLabel.Abstract) {
                    level = label.getLabel();
                }
                features.put(BaseTypeSystem.DocumentStructureFeatures.StructureLevel, level);
                features.put(PdfAnnotations.BoundingBoxList, new BoundingBoxList(textBlock.getBoundingBox()));
                features.put(PdfAnnotations.Page, new Page.PageInfo(page));
                document.newAnnotation(0, document.getText().length(), BaseTypeSystem.DocumentStructure, features);
                annotatorPipeline.annotate(document, Language.English);
            }
		});
	}
	
    private String getStrucutureLevel(Block block, DocumentStructure documentStructure) {
        String result = "";
        Integer headingLevel = documentStructure.getHeadingLevel(block);
        if (headingLevel != null) {
            switch (headingLevel) {
            case 0: result = PdfAnnotations.HeadingLevelTitle; break;
            case 1: result = PdfAnnotations.HeadingLevelSection; break;
            case 2: result = PdfAnnotations.HeadingLevelSubSection; break;
            case 3: result = PdfAnnotations.HeadingLevelSubSubSection; break;
            case 4: result = PdfAnnotations.HeadingLevelParagraph; break;
            }
        }
        return result;
    }
    
   public static final class LineCollector {
    	public final List<Block> lines = new ArrayList<Block>();
    	public final Map<Block, Block> lineToBlock = new HashMap<Block, Block>();
    	public final Map<Block, Page> lineToPage = new HashMap<Block, Page>();
		public final Callback callback;
    	
    	public LineCollector(Callback callback) {
			this.callback = callback;
		}

		public void add(Block lineBlock, Block block, Page page) {
        	lines.add(lineBlock);
        	lineToBlock.put(lineBlock, block);
        	lineToPage.put(lineBlock, page);
		}
		
//		public LineCollector copy() {
//			LineCollector lc = new LineCollector(this.callback);
//			lc.lines.addAll(this.lines);
//			for(Entry<Block>)
//			return lc;
//		}
    }
    
    
    static final class AnnotationCollector {
        private final Dehyphenator dehyphenator;
//        private final SentenceDetectorME sentenceDetector;
        private final ParagraphInformation paragraphInformation;
        private final Map<BlockLabel, LineCollector> textBlockLineList = new LinkedHashMap<BlockLabel, LineCollector>();
        private final BlockLabeling labeling;
        List<AnnotatedDocument> documents = new ArrayList<AnnotatedDocument>();
        List<Paragraph> paragraphs = new ArrayList<Paragraph>();
        /**
         * Creates a new instance of this class.
         * @param dehyphenator 
         * @param paragraphInformation 
         * @param labeling 
         */
        public AnnotationCollector(Dehyphenator dehyphenator, ParagraphInformation paragraphInformation, BlockLabeling labeling) {
            this.dehyphenator = dehyphenator;
//			this.sentenceDetector = sentenceDetector;
			this.paragraphInformation = paragraphInformation;
			this.labeling = labeling;
        }
        
//        public void addReferences(List<DetectedReference> references) {
//        	StringBuilder builder = new StringBuilder();
//        	List<ScientificArticleMetadataAnnotation> metadataAnnotations = new ArrayList<ScientificArticleMetadataAnnotation>();
//        	List<ScientificArticleStructureAnnotation> structureAnnotations = new ArrayList<ScientificArticleStructureAnnotation>();
//        	
//        	builder.append("\nReferences\n");
//        	for (DetectedReference detectedReference : references) {
//        		int startReference = builder.length();
//        		builder.append("* ");
//        		List<ReferenceToken> tokens = detectedReference.getTokens();
//        		List<ReferenceTokenTypes> tokenTypes = detectedReference.getTokenTypes();
//        		ReferenceTokenTypes prevType = null;
//        		ScientificArticleMetadataAnnotation prevSa = null;
//        		for (int i = 0; i < tokens.size(); i++) {
//					ReferenceToken token = tokens.get(i);
//					ReferenceTokenTypes type = tokenTypes.get(i);
//					int start = builder.length();
//					builder.append(token.getText());
//					int end = builder.length();
//					builder.append(" ");
//					ScientificArticleMetadataAnnotation sa;
//					if (type == prevType && prevSa != null) {
//					    sa = prevSa;
//					    prevSa.end = end;
//					} else {
//                        sa = new ScientificArticleMetadataAnnotation("ref-"+type.name(), start, end, null, null);
//                        metadataAnnotations.add(sa);
//					}
//					prevType = type;
//					prevSa = sa;
//				}
//        		int endReference = builder.length();
//        		List<Block> lines = detectedReference.getLines();
//        		List<BoundingBox> bboxes = new ArrayList<BoundingBox>(lines.size());
//        		Page page = null;
//        		for (Block line : lines) {
//        			if (page == null) {
//        				page = line.getPage();
//        			}
//        			if (line.getPage() == page) {
//        				bboxes.add(line.getBoundingBox());
//        			}
//        		}
//        		BoundingBoxList bboxList = new BoundingBoxList(bboxes.toArray(new BoundingBox[bboxes.size()]));
//        		structureAnnotations.add(new ScientificArticleStructureAnnotation("reference", startReference, endReference,
//        				bboxList, page));
//        		builder.append("\n");
//        	}
//        	
//        	builder.append("\n");
//            
//            DefaultDocument document = new DefaultDocument();
//            document.setText(builder.toString());
//            
//            for (ScientificArticleStructureAnnotation annotation : structureAnnotations) {
//            	// TypedKey<BaseTypeSystem, String> key = TypedKey.createKey(BaseTypeSystem.class, annotation.type, String.class);
//                // document.newAnnotation(annotation.start, annotation.end, key);
//                document.newAnnotation(annotation.start, annotation.end, PdfAnnotations.ScientificArticleStructure, annotation.features);
//            }
//            for (ScientificArticleMetadataAnnotation annotation : metadataAnnotations) {
//            	// TypedKey<BaseTypeSystem, String> key = TypedKey.createKey(BaseTypeSystem.class, annotation.type, String.class);
//                // document.newAnnotation(annotation.start, annotation.end, key);
//                document.newAnnotation(annotation.start, annotation.end, PdfAnnotations.ScientificArticleMetadata, annotation.features);
//            }
//            documents.add(document);
//		}

		private void processBlocks(Block block, BlockLabel label, Page page, Callback callback) {
        	boolean isSplitBlocks = paragraphInformation != null && (label == BlockLabel.Abstract || label == BlockLabel.Main); 
        	LineCollector lineCollector = textBlockLineList.remove(label);
        	if (lineCollector == null) {
        		lineCollector = new LineCollector(callback);
        	}
        	
        	List<Block> lineBlocks = block.getLineBlocks();
        	if (isSplitBlocks) {
            	// flush all block not of current label (e.g. abstract before main text)
            	for (Iterator<Entry<BlockLabel, LineCollector>> iter = textBlockLineList.entrySet().iterator(); iter.hasNext(); ) {
            		Entry<BlockLabel, LineCollector> next = iter.next();
            		if (next.getKey() != label) {
            			Paragraph para = processParagraph(next.getValue());
            			para.label = label;
            			para.t = next.getValue();
            			paragraphs.add(para);
            			iter.remove();
            		}
            	}
            	
                int[] linesToSplit = paragraphInformation.getLinesToSplit(block);
                int lineIndex = 0;
                for (Block lineBlock : lineBlocks) {
                	lineCollector.add(lineBlock, block, page);
                    if (lineIndex > 0 && linesToSplit != null) {
                        if (Arrays.binarySearch(linesToSplit, lineIndex) >= 0) {
                			Paragraph para = processParagraph(lineCollector);
                			para.label = label;
                			para.t = lineCollector;
                			paragraphs.add(para);
                			lineCollector = new LineCollector(callback);
                        }
                    }
                    lineIndex++;
                }
        	} else {
                for (Block lineBlock : lineBlocks) {
                	lineCollector.add(lineBlock, block, page);
                }
        	}
            
            if (isSplitBlocks) {
                boolean isMergeTarget = paragraphInformation.isMergeTarget(block);//across pages
                if (isMergeTarget) {
                	textBlockLineList.put(label, lineCollector);
                } else {
                	Paragraph para = processParagraph(lineCollector);
                	para.label = label;
                	para.t =lineCollector;
        			paragraphs.add(para);
                }
            }
            else {
            	Paragraph para = processParagraph(lineCollector);
            	para.label = label;
            	para.t =lineCollector;
    			paragraphs.add(para);
            }
        }
        
        public void flush() {
        	for (Entry<BlockLabel, LineCollector> e : textBlockLineList.entrySet()) {
        		processParagraph(e.getValue());
        	}
        }
        	
        private Paragraph processParagraph(LineCollector lineCollector) {	
            List<Block> lineBlocks = lineCollector.lines;
            List<Block> detectSentences = detectSentenceBeginning(lineBlocks);
            Callback callback = lineCollector.callback;
            List<TokenAnnotation> tokenAnnotations = new ArrayList<TokenAnnotation>();
            List<ScientificArticleMetadataAnnotation> metadataAnnotations = new ArrayList<ScientificArticleMetadataAnnotation>();
            List<ScientificArticleStructureAnnotation> structureAnnotations = new ArrayList<ScientificArticleStructureAnnotation>();
            List<PageAnnotation> pageAnnotations = new ArrayList<PageAnnotation>();
            StringBuilder stringBuilder = new StringBuilder();
            Map<Block, Integer> blockToStart = new HashMap<Block, Integer>();
            Map<Block, Integer> blockToEnd = new HashMap<Block, Integer>();
            Map<Page, Integer> pageToStart = new HashMap<Page, Integer>();
            Map<Page, Integer> pageToEnd = new HashMap<Page, Integer>();

            Block previousWordBlock = null;
            for (Block lineBlock : lineBlocks) {
            	Page page = lineCollector.lineToPage.get(lineBlock);
            	if (!pageToStart.containsKey(page)) { pageToStart.put(page, stringBuilder.length()); }
            	Block block = lineCollector.lineToBlock.get(lineBlock);
            	if (!blockToStart.containsKey(block)) { blockToStart.put(block, stringBuilder.length()); }
            	
                List<Block> wordBlocks = lineBlock.getWordBlocks();
                boolean isFirst = true;
                for (Block wordBlock : wordBlocks) {
                	if (detectSentences.contains(wordBlock)) { stringBuilder.append('\n'); }
                    if (stringBuilder.length() > 0 && stringBuilder.charAt(stringBuilder.length()-1) != '\n') { stringBuilder.append(' '); }

                    int start = stringBuilder.length();
                    String text = wordBlock.getText();
                    text = Normalizer.normalize(text, Normalizer.Form.NFKC);
                    
                    BoundingBoxList boundingBoxList = null; 
					if (isFirst && previousWordBlock != null) {
                        String lastWordText = previousWordBlock.getText();
                        if (lastWordText.length() > 0) {
                            if (lastWordText.charAt(lastWordText.length()-1) == Dehyphenator.HYPHENATION_CHAR) {
                                String firstPart = lastWordText.substring(0, lastWordText.length()-1);
                                boolean isSplit = dehyphenator.checkHyphenation(firstPart, text);
                                if (isSplit) {
                                    TokenAnnotation lastAnnotation = tokenAnnotations.remove(tokenAnnotations.size()-1);
                                    stringBuilder.setLength(lastAnnotation.start);
                                    // start = lastAnnotation.start;
                                    text = firstPart + text;
                                    boundingBoxList = new BoundingBoxList(previousWordBlock.getBoundingBox());
                                } else {
                                    stringBuilder.setLength(stringBuilder.length()-1);
                                }
                            }
                        }
                    }
                    
                    start = stringBuilder.length();
                    stringBuilder.append(text);
                    int end = stringBuilder.length();
                    TokenAnnotation annotation = new TokenAnnotation(start, end);
                    annotation.addFeature(PdfAnnotations.BoundingBoxList, boundingBoxList != null ?
                            new BoundingBoxList(boundingBoxList, wordBlock.getBoundingBox()) : new BoundingBoxList(wordBlock.getBoundingBox()));
                    annotation.addFeature(PdfAnnotations.Page, new Page.PageInfo(page));
                    tokenAnnotations.add(annotation);
                    
                    BlockLabel wordLabel = labeling.getLabel(wordBlock);
                    if (wordLabel != null) {
                        ScientificArticleMetadataAnnotation previousMetadataAnnotation =  metadataAnnotations.size() > 0 ? 
                                metadataAnnotations.get(metadataAnnotations.size()-1) : null;
						String labelString = wordLabel == null ? "null" : wordLabel.getLabel();
						if (previousMetadataAnnotation != null && previousMetadataAnnotation.type.equals(labelString)) {
							previousMetadataAnnotation.setEnd(end);
						} else if (wordLabel == BlockLabel.Affiliation || wordLabel == BlockLabel.Email
								|| wordLabel == BlockLabel.AcademicTitle || wordLabel == BlockLabel.GivenName
								|| wordLabel == BlockLabel.MiddleName || wordLabel == BlockLabel.Surname) {
							metadataAnnotations.add(new ScientificArticleMetadataAnnotation(labelString, start, end, null, null));
						} 
//						else if (TokenClassifierME.AFFILIATION_START.equals(labelString)) {
//							metadataAnnotations.add(new ScientificArticleMetadataAnnotation(BlockLabel.Affiliation.getLabel(), start, end, null, null));
//						}
                    }
                    
                    isFirst = false;
                    previousWordBlock = wordBlock;
                }
            	blockToEnd.put(block, stringBuilder.length()); 
            	pageToEnd.put(page, stringBuilder.length()); 
            }
            
            for (Entry<Page, Integer> e : pageToStart.entrySet()) {
				int start = e.getValue();
				int end = pageToEnd.get(e.getKey());
				PageAnnotation pageAnnotation = new PageAnnotation(start, end, e.getKey());
				pageAnnotations.add(pageAnnotation);
			}
            for (Entry<Block, Integer> e : blockToStart.entrySet()) {
				int start = e.getValue();
				Block block = e.getKey();
				int end = blockToEnd.get(block);
				BlockLabel blockLabel = labeling.getLabel(block);
				if (blockLabel == BlockLabel.Title || blockLabel == BlockLabel.Subtitle || blockLabel == BlockLabel.Journal) {
				    addAnnotationPerLine(stringBuilder, blockLabel.getLabel(), start, end, metadataAnnotations, block);
				} else if (blockLabel != null) {
					structureAnnotations.add(new ScientificArticleStructureAnnotation(blockLabel.getLabel(), 
							start, end, new BoundingBoxList(block.getBoundingBox()), block.getPage()));
				}
			}
            
           return finishParagraph(stringBuilder, tokenAnnotations, metadataAnnotations, structureAnnotations, pageAnnotations, callback);
        }
        
        private void addAnnotationPerLine(StringBuilder stringBuilder, String label, int begin, int end, 
                List<ScientificArticleMetadataAnnotation> metadataAnnotations, Block block) {
            int start = begin, stop = begin;
            do {
                int indexOf = stringBuilder.indexOf("\n", start);
                if (indexOf >= 0) {
                    stop = indexOf;
                } else {
                    stop = end;
                }
                if (stop > start) {
                    metadataAnnotations.add(new ScientificArticleMetadataAnnotation(label, start, stop,
                    		new BoundingBoxList(block.getBoundingBox()), block.getPage()));
                }
                start = stop+1;
            } while (stop < end);
        }
        
        private List<Block> detectSentenceBeginning(List<Block> lineBlocks) {
        	List<Block> sentenceBeginnings = new ArrayList<Block>();
        	
        	StringBuilder builder = new StringBuilder();
        	ArrayList<Block> posToWord = new ArrayList<Block>();
            for (Block lineBlock : lineBlocks) {
				List<Block> wordBlocks = lineBlock.getWordBlocks();
	        	for (Block word : wordBlocks) {
	        		if (builder.length() > 0 && builder.charAt(builder.length()-1) != '\n') { builder.append(' '); }
	                String text = word.getText();
	                text = Normalizer.normalize(text, Normalizer.Form.NFKC);
	                int start = builder.length();
	                builder.append(text);
	                int end = builder.length();
	                for (int i = posToWord.size(); i < start; i++) {
	                	posToWord.add(null);
	                }
	                for (int i = start; i < end; i++) {
	                	posToWord.add(word);
	                }
	        	}
            }
            finishSentence(builder, sentenceBeginnings, posToWord);
            return sentenceBeginnings;
		}
        
        private void finishSentence(StringBuilder builder, List<Block> sentenceBeginnings, ArrayList<Block> posToWord) {
//            Span[] sentPosDetect = sentenceDetector.sentPosDetect(builder.toString());
        	List<Span> sentPosDetect = StanfordNLPLight.getInstance().splitSentences(builder.toString());
            for (Span span : sentPosDetect) {
            	int start = span.getStart();
            	Block block = posToWord.get(start);
            	if (block != null) {
            		sentenceBeginnings.add(block);
            	}
            }
        }

		private Paragraph finishParagraph(StringBuilder stringBuilder, List<TokenAnnotation> tokenAnnotations, 
				List<ScientificArticleMetadataAnnotation> metadataAnnotations, List<ScientificArticleStructureAnnotation> structureAnnotations, 
				List<PageAnnotation> pageAnnotations, Callback callback) {
            stringBuilder.append("\n");
            
            DefaultDocument document = new DefaultDocument();
            document.setText(stringBuilder.toString());
            
            for (TokenAnnotation annotation : tokenAnnotations) {
                document.newAnnotation(annotation.start, annotation.end, BaseTypeSystem.Token, annotation.features);
            }
            for (ScientificArticleStructureAnnotation annotation : structureAnnotations) {
            	// TypedKey<BaseTypeSystem, String> key = TypedKey.createKey(BaseTypeSystem.class, annotation.type, String.class);
                // document.newAnnotation(annotation.start, annotation.end, key);
                document.newAnnotation(annotation.start, annotation.end, PdfAnnotations.ScientificArticleStructure, annotation.features);
            }
            for (ScientificArticleMetadataAnnotation annotation : metadataAnnotations) {
            	// TypedKey<BaseTypeSystem, String> key = TypedKey.createKey(BaseTypeSystem.class, annotation.type, String.class);
                //document.newAnnotation(annotation.start, annotation.end, key);
                document.newAnnotation(annotation.start, annotation.end, PdfAnnotations.ScientificArticleMetadata, annotation.features);
            }
            for (PageAnnotation annotation : pageAnnotations) {
            	// TypedKey<BaseTypeSystem, String> key = TypedKey.createKey(BaseTypeSystem.class, annotation.type, String.class);
                //document.newAnnotation(annotation.start, annotation.end, key);
                document.newAnnotation(annotation.start, annotation.end, PdfAnnotations.ScientificArticlePage, annotation.features);
            }
            callback.onFinished(document);
            documents.add(document);
//            paragraphs.add(new Paragraph(document.getText()));
            return new Paragraph(document.getText());
        }
        
        
        public List<AnnotatedDocument> getDocuments() {
            return documents;
        }
    }
    
    static interface Callback {
        public abstract void onFinished(AnnotatedDocument document);
    }
}
