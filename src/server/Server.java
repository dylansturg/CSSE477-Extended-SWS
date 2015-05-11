/*
 * Server.java
 * Oct 7, 2012
 *
 * Simple Web Server (SWS) for CSSE 477
 * 
 * Copyright (C) 2012 Chandan Raj Rupakheti
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
 */

package server;

import gui.WebServer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.thoughtworks.xstream.XStream;

import request.HTTPRequestFactory;
import response.ResponseHandler;
import strategy.ResourceStrategyFinder;
import configuration.InvalidConfigurationException;
import configuration.ResourceStrategyConfiguration;
import configuration.ResourceStrategyRouteOptions;
import configuration.ServerConfiguration;
import configuration.ServletMonitor;
import configuration.ServletMonitor.IInitialParseCompleteListener;

/**
 * This represents a welcoming server for the incoming TCP request from a HTTP
 * client such as a web browser.
 * 
 * @author Chandan R. Rupakheti (rupakhet@rose-hulman.edu)
 */
public class Server implements Runnable {
	private String rootDirectory;
	private String configurationFile;
	private int port;
	private boolean stop;
	private ServerSocket welcomeSocket;

	private String blacklistFile;
	public ArrayList<String> blacklist;
	private Timer blacklistTimer;
	private HashMap<String, Integer> blacklistCounts;
	private int blacklistMaxCount = 10;
	private int blacklistResetFrequency = 3000;

	private long connections;
	private long serviceTime;

	private WebServer window;

	private ServerConfiguration configuration;
	private ServletMonitor monitor;
	private ResourceStrategyConfiguration resourcesConfiguration;

	/**
	 * @param rootDirectory
	 * @param port
	 */
	public Server(String rootDirectory, final String configFolder, int port,
			WebServer window) throws InvalidConfigurationException {
		this.rootDirectory = rootDirectory;
		this.configurationFile = configFolder + "\\routes.xml";
		this.blacklistFile = configFolder + "\\blacklist.xml";
		this.port = port;
		this.stop = false;
		this.connections = 0;
		this.serviceTime = 0;
		this.window = window;
		this.blacklistTimer = new Timer();
		this.blacklistCounts = new HashMap<String, Integer>();

		resourcesConfiguration = new ResourceStrategyConfiguration();
		configuration = new ServerConfiguration(resourcesConfiguration);
		monitor = new ServletMonitor(new IInitialParseCompleteListener() {
			@Override
			public void pluginsParsed() {
				try {
					configuration.parseConfiguration(new File(configFolder
							+ "\\routes.xml"));
				} catch (InvalidConfigurationException configExp) {

				}
			}
		});
		monitor.registerAddedListener(configuration);
		(new Thread(monitor)).start();

		File config = new File(this.blacklistFile);
		XStream streamer = new XStream();

		Object result = streamer.fromXML(config);
		blacklist = (ArrayList<String>) result;

		// Sets a default root directory as picked by user - servlets specific
		// can be set in server config xml
		configuration.setConfigurationOption(
				ResourceStrategyRouteOptions.RootDirectoy, rootDirectory);

		Map<String, String> options = new HashMap<String, String>();
		options.put(ResourceStrategyRouteOptions.RootDirectoy, rootDirectory);

		this.blacklistTimer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				Iterator<String> keySetIterator = blacklistCounts.keySet()
						.iterator();

				while (keySetIterator.hasNext()) {
					String key = keySetIterator.next();
					if (blacklistCounts.get(key) > 0) {
						blacklistCounts.put(key, 0);
					}
				}
			}
		}, this.blacklistResetFrequency, this.blacklistResetFrequency);

	}

	/**
	 * Gets the root directory for this web server.
	 * 
	 * @return the rootDirectory
	 */
	public String getRootDirectory() {
		return rootDirectory;
	}

	/**
	 * Gets the port number for this web server.
	 * 
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	public void reloadServerConfiguration() {
		if (configuration != null && configurationFile != null) {
			try {
				configuration.parseConfiguration(new File(configurationFile));
			} catch (InvalidConfigurationException exp) {

			}
		}
	}

	/**
	 * Returns connections serviced per second. Synchronized to be used in
	 * threaded environment.
	 * 
	 * @return
	 */
	public synchronized double getServiceRate() {
		if (this.serviceTime == 0)
			return Long.MIN_VALUE;
		double rate = this.connections / (double) this.serviceTime;
		rate = rate * 1000;
		return rate;
	}

	/**
	 * Increments number of connection by the supplied value. Synchronized to be
	 * used in threaded environment.
	 * 
	 * @param value
	 */
	public synchronized void incrementConnections(long value) {
		this.connections += value;
	}

	/**
	 * Increments the service time by the supplied value. Synchronized to be
	 * used in threaded environment.
	 * 
	 * @param value
	 */
	public synchronized void incrementServiceTime(long value) {
		this.serviceTime += value;
	}

	/**
	 * The entry method for the main server thread that accepts incoming TCP
	 * connection request and creates a {@link ConnectionHandler} for the
	 * request.
	 */
	public void run() {
		try {
			this.welcomeSocket = new ServerSocket(port);

			// Now keep welcoming new connections until stop flag is set to true
			while (true) {
				// Listen for incoming socket connection
				// This method block until somebody makes a request
				Socket connectionSocket = this.welcomeSocket.accept();

				String ip = connectionSocket.getInetAddress().toString();
				if (this.blacklist.contains(ip)) {
					connectionSocket.close();
					continue;
				}

				if (this.blacklistCounts.containsKey(ip)) {
					if (this.blacklistCounts.get(ip) > blacklistMaxCount) {
						this.updateBlacklist(ip);
					} else {
						int currentCount = this.blacklistCounts.get(ip);
						this.blacklistCounts.put(ip, currentCount + 1);
					}
				} else {
					this.blacklistCounts.put(ip, 1);
				}

				// Come out of the loop if the stop flag is set
				if (this.stop)
					break;

				ResponseHandler connectionResponseHandler = new ResponseHandler(
						configuration, this);
				HTTPRequestFactory connectionRequestFactory = new HTTPRequestFactory();
				ResourceStrategyFinder connectionResourceMapper = new ResourceStrategyFinder(
						configuration);

				ConnectionHandler handler = new ConnectionHandler(this,
						connectionResponseHandler, connectionRequestFactory,
						connectionResourceMapper);

				handler.serverClientSocket(connectionSocket);

				// Create a handler for this incoming connection and start the
				// handler in a new thread
				// ConnectionHandler handler = new ConnectionHandler(this,
				// connectionSocket);
				new Thread(handler).start();
				new Thread(connectionResponseHandler).start();
			}
			this.welcomeSocket.close();
		} catch (Exception e) {
			window.showSocketException(e);
		}
	}

	/**
	 * Stops the server from listening further.
	 */
	public synchronized void stop() {
		if (this.stop)
			return;

		// Set the stop flag to be true
		this.stop = true;
		try {
			// This will force welcomeSocket to come out of the blocked accept()
			// method
			// in the main loop of the start() method
			Socket socket = new Socket(InetAddress.getLocalHost(), port);

			// We do not have any other job for this socket so just close it
			socket.close();
		} catch (Exception e) {
		}
	}

	/**
	 * Checks if the server is stopeed or not.
	 * 
	 * @return
	 */
	public boolean isStoped() {
		if (this.welcomeSocket != null)
			return this.welcomeSocket.isClosed();
		return true;
	}

	public void updateBlacklist(String badIP) {
		this.blacklist.add(badIP);
		File blacklistConfig = new File(blacklistFile);
		XStream streamer = new XStream();

		try {
			blacklistConfig.createNewFile();
			FileOutputStream out = new FileOutputStream(blacklistConfig);
			streamer.toXML(this.blacklist, out);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
