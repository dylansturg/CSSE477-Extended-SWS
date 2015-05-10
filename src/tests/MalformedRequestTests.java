/*
 * MalformedRequestTests.java
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

package tests;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;

import org.junit.Test;
import org.junit.Assert.*;

import request.DELETEHTTPRequest;
import request.GETHTTPRequest;
import request.HTTPRequest;
import request.HTTPRequestFactory;
import request.MalformedHTTPRequest;
import request.POSTHTTPRequest;
import request.PUTHTTPRequest;

/**
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class MalformedRequestTests {
	
	@Test
	public void testGetRequest() {
		HTTPRequest testRequest = new HTTPRequestFactory()
				.createRequest(new FakeSocket(
						"GET /path/to/stuff HTTP/1.1\r\nHost: SomeBody.com\r\nAuthor: NotAPerson\r\n"), "C:");
		assertEquals(testRequest.getClass(), GETHTTPRequest.class);
	}
	
	@Test
	public void testDeleteRequest() {
		HTTPRequest testRequest = new HTTPRequestFactory()
				.createRequest(new FakeSocket(
						"DELETE /path/to/stuff HTTP/1.1\r\nHost: SomeBody.com\r\nAuthor: NotAPerson\r\n"), "C:");
		assertEquals(testRequest.getClass(), DELETEHTTPRequest.class);
	}
	
	@Test
	public void testPostRequest() {
		HTTPRequest testRequest = new HTTPRequestFactory()
				.createRequest(new FakeSocket(
						"POST /path/to/stuff HTTP/1.1\r\nHost: SomeBody.com\r\nAuthor: NotAPerson\r\nContent-Length: 5\r\n\r\n12345"), "C:");
		assertEquals(5, testRequest.getBodyLength());
		assertEquals("12345", testRequest.getContent());
		assertEquals(testRequest.getClass(), POSTHTTPRequest.class);
	}
	
	@Test
	public void testPostRequestWithShortBody() {
		HTTPRequest testRequest = new HTTPRequestFactory()
				.createRequest(new FakeSocket(
						"POST /path/to/stuff HTTP/1.1\r\nHost: SomeBody.com\r\nAuthor: NotAPerson\r\nContent-Length: 50\r\n\r\n12345blah blah blah"), "C:");
		assertEquals(testRequest.getClass(), MalformedHTTPRequest.class);
	}
	
	@Test
	public void testPutRequest() {
		HTTPRequest testRequest = new HTTPRequestFactory()
				.createRequest(new FakeSocket(
						"PUT /path/to/stuff HTTP/1.1\r\nHost: SomeBody.com\r\nAuthor: NotAPerson\r\nContent-Length: 5\r\n\r\n12345"), "C:");
		assertEquals(5, testRequest.getBodyLength());
		assertEquals("12345", testRequest.getContent());
		assertEquals(testRequest.getClass(), PUTHTTPRequest.class);
	}
	
	@Test
	public void testPutRequestWithShortBody() {
		HTTPRequest testRequest = new HTTPRequestFactory()
				.createRequest(new FakeSocket(
						"PUT /path/to/stuff HTTP/1.1\r\nHost: SomeBody.com\r\nAuthor: NotAPerson\r\nContent-Length: 50\r\n\r\n12345blah blah blah"), "C:");
		assertEquals(testRequest.getClass(), MalformedHTTPRequest.class);
	}


	private class FakeSocket extends Socket {
		String writeOutToCaller = "";
		ByteArrayOutputStream output;
		ByteArrayInputStream input;

		public FakeSocket(String writeOut) {
			writeOutToCaller = writeOut;
			output = new ByteArrayOutputStream();
			input = new ByteArrayInputStream(writeOut.getBytes(Charset
					.defaultCharset()));
		}

		public String getWrittenString() {
			return output.toString();
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return input;
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			return output;
		}
	}
}
