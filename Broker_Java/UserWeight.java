
public class UserWeight {
	
	private String user;
	private int user_no;
	private double ep[];
	
	public UserWeight(int numOfEp){
		user = new String();
		user_no = 0;
		ep = new double[numOfEp];
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public int getUser_no() {
		return user_no;
	}

	public void setUser_no(int user_no) {
		this.user_no = user_no;
	}
	
//	public void setNormDistValues(double [] normValues){
//		ep = normValues;
//	}
	
	public void setWeightValues(double [] normValues){
		ep = normValues;
	}
	
	public double [] getWeightValues(){
		return ep;
	}
}
