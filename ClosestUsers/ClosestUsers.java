import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class ClosestUsers {
	
	static Logger log = Logger.getLogger(ClosestUsers.class.getName());
	
	public static void main(String args[]){
		
		PropertyConfigurator.configure("log/log4j.properties");
		
		//ArrayList로 모든 User의 리스트를 저장
		ArrayList<User> users = new ArrayList<User>();
		
		//DB Connection
		Database db = new Database();
		db.mySqlConnect();
		
		//complete user id 리스트를 뽑아 온다
		users = db.getUsers();
		
		//logic1
		int NY_classifier = 0;
		int TEX_classifier = 0;
		int WA_classifier = 0;
		
		double NY_Counter = 1;
		double TEX_Counter = 1;
		double WA_Counter = 1;
		
		System.out.println("logic 1 =============================================================");
		for(int i=0; i<users.size(); i++){
			System.out.println("[LOGIC 1][" + (i+1) + "/" + users.size() + "]");
			
			String userId = users.get(i).getUserId();
			int epNum = db.getUserEpNum(users.get(i).getUserId());
			String userPlace = users.get(i).getUserLocation();
			
			log.debug("User Id : " + userId + ", EP Num : " + epNum + ", User Place : " + userPlace);
			
			//Newyork
			if(epNum == 3){	
				db.insertNewyorkUsers(userId, userPlace, NY_classifier);
				
				NY_Counter++;
				if((NY_Counter % 400) == 0.0)
					NY_classifier++;
				
				log.debug("User Id : " + userId + ", User Place : " + userPlace + ", Classifier : " + NY_classifier + ", Cloud : NEWYORK");
			
			//Texas
			}else if(epNum == 2){		
				db.insertTexasUsers(userId, userPlace, TEX_classifier);
				
				TEX_Counter++;
				if((TEX_Counter % 400) == 0.0)
					TEX_classifier++;
			
				log.debug("User Id : " + userId + ", User Place : " + userPlace + ", Classifier : " + TEX_classifier + ", Cloud : TEXAS");
				
			//Washington
			}else if(epNum == 1){		
				db.insertWashingtonUsers(userId, userPlace, WA_classifier);
				
				WA_Counter++;
				if((WA_Counter % 400) == 0.0)
					WA_classifier++;
				
				log.debug("User Id : " + userId + ", User Place : " + userPlace + ", Classifier : " + WA_classifier + ", Cloud : WASHINGTON");
			}
		}
		
		System.out.println("=====================================================================");
		System.out.println("END Data Analyzer");
		
		//DB Disconnection
		db.mySqlDisconnect();
	}
}
