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

import interfaces.IHttpRequest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import protocol.Protocol;

/**
 * 
 * @author Nathan Jarvis
 */

// HTTP base request that specific requests extend.
public class HTTPRequest implements IHttpRequest {

	protected Socket readSocket;
	protected Socket writeSocket;

	InputStreamReader streamReader;

	String method;
	String path;
	String version;
	Map<String, String> headers;
	Map<String, String> queryString;
	String body;
	Boolean bodyPresent;
	int bodyLength;

	public HTTPRequest(Socket socket, InputStreamReader inStreamReader) {
		headers = new HashMap<String, String>();
		readSocket = socket;
		streamReader = inStreamReader;
		queryString = new HashMap<String, String>();
	}

	public HTTPRequest(Socket socket) {
		headers = new HashMap<String, String>();
		readSocket = socket;
	}

	public Map<String, String> getQueryStrings() {
		return queryString;
	}

	public String getQueryString(String key) {
		return queryString.get(key);
	}

	public String getMethod() {
		return method;
	}

	public String getPath() {
		return path;
	}

	public int getBodyLength() {
		return bodyLength;
	}

	// Read in the headers and their content and place in Map
	public void readHeadersAndBody() throws Exception {
		InputStream inStream;
		String[] requestHeader;
		String[] queryText;

		BufferedReader reader = null;
		if (streamReader == null) {
			inStream = this.readSocket.getInputStream();
			reader = new BufferedReader(new InputStreamReader(inStream));
		} else {
			reader = new BufferedReader(streamReader);
		}

		String line;

		line = reader.readLine();
		System.out.println("Request Line: " + line);
		requestHeader = line.split("\\s+");

		queryText = requestHeader[0].split("\\?");
		path = queryText[0];
		version = requestHeader[1];

		if (queryText.length > 1) {
			for (String pair : queryText[1].split("&")) {
				int idxOfEqual = pair.indexOf("=");

				if (idxOfEqual < 0) {
					queryString.put(pair, "");
				} else {
					String key = pair.substring(0, idxOfEqual);
					String value = pair.substring(idxOfEqual + 1);
					queryString.put(key, value);
				}
			}
		}

		while ((line = reader.readLine()) != null) {
			if (line.isEmpty()) {
				break;
			}
			if (line.equals(Protocol.CRLF)) {
				break;
			}

			if (line.contains(Protocol.SEPERATOR + "")) {
				String headerKey = line.substring(0, line.indexOf(":"));
				String headerContent = line.substring(line.indexOf(":") + 2,
						line.length());
				this.headers.put(headerKey, headerContent);
			}
		}

		// Read the body
		body = "";
		if (!this.headers.containsKey("Content-Length")) {
			bodyPresent = false;
		} else {
			String contentLength = this.headers.get("Content-Length");
			bodyLength = Integer.parseInt(contentLength);
			int count = 0;

			int read = 0;
			int remaining = bodyLength - count;
			StringBuilder bodyBuilder = new StringBuilder();
			char[] buffer = new char[Protocol.CHUNK_LENGTH];

			while (count < bodyLength
					&& (read = reader.read(buffer, 0,
							Math.min(Protocol.CHUNK_LENGTH, remaining))) != -1) {
				if (count < bodyLength) {
					bodyBuilder.append(buffer, 0, read);
					count += read;
				} else {
					// Extra content characters.
					throw new Exception("Body is longer than expected.");
				}
			}
			body = bodyBuilder.toString();
			bodyPresent = count > 0;
		}
	}

	public String getHeader(String key) {
		return headers.get(key);
	}

	public String getContent() {
		return body;
	}

	/**
	 * @throws Exception
	 * 
	 */
	public void checkRequest() throws Exception {
	}
}
