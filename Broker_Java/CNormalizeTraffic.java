
import java.util.concurrent.Callable;

public class CNormalizeTraffic implements Callable {

	@Override
	public Object call() throws Exception {
		// TODO Auto-generated method stub
		Thread.sleep(10000);
		System.out.println("normalization of server traffic was done");
		return null;
	}

}
