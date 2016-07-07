import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.dbcp2.BasicDataSource;

import Utility.CoordHandler;
import Wrapper.coordInfo;

/**
 * ServiceServer.java
 * Initialize the service server,
 * accept the client's connection and give the thread it to handle the request
 */
public class ServiceServer {
	
	private final int mServerPort = 7777;
	private final int mMaxCon = 2147483647;		
	private ServerSocket mServerSocket;
	
	private int mNumThread;		
	private ExecutorService mThreadPool;
		
	public static BasicDataSource mBDS;
		
	public static volatile coordInfo mCoord;			
	
	
	/**
	 * main.
	 * Starts the service server
	 * 
	 * @param None
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @param: None
	 * @return: None
	 */
	public static void main(String[] args) throws IOException {						
		// create server
		ServiceServer server = new ServiceServer(CoordHandler.setLocation());
		server.initializeDBCP();
		server.listenSocket();		
	}
	
	/**
	 * Class constructor 
	 * Loads the coordinate data
	 * and sets the threadpool
	 * 
	 * @param loc the location name of the service server
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public ServiceServer(String loc) throws IOException {		
		mCoord = new coordInfo();
		CoordHandler.readCoord(mCoord, loc);
		
		int processors = Runtime.getRuntime().availableProcessors();
		mNumThread = processors * 2;
		mThreadPool = Executors.newFixedThreadPool(mNumThread);				
	}
	
	/**
	 * Initializes the DBCP setting
	 */
	private void initializeDBCP() {
		mBDS = new BasicDataSource();
		mBDS.setDriverClassName("com.mysql.jdbc.Driver");
		mBDS.setUsername("root");
		mBDS.setPassword("cclabj0gg00");
		mBDS.setUrl("jdbc:mysql://localhost:3306/snsdb?autoReconnect=true&useSSL=false");
		
		// optional
		// can be worked with defaults		
		int sizeDBCP = mNumThread * 2;
		long oneMin = 1000 * 60;
		
		mBDS.setInitialSize(sizeDBCP);
		mBDS.setMinIdle(sizeDBCP);
		mBDS.setMaxTotal(sizeDBCP);
		mBDS.setMaxIdle(sizeDBCP);
		
		mBDS.setValidationQuery("SELECT 1");
		
		mBDS.setTestOnBorrow(false);
		mBDS.setTestOnReturn(false);
		mBDS.setTestWhileIdle(true);
		mBDS.setTimeBetweenEvictionRunsMillis(oneMin * 20);
		mBDS.setNumTestsPerEvictionRun(sizeDBCP);
		mBDS.setMinEvictableIdleTimeMillis(oneMin * 30);				
	}
	
	/**
	 * Accepts the socket
	 * and passes it to the WorkerRunnable
	 * and executes the thread
	 * 
	 */
	private void listenSocket() {
		openServerSocket();				
		System.out.println("Service Server is initiallized");
		while(true) {
			try {		
				Socket accepted = mServerSocket.accept();
				WorkerRunnable work = new WorkerRunnable(accepted);												
				mThreadPool.execute(work);				
			} catch (IOException e) {
				System.out.println("[IOException]: " + e.getMessage());
			}		
		}
	}
	
	/**
	 * Opens server socket
	 */
	private void openServerSocket() {
		try {
			mServerSocket = new ServerSocket(mServerPort, mMaxCon);
			mServerSocket.setReuseAddress(true);			
		} catch (IOException e) {
			System.out.println("[openServerSocket]: " + e.getMessage());
		}
		System.out.println(mCoord.getServerLoc() + " has started.");
	}            
}