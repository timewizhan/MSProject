import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPool {

	private static final int THREAD_CNT = 20;
	private static ExecutorService threadPool;

	private ThreadPool () {}
	
	public static synchronized ExecutorService GetInstance () {
		if (threadPool == null)
			threadPool = Executors.newFixedThreadPool(THREAD_CNT);
		
		return threadPool;
	}
}
