/*
 * GetRequestHandler.java
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

package strategy.directoryoperations;

import interfaces.HttpResponseBase;
import interfaces.IHttpRequest;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import protocol.HttpResponseFactory;
import protocol.HttpStatusCode;
import protocol.Protocol;
import server.GMTConversion;

/**
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class GetRequestHandler extends RequestHandler {

	/*
	 * Examines the current directory for the specified file path, and serves it
	 * in the response if allowed to do so.
	 * 
	 * @see
	 * strategy.directoryoperations.RequestHandler#handle(protocol.HttpRequest)
	 */
	@Override
	public HttpResponseBase handle(IHttpRequest request) {
		// request is guaranteed to be GetHttpRequest

		File requestedFile;
		try {
			requestedFile = lookupFileForRequestPath(request.getPath());
		} catch (Exception e) {
			// Due to an invalid configuration or coding error on our end
			return handleEvaluationError(HttpStatusCode.INTERNAL_ERROR);
		}

		if (requestedFile.exists()) {
			if (requestedFile.isFile()) {
				return serveFile(requestedFile, request);
			} else if (requestedFile.isDirectory() && shouldHandleDirectories()) {
				return serveDirectory(requestedFile, request);
			}
		}

		return handleEvaluationError(HttpStatusCode.NOT_FOUND);
	}

	private HttpResponseBase serveFile(File requestedFile, IHttpRequest request) {
		Map<String, String> headers = new HashMap<String, String>();

		String conditionalGet = request.getHeader(Protocol.CONDITIONAL_GET);
		if (conditionalGet != null && !conditionalGet.isEmpty()) {
			try {
				Date cachedVersion = GMTConversion.fromGMTString(conditionalGet
						.trim());
				long ticksSinceEpoch = requestedFile.lastModified();
				Date modifiedDate = new Date(ticksSinceEpoch);

				if (modifiedDate.before(cachedVersion)) {
					return new FileHttpResponse(Protocol.VERSION,
							HttpStatusCode.NOT_MODIFIED, headers, null);
				}

			} catch (ParseException e) {
				// Pass
				// Server gave us a datetime string we couldn't understand, act
				// like there wasn't one
				// Chandan's class failed us
			}
		}

		return new FileHttpResponse(Protocol.VERSION, HttpStatusCode.OK,
				headers, requestedFile);
	}

	private HttpResponseBase serveDirectory(File requestedDirectory,
			IHttpRequest request) {
		StringBuilder responseContent = new StringBuilder(
				"Contents of Directory - " + requestedDirectory.getName()
						+ Protocol.CRLF);

		File[] contents = requestedDirectory.listFiles();
		for (File file : contents) {
			responseContent.append(file.getName());
			responseContent.append(Protocol.CRLF);
		}

		Map<String, String> headers = new HashMap<String, String>();
		return new StaticContentResponse(Protocol.VERSION, HttpStatusCode.OK,
				headers, responseContent.toString());
	}

	private HttpResponseBase handleEvaluationError(
			HttpStatusCode errorDescription) {
		HttpResponseBase response = HttpResponseFactory
				.createGenericErrorResponse(errorDescription, Protocol.CLOSE);
		return response;
	}

	private class StaticContentResponse extends HttpResponseBase {

		private String staticContent;

		public StaticContentResponse(String version, HttpStatusCode status,
				Map<String, String> headers, String content) {
			super(version, status, headers);
			staticContent = content;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see protocol.HttpResponse#writeContent(java.io.BufferedOutputStream)
		 */
		@Override
		protected void writeContent(BufferedOutputStream outStream)
				throws IOException {
			outStream.write(staticContent.getBytes(Protocol.CHARSET));
		}

	}

	private class FileHttpResponse extends HttpResponseBase {

		protected File servedFile;

		public FileHttpResponse(String version, HttpStatusCode status,
				Map<String, String> headers, File file) {
			super(version, status, headers);

			servedFile = file;

			if (file != null) {

				// Lets add last modified date for the file
				long timeSinceEpoch = file.lastModified();
				Date modifiedTime = new Date(timeSinceEpoch);
				putHeader(Protocol.LAST_MODIFIED, modifiedTime.toString());

				// Lets get content length in bytes
				long length = file.length();
				putHeader(Protocol.CONTENT_LENGTH, length + "");

				// Lets get MIME type for the file
				FileNameMap fileNameMap = URLConnection.getFileNameMap();
				String mime = fileNameMap.getContentTypeFor(file.getName());

				if (mime != null) {
					putHeader(Protocol.CONTENT_TYPE, mime);
				}
			}
		}

		public File getFile() {
			return servedFile;
		}

		@Override
		protected void writeContent(BufferedOutputStream outStream)
				throws IOException {

			File requestedFile = getFile();
			if (requestedFile == null) {
				return; // nothing to write out
			}

			// We are reading a file
			if (this.getStatusCode() == Protocol.OK_CODE
					&& requestedFile != null) {
				// Process text documents
				FileInputStream fileInStream = new FileInputStream(
						requestedFile);
				BufferedInputStream inStream = new BufferedInputStream(
						fileInStream, Protocol.CHUNK_LENGTH);

				byte[] buffer = new byte[Protocol.CHUNK_LENGTH];
				int bytesRead = 0;
				// While there is some bytes to read from file, read each chunk
				// and send to the socket out stream
				while ((bytesRead = inStream.read(buffer)) != -1) {
					outStream.write(buffer, 0, bytesRead);
				}
				// Close the file input stream, we are done reading
				inStream.close();
			}
		}

	}

}
