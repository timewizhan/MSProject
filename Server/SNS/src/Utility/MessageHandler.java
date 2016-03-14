package Utility;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import Type.opType;

public class MessageHandler {
	public static JSONObject msgParser(Socket socket) {		
		String result = "";	
		BufferedReader input = null;
		JSONObject msg = null;
						
		try {
			input = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			result = input.readLine();		
						
			JSONParser parser = new JSONParser();
			msg = (JSONObject) parser.parse(result);			
		} catch (UnsupportedEncodingException e) {
			System.out.println("[msgParser]UnsupportedEncodingException e: " + e.getMessage());			
		} catch (IOException e) {
			System.out.println("[msgParser]IOException e: " + e.getMessage());
		} catch (ParseException e) {
			System.out.println("[msgParser]ParseException e: " + e.getMessage());
		}	
		return msg;
	}
	
	@SuppressWarnings("unchecked")
	public static String msgGenerator(int result) {
		JSONObject response = new JSONObject();
		response.put("RESPONSE", result);
		
		return response.toString();
	}
	
	@SuppressWarnings("unchecked")
	public static String msgGenerator(JSONArray migrated) {
		JSONObject response = new JSONObject();
		response.put("TYPE", Integer.toString(opType.movein));
		response.put("MIGRATED", migrated);
		
		return response.toString();		
	}
	
	public static void sendMigrated(JSONArray migrated) {
    	String dstServerIP = "localhost";
    	int dstServerPort = 7777;
    	
    	try {
			Socket socket = new Socket(dstServerIP, dstServerPort);
			
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream(), "UTF-8"));
			
			String response = MessageHandler.msgGenerator(migrated);
			
			out.write(response);
			out.newLine();
			out.flush();
			
			socket.close();
			out.close();
		} catch (IOException e) {
			System.out.println("[sendMigrated]IOException: " + e.getMessage());
		}    	
    }
}
