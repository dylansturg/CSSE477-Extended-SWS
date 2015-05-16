/*
 * HTTPRequestFactory.java
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.Socket;

;

/**
 * 
 * @author Nathan Jarvis
 */

// Factory reads in the request verb and then creates through reflection the
// correct HTTPRequest
public class HTTPRequestFactory {

	public HTTPRequest createRequest(Socket socket) {
		InputStream inStream = null;
		String requestVerb = "";
		InputStreamReader reader;
		HTTPRequest httpRequestInstance;

		try {
			inStream = socket.getInputStream();

			reader = new InputStreamReader(inStream);
			int intChar;

			while ((intChar = reader.read()) != -1) {
				char ch = (char) intChar;
				if (Character.isWhitespace(ch)) {
					break;
				}
				requestVerb = requestVerb + ch;
			}
		} catch (Exception e) {
			// Log and send back bad request
			e.printStackTrace();

			return new MalformedHTTPRequest(socket);
		}

		try {
			Class<?> requestClass = Class.forName("request."
					+ requestVerb.toUpperCase() + "HTTPRequest");
			Constructor<?> constructor = requestClass.getConstructor(
					Socket.class, InputStreamReader.class);
			Object instance = constructor.newInstance(socket, reader);
			httpRequestInstance = (HTTPRequest) instance;

			httpRequestInstance.readHeadersAndBody();
			httpRequestInstance.checkRequest();
			httpRequestInstance.method = requestVerb;
		} catch (Exception e) {
			httpRequestInstance = new MalformedHTTPRequest(socket);
		}

		if (!isBadRequestPath(httpRequestInstance.path)) {
			return httpRequestInstance;
		} else {
			return new MalformedHTTPRequest(socket);
		}
	}

	public boolean isBadRequestPath(String path) {
		if (path != null && !path.contains("..")) {
			return false;
		} else {
			return true;
		}

	}
}
