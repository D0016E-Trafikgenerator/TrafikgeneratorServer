package se.ltu.trafikgeneratorserver;

import se.ltu.trafikgeneratorcoap.testing.SendTest;
import se.ltu.trafikgeneratorcoap.testing.Settings;
import se.ltu.trafikgeneratorcoap.testing.TrafficConfig;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.Executors;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.network.config.NetworkConfig;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;

public class ControlResource extends ResourceBase  {
	private TrafikgeneratorServer server;
	private PacketDumper pcapLog;
	private TrafficConfig sendConfig = null;
	private String sendToken = null, sendTime = null;
	public ControlResource(String name, TrafikgeneratorServer server) {
		super(name);
		this.server = server;
	}
	public void handleGET(CoapExchange exchange) {
		if (exchange.getRequestPayload().length == 0 && sendConfig != null) {
			try {
				exchange.accept();
				SendTest.run(sendConfig);
				pcapLog.stop();
				String testURI = String.format(Locale.ROOT, "coap://%1$s:%2$d/test", sendConfig.getStringSetting(Settings.TEST_SERVER), sendConfig.getIntegerSetting(Settings.TEST_TESTPORT));
				Request.newDelete().setURI(testURI).send();
			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
			}
		}
		else {
			sendToken = exchange.advanced().getCurrentRequest().getTokenString();
			sendConfig = new TrafficConfig(exchange.getRequestText());
			sendConfig.setStringSetting(Settings.TEST_SERVER, exchange.getSourceAddress().getHostAddress());
			String query = exchange.getRequestOptions().getURIQueryString();
			sendTime = query.split("=")[1];
			File root = new File(System.getProperty("user.home"));
			File appRoot = new File(root, "trafikgeneratorcoap");
			File subDir = new File(appRoot, "logs");
			File file = new File(subDir, sendTime + "-" + sendToken + "-sndr.pcap");
			file.getParentFile().mkdirs();
			if (!file.exists()) {
				try {
					pcapLog = new PacketDumper(file, 56830);
					new Thread(pcapLog).start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			exchange.respond(ResponseCode.CONTINUE);
		}
	}
	public void handlePOST(CoapExchange exchange) {
		exchange.accept();
		String options = exchange.getRequestText();
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
