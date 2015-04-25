/*
 * RequestHandler.java
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

import java.io.File;

import configuration.ResourceStrategyRoute;
import configuration.ResourceStrategyRouteOptions;
import protocol.HttpResponse;
import protocol.HttpStatusCode;
import protocol.Protocol;
import request.HTTPRequest;

/**
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public abstract class RequestHandler {
	protected ResourceStrategyRoute triggeredRoute;

	public abstract HttpResponse handle(HTTPRequest request);

	public ResourceStrategyRoute getTriggeredRoute() {
		return triggeredRoute;
	}

	public void setTriggeredRoute(ResourceStrategyRoute route) {
		triggeredRoute = route;
	}

	protected File lookupFileForRequestPath(String path)
			throws Exception {
		if (triggeredRoute == null) {
			// Misconfigured Server
			throw new IllegalStateException(
					"Misconfigured Server Route - Handling request without a route");
		}

		String rootDirectory = triggeredRoute
				.getStrategyOption(ResourceStrategyRouteOptions.RootDirectoy);
		if (rootDirectory == null || rootDirectory.isEmpty()) {
			// Misconfigured Server
			throw new IllegalStateException(
					"Misconfigured Server Route - DirectoryStrategy without a RootDirectory");
		}

		String desiredFilePath = createFilePath(rootDirectory, path);
		File requestedFile = new File(desiredFilePath);

		return requestedFile;
	}

	protected boolean shouldHandleDirectories() {
		String allowsDirectoryOperations = triggeredRoute
				.getStrategyOption(ResourceStrategyRouteOptions.ServeDirectories);
		return allowsDirectoryOperations != null
				&& allowsDirectoryOperations.equalsIgnoreCase("true");
	}

	private String createFilePath(String rootDir, String path) {
		return rootDir + path;
	}
}
