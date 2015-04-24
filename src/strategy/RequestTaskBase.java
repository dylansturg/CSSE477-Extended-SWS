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

package strategy;

import java.awt.color.CMMException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import protocol.HttpRequest;
import protocol.HttpResponse;
import strategy.IRequestTask.IRequestTaskCompletionListener;

/**
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public abstract class RequestTaskBase implements IRequestTask {
	protected List<IRequestTaskCompletionListener> completionListeners;
	protected HttpRequest request;
	protected boolean completed = false;

	protected HttpResponse response;

	public RequestTaskBase(HttpRequest request) {
		this.request = request;
	}

	@Override
	public void run() {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see strategy.IRequestTask#getResponse()
	 */
	@Override
	public HttpResponse getResponse() {
		return response;
	}

	@Override
	public boolean isComplete() {
		return completed;
	}

	@Override
	public void writeResponse(Socket clientSocket) {
		// TODO Write out the created response into the given socket

	}

}
