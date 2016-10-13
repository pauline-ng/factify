package org.factpub.factify.ui.extension.chrome;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class RuleInput {
	public static boolean downloadRuleInputZip(String dirTmp) {
		URL url;
		String dirRuleInput = dirTmp + File.separator + "Rule_INPUT";
		FileInputStream  fileIn  = null;
		FileOutputStream fileOut = null;
		try {
			url = new URL(FEConstants.SERVER_RULE_INPUT_ZIP);
			URLConnection uc = url.openConnection(); 
			ZipInputStream zipIn = new ZipInputStream(uc.getInputStream()); 
			//System.out.println("download success!");
	    	try{
	            File outDir = new File(dirRuleInput);
	            
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
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("IOException error downloading Rule_INPUT");
			return false;
		} 
		return true;
	}
}
