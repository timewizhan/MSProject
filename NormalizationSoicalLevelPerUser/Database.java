import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

public class Database {
	
	static Logger log = Logger.getLogger(NormalizationSocialLevelPerUser.class.getName());
	
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
	
	ArrayList<Friend> getFriendInSocialLevelPerUser(String sourceName){

		ArrayList<Friend> addedUserLinks = new ArrayList<Friend>();
		Friend eachFriend = null;
		Statement stmt = null;

		try {
			stmt = mySqlConn.createStatement();
			ResultSet resSet = stmt.executeQuery("SELECT destinationName, portion " 
												+ "FROM SocialLevelPerUser " 
												+ "WHERE sourceName = '" + sourceName + "';");
			
			while(resSet.next()){
				eachFriend = new Friend();
				
				String friendId = resSet.getString("destinationName");
				double portion = Double.parseDouble(resSet.getString("portion"));
				String location = getLocation(friendId);
				
				eachFriend.setUserId(friendId);
				eachFriend.setPortion(portion);
				eachFriend.setLocation(location);
				
				addedUserLinks.add(eachFriend);
			}
			
			resSet.close();
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return addedUserLinks;
	}
	
	String getLocation(String userId){
		
		String location = null;
		Statement st = null;

		try {
			st = mySqlConn.createStatement();
			ResultSet rs = st.executeQuery("SELECT userPlace " 
											+ "FROM CompleteUserId " 
											+ "WHERE userName = '" + userId + "';");
			
			if(rs.next()){
				location = rs.getString("userPlace");
			}
			
			rs.close();
			st.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return location;
	}
	
	boolean checkCompleteLink(String sourceName, String destinationName){
		boolean isCompleteLink = false;
		
		Statement stmt = null;

		try {
			stmt = mySqlConn.createStatement();
			ResultSet resSet = stmt.executeQuery("SELECT sourceName, destinationName, portion " 
												+ "FROM SocialLevelPerUser " 
												+ "WHERE sourceName = '" + sourceName + "' AND destinationName = '" + destinationName + "';");
			
			if(resSet.next()){
				isCompleteLink = true;
			}
			
			resSet.close();
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return isCompleteLink;
	}
	
	void insertNewLink(String sourceName, String destinationName, double portion){
		
		Statement stmt = null;

		try {
			stmt = mySqlConn.createStatement();
			stmt.executeUpdate("INSERT INTO SocialLevelPerUser (sourceName, destinationName, portion) " 
								+ "VALUES('" 
								+ sourceName + "', '" 
								+ destinationName + "', " 
								+ portion + ") ");
			
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
			st = mySqlConn.createStatement();
			rs = st.executeQuery("SELECT userName, userPlace, classifier "
					            + "FROM completeUserid");
			
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
	
	double getSumPortion(String userId){
		double sumPortion = 0.0;
		Statement stmt = null;
		
		try {
			stmt = mySqlConn.createStatement();
			ResultSet resSet = stmt.executeQuery("select sum(portion) from SocialLevelPerUser where sourceName = '" + userId + "';");
	
			if(resSet.next()){
				sumPortion = Double.parseDouble(resSet.getString("sum(portion)"));
			}
	
			resSet.close();
			stmt.close();
	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sumPortion;
	}
	
	void updateFriendPortion(String userId, String friendId, double eachFriendPortion, double sumPortion){

		Statement stmt = null;
		String sql = null;
		double updatedPortion = eachFriendPortion / sumPortion;
		
		try {
			stmt = mySqlConn.createStatement();
		    sql = "UPDATE SocialLevelPerUser SET portion = " + updatedPortion 
		    							+ " WHERE sourceName = '" + userId + "' AND destinationName = '" + friendId + "'";
		    stmt.executeUpdate(sql);
		} catch (SQLException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
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
