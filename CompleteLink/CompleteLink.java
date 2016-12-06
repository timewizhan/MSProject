import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class CompleteLink {

	static Logger log = Logger.getLogger(CompleteLink.class.getName());
	
	public static void main(String args[]) {
		
		PropertyConfigurator.configure("log/log4j.properties");
		
		Database db = new Database();
		db.mySqlConnect();
		
		//CompleteUserId·Î UserList (User ID, Location) »Ì¾Æ¿À±â
		ArrayList<User> completeUserId = new ArrayList<User>();
		completeUserId = db.getCompletedUserIdList();
		
		System.out.println("logic 1 =============================================================");
		int countOfAddedLink = 0;
		for(int i=0; i<completeUserId.size(); i++){
			System.out.println("[LOGIC 1][" + (i+1) + "/" + completeUserId.size() + "]");
			log.debug("CompleteUserId : " + completeUserId.get(i).getUserId() + "==========================================================================");
			
			ArrayList<Friend> friendList = db.getFriendInSocialLevelPerUser(completeUserId.get(i).getUserId().toString());
			for(int j=0; j<friendList.size(); j++){
				log.debug("================> Friend ID : " + friendList.get(j).getUserId() + ", PORTION : " + friendList.get(j).getPortion());
				boolean isCompleteLink = db.checkCompleteLink(friendList.get(j).getUserId().toString(), completeUserId.get(i).getUserId().toString());
				
				if(!isCompleteLink){
					db.insertNewLink(friendList.get(j).getUserId().toString(), completeUserId.get(i).getUserId().toString(), friendList.get(j).getPortion());
					countOfAddedLink++;
					log.debug("================================> Added Link : Friend ID : " + friendList.get(j).getUserId() + ", PORTION : " + friendList.get(j).getPortion());
				}
			}
		}
		
		db.mySqlDisconnect();
		System.out.println("=====================================================================");
		System.out.println("END Getting Social Weight Table");
		System.out.println("Total Number of Added Link : " + countOfAddedLink);
		log.debug("" + countOfAddedLink);
	
	}
}
