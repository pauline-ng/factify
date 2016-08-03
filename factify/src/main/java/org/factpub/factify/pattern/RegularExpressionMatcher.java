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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nlp.Sequence;
import utility.Span;

/**
 * Match regular expressions
 *
 */
public class RegularExpressionMatcher implements org.factpub.factify.pattern.Matcher{
	private HashSet<String> regExpressions;
	private String type;
	private String inputFilePath;
	private String inputFileVersion;
	private String inputFileVersionFromRoot;
	private String fileName;
	/**
	 * 
	 * @param regExps
	 * @param args type; inputFileVersion; inputFilePath; inputFileVersionFromRoot
	 */
	public RegularExpressionMatcher(HashSet<String> regExps, String...args) {
		this.regExpressions = new HashSet<String>();
		this.regExpressions.addAll(regExps);
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
	}

	@Override
	public List<Span> Match(Sequence senten) {
		HashSet<Span>	results = new HashSet<Span>();//[)
		for(String reg : regExpressions) {
			Pattern pattern = Pattern.compile(reg);
			for(int i = 0; i < senten.getWordCount(); i++) {
				String word = senten.getWord(i);
				Matcher matcher = pattern.matcher(word);//word has been trimmed
				if (matcher.find()) {
					results.add(new Span(senten.getSpanOfWord(i).getStart(), senten.getSpanOfWord(i).getEnd()));
				}
			}
		}
		List<Span> results_ = new ArrayList<Span>(); results_.addAll(results);
		return results_;
	}

	public String getInputFilePath() {
		return inputFilePath;
	}
	public String getInputFileName() {
		return this.fileName;
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
