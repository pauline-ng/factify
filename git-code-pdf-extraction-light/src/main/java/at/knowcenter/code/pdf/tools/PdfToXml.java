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
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import at.knowcenter.code.api.pdf.BlockLabel;
import at.knowcenter.code.api.pdf.PdfAnnotations;
import at.knowcenter.code.api.pdf.PdfExtractorResult;
import at.knowcenter.code.pdf.PdfExtractionPipeline;
import at.knowcenter.code.pdf.blockclassification.detection.metadata.utils.ScientificPublicationsUtils;
import at.knowcenter.ie.AnnotatedDocument;
import at.knowcenter.ie.Annotation;
import at.knowcenter.ie.BaseTypeSystem;
import at.knowcenter.ie.Language;
//import at.knowcenter.ie.documentviewer.AnnotatedDocumentViewer;
import at.knowcenter.ie.pipelines.AnnotatorPipeline;
import at.knowcenter.util.XmlHelper;
import at.knowcenter.util.typedkey.TypedKey;

/**
 * Command line tool to convert a PDF file into its text representation.
 * 
 * @author rkern@know-center.at
 */
public class PdfToXml {

    /**
     * @param args the name of the PDF file
     */
    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            System.err.println("Expected arguments: [-v] [-a] <pdf-file>+");
            System.err.println(" -v verbose output");
            System.err.println(" -a annotation format (.txt + .xml)");
            System.exit(-1);
        }
        
        try {
            File modelDir = new File("models");
            
            AnnotatorPipeline annotators = new AnnotatorPipeline(Language.English);
            Map<String, String> modelNameMap = new HashMap<String, String>(); 
            modelNameMap.put("model-dir", modelDir.getPath());
            PdfExtractionPipeline pipeline = new PdfExtractionPipeline(
                    StrSubstitutor.replace("${model-dir}/block-type-classifier-model.bin", modelNameMap),
                    StrSubstitutor.replace("${model-dir}/block-features.arff", modelNameMap),
                    StrSubstitutor.replace("${model-dir}/token-classifier-model.bin", modelNameMap),
                    StrSubstitutor.replace("${model-dir}/language-model", modelNameMap),
                    StrSubstitutor.replace("${model-dir}/references-classifier-model.bin", modelNameMap)
                    );
            
            boolean verbose = false;
            boolean annotationFormat = false;
            for (String fileName : args) {
                if ("-v".equals(fileName)) { verbose = true; continue; }
                if ("-a".equals(fileName)) { annotationFormat = true; continue; }
                
                File file = new File(fileName);
                System.out.println("Start processing: "+file);
                PdfExtractorResult extractionResult = pipeline.extract(file);
                AnnotatedDocument annotatedDocument = extractionResult.getAnnotatedDocument();
                File textFile = new File(fileName+".txt");
                File xmlFile = new File(fileName+".xml");
                Document document = XmlHelper.getDocumentBuilderFactory().newDocumentBuilder().newDocument();
                Node rootNode;
                if (!annotationFormat) {
                    rootNode = document.appendChild(document.createElement("scientific-article"));
                    Node coreMetatdataNode = rootNode.appendChild(document.createElement("core-metadata"));
                    Iterable<Annotation> annotations = annotatedDocument.getAnnotations(PdfAnnotations.ScientificArticleMetadata);
                    Set<Annotation> currentGivenNames = new HashSet<Annotation>();
                    Set<Annotation> currentMiddleNames = new HashSet<Annotation>();
                    Set<Annotation> currentSurNames = new HashSet<Annotation>();
                    String lastFeature = null;
                    for (Annotation annotation : annotations) {
                        String feature = annotation.getFeature(BaseTypeSystem.NamedEntityFeatures.EntityClass);
                        if (!feature.startsWith("ref-")) {
                            if (feature.equals(BlockLabel.GivenName.getLabel())) {
                                if (!BlockLabel.GivenName.getLabel().equals(lastFeature)) {
                                    flushAuthor(currentGivenNames, currentMiddleNames, currentSurNames, document, coreMetatdataNode);
                                }
                                currentGivenNames.add(annotation);
                            } else if (feature.equals(BlockLabel.MiddleName.getLabel())) {
                                if (!BlockLabel.GivenName.getLabel().equals(lastFeature) && !BlockLabel.MiddleName.getLabel().equals(lastFeature)) {
                                    flushAuthor(currentGivenNames, currentMiddleNames, currentSurNames, document, coreMetatdataNode);
                                }
                                currentMiddleNames.add(annotation);
                            } else if (feature.equals(BlockLabel.Surname.getLabel())) {
                                if (!BlockLabel.GivenName.getLabel().equals(lastFeature) && !BlockLabel.MiddleName.getLabel().equals(lastFeature) && !BlockLabel.Surname.getLabel().equals(lastFeature)) {
                                    flushAuthor(currentGivenNames, currentMiddleNames, currentSurNames, document, coreMetatdataNode);
                                }
                                currentSurNames.add(annotation);
                            } else {
                                flushAuthor(currentGivenNames, currentMiddleNames, currentSurNames, document, coreMetatdataNode);
                                Node node = coreMetatdataNode.appendChild(document.createElement(feature));
                                node.appendChild(document.createTextNode(ScientificPublicationsUtils.cleanMetadata(annotation.getText())));
                            }
                        }
                        lastFeature = feature;
                    }
                    flushAuthor(currentGivenNames, currentMiddleNames, currentSurNames, document, coreMetatdataNode);
                    
                    /*
                    Node structureNode = rootNode.appendChild(document.createElement("structure"));
                    annotations = annotatedDocument.getAnnotations(PdfAnnotations.ScientificArticleStructure);
                    for (Annotation annotation : annotations) {
                        String feature = annotation.getFeature(BaseTypeSystem.NamedEntityFeatures.EntityClass);
                        if ("abstract".equals(feature) || "heading".equals(feature)) {
                            Node node = structureNode.appendChild(document.createElement(feature));
                            node.appendChild(document.createTextNode(annotation.getText()));
                        }
                    }
                    */
                    Node referencesNode = rootNode.appendChild(document.createElement("references"));
                    annotations = annotatedDocument.getAnnotations(PdfAnnotations.ScientificArticleStructure);
                    for (Annotation annotation : annotations) {
                        String feature = annotation.getFeature(BaseTypeSystem.NamedEntityFeatures.EntityClass);
                        if ("reference".equals(feature)) {
                            Node node = referencesNode.appendChild(document.createElement("reference"));
                            node.appendChild(document.createTextNode(annotation.getText()));
                        }
                    }
                } else {
                    rootNode = document.appendChild(document.createElement("annotations"));
                    Iterable<Annotation> annotations = annotatedDocument.getAnnotations();
                    for (Annotation annotation : annotations) {
                        Element annotationNode = (Element)rootNode.appendChild(document.createElement("annotation"));
                        TypedKey<BaseTypeSystem, String> type = annotation.getType();
                        annotationNode.setAttribute("type", type.getName());
                        annotationNode.setAttribute("start", Integer.toString(annotation.getStart()));
                        annotationNode.setAttribute("end", Integer.toString(annotation.getEnd()));
                        
                        Element featureNode = null;
                        Iterator<TypedKey<BaseTypeSystem, ? extends Serializable>> featureKeys = annotation.getFeatureKeys();
                        while (featureKeys.hasNext()) {
                            TypedKey<BaseTypeSystem, ? extends Serializable> featureKey = featureKeys.next();
                            Serializable value = annotation.getFeature(featureKey);
                            if (value instanceof String) {
                                if (featureNode == null) {
                                    featureNode = (Element)annotationNode.appendChild(document.createElement("feature"));
                                }
                                featureNode.setAttribute("value", (String)value);
                            }
                        }
                    }
                }
                if (!annotationFormat) {
                    FileUtils.writeStringToFile(xmlFile, XmlHelper.toXml(document));
                } else {
                    FileUtils.writeStringToFile(textFile, annotatedDocument.getText());
                    FileUtils.writeStringToFile(xmlFile, XmlHelper.toXml(document));
                }
                
                if (verbose) {
//                    new AnnotatedDocumentViewer(annotatedDocument).show();
                }
                System.out.println("Finished processing: "+file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * @param currentGivenNames
     * @param currentMiddleNames
     * @param currentSurNames
     * @param coreMetatdataNode 
     */
    private static void flushAuthor(Set<Annotation> currentGivenNames,
            Set<Annotation> currentMiddleNames, Set<Annotation> currentSurNames, Document document, Node coreMetatdataNode) {
        if (currentGivenNames.isEmpty() && currentMiddleNames.isEmpty() && currentSurNames.isEmpty()) { return; }
        
        Node authorNode = coreMetatdataNode.appendChild(document.createElement("author"));
        for (Annotation annotation : currentGivenNames) {
            Node node = authorNode.appendChild(document.createElement("given-name"));
            node.appendChild(document.createTextNode(ScientificPublicationsUtils.cleanMetadata(annotation.getText())));
        }
        for (Annotation annotation : currentMiddleNames) {
            Node node = authorNode.appendChild(document.createElement("middle-name"));
            node.appendChild(document.createTextNode(ScientificPublicationsUtils.cleanMetadata(annotation.getText())));
        }
        for (Annotation annotation : currentSurNames) {
            Node node = authorNode.appendChild(document.createElement("surname"));
            node.appendChild(document.createTextNode(ScientificPublicationsUtils.cleanMetadata(annotation.getText())));
        }
        currentGivenNames.clear();
        currentMiddleNames.clear();
        currentSurNames.clear();
    }

    

}
