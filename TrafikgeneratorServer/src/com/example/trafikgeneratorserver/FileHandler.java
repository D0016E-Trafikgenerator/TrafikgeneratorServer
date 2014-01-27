package com.example.trafikgeneratorserver;
import java.io.*;
import java.util.*;
import java.lang.Object;
//import java.io.BufferReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;


public class FileHandler {
	static Path defaultPath = Paths.get("C:\\CoAPFileServer\\");
	static String testFile = "log1.txt";
	static String testInsertion = "1 hej\n10000000 prutt\n";
	Path logName;
	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		//read(testFile, testInsertion);
		
	}
	private void delete(String fileName){
		//Not prioratized
	}
	void create(String fileName) throws IOException { 
		logName = Paths.get(defaultPath + "\\" + fileName);
		Files.createDirectories(logName.getParent());
		try {
			Files.createFile(logName);
		} catch (FileAlreadyExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	void addLog(String content) throws FileNotFoundException{
		StringList sl = new StringList();
		sl.read(logName.toString());
		
		String [] items = content.split("\n");
		ArrayList<String> sb = new ArrayList<String>(Arrays.asList(items));
		
		for(int i = 0; i < sb.size(); i++){
			sl.add(sb.get(i));
		}
		sl.save(logName.toString());
	}
	//skdajskld
	void add(String content) throws FileNotFoundException{
		try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(logName.toString(), true)))) {
		    out.println("\n" + content);
		    out.close();
		}catch (IOException e) {
		    //exception handling left as an exercise for the reader
		}
		
		/*StringList sl = new StringList();
		sl.read(logName.toString());
		
		String [] items = content.split("\n");
		ArrayList<String> sb = new ArrayList<String>(Arrays.asList(items));
		
		for(int i = 0; i < sb.size(); i++){
			sl.add(sb.get(i));
		}
		sl.save(logName.toString());*/
	}
	
	void merge(String fileName, String clientLog, String serverLog) throws FileNotFoundException{

		StringList clientLogList = new StringList();
		clientLogList.read(defaultPath.toString() +"\\"+ clientLog);
		StringList serverLogList = new StringList();
		serverLogList.read(defaultPath.toString() +"\\"+ serverLog);
		
		StringList sl = new StringList();
		sl = mergeSort(clientLogList,serverLogList);
		
		sl.save(defaultPath.toString() +"\\"+ fileName);
		
		//return fileString;
	}
	
	private static StringList mergeSort(StringList sl, StringList insertions){
		//Merge two sorted 
		int slCounter = 0;
		int insertCounter = 0;
		
		StringList sb = new StringList();
		
		while( slCounter < sl.size() && insertCounter < insertions.size()){
			//Get timestamps
			int slTime = Integer.parseInt(sl.get(slCounter).split(" ")[0]);
			int insTime = Integer.parseInt(insertions.get(insertCounter).split(" ")[0]);
			
			//Append smallest timestamp
			if(slTime < insTime){
				sb.add(sl.get(slCounter));
				slCounter += 1;
			} else {
				sb.add(insertions.get(insertCounter));
				insertCounter += 1;
			}
		}
		//Append leftovers
		if(slCounter < sl.size()){
			for(int i=slCounter; i < sl.size(); i += 1){
				sb.add(sl.get(i));
			}
		} else {
			for(int i=insertCounter; i < insertions.size(); i += 1){
				sb.add(insertions.get(i));
			}
		}
		return sb;
	}
}



/*

	ladda in existerande fil
	
	läs rad från input
	
	kolla vart i existerande fil raden ska läggas in mha timestampsen
	
	läs nästa rad från input, annars spara ner fil

*/