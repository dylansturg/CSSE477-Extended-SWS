/*
 * ServletMonitor.java
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

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * @author Nathan Jarvis
 */
public class ServletMonitor implements Runnable {
	
	public interface IInitialParseCompleteListener{
		public void pluginsParsed();
	}
	
	private List<IPluginAddedListener> addedListeners;
	private List<IPluginRemovedListener> removedListeners;
	private static String extensionToUse = ".jar";
	private static final String CONFIG_FILE_NAME = "plugin.xml";
	
	private IInitialParseCompleteListener parseCompleteListener;
	
	private ArrayList<PluginData> plugins;

	public ServletMonitor(IInitialParseCompleteListener completeListener) {
		addedListeners = new ArrayList<IPluginAddedListener>();
		removedListeners = new ArrayList<IPluginRemovedListener>();
		plugins = new ArrayList<PluginData>();
		parseCompleteListener = completeListener;
	}
	
	public ArrayList<PluginData> getPlugins(){
		return plugins;
	}

	public void findExistingFiles(Path folder) {
		File dir = new File(folder.toString());
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			for (File child : directoryListing) {
				// Do something with child
				if (child.toString().contains(extensionToUse)) {
					System.out.println(child);
					// Call to register the plugin
					this.readPluginConfig(child.toPath());
					// PluginManager.sharedInstance.registerPlugin(child.toPath());

				}
			}
		} else {
			// Handle the case where dir is not really a directory.
			// Checking dir.isDirectory() above would not be sufficient
			// to avoid race conditions with another process that deletes
			// directories.
		}
		
		if(parseCompleteListener != null){
			parseCompleteListener.pluginsParsed();
		}
	}

	public void watchDirectoryPath(Path path) {
		// Sanity check - Check if path is a folder
		try {
			Boolean isFolder = (Boolean) Files.getAttribute(path,
					"basic:isDirectory", NOFOLLOW_LINKS);
			if (!isFolder) {
				throw new IllegalArgumentException("Path: " + path
						+ " is not a folder");
			}
		} catch (IOException ioe) {
			// Folder does not exists
			ioe.printStackTrace();
		}

		System.out.println("Watching path: " + path);

		// We obtain the file system of the Path
		FileSystem fs = path.getFileSystem();

		// We create the new WatchService using the new try() block
		try (WatchService service = fs.newWatchService()) {

			// We register the path to the service
			// We watch for creation events
			path.register(service, ENTRY_CREATE);

			// Start the infinite polling loop
			WatchKey key = null;
			while (true) {
				key = service.take();

				// Dequeueing events
				Kind<?> kind = null;
				for (WatchEvent<?> watchEvent : key.pollEvents()) {
					// Get the type of the event
					kind = watchEvent.kind();
					if (OVERFLOW == kind) {
						continue; // loop
					} else if (ENTRY_CREATE == kind) {
						// A new Path was created
						Path newPath = ((WatchEvent<Path>) watchEvent)
								.context();
						// Output
						if (newPath.toString().contains(extensionToUse)) {
							System.out.println("New path created: " + newPath);
							//Read in config and notify listeners
							this.readPluginConfig(newPath);
						}
					}
				}

				if (!key.reset()) {
					break; // loop
				}
			}

		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}

	}

	public void readPluginConfig(Path newPath) {
		String filesystem = newPath.toAbsolutePath().getParent().toString();
		String filename = newPath.getFileName().toString();
		String path = "";
		// idk if right, but we need it right now
		if (filesystem.endsWith("plugins")) {
			path = filesystem + File.separator + filename;
		} else {
			path = filesystem + File.separator + "plugins" + File.separator
					+ filename;
		}

		JarFile pluginJar;
		try {
			pluginJar = new JarFile(path);
			Enumeration<JarEntry> entries = pluginJar.entries();

			String className = null;
			JarEntry entry = null;
			while (entries.hasMoreElements()) {
				entry = entries.nextElement();

				String name = entry.getName();
				if (name.equals(CONFIG_FILE_NAME)) {
					try {
						InputStream inStream = pluginJar.getInputStream(entry);
						//Read in xml and register plugin.
						XStream streamer = new XStream();
						
						streamer.alias("plugin", PluginData.class);
						streamer.alias("servlets", List.class);
						streamer.alias("servlet", ServletData.class);
						streamer.alias("expectedMethods", List.class);
						
						Object result = streamer.fromXML(inStream);
						
						PluginData plugin = (PluginData) result;
						plugin.setJarPath(path.toString());
						
						this.plugins.add(plugin);
						
						//Notify all of the registered listeners about the plugins
						for(int i = 0; i < addedListeners.size(); i++){
							addedListeners.get(i).addPlugin(plugin);
						}
						
						inStream.close();
					} catch (IOException e) {
						throw new IllegalArgumentException(
								"Failed to read the config file.",
								e);
					}
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void registerAddedListener(IPluginAddedListener listener) {
		this.addedListeners.add(listener);
	}

	public void unregisterAddedListener(IPluginAddedListener listener) {
		this.addedListeners.remove(listener);
	}

	public void registerRemovedListener(IPluginRemovedListener listener) {
		this.removedListeners.add(listener);
	}

	public void unregisterRemovedListener(IPluginRemovedListener listener) {
		this.removedListeners.remove(listener);
	}
	
	public void run(){
		//Folder to check for plugins
		Path folder = Paths.get("plugins");
		
		this.findExistingFiles(folder);
		this.watchDirectoryPath(folder);
	}
}
