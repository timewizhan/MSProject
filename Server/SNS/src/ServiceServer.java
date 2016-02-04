import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

interface ReqType {
	int TWEET = 1, READ = 2, REPLY = 3, RETWEET = 4, REPLACEMENT = 5;
}

public class ServiceServer implements Runnable {
	ServerSocket mServerSocket;
	Thread[] mThreadArr;
		
	private final static int mResident = 1;
	private final static int mVisitor = 2;
	private final static int mNumRead = 5;
	private final static int mNumRand = 10;
	
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

	@Override
	public void run() {
		while (true) {			
			Socket socket = null;			
			BufferedWriter out = null;
			
			try {						
				System.out.println(getTime() + " is waiting for requests.");				
				
				socket = mServerSocket.accept();
				System.out.println(getTime() + " received a request from " 
						+ socket.getInetAddress());														 			
															
				String response = msgGenerator(operationHandler(msgParser(socket)));								
								
				
				out = new BufferedWriter(new OutputStreamWriter(
						socket.getOutputStream(), "UTF-8"));				
							
				Thread.sleep(calRTT());
			
				out.write(response);
				out.newLine();
				out.flush();																	
			} catch (IOException e) {
				System.out.println("[run]IOException: " + e.getMessage());
			} catch (PropertyVetoException e) {
				System.out.println("[run]PropertyVetoException: " + e.getMessage());
			} catch (SQLException e) {
				System.out.println("[run]SQLException: " + e.getMessage());
			} catch (InterruptedException e) {
				System.out.println("[run]InterruptException: " + e.getMessage());
			} catch (ParseException e) {
				System.out.println("[run]ParseException: " + e.getMessage());
			} finally {
				if (socket != null)
					try {
						socket.close();
					} catch (IOException e) {
						System.out.println("[run/socket]IOException: " + e.getMessage());
					}
								
				System.out.println(getTime() + " has handled the request.");
			}						
		}		
	}
		
	private String operationHandler(JSONObject request) throws PropertyVetoException, SQLException, IOException {		
		int uid = -1;
		int reqSize = request.toString().length();
		String res = null;		
						
		int reqType = Integer.parseInt((String) request.get("TYPE"));	
		String src = (String) request.get("SENDER");		
		String dst = (String) request.get("RECEIVER");
		String loc = (String) request.get("LOCATION");
		String msg = (String) request.get("MSG");		 
		
		switch (reqType) {                                                                                                                                                                                                      
		case ReqType.TWEET:			
			uid = DBConnection.isThere(src, mResident, loc);			
			res = DBConnection.writeStatus(uid, msg, reqSize);
			break;
		case ReqType.READ:
			uid = DBConnection.isThere(src, mVisitor, loc);
			res = DBConnection.readStatus(uid, dst, reqSize, mNumRead);
			break;
		case ReqType.REPLY:
			DBConnection.getMonitor();
			uid = DBConnection.isThere(src, mVisitor, loc);			
			res = DBConnection.writeReply(uid, dst, msg, reqSize, mNumRand);
			break;
		case ReqType.RETWEET:
			uid = DBConnection.isThere(src, mVisitor, loc);
			res = DBConnection.readStatus(uid, dst, reqSize, mNumRand);
			break;
		case ReqType.REPLACEMENT:
			// do data replacement
			break;													
		}
		
		return res;
	}

	private JSONObject msgParser(Socket socket) throws UnsupportedEncodingException, IOException, ParseException {		
		String result = "";	
		BufferedReader input = null;
		JSONObject msg = null;
		
		try {
			input = new BufferedReader(new InputStreamReader(
					socket.getInputStream(), "UTF-8"));
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
	private String msgGenerator(String result) {
		JSONObject response = new JSONObject();
		response.put("RESPONSE", result);
		
		return response.toString();
	}
	
	private int calRTT() {
		
		return 0;
	}
	
	private String getTime() {
		String name = Thread.currentThread().getName();
		SimpleDateFormat f = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]");
		return f.format(new Date()) + name;
	}	
}