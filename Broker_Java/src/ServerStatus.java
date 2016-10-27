import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.apache.log4j.Logger;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PropertyConfigurator;

public class ServerStatus {

	static Logger log = Logger.getLogger(CBroker.class.getName());
	
	private int currentTraffic;
	private int expectedTraffic;
	private int maximumTraffic;
	private String serverIp;
	private int epNo;
	private int remainTraffic;
	static public double ratioOfTraffic;
	
	public double getRatioOfTraffic(){
		return this.ratioOfTraffic;
	}
	
	public void setRatioOfTraffic(double ratioOfTraffic){
		this.ratioOfTraffic = ratioOfTraffic;
	}
	
	public int getRemainTraffic(){
		return remainTraffic;
	}
	public void setRemainTraffic(int remainTraffic){
		this.remainTraffic = remainTraffic;
	}
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
		
	//	log.debug("		EP No. : " + epNo);
		
		//���� ��Ż ���� Ʈ����
		double currTotalTraffic = (double)getCurrentTotalCloudsTraffic();
	//	log.debug("		Current Total Traffic : " + currTotalTraffic);
		
		//previous ��Ż ���� Ʈ����
		double prevTotalTraffic = (double)getPreviousTotalCloudsTraffic(currTotalTraffic);
	//	log.debug("		Previous Total Traffic : " + prevTotalTraffic);
		
		//���� ����Ǵ� �� ����ڵ��� Ʈ������ ���ϱ�����,
		//���� ��Ż Ʈ���Ȱ� ���� ��Ż Ʈ������ ����
		double ratioOfTraffic = currTotalTraffic/prevTotalTraffic;
		setRatioOfTraffic(ratioOfTraffic);
		
	//	log.debug("		Ratio of Traffic : " + ratioOfTraffic + "\n");
		
		//���� �ش� ������ ��Ī�� �� �������� Ʈ���� + ���� => ����Ǵ� ��Ż Ʈ����
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
