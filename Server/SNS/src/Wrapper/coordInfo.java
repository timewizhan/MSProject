package Wrapper;

import java.util.HashMap;

/**
 * The Class coordInfo.
 * Struct for the location information
 * including location name and its coordinate
 */
public class coordInfo {
	
	/**
	 * variables for the whole location info
	 */
	private HashMap<String, Double> mLat;
	private HashMap<String, Double> mLong;
	
	private String mServer_Loc;	
	private double mServer_Lat;
	private double mServer_Long;
	
	/**
	 * Class constructor
	 * Initializes the variables
	 */
	public coordInfo() {
		mLat = new HashMap<String, Double>();
		mLong = new HashMap<String, Double>();
		
		mServer_Loc = null;
		mServer_Lat = -1;
		mServer_Long = -1;
	}
	
	/**
	 * Sets the location name and its coordinate
	 *
	 * @param loc location name
	 * @param lat latitude of the location
	 * @param lon longitude of the location
	 */
	public void setCoord(String loc, double lat, double lon) {
		mLat.put(loc, lat);
		mLong.put(loc, lon);
	}
	
	/**
	 * Gets the latitude of the location
	 *
	 * @param loc location name
	 * @return the latitude of the location
	 */
	public double getLat(String loc) {
		return mLat.get(loc);
	}
	
	/**
	 * Gets the longitude of the location
	 *
	 * @param loc location name
	 * @return the longitude of the location
	 */
	public double getLong(String loc) {
		return mLong.get(loc);
	}
	
	/**
	 * Sets the service server's location information
	 *
	 * @param loc location name of the service server
	 */
	public void setServerCoord(String loc) {
		mServer_Loc = loc;
		mServer_Lat = mLat.get(loc);
		mServer_Long = mLong.get(loc);
	}
	
	/**
	 * Gets the location name of the service server
	 *
	 * @return the location name of the service server
	 */
	public String getServerLoc() {
		return mServer_Loc;
	}
	
	/**
	 * Gets the latitude of the service server
	 *
	 * @return the latitude of the service server
	 */
	public double getServerLat() {
		return mServer_Lat;
	}
	
	/**
	 * Gets the longitude of the service server
	 *
	 * @return the longitude of the service server
	 */
	public double getServerLong() {
		return mServer_Long;
	}
}