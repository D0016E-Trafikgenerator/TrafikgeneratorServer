package com.example.trafikgeneratorserver;

import java.util.List;
import java.util.Random;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.Option;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;

public class FileServer {

}
public void handleGET(CoapExchange exchange) {
	String number = exchange.getRequestOptions().getURIQueryString();
	System.out.println(exchange.getRequestOptions().asSortedList().get(3).getNumber());
	List<Option> optionList = exchange.getRequestOptions().asSortedList();
	
	
	for(int x = 3;x <= optionList.size();x++){
		System.out.println(optionList.get(x).getNumber());
	}
	
	Long seed = Long.parseLong(number);
	Random rnd = new Random(seed);
	byte[] dummyData = new byte[150];
	rnd.nextBytes(dummyData);
	//System.out.println(new String(dummyData, Charset.forName("ISO-8859-1")));
	exchange.respond(ResponseCode.CONTENT, dummyData);
}