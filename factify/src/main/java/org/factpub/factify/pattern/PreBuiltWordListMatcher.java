/**
    Copyright (C) 2016, Genome Institute of Singapore, A*STAR  

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.factpub.factify.pattern;

import java.io.File;
import java.util.HashSet;
import java.util.List;

import org.factpub.factify.nlp.Sequence;
import org.factpub.factify.nlp.StanfordNLPLight;
import org.factpub.factify.utility.Debug;
import org.factpub.factify.utility.Span;
import org.factpub.factify.utility.Debug.DEBUG_CONFIG;

/**
 * 
 * Match word stems with PreBuiltWordList
 *
 */
public class PreBuiltWordListMatcher implements Matcher{
	private HashSet<Sequence> wordList;
	private String type;
	private String inputFilePath;
	private String fileName;
	private String inputFileVersion;
	private String inputFileVersionFromRoot;
	private String match;//if match is not null (="contains") then this is a "contain" match. For "contain" match, each word in wordlist should be single word, otherwise the program will overwrite "contain" match to match.
	/**
	 * 
	 * @param words
	 * @param args type; inputFileVersion; inputFilePath; inputFileVersionFromRoot; match
	 */
	public PreBuiltWordListMatcher(HashSet<String> words, String...args) {
		this.wordList = new HashSet<Sequence>();
 		for(String s : words) {
 			List<Sequence> seqs = StanfordNLPLight.INSTANCE.textToSequence(s, false);
 			if(seqs.size() > 1) {
 				Debug.println("Warning: " + s + " in " + fileName + " has " + seqs.size() + " sentences!", DEBUG_CONFIG.debug_warning);
 			}
 			wordList.addAll(seqs);
		}
		int length = args.length;
		if(length > 0) {
			setType(args[0]);
		} 
		if(length > 1) {
			inputFileVersion = args[1];
		}
		if(length > 2) {
			inputFilePath = args[2];
			File file = new File(inputFilePath);
			if(file.exists()) fileName = file.getName();
		}
		if(length > 3) {
			setInputFileVersionFromRoot(args[3]);
		}
		if(length > 4) {
			this.match = args[4];
		}
	}

	@Override
	public List<Span> Match(Sequence senten) {
		if(this.match == null) {
			PatternMatcher pm = new PatternMatcher();
			return pm.findMatches(senten, wordList); 
		}
		else {
			PatternMatcher pm = new PatternMatcher();
			return pm.findContainingMatches(senten, wordList);
		}
	}

	public String getInputFilePath() {
		return inputFilePath;
	}
	public String getInputFileName() {
		return fileName;
	}
	public String getInputFileVersion() {
		return this.inputFileVersion;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getInputFileVersionFromRoot() {
		return inputFileVersionFromRoot;
	}

	public void setInputFileVersionFromRoot(String inputFileVersionFromRoot) {
		this.inputFileVersionFromRoot = inputFileVersionFromRoot;
	}
}
