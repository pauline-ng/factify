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


package org.factpub.factify.ui.standalone.utility;

import java.io.File;

public interface FEConstants {
	
	static final boolean FLAG_LOG = true;
	
	static final String WINDOW_TITLE = "factpub uploader";
	static final int TABLE_COLUMN_NUM = 2;
	static final int TABLE_COLUMN_FILE = 0;
	static final int TABLE_COLUMN_STATUS = 1;
	
	static final String[] TABLE_COLUMN_HEADINGS = {"File", "Status"};
	
	static final int MAX_THREADS = 2;
	
	static final String STATUS_UPLOADING = "Uploading...";
	static final String STATUS_UPLOAD_DONE = "Upload Success!";
	static final String STATUS_UPLOAD_FAILED = "Failed to upload.";
	
	// below constants must be the same to serverRequestHandler.go (serverside script)
	static final String SERVER_RES_TITLE_BEGIN = "BEGINOFPAGETITLE:";
	static final String SERVER_RES_TITLE_END = ":ENDOFPAGETITLE";
	
	static final String DIR_FE_HOME = System.getProperty("java.io.tmpdir") + "factpub";
	static final String DIR_RULE_INPUT =  DIR_FE_HOME + File.separator + "Rule_INPUT";
	static final String DIR_JSON_OUTPUT =  DIR_FE_HOME + File.separator + "JSON";
	
	static final String FILE_RULE_MATCHER = DIR_RULE_INPUT + File.separator + "RuleMatcher.json";
	static final String FILE_ANNOUNCEMENT = DIR_FE_HOME + File.separator + "announcement.txt";
	static final String FILE_LOG = DIR_FE_HOME + File.separator + "log.txt";
	static final String FILE_RULE_INPUT_ZIP = "/factify/Rule_INPUT.zip";
	
	static final String IP_ADDRESS = "factpub.org";
	
	static final String IMAGE_DND = "Drop-Academic-Papers(PDF)-Here.png";
	static final String IMAGE_ICON = "logo_factpub.png";
	

	static final String SERVER_PUBLIC = "http://" + IP_ADDRESS + "/public";
	static final String SERVER_RULE_INPUT_ZIP = SERVER_PUBLIC + FILE_RULE_INPUT_ZIP;
	
	static final String SERVER_ANNOUNCEMENT = "http://" + IP_ADDRESS + "/public/announcement.txt";
	static final String SERVER_TOP = "http://" + IP_ADDRESS + "/wiki/";
	
	static final String SERVER_POST_HANDLER = "http://" + IP_ADDRESS + ":8080/uploadGUIFactExtractor";
	//use https for security
	static final String SERVER_API = "http://" + IP_ADDRESS + "/wiki/api.php?";
	//static final String SERVER_API = "https://" + IP_ADDRESS + "/wiki/api.php?";

	static final String SERVER_UPLOAD_FILE_NAME = "uploadfile";
	
	static final String PAGE_CREATED = "http://" + IP_ADDRESS + "/wiki/index.php/";
	static final String PAGE_REGISTER = "http:/" + IP_ADDRESS + "/wiki/index.php?title=Special:UserLogin&returnto=FactPub&type=signup";

	static final String FE_STATUS_CODE_MINUS_1 = "Input parameter error";
	static final String FE_STATUS_CODE_0 = "Input file not exist";
	static final String FE_STATUS_CODE_1 = "Uploading...";
	static final String FE_STATUS_CODE_2 = "PDF Converter Failed";
	static final String FE_STATUS_CODE_3 = "PDF Converter Succeeded, but no body text (or section heading";
	static final String FE_STATUS_CODE_4 = "Facts Exists";
		
}
