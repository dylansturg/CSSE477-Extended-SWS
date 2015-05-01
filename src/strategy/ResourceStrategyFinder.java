/*
 * ResourceStrategyFinder.java
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

import interfaces.IResourceStrategy;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import request.HTTPRequest;
import configuration.ResourceStrategyRoute;
import configuration.ServerConfiguration;

/**
 * Determines the appropriate IResourceStrategy concrete implementation for an
 * incoming HTTPRequest. The finder parses any necessary information about the
 * HTTPRequest and the server's current configuration.
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class ResourceStrategyFinder {
	private ServerConfiguration serverConfiguration;

	private static Map<String, IResourceStrategy> CACHED_STRATEGIES = new HashMap<String, IResourceStrategy>();

	public ResourceStrategyFinder(ServerConfiguration server) {
		serverConfiguration = server;
	}

	public ResourceStrategyRoute findRouteForRequest(HTTPRequest request) {
		return serverConfiguration.getManagedResourceConfiguration()
				.findRouteForResourcePath(request.getPath(), request.getMethod());
	}

	public IResourceStrategy getStrategyForResourceRoute(
			ResourceStrategyRoute resourceRoute) {

		Class<?> strategyClass = resourceRoute.getStrategyClass();

		if (CACHED_STRATEGIES.containsKey(strategyClass.getName())) {
			return CACHED_STRATEGIES.get(strategyClass.getName());
		}

		try {
			Constructor<?> strategyCtor = strategyClass.getConstructor();
			IResourceStrategy strategy = (IResourceStrategy) strategyCtor
					.newInstance();
			CACHED_STRATEGIES.put(strategyClass.getName(), strategy);
			return strategy;

		} catch (Exception e) {
			/*
			 * Lots of potential exceptions here. Java wants to make reflection
			 * hard.
			 */
			return new InternalErrorStrategy();
		}
	}
}
