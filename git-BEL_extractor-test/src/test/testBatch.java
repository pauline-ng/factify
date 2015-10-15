package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import extractor.ExtractorBELExtractor;
import utility.Debug;
import utility.Debug.DEBUG_CONFIG;
import utility.utility;

public class testBatch {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		testFromFolder("D:\\huangxcwd\\Data\\reddit\\odesk\\allpapers\\");
//		clean();
//		System.out.println("test");
		if(args.length != 4) {
			Debug.print("Please specify 3 parameters: filePath, output_dir, debug_dir, and matcher file!", DEBUG_CONFIG.debug_error);
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
			testOneFile(args[0], args[1], args[2], args[3]);
		}else if(fileOrFolder == false) {
			testFromFolder(args[0], args[1], args[2], args[3]);
		}
		
	}
	
	public static void testFromFolder(String path, String output_dir, String debug_dir, String matcherFile) {
//		String path = "D:\\huangxcwd\\Data\\reddit\\odesk\\allpapers";
		File folder = new File(path);
		if(!folder.exists() || !folder.isDirectory()) {
			Debug.print(path + " is not a valid directory!", DEBUG_CONFIG.debug_error);
			return;
		}
		File[] listOfFiles = folder.listFiles();
		int total_pdf = 0;
		utility util = new utility();
//		String output_dir = "output\\resultOfTestingPDFs\\";
		String output_stat = output_dir + "stat.txt";
//		util.writeFile(output_stat, "", false);
		HashSet<String> finished = getFinishedSet(output_stat);
		System.out.println("finshed " + finished.size());
		for(File file : listOfFiles) {
//			if(!file.getName().equals("1hgs79.pdf")) {
//				continue;
//			}
			if(total_pdf > 500) return;
			if(finished.contains(file.getName())) {
				System.out.println("skip " + file.getName());
				continue;
			}
			if(file.getName().endsWith(".pdf")) {
				total_pdf++;
				System.out.println("process " + file.getName());
				int error = ExtractorBELExtractor.examplePDFExtractor_JSON(file.getAbsolutePath(), output_dir, debug_dir, matcherFile);
				util.writeFile(output_stat, total_pdf + "\t" + file.getName() + "\t" + error + "\r\n", true);
			}
			
		}
//		util.writeFile(output_stat, "in total\t" + total_pdf, true);
		
	}
	public static void testOneFile(String path, String output_dir, String debug_dir, String matcherFile) {
//		String path = "D:\\huangxcwd\\Data\\reddit\\odesk\\allpapers";
		File file = new File(path);
		if(!file.exists() || !file.isFile()) {
			Debug.print(path + " is not a valid file!", DEBUG_CONFIG.debug_error);
			return;
		}
		if(file.getName().endsWith(".pdf")) {
			System.out.println("process " + file.getName());
			int error = ExtractorBELExtractor.examplePDFExtractor_JSON(file.getAbsolutePath(), output_dir, debug_dir, matcherFile);
			Debug.print("Finished with errorcode " + error, DEBUG_CONFIG.debug_error);
		}
	}
	public static HashSet<String> getFinishedSet(String path) {
		File file = new File(path);
		if(!file.exists() || !file.isFile()) return new HashSet<String>();
		 BufferedReader br;
		 HashSet<String> result = new HashSet<String>();
		 try {
				 br= new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
				 String line;
				 while((line = br.readLine())!=null) {
					 StringTokenizer st = new StringTokenizer(line, "\t");
					 result.add(st.nextToken());
				 }
				 return result;
		 }
		 catch(Exception e) {
			 e.printStackTrace();
			 return null;
		 }
			
		
	}
	public static void clean() {
		String output_dir = "output\\resultOfTestingPDFs\\";
		String output_stat = output_dir + "stat.txt";
	
		File file = new File(output_stat);
		if(!file.exists() || !file.isFile()) return ;
		utility util = new utility();
		 BufferedReader br;
		 try {
				 br= new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
				 util.writeFile(output_stat + "1", "", false);
				 String line;
				 while((line = br.readLine())!=null) {
					 StringTokenizer st = new StringTokenizer(line, "\t");
					 String fileName = st.nextToken() ;
					 String errorCode = st.nextToken();
					 if(errorCode.equals("1") && util.readFromFile(new File(output_dir + fileName+ "_body_standard.txt")).length() == 0) {
						 util.writeFile(output_stat + "1", fileName + "\t" + "3" + "\r\n", true);
					 }else {
						 util.writeFile(output_stat + "1", line + "\r\n", true);
					 }
				 }
		 }
		 catch(Exception e) {
			 e.printStackTrace();
			 return ;
		 }
		
	}

}
 