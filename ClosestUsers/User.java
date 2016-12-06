import java.util.ArrayList;

public class User {
	String UserId;
	String UserLocation;
	int classifier;
	
	User(){
		this.UserId = null;
		this.UserLocation = null;
		this.classifier = 0;
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
}
