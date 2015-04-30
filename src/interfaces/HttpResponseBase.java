package interfaces;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import protocol.HttpStatusCode;
import protocol.Protocol;

public abstract class HttpResponseBase implements IHttpResponse {

	protected String version;
	protected Map<String, String> headers = new HashMap<String, String>();
	protected HttpStatusCode status;

	@Override
	public String getHeader(String key) {
		return headers.get(key);
	}

	@Override
	public void putHeader(String key, String value) {
		headers.put(key, value);
	}

	/**
	 * Cannot be overwritten by Servlet's implementation. Enforces strict
	 * ordering on writing out the data into the stream.
	 */
	@Override
	public final void write(OutputStream outStream) throws IOException {
		BufferedOutputStream out = new BufferedOutputStream(outStream,
				Protocol.CHUNK_LENGTH);

		writeStatusLine(out);
		writeHeaders(out);
		writeContent(out);
		// Flush the data so that outStream sends everything through the socket
		out.flush();
	}

	protected void writeStatusLine(BufferedOutputStream outStream)
			throws IOException {
		String line = this.version + Protocol.SPACE
				+ this.status.getStatusCode() + Protocol.SPACE
				+ this.status.getStatusMessage() + Protocol.CRLF;
		outStream.write(line.getBytes());
	}

	protected void writeHeaders(BufferedOutputStream outStream)
			throws IOException {
		String line;
		// Write header fields if there is something to write in header field
		if (headers != null && !headers.isEmpty()) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();

				// Write each header field line
				line = key + Protocol.SEPERATOR + Protocol.SPACE + value
						+ Protocol.CRLF;
				outStream.write(line.getBytes());
			}
		}

		// Write a blank line
		outStream.write(Protocol.CRLF.getBytes());
	}

	/*
	 * Perform any logic specific to writing out the body of the response here.
	 * Intended to be implemented in subclasses (created by Servlets).
	 */
	protected abstract void writeContent(BufferedOutputStream outStream)
			throws IOException;
}
