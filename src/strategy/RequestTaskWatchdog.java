package strategy;

import interfaces.RequestTaskBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public class RequestTaskWatchdog implements Runnable {

	private class Data {
		long startTime;
		Thread runThread;

		public Data(long time, Thread t) {
			startTime = time;
			runThread = t;
		}
	}

	public static final int MAX_EXECUTION_SECONDS = 2;
	public static final int CHECK_DELAY_SECONDS = 1;

	private boolean stopped = false;
	private List<ITaskEndedObserver> murderObservers = new ArrayList<ITaskEndedObserver>();
	private Map<FutureRequestTask<RequestTaskBase, Void>, Data> monitoredTasks;

	public RequestTaskWatchdog() {
		monitoredTasks = Collections
				.synchronizedMap(new HashMap<FutureRequestTask<RequestTaskBase, Void>, Data>());
	}

	public void watchTask(FutureRequestTask<RequestTaskBase, Void> task,
			Thread runThread) {
		if (!monitoredTasks.containsKey(task)) {
			monitoredTasks.put(task, new Data(System.currentTimeMillis(),
					runThread));
		}
	}

	public void markTaskComplete(FutureRequestTask<RequestTaskBase, Void> task) {
		monitoredTasks.remove(task);
	}

	public void registerObserver(ITaskEndedObserver observer) {
		murderObservers.add(observer);
	}

	@Override
	public void run() {
		while (!stopped) {
			try {
				Thread.sleep(TimeUnit.MILLISECONDS.convert(CHECK_DELAY_SECONDS,
						TimeUnit.SECONDS));
			} catch (InterruptedException e) {
				stopped = true;
				continue;
			}

			synchronized (monitoredTasks) {

				List<Entry<FutureRequestTask<RequestTaskBase, Void>, Data>> killedSet = new ArrayList<Map.Entry<FutureRequestTask<RequestTaskBase, Void>, Data>>();

				long now = System.currentTimeMillis();
				long killIfStartedBefore = now
						- TimeUnit.MILLISECONDS.convert(MAX_EXECUTION_SECONDS,
								TimeUnit.SECONDS);
				for (Entry<FutureRequestTask<RequestTaskBase, Void>, Data> entry : monitoredTasks
						.entrySet()) {
					long timestamp = entry.getValue().startTime;
					if (timestamp < killIfStartedBefore) {
						// Taking too long - needs to die
						boolean cancelled = entry.getKey().cancel(true);
						entry.getValue().runThread.stop();
						killedSet.add(entry);
					}
				}

				for (Entry<FutureRequestTask<RequestTaskBase, Void>, Data> entry : killedSet) {
					monitoredTasks.remove(entry.getKey());
					alertObserversOfMurder(entry.getKey());
				}
			}
		}
	}

	private void alertObserversOfMurder(
			FutureRequestTask<RequestTaskBase, Void> task) {
		for (ITaskEndedObserver iTaskEndedObserver : murderObservers) {
			iTaskEndedObserver.endedTask(task);
		}
	}

	public void stop() {
		stopped = true;
	}

}
