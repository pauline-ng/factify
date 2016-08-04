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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class InitTempDir implements FEConstants {

	public static String makeTempDir() {
		// Create a Directory under user home 
		File dirTemp = new File(DIR_FE_HOME);
		delete(dirTemp);
		if(!dirTemp.exists()){
			dirTemp.mkdirs();
		}
		return dirTemp.getAbsolutePath();
	}
	
	public static String makeJsonDir() {
		// Create a Directory under user home 
		File dirJSON = new File(DIR_JSON_OUTPUT);
		delete(dirJSON);
		if(!dirJSON.exists()){
			dirJSON.mkdirs();
		}
		return dirJSON.getAbsolutePath();
	}
	
	public static String makeLogFile() {
		// Create a Directory under user home 
		File fileLog = new File(FILE_LOG);
		if(!fileLog.exists()){
			try {
				fileLog.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("cannot create log file.");
				e.printStackTrace();
			}
		}
		return fileLog.getAbsolutePath();
	}
	
	public static void downloadRuleInputFiles() {
		URL url;
		FileInputStream  fileIn  = null;
		FileOutputStream fileOut = null;
		try {
			url = new URL(FEConstants.SERVER_RULE_INPUT_ZIP);
			URLConnection uc = url.openConnection(); 
			ZipInputStream zipIn = new ZipInputStream(uc.getInputStream()); 
			System.out.println("download success!");
	    	try{
	            File outDir = new File(FEConstants.DIR_RULE_INPUT);
	            
	            // Open zip file
	            ZipEntry entry = null;
	            while( ( entry = zipIn.getNextEntry() ) != null ){
	                if( entry.isDirectory() ){
	                    String relativePath = entry.getName();
	                    outDir = new File( outDir, relativePath );
	                    outDir.mkdirs();
	                    
	                } else {
	                    String relativePath = entry.getName();
	                    File   outFile = new File( outDir, relativePath );
	                    
	                    File   parentFile = outFile.getParentFile();
	                    parentFile.mkdirs();
	                    
	                    fileOut = new FileOutputStream( outFile );
	                    
	                    byte[] buf  = new byte[ 256 ];
	                    int    size = 0;
	                    while( ( size = zipIn.read( buf ) ) > 0 ){
	                        fileOut.write( buf, 0, size );
	                    }
	                    fileOut.close();
	                    fileOut = null;
	                }
	                zipIn.closeEntry();
	            }
	            
	        }catch( Exception e){
	            e.printStackTrace();
	            
	        } finally {
	            if(fileIn!= null){
	                try{
	                    fileIn.close();
	                }catch( Exception e){}
	            }
	            if( fileOut != null ){
	                try{
	                    fileOut.close();
	                }catch( Exception e){}
	            } 
	        }
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("MalformedURLException error downloading Rule_INPUT");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("IOException error downloading Rule_INPUT");
		} 		
	}
	
	public static void initTempDir() {
		InitTempDir.makeTempDir();
		InitTempDir.makeJsonDir();
		InitTempDir.downloadRuleInputFiles();
		
		// if you keep the log in log.txt, set FLAG_LOG = True 
		if(FEConstants.FLAG_LOG){
			String logFile = InitTempDir.makeLogFile();
			
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
		}
	}
    
    // Delete file or directory
    private static void delete(File f){
        
        // Don't do anything if file or directory does not exist
        if(f.exists() == false) {
            return;
        }

        if(f.isFile()) {
            
            // if it's file, delete it.
            f.delete();

        } else if(f.isDirectory()){
            // if it's directory, delete all the contents'
            // get the contents
            
            File[] files = f.listFiles();

            //delete all files and directory
            for(int i=0; i<files.length; i++) {
            	// use recursion
                delete( files[i] );
            }
            
            // delete itself
            f.delete();
        }
    }
	
}
