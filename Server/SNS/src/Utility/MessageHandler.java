package Utility;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import Type.ServerAddr;
import Type.opType;

public class MessageHandler {
	
	public final static String store_complete = "store_complete";
	public final static String data_replacement_complete = "data_replacement_complete";
	public final static String restart_cpu_monitoring = "restart_cpu_monitoring";
	public final static String invalid_operation_type = "invalid_operation_type";	
	
	public static JSONObject msgParser(Socket socket) {		
		String result = "";	
		BufferedReader in = null;
		JSONObject msg = null;
						
		try {
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			result = in.readLine();		
						
			JSONParser parser = new JSONParser();
			msg = (JSONObject) parser.parse(result);			
		} catch (UnsupportedEncodingException e) {
			System.out.println("[msgParser]UnsupportedEncodingException: " + e.getMessage());			
		} catch (IOException e) {
			System.out.println("[msgParser]IOException: " + e.getMessage());
		} catch (ParseException e) {
			System.out.println("[msgParser]ParseException: " + e.getMessage());
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
	public static String msgGenerator(JSONObject migrated) {
		JSONObject response = new JSONObject();
		response.put("TYPE", String.valueOf(opType.movein));
		response.put("MIGRATED", migrated);
		
		return response.toString();		
	}
	
	public static int sendMigrated(int curr, JSONObject migrated) {
    	String dstServerIP = ServerAddr.IP_LIST[curr];
    	int dstServerPort = ServerAddr.PORT;    	
    	int result = 0;
    	
    	Socket socket = null;
    	BufferedWriter out = null;
    	
    	try {
			socket = new Socket(dstServerIP, dstServerPort);
			
			out = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream(), "UTF-8"));
			
			String migrated_data = MessageHandler.msgGenerator(migrated);
			
			out.write(migrated_data);			
			out.newLine();
			out.flush();			
						
			JSONObject response = msgParser(socket);												
			
			result = (int) (long) response.get("RESPONSE");						
		} catch (IOException e) {
			System.out.println("[sendMigrated]IOException: " + e.getMessage());
		} finally {			
			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
					System.out.println("[sendMigrated/out]IOException: " + e.getMessage());
				}
			if (socket != null)
				try {
					socket.close();
				} catch (IOException e) {
					System.out.println("[sendMigrated/socket]IOException: " + e.getMessage());
				}
		}		    	
    	return result;
    }
}
