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

/**
 * The Class MessageHandler.
 */
public class MessageHandler {
		
	public final static String mSTORE_COMPLETE = "store_complete";		
	public final static String mDATA_REPLACEMENT_COMPLETE = "data_replacement_complete";		
	public final static String mRESTART_CPU_MONITORING = "restart_cpu_monitoring";	
	
	/**
	 * Parses the string message into JSON object
	 *
	 * @param socket the client socket
	 * @return the message in JSON object
	 */
	public static JSONObject msgParser(Socket socket) {		
		String result = "";	
		BufferedReader in = null;
		JSONObject msg = null;
		
		try {
			System.out.println(socket.getInetAddress());
						
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
	
	/**
	 * Generates the response message
	 * for the user's request
	 *
	 * @param result the result of operation handling
	 * @param delay the calculated RTT
	 * @return the response message in string
	 */
	@SuppressWarnings("unchecked")
	public static String msgGenerator(int result, double delay) {
		JSONObject response = new JSONObject();
		response.put("RESPONSE", result);
		response.put("RTT", delay);
		
		return response.toString();
	}		
	
	/**
	 * Generates the migrated message to send
	 *
	 * @param migrated the migrated message in JSON object
	 * @return the migrated message in string
	 */
	@SuppressWarnings("unchecked")
	public static String msgGenerator(JSONObject migrated) {
		JSONObject response = new JSONObject();
		response.put("TYPE", String.valueOf(opType.mMOVEIN));
		response.put("MIGRATED", migrated);
		
		return response.toString();		
	}
	
	/**
	 * Sends the migrated data
	 *
	 * @param curr the next service server
	 * @param migrated the migrated data in JSON object
	 * @return the result of sending the migrated data
	 */
	public static int sendMigrated(int curr, JSONObject migrated) {
    	String dstServerIP = ServerAddr.getServerAddr(curr);
    	int dstServerPort = ServerAddr.getServerPort();
    	int result = 0;
    	
    	Socket socket = null;
    	BufferedWriter out = null;
    	if (dstServerIP != null) {
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
    	return result;
    }
	
	/**
	 * Restores the migrated data
	 * when the sending the migrated data is failed
	 *
	 * @param migrated the migrated message in JSON object
	 */
	public static void restoreMigrated(JSONObject migrated) {
    	String dstServerIP = ServerAddr.getLocalAddr();
    	int dstServerPort = ServerAddr.getServerPort();
    	
    	Socket socket = null;
    	BufferedWriter out = null;
    	if (dstServerIP != null) {
	    	try {    		
	    		socket = new Socket(dstServerIP, dstServerPort);
				
				out = new BufferedWriter(new OutputStreamWriter(
						socket.getOutputStream(), "UTF-8"));
				
				String migrated_data = MessageHandler.msgGenerator(migrated);
				
				out.write(migrated_data);			
				out.newLine();
				out.flush();																
			} catch (IOException e) {
				System.out.println("[restoreMigrated]IOException: " + e.getMessage());
			} finally {			
				if (out != null)
					try {
						out.close();
					} catch (IOException e) {
						System.out.println("[restoreMigrated/out]IOException: " + e.getMessage());
					}
				if (socket != null)
					try {
						socket.close();
					} catch (IOException e) {
						System.out.println("[restoreMigrated/socket]IOException: " + e.getMessage());
					}
			}		    		    	
    	}
    }
}