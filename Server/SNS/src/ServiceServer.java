import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import Wrapper.coordInfo;

public class ServiceServer {
	private int mServerPort = 7777;
	private final static int mMaxCon = 2147483647;
	
	private ServerSocket mServerSocket = null;	
	private ExecutorService mThreadPool = Executors.newFixedThreadPool(10);
	
	private coordInfo mCoord;
	
	private ScheduledExecutorService mScheduler;	
	private ArrayList<Double> mCPU_Log;
	private ArrayList<Double> mAVG_CPU_Log;
	
	public static void main(String[] args) throws InterruptedException, IOException {																		
		// disable c3p0 logging
		System.setProperty("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "WARNING");
		System.setProperty("com.mchange.v2.log.MLog", "com.mchange.v2.log.FallbackMLog");				
		
		// create server
		ServiceServer server = new ServiceServer(Utility.setLocation());
		server.listenSocket();
	}
	
	public ServiceServer(String loc) throws IOException {								
		mCoord = new coordInfo();		
		Utility.readCoord(mCoord, loc);
		
		mCPU_Log = new ArrayList<Double>();
		mAVG_CPU_Log = new ArrayList<Double>();	
	}
	
	private void listenSocket() {
		openServerSocket();		
		startCpuMonitor();
		
		while(true) {
			try {
				WorkerRunnable work = new WorkerRunnable(mServerSocket.accept(), mCoord);
				this.mThreadPool.execute(work);				
			} catch (IOException e) {
				e.printStackTrace();
			}		
		}
	}
	
	private void openServerSocket() {
		try {
			this.mServerSocket = new ServerSocket(mServerPort, mMaxCon);
		} catch (IOException e) {
			System.out.println("[openServerSocket]e: " + e.getMessage());
		}
	}	
	
	private void startCpuMonitor() {
		mScheduler = Executors.newSingleThreadScheduledExecutor();
		try {
			Utility.monitorCpuLoad(mScheduler, mCPU_Log, mAVG_CPU_Log);
		} catch (InterruptedException e) {
			System.out.println("[startCpuMonitor]e: " + e.getMessage());
		}
	}
	
	protected void finalize() {
		try {
			mServerSocket.close();			
		} catch (IOException e) {
			System.out.println("[finalize]e: " + e.getMessage());
		}
    }
}