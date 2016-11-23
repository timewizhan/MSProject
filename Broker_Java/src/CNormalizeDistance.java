
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

public class CNormalizeDistance implements Callable{

	static Logger log = Logger.getLogger(CBroker.class.getName());	
	
	@Override
	public Object call() throws Exception {
		// TODO Auto-generated method stub\
		log.debug("Normalization of Distance Start");
		
		CDatabase databaseInstance = new CDatabase();
		databaseInstance.connectBrokerDatabase();
		ArrayList<ClientData> alClientData = databaseInstance.extractClientData();
		ArrayList<ServerInfo> alServerInfo = databaseInstance.getServerInfo();	//여기서, 연결되어있는 서버의 IP addr와 location을 가져온다
		
		Iterator it = alClientData.iterator();
		while(it.hasNext()){
			
			ClientData clientData = (ClientData) it.next();
			double [] distances  = new double [alServerInfo.size()];
			
			for(int i=0; i<alServerInfo.size(); i++){
				
				double dist = IDistanceCalculation.calculateDistance(clientData.getUserLocation(), alServerInfo.get(i).getServerLocation());
				distances[alServerInfo.get(i).getEpNo()-1] = dist;
				
				//min, max 찾기
				if(i==0){
					ClientData.minDistance = dist;
					ClientData.maxDistance = dist;
					
				} else {
					if(ClientData.minDistance >= dist)
						ClientData.minDistance = dist;
					
					if(ClientData.maxDistance <= dist)
						ClientData.maxDistance = dist;
				}
			}
		//	log.debug("	* Calculate Distance: user id - " + clientData.getUserID() + "("+ clientData.getUserLocation() +"), EP1 - " + distances[0] + ", EP2 - " + distances[1] + ", EP3 - " + distances[2]);
			
			double [] normalizedDistances = new double [CBroker.NUM_OF_EP];
			for(int i=0; i<alServerInfo.size(); i++){
				
				double normalizedValue = 0.0;
				normalizedValue = iNormFormula.normalize(distances[i], ClientData.maxDistance, ClientData.minDistance);
				normalizedDistances[i] = normalizedValue;
			}
			
			databaseInstance.insertNormDistanceData(clientData.getUserID(), normalizedDistances);
		}
		
		log.debug("Normalization of Distance Was Done");
		
		databaseInstance.disconnectBrokerDatabase();
		
		return null;
	}

}
