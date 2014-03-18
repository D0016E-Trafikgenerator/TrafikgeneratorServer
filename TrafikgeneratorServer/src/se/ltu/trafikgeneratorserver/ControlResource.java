package se.ltu.trafikgeneratorserver;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.network.config.NetworkConfig;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;

public class ControlResource extends ResourceBase  {
	private Process proc;
	private TrafikgeneratorServer server;
	private PacketDumper pcapLog;
	public ControlResource(String name, TrafikgeneratorServer server) {
		super(name);
		this.server = server;
	}
	public void handlePOST(CoapExchange exchange) {
		exchange.accept();
		String options = exchange.getRequestText();
		
		//Long clientTimeBeforeTest = null;
		String query = exchange.getRequestOptions().getURIQueryString();
		String time = query.split("=")[1];
		//Test protocol 1.3a.4
		String token = exchange.advanced().getRequest().getTokenString();
		System.out.println("rq skulle till " + exchange.advanced().getRequest().getDestination());
		File root = new File(System.getProperty("user.home"));
		File appRoot = new File(root, "trafikgeneratorcoap");
		File subDir = new File(appRoot, "logs");
		File file = new File(subDir, time + "-" + token + "-rcvr.pcap");
		file.getParentFile().mkdirs();
		try {
			if (!file.exists()) {
				
				
				pcapLog = new PacketDumper(file, 56830);
				new Thread(pcapLog).start();
				if (file.exists()) {
					NetworkConfig testConfig = TrafficConfig.stringListToNetworkConfig(options);
					TrafikgeneratorServer testserver = new TrafikgeneratorServer(testConfig);
					testserver.setExecutor(Executors.newScheduledThreadPool(4));
					TestResource test = new TestResource("test", null, null);
					//testserver.clientTimeBeforeTest = clientTimeBeforeTest;
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
					//Logger.exit();
					pcapLog.stop();
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
