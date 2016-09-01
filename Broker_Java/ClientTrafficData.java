
public class ClientTrafficData {
	
	private String userId;
	private int userTraffic;
	private int cloudNo;
	
	public ClientTrafficData() {
	}
	
	public ClientTrafficData(ClientTrafficData clientData){
		this.userId = clientData.getUserId();
		this.userTraffic = clientData.getUserTraffic();
		this.cloudNo = clientData.getCloudNo();
	}
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public int getUserTraffic() {
		return userTraffic;
	}
	public void setUserTraffic(int userTraffic) {
		this.userTraffic = userTraffic;
	}
	public int getCloudNo() {
		return cloudNo;
	}
	public void setCloudNo(int cloudNo) {
		this.cloudNo = cloudNo;
	}
}
