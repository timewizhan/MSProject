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

import Wrapper.coordInfo;
import Wrapper.userInfo;

public class ServiceServer implements Runnable {
	private final static int SOMAXCONN = 2147483647;
	
	private final static int mTweet = 1;
	private final static int mRead = 2;
	private final static int mReply = 3;
	private final static int mRetweet = 4;
	private final static int mReplacement = 5;
	
	private final static int mResident = 1;
	private final static int mVisitor = 2;
	
	private final static int mNumRead = 10;
	
	private coordInfo mCoord;
	
	private ScheduledExecutorService mScheduler;	
	private ArrayList<Double> mCPU_Log;
	private ArrayList<Double> mAVG_CPU_Log;
		
	ServerSocket mServerSocket;
	Thread[] mThreadArr;
	
	public static void main(String[] args) throws InterruptedException {																		
		// disable c3p0 logging
		System.setProperty("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "WARNING");
		System.setProperty("com.mchange.v2.log.MLog", "com.mchange.v2.log.FallbackMLog");				
		
		// create server threads
		ServiceServer server = new ServiceServer(10, Utility.setLocation());
		server.start();
		server.startCpuMonitor();
	}
	
	public ServiceServer(int num, String loc) {								
		mCoord = new coordInfo();		
		Utility.readCoord(mCoord, loc);
		
		mCPU_Log = new ArrayList<Double>();
		mAVG_CPU_Log = new ArrayList<Double>();
				
		try {			
			// create a server socket binded with 7777 port
			// set # backlog as Maximum
			mServerSocket = new ServerSocket(7777, SOMAXCONN);
			System.out.println(getTime() + " SNS Server is ready.");
						
			mThreadArr = new Thread[num];			
		} catch (IOException e) {
			System.out.println("[ServiceServer]IOException e: " + e.getMessage());
		}	
	}
	
	private void start() {
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
				System.out.print(getTime() + " received a request from ");														 			
									
				JSONObject request  = Utility.msgParser(socket);
				
				System.out.println("[" + (String) request.get("SRC") + "]");
				
				// check the request type
				
				// For the service
				// need to figure out whether the request is about the service or not
				// and do proper operation according to the request
				// there should be an additional method to handle the data replacement
				String response = Utility.msgGenerator(operationHandler(request));								
												
				out = new BufferedWriter(new OutputStreamWriter(
						socket.getOutputStream(), "UTF-8"));			
								
				Thread.sleep(Utility.calRTT(mCoord, (String) request.get("LOC")));
				
				// For the monitoring result report
				// need an additional msgGenerator
				
				// For the data replacement
				// need an additional msgGenerator
					// sending data
					// receiving data
				
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
		
		String src = (String) request.get("SRC");		
		String dst = (String) request.get("DST");
		String loc = (String) request.get("LOC");
		String msg = (String) request.get("MSG");
		
		switch (reqType) {                                                                                                                                                                                                      
		case mTweet:				
			uid = DBConnection.isThere(src, mResident, loc);			
			res = DBConnection.writeStatus(uid, msg, reqSize);			
			break;
		case mRead:
			uid = DBConnection.isThere(src, mVisitor, loc);
			res = DBConnection.readStatus(uid, dst, reqSize, mNumRead);
			break; 
		case mReply:
			uid = DBConnection.isThere(src, mVisitor, loc);			
			res = DBConnection.writeReply(uid, dst, msg, reqSize, mNumRead);
			break;
		case mRetweet:
			uid = DBConnection.isThere(src, mVisitor, loc);
			res = DBConnection.readStatus(uid, dst, reqSize, mNumRead);
			break;
		case mReplacement:
			Utility.stopScheduler(mScheduler);
			double total_cpu = 0;
			for (int i = 0; i < mCPU_Log.size(); i++) {
				System.out.println("CPU USAGE LOG: " + mCPU_Log.get(i));
				total_cpu = total_cpu + mCPU_Log.get(i);
			}
			System.out.println("Total: " + total_cpu + "(" + mCPU_Log.size() +")" + " " + "Average: " + total_cpu / mCPU_Log.size());
			mCPU_Log.clear();
			
			this.startCpuMonitor();
			userInfo[] result = DBConnection.getMonitor();
			
			System.out.println(result.length);
			
			for (int i = 0; i < result.length; i++)
				System.out.println(result[i].getName() + " " + result[i].getTraffic() + " " + result[i].getLoc());
			
			break;													
		}
		return res;
	}

	private String getTime() {
		String name = Thread.currentThread().getName();
		SimpleDateFormat f = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss.SSS]");
		return f.format(new Date()) + name;
	}
	
	private void startCpuMonitor() throws InterruptedException {
		mScheduler = Executors.newSingleThreadScheduledExecutor();
		Utility.monitorCpuLoad(mScheduler, mCPU_Log, mAVG_CPU_Log);
	}
}