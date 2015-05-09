/*
 * ResponseHandler.java
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

package response;

import interfaces.IRequestTask;
import interfaces.IRequestTask.IRequestTaskCompletionListener;
import interfaces.RequestTaskBase;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import server.Server;
import configuration.ServerConfiguration;

/**
 * Implements handling of IRequestTask instances to serve a collection of
 * clients. Does not enforce that the collection of clients has any relation
 * whatsoever, but the collection should have some relation. Likely, they should
 * have the same origin IP address.
 * 
 * @author Chandan R. Rupakheti (rupakhcr@clarkson.edu)
 */
public class ResponseHandler implements Runnable,
		IRequestTaskCompletionListener {
	private static final int DEFAULT_THREADS_ALLOCATED = 3;
	private static final int MAXIMUM_THREADS_ALLOCATED = 5;

	private static final int THREAD_KEEP_ALIVE = 10;

	private Server server;

	/**
	 * Can be used to get information about the current server's configuration.
	 * Eventually should contain information such as desired thread pool size.
	 * 
	 * Not used at the moment. Available for future extension.
	 */
	@SuppressWarnings("unused")
	private ServerConfiguration serverConfig;

	private List<Socket> clients;
	private Map<Socket, OutputStream> clientOutStreams;

	/**
	 * Map a client's socket to that client's specific Queue of IRequestTask
	 * operations.
	 */
	private Map<Socket, Queue<IRequestTask>> currentlyExecutingRequests;

	// Relies on Java's wait/notify, so the actual object class is unimportant.
	private Object taskCompletionMonitor = new Object();

	/**
	 * Handles details of scheduling Runnable tasks to be executed on an
	 * available Thread. Manages Thread pool size and Thread lifetime. Wakes up
	 * this Runnable when one is complete, so it can be written back to the
	 * appropriate client.
	 */
	private ThreadPoolExecutor activeTaskThreadPool;

	/**
	 * Represents all tasks that the handler still needs to evaluate. None have
	 * begun evaluation. Each needs to wait for a thread to run on.
	 * 
	 */
	private BlockingQueue<Runnable> tasksAwaitingExecution;

	private volatile boolean stopped = false;

	public ResponseHandler(ServerConfiguration configuration, Server server) {
		serverConfig = configuration;
		this.server = server;
		clients = new ArrayList<Socket>();
		clientOutStreams = new HashMap<Socket, OutputStream>();
		commonInit();
	}

	public ResponseHandler(ServerConfiguration configuration, Server server,
			Socket... clients) throws IOException {
		serverConfig = configuration;
		this.server = server;
		this.clients = new ArrayList<Socket>();
		clientOutStreams = new HashMap<Socket, OutputStream>();
		if (clients != null) {
			this.clients.addAll(Arrays.asList(clients));

			for (Socket socket : clients) {
				clientOutStreams.put(socket, socket.getOutputStream());
			}
		}
		commonInit();
	}

	private void commonInit() {
		tasksAwaitingExecution = new PriorityBlockingQueue<Runnable>();
		activeTaskThreadPool = new ThreadPoolExecutor(
				DEFAULT_THREADS_ALLOCATED, MAXIMUM_THREADS_ALLOCATED,
				THREAD_KEEP_ALIVE, TimeUnit.MILLISECONDS,
				tasksAwaitingExecution);

		this.currentlyExecutingRequests = Collections
				.synchronizedMap(new HashMap<Socket, Queue<IRequestTask>>());

		if (this.clients == null) {
			this.clients = new ArrayList<Socket>();
		}

	}

	public void addClientToServed(Socket client, OutputStream clientOut) {
		synchronized (clients) {
			if (!clients.contains(client)) {
				clients.add(client);
				clientOutStreams.put(client, clientOut);
			}
			// else we already were serving that client, so whatever
		}
	}

	/**
	 * Asks the ResponseHandler to take control/ownership of the Task. It will
	 * be scheduled, executed, and written to the given client at some time in
	 * the future, if at all possible.
	 * 
	 * @param task
	 * @param client
	 * @throws IllegalStateException
	 *             if the specified client is not already served by
	 *             ResponseHandler. Try calling addClientToServed first.
	 */
	public void enqueueRequestTaskForClient(RequestTaskBase task, Socket client) {
		synchronized (clients) {
			if (!clients.contains(client)) {
				// We aren't serving that client, and we don't want to.
				// Serving clients is hard and we're lazy
				throw new IllegalStateException(
						String.format(
								"enqueueRequestTaskForClient (%s) not served by ResponseHandler (%s)",
								client, this));
			}
		}

		synchronized (currentlyExecutingRequests) {
			Queue<IRequestTask> clientsQueue = currentlyExecutingRequests
					.get(client);
			if (clientsQueue == null) {
				clientsQueue = new LinkedList<IRequestTask>();
				currentlyExecutingRequests.put(client, clientsQueue);
			}

			clientsQueue.add(task);
			task.registerCompletionListener(this);
			task.setServer(server);

			// ThreadPoolExecutor will handle scheduling and running the task
			activeTaskThreadPool.execute(task);
		}
	}

	/**
	 * Blocks on taskCompletion monitor to be notified whenever a IRequestTask
	 * completes. Prevents busy waiting.
	 * 
	 * Whenever it gets to execute, it checks all served clients for any
	 * complete (and writeable) requests, serving all requests possible before
	 * waiting for another notification.
	 * 
	 */
	@Override
	public void run() {
		synchronized (taskCompletionMonitor) {
			while (!stopped) {
				try {
					taskCompletionMonitor.wait();

					for (Socket socket : clients) {
						Queue<IRequestTask> clientTaskQueue = currentlyExecutingRequests
								.get(socket);

						if (clientTaskQueue == null) {
							continue; // Probably shouldn't happen, but OSTRICH
										// MODE ENABLED
						}

						boolean finished = flushAllCompletedRequests(
								clientOutStreams.get(socket), clientTaskQueue);
						if (finished) {
							socket.close();
						}
					}

				} catch (InterruptedException | IOException e) {
					// TODO Log this error and learn what it means, someday
				}
			}
		}

		activeTaskThreadPool.shutdown();
	}

	/**
	 * Performs a blocking write operation. Always call from a background
	 * (blockable) thread.
	 * 
	 * Given a Socket that represents a HTTP client and a queue of IRequestTask
	 * associated with that client, writes the finished requests out to that
	 * client. Only writes completed requests. Stops writing as soon as it
	 * encounters an incomplete task in order to maintain ordering of
	 * requests/response.
	 * 
	 * @param client
	 * @param tasks
	 */
	private boolean flushAllCompletedRequests(OutputStream outStream,
			Queue<IRequestTask> tasks) {

		while (!tasks.isEmpty()) {
			IRequestTask currentTask = tasks.peek();
			if (!currentTask.isComplete()) {
				break; // we're done here
			}

			currentTask = tasks.remove();
			try {
				currentTask.writeResponse(outStream);
			} catch (IOException exp) {
				// TODO Log the exception, and close the socket
			}

			long startedTimeStamp = currentTask.getStartTime();
			long finishedTimeStamp = System.currentTimeMillis();
			server.incrementServiceTime(finishedTimeStamp - startedTimeStamp);
			server.getRequestDurationEstimator().requestCompleted(
					finishedTimeStamp - startedTimeStamp,
					currentTask.wasSuccessful(), currentTask.getRequest());

		}
		return tasks.isEmpty();
	}

	/**
	 * Runs in a ThreadPoolExecutor managed Thread instance.
	 * 
	 * Called by IRequestTask when it is complete, while still in its
	 * Runnable.Run.
	 * 
	 * @param completed
	 */
	@Override
	public void taskComplete(IRequestTask completed) {
		// TODO Consider finding a way to coalesce multiple incoming completion
		// requests together to only execute write operation once
		synchronized (taskCompletionMonitor) {
			taskCompletionMonitor.notifyAll();
		}
	}

}
