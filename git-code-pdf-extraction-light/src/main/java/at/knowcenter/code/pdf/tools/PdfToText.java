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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.text.StrSubstitutor;

import at.knowcenter.code.api.pdf.PdfParser.PdfParserException;
import at.knowcenter.code.pdf.PdfExtractionPipeline;
import at.knowcenter.code.pdf.PdfExtractionPipeline.PdfExtractionResult;
//import at.knowcenter.code.pdf.blockclassification.detection.metadata.opennlp.BlockTypeClassifierME;
//import at.knowcenter.code.pdf.blockclassification.detection.metadata.opennlp.tokens.TokenClassifierME;
import at.knowcenter.ie.AnnotatedDocument;
import at.knowcenter.ie.Annotation;
import at.knowcenter.ie.BaseTypeSystem;
import at.knowcenter.ie.Language;
import at.knowcenter.ie.BaseTypeSystem.DocumentStructureFeatures.StructureTypeValue;
import at.knowcenter.ie.pipelines.AnnotatorPipeline;

/**
 * Command line tool to convert a PDF file into its text representation.
 * 
 * @author rkern@know-center.at
 */
public class PdfToText {

    /**
     * @param args the name of the PDF file
     */
    public static void main(String[] args) {
        if (args == null || args.length != 3) {
            System.err.println("Expected argument: <model-dir> <pdf-file-name> <output-dir>");
            System.exit(-1);
        }
        
        try {
            String modelDir = args[0];
            File file = new File(args[1]);
            File outputFile = new File(new File(args[2]), file.getName()+".txt");
            
            AnnotatorPipeline annotators = new AnnotatorPipeline(Language.English);
            Map<String, String> modelNameMap = new HashMap<String, String>(); 
            modelNameMap.put("model-dir", modelDir);
            PdfExtractionPipeline pipeline = new PdfExtractionPipeline(
                    StrSubstitutor.replace("${model-dir}/block-type-classifier-model.bin", modelNameMap),
                    StrSubstitutor.replace("${model-dir}/block-features.arff", modelNameMap),
                    StrSubstitutor.replace("${model-dir}/token-classifier-model.bin", modelNameMap),
                    StrSubstitutor.replace("${model-dir}/language-model", modelNameMap),
                    StrSubstitutor.replace("${model-dir}/references-classifier-model.bin", modelNameMap)
                    );
            
            String text = getTextFromPdfFile(file, pipeline, annotators, false);
            FileUtils.writeStringToFile(outputFile, text);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static String getTextFromPdfFile(File file, PdfExtractionPipeline pipeline, AnnotatorPipeline annotators,
    		boolean onlyBody) throws IOException, PdfParserException {
    	PdfExtractionResult result = pipeline.runPipeline(file.getName(), file);
    	return getTextFromPdfFile(result, annotators, onlyBody);
    }
    
    public static String getTextFromPdfFile(PdfExtractionResult result, boolean onlyBody) throws IOException {
    	 AnnotatorPipeline annotators = new AnnotatorPipeline(Language.English);
    	 return getTextFromPdfFile(result, annotators, onlyBody);
    }
    
    private static String getTextFromPdfFile(PdfExtractionResult result, AnnotatorPipeline annotators,
    		boolean onlyBody) {
        
        AnnotatedDocument annotatedDocument = result.annotatedDocument;
        annotators.annotate(annotatedDocument);

        StringBuilder buffer = new StringBuilder();
        String title = null;
        List<String> authors = new ArrayList<String>();
        List<String> affiliations = new ArrayList<String>();
        List<String> emails = new ArrayList<String>();
        Set<Integer> blocksToIgnore = new HashSet<Integer>();
        List<String> authorDetails = new ArrayList<String>();
// commented by huangxc
//        for (Annotation ne : annotatedDocument.getAnnotations(BaseTypeSystem.NamedEntity)) {
//            String entityClass = ne.getFeature(BaseTypeSystem.NamedEntityFeatures.EntityClass);
//            if (BlockTypeClassifierME.TITLE.equals(entityClass)) {
//                title = getText(annotatedDocument, ne);
//                blocksToIgnore.add(ne.getStart());
//            } else if (BlockTypeClassifierME.JOURNAL.equals(entityClass)) {
//                blocksToIgnore.add(ne.getStart());
//            } else if (TokenClassifierME.AFFILIATION.equals(entityClass)) {
//                affiliations.add(getText(annotatedDocument, ne));
//                blocksToIgnore.add(ne.getStart());
//            } else if (TokenClassifierME.EMAIL.equals(entityClass)) {
//                emails.add(getText(annotatedDocument, ne));
//                blocksToIgnore.add(ne.getStart());
//            } else if (TokenClassifierME.TITLE.equals(entityClass)) {
//                authorDetails.add(getText(annotatedDocument, ne));
//                blocksToIgnore.add(ne.getStart());
//            } else if (TokenClassifierME.GIVEN_NAME.equals(entityClass)) {
//                authorDetails.add(getText(annotatedDocument, ne));
//                blocksToIgnore.add(ne.getStart());
//            } else if (TokenClassifierME.MIDDLE_NAME.equals(entityClass)) {
//                authorDetails.add(getText(annotatedDocument, ne));
//                blocksToIgnore.add(ne.getStart());
//            } else if (TokenClassifierME.SURNAME.equals(entityClass)) {
//                authorDetails.add(getText(annotatedDocument, ne));
//                StringBuilder sb = new StringBuilder();
//                for (String detail : authorDetails) {
//                    if (shouldInsertSpace(sb)) { sb.append(' '); }
//                    sb.append(detail);
//                }
//                authorDetails.clear();
//                authors.add(sb.toString());
//                blocksToIgnore.add(ne.getStart());
//            }
//        }
        if (title != null) {
            buffer.append(title);
        }
        if (authors.size() > 0) {
            for (int i = 0; i < authors.size(); i++) {
                if (i > 0) { buffer.append(" "); }
                buffer.append(authors.get(i));
            }
            buffer.append("\n\n");
        }
        if (affiliations.size() > 0 || emails.size() > 0) {
            for (int i = 0; i < affiliations.size(); i++) {
                buffer.append(affiliations.get(i));
            }
            for (int i = 0; i < emails.size(); i++) {
                buffer.append(emails.get(i));
            }
            buffer.append("\n\n");
        }

        StringBuilder abstractText = new StringBuilder();
        for (Annotation ds : annotatedDocument.getAnnotations(BaseTypeSystem.DocumentStructure)) {
            String structureLevel = ds.getFeature(BaseTypeSystem.DocumentStructureFeatures.StructureLevel);
//            commented by huangxc
//            if (BlockTypeClassifierME.ABSTRACT.equals(structureLevel)) {
//                if (shouldInsertSpace(abstractText)) { abstractText.append("\n\n"); }
//                abstractText.append(getText(annotatedDocument, ds));
//                blocksToIgnore.add(ds.getStart());
//            }
        }
        if (shouldInsertSpace(abstractText)) {
            buffer.append("\n\n");
            buffer.append(abstractText);
        }

        StringBuilder bodyBuffer = new StringBuilder();
        for (Annotation ds : annotatedDocument.getAnnotations(BaseTypeSystem.DocumentStructure)) {
            StructureTypeValue structureTypeValue = ds.getFeature(BaseTypeSystem.DocumentStructureFeatures.StructureType);
            if (BaseTypeSystem.DocumentStructureFeatures.StructureTypeValue.Paragraph == structureTypeValue) {
                boolean ignoreBlock = false;
                for (int o : blocksToIgnore) {
                    if (o >= ds.getStart() && o < ds.getEnd()) {
                        ignoreBlock = true;
                        break;
                    }
                }
                if (ignoreBlock) { continue; }
                
                String structureLevel = ds.getFeature(BaseTypeSystem.DocumentStructureFeatures.StructureLevel);
                if (structureLevel != null && structureLevel.length() > 0) {
                    bodyBuffer.append("\n\n");
                    //buffer.append(structureLevel);
                    bodyBuffer.append(getText(annotatedDocument, ds));
                } else if (!(onlyBody && bodyBuffer.length()==0)){
                    bodyBuffer.append("\n\n");
                    bodyBuffer.append(getText(annotatedDocument, ds));
                }
            }
        }
        String bodyString = bodyBuffer.toString();
		buffer.append(bodyString);
        
        return onlyBody ? bodyString : buffer.toString();
    }

    private static String getText(AnnotatedDocument annotatedDocument, Annotation annotation) {
        StringBuilder builder = new StringBuilder();
        
        String prevText = null;
        for (Annotation token : annotatedDocument.getAnnotations(annotation.getStart(), annotation.getEnd(), BaseTypeSystem.Token)) {
            String text = token.getText();
            if (shouldInsertSpace(builder, text, prevText)) { builder.append(' '); }
            // text = text.replaceAll("&", "\\\\&").replaceAll("%", "\\\\%");
            builder.append(text);
            prevText = text;
        }
        
        return builder.toString();
    }

    private static boolean shouldInsertSpace(StringBuilder builder, String text, String prevText) {
        if ("(".equals(prevText)) { return false; }
        if (")".equals(text)) { return false; }
        if ("“".equals(prevText)) { return false; }
        if ("”".equals(text)) { return false; }
        if ("?".equals(text)) { return false; }
        if (".".equals(text)) { return false; }
        if (",".equals(text)) { return false; }
        if (";".equals(text)) { return false; }
        if ("'".equals(prevText)) { return false; }
        if ("'".equals(text)) { return false; }
        if ("’".equals(prevText)) { return false; }
//        if ("’".equals(text)) { return false; }
        return builder.length() > 0;
    }
    
    private static boolean shouldInsertSpace(StringBuilder builder) {
        return builder.length() > 0;
    }

}
