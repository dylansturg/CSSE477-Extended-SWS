/*
s * HttpResponseBaseFactory.java
 * Oct 7, 2012
 *
 * Simple Web Server (SWS) for CSSE 477
 * 
 * Copyright (C) 2012 Chandan Raj Rupakheti
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
 */

package protocol;

import interfaces.HttpResponseBase;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLEngineResult.Status;

import response.DefaultHttpResponse;

/**
 * This is a factory to produce various kind of HTTP responses.
 * 
 * @author Chandan R. Rupakheti (rupakhet@rose-hulman.edu)
 */
public class HttpResponseFactory {

	public static HttpResponseBase createGenericErrorResponse(
			HttpStatusCode code, String connectionStyle) {
		HttpResponseBase response = new DefaultHttpResponse(Protocol.VERSION,
				code, new HashMap<String, String>());
		return response;
	}

	public static HttpResponseBase createGenericSuccessfulResponse(
			HttpStatusCode code, String connectionStyle) {
		HttpResponseBase response = new DefaultHttpResponse(Protocol.VERSION,
				code, new HashMap<String, String>());
		return response;
	}
}
