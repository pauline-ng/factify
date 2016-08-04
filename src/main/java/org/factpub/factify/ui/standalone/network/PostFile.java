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
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.factpub.factify.ui.standalone.FEConstants;

public class PostFile {
  public static List<String> uploadToFactpub(File file) throws Exception {
	List<String> status = new ArrayList<String>();
	int i = 0;
	
    HttpClient httpclient = new DefaultHttpClient();
    httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
    
    String postUrl = FEConstants.SERVER_POST_HANDLER + "?id=" + AuthMediaWikiIdHTTP.authorisedUser;
    
    HttpPost httppost = new HttpPost(postUrl);
    
    System.out.println(postUrl);
   
    MultipartEntity mpEntity = new MultipartEntity();
    ContentBody cbFile = new FileBody(file, "json");
    
    // name must be "uploadfile". this is same on the server side.
    mpEntity.addPart(FEConstants.SERVER_UPLOAD_FILE_NAME, cbFile);

    httppost.setEntity(mpEntity);
    System.out.println("executing request " + httppost.getRequestLine());
    HttpResponse response = httpclient.execute(httppost);
    HttpEntity resEntity = response.getEntity();

    System.out.println(response.getStatusLine());
    
    if (response.getStatusLine().toString().contains("502 Bad Gateway")){
    	status.add("Looks server is down.");
    }else{
	    if (resEntity != null) {
	      status.add(EntityUtils.toString(resEntity));
	      System.out.println(status.get(i));
	      i++;
	    }
	    
	    if (resEntity != null) {
	      resEntity.consumeContent();
	    }
    }
    httpclient.getConnectionManager().shutdown();
    
    //String status = "Upload Success!";
    return status;
  }
  
}