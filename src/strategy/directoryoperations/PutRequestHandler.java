/*
 * PutRequestHandler.java
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

package strategy.directoryoperations;

import interfaces.HttpResponseBase;
import interfaces.IHttpRequest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import protocol.HttpResponseFactory;
import protocol.HttpStatusCode;
import protocol.Protocol;
import request.HTTPRequest;

/**
 * PUT creates a new file, if necessary, and appends the request content to the
 * end of the specified file.
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class PutRequestHandler extends RequestHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * strategy.directoryoperations.RequestHandler#handle(request.HTTPRequest)
	 */
	@Override
	public HttpResponseBase handle(IHttpRequest request) {
		File requestedFile;
		try {
			requestedFile = lookupFileForRequestPath(request.getPath());
		} catch (Exception e) {
			return HttpResponseFactory.createGenericErrorResponse(
					HttpStatusCode.INTERNAL_ERROR, Protocol.CLOSE);
		}

		HttpStatusCode responseCode = HttpStatusCode.OK;
		if (!requestedFile.exists()) {
			responseCode = HttpStatusCode.CREATED;
			try {
				requestedFile.createNewFile();
			} catch (IOException e) {
				// TODO Log the exception
				return HttpResponseFactory.createGenericErrorResponse(
						HttpStatusCode.INTERNAL_ERROR, Protocol.CLOSE);
			}
		} else if (requestedFile.isDirectory()) {
			return HttpResponseFactory.createGenericErrorResponse(
					HttpStatusCode.METHOD_NOT_ALLOWED, Protocol.CLOSE);
		}

		try {
			FileWriter writer = new FileWriter(requestedFile, true);
			writer.write(request.getContent());
			writer.close();

			return HttpResponseFactory.createGenericSuccessfulResponse(
					responseCode, Protocol.CLOSE);

		} catch (IOException e) {
			// TODO log the exception
			return HttpResponseFactory.createGenericErrorResponse(
					HttpStatusCode.INTERNAL_ERROR, Protocol.CLOSE);
		}

	}

}
