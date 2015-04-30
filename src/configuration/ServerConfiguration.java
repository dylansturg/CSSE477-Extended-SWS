/*
 * ServerConfiguration.java
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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

import com.thoughtworks.xstream.*;

/**
 * Encapsulates information about the server's configuration. Doesn't really do
 * anything right now, but existing so dependency injection can be setup and
 * working for now.
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class ServerConfiguration implements IPluginAddedListener,
		IPluginRemovedLIstener {

	protected static final String MATCH_ALL_REGEX = "(.*?)";
	protected static final String ROUTE_REGEX = "^/{0}/{1}/";

	protected Map<String, PluginData> availablePlugins;

	public void parseConfiguration(File configFile) {
		XStream parser = new XStream();

		parser.alias("route", ServerRoute.class);
		parser.alias("options", Map.class);
		parser.alias("routes", List.class);

		if (configFile.exists()) {
			boolean exists = true;
		} else {
			boolean exists = false;
		}

		Object result = parser.fromXML(configFile);
		List<ServerRoute> parsedRoutes = (List<ServerRoute>) result;

		List<ConfigurationWarning> warnings = new ArrayList<ConfigurationWarning>();
		List<ResourceStrategyRoute> resourceRoutes = new ArrayList<ResourceStrategyRoute>();
		for (ServerRoute serverRoute : parsedRoutes) {
			String pluginName = serverRoute.getPlugin();
			String path = serverRoute.getPath();

			if (!availablePlugins.containsKey(pluginName)) {
				warnings.add(new ConfigurationWarning(String.format(
						"Failed to find plugin named {0}", pluginName)));
				continue;
			}
			PluginData plugin = availablePlugins.get(pluginName);

			try {
				URL[] urls = { new URL("jar:file:" + plugin.getJarPath() + "!/") };
				URLClassLoader classLoader = URLClassLoader.newInstance(urls);

				for (ServletData servlet : plugin.getServlets()) {
					try {
						Class<?> servClass = Class.forName(
								servlet.getClassPath(), true, classLoader);
						String servletRouteMatcher = String.format();

						ResourceStrategyRoute servletRoute = new ResourceStrategyRoute(
								servClass, servlet.getRelativeUrl(),
								serverRoute.getOptions());

					} catch (ClassNotFoundException e) {
						warnings.add(new ConfigurationWarning(
								String.format(
										"Failed to create Servlet class named {0} when register routes for plugin {1}",
										servlet.getClassPath(), pluginName)));
						continue;
					}
				}

			} catch (MalformedURLException badJarPath) {

			}

		}

	}

	@Override
	public void removedPlugin(PluginData plugin) {
		if (availablePlugins.containsKey(plugin.getPluginName())) {
			availablePlugins.remove(plugin.getPluginName());
		}
	}

	@Override
	public void addPlugin(PluginData plugin) {
		availablePlugins.put(plugin.getPluginName(), plugin);
	}

	class ConfigurationWarning {
		String description;

		public ConfigurationWarning(String desc) {
			this.description = desc;
		}

		@Override
		public String toString() {
			return description;
		}
	}
}
