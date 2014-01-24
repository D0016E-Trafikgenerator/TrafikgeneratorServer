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
		Path path = Paths.get(defaultPath + "\\" + fileName);
		Files.createDirectories(path.getParent());
		try {
			Files.createFile(path);
		} catch (FileAlreadyExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/*private String save(String fileName, String content) throws FileNotFoundException{
		BufferedReader br = new BufferedReader(content);
		try {
			ArrayList<String> sb = new ArrayList<String>();
		    String line = br.readLine();

		    while (line != null) {

		    	sb.append(line);
		        sb.append('\n');
		        line = br.readLine();
		    }
		        String everything = sb.toString();
		    } finally {
		        br.close();
		    }
	}*/
	
	void saveTo(String fileName, String content) throws FileNotFoundException{

		
		StringList sl = new StringList();
		sl.read(defaultPath.toString() +"\\"+ fileName);
		
		//ArrayList<String> insertions = new ArrayList<String>();
		
		//BufferedReader br = new BufferedReader( new FileReader(content));
		//List<String> sb = Lists.newArrayList(Splitter.on("\n").split(content));
		String [] items = content.split("\n");
		
		ArrayList<String> sb = new ArrayList<String>(Arrays.asList(items));
		
		//Collection.addAll(sb, items);
		/*try {
			//StringBuilder sb = new StringBuilder();
			ArrayList<String> sb = new ArrayList<String>();
			String line = br.readLine();

		    while (line != null) {

		    	sb.add(line);
		        //sb.append('\n');
		        line = br.readLine();
		    }
		        //String everything = sb.toString();
	    } finally {
	        br.close();
	    }*/
		sl = mergeSort(sl,sb);
		
		sl.save(defaultPath.toString() +"\\"+ fileName);
		
		//return fileString;
	}
	
	private static StringList mergeSort(StringList sl, ArrayList<String> insertions){
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
		sl = sb;
		return sl;
	}
}



/*

	ladda in existerande fil
	
	läs rad från input
	
	kolla vart i existerande fil raden ska läggas in mha timestampsen
	
	läs nästa rad från input, annars spara ner fil

*/