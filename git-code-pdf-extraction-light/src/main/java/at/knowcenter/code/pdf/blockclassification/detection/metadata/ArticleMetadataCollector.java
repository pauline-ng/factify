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
package at.knowcenter.code.pdf.blockclassification.detection.metadata;

import java.util.ArrayList;
import java.util.List;

public class ArticleMetadataCollector {
//	final List<DetectedReference> references = new ArrayList<DetectedReference>();
//	final Map<TableRegion, String> htmlTables = new HashMap<TableRegion, String>();
	List<String> htmlTables_string =  new ArrayList<String>();
	List<String> htmlTables_caption = new ArrayList<String>();
//	final ReferenceMarkerUsage referenceMarkerUsage = new ReferenceMarkerUsage();
	
	public void beginArticle() {
	}
	
	public void endArticle() {
	}

//	public List<DetectedReference> getReferences() {
//		return references;
//	}

//	public Map<TableRegion, String> getHtmlTables() {
//		return htmlTables;
//	}
	public List<String> getHtmlTables_string() {
		return this.htmlTables_string;
	}
	public List<String> getHtmlTables_caption() {
		return this.htmlTables_caption;
	}

	/**
     * Returns the referenceMarkerUsage.
     * @return the referenceMarkerUsage
     */
//    public ReferenceMarkerUsage getReferenceMarkerUsage() {
//        return referenceMarkerUsage;
//    }
//
//	public void addReference(List<ReferenceToken> tokens, String[] prediction) {
//		
//	}
//
//	public void addReference(DetectedReference detectedReference) {
//		references.add(detectedReference);
//	}
	
//	public void addHtmlTable(TableRegion tableRegion, String htmlTable) {
//		htmlTables.put(tableRegion, htmlTable);
//	}
	public void addHtmlTable(String htmlTable, String caption) {
		this.htmlTables_string.add(htmlTable);
		this.htmlTables_caption.add(caption);
	}

    /**
     * @param referenceMarkerUsage
     */
//    public void addMarkerPatterns(ReferenceMarkerUsage referenceMarkerUsage) {
//        if (referenceMarkerUsage == null) { return; }
//        
//        for (Entry<Pattern, Set<String>> e : referenceMarkerUsage.getFoundMarkers().entrySet()) {
//            for (String s : e.getValue()) {
//                this.referenceMarkerUsage.add(e.getKey(), s);
//            }
//        }
//    }
}
