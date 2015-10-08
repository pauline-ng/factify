package test;

import extractor.ExtractorBELExtractor;

public class Debug_BEL_extractor {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String path = "D:\\huangxcwd\\Data\\reddit\\odesk\\allpapers\\ccok4xz.pdf.pdf";
//		examplePDFExtractor(path, path + "_.fact");
//		exampleXMLExtractor(path,path + "_");
//		ExtractorBELExtractor extractor = new ExtractorBELExtractor();
		
		ExtractorBELExtractor.examplePDFExtractor_JSON(path, "output_BEL_extractor\\", "debugOutput_BEL_extractor\\");
	}

}
