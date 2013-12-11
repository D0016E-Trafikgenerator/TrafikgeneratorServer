package com.example.trafikgeneratorserver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.Option;
import ch.ethz.inf.vs.californium.network.config.NetworkConfig;
import ch.ethz.inf.vs.californium.network.config.NetworkConfigDefaults;
import ch.ethz.inf.vs.californium.server.Server;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;

public class FileServer {
	public static void main(Map<String, Option> args) {
		NetworkConfig nwSettings = nwSetup(args);
		Server självaServern = new Server(nwSettings);
		självaServern.setExecutor(Executors.newScheduledThreadPool(4));
		Resurs självaResursen = new Resurs("backwards");
		RandomResource slumpen = new RandomResource("random");
		självaServern.add(självaResursen);
		självaServern.add(slumpen);
		självaServern.start();
	}
	
	public static void rxServer(Map<String, Option> args){
		NetworkConfig nwSettings = nwSetup(args);
		Server självaServern = new Server(nwSettings);
		självaServern.setExecutor(Executors.newScheduledThreadPool(4));
			
		//args to resource constructor is name of resource + senders IP
		
		DummyResource dummyResource = new DummyResource("dummydata","xxx.xxx.xxx.xxx");
		FileServerResource fileServerResource = new FileServerResource("fileserver", "xxx.xxx.xxx.xxx");
		självaServern.add(dummyResource);
		självaServern.add(fileServerResource);
		självaServern.start();
	}
	
	
	
	private static NetworkConfig nwSetup(Map<String, Option> customSettings){
		NetworkConfig nwSettings = new NetworkConfig();
		
		if(customSettings.containsKey("PORT")){
			nwSettings.setInt(NetworkConfigDefaults.DEFAULT_COAP_PORT, customSettings.get("PORT").getIntegerValue());
		}
		/*if(customSettings.containsKey("TRANSMISSION_TYPE")){
			nwSettings.setInt(NetworkConfigDefaults., customSettings.get("PORT").getIntegerValue());
		}*/
		if(customSettings.containsKey("ACK_TIMEOUT")){
			nwSettings.setInt(NetworkConfigDefaults.ACK_TIMEOUT, customSettings.get("ACK_TIMEOUT").getIntegerValue());
		}
		if(customSettings.containsKey("ACK_RANDOM_FACTOR")){
			float x = Float.parseFloat(customSettings.get("ACK_RANDOM_FACTOR").getStringValue());
			nwSettings.setFloat(NetworkConfigDefaults.ACK_RANDOM_FACTOR, x);
		}
		if(customSettings.containsKey("MAX_RETRANSMIT")){
			nwSettings.setInt(NetworkConfigDefaults.MAX_RETRANSMIT, customSettings.get("MAX_RETRANSMIT").getIntegerValue());
		}
		if(customSettings.containsKey("NSTART")){
			nwSettings.setInt(NetworkConfigDefaults.NSTART, customSettings.get("NSTART").getIntegerValue());
		}
		if(customSettings.containsKey("PROBING_RATE")){
			float x = Float.parseFloat(customSettings.get("PROBING_RATE").getStringValue());
			nwSettings.setFloat(NetworkConfigDefaults.PROBING_RATE, x);
		}
		return nwSettings;
	}

}
class DummyResource extends ResourceBase {
	public DummyResource(String name, String IP) {
		super(name);
		String senderIP = IP;
		// TODO Auto-generated constructor stub
	}
	public void handleGET(CoapExchange exchange) {
		//generera slumpdata med seed ?xxxxx(skickas från klient), som en byte[size](definierad som option#3),
		//skicka tillbaka denna data
		
		int size = exchange.getRequestOptions().asSortedList().get(3).getIntegerValue();
		String number = exchange.getRequestOptions().getURIQueryString();
		
		Long seed = Long.parseLong(number);
		Random rnd = new Random(seed);
		byte[] dummyData = new byte[size];
		rnd.nextBytes(dummyData);
		exchange.respond(ResponseCode.CONTENT, dummyData);		
	}
	
	public void handlePOST(CoapExchange exchange) {
		//ta emot slumpdata exchange.etcetera, jämför den med egenskapad slumpdata, jämför och skicka tillbaka bedömning
		
	}
	public void handleDELETE(CoapExchange exchange) {
		/*
		 * TODO if there is time
		 * low prio
		 */
	}
}
class FileServerResource extends ResourceBase {
	public FileServerResource(String name, String IP) {
		super(name);
		String senderIP = IP;
		// TODO Auto-generated constructor stub
	}
	public void handleGET(CoapExchange exchange) {
	}
	public void handlePOST(CoapExchange exchange) {
		
	}
	public void handleDELETE(CoapExchange exchange) {
		/*
		 * TODO if there is time
		 * low prio
		 */
	}
}

class ListeningResource extends ResourceBase {
	public ListeningResource(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	/*
	 * SCHEME: GET for coap://server/random?12345 where 12345 is a seed number
	 *         for a number generator. Returns random bytes.
	 */

	public void handleGET(CoapExchange exchange) {
		String number = exchange.getRequestOptions().getURIQueryString();
		System.out.println(exchange.getRequestOptions().asSortedList().get(3).getNumber());
		List<Option> optionList = exchange.getRequestOptions().asSortedList();
		Map<String, Option> startOptions = new HashMap<String, Option>(); 
		
		for(int x = 3;x < optionList.size();x++){
			switch(optionList.get(x).getNumber()){
			case 123:	startOptions.put("TEST", optionList.get(x));
						break;
			case 65000: startOptions.put("PORT", optionList.get(x));
						break;
			//case 65001: startOptions.put("TRANSMISSION_TYPE", optionList.get(x));
			//			break;
			case 65002: startOptions.put("ACK_TIMEOUT", optionList.get(x));
						break;
			case 65003: startOptions.put("ACK_RANDOM_FACTOR", optionList.get(x));
						break;
			case 65004: startOptions.put("MAX_RETRANSMIT", optionList.get(x));
						break;
			case 65005: startOptions.put("NSTART", optionList.get(x));
						break;
			case 65006: startOptions.put("PROBING_RATE", optionList.get(x));
						break;
			}
		}
			
		Long seed = Long.parseLong(number);
		Random rnd = new Random(seed);
		byte[] dummyData = new byte[150];
		rnd.nextBytes(dummyData);
		//System.out.println(new String(dummyData, Charset.forName("ISO-8859-1")));
		exchange.respond(ResponseCode.CONTENT, dummyData);
	}
}