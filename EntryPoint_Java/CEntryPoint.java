
public class CEntryPoint {
	
	public static void main(String [] args){
		
		System.out.println("EntryPoint Start");
		
		CNetworkClient cEP = new CNetworkClient();
		int keepGoing = cEP.start();
		
		if(keepGoing==1){
			
		}else if(keepGoing==0){
			
		}
	}
}
