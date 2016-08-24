import java.util.ArrayList;

public class ServerStatus {

	private int currentTraffic;
	private int expectedTraffic;
	private int maximumTraffic;
	private String serverIp;
	private int epNo;
	
	public int getCurrentTraffic() {
		return currentTraffic;
	}
	public void setCurrentTraffic(int currentTraffic) {
		this.currentTraffic = currentTraffic;
	}
	public int getExpectedTraffic() {
		return expectedTraffic;
	}
	/*
	public void setExpectedTraffic(int expectedTraffic) {
		this.expectedTraffic = expectedTraffic;
	}
	*/
	public void setExpectedTraffic(int epNo, ArrayList<ClientTrafficData> clientTrafficData) {
		
		//현재 토탈 서버 트래픽
		double currTotalTraffic = (double)getCurrentTotalCloudsTraffic();
				
		//previous 토탈 서버 트래픽
		double prevTotalTraffic = (double)getPreviousTotalCloudsTraffic(currTotalTraffic);
		
		//현재 예상되는 각 사용자들의 트래픽을 구하기위한,
		//이전 토탈 트래픽과 현재 토탈 트래픽의 비율
		double ratioOfTraffic = currTotalTraffic/prevTotalTraffic;
		
		//현재 해당 서버에 매칭된 각 유저들의 트래픽 + 비율 => 예상되는 토탈 트래픽
		int expectedTraffic = 0;
		for(int i=0; i<clientTrafficData.size(); i++){
			int currClientTraffic = clientTrafficData.get(i).getUserTraffic();
			expectedTraffic += (ratioOfTraffic * currClientTraffic);
		}
		
		this.expectedTraffic = expectedTraffic;
	}
	
	double getPreviousTotalCloudsTraffic(double currTotalTraffic){
		double prevTotalTraffic = 0;
		
		CDatabase databaseInstance = new CDatabase();
		databaseInstance.connectBrokerDatabase();
		
		prevTotalTraffic = databaseInstance.getPrevTotalCloudsTraffic(currTotalTraffic);
		
		databaseInstance.updatePrevTotalCloudsTraffic(currTotalTraffic);
		
		databaseInstance.disconnectBrokerDatabase();
		
		return prevTotalTraffic;
	}
	
	double getCurrentTotalCloudsTraffic(){
		double currTotalTraffic = 0;
		
		CDatabase databaseInstance = new CDatabase();
		databaseInstance.connectBrokerDatabase();
		
		currTotalTraffic = databaseInstance.getCurrTotalCloudsTraffic();
		
		databaseInstance.disconnectBrokerDatabase();
		
		return currTotalTraffic;
	}
	
	public int getMaximumTraffic() {
		return maximumTraffic;
	}
	public void setMaximumTraffic(int maximumTraffic) {
		this.maximumTraffic = maximumTraffic;
	}
	public String getServerIp() {
		return serverIp;
	}
	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}
	public int getEpNo() {
		return epNo;
	}
	public void setEpNo(int epNo) {
		this.epNo = epNo;
	}
}
