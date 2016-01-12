import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
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
						res = writeStatus(msg, socket);
						break;
					case Type.READ:
						res = readStatus(msg, socket);
						break;
					case Type.REPLY:
						res = writeReply(msg, socket);
						break;
					case Type.RETWEET:
						res= writeRetweet(msg, socket);
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
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			} 
		}
	}

	private String writeStatus(JSONObject msg, Socket socket) {
		// write the status
		
		return "";		
	}

	private String readStatus(JSONObject msg, Socket socket) {
		
		return "";
	}

	private String writeReply(JSONObject msg, Socket socket) {
				
		return "";
	}
	
	private String writeRetweet(JSONObject msg, Socket socket) {
				
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