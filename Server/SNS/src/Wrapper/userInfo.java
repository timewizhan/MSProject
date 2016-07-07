package Wrapper;

/**
 * The Class userInfo.
 * Struct for the user information stored in the database
 */
public class userInfo {
	
	private int mUID;
	private String mName;
	private String mLocation;	
	private int mTraffic;	
	
	/**
	 * Class constructor
	 * Initializes the variables
	 */
	public userInfo() {
		mUID = -1;
		mName = null;
		mLocation = null;
		mTraffic = 0;
	}
	
	/**
	 * Sets the user information
	 *
	 * @param uid the user's id
	 * @param uname the user's name
	 * @param loc the user's current location name
	 */
	public void setInfo(int uid, String uname, String loc) {
		mUID = uid;
		mName = uname;
		mLocation = loc;
	}
	
	/**
	 * Calculates the total traffic incurred by the user
	 *
	 * @param traffic the traffic value
	 */
	public void updateTraffic(int traffic) {
		mTraffic = mTraffic + traffic;
	}
	
	/**
	 * Gets the id of user
	 *
	 * @return the user's id
	 */
	public int getUID() {
		return mUID;
	}
	
	/**
	 * Gets the name of the user
	 *
	 * @return the user's name
	 */
	public String getName() {
		return mName;				
	}
	
	/**
	 * Gets the current location name of the user 
	 *
	 * @return the user's current location name
	 */
	public String getLoc() {
		return mLocation;
	}
	
	/**
	 * Gets the total traffic incurred by the user
	 *
	 * @return the total traffic incurred by the user
	 */
	public int getTraffic() {
		return mTraffic;
	}
}