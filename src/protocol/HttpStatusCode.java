/*
 * HttpStatusCode.java
 * Apr 24, 2015
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

package protocol;

/**
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class HttpStatusCode {

	public static final HttpStatusCode OK = Create(200, "OK");
	public static final HttpStatusCode NOT_MODIFIED = Create(304,
			"Not Modified");
	public static final HttpStatusCode BAD_REQUEST = Create(400, "Bad Request");
	public static final HttpStatusCode NOT_FOUND = Create(404, "Not Found");
	public static final HttpStatusCode METHOD_NOT_ALLOWED = Create(405,
			"Method Not Allowed");
	public static final HttpStatusCode LENGTH_REQUIRED = Create(411,
			"Length Required");
	public static final HttpStatusCode TEAPOT = Create(418, "I'm a Teapot");
	public static final HttpStatusCode INTERNAL_ERROR = Create(500,
			"Internal Server Error");

	private int statusCode;
	private String codeMessage;

	public HttpStatusCode(int code, String message) {
		statusCode = code;
		codeMessage = message;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getStatusMessage() {
		return codeMessage;
	}

	/**
	 * Not currently in use, but can be used to return a default page for
	 * certain status codes.
	 * 
	 * For example, a 404 could have a custom (fancy) page.
	 * 
	 * @return
	 */
	public String getPathToDefaultFile() {
		return null;
	}

	private static HttpStatusCode Create(int code, String message) {
		return new HttpStatusCode(code, message);
	}
}