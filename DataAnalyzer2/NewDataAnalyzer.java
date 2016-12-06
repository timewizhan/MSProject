import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class NewDataAnalyzer {
	static Logger log = Logger.getLogger(NewDataAnalyzer.class.getName());
	public static void main(String args[]){
		PropertyConfigurator.configure("log/log4j.properties");
		
		//DB Connection
		Database db = new Database();
		db.mySqlConnect();
		
		//ArrayList로 모든 User의 리스트를 저장
		ArrayList<User> users = new ArrayList<User>();
		//UserProperty 테이블에 있는 유저들 리스트 뽑아오기
		users = db.getUserListFromUserProperty();
		
		//UserProperty 테이블에서 사용자들 리스트 불러온다
		ArrayList<UserProperty> properties = new ArrayList<UserProperty>();
		
		//logic1 - UserProperty에 있는 유저 아이디, 장소, 트윗 개수 정보를 가지고 온다.
		System.out.println("logic 1 =============================================================");
		//2000개만 저장
		int maxUserSize = 2000;
		for(int i=0; i<users.size(); i++){
			System.out.println("[LOGIC 1][" + (i+1) + "/" + maxUserSize + "]");
			
			if(maxUserSize < 0){
				break;
			} else {
				//트윗 개수가 20개 이상 301개 이하인 유저는 새로 만든 UserProperty 테이블에 넣는다. 
				if(users.get(i).getNumOfTweet() >= 20 && users.get(i).getNumOfTweet() <= 301){
					properties = db.getUserProperties(users.get(i).getUserId());
					db.insertNewUserProperty(properties);
					
					maxUserSize--;
				}
			}
		}
		
		System.out.println("=====================================================================");
		System.out.println("END Data Analyzer");
	/*	
		System.out.println("logic 1 =============================================================");
		for(int i=0; i<users.size(); i++){
			System.out.println("[LOGIC 1][" + (i+1) + "/" + users.size() + "]");
			
			//트윗 개수가 20개 이상 301개 이하인 유저는 새로 만든 UserProperty 테이블에 넣는다. 
			if(users.get(i).getNumOfTweet() >= 20 && users.get(i).getNumOfTweet() <= 301){
				properties = db.getUserProperties(users.get(i).getUserId());
				db.insertNewUserProperty(properties);
			}
		}
		
		System.out.println("=====================================================================");
		System.out.println("END Data Analyzer");
	*/	
		//DB Disconnection
		db.mySqlDisconnect();
	}
}
