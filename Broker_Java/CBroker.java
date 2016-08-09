
public class CBroker {
	
	public static void main(String [] args){
		
		System.out.println("Broker Start");
		
		ThreadPool.GetInstance().execute(new CLPCalculation());
		
		CNetworkServer cBroker = new CNetworkServer(); 
		cBroker.start();
	}
}
