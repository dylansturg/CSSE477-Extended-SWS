package strategy;

import interfaces.RequestTaskBase;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CancellableThreadPoolExecutor extends ThreadPoolExecutor {

	public CancellableThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
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

}
