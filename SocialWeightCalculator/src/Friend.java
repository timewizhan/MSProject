
public class Friend {
	String userId;
	String location;
	double portion;
	
	Friend(){
		this.userId = null;
		this.location = null;
		this.portion = 0.0;
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
	
	void setPortion(double portion){
		this.portion = portion;
	}
	
	double getPortion(){
		return this.portion;
	}
}
