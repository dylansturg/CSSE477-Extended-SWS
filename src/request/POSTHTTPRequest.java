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
// Request that is created for Delete
public class POSTHTTPRequest extends HTTPRequest {

	public POSTHTTPRequest(Socket socket, InputStreamReader reader)
			throws Exception {
		super(socket, reader);
		this.commonInit();
		checkRequest();
	}

	/**
	 * @param socket
	 * @param headerMap
	 * @param verb
	 * @throws Exception
	 */
	public POSTHTTPRequest(Socket socket) throws Exception {
		super(socket);
		this.commonInit();
		checkRequest();
	}

	// Check to see if valid request
	public void checkRequest() throws Exception {
		if (!this.bodyPresent) {
			// There should be a body for a post request
			throw new Exception("Post request should have a body.");
		}

		if (this.bodyLength == this.body.length()) {
			throw new Exception(
					"Post request body length is not equal to expected content length.");
		}
	}
}
