package se.ltu.trafikgeneratorserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.server.resources.CoapExchange;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;

public class FileResource extends ResourceBase {
	private TrafikgeneratorServer server;
	public FileResource(String name, TrafikgeneratorServer server) {
		super(name);
		this.server = server;
	}
	public void handlePOST(CoapExchange exchange) {
		exchange.accept();
		String query = exchange.getRequestOptions().getURIQueryString();
		if (query.split("&").length == 3 && query.split("=").length == 4) {
			String[] options = query.split("&");
			String token = options[1].split("=")[1];
			String time = options[2].split("=")[1];
			String fileType = options[0].split("=")[1];
			File root = new File(System.getProperty("user.home"));
			File appRoot = new File(root, "trafikgeneratorcoap");
			File subDir = new File(appRoot, "logs");
			subDir.mkdirs();
			
			File metaFile = new File(subDir, (time + "-" + token + "-meta.txt"));
			File rcvrFile = new File(subDir, (time + "-" + token + "-rcvr.pcap"));
			File sndrFile = new File(subDir, (time + "-" + token + "-sndr.pcap"));
			if (rcvrFile.exists() || sndrFile.exists()) {
				if(fileType.equals("log")){
					File localFile = null, remoteFile = null;
					if (rcvrFile.exists() && !sndrFile.exists()) {
						localFile = rcvrFile;
						remoteFile = sndrFile;
					}
					else if (sndrFile.exists() && !rcvrFile.exists()) {
						localFile = sndrFile;
						remoteFile = rcvrFile;
					}
					try {
						//Test protocol 1.3a.9
						FileOutputStream fos = new FileOutputStream(remoteFile);
						fos.write(exchange.getRequestPayload());
						fos.close();
						exchange.respond(ResponseCode.VALID);
						for (TrafikgeneratorServer server : this.server.subservers) {
							if (server.token.equals(token)) {
								BufferedReader fis = new BufferedReader(new FileReader(metaFile));
								String z;
								int millisecondsOffset = 0;
								
								while(fis.ready()){
									z = fis.readLine();
									if(z.contains("BEFORE_TEST NTP_ERROR="))
										millisecondsOffset = Integer.valueOf(z.split("=")[1]);
										//TODO: average of before/after?
								}
								int seconds = millisecondsOffset / 1000;
								int microseconds = (millisecondsOffset - (seconds*1000))*1000;
								PacketEditor.modifyTimestamps(remoteFile, seconds, microseconds);
								fis.close();
								
								//Test protocol 1.3a.10
								//Merge logs with mergecap
								//Logger.mergeLog(file);
							}
						}
					} catch (IOException e) {
						exchange.respond(ResponseCode.INTERNAL_SERVER_ERROR);
					}
				} else if(fileType.equals("meta")) {
					try {
						FileOutputStream fos = new FileOutputStream(metaFile);
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
