import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.apache.log4j.Logger;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PropertyConfigurator;

public class ServerStatus {

	static Logger log = Logger.getLogger(CBroker.class.getName());
	
	private long currentTraffic;
	private long expectedTraffic;
	private long maximumTraffic;
	private String serverIp;
	private int epNo;
	private long remainTraffic;
	static public double ratioOfTraffic;
	
	public double getRatioOfTraffic(){
		return this.ratioOfTraffic;
	}
	
	public void setRatioOfTraffic(double ratioOfTraffic){
		this.ratioOfTraffic = ratioOfTraffic;
	}
	
	public long getRemainTraffic(){
		return remainTraffic;
	}
	public void setRemainTraffic(long remainTraffic){
		this.remainTraffic = remainTraffic;
	}
	public long getCurrentTraffic() {
		return currentTraffic;
	}
	public void setCurrentTraffic(int currentTraffic) {
		this.currentTraffic = currentTraffic;
	}
	public long getExpectedTraffic() {
		return expectedTraffic;
	}
	/*
	public void setExpectedTraffic(int expectedTraffic) {
		this.expectedTraffic = expectedTraffic;
	}
	*/
	public void setExpectedTraffic(int epNo, ArrayList<ClientTrafficData> clientTrafficData) {
		
		//현재 해당 서버에 매칭된 각 유저들의 트래픽 + 비율 => 예상되는 토탈 트래픽
		int expectedTraffic = 0;
		for(int i=0; i<clientTrafficData.size(); i++){
			int currClientTraffic = clientTrafficData.get(i).getUserTraffic();
		//	expectedTraffic += (ratioOfTraffic * currClientTraffic);
			expectedTraffic += currClientTraffic;
		}
		
		this.expectedTraffic = expectedTraffic;
	}
	
	public void setExpectedTraffic(int epNo, int expectedTraffic) {
		
		this.expectedTraffic = expectedTraffic;
	}
	
	double getPreviousTotalCloudsTraffic(double currTotalTraffic){
		double prevTotalTraffic = 0;
		
		CDatabase databaseInstance = new CDatabase();
		databaseInstance.connectBrokerDatabase();
		
		prevTotalTraffic = databaseInstance.getPrevTotalCloudsTraffic(currTotalTraffic);
		/*
		if(databaseInstance.checkPrevTotalTrafficExisting()){
			databaseInstance.updatePrevTotalCloudsTraffic(currTotalTraffic);
		} else {
			databaseInstance.insertPrevTotalCloudsTraffic(currTotalTraffic);
		}
		*/
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
	
	public long getMaximumTraffic() {
		return maximumTraffic;
	}
	public void setMaximumTraffic(long maximumTraffic) {
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
