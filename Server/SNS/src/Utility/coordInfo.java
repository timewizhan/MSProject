package Utility;

import java.util.HashMap;

public class coordInfo {
	private HashMap<String, Double> mLat;
	private HashMap<String, Double> mLong;
	
	private String mServer_Loc;
	private double mServer_Lat;
	private double mServer_Long;
	
	public coordInfo() {
		mLat = new HashMap<String, Double>();
		mLong = new HashMap<String, Double>();
		
		mServer_Loc = null;
		mServer_Lat = -1;
		mServer_Long = -1;
	}
	
	public void setCoord(String loc, double lat, double lon) {
		mLat.put(loc, lat);
		mLong.put(loc, lon);
	}
	
	public double getLat(String loc) {
		return mLat.get(loc);
	}
	
	public double getLong(String loc) {
		return mLong.get(loc);
	}
	
	public void setServerCoord(String loc) {
		mServer_Loc = loc;
		mServer_Lat = mLat.get(loc);
		mServer_Long = mLong.get(loc);
	}
	
	public String getServerLoc() {
		return mServer_Loc;
	}
	
	public double getServerLat() {
		return mServer_Lat;
	}
	
	public double getServerLong() {
		return mServer_Long;
	}
}
