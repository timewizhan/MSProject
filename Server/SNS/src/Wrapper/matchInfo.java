package Wrapper;

/**
 * The Class matchInfo.
 * Struct for the match result information
 */
public class matchInfo {
	
	private String mName;
	
	private int mPrev;
	private int mCurr;
	
	/**
	 * Class constructor.
	 * Initializes the variables
	 */
	public matchInfo() {
		mName = null;
		mPrev = 0;
		mCurr = 0;		
	}
	
	/**
	 * Sets the match result information
	 *
	 * @param uname a user's name
	 * @param prev current location of the user's data
	 * @param curr next location of the user's data
	 */
	public void setInfo(String uname, int prev, int curr) {
		mName = uname;
		mPrev = prev;
		mCurr = curr;		
	}
	
	/**
	 * Gets the user's name
	 *
	 * @return the user's name
	 */
	public String getName() {
		return mName;
	}
	
	/**
	 * Gets the current location of the user's data
	 *
	 * @return the current location of the user's data
	 */
	public int getPrev() {
		return mPrev;
	}
	
	/**
	 * Gets the next location of the user's data.
	 *
	 * @return the next location of the user's data
	 */
	public int getCurr() {
		return mCurr;
	}
}