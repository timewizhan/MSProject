package Utility;

public class userInfo {
	private int mUID;
	private String mName;
	private String mLocation;
	private int mTraffic;
	
	public userInfo() {
		this.mUID = -1;
		this.mName = null;
		this.mLocation = null;
		this.mTraffic = 0;
	}
	
	public void setInfo(int uid, String uname, String loc) {
		this.mUID = uid;
		this.mName = uname;
		this.mLocation = loc;
	}
	
	public void updateTraffic(int traffic) {
		this.mTraffic = this.mTraffic + traffic;
	}
	
	public int getUID() {
		return this.mUID;
	}
	
	public String getName() {
		return this.mName;				
	}
	
	public String getLoc() {
		return this.mLocation;
	}
	
	public int getTraffic() {
		return this.mTraffic;
	}
}
