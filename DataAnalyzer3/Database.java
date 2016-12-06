import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

public class Database {
	
	static Logger log = Logger.getLogger(NewNewDataAnalyzer.class.getName());
	
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
	
	void insertUsers(User user){
		
		boolean isDuplicated = checkDuplicatedinUsers(user.getUserId().toString());
		
		if(!isDuplicated){
			
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
	}
	
	void insertUsers(Friend user){
		
		boolean isDuplicated = checkDuplicatedinUsers(user.getUserId().toString());
		
		if(!isDuplicated){
			
			Statement stmt = null;
			
			try {
				stmt = mySqlConn.createStatement();
				stmt.executeUpdate("INSERT INTO Users VALUES('" + user.getUserId() + "', '" + user.getLocation() + "')");
				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	void insertUsersHavingManyFriends(String user){
		
		boolean isDuplicated = checkDuplicatedinUsersHavingMany(user);
		
		if(!isDuplicated){
		
			Statement stmt = null;
		
			try {
				stmt = mySqlConn.createStatement();
				stmt.executeUpdate("INSERT INTO UsersHavingManyFriends VALUES('" + user + "') ");
				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	void insertUsersNotHavingManyFriends(String user, String location){
		
		boolean isDuplicated = checkDuplicatedinUsersNotHavingMany(user);
		
		if(!isDuplicated){
		
			Statement stmt = null;
		
			try {
				stmt = mySqlConn.createStatement();
				stmt.executeUpdate("INSERT INTO UsersNotHavingManyFriends VALUES('" + user + "', '" + location + "') ");
				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	void insertFriends(Friend user, String friendOf){
		
		boolean isDuplicated = checkDuplicatedinFriends(user.getUserId().toString());
		
		if(!isDuplicated){
		
			Statement stmt = null;
		
			try {
				stmt = mySqlConn.createStatement();
				stmt.executeUpdate("INSERT INTO Friends (uid, isComplete, friendOf, isFollower, followingPortion, followerPortion, location) VALUES('" 
														+ user.getUserId() + "', "  
														+ user.getIsCompleteUserId()  + ", '"
														+ friendOf + "', "
														+ user.getIsFollower() + ", "
														+ user.getFollowingPortion() + ", "
														+ user.getFollowerPortion() + ", '"
														+ user.getLocation() + "') ");
				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	boolean checkDuplicatedinFriends(String user) {
		
		boolean isDuplicated = false;
		Statement stmt = null;

		try {
			stmt = mySqlConn.createStatement();
			ResultSet resSet = stmt.executeQuery("select * from Friends where uid = '" + user + "';");

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
	
	boolean checkDuplicatedinUsers(String user) {
	
		boolean isDuplicated = false;
		Statement stmt = null;

		try {
			stmt = mySqlConn.createStatement();
			ResultSet resSet = stmt.executeQuery("select * from Users where uid = '" + user + "';");

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
	
	boolean checkDuplicatedinUsersHavingMany(String user) {
		
		boolean isDuplicated = false;
		Statement stmt = null;

		try {
			stmt = mySqlConn.createStatement();
			ResultSet resSet = stmt.executeQuery("select * from UsersHavingManyFriends where uid = '" + user + "';");

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
	
	boolean checkDuplicatedinUsersNotHavingMany(String user) {
		
		boolean isDuplicated = false;
		Statement stmt = null;

		try {
			stmt = mySqlConn.createStatement();
			ResultSet resSet = stmt.executeQuery("select * from UsersNotHavingManyFriends where uid = '" + user + "';");

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
	
	ArrayList<Friend> getAllTheFriends(){

		ArrayList<Friend> allFriends = new ArrayList<Friend>();
		Friend eachFriend = null;
		Statement stmt = null;

		try {
			stmt = mySqlConn.createStatement();
			ResultSet resSet = stmt.executeQuery("select uid, location from Friends;");
			
			while(resSet.next()){
				eachFriend = new Friend();
				
				String userId = resSet.getString("uid");
				String location = resSet.getString("location");
				
				eachFriend.setUserId(userId);
				eachFriend.setLocation(location);
				
				allFriends.add(eachFriend);
			}
			
			resSet.close();
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return allFriends;
	}
	
	ArrayList<User> getUsersNotHavingManyFriends(){
		ArrayList<User> usersNotHavingManyFriends = new ArrayList<User>();
		
		User eachUser = null;
		Statement stmt = null;

		try {
			stmt = mySqlConn.createStatement();
			ResultSet resSet = stmt.executeQuery("select uid, location from UsersNotHavingManyFriends;");
			
			while(resSet.next()){
				eachUser = new User();
				
				String userId = resSet.getString("uid");
				String location = resSet.getString("location");
				
				eachUser.setUserId(userId);
				eachUser.setUserLocation(location);
				
				usersNotHavingManyFriends.add(eachUser);
			}
			
			resSet.close();
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return usersNotHavingManyFriends;
	}
	
	ArrayList<User> getAllFriendsNotInHeavy(){

		ArrayList<User> allFriends = new ArrayList<User>();
		User eachUser = null;
		Statement stmt = null;

		try {
			stmt = mySqlConn.createStatement();
			ResultSet resSet = stmt.executeQuery("select uid, location from FriendsNotInHeavy;");
			
			while(resSet.next()){
				eachUser = new User();
				
				String userId = resSet.getString("uid");
				String location = resSet.getString("location");
				
				eachUser.setUserId(userId);
				eachUser.setUserLocation(location);
				
				allFriends.add(eachUser);
			}
			
			resSet.close();
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return allFriends;
	}	
	
	boolean findInHeavyUsers(String users){

		boolean isInHeavyUsers = false;
		Statement stmt = null;

		try {
			stmt = mySqlConn.createStatement();
			ResultSet resSet = stmt.executeQuery("select uid from UsersHavingManyFriends "
													+ "where uid ='" + users + "'");
			
			if(resSet.next()){
				isInHeavyUsers = true;
			}
			
			resSet.close();
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return isInHeavyUsers;
	}
	
	ArrayList<User> getUsers(){

		Statement stmt = null;
		ArrayList<User> userList = new ArrayList<User>();
		User eachUser = new User();
		
		try {
			stmt = mySqlConn.createStatement();
			ResultSet resSet = stmt.executeQuery("select uid, location from Users");
			
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
	
	ArrayList<User> getFinalUsers(){

		Statement stmt = null;
		ArrayList<User> userList = new ArrayList<User>();
		User eachUser = new User();
		
		try {
			stmt = mySqlConn.createStatement();
			ResultSet resSet = stmt.executeQuery("select userName, userPlace, classifier from CompleteUserId");
			
			while(resSet.next()){
				eachUser = new User();
				eachUser.setUserId(resSet.getString("userName"));
				eachUser.setUserLocation(resSet.getString("userPlace"));
				eachUser.setClassifier(Integer.parseInt(resSet.getString("classifier")));
			
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
	
	ArrayList<UserProperty> getRandUserProperty(){

		Statement stmt = null;
		ArrayList<UserProperty> propertyList = new ArrayList<UserProperty>();
		UserProperty eachUserProperty = new UserProperty();
		
		try {
			stmt = mySqlConn.createStatement();
			ResultSet resSet = stmt.executeQuery("select TweetTime, UserName, TweetPlace, UserPlace, TweetID from newUserProperty "
													+ "order by rand() limit 1");
			
			if(resSet.next()){
				eachUserProperty = new UserProperty();
				
				eachUserProperty.setTweetTime(resSet.getString("TweetTime"));
				eachUserProperty.setUserName(resSet.getString("UserName"));
				eachUserProperty.setTweetPlace(resSet.getString("TweetPlace"));
				eachUserProperty.setUserPlace(resSet.getString("UserPlace"));
				eachUserProperty.setTweetID(resSet.getString("TweetID"));
				
				propertyList = getUserProperty(resSet.getString("UserName").toString());
			}
			
			resSet.close();
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return propertyList;
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
	
	void insertMFRatio(User eachUser, MFRatio mfRatio){
		
		Statement stmt = null;

		try {
			stmt = mySqlConn.createStatement();
			stmt.executeUpdate("INSERT INTO MFRatio (SourceName, MyselfRatio, FriendRatio) " 
								+ "VALUES('" 
								+ eachUser.getUserId().toString() + "', '" 
								+ mfRatio.getMyselfRatio().toString() + "', " 
								+ mfRatio.getFriendRatio().toString() + ") ");
			
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void insertRandMFRatio(User eachUser, MFRatio mfRatio){
		
		Statement stmt = null;

		try {
			stmt = mySqlConn.createStatement();
			stmt.executeUpdate("INSERT INTO MFRatio (SourceName, MyselfRatio, FriendRatio) " 
								+ "VALUES('" 
								+ eachUser.getUserId() + "', '" 
								+ mfRatio.getMyselfRatio().toString() + "', " 
								+ mfRatio.getFriendRatio().toString() + ") ");
			
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void insertRandUserProperty(User eachUser, ArrayList<UserProperty> propertyList){
		
		Statement stmt = null;
	//	UserProperty eachUserProperty = new UserProperty();
		
		for(int i=0; i<propertyList.size(); i++){
			
			try {
				stmt = mySqlConn.createStatement();
				stmt.executeUpdate("INSERT INTO madeUserProperty (TweetTime, UserName, TweetPlace, UserPlace, TweetID) " 
									+ "VALUES('" 
									+ propertyList.get(i).getTweetTime() + "', '" 
									+ eachUser.getUserId().toString() + "', '"
									+ propertyList.get(i).getTweetPlace().toString() + "', '"
									+ propertyList.get(i).getUserPlace().toString() + "', '"
									+ propertyList.get(i).getTweetID().toString() + "') ");
				
				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	void insertUserProperty(User eachUser, ArrayList<UserProperty> propertyList){
		
		Statement stmt = null;

		for(int i=0; i<propertyList.size(); i++){
			
			try {
				stmt = mySqlConn.createStatement();
				stmt.executeUpdate("INSERT INTO madeUserProperty (TweetTime, UserName, TweetPlace, UserPlace, TweetID) " 
									+ "VALUES('" 
									+ propertyList.get(i).getTweetTime() + "', '" 
									+ eachUser.getUserId().toString() + "', '"
									+ propertyList.get(i).getTweetPlace().toString() + "', '"
									+ propertyList.get(i).getUserPlace().toString() + "', '"
									+ propertyList.get(i).getTweetID().toString() + "') ");
				
				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
			connection = DriverManager.getConnection("jdbc:postgresql://165.132.123.83:5432/postgres","postgres","1111");
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
	
	ArrayList<User> getUsersFromUserProperty(){
		
		ArrayList<User> users = new ArrayList<User>();
		User eachUser = new User();
		
		try {
			st = mySqlConn.createStatement();
			rs = st.executeQuery("SELECT distinct userName, userPlace "
					            + "FROM newUserProperty");
			
			while(rs.next()){
				eachUser = new User();
				
				String userId = rs.getString("userName");
				String userPlace = rs.getString("userPlace");
			//	int classifier = Integer.parseInt(rs.getString("classifier"));
				
				eachUser.setUserId(userId);
				eachUser.setUserLocation(userPlace);
			//	eachUser.setClassifier(classifier);
				
				users.add(eachUser);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return users;
	}
	
	ArrayList<UserProperty> getUserProperty(String userId){
		
		ArrayList<UserProperty> propertyList = new ArrayList<UserProperty>();
		UserProperty eachUserProperty = null;
		Statement stmt = null;
		
		try {
			stmt = mySqlConn.createStatement();
			ResultSet resSet = stmt.executeQuery("SELECT TweetTime, UserName, TweetPlace, UserPlace, TweetID "
					            + "FROM newUserProperty "
								+ "WHERE UserName ='" + userId + "'");
			
			while(resSet.next()){
				eachUserProperty = new UserProperty();
				eachUserProperty.setTweetTime(resSet.getString("TweetTime"));
				eachUserProperty.setUserName(resSet.getString("UserName"));
				eachUserProperty.setTweetPlace(resSet.getString("TweetPlace"));
				eachUserProperty.setUserPlace(resSet.getString("UserPlace"));
				eachUserProperty.setTweetID(resSet.getString("TweetID"));
				
				propertyList.add(eachUserProperty);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return propertyList;
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
	
	MFRatio getMFRatio(String userId){
		
		Statement state = null;
		ResultSet res = null;
		
		MFRatio eachMFRatio = null;
		
		try {
			state = connection.createStatement();
			res = state.executeQuery("SELECT \"SourceName\", \"MyselfRatio\", \"FriendRatio\" "
					            + "FROM public.\"MFRatio\" "
								+ "WHERE \"SourceName\" = '" + userId + "'");
			
			if(res.next()){
				eachMFRatio = new MFRatio();
				eachMFRatio.setSourceName(res.getString("SourceName"));
				eachMFRatio.setMyselfRatio(res.getString("MyselfRatio"));
				eachMFRatio.setFriendRatio(res.getString("FriendRatio"));
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
		
		return eachMFRatio;
	}
	
	MFRatio getRandMFRatio() {
		
		Statement state = null;
		ResultSet res = null;
		
		MFRatio eachMFRatio = null;
		
		try {
			state = connection.createStatement();
			res = state.executeQuery("SELECT \"SourceName\", \"MyselfRatio\", \"FriendRatio\" "
					            + "FROM public.\"MFRatio\" "
								+ "ORDER BY random() LIMIT 1");
			
			if(res.next()){
				eachMFRatio = new MFRatio();
				eachMFRatio.setSourceName(res.getString("SourceName"));
				eachMFRatio.setMyselfRatio(res.getString("MyselfRatio"));
				eachMFRatio.setFriendRatio(res.getString("FriendRatio"));
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
		
		return eachMFRatio;
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
