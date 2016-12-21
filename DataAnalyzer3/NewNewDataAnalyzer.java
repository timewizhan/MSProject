import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class NewNewDataAnalyzer {
	static Logger log = Logger.getLogger(NewNewDataAnalyzer.class.getName());
	
	public static void main(String args[]){
		
		PropertyConfigurator.configure("log/log4j.properties");
		
		//ArrayList�� ��� User�� ����Ʈ�� ����
				ArrayList<User> users = new ArrayList<User>();
				
				//������ �̹� ģ���� ���� �ֵ� ����Ʈ
				ArrayList<User> existingUserList = new ArrayList<User>();
				
				//DB Connection
				Database db = new Database();
				db.connect();
				db.mySqlConnect();
		/*		
				//Extract the completeUserId
			//	users = db.getCompletedUserIdList();
				users = db.getUsersFromUserProperty();
				//logic1
				System.out.println("logic 1 =============================================================");
				for(int i=0; i<users.size(); i++){
					System.out.println("[LOGIC 1][" + (i+1) + "/" + users.size() + "]");
					
					db.insertUsers(users.get(i));
					db.insertUsersNotHavingManyFriends(users.get(i).getUserId().toString(), users.get(i).getUserLocation().toString());
		*/
					/*	
					ArrayList<Friend> friendList = db.getFriendList(users.get(i).getUserId());
					
					if(friendList.size() >= 10){
						//�켱, ������ �̹� ģ���� ���� �ֵ� ����Ʈ�� ����
						users.get(i).setFriendList(friendList);
						existingUserList.add(users.get(i));
						
						//MySQL DB�� ��� User�� �����ϴ� ���̺��� �ش� ����� �ֱ�
						db.insertUsers(users.get(i));
						//MySQL DB�� ģ���� ���� User�鸸 �����ϴ� ���̺��� �ش� ����� �ֱ�
						db.insertUsersHavingManyFriends(users.get(i).getUserId());
						
						log.info("[" + (i+1) + "/" + users.size() + "] User id : " + users.get(i).getUserId());
						for(int j=0; j<friendList.size(); j++){
							//�� ����ڵ��� ģ���鵵 ��� User�� �����ϴ� ���̺��� �ֱ�
							db.insertUsers(friendList.get(j));
							//ģ���鸸 Insert
							db.insertFriends(friendList.get(j), users.get(i).getUserId());
							log.debug(" - friend id : " + friendList.get(j).getUserId() 
										+ ", isComplete : " + friendList.get(j).getIsCompleteUserId()
										+ ", isFollower : " + friendList.get(j).getIsFollower()
										+ ", followingPortion : " + friendList.get(j).getFollowingPortion()
										+ ", followerPortion : " + friendList.get(j).getFollowerPortion());
						}
					
					// �ϴ� ģ���� ���ų�, ���� ���� (10�� �̸�) �� �����鵵 complete user id�� ������ֱ� ����
					// users ���̺��� �ִ´�.
					} else {
						db.insertUsers(users.get(i));
						db.insertUsersNotHavingManyFriends(users.get(i).getUserId().toString(), users.get(i).getUserLocation().toString());
					}
					*/
		
					
		/*		}
				
				
				//logic2 - ģ���� ����Ʈ �߿���, ģ���� 10�� �̻��� User �׷쿡 ������ �Ǵ� ����/������ �ȵǴ� ����
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
			
				
				//logic 3-1 - ���� Location �����ؼ�, ���� ���� ����Ʈ���� ģ���� 10 ������ �ֵ� ģ�� ����� �ֱ� (SocialLevelPerUser ���̺��� �� ģ���� ������ֱ�)
				System.out.println("logic 3-1 =============================================================");
				//�ϴ� FriendsNotInHeavy�� �����ϴ� �ֵ� �� �ҷ�����
				ArrayList<User> originUserList = new ArrayList<User>();
			//	userList = db.getAllFriendsNotInHeavy();
				originUserList = db.getUsersNotHavingManyFriends();
				
				for(int i=0; i<originUserList.size(); i++){
					System.out.println("[LOGIC 3-1][" + (i+1) + "/" + originUserList.size() + "]");
					
					//FriendsNotInHeavy�� ���ԵǴ� �ֵ� ��ο���, Users ���̺����� �̾Ƽ� ģ�� 20���� ���������
					String userLocation = originUserList.get(i).getUserLocation();
					int numUsersAtSamePlace = 5;
					int numUsersAtOtherPlace = 15;
					
					//5���� ���� Location
					ArrayList<User> samePlaceUsers = db.getUsersSameLocation(userLocation, numUsersAtSamePlace);
					
					//15���� �ٸ� Location
					ArrayList<User> otherPlaceUsers = db.getUsersOtherLocation(userLocation, numUsersAtOtherPlace);
					
					//insert into addedSocialLevelPerUser table
					db.insertIntoAddedSocialLevelPerUser(originUserList.get(i), samePlaceUsers, otherPlaceUsers);
				}
				
				
				//logic3-2 - ���� Location �����ؼ�, ģ���� ����Ʈ �߿��� ģ���� 10 ������ �ֵ� ģ�� ����� �ֱ� (SocialLevelPerUser ���̺��� �� ģ���� ������ֱ�)
				System.out.println("logic 3-2 =============================================================");
				//�ϴ� FriendsNotInHeavy�� �����ϴ� �ֵ� �� �ҷ�����
				ArrayList<User> userList = new ArrayList<User>();
				userList = db.getAllFriendsNotInHeavy();
				
				for(int i=0; i<userList.size(); i++){
					System.out.println("[LOGIC 3-2][" + (i+1) + "/" + userList.size() + "]");
					
					//FriendsNotInHeavy�� ���ԵǴ� �ֵ� ��ο���, Users ���̺����� �̾Ƽ� ģ�� 20���� ���������
					String userLocation = userList.get(i).getUserLocation();
					int numUsersAtSamePlace = 5;
					int numUsersAtOtherPlace = 15;
					
					//5���� ���� Location
					ArrayList<User> samePlaceUsers = db.getUsersSameLocation(userLocation, numUsersAtSamePlace);
					
					//15���� �ٸ� Location
					ArrayList<User> otherPlaceUsers = db.getUsersOtherLocation(userLocation, numUsersAtOtherPlace);
					
					//insert into addedSocialLevelPerUser table
					db.insertIntoAddedSocialLevelPerUser(userList.get(i), samePlaceUsers, otherPlaceUsers);
				}
				
				
				//logic4 - ������ SocialLevelPerUser ���̺� ������ֱ� (���� ������ + ���� ���������)
				//Portion�� �����ϰ� �־��ֱ�
				System.out.println("logic 4 =============================================================");
				for(int i=0; i<existingUserList.size(); i++){
					System.out.println("[LOGIC 4-1][" + (i+1) + "/" + existingUserList.size() + "]");
					
					db.insertIntoSocialLevelPerUser(existingUserList.get(i));
				}
				
				
				//���⼭ ���ο� �ֵ� �־������
				ArrayList<UserLink> addedUserLinkList = new ArrayList<UserLink>();
				addedUserLinkList = db.getAddedSocialLevelPerUser();
				for(int i=0; i<addedUserLinkList.size(); i++){
					System.out.println("[LOGIC 4-2][" + (i+1) + "/" + addedUserLinkList.size() + "]");
					
					db.insertAddedUsersIntoSocialLevelPerUser(addedUserLinkList.get(i));
				}
				
		*/		
				//logic5 - ���� ������� SocialLevelPerUser�� ���� UserLink ���̺� ������ֱ�
//				System.out.println("logic 5 =============================================================");
//				int totalWriteMyself = 0;
//				int totalWriteOthers = 0;
//				ArrayList<User> finalUserList = new ArrayList<User>();
//				finalUserList = db.getUsers();
//				for(int i=0; i<finalUserList.size(); i++){
//					System.out.print("[LOGIC 5][" + (i+1) + "/" + finalUserList.size() + "]");
//					totalWriteMyself = db.getTotalNumWriteOfMyself(finalUserList.get(i).getUserId());
//					totalWriteOthers = db.getTotalNumWriteOfFriends(finalUserList.get(i).getUserId());
//					System.out.print(" total write to myself : " + totalWriteMyself);
//					System.out.println(", total write to others : " + totalWriteOthers);
//					
//					//insert TweetMyself
//					db.insertTweetMySelf(finalUserList.get(i).getUserId(), totalWriteMyself);
//					
//					//get list of friends (user id, portion) from social level per user table 
//					ArrayList<UserLink> userLinkInSocialLevelPerUser = db.getFriendInSocialLevelPerUser(finalUserList.get(i).getUserId());
//					for(int j=0; j<userLinkInSocialLevelPerUser.size(); j++){
//						String friendId = userLinkInSocialLevelPerUser.get(j).getDestinationName().toString();
//						double portion = userLinkInSocialLevelPerUser.get(j).getPortion();
//						int numTweetToFriend = (int)(totalWriteOthers * portion);
//						db.insertTweetOther(finalUserList.get(i).getUserId(), friendId, numTweetToFriend);
//					}
//				}
				
		/*		
				//logic6 - ���� �������� ���迡 ���� completeUserId ���̺� ������ֱ�
				System.out.println("logic 6 =============================================================");
				ArrayList<User> finalUserList = new ArrayList<User>();
				finalUserList = db.getUsers();
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
				
				
				System.out.println("logic 7 =============================================================");
				/////////////MFRatio ���̺� �������� ��
				/////////////MFRatio �� �����Ͱ� ������ �״�� ����ϰ�
				/////////////MFRatio �� �����Ͱ� ������ ������ �ִ� �� �߿� �ϳ��� �����ϰ� �̾Ƽ� Insert �Ѵ�
				finalUserList = new ArrayList<User>();
				finalUserList = db.getFinalUsers();
				MFRatio mfRatio = new MFRatio();
				for(int i=0; i<finalUserList.size(); i++){
					System.out.println("[LOGIC 7][" + (i+1) + "/" + finalUserList.size() + "]");
					
					// db.83�� �ִ� ����� MFRatio�� ���� ������ ������ �´�
					mfRatio = new MFRatio();
					mfRatio = db.getMFRatio(finalUserList.get(i).getUserId());
					if(mfRatio == null){
						
						//���࿡ ���� ���� ������ �ű� �ִ� �� �� ������ ���� ������ �´�
						mfRatio = db.getRandMFRatio();
						db.insertRandMFRatio(finalUserList.get(i), mfRatio);
						
					} else {
						
						//���� ������ �װ� �̿��ؼ� MFRatio ���̺��� Insert
						db.insertMFRatio(finalUserList.get(i), mfRatio);
					}
					
				}
		*/		
				
				System.out.println("logic 8 =============================================================");
				////////////UserProperty ���̺� ������ֱ�
				ArrayList<User> finalUserList = new ArrayList<User>();
				finalUserList = db.getFinalUsers();
				ArrayList<UserProperty> propertyList = new ArrayList<UserProperty>();
				UserProperty eachUserProperty = new UserProperty();
				
				for(int i=0; i<finalUserList.size(); i++){
					System.out.println("[LOGIC 8][" + (i+1) + "/" + finalUserList.size() + "]");
					
					eachUserProperty = new UserProperty();
					// mysql�� newUserproperty ���̺��� ������ �״�� �����ͼ� �ְ�
					propertyList = db.getUserProperty(finalUserList.get(i).getUserId().toString());
					if(propertyList.size() == 0) {
						// ������, ���Ƿ� �ϳ� ��� �ִ´�.
						propertyList = db.getRandUserProperty();
						db.insertRandUserProperty(finalUserList.get(i), propertyList);
						
					} else {
						
						//���� ������ �װ� �̿��ؼ� MFRatio ���̺��� Insert
						db.insertUserProperty(finalUserList.get(i), propertyList);
					}
				}
				
				//SocialLevelPerUser ���̺����� SourceName�̶� DestinationName �̶� ���� Row�� ����
				//SocialLevelPerUser ���̺��� SourceName �ε���
				
				System.out.println("=====================================================================");
				System.out.println("END Data Analyzer");
				
				//DB Disconnection
				db.disconnect();
				db.mySqlDisconnect();
	}
}