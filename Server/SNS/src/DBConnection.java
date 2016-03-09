import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import Wrapper.statusInfo;
import Wrapper.userInfo;

public class DBConnection {
	private static DBConnection mDS;
	private ComboPooledDataSource mCDPS;
	
	private final static int mResident = 1;	
	private final static int mBasicResponseSize = 22;	
	private final static int mPeriod = -1;
	
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
		mCDPS.setMaxPoolSize(18);
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
			
	public static int isThere(String uname, int check, String loc) throws PropertyVetoException, SQLException, IOException {
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
	
	public static int writeStatus(int uid, String msg, int reqSize) throws PropertyVetoException, SQLException, IOException {
		Connection conn = null;
		PreparedStatement prepared = null;
		
		try {
			conn = DBConnection.getInstance().getConnection();
			prepared = conn.prepareStatement("INSERT INTO status "
					+ "(uid, status, traffic) VALUES "
					+ "(?, ?, ?)");
			
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
	
	public static int readStatus(int uid, String uname, int reqSize, int num) throws PropertyVetoException, SQLException, IOException {
		int t_uid = getUID(uname);
		if (t_uid != -1) {
			statusInfo result = getStatus(t_uid, num);
		
			int [] t_sids = result.getSIDs();
			String t_status = result.getStatus();					
			storeLatent(uid, t_sids, reqSize, t_status.length());
		
			return mSuccess;
		} else
			return mFail;
	}
	
	public static int writeReply(int uid, String uname, String msg, int reqSize, int num) throws PropertyVetoException, SQLException, IOException {
		int t_uid = getUID(uname);
		if (t_uid != -1) {
			statusInfo result = getStatus(t_uid, num);
			
			int [] t_sids = result.getSIDs();		
			Random rand = new Random();
			if (t_sids.length > 0) {
				int picked = 0;
				if (t_sids.length != 1)
					picked = rand.nextInt(t_sids.length - 1);											
				return storeReply(uid, t_sids[picked], msg, reqSize);
			} else
				return mFail;
		} else {
			return mFail;
		}
	}

	// when we store the traffic info into the database
	// we should check whether the user's traffic info is 0 or not
	// if the traffic info is 0, we don't need to store the corresponding user
	public static userInfo [] getMonitor() {		
		userInfo [] uInfo = getUserInfo();
		HashMap<Integer, Integer> tInfo = getTrafficLog();						
		
		for (int i = 0; i < uInfo.length; i++) {
			int uid = uInfo[i].getUID();
			if (tInfo.get(uid) != null)
				uInfo[i].updateTraffic(tInfo.get(uid));
			
		}
		
		return uInfo;
	}
	
	private static void updateUser(int uid, int utype) throws PropertyVetoException, SQLException, IOException {
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
	
	private static int addUser(String uname, int utype, String loc) throws PropertyVetoException, SQLException, IOException {
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
				uid = rs.getInt("uid");
				type = rs.getInt("type");
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
		
		//is Resident (i.e. actual user of corresponding server)
		if ((type & mResident) == 1)
			return uid;
		else
			return -1;
	}
	
	private static statusInfo getStatus(int uid, int num) throws PropertyVetoException, SQLException, IOException {
		Connection conn = null;
		PreparedStatement prepared = null;
		ResultSet rs = null;
		
		int sids[] = null;
		String status = "";
									
		try {
			conn = DBConnection.getInstance().getConnection();
			prepared = conn.prepareStatement("SELECT sid,status FROM status WHERE "
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
			rs.beforeFirst();
			while (rs.next()) {
				sids[i] = rs.getInt("sid");
				status = status.concat(rs.getString("status"));			
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
				
		return new statusInfo(sids, status);
	}
	
	private static void storeLatent(int uid, int[] sids, int reqSize, int slen) throws SQLException {
		Connection conn = null;
		PreparedStatement prepared = null;				
		int t_sids[] = sids;
						
		try {
			conn = DBConnection.getInstance().getConnection();			
			prepared = conn.prepareStatement("INSERT INTO latent "
					+ "(uid, sid, traffic) VALUES "
					+ "(?, ?, ?)");
			
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
	}
	
	private static int storeReply(int uid, int sid, String msg, int reqSize) throws SQLException {
		Connection conn = null;
		PreparedStatement prepared = null;						
						
		try {
			conn = DBConnection.getInstance().getConnection();			
			prepared = conn.prepareStatement("INSERT INTO reply "
					+ "(uid, sid, reply, traffic) VALUES "
					+ "(?, ?, ?, ?)");
			
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
	
	private static userInfo[] getUserInfo () {
		Connection conn = null;
		PreparedStatement prepared = null;
		ResultSet rs = null;

		userInfo [] uInfo = null;
		
		try {
			conn = DBConnection.getInstance().getConnection();
			prepared = conn.prepareStatement("SELECT uid,uname,location FROM users",
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
						
			rs = prepared.executeQuery();
			
			int i = 0;
			rs.last();
			int rowCnt = rs.getRow();			
			uInfo = new userInfo [rowCnt];			
			rs.beforeFirst();
			while (rs.next()) {
				uInfo[i] = new userInfo();
				uInfo[i].setInfo(rs.getInt("uid"), rs.getString("uname"), rs.getString("location"));				
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
	
	private static HashMap<Integer, Integer> getTrafficLog() {
		Connection conn = null;
		PreparedStatement prepared = null;
		ResultSet rs = null;				
		HashMap<Integer, Integer> tMap = new HashMap<Integer, Integer>();
		
		try {
			conn = DBConnection.getInstance().getConnection();
			prepared = conn.prepareStatement("SELECT uid, sum(traffic) FROM ("
					+ "SELECT uid, traffic, time FROM status "
					+ "UNION ALL "
					+ "SELECT uid, traffic, time FROM reply "
					+ "UNION ALL "
					+ "SELECT uid, traffic, time FROM latent) x "
					+ "WHERE time BETWEEN ? AND ? "
					+ "GROUP BY uid");
						
			Date date = new Date();			
			SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");						
			Calendar cal = Calendar.getInstance();
			
			cal.setTime(date);			
			cal.add(Calendar.HOUR, mPeriod);			
			String start = f.format(cal.getTime());
			
			cal.setTime(date);
			String end = f.format(cal.getTime());
																		
			prepared.setString(1, start);
			prepared.setString(2, end);
			
			rs = prepared.executeQuery();						
						
			while(rs.next()) {
				int t_uid = rs.getInt("uid");
				int t_traffic = rs.getInt("sum(traffic)");				
				tMap.put(t_uid, t_traffic);				
			}					
		} catch (PropertyVetoException e) {
			System.out.println("[getStatusTraffic]PropertyVetoException: " + e.getMessage());
		} catch (SQLException e) {
			System.out.println("[getStatusTraffic]SQLException: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("[getStatusTraffic]IOException: " + e.getMessage());
		} finally {						
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					System.out.println("[getStatusTraffic/conn]SQLException: " + e.getMessage());
				}
		}
		return tMap;
	}
}