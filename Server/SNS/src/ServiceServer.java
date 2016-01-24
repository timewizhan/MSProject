import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

interface Type {
	int TWEET = 1, READ = 2, REPLY = 3, RETWEET = 4, REPLACEMENT = 5;
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

				System.out.println("Call writeStatus");
				writeStatus();
				
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
//						res = writeStatus(msg);
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
					case Type.REPLACEMENT:
						doReplacement();
						break;													
				}
											
				// prepare the response message
				JSONObject resp = new JSONObject();
				resp.put("RESPONSE", res);					
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
						socket.getOutputStream(), "UTF-8"));				
				
				// insert the delay to emulate geo-distributed clouds 
				try {
					Thread.sleep(calRTT());
				} catch(InterruptedException e) {
					System.out.println("InterruptedException: " + e.getMessage());
				}
				
				// send the response message
				out.write(resp.toString());
								
				// close
				out.close();
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

	private String writeStatus() {
		
		try {
			Connection conn = DriverManager.getConnection(
					"jdbc:mysql://localhost/snsdb?autoReconnect=true&useSSL=false", "snsuser", "password");			
			
			System.out.println(getTime() + "connected to MySQL.");

			String uname = "bokor";
			
			Statement isResident = conn.createStatement();
			ResultSet res = isResident.executeQuery("SELECT uid,type FROM users WHERE uname =" + "'" + uname + "'");
						
			if (res.next()) {			
				System.out.println("bokor does exist!");
				
				// check the type and update the type from 10 (V) to 11 (RV) if needed
			
				// write his/her own status
								
			} else {
				System.out.println("bokor does not exist!");
				// add him/her to USERS as 01 (RESIDENT)
				
				// write his/her own status
				
			}
			
			res.close();
			conn.close();			
		} catch (SQLException e) {
			System.out.println("SQLExecption: " + e.getMessage());
		} finally {
			System.out.println("MySQL has handled the query.");
		}
		
		return "";		
	}

	private String readStatus(JSONObject msg) {
		
		try {
			Connection conn = DriverManager.getConnection(
					"jdbc:mysql://localhost/snsdb?autoReconnect=true&useSSL=false", "snsuser", "password");
			
			System.out.println(getTime() + "connected to MySQL.");
			
			String uname = "bokor";
			
			Statement isVisitor = conn.createStatement();
			ResultSet res = isVisitor.executeQuery("SELECT uid,type FROM users WHERE uname =" + "'" + uname + "'");

			// assume that
			// residents are already added to USERS
			// and have enough status for Read or Write operations
			
			if (res.next()) {
				
				// check the type and update the type from 01 (R) to 11 (RV) if needed
				
				// read randomly selected resident's status
				
			} else {
				// add his/her to USERS as 10 (VISITOR)
				
				// read randomly selected resident
			}
			
			
			res.close();
			conn.close();
		} catch (SQLException e) {
			System.out.println("SQLException: " + e.getMessage());
		} finally {
			System.out.println("MySQL has handled the query.");
		}
		
		return "";
	}

	private String writeReply(JSONObject msg) {

		try {
			Connection conn = DriverManager.getConnection(
					"jdbc:mysql://localhost/snsdb?autoReconnect=true&useSSL=false", "snsuser", "password");
			
			System.out.println(getTime() + "connected to MySQL.");
			
			String uname = "bokor";
			
			Statement isVisitor = conn.createStatement();
			ResultSet res = isVisitor.executeQuery("SELECT uid,type FROM users WHERE uname =" + "'" + uname + "'");

			// assume that
			// residents are already added to USERS
			// and have enough status for Read or Write operations
			
			if (res.next()) {
				
				// check the type and update the type from 01 (R) to 11 (RV) if needed
				
				// write his/her reply to randomly selected resident's status
				
			} else {
				// add his/her to USERS as 10 (VISITOR)
				
				// write his/her reply to randomly selected resident
			}
						
			res.close();
			conn.close();			
		} catch (SQLException e) {
			System.out.println("SQLException: " + e.getMessage());
		} finally {
			System.out.println("MySQL has handled the query.");
		}
		
		return "";
	}
	
	private String writeRetweet(JSONObject msg) {
		
		try {
			Connection conn = DriverManager.getConnection(
					"jdbc:mysql://localhost/snsdb?autoReconnect=true&useSSL=false", "snsuser", "password");
			
			System.out.println(getTime() + "connected to MySQL.");
			
			String uname = "bokor";
			
			Statement isVisitor = conn.createStatement();
			ResultSet res = isVisitor.executeQuery("SELECT uid,type FROM users WHERE uname =" + "'" + uname + "'");

			// assume that
			// residents are already added to USERS
			// and have enough status for Read or Write operations
			
			if (res.next()) {
				
				// check the type and update the type from 01 (R) to 11 (RV) if needed
				
				// read randomly selected resident's status
				
				// do writeStatus				
			} else {
				// add his/her to USERS as 10 (VISITOR)
				
				// read randomly selected resident
				
				// do writeStatus
			}
						
			res.close();
			conn.close();			
		} catch (SQLException e) {
			System.out.println("SQLException: " + e.getMessage());
		} finally {
			System.out.println("MySQL has handled the query.");
		}
		
		return "";
	}
	
	private void doReplacement() {
		
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