
public class LpMatchResult {

	private String userId;
	private int userNo;
	private int cloudNo;
	
	public LpMatchResult(){}
	
	public LpMatchResult(LpMatchResult matchRes){
		this.setUserId(matchRes.getUserId());
		this.setUserNo(matchRes.getUserNo());
		this.setCloudNo(matchRes.getCloudNo());
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public int getUserNo() {
		return userNo;
	}
	public void setUserNo(int userNo) {
		this.userNo = userNo;
	}
	public int getCloudNo() {
		return cloudNo;
	}
	public void setCloudNo(int cloudNo) {
		this.cloudNo = cloudNo;
	}
}
