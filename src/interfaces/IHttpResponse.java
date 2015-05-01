package interfaces;

import java.io.IOException;
import java.io.OutputStream;

import protocol.HttpStatusCode;

public interface IHttpResponse {

	public String getVersion();

	public String getHeader(String key);

	public void putHeader(String key, String value);

	public void write(OutputStream outStream) throws IOException;

	public String getStatusPhrase();

	public int getStatusCode();

	public void setStatus(HttpStatusCode status);

	public HttpStatusCode getStatus();

	public void populateServerDefaultHeaders();
}
