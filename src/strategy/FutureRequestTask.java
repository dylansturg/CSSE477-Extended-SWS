package strategy;

import interfaces.RequestTaskBase;

import java.util.Date;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class FutureRequestTask<V extends RequestTaskBase, T> extends
		FutureTask<T> implements Comparable<FutureRequestTask<V, T>> {

	private V requestTask;

	public FutureRequestTask(V runnable, T result) {
		super(runnable, result);
		requestTask = runnable;
	}

	public FutureRequestTask(V runnable) {
		super(runnable, null);
		requestTask = runnable;
	}

	private long timeDifference(Date first, Date second, TimeUnit unit) {
		long milis = second.getTime() - first.getTime();
		return unit.convert(milis, TimeUnit.MILLISECONDS);
	}

	private double getWaitingTime(Date now) {
		return (double) timeDifference(requestTask.getReceivedTimeStamp(), now,
				TimeUnit.MILLISECONDS);
	}

	private double getEstimatedRunTime() {
		if (requestTask.getRequest() == null) {
			return -1; // How'd that happen?
		}
		return requestTask.getServer() != null ? requestTask.getServer()
				.getRequestDurationEstimator()
				.estimateExecutionTimeForRequest(requestTask.getRequest()) : -1;
	}

	@Override
	public int compareTo(FutureRequestTask<V, T> o) {
		if (o == null) {
			throw new NullPointerException(
					"Attempt to compare to null IRequestTask");
		}

		/**
		 * Use the estimated request time as a benchmark. If the request has
		 * been waiting longer than its estimate time to execute, then order as
		 * FIFO. Otherwise, order as shortest duration to compute first.
		 */

		double myEstimate = getEstimatedRunTime();
		double otherEstimate = o.getEstimatedRunTime();

		Date now = new Date();
		double myWait = getWaitingTime(now);
		double otherWait = o.getWaitingTime(now);

		if (myEstimate < myWait || otherEstimate < otherWait) {
			// if a wait time ever exceeds the estimate for runtime, order as
			// FIFO
			return requestTask.getReceivedTimeStamp().compareTo(
					o.requestTask.getReceivedTimeStamp());
		}

		// Longify for most precision, then intify for conformance
		return (int) ((long) myEstimate - (long) otherEstimate);

	}

}
