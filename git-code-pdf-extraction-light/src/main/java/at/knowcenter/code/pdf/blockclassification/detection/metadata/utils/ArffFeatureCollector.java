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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;

/**
 * 
 * 
 * @author Roman Kern <rkern@know-center.at>
 */
public class ArffFeatureCollector implements FeatureCollector {
    Map<String, List<TreeSet<String>>> collector    = new HashMap<String, List<TreeSet<String>>>();
// Map<String, Class<?>> featureNames = new HashMap<String, Class<?>>();
    Map<String, Integer>               featureNames = new TreeMap<String, Integer>();
    int                                instanceCounter;
    private final FastVector           trainAttributes;
    private final Set<String>          trainFeatureNames;

    /**
     * Creates a new instance of this class.
     */
    public ArffFeatureCollector() {
        this.trainAttributes = null;
        this.trainFeatureNames = null;
    }

    /**
     * Creates a new instance of this class.
     * 
     * @param trainFile
     * @throws IOException
     */
    @SuppressWarnings("rawtypes")
    public ArffFeatureCollector(File trainFile) throws IOException {
        try {
            ArffLoader loader = new ArffLoader();
            loader.setSource(trainFile);
            Instances instances = loader.getStructure();
            trainAttributes = new FastVector();
            trainFeatureNames = new TreeSet<String>();
            Enumeration enumeration = instances.enumerateAttributes();
            while (enumeration.hasMoreElements()) {
                Attribute elem = (Attribute)enumeration.nextElement();
                trainAttributes.addElement(elem);
                trainFeatureNames.add(elem.name());
            }
        } catch (IOException e) {
            throw new IOException("Cannot load weka file '" + trainFile + "'", e);
        }
    }

    /**
     * @param features
     * @param label
     * @param index
     */
    @Override
    public void update(List<String> features, String label, int index) {
        List<TreeSet<String>> list = collector.get(label);
        if (list == null) {
            collector.put(label, list = new ArrayList<TreeSet<String>>());
        }
        list.add(new TreeSet<String>(features));
        for (String f : features) {
            Integer counter = featureNames.get(f);
            featureNames.put(f, counter != null ? counter + 1 : 1);
        }
        instanceCounter++;
    }

    /**
     * @param file
     * @throws IOException
     */
    @Override
    public void writeDataSet(File file) throws IOException {
        FastVector attributes;
        Attribute labelAttribute;
        String[] fna;
        if (this.trainAttributes != null) {
            attributes = this.trainAttributes;
            labelAttribute = (Attribute)attributes.elementAt(attributes.size() - 1);
            fna = trainFeatureNames.toArray(new String[trainFeatureNames.size()]);
        } else {
            FastVector labelsVector = new FastVector();
            for (String label : collector.keySet()) {
                labelsVector.addElement(label);
            }
            labelAttribute = new Attribute("x-label", labelsVector);
            attributes = new FastVector();
            List<String> names = new ArrayList<String>(featureNames.size());
            for (Entry<String, Integer> e : featureNames.entrySet()) {
                if (e.getValue() >= 2) {
                    String name = e.getKey();
                    attributes.addElement(new Attribute(name));
                    names.add(name);
                }
            }
            attributes.addElement(labelAttribute);
            fna = names.toArray(new String[names.size()]);
        }

        Instances instances = new Instances("blocks", attributes, instanceCounter);
        instances.setClassIndex(attributes.size() - 1);

        int counter = 0, li = 0;
        for (Entry<String, List<TreeSet<String>>> e : collector.entrySet()) {
            String label = e.getKey();
            for (TreeSet<String> sample : e.getValue()) {
                int[] indices = new int[sample.size()];
                double[] values = new double[sample.size()];
                int i = 0;
                for (String f : sample) {
                    int index = Arrays.binarySearch(fna, f);
                    if (index >= 0) {
                        values[i] = 1;
                        indices[i++] = index;
                    }
                }
                if (i != sample.size()) {
                    indices = Arrays.copyOf(indices, i);
                    values = Arrays.copyOf(values, i);
                }
                SparseInstance instance = new SparseInstance(1, values, indices, indices.length);
                instance.setValue(labelAttribute, label);
                instances.add(instance);
                counter++;
            }
            System.out.println("Added type '" + label + "', instance counter at " + counter + ".");
            li++;
        }

        assert counter == instanceCounter;

        ArffSaver saver = new ArffSaver();
        saver.setDestination(new FileOutputStream(file));
        saver.setInstances(instances);
        saver.writeBatch();
    }
}
