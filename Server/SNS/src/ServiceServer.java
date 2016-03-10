import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import Wrapper.coordInfo;

public class ServiceServer {
	private final static int SOMAXCONN = 2147483647;
	
	private coordInfo mCoord;
	
	private ScheduledExecutorService mScheduler;	
	private ArrayList<Double> mCPU_Log;
	private ArrayList<Double> mAVG_CPU_Log;
			
	protected int mServerPort = 7777;
	protected ServerSocket mServerSocket = null;
	protected boolean mStopped = false;
	protected Thread mRunningThread = null;
	protected ExecutorService mThreadPool = Executors.newFixedThreadPool(10);
	
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
	
	public void listenSocket() {
		openServerSocket();
		
		while(true) {
			try {
				this.mThreadPool.execute(new WorkerRunnable(mServerSocket.accept()));				
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		System.out.println("Handled!");
		}
	}
	
	private void openServerSocket() {
		try {
			this.mServerSocket = new ServerSocket(mServerPort, SOMAXCONN);
		} catch (IOException e) {
			System.out.println("[openServerSocket]e: " + e.getMessage());
		}
	}

	protected void finalize() {
		try {
			mServerSocket.close();			
		} catch (IOException e) {
			System.out.println("[finalize]e: " + e.getMessage());
		}
    }
	
	private void startCpuMonitor() throws InterruptedException {
		mScheduler = Executors.newSingleThreadScheduledExecutor();
		Utility.monitorCpuLoad(mScheduler, mCPU_Log, mAVG_CPU_Log);
	}
}