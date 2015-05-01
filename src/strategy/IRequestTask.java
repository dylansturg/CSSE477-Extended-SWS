/*
 * IRequestTask.java
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

import interfaces.HttpResponseBase;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Wrapper around Runnable to represent the action necesary for a HTTPRequest to
 * be evaluated and generate a HTTPResponse to be sent to the client.
 * 
 * Utilizes a listener to notify any observers of its completion through
 * IRequestTaskCompletionListener.
 * 
 * Should be executed on a background thread.
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public interface IRequestTask extends Runnable {

	public interface IRequestTaskCompletionListener {
		public void taskComplete(IRequestTask completed);
	}

	public void registerCompletionListener(
			IRequestTaskCompletionListener listener);

	public boolean isComplete();

	public HttpResponseBase getResponse();

	public void setRequestingClient(Socket client);

	public Socket getRequestingClient();

	public void writeResponse(OutputStream out) throws IOException;

	public long getStartTime();

	public void setStartTime(long timestamp);

}
