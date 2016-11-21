
public class Friend {
	String userId;
	String location;
	boolean isCompleteUser;
	boolean isFollower;
	double followingPortion;
	double followerPortion;
	
	Friend(){
		this.userId = null;
		this.isFollower = false;
		this.followingPortion = 0.0;
		this.followerPortion = 0.0;
		this.location = null;
	}
	
	void setUserId(String userId){
		this.userId = userId;
	}
	
	String getUserId(){
		return this.userId;
	}
	
	void setLocation(String location){
		this.location = location;
	}
	
	String getLocation(){
		return this.location;
	}
	
	void setIsFollower(boolean isFollow){
		this.isFollower = isFollow;
	}
	
	boolean getIsFollower(){
		return this.isFollower;
	}
	
	void setFollowingPortion(double portion){
		this.followingPortion = portion;
	}
	
	double getFollowingPortion(){
		return this.followingPortion;
	}
	
	void setFollowerPortion(double portion){
		this.followerPortion = portion;
	}
	
	double getFollowerPortion(){
		return this.followerPortion;
	}
	
	void setIsCompleteUserId(boolean isCompleteUser){
		this.isCompleteUser = isCompleteUser;
	}
	
	boolean getIsCompleteUserId(){
		return this.isCompleteUser;
	}
}
