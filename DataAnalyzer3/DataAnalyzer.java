import java.sql.*;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class DataAnalyzer {
	
	static Logger log = Logger.getLogger(DataAnalyzer.class.getName());
	
	public static void main(String args[]){
		
		PropertyConfigurator.configure("log/log4j.properties");
		
		//ArrayList로 모든 User의 리스트를 저장
		ArrayList<User> users = new ArrayList<User>();
		
		//기존에 이미 친구가 많은 애들 리스트
		ArrayList<User> existingUserList = new ArrayList<User>();
		
		//DB Connection
		Database db = new Database();
		db.connect();
		db.mySqlConnect();
		
		//Extract the completeUserId
	//	users = db.getCompletedUserIdList();
		users = db.getUsersFromUserProperty();
		//logic1
		System.out.println("logic 1 =============================================================");
		for(int i=0; i<users.size(); i++){
			System.out.println("[LOGIC 1][" + (i+1) + "/" + users.size() + "]");
			
			ArrayList<Friend> friendList = db.getFriendList(users.get(i).getUserId());
			
			if(friendList.size() >= 10){
				//우선, 기존에 이미 친구가 많은 애들 리스트에 저장
				users.get(i).setFriendList(friendList);
				existingUserList.add(users.get(i));
				
				//MySQL DB의 모든 User들 저장하는 테이블에 해당 사용자 넣기
				db.insertUsers(users.get(i));
				//MySQL DB의 친구가 많은 User들만 저장하는 테이블에 해당 사용자 넣기
				db.insertUsersHavingManyFriends(users.get(i).getUserId());
				
				log.info("[" + (i+1) + "/" + users.size() + "] User id : " + users.get(i).getUserId());
				for(int j=0; j<friendList.size(); j++){
					//각 사용자들의 친구들도 모든 User들 저장하는 테이블에 넣기
					db.insertUsers(friendList.get(j));
					//친구들만 Insert
					db.insertFriends(friendList.get(j), users.get(i).getUserId());
					log.debug(" - friend id : " + friendList.get(j).getUserId() 
								+ ", isComplete : " + friendList.get(j).getIsCompleteUserId()
								+ ", isFollower : " + friendList.get(j).getIsFollower()
								+ ", followingPortion : " + friendList.get(j).getFollowingPortion()
								+ ", followerPortion : " + friendList.get(j).getFollowerPortion());
				}
			
			// 일단 친구가 없거나, 많이 없는 (10명 미만) 의 유저들도 complete user id로 만들어주기 위해
			// users 테이블에 넣는다.
			} else {
				db.insertUsers(users.get(i));
				db.insertUsersNotHavingManyFriends(users.get(i).getUserId().toString(), users.get(i).getUserLocation().toString());
			}
		}
		
		
		//logic2 - 친구들 리스트 중에서, 친구가 10개 이상인 User 그룹에 포함이 되는 유저/포함이 안되는 유저
		System.out.println("logic 2 =============================================================");
		ArrayList<Friend> allTheFriends = new ArrayList<Friend>();
		allTheFriends = db.getAllTheFriends(); 
		for(int i=0; i<allTheFriends.size(); i++){
			System.out.println("[LOGIC 2][" + (i+1) + "/" + allTheFriends.size() + "]");
			
			boolean isIncludeHeavyUser = db.findInHeavyUsers(allTheFriends.get(i).getUserId());
			if(isIncludeHeavyUser){
				db.insertFriendsInHeavy(allTheFriends.get(i).getUserId());
			} else {
				db.insertFriendsNotInHeavy(allTheFriends.get(i));
			}
		}
	
		
		//logic 3-1 - 유저 Location 고려해서, 원래 유저 리스트에서 친구가 10 이하인 애들 친구 만들어 주기 (SocialLevelPerUser 테이블에 들어갈 친구들 만들어주기)
		System.out.println("logic 3-1 =============================================================");
		//일단 FriendsNotInHeavy에 포함하는 애들 다 불러오기
		ArrayList<User> originUserList = new ArrayList<User>();
	//	userList = db.getAllFriendsNotInHeavy();
		originUserList = db.getUsersNotHavingManyFriends();
		
		for(int i=0; i<originUserList.size(); i++){
			System.out.println("[LOGIC 3-1][" + (i+1) + "/" + originUserList.size() + "]");
			
			//FriendsNotInHeavy에 포함되는 애들 모두에게, Users 테이블에서 뽑아서 친구 20명씩 만들어주자
			String userLocation = originUserList.get(i).getUserLocation();
			int numUsersAtSamePlace = 5;
			int numUsersAtOtherPlace = 15;
			
			//5명은 같은 Location
			ArrayList<User> samePlaceUsers = db.getUsersSameLocation(userLocation, numUsersAtSamePlace);
			
			//15명은 다른 Location
			ArrayList<User> otherPlaceUsers = db.getUsersOtherLocation(userLocation, numUsersAtOtherPlace);
			
			//insert into addedSocialLevelPerUser table
			db.insertIntoAddedSocialLevelPerUser(originUserList.get(i), samePlaceUsers, otherPlaceUsers);
		}
		
		
		//logic3-2 - 유저 Location 고려해서, 친구들 리스트 중에서 친구가 10 이하인 애들 친구 만들어 주기 (SocialLevelPerUser 테이블에 들어갈 친구들 만들어주기)
		System.out.println("logic 3-2 =============================================================");
		//일단 FriendsNotInHeavy에 포함하는 애들 다 불러오기
		ArrayList<User> userList = new ArrayList<User>();
		userList = db.getAllFriendsNotInHeavy();
		
		for(int i=0; i<userList.size(); i++){
			System.out.println("[LOGIC 3-2][" + (i+1) + "/" + userList.size() + "]");
			
			//FriendsNotInHeavy에 포함되는 애들 모두에게, Users 테이블에서 뽑아서 친구 20명씩 만들어주자
			String userLocation = userList.get(i).getUserLocation();
			int numUsersAtSamePlace = 5;
			int numUsersAtOtherPlace = 15;
			
			//5명은 같은 Location
			ArrayList<User> samePlaceUsers = db.getUsersSameLocation(userLocation, numUsersAtSamePlace);
			
			//15명은 다른 Location
			ArrayList<User> otherPlaceUsers = db.getUsersOtherLocation(userLocation, numUsersAtOtherPlace);
			
			//insert into addedSocialLevelPerUser table
			db.insertIntoAddedSocialLevelPerUser(userList.get(i), samePlaceUsers, otherPlaceUsers);
		}
		
		
		//logic4 - 완전한 SocialLevelPerUser 테이블 만들어주기 (기존 데이터 + 새로 만들어진거)
		//Portion은 공평하게 넣어주기
		System.out.println("logic 4 =============================================================");
		for(int i=0; i<existingUserList.size(); i++){
			System.out.println("[LOGIC 4-1][" + (i+1) + "/" + existingUserList.size() + "]");
			
			db.insertIntoSocialLevelPerUser(existingUserList.get(i));
		}
		
		
		//여기서 새로운 애들 넣어줘야함
		ArrayList<UserLink> addedUserLinkList = new ArrayList<UserLink>();
		addedUserLinkList = db.getAddedSocialLevelPerUser();
		for(int i=0; i<addedUserLinkList.size(); i++){
			System.out.println("[LOGIC 4-2][" + (i+1) + "/" + addedUserLinkList.size() + "]");
			
			db.insertAddedUsersIntoSocialLevelPerUser(addedUserLinkList.get(i));
		}
		
		
		//logic5 - 새로 만들어진 SocialLevelPerUser에 따라서 UserLink 테이블 만들어주기
		System.out.println("logic 5 =============================================================");
		int totalWriteMyself = 0;
		int totalWriteOthers = 0;
		ArrayList<User> finalUserList = new ArrayList<User>();
		finalUserList = db.getUsers();
		for(int i=0; i<finalUserList.size(); i++){
			System.out.print("[LOGIC 5][" + (i+1) + "/" + finalUserList.size() + "]");
			totalWriteMyself = db.getTotalNumWriteOfMyself(finalUserList.get(i).getUserId());
			totalWriteOthers = db.getTotalNumWriteOfFriends(finalUserList.get(i).getUserId());
			System.out.print(" total write to myself : " + totalWriteMyself);
			System.out.println(", total write to others : " + totalWriteOthers);
			
			//insert TweetMyself
			db.insertTweetMySelf(finalUserList.get(i).getUserId(), totalWriteMyself);
			
			//get list of friends (user id, portion) from social level per user table 
			ArrayList<UserLink> userLinkInSocialLevelPerUser = db.getFriendInSocialLevelPerUser(finalUserList.get(i).getUserId());
			for(int j=0; j<userLinkInSocialLevelPerUser.size(); j++){
				String friendId = userLinkInSocialLevelPerUser.get(j).getDestinationName().toString();
				double portion = userLinkInSocialLevelPerUser.get(j).getPortion();
				int numTweetToFriend = (int)(totalWriteOthers * portion);
				db.insertTweetOther(finalUserList.get(i).getUserId(), friendId, numTweetToFriend);
			}
		}
		
		
		//logic6 - 새로 만들어놓은 관계에 따라 completeUserId 테이블 만들어주기
		System.out.println("logic 6 =============================================================");
		int classifier = 0;
		for(int i=0; i<finalUserList.size(); i++){
			System.out.println("[LOGIC 6][" + (i+1) + "/" + finalUserList.size() + "]");
			
			if(i >= 0 && i <= 399){
				classifier = 1;
			} else if(i >= 400 && i <= 799){
				classifier = 2;
			} else if(i >= 800 && i <= 1199){
				classifier = 3;
			} else if(i >= 1200 && i <= 1599){
				classifier = 4;
			} else if(i >= 1600 && i <= 1999){
				classifier = 5;
			} else if(i >= 2000 && i <= 2399){
				classifier = 6;
			} else if(i >= 2400 && i <= 2799){
				classifier = 7;
			} else if(i >= 2800 && i <= 3199){
				classifier = 8;
			} else if(i >= 3200 && i <= 3599){
				classifier = 9;
			}
			
			db.createCompleteUserIdTable(finalUserList.get(i).getUserId().toString(), finalUserList.get(i).getUserLocation().toString(), classifier);
		}
		
		/////////////MFRatio 테이블 만들어줘야 함
		/////////////MFRatio 에 데이터가 있으면 그대로 사용하고
		/////////////MFRatio 에 데이터가 없으면 기존에 있는 것 중에 하나를 랜덤하게 뽑아서 Insert 한다
		finalUserList = new ArrayList<User>();
		finalUserList = db.getFinalUsers();
		MFRatio mfRatio = new MFRatio();
		for(int i=0; i<finalUserList.size(); i++){
			
			// db.83에 있는 디비의 MFRatio에 값이 있으면 가지고 온다
			mfRatio = new MFRatio();
			mfRatio = db.getMFRatio(finalUserList.get(i).getUserId());
			if(mfRatio == null){
				
				//만약에 저기 값이 없으면 거기 있는 값 중 임의의 값을 가지고 온다
				mfRatio = db.getRandMFRatio();
				db.insertRandMFRatio(finalUserList.get(i), mfRatio);
				
			} else {
				
				//값이 있으면 그걸 이용해서 MFRatio 테이블에 Insert
				db.insertMFRatio(finalUserList.get(i), mfRatio);
			}
			
		}
		
		
		////////////UserProperty 테이블 만들어주기
		finalUserList = new ArrayList<User>();
		finalUserList = db.getFinalUsers();
		UserProperty eachUserProperty = new UserProperty();
		
		for(int i=0; i<finalUserList.size(); i++){
			
			eachUserProperty = new UserProperty();
			// mysql에 newUserproperty 테이블에 있으면 그대로 가져와서 넣고
			eachUserProperty = db.getUserProperty(finalUserList.get(i).getUserId().toString());
			if(eachUserProperty == null) {
				// 없으면, 임의로 하나 골라서 넣는다.
				eachUserProperty = db.getRandUserProperty();
				db.insertRandUserProperty(finalUserList.get(i), eachUserProperty);
				
			} else {
				
				//값이 있으면 그걸 이용해서 MFRatio 테이블에 Insert
				db.insertUserProperty(finalUserList.get(i), eachUserProperty);
			}
		}
		
		//SocialLevelPerUser 테이블에서 SourceName이랑 DestinationName 이랑 같은 Row는 삭제
		//SocialLevelPerUser 테이블에 SourceName 인덱싱
		
		System.out.println("=====================================================================");
		System.out.println("END Data Analyzer");
		
		//DB Disconnection
		db.disconnect();
		db.mySqlDisconnect();
	}
}
