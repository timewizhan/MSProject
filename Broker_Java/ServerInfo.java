
public class ServerInfo {

	private String epAddr;
	private String location;
	
	public ServerInfo mappingIpAddrToLocation(String epAddr){
		
		CDatabase databaseInstance = new CDatabase();
		databaseInstance.connectBrokerDatabase();
		
		this.epAddr = epAddr;
		this.location = databaseInstance.getLocationWithIp(epAddr);
	
		databaseInstance.disconnectBrokerDatabase();
		
		return this;
	}
	
	public String getEpAddr(){
		return this.epAddr;
	}
	
	public String getServerLocation(){
		return this.location;
	}
}
