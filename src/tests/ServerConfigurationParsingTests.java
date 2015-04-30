package tests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import request.HTTPRequest;
import strategy.IRequestTask;
import strategy.IResourceStrategy;

import com.thoughtworks.xstream.XStream;

import configuration.InvalidConfigurationException;
import configuration.PluginData;
import configuration.ResourceStrategyConfiguration;
import configuration.ResourceStrategyRoute;
import configuration.ServerConfiguration;
import configuration.ServerRoute;
import configuration.ServletData;

public class ServerConfigurationParsingTests {

	public void createExampleXMLFile(String path) {
		File testConfig = new File(path);
		XStream streamer = new XStream();

		HashMap<String, String> options = new HashMap<String, String>();
		options.put("MaxThreads", "3");
		options.put("RootDirectory", "web/");

		List<ServerRoute> configs = new ArrayList<ServerRoute>();
		configs.add(new ServerRoute("myplugin", "/path/to/myplugin/", options));
		configs.add(new ServerRoute("otherplugin", "/otherplugin/", options));

		try {
			testConfig.createNewFile();
			FileOutputStream out = new FileOutputStream(testConfig);
			streamer.toXML(configs, out);
		} catch (Exception exp) {
			fail("Creating example xml file failed - is your file system working?");
		}

	}

	@Test
	public void testServerParsesXml() {
		String path = "conf/example.xml";
		createExampleXMLFile(path);

		File testConfig = new File(path);
		ServerConfiguration tester = new ServerConfiguration(
				new TestRouteConfig());

		List<ServletData> myServlets = new ArrayList<ServletData>();
		myServlets.add(new ServletData(TestServlet.class.getName(),
				"fancyservlet"));
		myServlets.add(new ServletData(TestServlet.class.getName(),
				"morefancyservlet"));

		PluginData myPlugin = new PluginData("myplugin", null, myServlets);
		tester.addPlugin(myPlugin);

		List<ServletData> otherServlets = new ArrayList<ServletData>();
		otherServlets.add(new ServletData(TestServlet.class.getName(),
				"fancyservlet"));

		PluginData otherPlugin = new PluginData("otherplugin", null,
				otherServlets);
		tester.addPlugin(otherPlugin);

		try {
			tester.parseConfiguration(testConfig);
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
			fail();
		}

		ResourceStrategyRoute testRoute = tester
				.getManagedResourceConfiguration().findRouteForResourcePath(
						"/path/to/myplugin/fancyservlet/");

		assertNotNull(testRoute);
		assertEquals(testRoute.getStrategyClass(), TestServlet.class);

		testRoute = tester.getManagedResourceConfiguration()
				.findRouteForResourcePath("/otherplugin/fancyservlet/");
		assertNotNull(testRoute);
		assertEquals(testRoute.getStrategyClass(), TestServlet.class);
	}

	public class TestRouteConfig extends ResourceStrategyConfiguration {
		public List<ResourceStrategyRoute> getRoutes() {
			return this.activeRoutes;
		}
	}

	public class TestServlet implements IResourceStrategy {

		@Override
		public IRequestTask prepareEvaluation(HTTPRequest request,
				ResourceStrategyRoute fromRoute) {
			return null;
		}

	}
}
