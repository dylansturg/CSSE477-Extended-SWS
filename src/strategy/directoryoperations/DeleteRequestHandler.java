/*
 * DeleteRequestHandler.java
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

import protocol.HttpResponseFactory;
import protocol.HttpStatusCode;
import protocol.Protocol;

/**
 * DELETE request will delete an existing file from the file system. If no such
 * file exists, an appropriate (404) response is returned.
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class DeleteRequestHandler extends RequestHandler {

	@Override
	public HttpResponseBase handle(IHttpRequest request) {

		File requestedFile;

		try {
			requestedFile = lookupFileForRequestPath(request.getPath());
		} catch (Exception e) {
			return HttpResponseFactory.createGenericErrorResponse(
					HttpStatusCode.INTERNAL_ERROR, Protocol.CLOSE);
		}

		if (!requestedFile.exists()) {
			return HttpResponseFactory.createGenericErrorResponse(
					HttpStatusCode.NOT_FOUND, Protocol.CLOSE);
		} else if (requestedFile.isDirectory() && !shouldHandleDirectories()) {
			return HttpResponseFactory.createGenericErrorResponse(
					HttpStatusCode.METHOD_NOT_ALLOWED, Protocol.CLOSE);
		}

		requestedFile.delete();

		return HttpResponseFactory.createGenericSuccessfulResponse(
				HttpStatusCode.NO_CONTENT, Protocol.CLOSE);
	}

}
