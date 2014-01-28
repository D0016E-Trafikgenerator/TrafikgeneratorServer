package com.example.trafikgeneratorserver;

import java.awt.event.ActionEvent;
import java.net.InetAddress;
import java.util.Date;
import org.apache.commons.net.ntp.NTPUDPClient; 
import org.apache.commons.net.ntp.TimeInfo;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import javax.swing.Timer;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.Option;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.network.config.NetworkConfig;
import ch.ethz.inf.vs.californium.network.config.NetworkConfigDefaults;
import ch.ethz.inf.vs.californium.server.Server;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;
import java.util.Observable;
import java.util.Observer;
public class FileServer {
	static Server testServer;
	private static long startTime;
	public static void main(String[] args)
	{
		NetworkConfig nwSettings = new NetworkConfig();		
		Server mainServer = new Server(nwSettings);
		mainServer.setExecutor(Executors.newScheduledThreadPool(4));
		//Resurs självaResursen = new Resurs("backwards");//Här och nedanför bör kanske istället lyssningsresursen skapas och läggas till 
		//RandomResource slumpen = new RandomResource("random");
		//självaServern.add(självaResursen);
		//självaServern.add(slumpen);
		ListeningResource listener = new ListeningResource("control", testServer);
		mainServer.add(listener);
		mainServer.start();
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
	public static Server rxServer(Map<String, Option> args, InetAddress ip, FileHandler fh) {
		NetworkConfig nwSettings = nwSetup(args);
		testServer = new Server(nwSettings);
		testServer.setExecutor(Executors.newScheduledThreadPool(4));
			
		//args to resource constructor is name of resource + senders IP
		//IP kan man få från själva CoAP-exchange, men den kan ju vara lite jobbig att få _här_
		//Kanske bättre att använda någon annanstans?
				
		DummyResource dummyResource = new DummyResource("dummydata",ip, fh, testServer);
		FileServerResource fileServerResource = new FileServerResource("fileserver", ip);
		testServer.add(dummyResource);
		testServer.add(fileServerResource);//fileServerResource är ej gjord ännu.
		testServer.start();
		
		return testServer;
		//Somehow make testServer stop when STOP has been sent from client.
		/*synchronized(dummyResource) {
			try {
				testServer.wait();
			} catch (InterruptedException e) {
				synchronized(fileServerResource){
					try {
						testServer.wait();
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						testServer.stop();
					}
				}
			}
		}*/
		

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
class DummyResource extends ResourceBase  {
	private InetAddress senderIP;
	private FileHandler fh;
	Date time;
	NTPUDPClient timeClient;
	TimeInfo timeInfo;
	String TIME_SERVER;
	InetAddress inetAddress;
	Server testServer;
	public DummyResource(String name, InetAddress ip, FileHandler FH, Server testServer) {
		super(name);
		senderIP = ip;
		fh = FH;
		this.testServer = testServer;
		TIME_SERVER = "time-a.nist.gov";   
		timeClient = new NTPUDPClient();
		inetAddress = null;
		try {
			inetAddress = InetAddress.getByName("0.se.pool.ntp.org");
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			System.out.println("Server Error: NTP Server not found");
			e1.printStackTrace();
		}
		
		// TODO Auto-generated constructor stub
	}
	
	private void addToLog(String event, String msgType, CoapExchange exchange){
		//Save log 
		String timeStamp =""; //NTP server = 0.se.pool.ntp.org
		String msgId = "";
		String payloadSize = "";
		String code = "";
		String logContent = "";
		String token = "";
		try {
			timeInfo = timeClient.getTime(inetAddress);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Server Error: Could not get time from NTP server");
			e.printStackTrace();
		}
		long returnTime = timeInfo.getMessage().getTransmitTimeStamp().getTime();
		time = new Date(returnTime);
		timeStamp = Long.toString(time.getTime());
		if(event.equals("SERVER_RCVD")){
			msgId = Integer.toString(exchange.advanced().getCurrentRequest().getMID());
			token = exchange.advanced().getCurrentRequest().getTokenString();
			payloadSize = Integer.toString(exchange.advanced().getCurrentRequest().getPayloadSize());
			code = exchange.advanced().getCurrentRequest().getCode().toString();
		} else if (event.equals("SERVER_SENT")) {
			msgId = Integer.toString(exchange.advanced().getCurrentResponse().getMID());
			payloadSize = Integer.toString(exchange.advanced().getCurrentResponse().getPayloadSize());
			code = exchange.advanced().getCurrentResponse().getCode().toString();
			token = exchange.advanced().getCurrentResponse().getTokenString();
		}
		
		logContent = timeStamp + " " + event + " " + msgId  + " " + msgType + " " + payloadSize + " " + code + " " + token; 

		try {
			fh.add(logContent);
		} catch (FileNotFoundException e) {
			System.out.println("File Server ERROR: Log file not found!");
			e.printStackTrace();
		}
	}
 	public void handleGET(CoapExchange exchange) {
		//används när server blir client?
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
		
		
		
		//STOP code has been sent from client
		if(exchange.getRequestOptions().hasOption(65009)){
			addToLog("SERVER_RCVD", "STOP", exchange);
			exchange.respond(ResponseCode.DELETED); //The server is deleted
			testServer.stop();
		
		} else if(exchange.advanced().getCurrentRequest().isConfirmable())	{
			//Respond to client
			addToLog("SERVER_RCVD", "CON", exchange);
			exchange.respond(ResponseCode.VALID);
			addToLog("SERVER_SENT", "ACK", exchange);
		
		} else {
			//Do not respond to client
			addToLog("SERVER_RCVD", "NON", exchange);
		}
		/*
		//ta emot slumpdata exchange.etcetera, jämför den med egenskapad slumpdata, jämför och skicka tillbaka bedömning
		int size = exchange.getRequestOptions().asSortedList().get(3).getIntegerValue();
		int payloadSize = size;
		String number = exchange.getRequestOptions().getURIQueryString();
		
		//tar ut datat från payloaden		
		byte[] payloadData = new byte[payloadSize];
		payloadData = exchange.getRequestPayload();
		*/
		
		
		/*//genererar slumpdata som förhoppningsvis ska stämma med den mottagna slumpdatan
		Long seed = Long.parseLong(number);
		Random rnd = new Random(seed);
		byte[] dummyData = new byte[size];
		rnd.nextBytes(dummyData);
		
		//skriver ut egengenererat data och mottaget
		System.out.println((new String(dummyData, Charset.forName("ISO-8859-1"))) + "]");
		System.out.println((new String(payloadData, Charset.forName("ISO-8859-1"))) + "]");
		
		
		//Ta bort check av dummydata
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
		}*/
		
		//Hantera STOP - stäng ner servern
		
		
		
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
	private InetAddress senderIP;
	private FileHandler fh;
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

class ListeningResource extends ResourceBase  {
	Server testServer;
	public ListeningResource(String name, Server testServer) {
		super(name);
		this.testServer = testServer;
		// TODO Auto-generated constructor stub
	}
	/*
	 * SCHEME: GET for coap://server/random?12345 where 12345 is a seed number
	 *         for a number generator. Returns random bytes.
	 */

	public void handlePOST(CoapExchange exchange) {
		//If START code has been sent
		if(exchange.getRequestOptions().hasOption(65008)){
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
				
			FileHandler fh = new FileHandler();
			
			Date date = new Date();
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
			String formatedDate = format.format(date);
			String logName = formatedDate + "\\" + exchange.advanced().getCurrentRequest().getTokenString() + "_server.log";
			
			try {
				fh.create(logName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("File Server Error: Unable to create file!");
				e.printStackTrace();
			}
			
			InetAddress ip = exchange.getSourceAddress();
			testServer = FileServer.rxServer(startOptions, ip, fh);
			exchange.respond(ResponseCode.CREATED);
		} else if(exchange.getRequestOptions().hasOption(65009)) {
			String URI = exchange.getRequestOptions().getURIQueryString();
			String [] items = URI.split("=");
			ArrayList<String> URIlist = new ArrayList<String>(Arrays.asList(items));
			
			//Test is finished, client sends log to be merged
			System.out.println("\n" + URIlist + "\n");
			if(URIlist.size()>0 && URIlist.get(0).equals("token")){
				System.out.println("\n" + "MERGING OF LOGS STARTED!" + "\n");
				FileHandler fh = new FileHandler();
				Date date = new Date();
				SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
				String formatedDate = format.format(date);
				String logNameClient = formatedDate + "\\" + URIlist.get(1) + "_client.log";
				String logNameServer = formatedDate + "\\" + URIlist.get(1) + "_server.log";
				String logName = formatedDate + "\\" + URIlist.get(1) + ".log";
				
				try {
					fh.create(logNameClient);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("File Server Error: Unable to create client log file!");
					e.printStackTrace();
				}
				try {
					fh.addLog(exchange.getRequestText());
					System.out.println("\n" + exchange.getRequestText() + "\n");
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					System.out.println("File Server Error: Could not save payload to file");
					e.printStackTrace();
				}
				try {
					fh.merge(logName, logNameClient, logNameServer);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					System.out.println("File Server Error: Could not merge log files");
					e.printStackTrace();
				}
				
				exchange.respond(ResponseCode.DELETED);
				testServer.stop();
				
			} else {
				
				
				
				
			}	
			
		}	
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

