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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import request.HTTPRequest;

;

/**
 * 
 * @author Nathan Jarvis
 */
public class HTTPRequestFactory {

	public HTTPRequest createRequest(Socket socket) {
		InputStream inStream = null;
		String requestVerb = "";
		InputStreamReader reader;

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
			// Cannot do anything if we have exception reading input or output
			// stream
			// May be have text to log this for further analysis?
			e.printStackTrace();

			return null;
		}

		try {
			Class<?> requestClass = Class.forName("request." + requestVerb
					+ "HTTPRequest");
			Constructor<?> constructor = requestClass.getConstructor(
					Socket.class, InputStreamReader.class);
			Object instance = constructor.newInstance(socket, reader);
			HTTPRequest httpRequestInstance = (HTTPRequest) instance;

			httpRequestInstance.method = requestVerb;

			return httpRequestInstance;
		} catch (Exception e) {
			// TODO handle exceptions sent back and create malformed request.

			MalformedHTTPRequest badRequest = new MalformedHTTPRequest(socket);
			badRequest.errorMessage = "";
			return badRequest;
		}
	}
}
