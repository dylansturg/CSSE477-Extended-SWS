/*
 * ResourceStrategyConfiguration.java
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

package configuration;

import interfaces.IResourceRoute;

import java.util.ArrayList;
import java.util.List;

/**
 * Designed to receive a parsed configuration for all possible ResourceStrategy
 * implementations the server can access and mappings to them from various
 * resources the server might host.
 * 
 * Currently only returns a DirectoryStrategy which matches any request. But
 * provides a point for future improvements and flexible enhancements.
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class ResourceStrategyConfiguration {
	protected List<ResourceStrategyRoute> activeRoutes;

	public ResourceStrategyConfiguration() {
		activeRoutes = new ArrayList<ResourceStrategyRoute>();
	}

	public ResourceStrategyConfiguration(List<ResourceStrategyRoute> routes) {
		activeRoutes = routes;
	}

	protected void setNewRoutes(List<ResourceStrategyRoute> routes) {
		activeRoutes = routes;
	}

	public void addRoute(ResourceStrategyRoute route) {
		activeRoutes.add(route);
	}

	public IResourceRoute findRouteForResourcePath(String path,
			String method) {
		if (path == null) {
			return ResourceStrategyRoute.INVALID;
		}

		try {
			for (IResourceRoute resourceStrategyRoute : activeRoutes) {
				String routeRegex = resourceStrategyRoute.getRouteMatch();
				if (routeRegex != null && path.startsWith(routeRegex)) {
					if (resourceStrategyRoute.respondsToMethod(method)) {
						return resourceStrategyRoute;
					}
				}
			}
		} catch (Exception e) {
			// Pass
		}

		return ResourceStrategyRoute.None;
	}
}
