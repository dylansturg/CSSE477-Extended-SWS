/*
 * PluginData.java
 * Apr 30, 2015
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

import java.util.List;

/**
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class PluginData {
	protected String pluginName;
	protected String jarPath;
	protected List<ServletData> servlets;

	/**
	 * @return the pluginName
	 */
	public String getPluginName() {
		return pluginName;
	}

	/**
	 * @param pluginName
	 *            the pluginName to set
	 */
	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}

	/**
	 * @return the jarPath
	 */
	public String getJarPath() {
		return jarPath;
	}

	/**
	 * @param jarPath
	 *            the jarPath to set
	 */
	public void setJarPath(String jarPath) {
		this.jarPath = jarPath;
	}

	/**
	 * @return the servlets
	 */
	public List<ServletData> getServlets() {
		return servlets;
	}

	/**
	 * @param servlets
	 *            the servlets to set
	 */
	public void setServlets(List<ServletData> servlets) {
		this.servlets = servlets;
	}

}
