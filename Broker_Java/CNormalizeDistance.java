
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
		ArrayList<ServerInfo> alServerInfo = databaseInstance.getServerInfo();	//여기서, 연결되어있는 서버의 IP addr와 location을 가져온다
		
		Iterator it = alClientData.iterator();
		while(it.hasNext()){
			
			ClientData clientData = (ClientData) it.next();
			double [] distances  = new double [alServerInfo.size()];
			
			for(int i=0; i<alServerInfo.size(); i++){
				
				double dist = IDistanceCalculation.calculateDistance(clientData.getUserLocation(), alServerInfo.get(i).getServerLocation());
				distances[alServerInfo.get(i).getEpNo()-1] = dist;
				
				System.out.println("user: " + clientData.getUserLocation() + ", server location: " + alServerInfo.get(i).getServerLocation()
						+ ", ep no.:" + alServerInfo.get(i).getEpNo());
				System.out.println("current distance array contents: [" + distances[0] + ", " + distances[1] + ", " + distances[2] +"]");
				
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
