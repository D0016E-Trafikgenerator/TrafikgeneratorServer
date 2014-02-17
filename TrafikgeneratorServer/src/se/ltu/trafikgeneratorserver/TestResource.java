package se.ltu.trafikgeneratorserver;

import java.net.InetAddress;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;

class TestResource extends ResourceBase  {
	Long TTL = null;
	InetAddress testclient;
	public TestResource(String name, String IP, Long TTL) {
		super(name);
	}
	public void handlePOST(CoapExchange exchange) {
		//TODO: check if test data comes from the expected IP
		//TODO: kill the server if time-to-live has passed
		if (exchange.advanced().getCurrentRequest().isConfirmable())
			exchange.respond(ResponseCode.CONTINUE);
	}
}