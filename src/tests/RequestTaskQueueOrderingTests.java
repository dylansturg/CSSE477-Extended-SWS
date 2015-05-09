package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import interfaces.IHttpRequest;
import interfaces.RequestTaskBase;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;

import org.junit.Test;

import server.Server;
import strategy.RequestDurationCache;

public class RequestTaskQueueOrderingTests {

	@Test
	public void testUnestimatedSameStartTimeTasks() {
		Date timestamp = new Date();
		Server server = new FakeServer(new RequestDurationCache());

		RequestTaskBase task1 = createTestTask(null, timestamp, server);
		RequestTaskBase task2 = createTestTask(null, timestamp, server);

		int comp = task1.compareTo(task2);
		assertEquals(0, comp);
	}

	@Test
	public void testEstimatedSameStartTimeTasks() {
		Date timestamp = new Date();
		Server server = new FakeServer(new RequestDurationCache());

		IHttpRequest request = new RequestCacheTests.FakeHttpRequest("GET", "/");

		server.getRequestDurationEstimator().requestCompleted(2, true, request);

		RequestTaskBase task1 = createTestTask(
				new RequestCacheTests.FakeHttpRequest("GET", "/"), timestamp,
				server);
		RequestTaskBase task2 = createTestTask(
				new RequestCacheTests.FakeHttpRequest("GET", "/"), timestamp,
				server);

		int comp = task1.compareTo(task2);
		assertEquals(0, comp);
	}

	@Test
	public void testEstimatedDifferentStartTimeTasks() {
		Date timestamp = new Date();
		Server server = new FakeServer(new RequestDurationCache());

		IHttpRequest request = new RequestCacheTests.FakeHttpRequest("GET", "/");

		// That's 10 minutes
		server.getRequestDurationEstimator().requestCompleted(600000, true,
				request);

		RequestTaskBase task1 = createTestTask(
				new RequestCacheTests.FakeHttpRequest("GET", "/"), timestamp,
				server);
		RequestTaskBase task2 = createTestTask(
				new RequestCacheTests.FakeHttpRequest("GET", "/"), new Date(
						timestamp.getTime() - 1000), server);
		// Neither will have waited 10 mins, so it will still default to order
		// by wait time, which is the same
		int comp = task1.compareTo(task2);
		assertEquals(0, comp);
	}

	@Test
	public void testEstimatedDifferentlyWithDifferentStartTimeTasks() {
		Date timestamp = new Date();
		Server server = new FakeServer(new RequestDurationCache());

		IHttpRequest request = new RequestCacheTests.FakeHttpRequest("GET",
				"/path1");

		// That's 10 minutes
		server.getRequestDurationEstimator().requestCompleted(600000, true,
				request);
		server.getRequestDurationEstimator().requestCompleted(300000, true,
				new RequestCacheTests.FakeHttpRequest("GET", "/path2"));

		RequestTaskBase task1 = createTestTask(
				new RequestCacheTests.FakeHttpRequest("GET", "/path1"),
				timestamp, server);
		RequestTaskBase task2 = createTestTask(
				new RequestCacheTests.FakeHttpRequest("GET", "/path2"),
				new Date(timestamp.getTime() - 1000), server);
		// task2 should be sorted ahead of task1 (estimated faster with neither
		// waiting long enough)
		int comp = task1.compareTo(task2);
		assertTrue("task1 comp task2 positive, so task2 is ordered first",
				comp > 0);
	}

	@Test
	public void testFewEstimatedDifferentlyWithDifferentStartTimeTasks() {
		Date timestamp = new Date();
		Server server = new FakeServer(new RequestDurationCache());

		server.getRequestDurationEstimator().requestCompleted(600000, true,
				new RequestCacheTests.FakeHttpRequest("GET", "/path1"));
		server.getRequestDurationEstimator().requestCompleted(300000, true,
				new RequestCacheTests.FakeHttpRequest("GET", "/path2"));
		server.getRequestDurationEstimator().requestCompleted(150000, true,
				new RequestCacheTests.FakeHttpRequest("GET", "/path3"));

		RequestTaskBase task1 = createTestTask(
				new RequestCacheTests.FakeHttpRequest("GET", "/path1"),
				timestamp, server);
		RequestTaskBase task2 = createTestTask(
				new RequestCacheTests.FakeHttpRequest("GET", "/path2"),
				new Date(timestamp.getTime() - 1000), server);
		RequestTaskBase task3 = createTestTask(
				new RequestCacheTests.FakeHttpRequest("GET", "/path3"),
				new Date(timestamp.getTime() - 1000), server);
		RequestTaskBase task4 = createTestTask(
				new RequestCacheTests.FakeHttpRequest("GET", "/path4"),
				new Date(timestamp.getTime() - 500), server);

		RequestTaskBase[] tasks = new RequestTaskBase[] { task1, task2, task3,
				task4 };

		Arrays.sort(tasks);

		assertEquals(task3, tasks[0]);
		assertEquals(task2, tasks[1]);
		assertEquals(task4, tasks[2]);
		assertEquals(task1, tasks[3]);
	}

	@Test
	public void testEntirelySameStartWithDifferentEstimates() {
		Date timestamp = new Date();
		Server server = new FakeServer(new RequestDurationCache());

		server.getRequestDurationEstimator().requestCompleted(600000, true,
				new RequestCacheTests.FakeHttpRequest("GET", "/path1"));
		server.getRequestDurationEstimator().requestCompleted(300000, true,
				new RequestCacheTests.FakeHttpRequest("GET", "/path2"));
		server.getRequestDurationEstimator().requestCompleted(150000, true,
				new RequestCacheTests.FakeHttpRequest("GET", "/path3"));
		server.getRequestDurationEstimator().requestCompleted(10000, true,
				new RequestCacheTests.FakeHttpRequest("GET", "/path4"));

		RequestTaskBase task1 = createTestTask(
				new RequestCacheTests.FakeHttpRequest("GET", "/path1"),
				timestamp, server);
		RequestTaskBase task2 = createTestTask(
				new RequestCacheTests.FakeHttpRequest("GET", "/path2"),
				new Date(timestamp.getTime() - 1000), server);
		RequestTaskBase task3 = createTestTask(
				new RequestCacheTests.FakeHttpRequest("GET", "/path3"),
				new Date(timestamp.getTime() - 1000), server);
		RequestTaskBase task4 = createTestTask(
				new RequestCacheTests.FakeHttpRequest("GET", "/path4"),
				new Date(timestamp.getTime() - 500), server);

		RequestTaskBase[] tasks = new RequestTaskBase[] { task1, task2, task3,
				task4 };

		Arrays.sort(tasks);

		assertEquals(task4, tasks[0]);
		assertEquals(task3, tasks[1]);
		assertEquals(task2, tasks[2]);
		assertEquals(task1, tasks[3]);
	}

	@Test
	public void testEntirelyUnestimatedSetOfRequests() {
		Date timestamp = new Date();
		Server server = new FakeServer(new RequestDurationCache());

		RequestTaskBase task1 = createTestTask(
				new RequestCacheTests.FakeHttpRequest("GET", "/path1"),
				timestamp, server);
		RequestTaskBase task2 = createTestTask(
				new RequestCacheTests.FakeHttpRequest("GET", "/path2"),
				new Date(timestamp.getTime() - 1000), server);
		RequestTaskBase task3 = createTestTask(
				new RequestCacheTests.FakeHttpRequest("GET", "/path3"),
				new Date(timestamp.getTime() - 1000), server);
		RequestTaskBase task4 = createTestTask(
				new RequestCacheTests.FakeHttpRequest("GET", "/path4"),
				new Date(timestamp.getTime() - 500), server);

		RequestTaskBase[] tasks = new RequestTaskBase[] { task1, task2, task3,
				task4 };

		Arrays.sort(tasks);

		// FIFO ordering will tell us that task1 and task4 and the last ones in
		// the queue
		// but task2 and task3 could be ordered in any direction
		assertEquals(task4, tasks[2]);
		assertEquals(task1, tasks[3]);
	}

	private static FakeTask createTestTask(IHttpRequest request,
			Date timestamp, Server server) {
		if (timestamp == null) {
			timestamp = new Date();
		}
		if (request == null) {
			request = new RequestCacheTests.FakeHttpRequest("GET", "");
		}

		FakeTask result = new FakeTask(request, server);
		try {
			Field receivedField = RequestTaskBase.class
					.getDeclaredField("receivedTimeStamp");
			receivedField.setAccessible(true);

			receivedField.set(result, timestamp);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public static class FakeServer extends Server {
		private RequestDurationCache cache;

		public FakeServer(RequestDurationCache requestCache) {
			cache = requestCache;
		}

		@Override
		public RequestDurationCache getRequestDurationEstimator() {
			return cache;
		}

	}

	public static class FakeTask extends RequestTaskBase {

		public FakeTask(IHttpRequest request, Server server) {
			super(request);

			setServer(server);
		}

	}
}
