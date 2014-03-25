package se.ltu.trafikgeneratorserver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jnetpcap.JBufferHandler;
import org.jnetpcap.Pcap;
import org.jnetpcap.PcapBpfProgram;
import org.jnetpcap.PcapDumper;
import org.jnetpcap.PcapHeader;
import org.jnetpcap.PcapIf;
import org.jnetpcap.nio.JBuffer;

public class PacketDumper implements Runnable {
	private Pcap packetCapture;
	private PcapDumper dumper;
	private JBufferHandler<PcapDumper> dumpHandler;
	PacketDumper(File file, int port) throws IOException {
		List<PcapIf> allInterfaces = new ArrayList<PcapIf>();
		Pcap.findAllDevs(allInterfaces, new StringBuilder());
		/*
		 * TODO: Some smarter kind of interface recognition?
		 * For example, "use the interface with the same IP as in the received packet".
		 * 
		 * If the default interface doesn't work, i.e. no packets are dumped,
		 * the interface in question may have to be explicitly specified.
		 */
		PcapIf pcapInterface = PcapIf.findDefaultIf(new StringBuilder());
		//PcapIf pcapInterface = allInterfaces.get(3);
		packetCapture = Pcap.openLive(pcapInterface.getName(), 64*1024, Pcap.MODE_PROMISCUOUS, 10000, new StringBuilder());
		PcapBpfProgram filter = new PcapBpfProgram();
		packetCapture.compile(filter, "port " + port, 0, 0);
		packetCapture.setFilter(filter);
		dumper = packetCapture.dumpOpen(file.toString());
		dumpHandler = new JBufferHandler<PcapDumper>() {
			public void nextPacket(PcapHeader header, JBuffer buffer, PcapDumper dumper) {
				dumper.dump(header, buffer);
			}
		};
	}
	@Override
	public void run() {
		packetCapture.loop(Integer.MAX_VALUE, dumpHandler, dumper);
	}
	void stop() {
		packetCapture.breakloop();
	    dumper.close();
		packetCapture.close();
	}
}
