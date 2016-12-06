import java.util.ArrayList;

public class User {
	String UserId;
	String UserLocation;
	int numOfTweet;
	int classifier;
	ArrayList<Friend> friendList;
	
	User(){
		this.UserId = null;
		this.UserLocation = null;
		this.classifier = 0;
		this.friendList = new ArrayList<Friend>();
	}
	
	void setUserId(String userId){
		this.UserId = userId;
	}
	
	String getUserId(){
		return this.UserId;
	}
	
	void setUserLocation(String userLocation){
		this.UserLocation = userLocation;
	}
	
	String getUserLocation(){
		return this.UserLocation;
	}
	
	void setClassifier(int classifier){
		this.classifier = classifier;
	}
	
	int getClassifier(){
		return this.classifier;
	}
	
	void setNumOfTweet(int numOfTweet){
		this.numOfTweet = numOfTweet;
	}
	
	int getNumOfTweet(){
		return this.numOfTweet;
	}
	
	void setFriendList(ArrayList<Friend> friendList){
		this.friendList = friendList;
	}
	
	ArrayList<Friend> getFriendList(){
		return this.friendList;
	}
}
