package Wrapper;

public class statusInfo {	
	private int[] mSIDs;
	private String[] mStatus;
	private String[] mTime;
	
	public statusInfo(int[] sid, String[] status, String[] time) {
		mSIDs = sid;
		mStatus = status;
		mTime = time;
	}
	
	public int[] getSIDs() {
		return mSIDs;
	}
	
	public String[] getStatusList() {
		return mStatus;
	}
	
	public String[] getTimeList() {
		return mTime;
	}
}