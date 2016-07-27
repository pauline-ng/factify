package org.factpub.factify.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.StringTokenizer;

import org.factpub.factify.Main;

import utility.Debug;
import utility.Debug.DEBUG_CONFIG;
import utility.Utility;

public class testBatch {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		testFromFolder("D:\\huangxcwd\\Data\\reddit\\odesk\\allpapers\\");
//		clean();
//		System.out.println("test");
		{
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
			Calendar cal = Calendar.getInstance();
//			System.out.println(dateFormat.format(cal.getTime())); //2014/08/06 16:00:22
			Debug.debugFile = "D:\\debug-" + dateFormat.format(cal.getTime()) + ".txt";
		}
//		if(args.length != 4) {
//			Debug.print("Please specify 3 parameters: filePath, output_dir, debug_dir, and matcher file!", DEBUG_CONFIG.debug_error);
//			return;
//		}
//		if(!new File(args[0]).exists()) {
//			Debug.print("Please specify a valid input file or folder!", DEBUG_CONFIG.debug_error);
//			return;
//		}
		Boolean fileOrFolder = null;//true if it is a file
		if(new File(args[0]).isFile()) {
			fileOrFolder = true;
		}else if (new File(args[0]).isDirectory()) {
			fileOrFolder = false;
		}
		if(fileOrFolder == null) {
			Debug.println("Error: the input is undetermined! input is " + args[0], DEBUG_CONFIG.debug_error);
			return;
		}
		if(fileOrFolder == true) {
			testOneFile(args);
		}else if(fileOrFolder == false) {
			testFromFolder(args);
		}
		
	}
	
	public static void testFromFolder(String ...args ) {
//		String path = "D:\\huangxcwd\\Data\\reddit\\odesk\\allpapers";
		String path = args[0];
		String output_dir = args[1];
		File folder = new File(path);
		if(!folder.exists() || !folder.isDirectory()) {
			Debug.print(path + " is not a valid directory!", DEBUG_CONFIG.debug_error);
			return;
		}
		File[] listOfFiles = folder.listFiles();
		int total_pdf = 0;
//		String output_dir = "output\\resultOfTestingPDFs\\";
		String output_stat = output_dir + "stat.txt";
//		util.writeFile(output_stat, "", false);
		HashSet<String> finished = getFinishedSet(output_stat);
//		System.out.println("finshed " + finished.size());
		for(File file : listOfFiles) {
//			if(!file.getName().equals("1hgs79.pdf")) {
//				continue;
//			}
			//if(total_pdf > 500) return;
			if(finished.contains(file.getName())) {
				System.out.println("skip " + file.getName());
				continue;
			}
			if(file.getName().endsWith(".pdf")) {
				total_pdf++;
				System.out.println("process " + file.getName());
				String[] parameters = args.clone();
				parameters[0] = file.getAbsolutePath();
				int error = Main.examplePDFExtractor_JSON(parameters);
				Utility.writeFile(output_stat, total_pdf + "\t" + file.getName() + "\t" + error + "\r\n", true);
			}
			
		}
//		util.writeFile(output_stat, "in total\t" + total_pdf, true);
		
	}
	
	/**
	 * 
	 * @param args: String path, String output_dir, String debug_dir, String matcherFile, String debug_log
	 */
	public static void testOneFile(String ...args) {
//		String path = "D:\\huangxcwd\\Data\\reddit\\odesk\\allpapers";
//		File file = new File(path);
//		if(!file.exists() || !file.isFile()) {
//			Debug.print(path + " is not a valid file!", DEBUG_CONFIG.debug_error);
//			return;
//		}
//		if(file.getName().endsWith(".pdf")) {
			int error = Main.examplePDFExtractor_JSON(args);
			Debug.print("Finished with errorcode " + error, DEBUG_CONFIG.debug_error);
//		}
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
					 st.nextToken();
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
		 BufferedReader br;
		 try {
				 br= new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
				 Utility.writeFile(output_stat + "1", "", false);
				 String line;
				 while((line = br.readLine())!=null) {
					 StringTokenizer st = new StringTokenizer(line, "\t");
					 String fileName = st.nextToken() ;
					 String errorCode = st.nextToken();
					 if(errorCode.equals("1") && Utility.readFromFile(new File(output_dir + fileName+ "_body_standard.txt")).length() == 0) {
						 Utility.writeFile(output_stat + "1", fileName + "\t" + "3" + "\r\n", true);
					 }else {
						 Utility.writeFile(output_stat + "1", line + "\r\n", true);
					 }
				 }
		 }
		 catch(Exception e) {
			 e.printStackTrace();
			 return ;
		 }
		
	}

}
 