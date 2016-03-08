package Wrapper;
public class statusInfo {	
	private int[] mSIDs;
	private String mStatus;
	
	public statusInfo(int[] sid, String status) {
		this.mSIDs = sid;
		this.mStatus = status;
	}
	
	public int[] getSIDs() {
		return this.mSIDs;
	}
	
	public String getStatus() {
		return this.mStatus;
	}
}