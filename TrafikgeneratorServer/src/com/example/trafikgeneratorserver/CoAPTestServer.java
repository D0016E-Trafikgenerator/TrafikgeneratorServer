package com.example.trafikgeneratorserver;
import java.util.Random;
import java.util.concurrent.Executors;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.network.config.NetworkConfig;
import ch.ethz.inf.vs.californium.network.config.NetworkConfigDefaults;
import ch.ethz.inf.vs.californium.server.Server;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;

public class CoAPTestServer {
	public static void main(String[] args) {
		NetworkConfig inställningar = new NetworkConfig();
		inställningar.setInt(NetworkConfigDefaults.ACK_TIMEOUT, 2020);
		Server självaServern = new Server(inställningar);
		självaServern.setExecutor(Executors.newScheduledThreadPool(4));
		Resurs självaResursen = new Resurs("backwards");
		RandomResource slumpen = new RandomResource("random");
		självaServern.add(självaResursen);
		självaServern.add(slumpen);
		självaServern.start();
	}
}

class Resurs extends ResourceBase {
	private String textmeddelande;
	Resurs(String namn) {
		super(namn);
	}
	public void handlePOST(CoapExchange exchange) {
		textmeddelande = exchange.getRequestText();
		exchange.respond(ResponseCode.CHANGED);
	}
	public void handleGET(CoapExchange exchange) {
		exchange.respond(ResponseCode.CONTENT, new StringBuilder(textmeddelande).reverse().toString());
	}
}

class RandomResource extends ResourceBase {
	/*
	 * SCHEME: GET for coap://server/random?12345 where 12345 is a seed number
	 *         for a number generator. Returns random bytes.
	 */
	public RandomResource(String name) {
		super(name);
	}
	public void handleGET(CoapExchange exchange) {
		String number = exchange.getRequestOptions().getURIQueryString();
		System.out.println(exchange.getRequestOptions().asSortedList().get(3).getStringValue());
		Long seed = Long.parseLong(number);
		Random rnd = new Random(seed);
		byte[] dummyData = new byte[150];
		rnd.nextBytes(dummyData);
		//System.out.println(new String(dummyData, Charset.forName("ISO-8859-1")));
		exchange.respond(ResponseCode.CONTENT, dummyData);
	}
}