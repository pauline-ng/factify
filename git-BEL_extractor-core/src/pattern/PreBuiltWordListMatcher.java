package pattern;

import java.io.File;
import java.util.HashSet;
import java.util.List;

import nlp.Sequence;
import nlp.StanfordNLPLight;
import utility.Debug;
import utility.Span;
import utility.Debug.DEBUG_CONFIG;

public class PreBuiltWordListMatcher implements Match{
	private HashSet<Sequence> wordList;
	private String type;
	private String inputFilePath;
	private String fileName;
	private String inputFileVersion;
	private String inputFileVersionFromRoot;
	/**
	 * 
	 * @param postags
	 * @param args: type; inputFileVersion; inputFilePath
	 */
	public PreBuiltWordListMatcher(HashSet<String> words, String...args) {
		// TODO Auto-generated constructor stub
		this.wordList = new HashSet<Sequence>();
		if(StanfordNLPLight.nlp == null) StanfordNLPLight.nlp = new StanfordNLPLight("tokenize, ssplit, pos, lemma");
 		for(String s : words) {
 			List<Sequence> seqs = StanfordNLPLight.nlp.textToSequence(s, false);
 			if(seqs.size() > 1) {
 				Debug.println("Warning: " + s + " in " + fileName + " has " + seqs.size() + " sentences!", DEBUG_CONFIG.debug_warning);
 			}
 			wordList.addAll(seqs);
//			StringTokenizer st = new StringTokenizer(s, " ");
//			List<String> ele = new ArrayList<String>();
//			while(st.hasMoreTokens()) ele.add(st.nextToken());
//			wordList.add(new Sequence(null, ele, null, null, s));//sequence equality is based on stems
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
	}

	@Override
	public List<Span> Match(Sequence senten) {
		// TODO Auto-generated method stub
 		PatternMatcher pm = new PatternMatcher();
		return pm.findMatches(senten, wordList);
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
