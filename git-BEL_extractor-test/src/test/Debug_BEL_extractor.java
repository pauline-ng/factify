package test;

import java.io.File;

import utility.Debug;
import utility.Debug.DEBUG_CONFIG;
import extractor.ExtractorBELExtractor;

public class Debug_BEL_extractor {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		String path = "D:\\huangxcwd\\Data\\reddit\\odesk\\allpapers\\ccok4xz.pdf.pdf";
//		examplePDFExtractor(path, path + "_.fact");
//		exampleXMLExtractor(path,path + "_");
//		ExtractorBELExtractor extractor = new ExtractorBELExtractor();
		if(args.length != 3) {
			Debug.print("Please specify 3 parameters: filePath, output_dir, and debug_dir!", DEBUG_CONFIG.debug_error);
			return;
		}
		if(!new File(args[0]).exists()) {
			Debug.print("Please specify a valid input file or folder!", DEBUG_CONFIG.debug_error);
			return;
		}
		Boolean fileOrFolder = null;//true if it is a file
		if(new File(args[0]).isFile()) {
			fileOrFolder = true;
		}else if (new File(args[0]).isDirectory()) {
			fileOrFolder = false;
		}
		if(fileOrFolder == true) {
			ExtractorBELExtractor.examplePDFExtractor_JSON(args[0], args[1], args[2]);
		}else if(fileOrFolder == false) {
//			testFromFolder(args[0], args[1], args[2]);
		}
		
		
	}

}
