package Wrapper;

/**
 * The Class statusInfo.
 * Struct for the status(i.e. tweet) information stored in the database as a list
 * Note that the data stored in same row has the same index in the list
 */
public class statusInfo {	
	
	private int[] mSIDs;
	private String[] mStatus;
	private String[] mTime;
	private int[] mTraffic;
	
	/**
	 * Class constructor
	 * Initializes the variables
	 *
	 * @param sid a list of status ids
	 * @param status a list of status texts
	 * @param time a list of status timestamps
	 * @param traffic a list of status traffic values
	 *
	 */
	public statusInfo(int[] sid, String[] status, String[] time, int[] traffic) {
		mSIDs = sid;
		mStatus = status;
		mTime = time;
		mTraffic = traffic;
	}
	
	/**
	 * Gets the list of status ids
	 *
	 * @return the list of status ids
	 */
	public int[] getSIDs() {
		return mSIDs;
	}
	
	/**
	 * Gets the list of status texts
	 *
	 * @return the list of status texts 
	 */
	public String[] getStatusList() {
		return mStatus;
	}
	
	/**
	 * Gets the list of status timestamps
	 *
	 * @return the list of status timestamps
	 */
	public String[] getTimeList() {
		return mTime;
	}
	
	/**
	 * Gets the list of status traffic values
	 *
	 * @return the list of status traffic values
	 */
	public int[] getTrafficList() {
		return mTraffic;
	}
}