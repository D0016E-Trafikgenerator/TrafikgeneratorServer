package se.ltu.trafikgeneratorserver;

import java.util.ArrayList;
import java.util.concurrent.Executors;

import ch.ethz.inf.vs.californium.network.config.NetworkConfig;
import ch.ethz.inf.vs.californium.server.Server;

public class TrafikgeneratorServer extends Server {
	/* The main server application keeps a list of sub-servers; that is,
	 * temporary test servers that run on different ports for different clients.
	 * Each server is identified with the CoAP token with which it was started.
	 */
	ArrayList<TrafikgeneratorServer> subservers = new ArrayList<TrafikgeneratorServer>();
	String token = "";
	long clientTimeBeforeTest, clientTimeAfterTest;
	public TrafikgeneratorServer(NetworkConfig networkConfig) {
		super(networkConfig);
	}
	public static void main(String[] args) {
		/* A Californium resource is in essence which URI paths the server
		 * offers interactions on. We tell the server to keep a "control resource"
		 * on coap://server/control and a "file resource" on coap://server/file .
		 * We also start an NTP server, but this can be supplanted by any other
		 * standard NTP server.
		 */
		TrafikgeneratorServer server = new TrafikgeneratorServer(NetworkConfig.createStandardWithoutFile());
		server.setExecutor(Executors.newScheduledThreadPool(4));
		ControlResource control = new ControlResource("control", server);
		server.add(control);
		FileResource fileResource = new FileResource("file", server);
		server.add(fileResource);
		server.start();
		NTPServer.main(null);
	}
}
