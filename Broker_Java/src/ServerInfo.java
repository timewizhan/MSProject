
public class ServerInfo {

	private String epAddr;
	private int epNo;
	private String location;
	
	public void mappingIpAddrToLocation(String epAddr){
		
		CDatabase databaseInstance = new CDatabase();
		databaseInstance.connectBrokerDatabase();
		
		this.epAddr = epAddr;
		this.location = databaseInstance.getLocationWithIp(epAddr);
		
		databaseInstance.disconnectBrokerDatabase();
	}
	
	public void mappingIpAddrToEpNo(String epAddr){

		CDatabase databaseInstance = new CDatabase();
		databaseInstance.connectBrokerDatabase();
			
		this.epNo = databaseInstance.getEpNoWithIp(epAddr);
		
		databaseInstance.disconnectBrokerDatabase();
	}

	public String getEpAddr(){
		return this.epAddr;
	}
	
	public int getEpNo(){
		return this.epNo;
	}
	
	public String getServerLocation(){
		return this.location;
	}
	
	
}
