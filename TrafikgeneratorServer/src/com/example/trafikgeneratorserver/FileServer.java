package com.example.trafikgeneratorserver;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.Arrays;
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
	//main-metod, Frans idé
	public static void main(String[] args)
	{
		NetworkConfig nwSettings = new NetworkConfig();		
		Server självaServern = new Server(nwSettings);
		självaServern.setExecutor(Executors.newScheduledThreadPool(4));
		//Resurs självaResursen = new Resurs("backwards");//Här och nedanför bör kanske istället lyssningsresursen skapas och läggas till 
		//RandomResource slumpen = new RandomResource("random");
		//självaServern.add(självaResursen);
		//självaServern.add(slumpen);
		ListeningResource listener = new ListeningResource("lyssnare");
		självaServern.add(listener);
		självaServern.start();
}
	/*//Den ursprungliga main-metoden, före Frans lade sina flottiga fingrar på den
	public static void main(Map<String, Option> args)//ska main ta våra customfunktioner verkligen?
													 //ska inte bara rxServern vi startar upp ta dem?
	{
		NetworkConfig nwSettings = nwSetup(args);
		Server självaServern = new Server(nwSettings);
		självaServern.setExecutor(Executors.newScheduledThreadPool(4));
		//Resurs självaResursen = new Resurs("backwards");//Här och nedanför bör kanske istället lyssningsresursen skapas och läggas till 
		//RandomResource slumpen = new RandomResource("random");
		//självaServern.add(självaResursen);
		//självaServern.add(slumpen);
		ListeningResource listener = new ListeningResource("lyssnare");
		självaServern.add(listener);
		självaServern.start();
	}
	*/
	public static void rxServer(Map<String, Option> args, InetAddress ip){
		NetworkConfig nwSettings = nwSetup(args);
		Server självaServern = new Server(nwSettings);
		självaServern.setExecutor(Executors.newScheduledThreadPool(4));
			
		//args to resource constructor is name of resource + senders IP
		//IP kan man få från själva CoAP-exchange, men den kan ju vara lite jobbig att få _här_
		//Kanske bättre att använda någon annanstans?
		
		DummyResource dummyResource = new DummyResource("dummydata",ip);
		FileServerResource fileServerResource = new FileServerResource("fileserver", ip);
		självaServern.add(dummyResource);
		självaServern.add(fileServerResource);//fileServerResource är ej gjord ännu.
		självaServern.start();
	}
	
	
	
	private static NetworkConfig nwSetup(Map<String, Option> customSettings){
		NetworkConfig nwSettings = new NetworkConfig();
		
		if(customSettings.containsKey("PORT")){
			nwSettings.setInt(NetworkConfigDefaults.DEFAULT_COAP_PORT, customSettings.get("PORT").getIntegerValue());
		}
		System.out.println("Port:" + customSettings.get("PORT").getIntegerValue());
		/*
		if(customSettings.containsKey("TRANSMISSION_TYPE")){
			nwSettings.setInt(NetworkConfigDefaults., customSettings.get("PORT").getIntegerValue());
		}
		*/
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
//>olika klasser i samma javafil
/*
 * Dummydataresurs
 */
class DummyResource extends ResourceBase {
	private InetAddress senderIP;
	public DummyResource(String name, InetAddress ip) {
		super(name);
		senderIP = ip;
		// TODO Auto-generated constructor stub
	}
	public void handleGET(CoapExchange exchange) {
		//generera slumpdata med seed ?xxxxx(skickas från klient), som en byte[size](definierad som option#3),
		//skicka tillbaka denna data
		if(this.senderIP.equals(exchange.getSourceAddress())){
			int size = exchange.getRequestOptions().asSortedList().get(3).getIntegerValue();
			String number = exchange.getRequestOptions().getURIQueryString();
			
			Long seed = Long.parseLong(number);
			Random rnd = new Random(seed);
			byte[] dummyData = new byte[size];
			rnd.nextBytes(dummyData);
			exchange.respond(ResponseCode.CONTENT, dummyData);		
		} else {
			exchange.respond(ResponseCode.UNAUTHORIZED);
		}
	}
	
	public void handlePOST(CoapExchange exchange) {
		//ta emot slumpdata exchange.etcetera, jämför den med egenskapad slumpdata, jämför och skicka tillbaka bedömning
		int size = exchange.getRequestOptions().asSortedList().get(3).getIntegerValue();
		int payloadSize = size;
		String number = exchange.getRequestOptions().getURIQueryString();
		
		//tar ut datat från payloaden		
		byte[] payloadData = new byte[payloadSize];
		payloadData = exchange.getRequestPayload();		
		
		//genererar slumpdata som förhoppningsvis ska stämma med den mottagna slumpdatan
		Long seed = Long.parseLong(number);
		Random rnd = new Random(seed);
		byte[] dummyData = new byte[size];
		rnd.nextBytes(dummyData);
		
		//skriver ut egengenererat data och mottaget
		System.out.println((new String(dummyData, Charset.forName("ISO-8859-1"))) + "]");
		System.out.println((new String(payloadData, Charset.forName("ISO-8859-1"))) + "]");
		
		
		if (Arrays.equals(payloadData, dummyData)){
			//sänd tillbaka något som visar att det var okej
			System.out.println("Egengenererat data stämmer med mottaget!");
			exchange.respond(ResponseCode.VALID);
		}
		else{
			//sänd tillbaka något som skriker att det var fel
			System.out.println("Egengenererat data är inte samma som mottaget data!");
			exchange.respond(ResponseCode.INTERNAL_SERVER_ERROR);
		//Trots responskoden behöver förstås inte fel ligga i serverdelen, korrupt data eller annat högteknologist jävulskap kan ju göra sådant
		}
		
		
		
	}
	public void handleDELETE(CoapExchange exchange) {
		/*
		 * TODO if there is time
		 * low prio
		 */
	}
}


/*
 * Filserverresurs
 */
class FileServerResource extends ResourceBase {
	public FileServerResource(String name, InetAddress ip) {
		super(name);
		InetAddress senderIP = ip;
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

/*
 * lyssnarresurs
 */

class ListeningResource extends ResourceBase {
	public ListeningResource(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	/*
	 * SCHEME: GET for coap://server/random?12345 where 12345 is a seed number
	 *         for a number generator. Returns random bytes.
	 */

	public void handlePOST(CoapExchange exchange) {
		String number = exchange.getRequestOptions().getURIQueryString();
		//System.out.println(exchange.getRequestOptions().asSortedList().get(3).getNumber());
		List<Option> optionList = exchange.getRequestOptions().asSortedList();
		Map<String, Option> startOptions = new HashMap<String, Option>(); 
		
		//FIXA SÅ ATT DET SER UT SÅHÄR!
		//exchange.getRequestOptions().hasOption(65000)
		
		for(int x = 0;x < optionList.size();x++){
			switch(optionList.get(x).getNumber()){
			case 123:	startOptions.put("TEST", optionList.get(x));
						
						break;
			case 65000: startOptions.put("PORT", optionList.get(x));
						System.out.println("Port "+ optionList.get(x).getIntegerValue() + " recieved");
						break;
			//case 65001: startOptions.put("TRANSMISSION_TYPE", optionList.get(x));
			//			break;
			case 65002: startOptions.put("ACK_TIMEOUT", optionList.get(x));
			System.out.println("ACK_TIMEOUT set");
						break;
			case 65003: startOptions.put("ACK_RANDOM_FACTOR", optionList.get(x));
			System.out.println("ACK_RANDOM_FACTOR set");
						break;
			case 65004: startOptions.put("MAX_RETRANSMIT", optionList.get(x));
			System.out.println("MAX_RETRANSMIT set");
						break;
			case 65005: startOptions.put("NSTART", optionList.get(x));
						break;
			case 65006: startOptions.put("PROBING_RATE", optionList.get(x));
						System.out.println("PROBING_RATE set");
						break;
			default: System.out.println("Could not find a valid option for option number: " + optionList.get(x).getNumber());
						break;
			}
		}
		InetAddress ip = exchange.getSourceAddress();
		FileServer.rxServer(startOptions, ip);
		
		exchange.respond(ResponseCode.CREATED);
		//Below should be commented out, it just returns somr dummydata to the sender
		/*
		Long seed = Long.parseLong(number);
		Random rnd = new Random(seed);
		byte[] dummyData = new byte[150];
		rnd.nextBytes(dummyData);
		//System.out.println(new String(dummyData, Charset.forName("ISO-8859-1")));
		exchange.respond(ResponseCode.CONTENT, dummyData);
		*/
	}
}