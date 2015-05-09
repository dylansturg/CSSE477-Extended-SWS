package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import interfaces.IHttpRequest;

import java.util.Map;

import org.junit.Test;

import strategy.RequestDurationCache;

public class RequestCacheTests {
	private static final double TEST_DELTA = 0.000001;

	@Test
	public void testRequestCacheExists() {
		RequestDurationCache test = new RequestDurationCache();
		assertNotNull(test);
	}

	@Test
	public void testRequestCacheHandlesUnCachedRequest() {
		RequestDurationCache test = new RequestDurationCache();

		double estimate = test
				.estimateExecutionTimeForRequest(new FakeHttpRequest("GET",
						"/places/that/arent/real"));
		assertEquals(-1, estimate, TEST_DELTA);
	}

	@Test
	public void testRequestCacheCrashesNullRequest() {
		RequestDurationCache test = new RequestDurationCache();
		try {
			test.estimateExecutionTimeForRequest(null);
			fail("Should have errored on estimateExecutionTimeForRequest with null IHttpRequest");
		} catch (NullPointerException exp) {
			// pass
		}

		try {
			test.requestCompleted(-1, true, new FakeHttpRequest("GET",
					"/nothing/"));
			fail("Should have errored on requestCompleted with negative executionTime");
		} catch (IllegalArgumentException exp) {
			// pass
		}

		try {
			test.requestCompleted(3.0, true, null);
			fail("Should have errored on requestCompleted with null IHttpRequest");
		} catch (NullPointerException exp) {
			// pass
		}
	}

	@Test
	public void testRequestCacheSimpleEstimate() {
		RequestDurationCache test = new RequestDurationCache();
		FakeHttpRequest request = new FakeHttpRequest("GET", "/path/");

		test.requestCompleted(3, true, request);

		double estimate = test.estimateExecutionTimeForRequest(request);
		assertEquals(3, estimate, TEST_DELTA);

		estimate = test.estimateExecutionTimeForRequest(new FakeHttpRequest(
				"GET", "/path/"));
		assertEquals(3, estimate, TEST_DELTA);

		estimate = test.estimateExecutionTimeForRequest(new FakeHttpRequest(
				"get", "/PATH/"));
		assertEquals(3, estimate, TEST_DELTA);
	}

	public class FakeHttpRequest implements IHttpRequest {

		private String method = "";
		private String path = "";

		public FakeHttpRequest(String method, String path) {

			this.method = method;
			this.path = path;
		}

		@Override
		public String getMethod() {
			return method;
		}

		@Override
		public String getPath() {
			return path;
		}

		@Override
		public String getHeader(String key) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException();
		}

		@Override
		public Map<String, String> getQueryStrings() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException();
		}

		@Override
		public String getQueryString(String key) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException();
		}

		@Override
		public String getContent() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException();
		}

		@Override
		public void readHeadersAndBody() throws Exception {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException();
		}

		@Override
		public void checkRequest() throws Exception {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException();
		}

	}

}
