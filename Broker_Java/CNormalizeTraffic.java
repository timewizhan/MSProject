
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Callable;

public class CNormalizeTraffic implements Callable {

	@Override
	public Object call() throws Exception {
		// TODO Auto-generated method stub
		System.out.println("normalization of server traffic start");

		CDatabase databaseInstance = new CDatabase();
		databaseInstance.connectBrokerDatabase();
		ArrayList<ServerData> alServerData = databaseInstance.extractServerData();
		
		Iterator it = alServerData.iterator();
		while(it.hasNext()){
			ServerData serverData = (ServerData) it.next();
		
			double normalizedValue = 0.0;
			normalizedValue = iNormFormula.normalize(serverData.getServerTraffic(), ServerData.maxServerTraffic, ServerData.minServerTraffic);
			
			databaseInstance.insertNormServerData(serverData.getEpAddr(), normalizedValue);
		}
		
		System.out.println("normalization of server traffic was done");
		return null;
	}

}
