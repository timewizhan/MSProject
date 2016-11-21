import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

public class Database {
	
	static Logger log = Logger.getLogger(SocialWeight.class.getName());
	
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
		//	log.info("# Database : CONNECT EP DATABASE SESSION");
		}catch(ClassNotFoundException cnfe){
			cnfe.printStackTrace();
		}catch(SQLException se){
			se.printStackTrace();
		}
	}
	
	public void mySqlDisconnect(){
		
		try {
			mySqlConn.close();
		//	log.info("# Database : DISCONNECT EP DATABASE SESSION");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	public CoordValue getLatitudeLongitude(String location){
	
		CoordValue coordValue = new CoordValue();
		
		Statement stmt = null;
		try {
			stmt = mySqlConn.createStatement();
			ResultSet rs = stmt.executeQuery("select latitude, longitude from coord_list_table where state = '" + location +"';");

			int rsCount = 0;
			while(rs.next()){
				coordValue.setLatitude(Double.parseDouble(rs.getString("latitude")));
				coordValue.setLongitude(Double.parseDouble(rs.getString("longitude")));
				
				rsCount++;
			}
			
			if(rsCount==0)
				log.info("---------------------------------------------------------------------"+location+"에 대한 위도/경도 정보가 없음");

			rs.close();
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return coordValue;
	}
	
	ArrayList<User> getCompletedUserIdList(){
		
		ArrayList<User> users = new ArrayList<User>();
		User eachUser = new User();
		
		try {
			st = mySqlConn.createStatement();
			rs = st.executeQuery("SELECT userName, userPlace "
					            + "FROM completeUserid");
			
			while(rs.next()){
				eachUser = new User();
				
				String userId = rs.getString("userName");
				String userPlace = rs.getString("userPlace");
				
				eachUser.setUserId(userId);
				eachUser.setUserLocation(userPlace);
				
				users.add(eachUser);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		
			try {
				rs.close();
				st.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return users;
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
	
	void insertSocialWeight(String userId, double [] normalizedCloudDistances){
		
		Statement stmt = null;
	
		try {
			stmt = mySqlConn.createStatement();
			stmt.executeUpdate("INSERT INTO normalized_social_level_table (user, ep1, ep2, ep3) VALUES('" 
													+ userId + "', "  
													+ normalizedCloudDistances[0]  + ", "
													+ normalizedCloudDistances[1] + ", "
													+ normalizedCloudDistances[2] + ")");
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			try {
				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
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
}
