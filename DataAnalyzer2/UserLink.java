
public class UserLink {
	String sourceName;
	String destinationName;
	double portion;
	
	UserLink(){
		this.sourceName = null;
		this.destinationName = null;
		this.portion = 0.0;
	}
	
	void setSourceName(String srcName){
		this.sourceName = srcName;
	}
	
	String getSourceName(){
		return this.sourceName;
	}
	
	void setDestinationName(String dstName){
		this.destinationName = dstName;
	}
	
	String getDestinationName(){
		return this.destinationName;
	}
	
	void setPortion(double portion){
		this.portion = portion;
	}
	
	double getPortion(){
		return this.portion;
	}
}
