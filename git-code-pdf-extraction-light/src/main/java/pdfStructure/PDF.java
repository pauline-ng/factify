package pdfStructure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.knowcenter.code.api.pdf.TableRegion;

public class PDF {
	public List<Paragraph> body_and_heading;
//	public Map<TableRegion, String> htmlTables = new HashMap<TableRegion, String>();
	public List<String> htmlTables_string;
	public List<String> htmlTables_caption;
	public List<Paragraph> noneBodynorHeading;
	public List<Paragraph> candidateTitle;
	public PDF() {
		body_and_heading = new ArrayList<Paragraph>();
		noneBodynorHeading = new ArrayList<Paragraph>();
		candidateTitle = new ArrayList<Paragraph>();
		this.htmlTables_caption = new ArrayList<String>();
		this.htmlTables_string = new ArrayList<String>();
		
	}
//	public Collection<String> getTables() {
//		return htmlTables.values();
//	}
	public String doi;
}
