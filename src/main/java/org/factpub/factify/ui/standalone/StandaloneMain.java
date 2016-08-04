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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.factpub.factify.ui.standalone.gui.MainFrame;
import org.factpub.factify.ui.standalone.network.AuthMediaWikiIdHTTP;
import org.factpub.factify.ui.standalone.utility.FEConstants;


public class StandaloneMain implements FEConstants {

	public static void main(String[] args) throws ParseException {
		// TODO Auto-generated method stub
		
		// Initialize %userhome%/factpub folder
		InitTempDir.initTempDir();
		
		if(args.length > 0){
			// CUI mode
			
			//Create Option instance
	        Options options = new Options();

	        //-f Option
	        Option file =
	            OptionBuilder
	                .hasArg(true)
	                .withArgName("file")
	                .isRequired(true)
	                .withDescription("academic papers (pdfs): -f paper1.pdf -f paper2.pdf ...")
	                .withLongOpt("file")
	                .create("f");
	        
	        Option user =
		            OptionBuilder
		                .hasArg(true)
		                .withArgName("username")
		                .isRequired(false)
		                .withDescription("factpub id (requires <password>)")
		                .withLongOpt("user")
		                .create("u");   
	        
	        Option password =
		            OptionBuilder
		                .hasArg(true)
		                .withArgName("password")
		                .isRequired(false)
		                .withDescription("password (requires <username>)")
		                .withLongOpt("password")
		                .create("p");

	        options.addOption(file);
	        options.addOption(password);
	        options.addOption(user);

	        //Create Parser
	        CommandLineParser parser = new PosixParser();

	        //Analyze
	        CommandLine cmd = null;
	        try {
	            cmd = parser.parse(options, args);
	        } catch (ParseException e) {
	            //Show help and close
	            HelpFormatter help = new HelpFormatter();
	            help.printHelp("java -jar factpub_uploader.jar", options, true);
	            return;
	        }
	        
	        System.out.println("Run in CUI mode");
			
	        if (cmd.hasOption("u") && cmd.hasOption("p")){
	        	System.out.println("User authentication starts.");
	        	try {
					AuthMediaWikiIdHTTP.authMediaWikiAccount(cmd.getOptionValue("u"), cmd.getOptionValue("p"));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("login failed");
				}
	        }else if((cmd.hasOption("u") && !cmd.hasOption("p")) || (!cmd.hasOption("u") && cmd.hasOption("p"))){
	        	System.out.println("<username> and <password> must be given together.");
	        	System.out.println("Log in as Anonymouse user.");
	        }
	        
	        //Check result
	        if (cmd.hasOption("f")) {
	            String[] pdfs = cmd.getOptionValues("f");
	            for(String pdf : pdfs){
		            FEWrapperCUI.launchCUI(pdf);
	            }
	        }
	        
		}else{
			// GUI mode
			System.out.println("If you want to run in console, please give me arguments.");
			/*
				usage: factpub_uploader.jar -f <pdf file> [-p <password>] [-u <username>]
				 -f,--file <pdf file>       academic papers (pdfs)
				 -p,--password <password>   password (requires <username>)
				 -u,--user <username>       factpub id (requires <password>)
			 */
			
			System.out.println("Run in GUI mode");
			MainFrame.launchGUI();
		}
		
	}

}
