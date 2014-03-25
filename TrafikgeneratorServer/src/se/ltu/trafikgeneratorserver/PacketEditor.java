package se.ltu.trafikgeneratorserver;

import java.io.File;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapDumper;
import org.jnetpcap.packet.PcapPacket;

public class PacketEditor {
	/*
	 * A class for editing Pcap log files.
	 */
	public static void modifyTimestamps(File pcapFile, int seconds, int microseconds) {
		File editedPcapFile = new File(pcapFile.toString().replace(".pcap", "_edited.pcap"));
		Pcap packetCaptureIn = Pcap.openOffline(pcapFile.toString(), new StringBuilder());
		PcapDumper packetCaptureOut = packetCaptureIn.dumpOpen(editedPcapFile.toString());
		PcapPacket packet = new PcapPacket(0);
		while (packetCaptureIn.nextEx(packet) == 1) {
			packet.getCaptureHeader().hdr_usec(packet.getCaptureHeader().hdr_usec()+microseconds);
			int addSeconds = packet.getCaptureHeader().hdr_usec()/1000000;
			packet.getCaptureHeader().hdr_usec(packet.getCaptureHeader().hdr_usec()-(addSeconds*1000000));
			packet.getCaptureHeader().hdr_sec(packet.getCaptureHeader().hdr_sec()+seconds+addSeconds);
			packetCaptureOut.dump(packet);
		}
		packetCaptureOut.close();
		packetCaptureIn.close();
	}
}
