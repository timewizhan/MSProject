
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
		ArrayList<ServerInfo> alServerInfo = databaseInstance.getServerInfo();	//���⼭, ����Ǿ��ִ� ������ IP addr�� location�� �����´�
		
		Iterator it = alClientData.iterator();
		while(it.hasNext()){
			
			ClientData clientData = (ClientData) it.next();
			double [] distances  = new double [alServerInfo.size()];
			
			for(int i=0; i<alServerInfo.size(); i++){
				
				double dist = IDistanceCalculation.calculateDistance(clientData.getUserLocation(), alServerInfo.get(i).getServerLocation());
				distances[alServerInfo.get(i).getEpNo()-1] = dist;
				
			//	log.debug(" 	* user id:" + clientData.getUserID() + ", user location: " + clientData.getUserLocation() + ", server location: " + alServerInfo.get(i).getServerLocation()
			//			+ ", ep no.:" + alServerInfo.get(i).getEpNo());
				
				
				
				//min, max ã��
				if(i==0){
					ClientData.minDistance = dist;
					ClientData.maxDistance = dist;
					
				} else {
					if(ClientData.minDistance >= dist)
						ClientData.minDistance = dist;
					
					if(ClientData.maxDistance <= dist)
						ClientData.maxDistance = dist;
				}
				
			//	log.debug("	* current distance array contents: [" + distances[0] + ", " + distances[1] + ", " + distances[2] +"]");
			}
			
			double [] normalizedDistances = new double [alServerInfo.size()];
			for(int i=0; i<alServerInfo.size(); i++){
				
				double normalizedValue = 0.0;
				normalizedValue = iNormFormula.normalize(distances[i], ClientData.maxDistance, ClientData.minDistance);
				normalizedDistances[i] = normalizedValue;
			}
			
		//	log.debug("	* current distance array contents (normalized): [" + normalizedDistances[0] + ", " + normalizedDistances[1] + ", " + normalizedDistances[2] +"] \r\n");
			databaseInstance.insertNormDistanceData(clientData.getUserID(), normalizedDistances);
		}
		
		log.debug("Normalization of Distance Was Done");
		
		databaseInstance.disconnectBrokerDatabase();
		
		return null;
	}

}
