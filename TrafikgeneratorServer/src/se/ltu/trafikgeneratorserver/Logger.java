package se.ltu.trafikgeneratorserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Logger {
	static Process proc;
	static Process tempProc;
	static Runtime dumpcap;
	static List<String> cmdList;
	private static final String TASKLIST = "tasklist";
	private static final String KILL = "taskkill /IM ";
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public static void startLog(File file){
		dumpcap = Runtime.getRuntime();
		cmdList = new ArrayList<String>();
		try {
			cmdList = isCmdRunning("cmd.exe");
				
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			//TODO: make port a variable
			proc = dumpcap.exec("cmd /c start cmd.exe /K \"cd \\Program Files\\Wireshark && " +
					"dumpcap -w " + file.toString() + " -f \"udp port 56830\"\"");
			//proc =  rt.exec("\"C:\\Program Files\\Wireshark\\dumpcap\" -w " + file.toString() + " -f \"udp port 56830\"\"");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void mergeLog(File logFile){
		Runtime rt = Runtime.getRuntime();
		String merger = "";
		if(logFile.toString().contains("rcvr")){
			merger = "\"C:\\Program Files\\Wireshark\\mergecap\" -w " + logFile.toString().replace("-rcvr", "") + " " +
					logFile.toString().replace("rcvr", "sndr") + " " +
					logFile.toString().replace(".pcap", "_edited.pcap") +"  ";
		} else {
			merger = "\"C:\\Program Files\\Wireshark\\mergecap\" -w " + logFile.toString().replace("-sndr", "") + " " +
					logFile.toString().replace("sndr", "rcvr") + " " +
					logFile.toString().replace(".pcap", "_edited.pcap") +"  ";
		}
		try { 
			tempProc = rt.exec(merger);
			System.out.println(merger);
			
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
			/*tempProc = rt.exec("cmd /c start cmd.exe /K \"cd \\Program Files\\Wireshark " +
					"&& wireshark -r " + file.toString().replace("-sndr", "") +" \""); */
			tempProc = rt.exec("\"C:\\Program Files\\Wireshark\\wireshark\" -r " + file.toString().replace("-sndr", "").replace("-rcvr", ""));
			
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
			if (isProcessRunning(processName)) {

			  killProcess(processName);
			 }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static boolean isProcessRunning(String serviceName) throws Exception {

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
	
	private static List<String> isCmdRunning(String serviceName) throws Exception {
		List<String> cmdList = new ArrayList<String>();
		String[] tempList;
		Process p = Runtime.getRuntime().exec(TASKLIST);
		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.contains(serviceName)) {
				tempList =  line.split("\\s+");
				cmdList.add(tempList[1]);
				//cmdList.add(line.split(" ")[1]);
				System.out.println(line);
				
			}
		}

		return cmdList;
	
	}

	private static void killProcess(String serviceName) throws Exception {
		List<String> newCmdList = new ArrayList<String>();
		newCmdList = isCmdRunning("cmd.exe");
		//Runtime.getRuntime().exec(KILL + serviceName);
		Runtime rt = Runtime.getRuntime();
		rt.exec("taskkill /f /im " + serviceName);
		for(int x=0; x<newCmdList.size() ;x++){
			if(!cmdList.contains(newCmdList.get(x))){
				rt.exec("taskkill /f /pid " + newCmdList.get(x));
				System.out.println("CMD WITH PID " + newCmdList.get(x) + " KILLED!");
			}
			
		}
		//rt.exec("taskkill /f /im cmd.exe");

		System.out.println("PROC KILLED!");
		System.out.println(newCmdList);
		System.out.println(cmdList);
	}
}
