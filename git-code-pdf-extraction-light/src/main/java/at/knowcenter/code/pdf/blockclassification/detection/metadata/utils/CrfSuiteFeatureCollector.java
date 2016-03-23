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
package at.knowcenter.code.pdf.blockclassification.detection.metadata.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
//import org.eclipse.jetty.io.WriterOutputStream;

/**
 * 
 * 
 * @author Roman Kern <rkern@know-center.at>
 */
public class CrfSuiteFeatureCollector implements FeatureCollector {
    private final ByteArrayOutputStream outputStream;
    private final PrintWriter writer;
    private int lineCounter;
    
    /**
     * Creates a new instance of this class.
     */
    public CrfSuiteFeatureCollector() {
        this(null);
    }
    
    /**
     * Creates a new instance of this class.
     * @param file 
     */
    public CrfSuiteFeatureCollector(File file) {
        try {
            outputStream = new ByteArrayOutputStream();
            writer = new PrintWriter(new OutputStreamWriter(new GZIPOutputStream(outputStream), "UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException("Cannot set up the CRFSuite feature collector", e);
        }
    }

    @Override
    public void update(List<String> features, String label, int index) {
        if (lineCounter > 0 && index == 0) {
            writer.println();
        }
        writer.print(label);
        for (String f : features) {
            writer.print('\t');
            writer.print(f.replace(':', '£'));
        }
        writer.println();
        lineCounter++;
    }

    @Override
    public void writeDataSet(File file) throws IOException {
        writer.close();
        
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        IOUtils.copy(new GZIPInputStream(new ByteArrayInputStream(outputStream.toByteArray())), fileOutputStream);
        IOUtils.closeQuietly(fileOutputStream);
    }

}
