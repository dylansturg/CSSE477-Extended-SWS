/*
 * ResourceStrategyFinderTests.java
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

package tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import request.HTTPRequest;
import strategy.BadRequestStrategy;
import strategy.DirectoryStrategy;
import strategy.IResourceStrategy;
import strategy.ResourceStrategyFinder;
import configuration.ResourceStrategyConfiguration;
import configuration.ResourceStrategyRoute;
import configuration.ServerConfiguration;

/**
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class ResourceStrategyFinderTests {

	@Test
	public void testResourceFinderCanCreateDirectoryStrategy() {
		List<ResourceStrategyRoute> testRoutes = new ArrayList<ResourceStrategyRoute>();
		testRoutes.add(new ResourceStrategyRoute(DirectoryStrategy.class,
				"", Arrays.asList(new String[] { "GET" }), null));
		ResourceStrategyConfiguration testConfig = new ResourceStrategyConfiguration(
				testRoutes);

		ResourceStrategyFinder finder = new ResourceStrategyFinder(
				new ServerConfiguration(testConfig));

		ResourceStrategyRoute foundRoute = finder
				.findRouteForRequest(new FakeHttpRequest());

		IResourceStrategy strategy = finder
				.getStrategyForResourceRoute(foundRoute);

		assertNotNull(strategy);
		assertTrue(strategy.getClass() == DirectoryStrategy.class);
	}

	@Test
	public void testResourceFinderServesInternalErrorForUnMappedRoute() {
		ResourceStrategyConfiguration testConfig = createConfigurationForRoutes();
		ResourceStrategyFinder finder = new ResourceStrategyFinder(
				new ServerConfiguration(testConfig));

		IResourceStrategy strategy = finder.getStrategyForResourceRoute(finder
				.findRouteForRequest(new FakeHttpRequest()));

		assertNotNull(strategy);
		assertTrue(strategy.getClass() == BadRequestStrategy.class);

	}

	public ResourceStrategyConfiguration createConfigurationForRoutes(
			ResourceStrategyRoute... routes) {
		List<ResourceStrategyRoute> configurationRoutes = new ArrayList<ResourceStrategyRoute>();
		if (routes != null) {
			configurationRoutes.addAll(java.util.Arrays.asList(routes));
		}

		return new ResourceStrategyConfiguration(configurationRoutes);
	}

	public class FakeHttpRequest extends HTTPRequest {
		public String uri = "/";

		public FakeHttpRequest() {
			super(null);
		}

		@Override
		public String getPath() {
			return uri;
		}

		@Override
		public String getMethod() {
			return "GET";
		}
	}

}
