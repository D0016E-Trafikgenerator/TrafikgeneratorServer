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
		if (query.split("&").length == 3 && query.split("=").length == 4 /*&& query.split("=")[0].equals("token")*/) {
			String[] options = query.split("&");
			String token = options[1].split("=")[1];
			String time = options[2].split("=")[1];
			String fileType = options[0].split("=")[1];
			File root = new File(System.getProperty("user.home"));
			File appRoot = new File(root, "trafikgeneratorcoap");
			File subDir = new File(appRoot, "logs");
			subDir.mkdirs();
			
			
			File file = new File(subDir, (time + "-" + token + "-rcvr.pcap"));
			if (file.exists()) {
				if(fileType.equals("log")){
					file = new File(subDir, (time + "-" + token + "-sndr.pcap"));
					try {
						//Test protocol 1.3a.9
						FileOutputStream fos = new FileOutputStream(file);
						fos.write(exchange.getRequestPayload());
						fos.close();
						exchange.respond(ResponseCode.VALID);
						for (TrafikgeneratorServer server : this.server.subservers) {
							if (server.token.equals(token)) {
								
								//Test protocol 1.3a.10
									
								//Sync timestamps with editcap
								//TODO: take time from meta and send to editLog
								Logger.editLog(file, 0);
								
								//Merge logs with mergecap
								Logger.mergeLog(file);
								//proc.destroy();
								
								//Open wireshark with merged logs
								Logger.showLog(file);
									
								
							}
						}
					} catch (IOException e) {
						exchange.respond(ResponseCode.INTERNAL_SERVER_ERROR);
					}
				} else if(fileType.equals("meta")) {
					file = new File(subDir, (time + "-" + token + "-meta.txt"));
					try {
						//Test protocol 1.3a.11
						FileOutputStream fos = new FileOutputStream(file);
						fos.write(exchange.getRequestPayload());
						fos.close();
						exchange.respond(ResponseCode.VALID);
					} catch (IOException e) {
						exchange.respond(ResponseCode.INTERNAL_SERVER_ERROR);
					}
				}
			}
			else
				exchange.respond(ResponseCode.NOT_FOUND);
		}
		else
			exchange.respond(ResponseCode.BAD_REQUEST);
	}
}
