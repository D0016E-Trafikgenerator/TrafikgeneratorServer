package se.ltu.trafikgeneratorserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Logger {
	static Process proc;
	static Process tempProc;
	private static final String TASKLIST = "tasklist";
	private static final String KILL = "taskkill /IM ";
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
	
	public static void mergeLog(File logFile){
		Runtime rt = Runtime.getRuntime();
		try {
			String x = "\"C:\\Program Files\\Wireshark\\mergecap\" -w " + logFile.toString().replace("-sndr", "") + " " +
					logFile.toString().replace("sndr", "rcvr") + " " +
					logFile.toString().replace(".pcap", "_edited.pcap") +"  ";
			tempProc = rt.exec(x);
			System.out.println(x);
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Wireshark Error: mergecap failed");
		}
	}
	
	public static void editLog(File logFile, double d){
		Runtime rt = Runtime.getRuntime();
		try {
			String x ="\"C:\\Program Files\\Wireshark\\editcap\" -t " + Double.toString(d) + " " + 
					logFile.toString() + " " + 
					logFile.toString().replace(".pcap", "_edited.pcap") +"  "; 
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
		//proc.destroy();
		 String processName = "dumpcap.exe";

		 //System.out.print(isProcessRunging(processName));

		 try {
			if (isProcessRunging(processName)) {

			  killProcess(processName);
			 }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static boolean isProcessRunging(String serviceName) throws Exception {

		 Process p = Runtime.getRuntime().exec(TASKLIST);
		 BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		 String line;
		 while ((line = reader.readLine()) != null) {

		  System.out.println(line);
		  if (line.contains(serviceName)) {
		   return true;
		  }
		 }

		 return false;

		}

	private static void killProcess(String serviceName) throws Exception {
		System.out.println("PROC KILLED!");
	  Runtime.getRuntime().exec(KILL + serviceName);

	}
}
