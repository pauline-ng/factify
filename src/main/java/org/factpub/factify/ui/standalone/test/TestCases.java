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

package org.factpub.factify.ui.standalone.test;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.factpub.factify.ui.standalone.FEConstants;
import org.factpub.factify.ui.standalone.FEWrapperGUI;
import org.factpub.factify.ui.standalone.InitTempDir;
import org.factpub.factify.ui.standalone.network.AuthMediaWikiIdHTTP;
import org.factpub.factify.ui.standalone.network.PostFile;
import org.factpub.factify.utility.Utility;
import org.junit.Test;

public class TestCases implements FEConstants {

	// Test Case: test server connection & announcement text can be fetched.
	@Test public void testCase0() throws Exception{
		System.out.println("testCase0");
		
		Utility util = new Utility();
		String content = util.getAnnouncement();
		
		assertEquals("", content);
	}
		
	// Test Case: test temporary folders are created without error.
	@Test public void testCase1() throws Exception{
		System.out.println("testCase1");
		
		InitTempDir.makeTempDir();
		InitTempDir.makeJsonDir();
		InitTempDir.makeLogFile();
		InitTempDir.downloadRuleInputFiles();
		
		File dirTemp = new File(DIR_FE_HOME);
		File dirJSON = new File(DIR_JSON_OUTPUT);
		File dirRuleINPUT = new File(DIR_RULE_INPUT);

		assertEquals(true, dirTemp.exists());
		assertEquals(true, dirJSON.exists());
		assertEquals(true, dirRuleINPUT.exists());
	}
	
	// Test Case: test MD5 output name is solely on the pdf contents; check different file name produce same MD5 checksum
	@Test public void testCase2(){
		System.out.println("testCase2");
	    // Set up input files
		
		//Please specify the path for the pdf files, sample academic papers, below.
		File pdf_1 = new File("pdf"+ File.separator + "samplepdf1.pdf");
		File pdf_2 = new File("pdf"+ File.separator + "samplepdf2.pdf");
		
		//Use utility.utility.MD5() method in FactExtractor.jar to get MD5 filename.
		String fileNameMD5_1 = Utility.getFileNameMD5(pdf_1);
		String fileNameMD5_2 = Utility.getFileNameMD5(pdf_2);
		
		System.out.println("file_1: " + fileNameMD5_1);		
		System.out.println("file_2: " + fileNameMD5_2);
		assertEquals(fileNameMD5_1, fileNameMD5_2);
	}
	
	// Test Case: test non_pdf file will not be processed and return error msg.
	@Test public void testCase3(){
		System.out.println("testCase3");
		
		//Please specify the path for the dummy non-pdf file below: test.txt
		File non_pdf = new File("C:\\Users\\suns1\\Desktop\\workspace\\factpub_uploader\\resources\\test\\test.txt");
		String msg = FEWrapperGUI.GUI_Wrapper(non_pdf);
		System.out.println(msg);
		System.out.println(FE_STATUS_CODE_2);
		assertEquals(FE_STATUS_CODE_2, msg); //PDF Converter Failed
	}

	// Test Case: test factextractor works well for normal pdf paper
	@Test public void testCase4(){
		System.out.println("testCase4");
		
		//Please specify the path for the pdf file, sample academic paper, below.
		File pdf = new File("C:\\Users\\suns1\\Desktop\\workspace\\factpub_uploader\\resources\\test\\sample_doi_1.pdf");
		String msg = FEWrapperGUI.GUI_Wrapper(pdf);
		
		System.out.println(msg);
		System.out.println(FE_STATUS_CODE_1);
		assertEquals(FE_STATUS_CODE_1, msg); //Uploading...
	}
	
	// Test Case: test uploading JSON file is successful and page title is returned.
	@Test public void testCase5() throws Exception{
		System.out.println("testCase5");
		
		// set JSON file under test folder.
		String pageTitleExpected = "Predicting_the_effects_of_coding_non-synonymous_variants_on_protein_function_using_the_SIFT_algorithm._Kumar,P";
		
		//Please specify the path for the JSON file, extracted from sample_doi.pdf, below.
		File json = new File("C:\\Users\\suns1\\Desktop\\workspace\\factpub_uploader\\resources\\test\\sample_doi_test_title_Predicting_The_Effects.json");
		List<String> res = PostFile.uploadToFactpub(json);
		
		String pageTitle = "test";
		if(res.get(0).contains(FEConstants.SERVER_RES_TITLE_BEGIN)){
			//Embedding HyperLink
			pageTitle = (String) res.get(0).subSequence(res.get(0).indexOf(FEConstants.SERVER_RES_TITLE_BEGIN) + FEConstants.SERVER_RES_TITLE_BEGIN.length(), res.get(0).indexOf(FEConstants.SERVER_RES_TITLE_END));
			pageTitle = pageTitle.replace(" ", "_");
			System.out.println(pageTitle);
		}
		
		System.out.println();
		assertEquals(pageTitleExpected, pageTitle);		
	}
		
	// Test Case: test login function works well.
	@Test public void testCase6() throws Exception{
		System.out.println("testCase6");
		
		// Please set up valid user id and password for factpub.org
		String user_id = "dummyid";
		String user_pass = "dummypass";
		
		AuthMediaWikiIdHTTP.authorisedUser = "Anonymous";
		AuthMediaWikiIdHTTP.authMediaWikiAccount(user_id, user_pass);
		assertEquals(user_id, AuthMediaWikiIdHTTP.authorisedUser);
	}
	
}
