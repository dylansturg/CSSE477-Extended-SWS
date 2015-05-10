package strategy;

import interfaces.RequestTaskBase;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CancellableThreadPoolExecutor extends ThreadPoolExecutor {

	private RequestTaskWatchdog watchdog;

	public CancellableThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, RequestTaskWatchdog watchdog) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
		this.watchdog = watchdog;
	}

	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		if (r instanceof FutureRequestTask) {
			@SuppressWarnings("unchecked")
			FutureRequestTask<RequestTaskBase, Void> futureTask = (FutureRequestTask<RequestTaskBase, Void>) r;
			if (watchdog != null) {
				watchdog.watchTask(futureTask);
			}
		}
	}

	@Override
	protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
		// TODO Auto-generated method stub
		if (runnable instanceof RequestTaskBase) {
			return newTaskFor((RequestTaskBase) runnable, value);

		}
		return super.newTaskFor(runnable, value);
	}

	private <T> RunnableFuture<T> newTaskFor(RequestTaskBase runnable, T value) {
		return new FutureRequestTask<RequestTaskBase, T>(runnable, value);
	}

	@SuppressWarnings("unchecked")
	public FutureRequestTask<RequestTaskBase, Void> submit(RequestTaskBase task) {
		return (FutureRequestTask<RequestTaskBase, Void>) super.submit(task);
	}

}
