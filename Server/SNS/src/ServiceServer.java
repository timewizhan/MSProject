import java.beans.PropertyVetoException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

interface ReqType {
	int TWEET = 1, READ = 2, REPLY = 3, RETWEET = 4, REPLACEMENT = 5;
}

public class ServiceServer implements Runnable {
	ServerSocket mServerSocket;
	Thread[] mThreadArr;
	
	private final static String mLocation = "KR";
	
	private final static int mResident = 1;
	private final static int mVisitor = 2;
	
	private final static int mNumRead = 10;
	
	public static ArrayList<Double> mCPU_Log;	
	
	public static ScheduledExecutorService mScheduler;
	
	public static void main(String[] args) throws InterruptedException {								
		// create server threads
		ServiceServer server = new ServiceServer(4);
		server.start();
		
		// monitor cpu load			
		server.startCpuMonitor();				
	}
	
	public ServiceServer(int num) {		
		mCPU_Log = new ArrayList<>();
		
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
															
				String response = Utility.msgGenerator(operationHandler(Utility.msgParser(socket)));								
												
				out = new BufferedWriter(new OutputStreamWriter(
						socket.getOutputStream(), "UTF-8"));				
						
				// can be replaced by java.util.Timer and java.util.TimerTask classes
				Thread.sleep(Utility.calRTT("KR"));
			
				out.write(response);
				out.newLine();
				out.flush();																	
			} catch (IOException e) {
				System.out.println("[run]IOException: " + e.getMessage());
			} catch (PropertyVetoException e) {
				System.out.println("[run]PropertyVetoException: " + e.getMessage());
			} catch (SQLException e) {
				System.out.println("[run]SQLException: " + e.getMessage()); 
			} catch (ParseException e) {
				System.out.println("[run]ParseException: " + e.getMessage());
			} catch (InterruptedException e) {
				System.out.println("[run]InterruptedException: " + e.getMessage());
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
		
	private int operationHandler(JSONObject request) throws PropertyVetoException, SQLException, IOException, InterruptedException {		
		int uid = -1;
		int reqSize = request.toString().length();
		int res = 0;		
						
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
			uid = DBConnection.isThere(src, mVisitor, loc);			
			res = DBConnection.writeReply(uid, dst, msg, reqSize, mNumRead);
			break;
		case ReqType.RETWEET:
			uid = DBConnection.isThere(src, mVisitor, loc);
			res = DBConnection.readStatus(uid, dst, reqSize, mNumRead);
			break;
		case ReqType.REPLACEMENT:
			Utility.stopScheduler(mScheduler);
			double total_cpu = 0;
			for (int i = 0; i < mCPU_Log.size(); i++) {
				System.out.println("CPU USAGE LOG: " + mCPU_Log.get(i));
				total_cpu = total_cpu + mCPU_Log.get(i);
			}
			System.out.println("Total: " + total_cpu + "(" + mCPU_Log.size() +")" + " " + "Average: " + total_cpu / mCPU_Log.size());
			mCPU_Log.clear();
			
			this.startCpuMonitor();
			break;													
		}
		return res;
	}

	private String getTime() {
		String name = Thread.currentThread().getName();
		SimpleDateFormat f = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]");
		return f.format(new Date()) + name;
	}
	
	private void startCpuMonitor() throws InterruptedException {
		mScheduler = Executors.newSingleThreadScheduledExecutor();
		Utility.monitorCpuLoad(mScheduler, mCPU_Log);
	}
}