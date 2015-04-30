package interfaces;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import protocol.HttpStatusCode;
import protocol.Protocol;
import server.GMTConversion;

public abstract class HttpResponseBase implements IHttpResponse {

	private boolean hasPopulatedServerHeaders = false;

	protected String version;
	protected Map<String, String> headers = new HashMap<String, String>();
	protected HttpStatusCode status;

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public HttpStatusCode getStatus() {
		return status;
	}

	public void setStatus(HttpStatusCode status) {
		this.status = status;
	}

	@Override
	public String getStatusPhrase() {
		return status.getStatusMessage();
	}

	@Override
	public int getStatusCode() {
		return status.getStatusCode();
	}

	@Override
	public String getHeader(String key) {
		return headers.get(key);
	}

	@Override
	public void putHeader(String key, String value) {
		headers.put(key, value);
	}

	@Override
	public void populateServerDefaultHeaders() {
		hasPopulatedServerHeaders = true;

		// Lets add current date
		Date date = Calendar.getInstance().getTime();
		putHeader(Protocol.DATE, GMTConversion.toGMTString(date));
		// Lets add server info
		putHeader(Protocol.Server, Protocol.getServerInfo());
		// Lets add extra header with provider info
		putHeader(Protocol.PROVIDER, Protocol.AUTHOR);
	}

	/**
	 * Cannot be overwritten by Servlet's implementation. Enforces strict
	 * ordering on writing out the data into the stream.
	 */
	@Override
	public final void write(OutputStream outStream) throws IOException {
		BufferedOutputStream out = new BufferedOutputStream(outStream,
				Protocol.CHUNK_LENGTH);

		if (!hasPopulatedServerHeaders) {
			populateServerDefaultHeaders();
		}

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
	abstract protected void writeContent(BufferedOutputStream outStream)
			throws IOException;

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("----------------------------------\n");

		ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
		try {
			this.write(bytesOut);
			buffer.append(bytesOut.toString());
		} catch (IOException e) {
			// #YOLO
		}

		buffer.append("\n----------------------------------\n");
		return buffer.toString();
	}
}
