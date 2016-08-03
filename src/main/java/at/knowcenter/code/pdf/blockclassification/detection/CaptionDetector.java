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
package at.knowcenter.code.pdf.blockclassification.detection;

import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.knowcenter.code.api.pdf.Block;
import at.knowcenter.code.api.pdf.BlockLabel;
import at.knowcenter.code.api.pdf.Document;
import at.knowcenter.code.api.pdf.ReadingOrder;
import at.knowcenter.code.api.pdf.TextFragment;
import at.knowcenter.code.pdf.blockclassification.BlockLabeling;
import at.knowcenter.code.pdf.blockclassification.detection.metadata.ArticleMetadataCollector;
import at.knowcenter.code.pdf.blockrelation.geometric.BlockNeighborhood;
import at.knowcenter.code.pdf.blockrelation.geometric.DefaultBlockNeighborhood;
import at.knowcenter.code.pdf.blockrelation.geometric.DefaultBlockNeighborhood.Direction;
import at.knowcenter.code.pdf.utils.PdfExtractionUtils;

/**
 * implements a detector for labelling captions.
 * 
 * It looks for blocks starting with keywords such as
 * Table, Tab, Tab., Figure, Fig, Fig.
 * 
 * @author sklampfl
 * 
 */
public class CaptionDetector implements Detector {

	private static final Pattern CAPTION_NUMBER_PATTERN = Pattern.compile("(\\d+|[iIxXvV]+|[a-z]|[A-Z])[a-zA-Z]?(.?)");
	private final Set<String> tableCaptionStartWords = new HashSet<String>();
	private final Set<String> figureCaptionStartWords = new HashSet<String>();

	public CaptionDetector() {
		fillSets();
	}

	private void fillSets() {
		tableCaptionStartWords.add("Table");
		tableCaptionStartWords.add("Table.");
		tableCaptionStartWords.add("Tab");
		tableCaptionStartWords.add("Tab.");
		figureCaptionStartWords.add("Figure");
		figureCaptionStartWords.add("Figure.");
		figureCaptionStartWords.add("Fig");
		figureCaptionStartWords.add("Fig.");
	}

	@Override
	public void detect(Document doc, List<Block> pageBlocks, BlockLabeling labeling, 
			ReadingOrder readingOrder, BlockNeighborhood neighborHood, ArticleMetadataCollector articleMetadata) {
		List<Block> candidateTableCaptionBlocks = new ArrayList<Block>();
		List<Block> candidateFigureCaptionBlocks = new ArrayList<Block>();
		
		for (Block pageBlock : pageBlocks) {
			for (Block block : pageBlock.getSubBlocks()) {
				String firstWord = null;
				String secondWord = null;
				boolean hasMoreWords = false;
				for (Block line : block.getSubBlocks()) {
					Block[] words = line.getSubBlocks().toArray(new Block[0]);
					if (words.length >= 1) {
						firstWord = words[0].getText();
						if (words.length >= 2) {
							secondWord = words[1].getText();
							if (words.length >= 3) {
								hasMoreWords = true;
							}
						}
					}
					break;
				}
				if (firstWord != null) {
					Set<Block> neighbors = getNeighbors(neighborHood, block);
					if(isCandidateTableCaption(block, firstWord, secondWord)) {
						boolean hasNeighbor = false;
						for (Block neighbor : neighbors) {
							if (labeling.hasNoLabel(neighbor)) {
								hasNeighbor = true;
								break;
							}
						}
						if (hasNeighbor || !hasMoreWords) {
							candidateTableCaptionBlocks.add(block);
						}
					}
					if(isCandidateFigureCaption(block, firstWord, secondWord)) {
						candidateFigureCaptionBlocks.add(block);
					}
				}
			}
		}
		
		labelBlocks(candidateTableCaptionBlocks, labeling);
		labelBlocks(candidateFigureCaptionBlocks, labeling);
	}

	private Set<Block> getNeighbors(BlockNeighborhood neighborHood, Block block) {
		Set<Block> neighbors;
		if (neighborHood instanceof DefaultBlockNeighborhood) {
			DefaultBlockNeighborhood nbh = (DefaultBlockNeighborhood)neighborHood;
			neighbors = nbh.getNeighbors(block, Direction.North);
			neighbors.addAll(nbh.getNeighbors(block, Direction.South));
		} else {
			neighbors = neighborHood.getNeighbors(block);
		}
		return neighbors;
	}
	
	private void labelBlocks(List<Block> candidateCaptionBlocks, BlockLabeling labeling) {
		String sep = getMajoritySeparator(candidateCaptionBlocks);
		int fontId = getMajorityFontId(candidateCaptionBlocks);
		int fontSize = getMajorityFontSize(candidateCaptionBlocks);
		
		for (Block block : candidateCaptionBlocks) {
			if ((sep.isEmpty() || sep.equals(getSeparator(block)))) { // && fontSize==getFontSize(block)) {
				labeling.setLabel(block, BlockLabel.Caption);
			}
		}
	}

	private boolean startsWithNumber(String word) {
		return CAPTION_NUMBER_PATTERN.matcher(word).matches();
	}

	private boolean isCandidateTableCaption(Block block, String firstWord,	String secondWord) {
		for (String startWord : tableCaptionStartWords) {
			if (firstWord.equalsIgnoreCase(startWord) && secondWord != null) {
				if (startsWithNumber(secondWord)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isCandidateFigureCaption(Block block, String firstWord, String secondWord) {
		for (String startWord : figureCaptionStartWords) {
			if (firstWord.equalsIgnoreCase(startWord) && secondWord != null) {
				if (startsWithNumber(secondWord)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private String getSeparator(Block captionBlock) {
		String sep = null;
		Block secondWordBlock = captionBlock.getWordBlocks().get(1);
		String secondWord = secondWordBlock.getText();
		Matcher matcher = CAPTION_NUMBER_PATTERN.matcher(secondWord);
		if (matcher.matches()) {
			sep = matcher.group(2);
		}
		return sep;
	}
	
	private int getFontId(Block captionBlock) {
		Block firstWordBlock = captionBlock.getWordBlocks().get(0);
		Block secondWordBlock = captionBlock.getWordBlocks().get(1);
		List<TextFragment> fragments = new ArrayList<TextFragment>(firstWordBlock.getFragments());
		fragments.addAll(secondWordBlock.getFragments());
		return PdfExtractionUtils.getMajorityFontId(fragments);
	}
	
	private int getFontSize(Block captionBlock) {
		Block firstWordBlock = captionBlock.getWordBlocks().get(0);
		Block secondWordBlock = captionBlock.getWordBlocks().get(1);
		List<TextFragment> fragments = new ArrayList<TextFragment>(firstWordBlock.getFragments());
		fragments.addAll(secondWordBlock.getFragments());
		return PdfExtractionUtils.getMajorityFontSize(fragments);
	}
	
	private String getMajoritySeparator(List<Block> candidateCaptionBlocks) {
		TObjectIntHashMap<String> separatorFrequency = new TObjectIntHashMap<String>();
		for (Block captionBlock : candidateCaptionBlocks) {
			separatorFrequency.adjustOrPutValue(getSeparator(captionBlock), 1, 1);
		}
		int maxFreq = -1;
		String majoritySep = null;
		int freq = 0;
		for (TObjectIntIterator<String> it = separatorFrequency.iterator(); it.hasNext(); ) {
        	it.advance();
        	freq = it.value();
        	if (freq > maxFreq) {
        		maxFreq = freq;
        		majoritySep = it.key();
        	}
        }
		return majoritySep;
	}
	
	private int getMajorityFontId(List<Block> candidateCaptionBlocks) {
		TIntIntHashMap fontIdFrequency = new TIntIntHashMap();
		for (Block captionBlock : candidateCaptionBlocks) {
			fontIdFrequency.adjustOrPutValue(getFontId(captionBlock), 1, 1);
		}
		int maxFreq = -1;
		int majorityId = -1;
		int freq = 0;
		for (TIntIntIterator it = fontIdFrequency.iterator(); it.hasNext(); ) {
        	it.advance();
        	freq = it.value();
        	if (freq > maxFreq) {
        		maxFreq = freq;
        		majorityId = it.key();
        	}
        }
		return majorityId;
	}
	
	private int getMajorityFontSize(List<Block> candidateCaptionBlocks) {
		TIntIntHashMap fontSizeFrequency = new TIntIntHashMap();
		for (Block captionBlock : candidateCaptionBlocks) {
			fontSizeFrequency.adjustOrPutValue(getFontSize(captionBlock), 1, 1);
		}
		int maxFreq = -1;
		int majoritySize = -1;
		int freq = 0;
		for (TIntIntIterator it = fontSizeFrequency.iterator(); it.hasNext(); ) {
        	it.advance();
        	freq = it.value();
        	if (freq > maxFreq) {
        		maxFreq = freq;
        		majoritySize = it.key();
        	}
        }
		return majoritySize;
	}


}
