package org.factpub.factify.ui.extension.chrome;
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;

import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class FactifyChromeMain{

	/* set true for production */
	private static boolean productionMode = true;
	
	private static JFrame frameMain;
	private static JTextField textField;
	private static JTextPane textPane;
	private static boolean FILE_LOG = false;
	
	public static void main(String[] args) throws IOException{
				
		//String DIR_JAR = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		//String DIR_JAR = URLDecoder.decode(jarPath, "UTF-8").substring(1);
		
		String DIR_JAR = "";
		String DIR_TMP = "factify";


		showGUI();
		
		if(productionMode == true){

			// After everything is done.
			frameMain.setVisible(true);
			FILE_LOG = true;
			setLog(FILE_LOG);
		}
		
		
		
		sendMsgToExtension("url", java.nio.charset.Charset.defaultCharset().toString());
		/*
		 * Step1: receiveMsgFromChrome: get saved PDF path.
		 */
		
		String msgJson = null;
		if(productionMode == true){
			
				while(true){
							msgJson = receiveMessage();
							if(!msgJson.isEmpty()) break;					
				}
				showDebug("Received Message from Chrome");
			
		}else{
//		msgJson = "{\"size\":3 ,\"pdf\":{\"0\":37,\"1\":80,\"2\":68}}";
//		
		msgJson = "{\"pdf\":\"https://faculty.washington.edu/wjs18/cSNPs/SIFT1.pdf\"}";
		}		
		
		String savedPDFPath = null;
		String pdfObjData = null;
		int pdfObjSize = 0;
		
		String factpubId = "anonymous";
		try{
			// pre-processing to extract valuables
			System.out.println("msgJson: " + msgJson);
			
			JsonElement msgElm = new JsonParser().parse(msgJson);
			System.out.println("msgElm: " + msgElm.toString());
			
			JsonObject msgObj = msgElm.getAsJsonObject();
		    System.out.println("msgObj: " + msgObj.toString());
		    
		    // pdfUrl
		    JsonElement urlElm = msgObj.get("pdfUrl");
		    String urlStr = urlElm.toString().replace('"',' ');
		    String pdfName = urlStr.toString().replace(':','_').replace('/','_').replace('?', '_').replace(' ', '_') + ".pdf";
		    savedPDFPath = pdfName;
		    showDebug("The PDF file name: " + pdfName);
		    
		    // pdfSize
		    pdfObjSize = msgObj.get("pdfSize").getAsInt();
		    showDebug("The PDF file size: " + pdfObjSize + " Bytes");
		    
		    // pdfData
		    JsonElement pdfElm = msgObj.get("pdfData");
		    pdfObjData = pdfElm.toString().replace('"',' ');
	   		
		    // factpubId
		    factpubId = msgObj.get("factpubId").getAsString();
		    showDebug("The factpub ID: " + factpubId.toString());
		    
		    // notifId
		    String notifId = msgObj.get("notifId").getAsString();
		    showDebug("The notifId: " + notifId.toString());
		    frameMain.setTitle("Factify Chrome Host Program [Debug Window] : " + notifId);
		    
		    Gson gson = new Gson();
		    Type type = new TypeToken<Map<String, Byte>>(){}.getType();
		    Map<String, Byte> pdfByteMap = gson.fromJson(pdfObjData, type);
		    
			
			byte[] bufferedArray = new byte[pdfObjSize];
			
			int i = 0;
			for (String key : pdfByteMap.keySet()) {
			  bufferedArray[i] = pdfByteMap.get(key);
			  i++;
	        }
			
			ByteArrayInputStream in = new ByteArrayInputStream(bufferedArray);
					
			// start forming PDF from the binary.			
			FileOutputStream fileOutStm = null;
			try {
				fileOutStm = new FileOutputStream(savedPDFPath);
			} catch (FileNotFoundException e1) {
				System.err.println("[Error] File does not exist");
				showDebug("[Error] File does not exist");
			}
			try {
				fileOutStm.write(bufferedArray);
			} catch (IOException e) {
				System.err.println("[Error] Stream error");
				showDebug("[Error] Stream error");
			}
		   
		    		    
		}catch(Exception e){
			showDebug("[Error] Message extraction failed.");
			sendMsgToExtension("error", "Message extraction failed.");
		}
		
		
		/*
		 * Step2: Creating temporal folder in the same directory to this program.
		 */
		
		try{			
			
			File tmpDir = new File(DIR_TMP);
			deleteFiles(tmpDir);
			if(!tmpDir.exists()){
				tmpDir.mkdirs();
			}
			
			showDebug("Working folder: " + tmpDir.getCanonicalPath() + " is created.");

			if(RuleInput.downloadRuleInputZip(DIR_TMP)){
				showDebug("RuleInput.zip is downloaded");
			}else{
				showDebug("[Error] failed to download RuleInput.zip");
			}

		}catch(Exception e){
			showDebug("[Error] Failed to create tmp folder");
		}
		
		
		/*
		 * Step3: runFactify: Perform Factify and save JSON output.
		 */
		
		File savedPDF = new File(savedPDFPath);
		showDebug("Start running Factify. It will take for a while...");
		String savedJSONPath = FactifyWrapper.runFactify(savedPDF, DIR_TMP);		
		showDebug("Factify outputs: " + new File(savedJSONPath).getAbsolutePath());

		/*io]90i
		 * Step5: Upload JSON file to factpub.org - serverRequestHandler.go
		 */
		
		try{

			File savedJSON = new File(savedJSONPath);
			showDebug("Sending facts: " + savedJSON.getCanonicalPath());
			
			String pageURL = "null";
		
			pageURL = PostFile.uploadToFactpub(savedJSON, factpubId);
			showDebug("Generated Page URL: " + pageURL);
			
			sendMsgToExtension("url", pageURL);
			
		}catch(Exception e){
			showDebug("[Error] Failed to upload JSON");
		}
	}

	private static void setLog(boolean flag) {
		// TODO Auto-generated method stub
		if(flag){
			String logFile = "factify.log.txt";
			
			// Set up log output stream
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(logFile);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			PrintStream ps = new PrintStream(fos);
			System.setOut(ps);
			System.setErr(ps);
		}
		
	}
	
	private static void sendMsgToExtension(String item, String msg){
		try{
		    
		    //JsonParser parser = new JsonParser();
		    //String msgJson = parser.parse("{\"" + item + "\": \"" + msg + "\"}").getAsString();
		    
			JSONObject jsonMsg = new JSONObject();
			jsonMsg.put(item, msg);
			String msgJson = jsonMsg.toString();
		    
			showDebug(msgJson);
		    showDebug("Message Length:" + msgJson.length());
		    
//			[C implementation]
//		    int main(int argc, char* argv[]) {
//		        // Define our message
//		        char message[] = "{\"text\": \"This is a response message\"}";
//		        // Collect the length of the message
//		        unsigned int len = strlen(message);
//		        // We need to send the 4 bytes of length information
//		        printf("%c%c%c%c", (char) (len & 0xff),
//		                           (char) ((len>>8) & 0xFF),
//		                           (char) ((len>>16) & 0xFF),
//		                           (char) ((len>>24) & 0xFF));
//		        // Now we can output our message
//		        printf("%s", message);
//		        return 0;
//		    }
		    
//			[C++ implementation]		    
//		    int main(int argc, char* argv[]) {
//		        // Define our message
//		        std::string message = "{\"text\": \"This is a response message\"}";
//		        // Collect the length of the message
//		        unsigned int len = message.length();
		    
		    // We need to send the 4 bytes of length information
//		        std::cout << char(((len>>0) & 0xFF))
//		                  << char(((len>>8) & 0xFF))
//		                  << char(((len>>16) & 0xFF))
//		                  << char(((len>>24) & 0xFF));
//		        
		    // Now we can output our message
//		        std::cout << message;
//		        return 0;
//		    }
		    
		    try {
		    	
	            System.out.write(getBytes(msgJson.length()));
	            System.out.write(msgJson.getBytes("UTF-8"));
	            System.out.flush();
	            
	        } catch (IOException e) {
	        	System.err.println("error in sending message to JS");
	            showDebug("error in sending message to JS");
	        }
		    
		    // We need to send the 4 bytes of length information
//		    System.out.write((byte) msgJson.length());
//		    System.out.write((byte) 0);
//		    System.out.write((byte) 0);
//		    System.out.write((byte) 0);
//		    
//		    //updateTextPane(getBytes(msgJson.length()).toString());
//
//		    // We need to send the 4 bytes of length information
//		    //System.out.write(msgJson.getBytes("UTF-8"));
////		    for(int i = 0; i < msgJson.length() ; i++){
////		    	System.out.append(msgJson.charAt(i));
////		    }
//		    System.out.write(msgJson.getBytes("UTF-8"));
		    
		    updateTextPane(msgJson.getBytes("UTF-8").toString());
		    
		    System.out.flush();
		}catch(IOException e){
			showDebug("[Error] Sending Messages to Extention failed: " + e.toString());
		}
		
	}

	static public String receiveMessage(){
		// Read Chrome App input
		byte[] b = new byte[4];
		
		try{
			System.in.read(b);
			int size = getInt(b);
			
			byte[] msg = new byte[size];
			System.in.read(msg);
			
			String msgStr = new String(msg, "UTF-8");
			
			//updateTextPane(msgStr);
			return msgStr;
			//return msg.toString();
			
		}catch (IOException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
			updateTextPane("[Error] Unable to read message from Extension.");
			return null;
		}
		
	}
		
	static public void showDebug(String text){
		updateTextPane(text);
	}
	
	// Receive Message from Extension
	public static int getInt(byte[] bytes) {
        return (bytes[3] << 24) & 0xff000000|
                (bytes[2] << 16)& 0x00ff0000|
                (bytes[1] << 8) & 0x0000ff00|
                (bytes[0] << 0) & 0x000000ff;
    }
	
	// Write Message to Extension
	public static byte[] getBytes(int length) {
		byte[] bytes = new byte[4];
	    bytes[0] = (byte) (length & 0xFF);
	    bytes[1] = (byte) ((length >> 8) & 0xFF);
	    bytes[2] = (byte) ((length >> 16) & 0xFF);
	    bytes[3] = (byte) ((length >> 24) & 0xFF);
	    return bytes;
    }	
	
	static void updateTextPane(String text){
		String tmpStr = textPane.getText();
		textPane.setText(tmpStr + "\n" + text);
	}
	
	// Delete file or directory
    private static void deleteFiles(File f){
        
        // Don't do anything if file or directory does not exist
        if(f.exists() == false) {
            return;
        }

        if(f.isFile()) {
            
            // if it's file, delete it.
            f.delete();

        }else if(f.isDirectory()){
            // if it's directory, delete all the contents'
            // get the contents
            
            File[] files = f.listFiles();
            
            //delete all files and directory
            for(int i=0; i<files.length; i++) {
            	// use recursion
                deleteFiles( files[i] );
            }
            
            // delete itself
            f.delete();
        }
    }
	
	static public void showGUI(){
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {

		}

		frameMain = new JFrame();
		frameMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frameMain.setBounds(100, 100, 987, 456);
		frameMain.setTitle("Factify Chrome Host Program [Debug Window]");
		frameMain.getContentPane().setLayout(null);
		
		textField = new JTextField();
		textField.setBounds(28, 387, 783, 20);
		frameMain.getContentPane().add(textField);
		textField.setColumns(10);
		
		textPane = new JTextPane();
		textPane.setBounds(28, 11, 915, 365);
		frameMain.getContentPane().add(textPane);
		
		JButton btnNewButton = new JButton("Send");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				sendMsgToExtension("text", textField.getText());
			}
		});
		btnNewButton.setBounds(838, 386, 105, 23);
		frameMain.getContentPane().add(btnNewButton);
		
		try {
			FactifyChromeMain window = new FactifyChromeMain();
			window.frameMain.setVisible(true);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}