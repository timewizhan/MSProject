import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import Utility.CoordHandler;
import Wrapper.coordInfo;

public class ServiceServer {
	private int mServerPort = 7777;
	private final static int mMaxCon = 2147483647;
	
	private ServerSocket mServerSocket;
	private ExecutorService mThreadPool = Executors.newFixedThreadPool(800);
//	private ExecutorService mThreadPool = Executors.newCachedThreadPool();
	
	public static coordInfo mCoord;
	public static ScheduledExecutorService mScheduler;	
	
	public static ArrayList<Double> mCPU_Log;
	public static ArrayList<Double> mAVG_CPU_Log;
	
	public static void main(String[] args) throws InterruptedException, IOException {																		
		// disable c3p0 logging
		System.setProperty("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "WARNING");
		System.setProperty("com.mchange.v2.log.MLog", "com.mchange.v2.log.FallbackMLog");				
						
		// create server
		ServiceServer server = new ServiceServer(CoordHandler.setLocation());
		server.listenSocket();
	}
	
	public ServiceServer(String loc) throws IOException {		
		mCoord = new coordInfo();		
		CoordHandler.readCoord(mCoord, loc);
		
		mCPU_Log = new ArrayList<Double>();
		mAVG_CPU_Log = new ArrayList<Double>();
	}
	
	private void listenSocket() {
		openServerSocket();		
		
		CpuMonitor.startCpuMonitor();
		
		while(true) {
			try {
				WorkerRunnable work = new WorkerRunnable(mServerSocket.accept());
				this.mThreadPool.execute(work);
				//Thread t = new Thread(work);
				//t.start();
			} catch (IOException e) {
				e.printStackTrace();
			}		
		}
	}
	
	private void openServerSocket() {
		try {
			this.mServerSocket = new ServerSocket(mServerPort, mMaxCon);
			this.mServerSocket.setReuseAddress(true);			
		} catch (IOException e) {
			System.out.println("[openServerSocket]e: " + e.getMessage());
		}
		System.out.println(mCoord.getServerLoc() + " has started.");
	}
}