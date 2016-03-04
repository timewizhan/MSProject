package Utility;

import java.util.HashMap;

public class coordInfo {
	private HashMap<String, Double> mLat;
	private HashMap<String, Double> mLong;
	
	public coordInfo() {
		this.mLat = new HashMap<String, Double>();
		this.mLong = new HashMap<String, Double>();
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
}
