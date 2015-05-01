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

import interfaces.IResourceStrategy;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.thoughtworks.xstream.*;

/**
 * Encapsulates information about the server's configuration. Doesn't really do
 * anything right now, but existing so dependency injection can be setup and
 * working for now.
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class ServerConfiguration implements IPluginAddedListener,
		IPluginRemovedListener, IConfigurationChangedListener {

	protected static final String MATCH_ALL_REGEX = "";
	protected static final String ROUTE_REGEX = "/%s/%s/";

	protected Map<String, String> configuration;

	protected Map<String, PluginData> availablePlugins;
	protected ResourceStrategyConfiguration managedResourceConfiguration;

	public ServerConfiguration(
			ResourceStrategyConfiguration managedResouceConfig) {
		managedResourceConfiguration = managedResouceConfig;
		availablePlugins = new HashMap<String, PluginData>();
		configuration = new HashMap<String, String>();
	}

	/**
	 * @return the managedResourceConfiguration
	 */
	public ResourceStrategyConfiguration getManagedResourceConfiguration() {
		return managedResourceConfiguration;
	}

	public void setConfigurationOption(String key, String value) {
		configuration.put(key, value);
	}

	/**
	 * @param managedResourceConfiguration
	 *            the managedResourceConfiguration to set
	 */
	public void setManagedResourceConfiguration(
			ResourceStrategyConfiguration managedResourceConfiguration) {
		this.managedResourceConfiguration = managedResourceConfiguration;
	}

	public void parseConfiguration(File configFile)
			throws InvalidConfigurationException {
		if (!configFile.exists() || !configFile.isFile()) {
			throw new InvalidConfigurationException(
					"Attempt to request configuration parse with nonfile not allowed.");
		}

		XStream parser = new XStream();

		parser.alias("routes", ArrayList.class);
		parser.alias("route", ServerRoute.class);
		parser.alias("options", Map.class);

		try {
			Object result = parser.fromXML(configFile);
			@SuppressWarnings("unchecked")
			List<ServerRoute> parsedRoutes = (List<ServerRoute>) result;

			List<ConfigurationWarning> warnings = new ArrayList<ConfigurationWarning>();
			List<ResourceStrategyRoute> resourceRoutes = new ArrayList<ResourceStrategyRoute>();
			for (ServerRoute serverRoute : parsedRoutes) {
				String pluginName = serverRoute.getPlugin();

				if (!availablePlugins.containsKey(pluginName)) {
					warnings.add(new ConfigurationWarning(String.format(
							"Failed to find plugin named %s", pluginName)));
					continue;
				}

				PluginData plugin = availablePlugins.get(pluginName);

				resourceRoutes.addAll(getRoutesForPlugin(warnings, serverRoute,
						plugin));
			}

			for (ConfigurationWarning configurationWarning : warnings) {
				Logger.getGlobal().log(Level.WARNING,
						configurationWarning.toString());
			}

			managedResourceConfiguration.setNewRoutes(resourceRoutes);
		} catch (Exception exp) {
			throw new InvalidConfigurationException(
					"Parsing configuration file failed - appears to be invalid file.",
					exp);
		}

	}

	private List<ResourceStrategyRoute> getRoutesForPlugin(
			List<ConfigurationWarning> warnings, ServerRoute serverRoute,
			PluginData plugin) {

		List<ResourceStrategyRoute> routes = new ArrayList<ResourceStrategyRoute>();
		String pluginName = plugin.getPluginName();

		try {
			ClassLoader loader;
			String jarPath = plugin.getJarPath();
			if (jarPath != null && !jarPath.isEmpty()) {
				URL[] urls = { new URL("jar:file:" + jarPath + "!/") };
				loader = URLClassLoader.newInstance(urls);
			} else {
				// Try to use whatever the current class loader is
				// it might work out
				loader = this.getClass().getClassLoader();
			}

			for (ServletData servlet : plugin.getServlets()) {
				try {
					Class<?> servClass = Class.forName(servlet.getClassPath(),
							true, loader);

					if (!IResourceStrategy.class.isAssignableFrom(servClass)) {
						warnings.add(new ConfigurationWarning(
								String.format(
										"Servlet class %s does not implement IResourceStrategy and is not valid",
										servClass.getName())));
						continue;
					}

					String servletRouteMatcher = formatServletRoute(
							serverRoute.getPath(), servlet.getRelativeUrl());

					// Includes server's defaults
					Map<String, String> options = new HashMap<String, String>(
							configuration);
					// Will override server defaults with route specific
					options.putAll(serverRoute.getOptions());

					ResourceStrategyRoute servletRoute = new ResourceStrategyRoute(
							servClass, servletRouteMatcher,
							servlet.getExpectedMethods(), options);
					routes.add(servletRoute);

				} catch (ClassNotFoundException e) {
					warnings.add(new ConfigurationWarning(
							String.format(
									"Failed to create Servlet class named %s when register routes for plugin %s",
									servlet.getClassPath(), pluginName)));
					continue;
				}
			}

		} catch (MalformedURLException badJarPath) {
			warnings.add(new ConfigurationWarning(String.format(
					"Failed to load servlet classes for plugin named %s",
					pluginName)));
		}

		return routes;
	}

	private String formatServletRoute(String pluginBase, String servletRelative) {
		// transform a pluginBase of form /my/plugin/path/ to my/plugin/path
		if (pluginBase.startsWith("/")) {
			pluginBase = pluginBase.substring(1);
		}
		if (pluginBase.endsWith("/")) {
			pluginBase = pluginBase.substring(0, pluginBase.length() - 1);
		}

		if (servletRelative == null || servletRelative.isEmpty()) {
			return String.format("/%s/", pluginBase);
		}

		if (servletRelative.startsWith("/")) {
			servletRelative = servletRelative.substring(1);
		}
		if (servletRelative.endsWith("/")) {
			servletRelative = servletRelative.substring(0,
					servletRelative.length() - 1);
		}

		return String.format(ROUTE_REGEX, pluginBase, servletRelative);
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

	@Override
	public void addedConfiguration(File configuration)
			throws InvalidConfigurationException {
		parseConfiguration(configuration);
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
