/*
 * IResourceStrategy.java
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

package interfaces;

import configuration.ResourceStrategyRoute;
import request.HTTPRequest;

/**
 * Defines an implementation of a server-side resource management protocol. The
 * strategy will accept a HTTPRequest and create a unit of execution that will
 * generate a HTTPResponse from it.
 * 
 * Any IResourceStrategy may be cached and utilized multiple times, on multiple
 * threads, by multiple clients. Every method should be entirely reentrant. The
 * bulk of HTTPRequest processing/evaluation should occur in a returned
 * IRequestTask to be deferred and evaluated in a resource constrained manner.
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public interface IResourceStrategy {

	/***
	 * Use a HTTPRequest to determine the appropriate action to take and
	 * encapsulate that action into a Runnable. Once the Runnable is run, it
	 * will create a HTTPResponse that should be returned to the client.
	 * 
	 * @param request
	 * @return Runnable to execute on a thread which will generate a
	 *         HTTPResponse
	 */
	public IRequestTask prepareEvaluation(HTTPRequest request,
			ResourceStrategyRoute fromRoute);
}
