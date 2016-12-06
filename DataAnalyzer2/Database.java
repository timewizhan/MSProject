import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

public class Database {
	
	static Logger log = Logger.getLogger(NewDataAnalyzer.class.getName());
	
	Connection connection = null;
	Statement st = null;
	ResultSet rs = null;
	
	Connection mySqlConn = null;
	
	void mySqlConnect(){

		String port = "3306";
		String dbName = "dataAnalyzer";
		String id = "root";
		String password = "cclab";
		
		try{
			Class.forName("com.mysql.jdbc.Driver");
			mySqlConn = DriverManager.getConnection("jdbc:mysql://localhost:" + port + "/" + dbName + "?autoReconnect=true&useSSL=false", id, password);
		//	log.info("# CONNECT EP DATABASE SESSION");
		}catch(ClassNotFoundException cnfe){
			cnfe.printStackTrace();
		}catch(SQLException se){
			se.printStackTrace();
		}
	}
	
	public void mySqlDisconnect(){
		
		try {
			mySqlConn.close();
		//	log.info("# DISCONNECT EP DATABASE SESSION");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	ArrayList<User> getUserListFromUserProperty() {
		ArrayList<User> users = new ArrayList<User>();
		User eachUser = new User();
		
		try {
			st = mySqlConn.createStatement();
			rs = st.executeQuery("SELECT userName, userPlace, COUNT(userName) AS num "
					            + "FROM UserProperty GROUP BY userName");
			
			while(rs.next()){
				eachUser = new User();
				
				String userId = rs.getString("userName");
				String userPlace = rs.getString("userPlace");
				int numOfTweet = Integer.parseInt(rs.getString("num"));
				
				eachUser.setUserId(userId);
				eachUser.setUserLocation(userPlace);
				eachUser.setNumOfTweet(numOfTweet);
				
				users.add(eachUser);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return users;
	}
	
	ArrayList<UserProperty> getUserProperties(String userID) {
		ArrayList<UserProperty> properties = new ArrayList<UserProperty>();
		UserProperty eachProperty = new UserProperty();
		
		try {
			st = mySqlConn.createStatement();
			rs = st.executeQuery("SELECT TweetTime, UserName, TweetPlace, UserPlace, TweetID "
					            + "FROM UserProperty WHERE UserName = '" + userID + "'");
			
			while(rs.next()){
				eachProperty = new UserProperty();
				
				String tweetTime = rs.getString("TweetTime");
				String userName = rs.getString("UserName");
				String tweetPlace = rs.getString("TweetPlace");
				String userPlace = rs.getString("UserPlace");
				String tweetID = rs.getString("TweetID");
				
				if(userID.equals(userName)){
					eachProperty.setTweetTime(tweetTime);
					eachProperty.setUserName(userName);
					eachProperty.setTweetPlace(tweetPlace);
					eachProperty.setUserPlace(userPlace);
					eachProperty.setTweetID(tweetID);
					
					properties.add(eachProperty);
				
				} else {
					System.out.println("UserID is different with UserName! It's an error! ");
				}
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return properties;
	}
	
	void insertNewUserProperty(ArrayList<UserProperty> properties) {
		
		for(int i=0; i<properties.size(); i++){
			
			Statement stmt = null;
			
			try {
				stmt = mySqlConn.createStatement();
				stmt.executeUpdate("INSERT INTO NewUserProperty VALUES('" + properties.get(i).getTweetTime() + "', '" 
																		+ properties.get(i).getUserName() + "', '" 
																		+ properties.get(i).getTweetPlace() + "', '"
																		+ properties.get(i).getUserPlace() + "', '"
																		+ properties.get(i).getTweetID() + "')");
				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	void insertUsers(User user){
		
		Statement stmt = null;
		
		try {
			stmt = mySqlConn.createStatement();
			stmt.executeUpdate("INSERT INTO Users VALUES('" + user.getUserId() + "', '" + user.getUserLocation() + "')");
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	ArrayList<User> getUsersSameLocation(String location, int numUsersSamePlace){

		Statement stmt = null;
		ArrayList<User> userList = new ArrayList<User>();
		User eachUser = new User();
		
		try {
			stmt = mySqlConn.createStatement();
			ResultSet resSet = stmt.executeQuery("select uid, location from Users "
													+ "where location ='" + location + "' "
													+ "order by rand() limit " + numUsersSamePlace);
			
			while(resSet.next()){
				eachUser = new User();
				eachUser.setUserId(resSet.getString("uid"));
				eachUser.setUserLocation(resSet.getString("location"));
				userList.add(eachUser);
			}
			
			resSet.close();
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return userList;
	}
	
	ArrayList<User> getUsersOtherLocation(String location, int numUsersOtherPlace){

		Statement stmt = null;
		ArrayList<User> userList = new ArrayList<User>();
		User eachUser = new User();
		
		try {
			stmt = mySqlConn.createStatement();
			ResultSet resSet = stmt.executeQuery("select uid, location from Users "
													+ "where location not in ('" + location + "') "
													+ "order by rand() limit " + numUsersOtherPlace);
			
			while(resSet.next()){
				eachUser = new User();
				eachUser.setUserId(resSet.getString("uid"));
				eachUser.setUserLocation(resSet.getString("location"));
				userList.add(eachUser);
			}
			
			resSet.close();
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return userList;
	}
	
	ArrayList<UserLink> getAddedSocialLevelPerUser(){

		ArrayList<UserLink> addedUserLinks = new ArrayList<UserLink>();
		UserLink eachLink = null;
		Statement stmt = null;

		try {
			stmt = mySqlConn.createStatement();
			ResultSet resSet = stmt.executeQuery("SELECT sourceName, destinationName, portion FROM AddedSocialLevelPerUser;");
			
			while(resSet.next()){
				eachLink = new UserLink();
				
				String srcName = resSet.getString("sourceName");
				String dstName = resSet.getString("destinationName");
				double portion = Double.parseDouble(resSet.getString("portion"));
				
				eachLink.setSourceName(srcName);
				eachLink.setDestinationName(dstName);
				eachLink.setPortion(portion);
				
				addedUserLinks.add(eachLink);
			}
			
			resSet.close();
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return addedUserLinks;
	}
	
	ArrayList<UserLink> getFriendInSocialLevelPerUser(String sourceName){

		ArrayList<UserLink> addedUserLinks = new ArrayList<UserLink>();
		UserLink eachLink = null;
		Statement stmt = null;

		try {
			stmt = mySqlConn.createStatement();
			ResultSet resSet = stmt.executeQuery("SELECT destinationName, portion " 
												+ "FROM SocialLevelPerUser " 
												+ "WHERE sourceName = '" + sourceName + "';");
			
			while(resSet.next()){
				eachLink = new UserLink();
				
				String dstName = resSet.getString("destinationName");
				double portion = Double.parseDouble(resSet.getString("portion"));
				
				eachLink.setDestinationName(dstName);
				eachLink.setPortion(portion);
				
				addedUserLinks.add(eachLink);
			}
			
			resSet.close();
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return addedUserLinks;
	}
	
	void createCompleteUserIdTable(String userId, String userLocation, int classifier){
		
		Statement stmt = null;

		try {
			stmt = mySqlConn.createStatement();
			stmt.executeUpdate("INSERT INTO CompleteUserId (userName, userPlace, classifier) " 
								+ "VALUES('" 
								+ userId + "', '" 
								+ userLocation + "', " 
								+ classifier + ") ");
			
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void insertIntoSocialLevelPerUser(User existingUser){
		
		Statement stmt = null;
		
		for(int i=0; i<existingUser.getFriendList().size(); i++){
			
			try {
				stmt = mySqlConn.createStatement();
				stmt.executeUpdate("INSERT INTO SocialLevelPerUser (sourceName, destinationName, portion) " 
									+ "VALUES('" 
									+ existingUser.getUserId().toString() + "', '" 
									+ existingUser.getFriendList().get(i).getUserId().toString() + "', " 
									+ existingUser.getFriendList().get(i).getFollowingPortion() + ") ");
				
				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	void insertAddedUsersIntoSocialLevelPerUser(UserLink addedUserLink){
		
		Statement stmt = null;
			
		try {
			stmt = mySqlConn.createStatement();
			stmt.executeUpdate("INSERT INTO SocialLevelPerUser (sourceName, destinationName, portion) " 
								+ "VALUES('" 
								+ addedUserLink.getSourceName().toString() + "', '" 
								+ addedUserLink.getDestinationName().toString() + "', " 
								+ addedUserLink.getPortion() + ") ");
			
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void insertIntoAddedSocialLevelPerUser(User user, ArrayList<User> samePlaceUsers, ArrayList<User> otherPlaceUsers){
		
		Statement stmt = null;
		double portion = 0.0;
		double samePlaceUsersSize = samePlaceUsers.size();
		double otherPlaceUsersSize = otherPlaceUsers.size();
		double totalSize = samePlaceUsersSize + otherPlaceUsersSize;
	//	portion = 1 / (samePlaceUsersSize + otherPlaceUsersSize);
		
		if(totalSize < 10 || totalSize > 20){	
			log.error("There's no case of TOTAL SIZE = " + totalSize);
		}
		
		Portion portions = new Portion();
		ArrayList<Double> portionList = portions.getPortionList(totalSize);
		int portionCnt = 0;
		
		for(int i=0; i<samePlaceUsers.size(); i++){
			try {
				stmt = mySqlConn.createStatement();
				stmt.executeUpdate("INSERT INTO addedSocialLevelPerUser (sourceName, sourceLocation, destinationName, destinationLocation, portion) " 
									+ " VALUES('" 
									+ user.getUserId().toString() + "', '" 
									+ user.getUserLocation().toString() + "', '"
									+ samePlaceUsers.get(i).getUserId().toString() + "', '" 
									+ samePlaceUsers.get(i).getUserLocation().toString() + "', "
									+ portionList.get(portionCnt).doubleValue() + ") ");
				
				portionCnt++;
				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		for(int i=0; i<otherPlaceUsers.size(); i++){
			try {
				stmt = mySqlConn.createStatement();
				stmt.executeUpdate("INSERT INTO addedSocialLevelPerUser (sourceName, sourceLocation, destinationName, destinationLocation, portion) " 
									+ " VALUES('" 
									+ user.getUserId().toString() + "', '" 
									+ user.getUserLocation().toString() + "', '"
									+ otherPlaceUsers.get(i).getUserId().toString() + "', '" 
									+ otherPlaceUsers.get(i).getUserLocation().toString() + "', "
									+ portionList.get(portionCnt).doubleValue() + ") ");
				
				portionCnt++;
				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	void insertFriendsInHeavy(String user){
		
		boolean isDuplicated = checkDuplicatedinFriendsInHeavy(user);
		
		if(!isDuplicated){
		
			Statement stmt = null;
		
			try {
				stmt = mySqlConn.createStatement();
				stmt.executeUpdate("INSERT INTO FriendsInHeavy VALUES('" + user + "') ");
				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	void insertTweetMySelf(String userId, int totalWriteMyself){
		
		Statement stmt = null;
	
		try {
			stmt = mySqlConn.createStatement();
			stmt.executeUpdate("INSERT INTO UserLink(SourceName, DestinationName, Friend, Follow, TweetMyself, TweetOther, Retweet) "
								+ "VALUES('" + userId + "', '" + userId + "', " + true + ", " + true + ", " + totalWriteMyself + ", 0, 0)");
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void insertTweetOther(String sourceName, String destinationName, int numWriteToOther){
		
		Statement stmt = null;
		
		try {
			stmt = mySqlConn.createStatement();
			stmt.executeUpdate("INSERT INTO UserLink(SourceName, DestinationName, Friend, Follow, TweetMyself, TweetOther, Retweet) "
								+ "VALUES('" + sourceName + "', '" + destinationName + "', " + true + ", " + true + ", 0, " + numWriteToOther + ", 0)");
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void insertFriendsNotInHeavy(Friend user){
		
		boolean isDuplicated = checkDuplicatedinFriendsNotInHeavy(user.getUserId().toString());
		
		if(!isDuplicated){
		
			Statement stmt = null;
		
			try {
				stmt = mySqlConn.createStatement();
				stmt.executeUpdate("INSERT INTO FriendsNotInHeavy (uid, location) VALUES('" + user.getUserId() + "', '" + user.getLocation() + "') ");
				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	boolean checkDuplicatedinFriendsInHeavy(String user) {
		
		boolean isDuplicated = false;
		Statement stmt = null;

		try {
			stmt = mySqlConn.createStatement();
			ResultSet resSet = stmt.executeQuery("select * from FriendsInHeavy where uid = '" + user + "';");

			if(resSet.next()){
				isDuplicated = true;
			}

			resSet.close();
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return isDuplicated;
	}

	boolean checkDuplicatedinFriendsNotInHeavy(String user) {
		
		boolean isDuplicated = false;
		Statement stmt = null;
	
		try {
			stmt = mySqlConn.createStatement();
			ResultSet resSet = stmt.executeQuery("select * from FriendsNotInHeavy where uid = '" + user + "';");
	
			if(resSet.next()){
				isDuplicated = true;
			}
	
			resSet.close();
			stmt.close();
	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return isDuplicated;
	}
	
	void testMySqlQuery(){
		Statement mySqlState = null;
		ResultSet mySqlRs = null;
		
		try {
			mySqlState = mySqlConn.createStatement();
			mySqlRs = mySqlState.executeQuery("SELECT VERSION()");
			
			if(mySqlRs.next()){
				System.out.println(mySqlRs.getString(1));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	void connect(){
		try {
			connection = DriverManager.getConnection("jdbc:postgresql://165.132.123.83:5432/import","postgres","1111");
		}catch (SQLException e){
			System.out.println("Connection failed!");
			e.printStackTrace();
			return;
		}
	}
	
	void disconnect(){
		try{
			if(rs != null){
				rs.close();
			}
			if(st != null){
				st.close();
			}
			if(connection != null){
				connection.close();
			}
		}catch(SQLException ex){
			ex.printStackTrace();
		}
	}
	
	void testQuery(){
		try {
			st = connection.createStatement();
			rs = st.executeQuery("SELECT VERSION()");
			
			if(rs.next()){
				System.out.println(rs.getString(1));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	ArrayList<User> getCompletedUserIdList(){
		
		ArrayList<User> users = new ArrayList<User>();
		User eachUser = new User();
		
		try {
			st = connection.createStatement();
			rs = st.executeQuery("SELECT \"userName\", \"userPlace\", classifier "
					            + "FROM public.\"completeUserid\"");
			
			while(rs.next()){
				eachUser = new User();
				
				String userId = rs.getString("userName");
				String userPlace = rs.getString("userPlace");
				int classifier = Integer.parseInt(rs.getString("classifier"));
				
				eachUser.setUserId(userId);
				eachUser.setUserLocation(userPlace);
				eachUser.setClassifier(classifier);
				
				users.add(eachUser);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return users;
	}
	
	ArrayList<Friend> getFriendList(String userId){
		
		ArrayList<Friend> friendList = new ArrayList<Friend>();
		Friend eachFriend = new Friend();
		try {
			st = connection.createStatement();
			rs = st.executeQuery("SELECT \"SourceName\", \"DestinationName\", \"Portion\" "
					            + "FROM public.\"SocialLevelPerUser\" "
					            + "WHERE \"SourceName\" = '" + userId + "'");
			
			while(rs.next()){
				eachFriend = new Friend();
				
				String friendId = rs.getString("DestinationName");
				double followingPortion = Double.parseDouble(rs.getString("Portion"));
				
				eachFriend.setUserId(friendId);
				eachFriend.setFollowingPortion(followingPortion);
				
				boolean isComplete = isCompleteUserId(friendId);
				boolean isFollow = isFollower(friendId, userId);
				double followerPortion = getFollowerPortion(friendId, userId);
				
				eachFriend.setIsCompleteUserId(isComplete);
				eachFriend.setIsFollower(isFollow);
				eachFriend.setFollowerPortion(followerPortion);
				
				String location = getFriendLocation(friendId);
				eachFriend.setLocation(location);
				
				friendList.add(eachFriend);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
		
		return friendList;
	}
	
	String getFriendLocation(String userId){
		String location = null;
		Statement state = null;
		ResultSet res = null;
		
		try {
			state = connection.createStatement();
			res = state.executeQuery("SELECT \"userName\", \"userPlace\", classifier "
					            + "FROM public.\"completeUserid\" "
								+ "WHERE \"userName\" = '" + userId + "'");
			
			while(res.next()){
				location = res.getString("userPlace");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				res.close();
				state.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return location;
	}
	
	int getTotalNumWriteOfMyself(String userId){
	
		int totalWriteNumOfMyself = 0;
		Statement state = null;
		ResultSet res = null;
		
		try {
			state = connection.createStatement();
			res = state.executeQuery("SELECT count(*) "
					            + "FROM public.\"UserProperty\" "
								+ "WHERE \"UserName\" = '" + userId + "' AND \"ToUser\" = '" + userId + "'");
			
			if(res.next()){
				totalWriteNumOfMyself = Integer.parseInt(res.getString("count"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				res.close();
				state.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return totalWriteNumOfMyself;
	}
	
	int getTotalNumWriteOfFriends(String userId){
		int totalWriteNumOfFriends = 0;
		Statement state = null;
		ResultSet res = null;
		
		try {
			state = connection.createStatement();
			res = state.executeQuery("SELECT count(*) "
					            + "FROM public.\"UserProperty\" "
								+ "WHERE \"UserName\" = '" + userId + "' AND \"ToUser\" != '" + userId + "'");
			
			if(res.next()){
				totalWriteNumOfFriends = Integer.parseInt(res.getString("count"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				res.close();
				state.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return totalWriteNumOfFriends;
	}
	
	boolean isCompleteUserId(String friendId){
		boolean isCompleteUserId = false;
		Statement state = null;
		ResultSet res = null;
		
		try {
			state = connection.createStatement();
			res = state.executeQuery("SELECT \"userName\", \"userPlace\", classifier "
		            + "FROM public.\"completeUserid\""
		            + "WHERE \"userName\" = '" + friendId + "'");
			
			if(res.next()){
				isCompleteUserId = true;
			} else {
				isCompleteUserId = false;
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				res.close();
				state.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return isCompleteUserId;
	}
	
	boolean isFollower(String friendId, String userId){
		boolean isFollow = false;
		Statement state = null;
		ResultSet res = null;
		
		try {
			state = connection.createStatement();
			res = state.executeQuery("SELECT \"SourceName\", \"DestinationName\", \"Portion\" "
					            + "FROM public.\"SocialLevelPerUser\" "
					            + "WHERE \"SourceName\" = '" + friendId + "' "
					            		+ "AND \"DestinationName\" = '" + userId +"'");
			
			if(res.next()){
				isFollow = true;
			} else {
				isFollow = false;
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				res.close();
				state.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return isFollow;
	}
	
	double getFollowerPortion(String friendId, String userId){
		double followerPortion = 0.0;
		Statement state = null;
		ResultSet res = null;
		
		try {
			state = connection.createStatement();
			res = state.executeQuery("SELECT \"SourceName\", \"DestinationName\", \"Portion\" "
		            + "FROM public.\"SocialLevelPerUser\" "
		            + "WHERE \"SourceName\" = '" + friendId + "' "
		            		+ "AND \"DestinationName\" = '" + userId +"'");
			
			while(res.next()){
				followerPortion = Double.parseDouble(rs.getString("Portion"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				res.close();
				state.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return followerPortion;
	}
}
