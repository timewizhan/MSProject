
public class CEntryPoint {
	
	public static void main(String [] args){
		
		System.out.println("EntryPoint Start");
		
		CNetworkClient cEP = new CNetworkClient();
		cEP.start();
	}
}
