package pattern;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nlp.Sequence;
import utility.Debug;
import utility.Span;
import utility.Debug.DEBUG_CONFIG;

public class RegularExpressionMatcher implements Match{
	private HashSet<String> regExpressions;
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
	public RegularExpressionMatcher(HashSet<String> regExps, String...args) {
		// TODO Auto-generated constructor stub
		this.regExpressions = new HashSet<String>();
		this.regExpressions.addAll(regExps);
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
		for(String reg : regExpressions) {
			Pattern pattern = Pattern.compile(reg);
			for(int i = 0; i < senten.size(); i++) {
				String word = senten.words.get(i);
				Matcher matcher = pattern.matcher(word);//word has been trimmed
				if (matcher.find()) {
					results.add(new Span(senten.spans.get(i).getStart(), senten.spans.get(i).getEnd()));
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
}
