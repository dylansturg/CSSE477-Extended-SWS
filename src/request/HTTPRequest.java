/*
 * HTTPRequest.java
 * Apr 23, 2015
 *
 * Simple Web Server (SWS) for EE407/507 and CS455/555
 * 
 * Copyright (C) 2011 Chandan Raj Rupakheti, Clarkson University
 * 
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either 
 * version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/lgpl.html>.
 * 
 * Contact Us:
 * Chandan Raj Rupakheti (rupakhcr@clarkson.edu)
 * Department of Electrical and Computer Engineering
 * Clarkson University
 * Potsdam
 * NY 13699-5722
 * http://clarkson.edu/~rupakhcr
 */
 
package request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Nathan Jarvis
 */
public class HTTPRequest {
	
	protected Socket readSocket;
	protected Socket writeSocket;
	String method;
	String path;
	String version;
	Map<String, String> headers;
	String body;
	Boolean bodyPresent;
	
	public HTTPRequest(Socket socket){
		headers = new HashMap<String,String>();
		readSocket = socket;
	}
	
	
	public void readHeaders() throws IOException{
		InputStream inStream;
		String[] requestHeader;
		inStream = this.readSocket.getInputStream();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
		String line;
		
		line = reader.readLine();
		requestHeader = line.split("\\s+");
		System.out.println("Requested path: " + requestHeader[0] + ", Request version: " + requestHeader[1] + "\n");
		
		path = requestHeader[0];
		version = requestHeader[1];
		
		while((line = reader.readLine()) != null){
			String headerKey = line.substring(0, line.indexOf(":"));
			String headerContent = line.substring(line.indexOf(":"), line.length());
			this.headers.put(headerKey, headerContent);
		}
	}
	
	public String getHeader(String key){
		return headers.get(key);
	}
	
	public String getContent(){
		return body;
	}
	
	protected void readBody() throws IOException{
		int bodyLength = -1;
		String contentLength = headers.get("Content-Length");
		if(contentLength == null){
			bodyPresent = false;
		}else{
			bodyLength = Integer.parseInt(contentLength);
			
			InputStream inStream;
			int count = 0;
			inStream = this.readSocket.getInputStream();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
			
			while(count < bodyLength){
				int intChar = reader.read();
				char ch = (char) intChar;
				body = body + ch;
				count++;
			}
			bodyPresent = true;
		}
	}
}
