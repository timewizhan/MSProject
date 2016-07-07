import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import Utility.Talculator;
import Wrapper.matchInfo;
import Wrapper.userInfo;

/**
 * The Class DBConnection.
 */
public class DBConnection {	
		
	private final static int mBasicStatusSize = 25;
	private final static int mBasicResponseSize = 10;
		
	private final static int mDefaultCpuUsage = 0;
		
	private final static int mSuccess = 1;
	
	/**
	 * Checks whether the user is new or not
	 * and does some proper works according to the result
	 *
	 * @param uname the user's name
	 * @param check the flag for the user's type
	 * @param loc the user's location
	 * @return the user's id stored in the users table
	 */
	public static int isThere(String uname, int check, String loc) {
		Connection conn = null;
		PreparedStatement prepared = null;
		ResultSet rs = null;
				
		int uid = -1;
		int utype = -1;
		
		try {
			conn = ServiceServer.mBDS.getConnection();									
			prepared = conn.prepareStatement("SELECT uid, type FROM users WHERE "
					+ "uname = ?");
			prepared.setString(1, uname);
			rs = prepared.executeQuery();						
			
			if (rs.next()) {				
				uid = rs.getInt("uid");
				utype = rs.getInt("type");
			}
		
			if (uid != -1) {				
				if((utype & check) == 0)					
					updateUser(uid, (utype | check));				
			} else			
				uid = addUser(uname, check, loc);
							
		} catch (SQLException e) {
			System.out.println("[isThere]SQLException: " + e.getMessage());
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					System.out.println("[isThere]SQLException: " + e.getMessage());
				}						
		}						
		return uid;
	}
	
	/**
	 * Updates the user information stored in the database
	 *
	 * @param uid the user's id stored in the users table
	 * @param utype the user's type
	 * @throws SQLException the SQL exception
	 */
	private static void updateUser(int uid, int utype) throws SQLException {
		Connection conn = null;
		PreparedStatement prepared = null;
		
		try {
			conn = ServiceServer.mBDS.getConnection();
			prepared = conn.prepareStatement("UPDATE users SET type = ? "
					+ "WHERE uid = ?");
			
			conn.setAutoCommit(false);
			
			prepared.setInt(1, utype);
			prepared.setInt(2, uid);
			prepared.executeUpdate();
			
			conn.commit();
		} catch (SQLException e) {
			System.out.println("[updateUser]SQLException: " + e.getMessage());
			System.out.println("Rolling back data...");
			if (conn != null)
				conn.rollback();
		} finally {						
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					System.out.println("[updateUser/conn]SQLException: " + e.getMessage());
				}
		}
	}
	
	/**
	 * Stores the new user information into the users table 
	 *
	 * @param uname the user's name
	 * @param utype the user's type
	 * @param loc the user's location
	 * @return the user's id stored in the users table
	 * @throws SQLException the SQL exception
	 */
	private static int addUser(String uname, int utype, String loc) throws SQLException {
		Connection conn = null;
		PreparedStatement prepared = null;
		ResultSet rs = null;		
		int uid = -1;		
		
		try {
			conn = ServiceServer.mBDS.getConnection();
			prepared = conn.prepareStatement("INSERT INTO users "
					+ "(uname, type, location) VALUES "
					+ "(?,?,?)", Statement.RETURN_GENERATED_KEYS);
			
			conn.setAutoCommit(false);
			
			prepared.setString(1, uname);
			prepared.setInt(2, utype);
			prepared.setString(3, loc);
			prepared.executeUpdate();
			
			conn.commit();
			
			rs = prepared.getGeneratedKeys();
			
			if (rs.next())
				uid = rs.getInt(1);
			
		} catch (SQLException e) {
			System.out.println("[addUser]SQLException: " + e.getMessage());
			System.out.println("Rolling back data...");
			if (conn != null)
				conn.rollback();
		} finally {				
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					System.out.println("[addUser/conn]SQLException: " + e.getMessage());
				}											
		}
		return uid;
	}
	
	/**
	 * Stores the monitoring information of write status into the status table
	 *
	 * @param uid the user's id stored in the users table
	 * @param msg the status included in the request
	 * @param reqSize the total size of the request
	 * @return the result of storing operation
	 * @throws SQLException the SQL exception
	 */
	public static int writeStatus(int uid, String msg, int reqSize) throws SQLException {
		Connection conn = null;
		PreparedStatement prepared = null;
		
		try {
			conn = ServiceServer.mBDS.getConnection();
			prepared = conn.prepareStatement("INSERT INTO status "
					+ "(uid, status, traffic) VALUES "
					+ "(?,?,?)");
			
			conn.setAutoCommit(false);
			
			prepared.setInt(1, uid);					
			prepared.setString(2, msg);				
			prepared.setInt(3, reqSize + mBasicResponseSize);
			prepared.executeUpdate();
			
			conn.commit();			
		} catch (SQLException e) {
			System.out.println("[writeStatus]SQLException: " + e.getMessage());
			System.out.println("Rolling back data...");
			if (conn != null)
				conn.rollback();
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					System.out.println("[writeStatus/conn]SQLException: " + e.getMessage());
				}						
		}		
		return mSuccess;
	}
	
	/**
	 * Stores the migrated data
	 *
	 * @param uid the user's id stored in the users table
	 * @param statusList all the status of the user
	 * @return the result of storing operation
	 * @throws SQLException the SQL exception
	 */
	public static int writeStatus(int uid, JSONArray statusList) throws SQLException {
		Connection conn = null;
		PreparedStatement prepared = null;
		
		try {
			conn = ServiceServer.mBDS.getConnection();
			prepared = conn.prepareStatement("INSERT INTO status "
					+ "(uid, status, time, traffic) VALUES "
					+ "(?,?,?,?)");
			
			conn.setAutoCommit(false);
			
			for (int i = 0; i < statusList.size(); i++) {
				JSONObject statusItem = (JSONObject) statusList.get(i);
				
				String status = (String) statusItem.get("STATUS");
				String time = (String) statusItem.get("TIME"); 
				int traffic =  (int) (long) statusItem.get("TRAFFIC");
				
				prepared.setInt(1, uid);
				prepared.setString(2, status);
				prepared.setString(3, time);
				prepared.setInt(4, traffic);
				prepared.addBatch();
			}

			prepared.executeBatch();						
			conn.commit();			
		} catch (SQLException e) {
			System.out.println("[writeStatus]SQLException: " + e.getMessage());
			System.out.println("Rolling back data...");
			if (conn != null)
				conn.rollback();
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					System.out.println("[writeStatus/conn]SQLException: " + e.getMessage());
				}						
		}
		return mSuccess;
	}
			
	/**
	 * Stores the monitoring information of read status into the latent table
	 *
	 * @param uid the user's id stored in the users table
	 * @param dst A particular user's name that the user wants to access 
	 * @param reqSize the total size of the request
	 * @param num the number of status that the user wants to read
	 * @return the result of storing operation
	 * @throws SQLException the SQL exception
	 */
	public static int readStatus(int uid, String dst, int reqSize, int num) throws SQLException {
		/*
		int t_uid = getUID(dst);
		if (t_uid != -1)																	
			return storeLatent(uid, reqSize, num);																		
		else
			return mFail;
		*/
		return storeLatent(uid, reqSize, num);
	}
	
	/**
	 * Actually stores the monitoring information of read status into the latent table
	 *
	 * @param uid the user's id stored in the users table
	 * @param reqSize the req size
	 * @param num the total size of the request
	 * @return the result of storing operation
	 * @throws SQLException the SQL exception
	 */
	private static int storeLatent(int uid, int reqSize, int num) throws SQLException {
		Connection conn = null;
		PreparedStatement prepared = null;						
						
		try {
			conn = ServiceServer.mBDS.getConnection();		
			prepared = conn.prepareStatement("INSERT INTO latent "
					+ "(uid, traffic) VALUES "
					+ "(?,?)");
			
			conn.setAutoCommit(false);
			
			for (int i = 0; i < num; i++) {
				prepared.setInt(1, uid);				
				prepared.setInt(2, Math.round(reqSize/num) + mBasicStatusSize);
				prepared.addBatch();
			}
			
			prepared.executeBatch();
			conn.commit();			
		} catch (SQLException e) {
			System.out.println("[storeLatent]SQLException: " + e.getMessage());
			System.out.println("Rolling back data...");
			if (conn != null)
				conn.rollback();
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					System.out.println("[storeLatent/conn]SQLException: " + e.getMessage());					
				}						
		}
		return mSuccess;
	}
	
	/**
	 * Stores the monitoring information of write reply into the reply table
	 *
	 * @param uid the user's id stored in the users table
	 * @param dst A particular user's name that the user wants to access
	 * @param msg the reply included in the request
	 * @param reqSize the total size of the request
	 * @return the result of storing operation
	 * @throws SQLException the SQL exception
	 */
	public static int writeReply(int uid, String dst, String msg, int reqSize) throws SQLException {
		/*
		int t_uid = getUID(dst);
		if (t_uid != -1) {
			return storeReply(uid, msg, reqSize);
		} else {
			return mFail;
		}
		*/
		return storeReply(uid, msg, reqSize);
	}
	
	/**
	 * Actually stores the monitoring information of write reply into the reply table
	 *
	 * @param uid the user's id stored in the users table
	 * @param msg the reply included in the request
	 * @param reqSize the total size of the request
	 * @return the result of storing operation
	 * @throws SQLException the SQL exception
	 */
	private static int storeReply(int uid, String msg, int reqSize) throws SQLException {
		Connection conn = null;
		PreparedStatement prepared = null;						
						
		try {
			conn = ServiceServer.mBDS.getConnection();	
			prepared = conn.prepareStatement("INSERT INTO reply "
					+ "(uid, reply, traffic) VALUES "
					+ "(?,?,?)");
			
			conn.setAutoCommit(false);
			
			prepared.setInt(1, uid);			
			prepared.setString(2, msg);
			prepared.setInt(3, reqSize + mBasicResponseSize);																					
			prepared.executeUpdate();
			
			conn.commit();			
		} catch (SQLException e) {
			System.out.println("[storeReply]SQLException: " + e.getMessage());
			System.out.println("Rolling back data...");
			if (conn != null)
				conn.rollback();
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					System.out.println("[storeReply/conn]SQLException: " + e.getMessage());					
				}						
		}
		return mSuccess;
	}		
	
	/*
	private static int getUID(String dst) {
		Connection conn = null;
		PreparedStatement prepared = null;
		ResultSet rs = null;
		int uid = -1;
		int type = -1;
	
		try {
			conn = ServiceServer.mBDS.getConnection()
			prepared = conn.prepareStatement("SELECT uid FROM users WHERE "
					+ "uname = ?");
			
			prepared.setString(1, dst);
			rs = prepared.executeQuery();
			
			if (rs.next()) {
				type = rs.getInt("type");
				if ((type & userType.resident) == 1)
					uid = rs.getInt("uid");								
			}
		} catch (SQLException e) {
			System.out.println("[getUID]SQLException: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("[getUID]IOException: " + e.getMessage());			
		} catch (PropertyVetoException e) {
			System.out.println("[getUID]PropertyVetoException: " + e.getMessage());
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					System.out.println("[getUID/conn]SQLException: " + e.getMessage());					
				}								
		}		
		return uid;
	}
	*/
	
	/**
	 * Deletes monitoring result from the monitoring tables 
	 *
	 * @throws SQLException the SQL exception
	 */
	public static void deleteMonitorResult() throws SQLException {
		Connection conn = null;
		PreparedStatement prepared = null;
		String[] sql = {"client_side_monitor", "server_side_monitor"};
		
		try {			
			conn = ServiceServer.mBDS.getConnection();
						
			for (int i = 0; i < sql.length; i++) {
				conn.setAutoCommit(false);
				prepared = conn.prepareStatement("DELETE FROM " + sql[i]);
				prepared.executeUpdate();
				conn.commit();
			}																													
		} catch (SQLException e) {
			System.out.println("[deleteMonitorResult]SQLException: " + e.getMessage());
			System.out.println("Rolling back data...");
			if (conn != null)
				conn.rollback();
		} finally {				
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					System.out.println("[deleteMonitorResult/conn]SQLException: " + e.getMessage());
				}											
		}
	}
	
	/**
	 * Stores client monitoring result into the client_side_monitor
	 *
	 * @return a list of server monitoring results
	 * @throws SQLException the SQL exception
	 */
	public static int[] storeClientMonitor() throws SQLException {
		Connection conn = null;
		PreparedStatement prepared = null;
		
		String[] period = Talculator.getPeriod();					
					
		userInfo [] uInfo = getMonitor(period);
		
		int server_side_traffic = 0;
		int total_request = getTotalReq(period);
		
		try {
			conn = ServiceServer.mBDS.getConnection();
			prepared = conn.prepareStatement("INSERT INTO client_side_monitor "
					+ "(user, location, client_side_traffic) VALUES "
					+ "(?,?,?)");
			
			conn.setAutoCommit(false);
							
			for (int i = 0; i < uInfo.length; i++) {								
				int uTraffic = uInfo[i].getTraffic();
				
				if (uTraffic == 0)
					continue;
				
				String uname = uInfo[i].getName();
				String ulocation = uInfo[i].getLoc(); 
				
				prepared.setString(1, uname);
				prepared.setString(2, ulocation);
				prepared.setInt(3, uTraffic);
				prepared.addBatch();
				server_side_traffic += uTraffic;										
			}
						
			prepared.executeBatch();
			conn.commit();						
		} catch (SQLException e) {
			System.out.println("[storeClientMonitor]SQLException: " + e.getMessage());
			System.out.println("Rolling back data...");
			if (conn != null)
				conn.rollback();
		} finally {				
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					System.out.println("[storeClientMonitor/conn]SQLException: " + e.getMessage());
				}											
		}
		
		int[] server_side_monitor = new int[2];
		server_side_monitor[0] = server_side_traffic;
		server_side_monitor[1] = total_request;
		
		return server_side_monitor;
	}
	
	/**
	 * Gets the monitoring result of all the users
	 *
	 * @param period the monitoring period
	 * @return the monitoring result of all the users for an one hour
	 */
	public static userInfo [] getMonitor(String[] period) {		
		userInfo [] uInfo = getUserInfo();
		HashMap<Integer, Integer> tInfo = getTrafficLog(period);						
		
		for (int i = 0; i < uInfo.length; i++) {
			int uid = uInfo[i].getUID();
			if (tInfo.get(uid) != null)
				uInfo[i].updateTraffic(tInfo.get(uid));			
		}		
		return uInfo;
	}
	
	/**
	 * Gets all of the user information from the users table
	 *
	 * @return a set of user information
	 */
	private static userInfo[] getUserInfo () {
		Connection conn = null;
		PreparedStatement prepared = null;
		ResultSet rs = null;

		userInfo [] uInfo = null;
		
		try {
			conn = ServiceServer.mBDS.getConnection();
			
			prepared = conn.prepareStatement("SELECT uid, uname, location FROM users",
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
						
			rs = prepared.executeQuery();
			
			int i = 0;
			rs.last();
			int rowCnt = rs.getRow();			
			uInfo = new userInfo [rowCnt];		
			rs.beforeFirst();
			while (rs.next()) {
				int uid = rs.getInt("uid");
				String uname = rs.getString("uname");
				String loc = rs.getString("location");
				
				uInfo[i] = new userInfo();
				uInfo[i].setInfo(uid, uname, loc);				
				i++;
			}				
		} catch (SQLException e) {
			System.out.println("[getUserInfo]SQLException: " + e.getMessage());
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					System.out.println("[getUserInfo/conn]SQLException: " + e.getMessage());					
				}								
		}		
		return uInfo;
	}
	
	/**
	 * Gets the traffic monitoring results from the status, reply, and latent tables
	 *
	 * @param period the monitoring period
	 * @return HashMap of user id and corresponding traffic monitoring result
	 */
	private static HashMap<Integer, Integer> getTrafficLog(String[] period) {
		Connection conn = null;
		PreparedStatement prepared = null;
		ResultSet rs = null;				
		HashMap<Integer, Integer> tMap = new HashMap<Integer, Integer>();
		
		try {
			conn = ServiceServer.mBDS.getConnection();
			prepared = conn.prepareStatement("SELECT uid, SUM(traffic) FROM ("
					+ "SELECT uid, traffic, time FROM status "
					+ "UNION ALL "
					+ "SELECT uid, traffic, time FROM reply "
					+ "UNION ALL "
					+ "SELECT uid, traffic, time FROM latent) x "
					+ "WHERE time BETWEEN ? AND ? "
					+ "GROUP BY uid");									
											
			String start = period[0];
			String end = period[1];
			
			prepared.setString(1, start);
			prepared.setString(2, end);
			
			rs = prepared.executeQuery();						
						
			while(rs.next()) {
				int t_uid = rs.getInt("uid");
				int t_traffic = rs.getInt("SUM(traffic)");				 
				tMap.put(t_uid, t_traffic);				
			}					
		} catch (SQLException e) {
			System.out.println("[getTrafficLog]SQLException: " + e.getMessage());
		} finally {						
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					System.out.println("[getTrafficLog/conn]SQLException: " + e.getMessage());
				}
		}
		return tMap;
	}
	
	/**
	 * Gets the total number of requests that service server handled
	 *
	 * @param period the monitoring period
	 * @return the total number of requests that the service server handled
	 */
	private static int getTotalReq(String[] period) {
		Connection conn = null;
		PreparedStatement prepared = null;
		ResultSet rs = null;				
		int total_req = 0;
		
		try {
			conn = ServiceServer.mBDS.getConnection();
			prepared = conn.prepareStatement("SELECT COUNT(uid) FROM ("
					+ "SELECT uid, time FROM status "
					+ "UNION ALL "
					+ "SELECT uid, time FROM reply "
					+ "UNION ALL "
					+ "SELECT uid, time FROM latent) x "
					+ "WHERE time BETWEEN ? AND ?");					
								
			String start = period[0];
			String end = period[1];
			
			prepared.setString(1, start);
			prepared.setString(2, end);
			
			rs = prepared.executeQuery();						
						
			while(rs.next()) {
				total_req = rs.getInt("COUNT(uid)");																
			}					
		} catch (SQLException e) {
			System.out.println("[getTotalReq]SQLException: " + e.getMessage());
		} finally {						
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					System.out.println("[getTotalReq/conn]SQLException: " + e.getMessage());
				}
		}
		return total_req;
	}
	
	/**
	 * Stores the server side monitoring result into the server_side_monitor table
	 *
	 * @param server_side_monitor the server side monitoring result
	 * @throws SQLException the SQL exception
	 */
	public static void storeServerMonitor(int[] server_side_monitor) throws SQLException {
		Connection conn = null;
		PreparedStatement prepared = null;				
		
		try {
			conn = ServiceServer.mBDS.getConnection();
			prepared = conn.prepareStatement("INSERT INTO server_side_monitor "
					+ "(cpu_util, server_side_traffic, num_request) VALUES "
					+ "(?,?,?)");
						
			conn.setAutoCommit(false);
			
			int server_side_traffic = server_side_monitor[0];
			int num_request = server_side_monitor[1];
			
			prepared.setInt(1, mDefaultCpuUsage);
			prepared.setInt(2, server_side_traffic);
			prepared.setInt(3, num_request);
			
			prepared.executeUpdate();
			conn.commit();						
		} catch (SQLException e) {
			System.out.println("[storeServerMonitor]SQLException: " + e.getMessage());
			System.out.println("Rolling back data...");
			if (conn != null)
				conn.rollback();
		} finally {				
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					System.out.println("[storeServerMonitor/conn]SQLException: " + e.getMessage());
				}											
		}		
	}
	
	/**
	 * Gets the match result from the math_result_table
	 *
	 * @return a set of match results
	 */
	public static matchInfo[] getMatchResult() {
		Connection conn = null;
		PreparedStatement prepared = null;
		ResultSet rs = null;
		
		matchInfo[] match = null;
									
		try {		
			conn = ServiceServer.mBDS.getConnection();
			prepared = conn.prepareStatement("SELECT user, prev_ep, curr_ep FROM match_result_table",
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
					
			rs = prepared.executeQuery();
						
			int i = 0;
			rs.last();
			int rowCnt = rs.getRow();			
			match = new matchInfo[rowCnt];
			rs.beforeFirst();
			while (rs.next()) {
				String uname = rs.getString("user");
				int prev = rs.getInt("prev_ep");
				int curr = rs.getInt("curr_ep");
												
				match[i] = new matchInfo();
				match[i].setInfo(uname, prev, curr);
				i++;
			}
		} catch (SQLException e) {
			System.out.println("[getMatchResult]SQLException: " + e.getMessage());
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					System.out.println("[getMatchResult/conn]SQLException: " + e.getMessage());					
				}		
		}
		return match;
	}
	
	/**
	 * Gets the migrated user's data according to the match result
	 *
	 * @param uname the user's name to be migrated
	 * @return a set of migrated data
	 * @throws SQLException the SQL exception
	 */
	@SuppressWarnings("unchecked")
	public static JSONObject getMigrated(String uname) throws SQLException {
		userInfo uInfo = getUserInfo(uname);								
		String loc = uInfo.getLoc();
		
		/*
		statusInfo uStatus = getAllStatus(uid);		
		String[] status = uStatus.getStatusList();						
		String[] time = uStatus.getTimeList(); 			
		int[] traffic = uStatus.getTrafficList();
		*/
		
		JSONObject userItem = new JSONObject();
		userItem.put("UNAME", uname);
		userItem.put("LOCATION", loc);
			
		/*
		JSONArray statusList = new JSONArray();				
		for (int j = 0; j < status.length; j++) {
			JSONObject statusItem = new JSONObject();
			statusItem.put("STATUS", status[j]);
			statusItem.put("TIME", time[j]);
			statusItem.put("TRAFFIC", traffic[j]); 
			statusList.add(statusItem);
		}
		userItem.put("STATUS_LIST", statusList);						
		*/
				
		return userItem;		
	}
	
	/**
	 * Gets the user information to be migrated from the users table
	 *
	 * @param uname the uname
	 * @return the user info
	 */
	private static userInfo getUserInfo(String uname) {
		Connection conn = null;
		PreparedStatement prepared = null;
		ResultSet rs = null;

		userInfo uInfo = new userInfo();
		
		try {
			conn = ServiceServer.mBDS.getConnection();
			prepared = conn.prepareStatement("SELECT uid, location FROM users WHERE "
						+ "uname = ?",
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
				
			prepared.setString(1, uname);				
									
			rs = prepared.executeQuery();
						
			if (rs.next()) {
				int uid = rs.getInt("uid");
				String loc = rs.getString("location");
				uInfo.setInfo(uid, uname, loc);												
			}				
		} catch (SQLException e) {
			System.out.println("[getUserInfo]SQLException: " + e.getMessage());
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					System.out.println("[getUserInfo/conn]SQLException: " + e.getMessage());					
				}								
		}		
		return uInfo;
	}
	
	/**
	 * Deletes the migrated data after sending it
	 *
	 * @param uname the migrated user's name
	 * @throws SQLException the SQL exception
	 */
	public static void deleteMigrated(String uname) throws SQLException {
		Connection conn = null;
		PreparedStatement prepared = null;		
				
		try {
			conn = ServiceServer.mBDS.getConnection();
			prepared = conn.prepareStatement("DELETE FROM users WHERE "
					+ "uname = ?");
			
			conn.setAutoCommit(false);								
			prepared.setString(1, uname);																	
			prepared.executeUpdate();
			conn.commit();				
		} catch (SQLException e) {
			System.out.println("[deleteMigrated]SQLException: " + e.getMessage());
			System.out.println("Rolling back data...");
			if (conn != null)
				conn.rollback();
		} finally {						
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					System.out.println("[deleteMigrated/conn]SQLException: " + e.getMessage());
				}
		}
	}
}