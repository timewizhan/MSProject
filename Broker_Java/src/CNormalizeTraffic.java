
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

public class CNormalizeTraffic implements Callable {

	static Logger log = Logger.getLogger(CBroker.class.getName());		//initiate logger
	
	@Override
	public Object call() throws Exception {
		// TODO Auto-generated method stub
		
		log.debug("Normalization of Server Traffic Start");
		
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
		
		log.debug("Normalization of Server Traffic Was Done");

		return null;
	}

}
