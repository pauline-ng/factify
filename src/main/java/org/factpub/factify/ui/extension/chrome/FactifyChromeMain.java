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

	private static final String STEP_1_END = "(1/5)Receiving PDF data from extension.";
	private static final String STEP_2_END = "(2/5)Initializing Rule_Matching files.";
	private static final String STEP_3_END = "(3/5)Running extraction process.";
	private static final String STEP_4_END = "(4/5)Sending facts to factpub.org.";
	private static final String STEP_5_END = "(5/5)Facts are donated";
		
	private static PrintStream defaultSysOut = null;
	private static PrintStream logSysOut = null;
	
	public static void main(String[] args) throws IOException{
				
		defaultSysOut = System.out;
		logSysOut = System.out;
		
		String DIR_TMP = "factify";

		showGUI();
		
		if(productionMode == true){	
			String logFile = "factify.log.txt";

			FileOutputStream fos = null;

			try {
				fos = new FileOutputStream(logFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			logSysOut = new PrintStream(fos);
			System.setOut(logSysOut);
			System.setErr(logSysOut);
		}
		
		
		/*
		 * Step1: receiveMsgFromChrome: get saved PDF path.
		 */
		sendMsgToExtension("steps", STEP_1_END);

		String msgJson = null;

		while(true){
					msgJson = receiveMessage();
					if(!msgJson.isEmpty()) break;					
		}
		showDebug("Received Message from Chrome");			
		
		String savedPDFPath = null;
		String pdfObjData = null;
		int pdfObjSize = 0;
		
		String factpubId = null;
		try{
			// pre-processing to extract valuables
			//System.out.println("msgJson: " + msgJson);
			
			JsonElement msgElm = new JsonParser().parse(msgJson);
			//System.out.println("msgElm: " + msgElm.toString());
			
			JsonObject msgObj = msgElm.getAsJsonObject();
		    //System.out.println("msgObj: " + msgObj.toString());
		    
		    // pdfUrl
			try{
			    JsonElement urlElm = msgObj.get("pdfUrl");
			    String urlStr = urlElm.toString().replace('"',' ');
			    String pdfName = urlStr.toString().replace(':','_').replace('/','_').replace('?', '_').replace(' ', '_') + ".pdf";
			    savedPDFPath = pdfName;
			    showDebug("The PDF file name: " + pdfName);
			}catch(Exception e){
		    	sendMsgToExtension("error", "Failed to get PDF name.");
		    	System.exit(1);
			}
			
		    // pdfSize
		    try{
			    pdfObjSize = msgObj.get("pdfSize").getAsInt();
			    showDebug("The PDF file size: " + pdfObjSize + " Bytes");
		    }catch(Exception e){
		    	sendMsgToExtension("error", "Failed to get PDF size.");
		    	System.exit(1);
		    }
		    
		    // pdfData
		    try{
		    	JsonElement pdfElm = msgObj.get("pdfData");
		    	pdfObjData = pdfElm.toString().replace('"',' ');
		    }catch(Exception e){
		    	sendMsgToExtension("error", "Failed to get PDF binary string.");
		    	System.exit(1);
		    }
		    
		    // factpubId
		    // It must NOT be undefined in JavaScript by Sun SAGONG@13Oct2016
		    try{
			    factpubId = msgObj.get("factpubId").getAsString();
			    showDebug("The factpub ID: " + factpubId.toString());
		    }catch(Exception e){
		    	//sendMsgToExtension("error", "Failed to get factpubId.");
		    	factpubId = "anonymous";
		    }
		    
		    // notifId
		    try{
			    String notifId = msgObj.get("notifId").getAsString();
			    showDebug("The notifId: " + notifId.toString());
			    frameMain.setTitle("Factify Chrome Host Program [Debug Window] : " + notifId);
		    }catch(Exception e){
		    	sendMsgToExtension("error", "Failed to get notifId.");
		    	System.exit(1);
		    }
		    
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
				System.exit(1);
			}
			try {
				fileOutStm.write(bufferedArray);
			} catch (IOException e) {
				System.err.println("[Error] Stream error");
				showDebug("[Error] Stream error");
				System.exit(1);
			}
		   
		    		    
		}catch(Exception e){
			showDebug("[Error] Failed to serialize PDF.");
			sendMsgToExtension("error", "Failed to serialize PDF.");
			System.exit(1);
		}
		
		
		/*
		 * Step2: Creating temporal folder in the same directory to this program.
		 */
		sendMsgToExtension("steps", STEP_2_END);
		try{			
			
			File tmpDir = new File(DIR_TMP);
			deleteFiles(tmpDir);
			if(!tmpDir.exists()){
				tmpDir.mkdirs();
			}
			
			sendMsgToExtension("workdir", tmpDir.getCanonicalPath());
			showDebug("Working folder: " + tmpDir.getCanonicalPath() + " is created.");
		
		}catch(Exception e){
			showDebug("[Error] Failed to create tmp folder");
			sendMsgToExtension("error", "Failed to create tmp folder.");
			System.exit(1);
		}
		
		try{
			RuleInput.downloadRuleInputZip(DIR_TMP);
			showDebug("RuleInput.zip is downloaded");	

		}catch(Exception e){
			showDebug("[Error] Failed to download RuleInput.zip");
			sendMsgToExtension("error", "Failed to download RuleInput.zip.");
			System.exit(1);
		}
		
		
		
		/*
		 * Step3: runFactify: Perform Factify and save JSON output.
		 */
		sendMsgToExtension("steps", STEP_3_END);
		
		File savedPDF = new File(savedPDFPath);
		showDebug("Start running Factify. It will take for a while...");
		String savedJSONPath = FactifyWrapper.runFactify(savedPDF, DIR_TMP);		
		showDebug("Factify outputs: " + new File(savedJSONPath).getAbsolutePath());

		/*
		 * Step4: Upload JSON file to factpub.org - serverRequestHandler.go
		 */
		sendMsgToExtension("steps", STEP_4_END);
		try{
			
			File savedJSON = new File(savedJSONPath);
			sendMsgToExtension("json", savedJSON.getCanonicalPath());
			showDebug("Sending facts: " + savedJSON.getCanonicalPath());
			
			String pageURL = "null";
		
			pageURL = PostFile.uploadToFactpub(savedJSON, factpubId);
			showDebug("Generated Page URL: " + pageURL);
			
			
			/*
			 * Step5: Show Page Link
			 */
			sendMsgToExtension("steps", STEP_5_END);			
			sendMsgToExtension("url", pageURL);
			
			System.exit(0);
		}catch(Exception e){
			showDebug("[Error] Failed to upload JSON");	
			System.exit(1);
		}
	}

	private static void sendMsgToExtension(String item, String msg){
		// FIXME: this does not work in development mode because stdio switching fails.
		// it works under production mode.
		
		// Probably, this part is the most tricky part of this class.
		// You must need to understand how Native Messaging Protocol works well.
		// https://developer.chrome.com/extensions/nativeMessaging
		// 
		//
		// Make sure to set stdio is the same stream when this host program is called by Chrome Extension.
		// I spent crazy amount of time to identify this stdio redirection problem.
		// by Sun SAGONG @ 12Oct2016 around noon
		System.setOut(defaultSysOut);
		
		 //JsonParser parser = new JsonParser();
		 //String msgJson = parser.parse("{\"" + item + "\": \"" + msg + "\"}").getAsString();
		 
		String msgJson = "{\"" + item + "\" : \"" + msg + "\"}";
		 
//		JSONObject jsonMsg = new JSONObject();
//		jsonMsg.put(item, msg);
//		String msgJson = jsonMsg.toString();
//		
		showDebug(msgJson);
		
		
		try {
			
		    System.out.write(getBytes(msgJson.length()));
		    System.out.write(msgJson.getBytes("UTF-8"));
		    System.out.flush();
		    
		} catch (IOException e) {
			System.err.println("error in sending message to JS");
		    showDebug("error in sending message to JS");
		}
		
		// Set stdio back to the stream before this method is called.
		System.setOut(logSysOut);

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
		if(productionMode == false){
			String tmpStr = textPane.getText();
			textPane.setText(tmpStr + "\n" + text);
		}
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
			if(productionMode == false){
				window.frameMain.setVisible(true);
			}else{
				window.frameMain.setVisible(false);	
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}