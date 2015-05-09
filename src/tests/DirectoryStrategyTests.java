/*
 * DirectoryStrategyTests.java
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import interfaces.IRequestTask;
import interfaces.IResourceRoute;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;

import org.junit.Test;

import request.HTTPRequest;
import request.HTTPRequestFactory;
import strategy.DirectoryStrategy;
import strategy.directoryoperations.GetRequestHandler;
import strategy.directoryoperations.RequestHandler;
import configuration.ResourceStrategyRoute;

/**
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class DirectoryStrategyTests {

	@Test
	public void testDirectoryStrategyCreatesGetForGetRequest() {
		DirectoryStrategy testStrategy = new DirectoryStrategy();
		HTTPRequest testRequest = new HTTPRequestFactory()
				.createRequest(new FakeSocket(
						"GET /path/to/stuff HTTP/1.1\r\nHost: SomeBody.com\r\nAuthor: NotAPerson\r\n"));
		IResourceRoute testRoute = new ResourceStrategyRoute(
				DirectoryStrategy.class, "",
				Arrays.asList(new String[] { "GET" }),
				new HashMap<String, String>());

		IRequestTask task = testStrategy.prepareEvaluation(testRequest,
				testRoute);

		assertNotNull(task);

		try {
			Field field = task.getClass().getDeclaredField("handler");
			field.setAccessible(true);
			RequestHandler handlerForTask = (RequestHandler) field.get(task);

			assertEquals(handlerForTask.getClass(), GetRequestHandler.class);

		} catch (IllegalArgumentException | IllegalAccessException
				| NoSuchFieldException | SecurityException e) {
			fail();
		}

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
