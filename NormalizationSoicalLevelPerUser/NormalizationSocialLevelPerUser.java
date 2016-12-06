import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class NormalizationSocialLevelPerUser {
	static Logger log = Logger.getLogger(NormalizationSocialLevelPerUser.class.getName());
	
	public static void main(String args[]){
		PropertyConfigurator.configure("log/log4j.properties");
		
		Database db = new Database();
		db.mySqlConnect();
		
		//CompleteUserId·Î UserList (User ID, Location) »Ì¾Æ¿À±â
		ArrayList<User> completeUserId = new ArrayList<User>();
		completeUserId = db.getCompletedUserIdList();
		
		System.out.println("logic 1 =============================================================");
		for(int i=0; i<completeUserId.size(); i++){
			System.out.println("[LOGIC 1][" + (i+1) + "/" + completeUserId.size() + "]");
			log.debug("CompleteUserId : " + completeUserId.get(i).getUserId() + "==========================================================================");
			
			ArrayList<Friend> friendList = db.getFriendInSocialLevelPerUser(completeUserId.get(i).getUserId().toString());
			for(int j=0; j<friendList.size(); j++){
				double sumPortion = db.getSumPortion(completeUserId.get(i).getUserId().toString());
				db.updateFriendPortion(completeUserId.get(i).getUserId().toString(), friendList.get(j).getUserId().toString(), friendList.get(j).getPortion(), sumPortion);
			}
		}
		
		db.mySqlDisconnect();
		System.out.println("=====================================================================");
		System.out.println("END Getting Social Weight Table");
	}
}
