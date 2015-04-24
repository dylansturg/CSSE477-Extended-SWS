/*
 * GetHTTPRequest.java
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

import java.io.InputStreamReader;
import java.net.Socket;

/**
 * 
 * @author Nathan Jarvis
 */
//Request that is created for GET
public class GETHTTPRequest extends HTTPRequest {

	public GETHTTPRequest(Socket socket, InputStreamReader reader) throws Exception {
		super(socket, reader);
		this.commonInit();
		checkBody();
	}

	/**
	 * @param socket
	 * @param headerMap
	 * @param verb
	 * @throws Exception 
	 */
	public GETHTTPRequest(Socket socket) throws Exception {
		super(socket);
		this.commonInit();
		checkBody();
	}

	public void checkBody() throws Exception {
		// TODO Check to see if valid GET request
		if (this.bodyPresent) {
			// Shouldn't be a body for get request.
			throw new Exception("Get request should not have body.");
		}
	}
}
