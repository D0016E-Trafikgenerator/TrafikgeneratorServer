package com.example.trafikgeneratorserver;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileHandler {
	static Path defaultPath = Paths.get("C:\\CoAPFileServer\\");
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		

	}
	private void delete(String fileName){
		
	}
	private void create(String fileName){
		
	}
	private String read(String fileName) throws FileNotFoundException{
		String fileString = fileName;//remove this, let fileString become the data from the file
		
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		try {
			StringBuilder sb = new StringBuilder();
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
			
		
		return fileString;
	}
}
