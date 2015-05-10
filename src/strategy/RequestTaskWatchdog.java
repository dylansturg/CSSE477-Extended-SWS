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

	public static final int MAX_EXECUTION_SECONDS = 2;
	public static final int CHECK_DELAY_SECONDS = 1;

	private boolean stopped = false;
	private List<ITaskEndedObserver> murderObservers = new ArrayList<ITaskEndedObserver>();
	private Map<FutureRequestTask<RequestTaskBase, Void>, Long> monitoredTasks;

	public RequestTaskWatchdog() {
		monitoredTasks = Collections
				.synchronizedMap(new HashMap<FutureRequestTask<RequestTaskBase, Void>, Long>());
	}

	public void watchTask(FutureRequestTask<RequestTaskBase, Void> task) {
		if (!monitoredTasks.containsKey(task)) {
			monitoredTasks.put(task, System.currentTimeMillis());
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

				List<Entry<FutureRequestTask<RequestTaskBase, Void>, Long>> killedSet = new ArrayList<Map.Entry<FutureRequestTask<RequestTaskBase, Void>, Long>>();

				long now = System.currentTimeMillis();
				long killIfStartedBefore = now
						- TimeUnit.MILLISECONDS.convert(MAX_EXECUTION_SECONDS,
								TimeUnit.SECONDS);
				for (Entry<FutureRequestTask<RequestTaskBase, Void>, Long> entry : monitoredTasks
						.entrySet()) {
					long timestamp = entry.getValue();
					if (timestamp < killIfStartedBefore) {
						// Taking too long - needs to die
						boolean cancelled = entry.getKey().cancel(true);
						killedSet.add(entry);
					}
				}

				for (Entry<FutureRequestTask<RequestTaskBase, Void>, Long> entry : killedSet) {
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
