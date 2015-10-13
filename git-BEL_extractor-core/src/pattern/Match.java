package pattern;

import java.util.List;

import nlp.Sequence;
import utility.Span;

public interface Match {
	
	List<Span> Match(Sequence senten);
}
