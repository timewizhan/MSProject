import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CBroker {
	
	private static final int THREAD_CNT 		= 10;
	private static ExecutorService threadPool 	= Executors.newFixedThreadPool(THREAD_CNT);
	
	public static void main(String [] args){
		
		System.out.println("Broker Start");
		
		threadPool.execute(new CLPCalculation());
		
		CNetworkServer cBroker = new CNetworkServer(); 
		cBroker.start(threadPool);

	}
}
