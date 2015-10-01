/* Copyright (C) 2010 
"Kompetenzzentrum fuer wissensbasierte Anwendungen Forschungs- und EntwicklungsgmbH" 
(Know-Center), Graz, Austria, office@know-center.at.

Licensees holding valid Know-Center Commercial licenses may use this file in
accordance with the Know-Center Commercial License Agreement provided with 
the Software or, alternatively, in accordance with the terms contained in
a written agreement between Licensees and Know-Center.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package at.knowcenter.code.pdf.tools;

import java.io.File;
import java.io.PrintWriter;

import at.knowcenter.code.api.pdf.TableRegion;
import at.knowcenter.code.pdf.PdfExtractionPipeline;
import at.knowcenter.code.pdf.PdfExtractionPipeline.PdfExtractionResult;
import at.knowcenter.code.pdf.utils.table.ClusteringTableParser;
import at.knowcenter.code.pdf.utils.table.TableCell;
import at.knowcenter.code.pdf.utils.table.TableParser;
import at.knowcenter.code.pdf.utils.table.TableUtils;

/**
 * 
 * 
 * @author rkern@know-center.at
 */
public class ExtractTables {

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args == null || args.length != 1) {
            System.err.println("Expected argument: <pdf-file-name>");
            System.exit(-1);
        }
        File file = new File(args[0]);
        
        try {
            PdfExtractionPipeline pipeline = new PdfExtractionPipeline(true);

            PdfExtractionResult result = pipeline.runPipeline(file.getName(), file);
            
            File outputFile = new File(file.getParentFile(), file.getName()+".html");
            TableParser tableParser = new ClusteringTableParser(10f, 0.5f, 1f, 0.5f, false);
            PrintWriter printWriter = new PrintWriter(outputFile);
            printWriter.println("<html><body><h1>"+file+"</h1>");
            for (TableRegion table : result.doc.getTables()) {
                TableCell[][] cells = tableParser.parseTable(table);
                String htmlTable = "<div>"+table.captionBlock.getText()+"</div>"+TableUtils.createHtmlTable(cells)+"<br/>";
                printWriter.println(htmlTable);
            }
            printWriter.println("</body></html>");
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
