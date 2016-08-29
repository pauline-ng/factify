package org.factpub.factify.ui.extension.firefox;
/**
 *  Author: Sun SAGONG
 *  Copyright (C) 2016, Genome Institute of Singapore, A*STAR
 *   
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *   
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *   
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * The following commented-out codes are GUI for Chrome extension.
 * By using it as a boiler plate, GUI of Firefox it to be also implemented.
 */

//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.io.ByteArrayInputStream;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.PrintStream;
//import java.lang.reflect.Type;
//import java.net.URL;
//import java.net.URLConnection;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.Map;
//
//import javax.swing.JButton;
//import javax.swing.JFrame;
//import javax.swing.JTextField;
//import javax.swing.JTextPane;
//import javax.swing.UIManager;
//
//import com.google.gson.Gson;
//import com.google.gson.JsonElement;
//import com.google.gson.JsonObject;
//import com.google.gson.JsonParser;
//import com.google.gson.reflect.TypeToken;
//
//public class FactifyChromeMain{
//
//	/* set true for production */
//	private static boolean productionMode = true;
//	
//	private static JFrame frameMain;
//	private static JTextField textField;
//	private static JTextPane textPane;
//	private static boolean FILE_LOG = false;
//	
//	public static void main(String[] args) throws IOException{
//				
//		//String DIR_JAR = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
//		//String DIR_JAR = URLDecoder.decode(jarPath, "UTF-8").substring(1);
//		
//		String DIR_JAR = "";
//		String DIR_TMP = "factify";
//
//		if(productionMode == true){
//			FILE_LOG = true;
//			setLog(FILE_LOG);
//		}
//		
//		showGUI();
//		
//		/*
//		 * Step1: Get PDF URL from extension.
//		 */
//		
//		String savedPDFPath = step1();
//				
//		/*
//		 * Step2: Creating temporal folder in the same directory to this program.
//		 */
//		
//		String dirTmp = step2(DIR_TMP);
//		
//		/*
//		 * Step2_1: Download Rule_INPUT files and save under the temporal folder.
//		 */
//		
//		String dirRuleInput = step2_1(DIR_TMP);
//		
//		/*
//		 * Step3: Retrieve PDF file from the URL and save into the folder created.
//		 */
//		
//		sendMessage("Step3 is dummy");
//		//String savedPDFPath = step3(pdf);
//		
//		// get saved PDF that is saved by the extension in tmp folder
//		//String savedPDFPath = step3_1(url);
//		
//		/*
//		 * Step4: Perform Factify and save JSON output.
//		 */
//		
//		String savedJSONPath = step4(savedPDFPath, DIR_TMP);
//
//		/*
//		 * Step5: Upload JSON file to factpub.org - serverRequestHandler.go
//		 */
//		
//		step5(savedJSONPath);
//		
//	}
//
//	private static void setLog(boolean flag) {
//		// TODO Auto-generated method stub
//		if(flag){
//			String logFile = "factify.log.txt";
//			
//			// Set up log output stream
//			FileOutputStream fos = null;
//			try {
//				fos = new FileOutputStream(logFile);
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			PrintStream ps = new PrintStream(fos);
//			System.setOut(ps);
//			System.setErr(ps);
//		}
//		
//	}
//
//	private static String step1() throws IOException {
//		// TODO Auto-generated method stub
//		sendMessage("Step1 has started! - Get serialized PDF from extension.");
//
//		String msgJson = null;
//		if(productionMode == true){
//			while(true){
//						msgJson = receiveMessage();
//						if(!msgJson.isEmpty()) break;					
//			}
//		}else{
////		msgJson = "{\"size\":3 ,\"pdf\":{\"0\":37,\"1\":80,\"2\":68}}";
////		
//		msgJson = "{\"pdf\":\"https://faculty.washington.edu/wjs18/cSNPs/SIFT1.pdf\"}";
//		}		
//
//		//sendMessage(msgJson);
//		sendMessage("pdfObject extraction start!");
//		System.out.println("msgJson: " + msgJson);
//		
//		JsonElement msgElm = new JsonParser().parse(msgJson);
//		System.out.println("msgElm: " + msgElm.toString());
//		
//		JsonObject msgObj = msgElm.getAsJsonObject();
//	    System.out.println("msgObj: " + msgObj.toString());
//	    
//
//	    /*
//	     *  For Name of PDF File.
//	     */
//	    JsonElement urlElm = msgObj.get("responseURL");
//	    String urlStr = urlElm.toString().replace('"',' ');
//	    String pdfName = urlStr.toString().replace(':','_').replace('/','_').replace('?', '_').replace(' ', '_') + ".pdf";
//	    String pdfSavedPath = pdfName;
//	    sendMessage("The PDF file name: " + pdfName);
//	    
//	    
//	    /*
//	     *  For Size of PDF File.
//	     */
//	    int pdfObjSize = msgObj.get("size").getAsInt();
//	    sendMessage("The PDF file size: " + pdfObjSize + " Bytes");
//	    
//	    
//	    
//	    /*
//	     *  For Data of PDF File. 
//	     */
//	    JsonElement pdfElm = msgObj.get("pdf");
//	    String pdfObjData = pdfElm.toString().replace('"',' ');
//   		
//	    //System.out.println(pdfObjData);
//	    
//	    sendMessage("pdfObject extraction done!");
//	    
//	    
//	    Gson gson = new Gson();
//	    Type type = new TypeToken<Map<String, Byte>>(){}.getType();
//	    Map<String, Byte> pdfByteMap = gson.fromJson(pdfObjData, type);
//	    
//		sendMessage("pdf: is extracted!");
//		
//		byte[] bufferedArray = new byte[pdfObjSize];
//		
//		int i = 0;
//		for (String key : pdfByteMap.keySet()) {
//		  bufferedArray[i] = pdfByteMap.get(key);
//		  i++;
//        }
//		
//		ByteArrayInputStream in = new ByteArrayInputStream(bufferedArray);
//				
//		sendMessage("start forming PDF from the binary.");
//		
//		FileOutputStream fileOutStm = null;
//		try {
//			fileOutStm = new FileOutputStream(pdfSavedPath);
//		} catch (FileNotFoundException e1) {
//			System.out.println("File does not exist!");
//		}
//		try {
//			fileOutStm.write(bufferedArray);
//		} catch (IOException e) {
//			System.out.println("stream error!");
//		}
//		
//		//Files.write(Paths.get("C:\\Users\\suns1\\Desktop\\127.0.0.1\\extensions_dev\\factify_chrome_extension\\host\\java\\factifyLog.pdf"), pdf);
//		
//		sendMessage("Step1 is done!");
//
//		return pdfSavedPath;		
//	}
//	
//	private static String step2(String dirTmp) throws IOException {
//		// TODO Auto-generated method stub
//		sendMessage("Step2 has started! - Creating temporal folder in the same directory to this program.");
//		sendMessage("Working Folder: " + dirTmp);
//		
//		String result = null;
//	
//		File tmpDir = new File(dirTmp);
//		deleteFiles(tmpDir);
//		if(!tmpDir.exists()){
//			tmpDir.mkdirs();
//		}
//		result = tmpDir.getAbsolutePath();
//		
//		//sendMessage(path);
//		
//		sendMessage("Step2 is done!");
//		return result;
//	}
//
//	private static String step2_1(String dirTmp) throws IOException {
//		// TODO Auto-generated method stub
//		
//		String result = null;
//		if(RuleInput.downloadRuleInputZip(dirTmp)){
//			sendMessage("downloading RuleInput.zip success!");
//		}else{
//			sendMessage("failed to download RuleInput.zip");
//		}
//		
//		return result;
//	}
//	
//	private static String step3(String pdfUrl) throws IOException {
//		// TODO Auto-generated method stub
//		sendMessage("Step3 has started! - Retrieve PDF file from the URL and save into the folder created");
//
//		URL url = new URL(pdfUrl); // Download URL
//		URLConnection conn = url.openConnection();
//		InputStream in = conn.getInputStream();
//
//		Path path = Paths.get(url.getPath());
//
//		File file = new File(FEConstants.DIR_JSON_OUTPUT + File.separator + path.getFileName()); // Save Destination 
//		FileOutputStream out = new FileOutputStream(file, false);
//		int b;
//		while((b = in.read()) != -1){
//		    out.write(b);
//		}
//
//		out.close();
//		in.close();
//		
//		sendMessage("Step3 is done!");
//		
//		return file.getAbsolutePath();
//
//	}
//	
//	private static String step4(String savedPDFPath, String dirTmp) throws IOException {
//		// TODO Auto-generated method stub
//		sendMessage("Step4 has started! - Perform Factify and save JSON output.");
//		
//		File savedPDF = new File(savedPDFPath);
//		String savedJSONPath = FactifyWrapper.runFactify(savedPDF, dirTmp);
//		
//		sendMessage("Step4 is done!");
//		
//		return savedJSONPath;
//	}
//
//	private static void step5(String savedJSONPath) throws IOException {
//		// TODO Auto-generated method stub
//		sendMessage("Step5 has started! - Upload JSON file to factpub.org - serverRequestHandler.go");
//		
//		File savedJSON = new File(savedJSONPath);
//		
//		try {
//			String pageURL = PostFile.uploadToFactpub(savedJSON);
//			sendMessage(pageURL);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		sendMessage("Step5 is done!");
//	}
//
//	static public String receiveMessage(){
//		// Read Chrome App input
//		byte[] b = new byte[4];
//		
//		try{
//			System.in.read(b);
//			int size = getInt(b);
//			
//			byte[] msg = new byte[size];
//			System.in.read(msg);
//			
//			String msgStr = new String(msg, "UTF-8");
//			
//			//updateTextPane(msgStr);
//			return msgStr;
//			//return msg.toString();
//			
//		}catch (IOException e){
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			updateTextPane("error occured!");
//			return null;
//		}
//		
//	}
//		
//	static public void sendMessage(String text) throws IOException {
//		// TODO Auto-generated method stub
//		
//	    JsonObject  msgObj =  new JsonParser().parse("{\"native\": \"" + text + "\"}").getAsJsonObject();
//	    updateTextPane(msgObj.toString());
//	    
//	    System.out.write(getBytes(msgObj.toString().length()));
//	    System.out.write(msgObj.toString().getBytes("UTF-8"));
//	    System.out.flush();
//	}
//	
//	public static int getInt(byte[] bytes) {
//        return (bytes[3] << 24) & 0xff000000|
//                (bytes[2] << 16)& 0x00ff0000|
//                (bytes[1] << 8) & 0x0000ff00|
//                (bytes[0] << 0) & 0x000000ff;
//    }
//	
//	static public byte[] getBytes(int length) {
//	    byte[] bytes = new byte[4];
//	    bytes[0] = (byte) (length & 0xFF);
//	    bytes[1] = (byte) ((length >> 8) & 0xFF);
//	    bytes[2] = (byte) ((length >> 16) & 0xFF);
//	    bytes[3] = (byte) ((length >> 24) & 0xFF);
//	    return bytes;
//	  }
//	
//	static void updateTextPane(String text){
//		String tmpStr = textPane.getText();
//		textPane.setText(tmpStr + "\n" + text);
//	}
//	
//	// Delete file or directory
//    private static void deleteFiles(File f){
//        
//        // Don't do anything if file or directory does not exist
//        if(f.exists() == false) {
//            return;
//        }
//
//        if(f.isFile()) {
//            
//            // if it's file, delete it.
//            f.delete();
//
//        }else if(f.isDirectory()){
//            // if it's directory, delete all the contents'
//            // get the contents
//            
//            File[] files = f.listFiles();
//            
//            //delete all files and directory
//            for(int i=0; i<files.length; i++) {
//            	// use recursion
//                deleteFiles( files[i] );
//            }
//            
//            // delete itself
//            f.delete();
//        }
//    }
//	
//	static public void showGUI(){
//		try {
//			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//		} catch (Exception e) {
//
//		}
//
//		frameMain = new JFrame();
//		frameMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frameMain.setBounds(100, 100, 987, 456);
//		frameMain.setTitle("PoC for extension");
//		frameMain.getContentPane().setLayout(null);
//		
//		textField = new JTextField();
//		textField.setBounds(28, 387, 783, 20);
//		frameMain.getContentPane().add(textField);
//		textField.setColumns(10);
//		
//		textPane = new JTextPane();
//		textPane.setBounds(28, 11, 915, 365);
//		frameMain.getContentPane().add(textPane);
//		
//		JButton btnNewButton = new JButton("Send");
//		btnNewButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				
//				try {
//					sendMessage("{\"text\":\"" + textField.getText() + "\"}");
//				} catch (IOException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}      
//			}
//		});
//		btnNewButton.setBounds(838, 386, 105, 23);
//		frameMain.getContentPane().add(btnNewButton);
//		
//		try {
//			FactifyChromeMain window = new FactifyChromeMain();
//			window.frameMain.setVisible(true);
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//}