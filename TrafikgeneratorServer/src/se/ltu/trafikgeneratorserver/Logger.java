package se.ltu.trafikgeneratorserver;

import java.io.File;
import java.io.IOException;

public class Logger {
	static Process proc;
	static Process tempProc;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public static void startLog(File file){
		Runtime rt = Runtime.getRuntime();
		try {
			proc = rt.exec("cmd /c start cmd.exe /K \"cd \\Program Files\\Wireshark && " +
					"dumpcap -w " + file.toString() + " -f \"udp port 56830\"\"");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void mergeLog(File file){
		Runtime rt = Runtime.getRuntime();
		try {
			String x = "\"C:\\Program Files\\Wireshark\\mergecap\" -w " + file.toString().replace("-sndr", "") + " " +
					file.toString().replace("sndr", "rcvr") + " " +
					file.toString().replace(".pcap", "_edited.pcap") +"  ";
			tempProc = rt.exec(x);
			System.out.println(x);
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Wireshark Error: mergecap failed");
		}
	}
	
	public static void editLog(File file, int timeDif){
		Runtime rt = Runtime.getRuntime();
		try {
			String x ="\"C:\\Program Files\\Wireshark\\editcap\" -t " + Integer.toString(timeDif) + " " + 
					file.toString() + " " + 
					file.toString().replace(".pcap", "_edited.pcap") +"  "; 
			tempProc = rt.exec(x);
			System.out.println(x);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (IOException e) {
		
			e.printStackTrace();
			System.out.println("Wireshark Error: editcap failed");
		}
	}
	
	public static void showLog(File file){
		Runtime rt = Runtime.getRuntime();
		try {
			tempProc = rt.exec("cmd /c start cmd.exe /K \"cd \\Program Files\\Wireshark " +
					"&& wireshark -r " + file.toString().replace("-sndr", "") +" \"");
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Wireshark Error: wireshark failed to open merged log");
		}
	}
	public static void exit(){
		proc.destroy();
	}
}
