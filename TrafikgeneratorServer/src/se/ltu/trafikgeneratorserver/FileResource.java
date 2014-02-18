package se.ltu.trafikgeneratorserver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;

public class FileResource extends ResourceBase {
	private TrafikgeneratorServer server;
	private long clientTimeBeforeTest, clientTimeAfterTest;
	public FileResource(String name, TrafikgeneratorServer server) {
		super(name);
		this.server = server;
	}
	public void handlePOST(CoapExchange exchange) {
		exchange.accept();
		String query = exchange.getRequestOptions().getURIQueryString();
		if (query.split("&").length == 1 && query.split("=").length == 2 && query.split("=")[0].equals("token")) {
			String token = query.split("=")[1];
			File root = new File(System.getProperty("user.home"));
			File appRoot = new File(root, "trafikgeneratorcoap");
			File subDir = new File(appRoot, "logs");
			subDir.mkdirs();
			//TODO: generalize so the user doesn't have to send the file the same day
			File file = new File(subDir, (new SimpleDateFormat("yyyyMMdd")).format(new Date()) + "-" + token + "-rcvr.pcap");
			if (file.exists()) {
				file = new File(subDir, (new SimpleDateFormat("yyyyMMdd")).format(new Date()) + "-" + token + "-sndr.pcap");
				try {
					//Test protocol 1.2b.9
					FileOutputStream fos = new FileOutputStream(file);
					fos.write(exchange.getRequestPayload());
					fos.close();
					exchange.respond(ResponseCode.VALID);
					for (TrafikgeneratorServer server : this.server.subservers) {
						if (server.token.equals(token)) {
							clientTimeBeforeTest = server.clientTimeBeforeTest;
							clientTimeAfterTest = server.clientTimeAfterTest;
							int timeDif = (int) ((clientTimeAfterTest+clientTimeBeforeTest)/2);
							//Test protocol 1.2b.10
<<<<<<< Upstream, based on rcv-pcap
							if (true || Math.abs(clientTimeAfterTest-clientTimeBeforeTest) > 3) {
								//TODO: use editcap to correct timestamps in received file
								//TODO: use mergecap to merge server and client log files
=======
							if (Math.abs(clientTimeAfterTest-clientTimeBeforeTest) > 3) {
								Runtime rt = Runtime.getRuntime();
								Process proc = null;
								try {
									proc = rt.exec("cmd /c start cmd.exe /K \"cd \\Program Files\\Wireshark " +
											"&& editcap -t " + Integer.toString(timeDif) + file.toString() + " " + file.toString().replace(".pcap", "_edited.pcap") +"  \"");
									
								} catch (IOException e) {
								
									e.printStackTrace();
									System.out.println("Wireshark Error: editcap failed");
								}
								proc.destroy();
								
								try {
									proc = rt.exec("cmd /c start cmd.exe /K \"cd \\Program Files\\Wireshark " +
											"&& mergecap -w " + file.toString().replace("-sndr", "") + " " +
											file.toString().replace("sndr", "rcvr") + " " +
											file.toString().replace(".pcap", "_edited.pcap") +"  \"");
									
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
									System.out.println("Wireshark Error: mergecap failed");
								}
								proc.destroy();
								
>>>>>>> 91c16b4 NTP merge timefix
								//TODO: start Wireshark with the merged file
								
							}
							else {
								exchange.respond(ResponseCode.NOT_ACCEPTABLE);
								System.out.println("The data is probably useless, the time on the client has drifted a lot during the test...");
							}
						}
					}
				} catch (IOException e) {
					exchange.respond(ResponseCode.INTERNAL_SERVER_ERROR);
				}
			}
			else
				exchange.respond(ResponseCode.NOT_FOUND);
		}
		else
			exchange.respond(ResponseCode.BAD_REQUEST);
	}
}
