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

//import java.util.ArrayList;
import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
import java.util.Map;
//import java.util.Map.Entry;
//import java.util.Set;
//import java.util.regex.Pattern;

import at.knowcenter.code.api.pdf.TableRegion;
//import at.knowcenter.code.pdf.blockclassification.detection.metadata.opennlp.references.ReferenceToken;
//import at.knowcenter.code.pdf.blockclassification.detection.references.DetectedReference;
//import at.knowcenter.code.pdf.blockclassification.detection.references.ReferenceMarkerUsage;

public class ArticleMetadataCollector {
//	final List<DetectedReference> references = new ArrayList<DetectedReference>();
	final Map<TableRegion, String> htmlTables = new HashMap<TableRegion, String>();
//	final ReferenceMarkerUsage referenceMarkerUsage = new ReferenceMarkerUsage();
	
	public void beginArticle() {
	}
	
	public void endArticle() {
	}

//	public List<DetectedReference> getReferences() {
//		return references;
//	}

	public Map<TableRegion, String> getHtmlTables() {
		return htmlTables;
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
	
	public void addHtmlTable(TableRegion tableRegion, String htmlTable) {
		htmlTables.put(tableRegion, htmlTable);
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
