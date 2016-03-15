package Wrapper;

public class statusInfo {	
	private int[] mSIDs;
	private String[] mStatus;
	private String[] mTime;
	private int[] mTraffic;
	
	public statusInfo(int[] sid, String[] status, String[] time, int[] traffic) {
		mSIDs = sid;
		mStatus = status;
		mTime = time;
		mTraffic = traffic;
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
	
	public int[] getTrafficList() {
		return mTraffic;
	}
}