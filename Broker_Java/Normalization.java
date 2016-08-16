import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Normalization {

	public Normalization(){

	}

	public void normalizeFactors(){

		System.out.println("normalization start");

		Set<Future<Integer>> set = new HashSet<Future<Integer>>();

		//set to normalize server traffic 
		Callable<Integer> callableNormTraffic = new CNormalizeTraffic();
		Future<Integer> futureNormTraffic = ThreadPool.GetInstance().submit(callableNormTraffic);
		set.add(futureNormTraffic);
		
		//set to normalize distance
		Callable<Integer> callableNormDist = new CNormalizeDistance();
		Future<Integer> futureNormDist = ThreadPool.GetInstance().submit(callableNormDist);
		set.add(futureNormDist);
		
		//set to normalize social level
		Callable<Integer> callableNormSocialLvl = new CNormalizeSocialLvl();
		Future<Integer> futureNormSocialLvl = ThreadPool.GetInstance().submit(callableNormSocialLvl);
		set.add(futureNormSocialLvl);
		
		//set to normalize cost
		Callable<Integer> callableNormCost = new CNormalizeCost();
		Future<Integer> futureNormCost = ThreadPool.GetInstance().submit(callableNormCost);
		set.add(futureNormCost);
		
		
		for (Future<Integer> future : set) {
		
			try {
				future.get();
			
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void normalizeServerTraffic(){

	}

	private void normalizeDistance(){

	}

	private void normalizeSocialLevel(){

	}

	private void normalizeCost(){

	}
}
