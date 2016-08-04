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


package org.factpub.factify.ui.standalone;

import java.io.File;

import org.factpub.factify.Factify;
import org.factpub.factify.ui.standalone.utility.FEConstants;
import org.factpub.factify.utility.Utility;

public class FEWrapperGUI implements FEConstants{
	
	public static String fileNameMD5 = null;	

	public static String GUI_Wrapper(File file){
		
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
		
		
		args[0] = file.getPath(); 		// File: PDF with full path
		args[1] = DIR_JSON_OUTPUT + File.separator;		// Directory where JSON is output
		args[2] = DIR_JSON_OUTPUT + File.separator; 	// Directory for debug - can be suppressed.
		args[3] = DIR_RULE_INPUT + File.separator + "RuleMatcher.json";
		args[4] = ""; 			// File: output.file only - without pathargs[1] = DIR_JSON_OUTPUT + File.separator;		// Directory where JSON is output
		args[5] = "MD5"; 	// FILE: output_facts file path: or "MD5"
		
		//Use utility.utility.MD5() method in FactExtractor.jar to get MD5 filename.
		fileNameMD5 = Utility.getFileNameMD5(file);
		
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
		
		return errorMsg;
	}

}
