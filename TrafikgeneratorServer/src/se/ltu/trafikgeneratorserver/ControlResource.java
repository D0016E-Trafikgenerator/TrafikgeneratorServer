package se.ltu.trafikgeneratorserver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;

import se.ltu.trafikgeneratorcoap.send.Sending;

import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.network.Endpoint;
import ch.ethz.inf.vs.californium.network.config.NetworkConfig;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;

public class ControlResource extends ResourceBase  {
	File root = new File(System.getProperty("user.home"));
	File appRoot = new File(root, "trafikgeneratorcoap");
	File subDir = new File(appRoot, "logs");
	private Process proc;
	private TrafikgeneratorServer server;
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
		
		
		
		File file = new File(subDir, time + "-" + token + "-rcvr.pcap");
		file.getParentFile().mkdirs();
		try {
			if (!file.exists() && file.createNewFile()) {
				//TODO: remove " && file.createNewFile()" in the line above; it's for the test below -- pcap logging creates a file
					
				Logger.startLog(file);
				
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
					Logger.exit();
					server.clientTimeAfterTest = clientTimeAfterTest;
					server.stop();
					exchange.respond(ResponseCode.DELETED);
				}
			}
		}
		else
			exchange.respond(ResponseCode.BAD_REQUEST);
	}
	public void handleGET(CoapExchange exchange) {
		TrafficConfig config;
		exchange.accept();
		String time = exchange.getRequestOptions().getURIQueryString().split("=")[1];
		String token  = exchange.advanced().getCurrentRequest().getTokenString();
		
		File configFile = new File(subDir, (time + "-" + token + "-config.txt"));
		try{
			FileOutputStream fos = new FileOutputStream(configFile);
			fos.write(exchange.getRequestPayload());
			fos.close();
		} catch (Exception e){
			;
		}
		
		//Test protocol 1.3a.4
		File file = new File(subDir, time + "-" + token + "-sndr.pcap");
		file.getParentFile().mkdirs();
		try {
			if (!file.exists() && file.createNewFile()) {
				//TODO: remove " && file.createNewFile()" in the line above; it's for the test below -- pcap logging creates a file
				config = new TrafficConfig(configFile.toString());
				config.toNetworkConfig();
				//TODO: config -> network config
				
				Logger.startLog(file);
				exchange.respond(ResponseCode.CONTINUE);
				Response response = exchange.advanced().getCurrentRequest().waitForResponse();
				if (!response.equals(null) && response.getCode().equals(ResponseCode.CREATED)) {
					//Sending.sendData(config, null);
					Request delete = Request.newDelete();
					delete.send(exchange.advanced().getEndpoint());
					response = delete.waitForResponse();
					if(!response.equals(null) && response.getCode().equals(ResponseCode.DELETED)){
						
					}
				}
				

			}
		} catch (Exception e) {
			exchange.respond(ResponseCode.INTERNAL_SERVER_ERROR);
		}
		
	}
}
