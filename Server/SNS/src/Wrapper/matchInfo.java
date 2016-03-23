package Wrapper;

public class matchInfo {
	private String mName;
	private int mPrev;
	private int mCurr;
	
	public matchInfo() {
		mName = null;
		mPrev = 0;
		mCurr = 0;		
	}
	
	public void setInfo(String uname, int prev, int curr) {
		mName = uname;
		mPrev = prev;
		mCurr = curr;		
	}
	
	public String getName() {
		return mName;
	}
	
	public int getPrev() {
		return mPrev;
	}
	
	public int getCurr() {
		return mCurr;
	}
}
