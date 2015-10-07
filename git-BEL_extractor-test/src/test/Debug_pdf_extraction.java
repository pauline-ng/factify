package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;

import utility.Debug;
import utility.Debug.DEBUG_CONFIG;
import utility.utility;
import at.knowcenter.code.pdf.PdfExtractionPipeline;
/**
 * cav6qhz.pdf: pdf-extraction works terribly on this pdf.
 * @author huangxc
 *
 */
public class Debug_pdf_extraction {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		findFile();
		moveFiles();
	}
	
	public static void testOneFile() {
		String pdfpath = "D:\\huangxcwd\\Data\\reddit\\odesk\\allpapers\\cav6qhz.pdf";
		String outputPath = "output_pdf_extraction\\";
		String debugPath = "debugOutput_pdf_extraction\\";
		PdfExtractionPipeline  pipeline = new PdfExtractionPipeline();
		pipeline.setParameter((new File(pdfpath)).getName(), outputPath, debugPath);
		Debug.set(DEBUG_CONFIG.debug_textpieces, true);
		pipeline.runPipeline(new File(pdfpath));
	}
	
	public static void findFile() {
		String input_folder = "D:\\GitHub\\n-projects-ws-repo\\n-projects-ws-repo\\git-BEL_extractor-test\\output\\resultOfTestingPDFs\\";
		File[] files = new File(input_folder).listFiles();
		utility util = new utility();
		int counter = 0;
		for(File file : files) {
			if(file.getName().contains("new")) {
				String s = util.readFromFile(file);
				counter++;
				if(s.contains("new paragraph")) {
					util.writeFile("output\\new_paragraph.txt", file.getName() + "\r\n", true);
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
