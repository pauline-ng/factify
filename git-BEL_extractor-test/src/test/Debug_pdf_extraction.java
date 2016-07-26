package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

import utility.Debug;
import utility.Debug.DEBUG_CONFIG;
import utility.Utility;
import at.knowcenter.code.pdf.PdfExtractionPipeline;
/**
 * cav6qhz.pdf: pdf-extraction works terribly on this pdf.
 *
 */
public class Debug_pdf_extraction {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		findFile();
//		moveFiles();
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
			testOneFile(args[0], args[1], args[2]);
		}else if(fileOrFolder == false) {
//			testFromFolder(args[0], args[1], args[2]);
		}
//		testOneFile();
	}
	
	public static void testOneFile(String pdfpath, String outputPath, String debugPath) {
//		String pdfpath = "D:\\huangxcwd\\Data\\reddit\\odesk\\allpapers\\cav6qhz.pdf";
//		String pdfpath = "D:\\huangxcwd\\Data\\reddit\\odesk\\allpapers\\cbvqoes_1.pdf";
//		String pdfpath = "D:\\huangxcwd\\Data\\reddit\\odesk\\allpapers\\1hk4zt.pdf";
//		String outputPath = "output_pdf_extraction\\";
//		String debugPath = "debugOutput_pdf_extraction\\";
		PdfExtractionPipeline  pipeline = new PdfExtractionPipeline();
		pipeline.setParameter((new File(pdfpath)).getName(), outputPath, debugPath);
		Debug.set(DEBUG_CONFIG.debug_textpieces, true);
		pipeline.runPipeline(new File(pdfpath));
	}
	
	public static void findFile() {
		String input_folder = "D:\\GitHub\\n-projects-ws-repo\\n-projects-ws-repo\\git-BEL_extractor-test\\output\\resultOfTestingPDFs\\";
		File[] files = new File(input_folder).listFiles();
		int counter = 0;
		for(File file : files) {
			if(file.getName().contains("new")) {
				String s = Utility.readFromFile(file);
				counter++;
				if(s.contains("new paragraph")) {
					Utility.writeFile("output\\new_paragraph.txt", file.getName() + "\r\n", true);
				}
			}
		}
		System.out.println("in total " + files.length + " new_paragraph_3.txt files");
	}
	public static void moveFiles() {
		String path = "D:\\GitHub\\n-projects-ws-repo\\n-projects-ws-repo\\git-BEL_extractor-test\\output\\";
		try {
			BufferedReader  br= new BufferedReader(new InputStreamReader(new FileInputStream(path + "new_paragraph.txt"), "UTF-8"));
			String line;
			while((line = br.readLine()) != null) {
//				new File(path + "resultOfTestingPDFs\\" + line.trim()).
//				Files.copy(new File(path + "resultOfTestingPDFs\\" + line.trim()).toPath(), 
//						new File(path + "check_new_paragrahs\\" + line.trim()).toPath());
				Files.copy(new File("D:\\huangxcwd\\Data\\reddit\\odesk\\allpapers\\" + line.trim().substring(0, line.trim().indexOf(".pdf") + 4)).toPath(), 
						new File(path + "check_new_paragrahs\\" + line.trim().substring(0, line.trim().indexOf(".pdf") + 4)).toPath());
			}
		} catch ( IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
