/*
 * RequestTaskBase.java
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

package interfaces;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import server.Server;

/**
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public abstract class RequestTaskBase implements IRequestTask {
	protected List<IRequestTaskCompletionListener> completionListeners;
	private IHttpRequest request;
	protected boolean completed = false;
	protected boolean successful = false;
	protected Socket client;

	// Not accessible in implementation
	private Server server;

	protected long startTimestamp;

	protected HttpResponseBase response;

	private Date receivedTimeStamp = new Date();

	public RequestTaskBase(IHttpRequest request) {
		this.request = request;
		completionListeners = new ArrayList<IRequestTask.IRequestTaskCompletionListener>();
	}

	public final void setServer(Server serv) {
		server = serv;
	}

	public final Server getServer() {
		return server;
	}

	public final Date getReceivedTimeStamp() {
		return receivedTimeStamp;
	}

	@Override
	public void run() {

		if (Thread.currentThread().isInterrupted()) {
			completed = false;
			return;
		}

		if (!completed) {
			throw new IllegalStateException(
					"Attempt to RUN in RequestTaskBase without setting completed not allowed.");
		}

		for (IRequestTaskCompletionListener listener : completionListeners) {
			listener.taskComplete(this);
		}
	}

	@Override
	public void registerCompletionListener(
			IRequestTaskCompletionListener listener) {
		completionListeners.add(listener);

		if (completed) {
			listener.taskComplete(this);
		}
	}

	@Override
	public final IHttpRequest getRequest() {
		return request;
	}

	@Override
	public HttpResponseBase getResponse() {
		return response;
	}

	@Override
	public boolean isComplete() {
		return completed;
	}

	@Override
	public boolean wasSuccessful() {
		return successful;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see strategy.IRequestTask#writeResponse(java.io.OutputStream)
	 */
	@Override
	public final void writeResponse(OutputStream out) throws IOException {
		IHttpResponse response = getResponse();
		response.write(out);

		if (response.getStatusCode() < 400) {
			// 100s, 200s, and 300s indicate success (of some sort)
			successful = true;
		}
	}

	@Override
	public Socket getRequestingClient() {
		return this.client;
	}

	@Override
	public void setRequestingClient(Socket client) {
		this.client = client;
	}

	@Override
	public long getStartTime() {
		return startTimestamp;
	}

	@Override
	public void setStartTime(long timestamp) {
		this.startTimestamp = timestamp;
	}
}
