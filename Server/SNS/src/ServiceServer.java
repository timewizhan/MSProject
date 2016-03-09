import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import Wrapper.coordInfo;

public class ServiceServer implements Runnable {
	private final static int SOMAXCONN = 2147483647;
	
	private coordInfo mCoord;
	
	private ScheduledExecutorService mScheduler;	
	private ArrayList<Double> mCPU_Log;
	private ArrayList<Double> mAVG_CPU_Log;
		
	ServerSocket mServerSocket2;
	Thread[] mThreadArr;
	
	protected int mServerPort = 7777;
	protected ServerSocket mServerSocket = null;
	protected boolean mStopped = false;
	protected Thread mRunningThread = null;
	
	public static void main(String[] args) throws InterruptedException, IOException {																		
		// disable c3p0 logging
		System.setProperty("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "WARNING");
		System.setProperty("com.mchange.v2.log.MLog", "com.mchange.v2.log.FallbackMLog");				
		
		// create server
		ServiceServer server = new ServiceServer(0, Utility.setLocation());
		new Thread(server).start();
	}
	
	public ServiceServer(int num, String loc) throws IOException {								
		mCoord = new coordInfo();		
		Utility.readCoord(mCoord, loc);
					
		mCPU_Log = new ArrayList<Double>();
		mAVG_CPU_Log = new ArrayList<Double>();	
	}

	@Override
	public void run() {
		synchronized(this) {
			this.mRunningThread = Thread.currentThread();
		}
		
		openServerSocket();
		
		while(! isStopped()) {
			Socket clientSocket = null;
			try {
				clientSocket = this.mServerSocket.accept();
			} catch (IOException e) {
				if(isStopped()) {
					System.out.println("Server Stopped.");
					return;
				}
				System.out.println("[run]e: " + e.getMessage());
			}			
			new Thread(new WorkerRunnable(clientSocket)).start();
			System.out.println("Server Stopped.");
		}
	}
		
	private synchronized boolean isStopped() {
		return this.mStopped;
	}
	
	public synchronized void stop() {
		this.mStopped = true;
		try {
			this.mServerSocket.close();
		} catch(IOException e) {
			System.out.println("[stop]e: " + e.getMessage());
		}
	}
	
	private void openServerSocket() {
		try {
			this.mServerSocket = new ServerSocket(mServerPort, SOMAXCONN);
		} catch (IOException e) {
			System.out.println("[openServerSocket]e: " + e.getMessage());
		}
	}
	
	private void startCpuMonitor() throws InterruptedException {
		mScheduler = Executors.newSingleThreadScheduledExecutor();
		Utility.monitorCpuLoad(mScheduler, mCPU_Log, mAVG_CPU_Log);
	}
}