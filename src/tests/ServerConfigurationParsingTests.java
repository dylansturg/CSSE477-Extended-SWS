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

import com.thoughtworks.xstream.XStream;

import configuration.ServerConfiguration;
import configuration.ServerRoute;

public class ServerConfigurationParsingTests {

	public void createExampleXMLFile() {
		File testConfig = new File("conf/example.xml");
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
		createExampleXMLFile();

		File testConfig = new File("conf/example.xml");
		ServerConfiguration tester = new ServerConfiguration();
		tester.parseConfiguration(testConfig);
	}

}
