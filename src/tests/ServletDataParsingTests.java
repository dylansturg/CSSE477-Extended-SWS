package tests;

import interfaces.IHttpRequest;
import interfaces.IRequestTask;
import interfaces.IResourceRoute;
import interfaces.IResourceStrategy;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.thoughtworks.xstream.XStream;

import configuration.PluginData;
import configuration.ServletData;

public class ServletDataParsingTests {
	private static final String TEST_FILE_PATH = "conf/servlet_test.xml";

	
	public static void setupServletXMLFile() throws Exception {
		File servletConfig = new File(TEST_FILE_PATH);

		ServletData serv1 = new ServletData(TestServlet.class.getName(),
				"/serv1/", Arrays.asList(new String[] { "GET", "POST", "PUT" }));
		ServletData serv2 = new ServletData(TestServlet.class.getName(),
				"/more/fancy/path/serv2/", Arrays.asList(new String[] { "GET",
						"POST", "HEAD", "DELETE" }));
		ServletData serv3 = new ServletData(TestServlet.class.getName(),
				"/serv3/", Arrays.asList(new String[] { "GET" }));

		PluginData plugin = new PluginData("fancyplugin", "",
				Arrays.asList(new ServletData[] { serv1, serv2, serv3 }));

		XStream streamer = new XStream();

		servletConfig.createNewFile();
		FileOutputStream out = new FileOutputStream(servletConfig);
		streamer.toXML(plugin, out);
		out.close();

	}

	@Test
	public void test() {
		File config = new File(TEST_FILE_PATH);
		XStream streamer = new XStream();
		
		streamer.alias("plugin", PluginData.class);
		streamer.alias("servlets", List.class);
		streamer.alias("servlet", ServletData.class);
		streamer.alias("expectedMethods", List.class);
		
		Object result = streamer.fromXML(config);
		
		System.out.println(result);
		
	}

	public class TestServlet implements IResourceStrategy {

		@Override
		public IRequestTask prepareEvaluation(IHttpRequest request,
				IResourceRoute fromRoute) {
			// TODO Auto-generated method stub
			return null;
		}

	}

}
