package response;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Map;

import protocol.HttpStatusCode;
import interfaces.HttpResponseBase;

public class DefaultHttpResponse extends HttpResponseBase {

	public DefaultHttpResponse(String version, HttpStatusCode status,
			Map<String, String> headers) {
		super(version, status, headers);
	}

	@Override
	protected void writeContent(BufferedOutputStream outStream)
			throws IOException {
		// Do nothing - we're returning empty
	}

}
