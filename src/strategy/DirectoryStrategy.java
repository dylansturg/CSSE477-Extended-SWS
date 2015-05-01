/*
 * DirectoryStrategy.java
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

package strategy;

import interfaces.IRequestTask;

import java.lang.reflect.Constructor;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import configuration.ResourceStrategyRoute;
import protocol.HttpRequest;
import protocol.HttpResponseFactory;
import request.HTTPRequest;
import strategy.directoryoperations.RequestHandler;
import strategy.directoryoperations.UnsupportedRequestHandler;

/**
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class DirectoryStrategy extends ResourceStrategyBase {
	private static final String REQUEST_HANDLERS_PACKAGE = "strategy.directoryoperations";
	private static final String REQUEST_HANDLERS_POSTFIX = "RequestHandler";

	private static final Map<String, RequestHandler> REQUEST_HANDLERS;
	static {
		REQUEST_HANDLERS = Collections
				.synchronizedMap(new HashMap<String, RequestHandler>());
	}

	/*
	 * Evaluates directory based HTTP operations. Intended to be used with
	 * routes that map to directories which file operations can be performed.
	 * Will utilize classes in the directoryoperations package to delegate
	 * responsibilities for each type of request.
	 * 
	 * Method should be entirely reentrant. Best to avoid instance variables
	 * unless you're a multi-threading pro.
	 * 
	 * @see
	 * strategy.ResourceStrategyBase#prepareEvaluation(protocol.HttpRequest)
	 */
	@Override
	public IRequestTask prepareEvaluation(HTTPRequest request,
			ResourceStrategyRoute fromRoute) {

		RequestHandler handler = null;

		try {

			String verb = request.getMethod();
			String expectedHandlerClass = buildHandlerClassName(verb);

			Class<?> handlerClass = Class.forName(expectedHandlerClass);
			Constructor<?> handlerCtor = handlerClass.getConstructor();

			handler = (RequestHandler) handlerCtor.newInstance();
			if (handler == null) {
				throw new IllegalStateException(
						"Found a Constructor for the DirectoryStrategy RequestHandler but failed to construct it.");
			}

		} catch (Exception e) {
			// Unsupported Method/Verb Detected (no corresponding implementation
			// class)
			handler = new UnsupportedRequestHandler();
		}

		handler.setTriggeredRoute(fromRoute);
		// Defer all of the heavy lifting into the Task
		return new DirectoryRequestTask(handler, request);
	}

	private String buildHandlerClassName(String verb) {
		verb = verb.toLowerCase();
		verb = String.format("%s%s", verb.substring(0, 1).toUpperCase(),
				verb.substring(1));

		return String.format("%s.%s%s", REQUEST_HANDLERS_PACKAGE, verb,
				REQUEST_HANDLERS_POSTFIX);
	}

	private class DirectoryRequestTask extends RequestTaskBase {
		private RequestHandler handler;

		public DirectoryRequestTask(RequestHandler handler, HTTPRequest request) {
			super(request);
			this.handler = handler;
		}

		@Override
		public void run() {
			try {
				response = handler.handle(request);
			} catch (Exception exp) {
				// Internal Server Error

			}
			completed = true;
			super.run();
		}
	}
}
