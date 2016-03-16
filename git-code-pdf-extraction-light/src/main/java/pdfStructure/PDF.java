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
