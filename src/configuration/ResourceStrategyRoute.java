/*
 * ResourceStrategyRoute.java
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
import java.util.Map;

/**
 * Storage object for a route that connects an HTTPRequest to a
 * IResourceStrategy implementation.
 * 
 * Contains a regex to match against any given criteria.
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class ResourceStrategyRoute implements IResourceRoute {

	public static final IResourceRoute None = new ResourceStrategyRouteNone();
	public static final IResourceRoute INVALID = new ResourceStrategyRouteInvalid();

	private Class<?> strategyClass;
	private String routeMatch;
	private List<String> methods;
	private Map<String, String> strategyOptions;

	public ResourceStrategyRoute(Class<?> strategy, String route,
			List<String> methods, Map<String, String> options) {
		strategyClass = strategy;
		routeMatch = route;
		this.methods = methods;
		strategyOptions = options;
	}

	/* (non-Javadoc)
	 * @see configuration.IResourceRoute#getStrategyClass()
	 */
	@Override
	public Class<?> getStrategyClass() {
		return strategyClass;
	}

	/* (non-Javadoc)
	 * @see configuration.IResourceRoute#getRouteMatch()
	 */
	@Override
	public String getRouteMatch() {
		return routeMatch;
	}

	/* (non-Javadoc)
	 * @see configuration.IResourceRoute#getStrategyOption(java.lang.String)
	 */
	@Override
	public String getStrategyOption(String option) {
		return strategyOptions.get(option);
	}

	/* (non-Javadoc)
	 * @see configuration.IResourceRoute#getMethods()
	 */
	@Override
	public List<String> getMethods() {
		return methods;
	}

	/* (non-Javadoc)
	 * @see configuration.IResourceRoute#respondsToMethod(java.lang.String)
	 */
	@Override
	public boolean respondsToMethod(String method) {
		for (String checkMethod : methods) {
			if (checkMethod.equalsIgnoreCase(method)) {
				return true;
			}
		}
		return false;
	}
}
