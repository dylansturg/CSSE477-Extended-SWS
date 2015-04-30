package tests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.junit.BeforeClass;
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
	private static final String TEST_FILE_PATH = "conf/test_routes.xml";

	@BeforeClass
	public static void createExampleXMLFile() {
		File testConfig = new File(TEST_FILE_PATH);
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
	public void testServerParseConfigWithoutPluginsInstalled()
			throws InvalidConfigurationException {
		// valid config exists there
		File testFile = new File(TEST_FILE_PATH);

		ServerConfiguration tester = new ServerConfiguration(
				new TestRouteConfig());
		tester.parseConfiguration(testFile);

		List<ResourceStrategyRoute> parsedRoutes = ((TestRouteConfig) tester
				.getManagedResourceConfiguration()).getRoutes();
		assertNotNull(parsedRoutes);
		assertEquals(parsedRoutes.size(), 0);
	}

	@Test(expected = InvalidConfigurationException.class)
	public void testServerRejectsInvalidFile()
			throws InvalidConfigurationException, IOException {
		File emptyTestFile = new File(TEST_FILE_PATH + ".empty");
		emptyTestFile.createNewFile();

		new ServerConfiguration(new ResourceStrategyConfiguration())
				.parseConfiguration(emptyTestFile);
		fail();
	}

	@Test(expected = InvalidConfigurationException.class)
	public void testServerRejectsNonFile() throws InvalidConfigurationException {
		File badTestConfig = new File(
				"/NOFREAKING/WAY/THIS/FILE/SHOULD/EVER/EXIST/ON/YOUR/SYSTEM/AND/IF/IT/FAILS/WTF/ARE/ARE/YOU/DOING/WITH/YOUR/COMPUTJER/?????/routes.xml");
		new ServerConfiguration(new ResourceStrategyConfiguration())
				.parseConfiguration(badTestConfig);

		fail();
	}

	@Test
	public void testServerParsesXmlAndResolvesRoutes() {

		File testConfig = new File(TEST_FILE_PATH);
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
