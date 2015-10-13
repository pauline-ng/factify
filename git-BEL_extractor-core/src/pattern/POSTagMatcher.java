package pattern;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import nlp.Sequence;
import utility.Debug;
import utility.Debug.DEBUG_CONFIG;
import utility.Span;

public class POSTagMatcher implements Match{

	private HashSet<String> postags;
	private String type;
	private String inputFilePath;
	private String inputFileVersion;
	private String inputFileVersionFromRoot;
	private String fileName;
	
	/**
	 * 
	 * @param postags
	 * @param args: type; inputFileVersion; inputFilePath
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
			inputFileVersionFromRoot = args[3];
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
	
}
