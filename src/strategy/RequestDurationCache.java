package strategy;

import interfaces.IHttpRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RequestDurationCache {

	/**
	 * Stores necessary information about addressing a IHttpRequest in the
	 * cache.
	 * 
	 * Requests are considered unique based on their method and path. Body
	 * content, query strings, and originator are not considered in this
	 * comparison. They are important, but ignored here.
	 * 
	 * @author dylans
	 *
	 */
	protected class RequestKey {
		public String method;
		public String path;

		/**
		 * Pulls the necessary data from the request.
		 * 
		 * Convenience constructor.
		 * 
		 * @param request
		 */
		public RequestKey(IHttpRequest request) {
			if (request != null) {
				method = (request.getMethod() != null ? request.getMethod()
						: "").toLowerCase();
				path = (request.getPath() != null ? request.getPath() : "")
						.toLowerCase();
			}
		}

		@Override
		public int hashCode() {
			// Java's string hashCode is probably alright
			return (method + path).hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof RequestKey)) {
				return false;
			}

			RequestKey other = (RequestKey) obj;
			// Delegate to the more specific check
			return equals(other);
		}

		/**
		 * Checks if this RequestKey matches another RequestKey by ensuring the
		 * method and path are the same. The method and path are considered case
		 * insensitively. This is debatable for the path component, but allows
		 * the "be liberal in what you accept..." guideline, and we're really
		 * just making a best guess anyway.
		 * 
		 * @param other
		 * @return
		 */
		public boolean equals(RequestKey other) {
			return stringsMatchCaseInsensitivelys(other.method, this.method)
					&& stringsMatchCaseInsensitivelys(other.path, this.path);
		}

		private boolean stringsMatchCaseInsensitivelys(String s1, String s2) {
			if (s1 == null && s1 == null) {
				return true;
			}

			if (s1 == null || s2 == null) {
				return false;
			}

			return s1.equalsIgnoreCase(s2);
		}
	}

	protected class RequestStatistics {
		public int successCount;
		public int failureCount;
		public long executionTimeMilisSum;

		public RequestStatistics() {
			successCount = 0;
			failureCount = 0;
			executionTimeMilisSum = 0;
		}

		public void appendStats(boolean success, double executionTime) {
			if (success) {
				successCount++;
			} else {
				failureCount++;
			}

			executionTimeMilisSum += executionTime;
		}

		public double estimateExecutionTime() {
			return executionTimeMilisSum / (successCount + failureCount);
		}

		public double currentSuccessRate() {
			return (double) successCount
					/ (double) (successCount + failureCount);
		}
	}

	private Map<RequestKey, RequestStatistics> cache;

	public RequestDurationCache() {
		cache = Collections
				.synchronizedMap(new HashMap<RequestKey, RequestStatistics>());
	}

	public RequestDurationCache(
			Map<RequestKey, RequestStatistics> underlyingCache) {
		assert underlyingCache != null;
		cache = underlyingCache;
	}

	/**
	 * Examines the current request cache and, if the request has been cached at
	 * least once, will delegate calculating an estimate onto the
	 * RequestStatistics. This will perform an average execution time
	 * calculation using the sum of all (cached) previously recorded request
	 * durations averaged per number of requests recorded.
	 * 
	 * Considers requests unique on the request method and path. Comparisons are
	 * performed case insensitively. This is a best guess. This value is no way
	 * a guarantee or possibly not even a remotely accurate representation of
	 * necessary execution time.
	 * 
	 * @param request
	 * @return estimated number of milis needed to execute the request, or -1 if
	 *         no estimate is available
	 */
	public double estimateExecutionTimeForRequest(IHttpRequest request) {
		assert request != null;

		RequestKey key = new RequestKey(request);
		if (cache.containsKey(key)) {
			return cache.get(key).estimateExecutionTime();
		}

		return -1;
	}

	/**
	 * Logs the request's statistics into the underlying cache, as appropriate.
	 * 
	 * @param executionTime
	 * @param successful
	 * @param request
	 */
	public void requestCompleted(double executionTime, boolean successful,
			IHttpRequest request) {
		assert request != null;
		assert executionTime > 0;

		if (executionTime < 0) {
			throw new IllegalArgumentException(
					"requestCompleted requires positive value for executionTime");
		}

		RequestKey key = new RequestKey(request);
		if (!cache.containsKey(key)) {
			cache.put(key, new RequestStatistics());
		}

		cache.get(key).appendStats(successful, executionTime);
	}

}
