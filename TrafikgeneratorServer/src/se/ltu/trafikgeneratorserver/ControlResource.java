package se.ltu.trafikgeneratorserver;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.network.config.NetworkConfig;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;

public class ControlResource extends ResourceBase  {
	private TrafikgeneratorServer server;
	public ControlResource(String name, TrafikgeneratorServer server) {
		super(name);
		this.server = server;
	}
	public void handlePOST(CoapExchange exchange) {
		exchange.accept();
		String[] options = exchange.getRequestText().split(";");
		if (options.length == 2) {
			String[] suboptions = options[0].split(",");
			Long clientTimeBeforeTest = null;
			for (int i = 0; i < suboptions.length; i++)
				if (suboptions[i].split("=").length == 2 && suboptions[i].split("=")[0].equals("NTP_OFFSET"))
					clientTimeBeforeTest = Long.valueOf(suboptions[i].split("=")[1]);
			//Test protocol 1.2b.4
			String token = exchange.advanced().getRequest().getTokenString();
			File appRoot = new File(System.getProperty("user.home"));
			File subDir = new File(appRoot, "logs");
			File file = new File(subDir, token + "-rcvr.pcap");
			file.getParentFile().mkdirs();
			try {
				if (!file.exists() && file.createNewFile()) {
					//TODO: remove " && file.createNewFile()" in the line above; it's for the test below -- pcap logging creates a file
					//TODO: insert code to start pcap logging 
					if (file.exists()) {
						NetworkConfig testConfig = TrafficConfig.stringListToNetworkConfig(options[1]);
						TrafikgeneratorServer testserver = new TrafikgeneratorServer(testConfig);
						testserver.setExecutor(Executors.newScheduledThreadPool(4));
						TestResource test = new TestResource("test", null, null);
						testserver.clientTimeBeforeTest = clientTimeBeforeTest;
						testserver.token = token;
						testserver.add(test);
						testserver.start();
						server.subservers.add(testserver);
						exchange.respond(ResponseCode.CREATED);
					}
				}
			} catch (IOException e) {
				exchange.respond(ResponseCode.INTERNAL_SERVER_ERROR);
			}
		}
		else
			exchange.respond(ResponseCode.BAD_OPTION);
	}
	public void handleDELETE(CoapExchange exchange) {
		exchange.accept();
		String query = exchange.getRequestOptions().getURIQueryString();
		if (query.split("&").length == 1 && query.split(",").length == 1 && query.split("=")[0].equals("token")) {
			String payload = exchange.getRequestText();
			Long clientTimeAfterTest = (long) 0;
			if (payload.split("=").length == 2 && payload.split("=")[0].equals("NTP_OFFSET"))
				clientTimeAfterTest = Long.valueOf(payload.split("=")[1]);
			String token = query.split("=")[1];
			for (TrafikgeneratorServer server : this.server.subservers) {
				if (server.token.equals(token)) {
					//Test protocol 1.2b.7
					//TODO: code to end pcap logging
					server.clientTimeAfterTest = clientTimeAfterTest;
					server.stop();
					exchange.respond(ResponseCode.DELETED);
				}
			}
		}
		else
			exchange.respond(ResponseCode.BAD_REQUEST);
	}
}