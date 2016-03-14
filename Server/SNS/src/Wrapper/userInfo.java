package Wrapper;

public class userInfo {
	private int mUID;
	private String mName;
	private String mLocation;
	private int mTraffic;	
	
	public userInfo() {
		mUID = -1;
		mName = null;
		mLocation = null;
		mTraffic = 0;
	}
	
	public void setInfo(int uid, String uname, String loc) {
		mUID = uid;
		mName = uname;
		mLocation = loc;
	}
	
	public void updateTraffic(int traffic) {
		mTraffic = mTraffic + traffic;
	}
	
	public int getUID() {
		return mUID;
	}
	
	public String getName() {
		return mName;				
	}
	
	public String getLoc() {
		return mLocation;
	}
	
	public int getTraffic() {
		return mTraffic;
	}
}
