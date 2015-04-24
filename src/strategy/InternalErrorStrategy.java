/*
 * InternalErrorStrategy.java
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

import configuration.ResourceStrategyRoute;
import protocol.HttpRequest;
import protocol.HttpResponse;

/**
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class InternalErrorStrategy extends ResourceStrategyBase {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * strategy.ResourceStrategyBase#prepareEvaluation(protocol.HttpRequest,
	 * configuration.ResourceStrategyRoute)
	 */
	@Override
	public IRequestTask prepareEvaluation(HttpRequest request,
			ResourceStrategyRoute fromRoute) {

		return new InternalErrorRequestTask(request);
	}

	private class InternalErrorRequestTask extends RequestTaskBase {

		/**
		 * @param request
		 */
		public InternalErrorRequestTask(HttpRequest request) {
			super(request);
		}

		@Override
		public void run() {

			completed = true;
			super.run();
		}

		@Override
		public HttpResponse getResponse() {
			// TODO return HTTP 500 error
			return null;
		}

	}

}
