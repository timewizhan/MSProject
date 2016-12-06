import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

public class Database {
	
	static Logger log = Logger.getLogger(ClosestUsers.class.getName());
	
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
		
	ArrayList<User> getUsers(){

		Statement stmt = null;
		ArrayList<User> userList = new ArrayList<User>();
		User eachUser = new User();
		
		try {
			stmt = mySqlConn.createStatement();
			ResultSet resSet = stmt.executeQuery("select userName, userPlace, classifier from completeUserId");
			
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
	
	int getUserEpNum(String userId){

		Statement stmt = null;
		int epNum = 0;
		
		try {
			stmt = mySqlConn.createStatement();
			ResultSet resSet = stmt.executeQuery("SELECT ep_num FROM closest_table "
													+ "WHERE userId ='" + userId + "' ");
			
			if(resSet.next()){
				epNum = Integer.parseInt(resSet.getString("ep_num"));
			}
			
			resSet.close();
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return epNum;
	}
	
	void insertNewyorkUsers(String userId, String userLocation, int classifier){
		
		Statement stmt = null;

		try {
			stmt = mySqlConn.createStatement();
			stmt.executeUpdate("INSERT INTO Newyork_User (userName, userPlace, classifier) " 
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
	
	void insertTexasUsers(String userId, String userLocation, int classifier){
		
		Statement stmt = null;

		try {
			stmt = mySqlConn.createStatement();
			stmt.executeUpdate("INSERT INTO Texas_User (userName, userPlace, classifier) " 
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
	
	void insertWashingtonUsers(String userId, String userLocation, int classifier){
		
		Statement stmt = null;

		try {
			stmt = mySqlConn.createStatement();
			stmt.executeUpdate("INSERT INTO Washington_User (userName, userPlace, classifier) " 
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
}
