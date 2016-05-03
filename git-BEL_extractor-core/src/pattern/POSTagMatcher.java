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
package pattern;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import nlp.Sequence;
import utility.Span;

public class POSTagMatcher implements Matcher{

	private HashSet<String> postags;
	private String type;
	private String inputFilePath;
	private String inputFileVersion;
	private String inputFileVersionFromRoot;
	private String fileName;
	
	/**
	 * 
	 * @param postags
	 * @param args: type; inputFileVersion; inputFilePath; inputFileVersionFromRoot
	 */
	public POSTagMatcher(HashSet<String> postags, String...args) {
		// TODO Auto-generated constructor stub
		this.postags = new HashSet<String>();
		this.postags.addAll(postags);
		int length = args.length;
		if(length > 0) {
			type = args[0];
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
		// TODO Auto-generated method stub
		HashSet<Span>	results = new HashSet<Span>();//[)
		for(int i = 0; i < senten.size(); i++) {
			String tag = senten.POSTags.get(i);
			if (this.postags.contains(tag)) {
				results.add(new Span(senten.spans.get(i).getStart(), senten.spans.get(i).getEnd()));
			}
		}
		List<Span> results_ = new ArrayList<Span>(); results_.addAll(results);
		return results_;
	}

	public String getInputFilePath() {
		return inputFilePath;
	}
	public String getInputFileName() {
		return fileName;
	}
	public String getinputFileVersion() {
		return this.inputFileVersion;
	}

	public String getType() {
		return type;
	}

	public String getInputFileVersionFromRoot() {
		return inputFileVersionFromRoot;
	}

	public void setInputFileVersionFromRoot(String inputFileVersionFromRoot) {
		this.inputFileVersionFromRoot = inputFileVersionFromRoot;
	}

}
