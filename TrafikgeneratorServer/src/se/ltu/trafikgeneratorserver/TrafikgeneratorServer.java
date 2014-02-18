package se.ltu.trafikgeneratorserver;

import java.util.ArrayList;
import java.util.concurrent.Executors;

import ch.ethz.inf.vs.californium.network.config.NetworkConfig;
import ch.ethz.inf.vs.californium.server.Server;

public class TrafikgeneratorServer extends Server {
	ArrayList<TrafikgeneratorServer> subservers = new ArrayList<TrafikgeneratorServer>();
	String token = "";
	long clientTimeBeforeTest, clientTimeAfterTest;
	public TrafikgeneratorServer(NetworkConfig networkConfig) {
		super(networkConfig);
	}
	public static void main(String[] args) {
		//Test protocol 1.2b.1
		//TODO: code to start NTP server
		TrafikgeneratorServer server = new TrafikgeneratorServer(new NetworkConfig());
		server.setExecutor(Executors.newScheduledThreadPool(4));
		ControlResource control = new ControlResource("control", server);
		server.add(control);
		FileResource file = new FileResource("file", server);
		server.add(file);
		server.start();
	}
}