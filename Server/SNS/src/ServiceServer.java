import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

interface Type {
	int TWEET = 1, READ = 2, REPLY = 3, RETWEET = 4, LIKE = 5;
}

public class ServiceServer implements Runnable {
	ServerSocket mServerSocket;
	Thread[] mThreadArr;
	
	public static void main(String[] args) {
		// create threads
		ServiceServer server = new ServiceServer(4);
		server.start();
		
	}
	
	public ServiceServer(int num) {
		try {
			// create a server socket binded with 7777 port
			mServerSocket = new ServerSocket(7777);
			System.out.println(getTime() + " SNS Server is ready.");
			
			mThreadArr = new Thread[num];
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void start() {
		for (int i = 0; i < mThreadArr.length; i++) {
			mThreadArr[i] = new Thread(this);
			mThreadArr[i].start();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		while (true) {
			try {
				System.out.println(getTime() + " is waiting for requests.");
				
				Socket socket = mServerSocket.accept();
				System.out.println(getTime() + " received a request from " 
						+ socket.getInetAddress());
				
				// get string from the socket
				// and parse to JSONObject
				String inputLine = null;
				String result = "";
				BufferedReader input = new BufferedReader(new InputStreamReader(
						socket.getInputStream(), "UTF-8"));
				while ((inputLine = input.readLine()) != null) {
					result = result.concat(inputLine);
				}												
				JSONParser parser = new JSONParser();
				JSONObject msg = (JSONObject) parser.parse(result);
				
				// check the type of the msg
				// and do the proper operation according to the type
				int type = (int) msg.get("Type");
				String res = null;
				switch (type) {
					case Type.TWEET:
						res = writeStatus(msg);
						break;
					case Type.READ:
						res = readStatus(msg);
						break;
					case Type.REPLY:
						res = writeReply(msg);
						break;
					case Type.RETWEET:
						res= writeRetweet(msg);
						break;
					case Type.LIKE:
						break;													
				}
											
				// send the response with proper message				
				JSONObject resp = new JSONObject();
				resp.put("RESPONSE", res);	
								
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
						socket.getOutputStream(), "UTF-8"));
				out.write(resp.toString());
				out.close();
				
				// close
				input.close();				 				
				socket.close();
			} catch (IOException e) {
				System.out.println("IOException: " + e.getMessage());
			} catch (ParseException e) {
				System.out.println("ParseException: " + e.getMessage());
			} finally {
				System.out.println(getTime() + " has handled the request.");
			}
		}
	}

	private String writeStatus(JSONObject msg) {
		// write the status
		try {
			Connection conn = DriverManager.getConnection("", "", "");			
			System.out.println(getTime() + "connected to MySQL.");
			
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("");
			
			while(rs.next()) {
				rs.getString(1);				
			}
			
			stmt.close();
			rs.close();
			conn.close();			
		} catch (SQLException e) {
			System.out.println("SQLExecption: " + e.getMessage());
		} finally {
			System.out.println("MySQL has handled the request.");
		}
		
		return "";		
	}

	private String readStatus(JSONObject msg) {
		
		return "";
	}

	private String writeReply(JSONObject msg) {
				
		return "";
	}
	
	private String writeRetweet(JSONObject msg) {
				
		return "";
	}

	private String getTime() {
		String name = Thread.currentThread().getName();
		SimpleDateFormat f = new SimpleDateFormat("[hh:mm:ss]");
		return f.format(new Date()) + name;
	}
	
	private int calRTT() {
		
		return 0;
	}
}