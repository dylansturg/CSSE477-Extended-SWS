package interfaces;

import java.io.IOException;
import java.io.OutputStream;

public interface IHttpResponse {
	
	public String getHeader(String key);
	public void putHeader(String key, String value);
	public void write(OutputStream outStream) throws IOException;
}
