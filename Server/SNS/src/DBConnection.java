import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import Type.userType;
import Utility.Talculator;
import Wrapper.matchInfo;
import Wrapper.statusInfo;
import Wrapper.userInfo;

public class DBConnection {
	private static DBConnection mDS;
	private ComboPooledDataSource mCDPS;
		
	private final static int mBasicResponseSize = 22;	
	
	private final static int mSuccess = 1;
	private final static int mFail = 0;
			
	private DBConnection() throws IOException, SQLException, PropertyVetoException {
		mCDPS = new ComboPooledDataSource();
		mCDPS.setDriverClass("com.mysql.jdbc.Driver");
		mCDPS.setJdbcUrl("jdbc:mysql://localhost:3306/snsdb?autoReconnect=true&useSSL=false");
		mCDPS.setUser("root");
		mCDPS.setPassword("cclabj0gg00");
		
		// the settings below are optional
		// c3p0 can work with defaults
		mCDPS.setMinPoolSize(3);
		mCDPS.setAcquireIncrement(5);
		mCDPS.setMaxPoolSize(50);
		mCDPS.setMaxStatements(0);				
	}
	
	public Connection getConnection() throws SQLException {
		return this.mCDPS.getConnection();
	} 
	
	public static DBConnection getInstance() throws IOException, SQLException, PropertyVetoException {
		if(mDS == null) {
			mDS = new DBConnection();
			return mDS;
		} else {
			return mDS;
		}		
	}
			
	public static int isThere(String uname, int check, String loc) {
		Connection conn = null;
		PreparedStatement prepared = null;
		ResultSet rs = null;
				
		int uid = -1;
		int utype = -1;
		
		try {
			conn = DBConnection.getInstance().getConnection();									
			prepared = conn.prepareStatement("SELECT uid,type FROM users WHERE "
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
							
		} catch (PropertyVetoException e) {
			System.out.println("[isThere]PropertyVetoException: " + e.getMessage());
		} catch (SQLException e) {
			System.out.println("[isThere]SQLException: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("[isThere]IOException: " + e.getMessage());	
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
	
	private static void updateUser(int uid, int utype) throws SQLException {
		Connection conn = null;
		PreparedStatement prepared = null;
		
		try {
			conn = DBConnection.getInstance().getConnection();
			prepared = conn.prepareStatement("UPDATE users SET type = ? "
					+ "WHERE uid = ?");
			
			conn.setAutoCommit(false);
			
			prepared.setInt(1, utype);
			prepared.setInt(2, uid);
			prepared.executeUpdate();
			
			conn.commit();
		} catch (PropertyVetoException e) {
			System.out.println("[updateUser]PropertyVetoException: " + e.getMessage());
		} catch (SQLException e) {
			System.out.println("[updateUser]SQLException: " + e.getMessage());
			System.out.println("Rolling back data...");
			if (conn != null)
				conn.rollback();
		} catch (IOException e) {
			System.out.println("[updateUser]IOException: " + e.getMessage());
		} finally {						
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					System.out.println("[updateUser/conn]SQLException: " + e.getMessage());
				}
		}
	}
	
	private static int addUser(String uname, int utype, String loc) throws SQLException {
		Connection conn = null;
		PreparedStatement prepared = null;
		ResultSet rs = null;		
		int uid = -1;		
		
		try {
			conn = DBConnection.getInstance().getConnection();
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
			
		} catch (PropertyVetoException e) {
			System.out.println("[addUser]PropertyVetoException: " + e.getMessage());
		} catch (SQLException e) {
			System.out.println("[addUser]SQLException: " + e.getMessage());
			System.out.println("Rolling back data...");
			if (conn != null)
				conn.rollback();
		} catch (IOException e) {
			System.out.println("[addUser]IOException: " + e.getMessage());
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
	
	public static int writeStatus(int uid, String msg, int reqSize) throws SQLException {
		Connection conn = null;
		PreparedStatement prepared = null;
		
		try {
			conn = DBConnection.getInstance().getConnection();
			prepared = conn.prepareStatement("INSERT INTO status "
					+ "(uid, status, traffic) VALUES "
					+ "(?,?,?)");
			
			conn.setAutoCommit(false);
			
			prepared.setInt(1, uid);					
			prepared.setString(2, msg);				
			prepared.setInt(3, reqSize + mBasicResponseSize);
			prepared.executeUpdate();
			
			conn.commit();			
		} catch (PropertyVetoException e) {
			System.out.println("[writeStatus]PropertyVetoException: " + e.getMessage());
		} catch (SQLException e) {
			System.out.println("[writeStatus]SQLException: " + e.getMessage());
			System.out.println("Rolling back data...");
			if (conn != null)
				conn.rollback();
		} catch (IOException e) {
			System.out.println("[writeStatus]IOException: " + e.getMessage());
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
	
	public static int writeStatus(int uid, JSONArray statusList) throws SQLException {
		Connection conn = null;
		PreparedStatement prepared = null;
		
		try {
			conn = DBConnection.getInstance().getConnection();
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
		} catch (PropertyVetoException e) {
			System.out.println("[writeStatus]PropertyVetoException: " + e.getMessage());
		} catch (SQLException e) {
			System.out.println("[writeStatus]SQLException: " + e.getMessage());
			System.out.println("Rolling back data...");
			if (conn != null)
				conn.rollback();
		} catch (IOException e) {
			System.out.println("[writeStatus]IOException: " + e.getMessage());
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
			
	public static int readStatus(int uid, String uname, int reqSize, int num) throws SQLException {
		int t_uid = getUID(uname);
		if (t_uid != -1) {
			statusInfo result = getStatus(t_uid, num);
		
			int[] t_sids = result.getSIDs();
			
			if (t_sids != null) {
				String total_t_status = "";
				String [] t_status = result.getStatusList();
				for (int i = 0; i < t_status.length; i++) {				
					total_t_status.concat(t_status[i]);			
				}															
				return storeLatent(uid, t_sids, reqSize, total_t_status.length());								
			} else
				return mFail;							
		} else
			return mFail;
	}
	
	private static int storeLatent(int uid, int[] sids, int reqSize, int slen) throws SQLException {
		Connection conn = null;
		PreparedStatement prepared = null;				
		int t_sids[] = sids;
						
		try {
			conn = DBConnection.getInstance().getConnection();			
			prepared = conn.prepareStatement("INSERT INTO latent "
					+ "(uid, sid, traffic) VALUES "
					+ "(?,?,?)");
			
			conn.setAutoCommit(false);
			
			for (int i = 0; i < t_sids.length; i++) {
				prepared.setInt(1, uid);
				prepared.setInt(2, t_sids[i]);
				prepared.setInt(3, Math.round((reqSize + slen)/sids.length));
				prepared.addBatch();
			}
			
			prepared.executeBatch();
			conn.commit();			
		} catch (PropertyVetoException e) {
			System.out.println("[storeLatent]PropertyVetoException: " + e.getMessage());			
		} catch (SQLException e) {
			System.out.println("[storeLatent]SQLException: " + e.getMessage());
			System.out.println("Rolling back data...");
			if (conn != null)
				conn.rollback();
		} catch (IOException e) {
			System.out.println("[storeLatent]IOException: " + e.getMessage()); 
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
	
	public static int writeReply(int uid, String uname, String msg, int reqSize, int num) throws SQLException {
		int t_uid = getUID(uname);
		if (t_uid != -1) {
			statusInfo result = getStatus(t_uid, num);
			
			int [] t_sids = result.getSIDs();		
						
			if (t_sids != null) {
				int picked = 0;
				if (t_sids.length != 1) {
					Random rand = new Random();
					picked = rand.nextInt(t_sids.length - 1);
				}
				return storeReply(uid, t_sids[picked], msg, reqSize);
			} else
				return mFail;
		} else {
			return mFail;
		}
	}
	
	private static int storeReply(int uid, int sid, String msg, int reqSize) throws SQLException {
		Connection conn = null;
		PreparedStatement prepared = null;						
						
		try {
			conn = DBConnection.getInstance().getConnection();			
			prepared = conn.prepareStatement("INSERT INTO reply "
					+ "(uid, sid, reply, traffic) VALUES "
					+ "(?,?,?,?)");
			
			conn.setAutoCommit(false);
			
			prepared.setInt(1, uid);
			prepared.setInt(2, sid);
			prepared.setString(3, msg);
			prepared.setInt(4, reqSize + mBasicResponseSize);																					
			prepared.executeUpdate();
			
			conn.commit();			
		} catch (PropertyVetoException e) {
			System.out.println("[storeReply]PropertyVetoException: " + e.getMessage());			
		} catch (SQLException e) {
			System.out.println("[storeReply]SQLException: " + e.getMessage());
			System.out.println("Rolling back data...");
			if (conn != null)
				conn.rollback();
		} catch (IOException e) {
			System.out.println("[storeReply]IOException: " + e.getMessage()); 
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
	
	private static int getUID(String uname) {
		Connection conn = null;
		PreparedStatement prepared = null;
		ResultSet rs = null;
		int uid = -1;
		int type = -1;
	
		try {
			conn = DBConnection.getInstance().getConnection();
			prepared = conn.prepareStatement("SELECT uid, type FROM users WHERE "
					+ "uname = ?");
			
			prepared.setString(1, uname);
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
	
	private static statusInfo getStatus(int uid, int num) throws SQLException {
		Connection conn = null;
		PreparedStatement prepared = null;
		ResultSet rs = null;
		
		int[] sids = null;
		String[] status = null;
		String[] time = null;
		int[] traffic = null;
									
		try {
			conn = DBConnection.getInstance().getConnection();
			prepared = conn.prepareStatement("SELECT sid,status,time,traffic FROM status WHERE "
					+ "uid = ? ORDER BY sid DESC LIMIT ?",
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			
			prepared.setInt(1, uid);
			prepared.setInt(2, num);
			
			rs = prepared.executeQuery();
						
			int i = 0;
			rs.last();
			int rowCnt = rs.getRow();
			sids = new int[rowCnt];
			status = new String[rowCnt];
			time = new String[rowCnt];
			traffic = new int[rowCnt];
			rs.beforeFirst();
			while (rs.next()) {
				sids[i] = rs.getInt("sid");
				status[i] = rs.getString("status");
				time[i] = rs.getString("time");
				traffic[i] = rs.getInt("traffic");
				i++;
			}
		} catch (PropertyVetoException e) {
			System.out.println("[getStatus]PropertyVetoException: " + e.getMessage());			
		}  catch (SQLException e) {
			System.out.println("[getStatus]SQLException: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("[getStatus]IOException: " + e.getMessage()); 
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					System.out.println("[getStatus/conn]SQLException: " + e.getMessage());					
				}		
		}
		return new statusInfo(sids, status, time, traffic);
	}
	
	public static void deleteMonitorResult() throws SQLException {
		Connection conn = null;
		PreparedStatement prepared = null;
		String[] sql = {"client_side_monitor", "server_side_monitor"};
		
		try {			
			conn = DBConnection.getInstance().getConnection();
						
			for (int i = 0; i < sql.length; i++) {
				conn.setAutoCommit(false);
				prepared = conn.prepareStatement("DELETE FROM " + sql[i]);
				prepared.executeUpdate();
				conn.commit();
			}																													
		} catch (PropertyVetoException e) {
			System.out.println("[deleteMonitorResult]PropertyVetoException: " + e.getMessage());
		} catch (SQLException e) {
			System.out.println("[deleteMonitorResult]SQLException: " + e.getMessage());
			System.out.println("Rolling back data...");
			if (conn != null)
				conn.rollback();
		} catch (IOException e) {
			System.out.println("[deleteMonitorResult]IOException: " + e.getMessage());
		} finally {				
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					System.out.println("[deleteMonitorResult/conn]SQLException: " + e.getMessage());
				}											
		}
	}
	
	public static int[] storeClientMonitor() throws SQLException {
		Connection conn = null;
		PreparedStatement prepared = null;
		
		String[] period = Talculator.getPeriod();
		
		userInfo [] uInfo = getMonitor(period);
		int total_request = getTotalReq(period);		
		int server_side_traffic = 0;
		
		try {
			conn = DBConnection.getInstance().getConnection();
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
		} catch (PropertyVetoException e) {
			System.out.println("[storeClientMonitor]PropertyVetoException: " + e.getMessage());
		} catch (SQLException e) {
			System.out.println("[storeClientMonitor]SQLException: " + e.getMessage());
			System.out.println("Rolling back data...");
			if (conn != null)
				conn.rollback();
		} catch (IOException e) {
			System.out.println("[storeClientMonitor]IOException: " + e.getMessage());
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
	
	private static userInfo[] getUserInfo () {
		Connection conn = null;
		PreparedStatement prepared = null;
		ResultSet rs = null;

		userInfo [] uInfo = null;
		
		try {
			conn = DBConnection.getInstance().getConnection();
			
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
		} catch (IOException e) {
			System.out.println("[getUserInfo]IOException: " + e.getMessage());			
		} catch (PropertyVetoException e) {
			System.out.println("[getUserInfo]PropertyVetoException: " + e.getMessage());
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
	
	private static HashMap<Integer, Integer> getTrafficLog(String[] period) {
		Connection conn = null;
		PreparedStatement prepared = null;
		ResultSet rs = null;				
		HashMap<Integer, Integer> tMap = new HashMap<Integer, Integer>();
		
		try {
			conn = DBConnection.getInstance().getConnection();
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
		} catch (PropertyVetoException e) {
			System.out.println("[getTrafficLog]PropertyVetoException: " + e.getMessage());
		} catch (SQLException e) {
			System.out.println("[getTrafficLog]SQLException: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("[getTrafficLog]IOException: " + e.getMessage());
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
	
	private static int getTotalReq(String[] period) {
		Connection conn = null;
		PreparedStatement prepared = null;
		ResultSet rs = null;				
		int total_req = 0;
		
		try {
			conn = DBConnection.getInstance().getConnection();
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
		} catch (PropertyVetoException e) {
			System.out.println("[getTotalReq]PropertyVetoException: " + e.getMessage());
		} catch (SQLException e) {
			System.out.println("[getTotalReq]SQLException: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("[getTotalReq]IOException: " + e.getMessage());
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
	
	public static void storeServerMonitor(int avgCPU, int[] server_side_monitor) throws SQLException {
		Connection conn = null;
		PreparedStatement prepared = null;				
		
		try {
			conn = DBConnection.getInstance().getConnection();
			prepared = conn.prepareStatement("INSERT INTO server_side_monitor "
					+ "(cpu_util, server_side_traffic, num_request) VALUES "
					+ "(?,?,?)");
						
			conn.setAutoCommit(false);
			
			int server_side_traffic = server_side_monitor[0];
			int num_request = server_side_monitor[1];
			
			prepared.setInt(1, avgCPU);
			prepared.setInt(2, server_side_traffic);
			prepared.setInt(3, num_request);
			
			prepared.executeUpdate();
			conn.commit();						
		} catch (PropertyVetoException e) {
			System.out.println("[storeServerMonitor]PropertyVetoException: " + e.getMessage());
		} catch (SQLException e) {
			System.out.println("[storeServerMonitor]SQLException: " + e.getMessage());
			System.out.println("Rolling back data...");
			if (conn != null)
				conn.rollback();
		} catch (IOException e) {
			System.out.println("[storeServerMonitor]IOException: " + e.getMessage());
		} finally {				
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					System.out.println("[storeServerMonitor/conn]SQLException: " + e.getMessage());
				}											
		}		
	}
	
	public static matchInfo[] getMatchResult() {
		Connection conn = null;
		PreparedStatement prepared = null;
		ResultSet rs = null;
		
		matchInfo[] match = null;
									
		try {		
			conn = DBConnection.getInstance().getConnection();
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
		} catch (PropertyVetoException e) {
			System.out.println("[getMatchRes]PropertyVetoException: " + e.getMessage());			
		}  catch (SQLException e) {
			System.out.println("[getMatchRes]SQLException: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("[getMatchRes]IOException: " + e.getMessage()); 
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					System.out.println("[getMatchRes/conn]SQLException: " + e.getMessage());					
				}		
		}
		return match;
	}
	
	@SuppressWarnings("unchecked")
	public static JSONObject getMigrated(String uname) throws SQLException {
		userInfo uInfo = getUserInfo(uname);						
		int uid = uInfo.getUID();
		String loc = uInfo.getLoc();
		
		statusInfo uStatus = getAllStatus(uid);		
		String[] status = uStatus.getStatusList();						
		String[] time = uStatus.getTimeList(); 			
		int[] traffic = uStatus.getTrafficList();
		
		JSONObject userItem = new JSONObject();
		userItem.put("UNAME", uname);
		userItem.put("LOCATION", loc);
					
		JSONArray statusList = new JSONArray();				
		for (int j = 0; j < status.length; j++) {
			JSONObject statusItem = new JSONObject();
			statusItem.put("STATUS", status[j]);
			statusItem.put("TIME", time[j]);
			statusItem.put("TRAFFIC", traffic[j]); 
			statusList.add(statusItem);
		}
		userItem.put("STATUS_LIST", statusList);						
		
		return userItem;		
	}
	
	private static userInfo getUserInfo(String uname) {
		Connection conn = null;
		PreparedStatement prepared = null;
		ResultSet rs = null;

		userInfo uInfo = new userInfo();
		
		try {
			conn = DBConnection.getInstance().getConnection();
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
		} catch (IOException e) {
			System.out.println("[getUserInfo]IOException: " + e.getMessage());			
		} catch (PropertyVetoException e) {
			System.out.println("[getUserInfo]PropertyVetoException: " + e.getMessage());
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
	
	private static statusInfo getAllStatus(int uid) throws SQLException {
		Connection conn = null;
		PreparedStatement prepared = null;
		ResultSet rs = null;
		
		int[] sids = null;
		String[] status = null;
		String[] time = null;
		int[] traffic = null;
									
		try {
			conn = DBConnection.getInstance().getConnection();
			prepared = conn.prepareStatement("SELECT sid,status,time,traffic FROM status WHERE "
					+ "uid = ?",
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			
			prepared.setInt(1, uid);			
			rs = prepared.executeQuery();
						
			int i = 0;
			rs.last();
			int rowCnt = rs.getRow();
			sids = new int[rowCnt];
			status = new String[rowCnt];
			time = new String[rowCnt];
			traffic = new int[rowCnt];
			rs.beforeFirst();
			while (rs.next()) {
				sids[i] = rs.getInt("sid");
				status[i] = rs.getString("status");
				time[i] = rs.getString("time");
				traffic[i] = rs.getInt("traffic");
				i++;
			}
		} catch (PropertyVetoException e) {
			System.out.println("[getAllStatus]PropertyVetoException: " + e.getMessage());			
		}  catch (SQLException e) {
			System.out.println("[getAllStatus]SQLException: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("[getAllStatus]IOException: " + e.getMessage()); 
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					System.out.println("[getAllStatus/conn]SQLException: " + e.getMessage());					
				}		
		}
		return new statusInfo(sids, status, time, traffic);
	}
	
	public static void deleteMigrated(String uname) throws SQLException {
		Connection conn = null;
		PreparedStatement prepared = null;		
				
		try {
			conn = DBConnection.getInstance().getConnection();
			prepared = conn.prepareStatement("DELETE FROM users WHERE "
					+ "uname = ?");
			
			conn.setAutoCommit(false);								
			prepared.setString(1, uname);																	
			prepared.executeUpdate();
			conn.commit();				
		} catch (PropertyVetoException e) {
			System.out.println("[deleteMigrated]PropertyVetoException: " + e.getMessage());
		} catch (SQLException e) {
			System.out.println("[deleteMigrated]SQLException: " + e.getMessage());
			System.out.println("Rolling back data...");
			if (conn != null)
				conn.rollback();
		} catch (IOException e) {
			System.out.println("[deleteMigrated]IOException: " + e.getMessage());
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