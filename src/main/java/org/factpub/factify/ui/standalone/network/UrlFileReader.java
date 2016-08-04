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

package org.factpub.factify.ui.standalone.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

public class UrlFileReader {
	private static ArrayList<String> contentList = null;
	private String content = null;
	public String FileReader(String link){
		try{
			URL url = new URL(link);
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			String line;
			contentList = new ArrayList<String>();
			while((line = in.readLine()) != null){
				contentList.add(line);
			}
			in.close();
			content = StringUtils.join(contentList, " ");
		}
		catch(MalformedURLException e){
			content = "NA";
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			content = "";
		}
		return content;
	}
}
