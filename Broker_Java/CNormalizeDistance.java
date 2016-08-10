
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Callable;

public class CNormalizeDistance implements Callable{

	@Override
	public Object call() throws Exception {
		// TODO Auto-generated method stub
		System.out.println("normalization of distance start");

		CDatabase databaseInstance = new CDatabase();
		databaseInstance.connectBrokerDatabase();
		ArrayList<ClientData> alClientData = databaseInstance.extractClientData();
		ArrayList<ServerInfo> alServerInfo = databaseInstance.getServerInfo();
		
		Iterator it = alClientData.iterator();
		while(it.hasNext()){
			
			ClientData clientData = (ClientData) it.next();
			double [] distances  = new double [alServerInfo.size()];
			
			for(int i=0; i<alServerInfo.size(); i++){
				
				//locationIP 테이블 업데이트: ip addr와 ep num 매칭 시킬 수 있게
				databaseInstance.updateLocationIpTable(alServerInfo.get(i).getEpAddr(), i+1);
				
				double dist = IDistanceCalculation.calculateDistance(clientData.getUserLocation(), alServerInfo.get(i).getServerLocation());
				distances[i] = dist;
				
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
			
			double [] normalizedDistances = new double [alServerInfo.size()];
			for(int i=0; i<alServerInfo.size(); i++){
				
				double normalizedValue = 0.0;
				normalizedValue = iNormFormula.normalize(distances[i], ClientData.maxDistance, ClientData.minDistance);
				normalizedDistances[i] = normalizedValue;
			}
			
			databaseInstance.insertNormDistanceData(clientData.getUserID(), normalizedDistances);
		}
		
		System.out.println("normalization of distance was done");
		databaseInstance.disconnectBrokerDatabase();
		return null;
	}

}
