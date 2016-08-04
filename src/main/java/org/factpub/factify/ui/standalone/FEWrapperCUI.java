package org.factpub.factify.ui.standalone;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.factpub.factify.Factify;
import org.factpub.factify.ui.standalone.network.PostFile;
import org.factpub.factify.ui.standalone.utility.FEConstants;
import org.factpub.factify.utility.Utility;

public class FEWrapperCUI implements FEConstants {
	
	public static void launchCUI(String pdf){
		//FactExtractor logic starts here!
		
	/**
	 * @return ErrorCode:
	 * -1: input parameter error 
	 * 0: input file not exist; 
	 * 1: succeeded
	 * 2: PDF Converter Failed
	 * 3: PDF Converter succeeded, but no body text (or section heading)
	 * 4: Facts Exists.
	 */
		
	// When file is chosen, want to make sure the arguments are set.
    // set up the arguments for FactExtactor
	String[] args = new String[6];
	
	/*
	 * @param args
	 * 0: path
	 * 1: output_dir
	 * 2: debug_dir
	 * 3: matcher file (by default: RuleMatcher.json)
	 * 4: output_log
	 * 5: output_facts file path: or "MD5"
	 * @param output
	 */
	
	Path currentRelativePath = Paths.get("");
	String currentDir = currentRelativePath.toAbsolutePath().toString();
	System.out.println(currentDir);
	
	args[0] = pdf; 		// File: PDF with full path
	args[1] = currentDir + File.separator;		// Directory where JSON is output
	args[2] = DIR_JSON_OUTPUT + File.separator; 	// Directory for debug - can be suppressed.
	args[3] = DIR_RULE_INPUT + File.separator + "RuleMatcher.json";
	args[4] = ""; 			// File: output.file only - without pathargs[1] = DIR_JSON_OUTPUT + File.separator;		// Directory where JSON is output
	args[5] = "MD5"; 	// FILE: output_facts file path: or "MD5"
	
	int error = Factify.runFactify(args);    //<----------- Where FactExtractor is executed.		
	
	String errorMsg = null;
	
	switch (error){
	case -1:
		System.out.println(FE_STATUS_CODE_MINUS_1);
		errorMsg = FE_STATUS_CODE_MINUS_1;
		break;
	case 0:
		System.out.println(FE_STATUS_CODE_0);
		errorMsg = FE_STATUS_CODE_0;
		break;
	case 1:
		System.out.println(FE_STATUS_CODE_1);
		errorMsg = FE_STATUS_CODE_1;
		break;
	case 2:
		System.out.println(FE_STATUS_CODE_2);
		errorMsg = FE_STATUS_CODE_2;
		break;
	case 3:
		System.out.println(FE_STATUS_CODE_3);
		errorMsg = FE_STATUS_CODE_3;
		break;
	case 4:
		System.out.println(FE_STATUS_CODE_4);
		errorMsg = FE_STATUS_CODE_4;
		break;
	}
			
	System.out.println("++++++++++++++++++FactExtractor Performed++++++++++++++++++++");
	System.out.println(errorMsg);
	
	if(!errorMsg.equals(FE_STATUS_CODE_0)){
		String status = null;
		
		File json = new File(Utility.getFileNameMD5(pdf));
		
		try{
			List<String> res = PostFile.uploadToFactpub(json);    				
			
			// If the server returns page title, put it into the array so browser can open the page when user click it.
			if(res.get(0).contains(FEConstants.SERVER_RES_TITLE_BEGIN)){
				
				//Embedding HyperLink
				String pageTitle = (String) res.get(0).subSequence(res.get(0).indexOf(FEConstants.SERVER_RES_TITLE_BEGIN) + FEConstants.SERVER_RES_TITLE_BEGIN.length(), res.get(0).indexOf(FEConstants.SERVER_RES_TITLE_END));
				pageTitle = pageTitle.replace(" ", "_");
				System.out.println(pageTitle);
				String uriString = FEConstants.PAGE_CREATED + pageTitle;
				
//				// Launch Browser
//				Desktop desktop = Desktop.getDesktop();
//				if (pageTitle != null) {
//					try {
//						URI uri = new URI(uriString);
//						desktop.browse(uri);
//					} catch (URISyntaxException e) {
//						e.printStackTrace();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				}			
				
				status = "Page is created: " + uriString;
				
				//change table color        				
			}else{
				
				status = "Upload success but page was not created.";
				        				
			}
			
			// embed HTML to the label
			}catch(Exception e){
				
    			status = FEConstants.STATUS_UPLOAD_FAILED;
    			
			}
		System.out.println(status);
		
		}
	}
}
