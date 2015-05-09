/*
 * ConnectionHandler.java
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

package server;

import interfaces.IResourceRoute;
import interfaces.IResourceStrategy;
import interfaces.RequestTaskBase;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import protocol.HttpRequest;
import request.HTTPRequest;
import request.HTTPRequestFactory;
import response.ResponseHandler;
import strategy.ResourceStrategyFinder;

/**
 * This class is responsible for handling a incoming request by creating a
 * {@link HttpRequest} object and sending the appropriate response be creating a
 * {@link HttpResponse} object. It implements {@link Runnable} to be used in
 * multi-threaded environment.
 * 
 * @author Chandan R. Rupakheti (rupakhet@rose-hulman.edu)
 */
public class ConnectionHandler implements Runnable {
	private Server server;
	private Socket socket;
	private ResponseHandler responseHandler;
	private HTTPRequestFactory requestFactory;
	private ResourceStrategyFinder resourceStrategyMapper;

	private Map<Socket, InputStream> clientInputStreams;

	private volatile boolean stopped = false;

	public ConnectionHandler(Server server, ResponseHandler responseHandler,
			HTTPRequestFactory requestFactory,
			ResourceStrategyFinder resourceMapper) {
		this.setServer(server);
		this.responseHandler = responseHandler;
		this.requestFactory = requestFactory;
		this.resourceStrategyMapper = resourceMapper;

		this.clientInputStreams = Collections
				.synchronizedMap(new HashMap<Socket, InputStream>());
	}

	public void serverClientSocket(Socket client) throws IOException {
		this.socket = client;
		InputStream inStream = client.getInputStream();
		OutputStream outStream = client.getOutputStream();

		this.clientInputStreams.put(client, inStream);
		this.responseHandler.addClientToServed(client, outStream);
	}

	/**
	 * @return the socket
	 */
	public Socket getSocket() {
		return socket;
	}

	public void stop() {
		stopped = true;
	}

	private HTTPRequest awaitAndReadHttpRequest() {
		return requestFactory.createRequest(socket);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		serveClient(socket);
	}

	private void serveClient(Socket client) {
		if (client != socket) {
			throw new IllegalStateException(
					"ConnectionHandler does not currently support serving multiple clients");
		}

		if (!this.stopped) {
			HTTPRequest incomingRequest = awaitAndReadHttpRequest();
			// Start the timer after a request comes in - no point counting dead
			// time

			long requestStartTimeStamp = System.currentTimeMillis();
			IResourceRoute requestRoute = resourceStrategyMapper
					.findRouteForRequest(incomingRequest);
			IResourceStrategy strategyForRequest = resourceStrategyMapper
					.getStrategyForResourceRoute(requestRoute);

			RequestTaskBase requestTask = strategyForRequest.prepareEvaluation(
					incomingRequest, requestRoute);
			requestTask.setStartTime(requestStartTimeStamp);

			responseHandler.enqueueRequestTaskForClient(requestTask, socket);
			this.stopped = true;
		}
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}
}
