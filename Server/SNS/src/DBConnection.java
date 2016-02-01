import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DBConnection {
	private static DBConnection mDS;
	private ComboPooledDataSource mCDPS;
	
	private final static int mBasicResponseSize = 22;
	
	private DBConnection() throws IOException, SQLException, PropertyVetoException {
		mCDPS = new ComboPooledDataSource();
		mCDPS.setDriverClass("com.mysql.jdbc.Driver");
		mCDPS.setJdbcUrl("jdbc:mysql://localhost/snsdb?autoReconnect=true&useSSL=false");
		mCDPS.setUser("snsuser");
		mCDPS.setPassword("password");
		
		// the settings below are optional
		// c3p0 can work with defaults
		mCDPS.setMinPoolSize(5);
		mCDPS.setAcquireIncrement(5);
		mCDPS.setMaxPoolSize(10);
		mCDPS.setMaxStatements(180);
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
	
	public static String writeStatus(int uid, String msg, int reqSize) throws PropertyVetoException, SQLException, IOException {
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
		return "Success";
	}
	
	public static String readStatus(int uid, String uname, int reqSize, int num) throws PropertyVetoException, SQLException, IOException {
		int t_uid = getUID(uname);
		statusInfo result = getStatus(t_uid, num);
		
		int [] t_sids = result.getSIDs();
		String t_status = result.getStatus();					
		storeLatent(uid, t_sids, reqSize, t_status.length());
		
		return t_status;
	}
	
	private static int getUID(String uname) {
		Connection conn = null;
		PreparedStatement prepared = null;
		ResultSet rs = null;
		int uid = -1;
	
		try {
			conn = DBConnection.getInstance().getConnection();
			prepared = conn.prepareStatement("SELECT uid FROM users WHERE "
					+ "uname = ?");
			
			prepared.setString(1, uname);
			rs = prepared.executeQuery();
			
			if (rs.next())				
				uid = rs.getInt("uid");				
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
			System.out.println("[getStatus]PropertyVetoException: " + e.getMessage());			
		} catch (SQLException e) {
			System.out.println("[getStatus]SQLException: " + e.getMessage());
			System.out.println("Rolling back data...");
			if (conn != null)
				conn.rollback();
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
	}
	
	public static String writeReply(int uid, String uname, String msg, int reqSize, int num) throws PropertyVetoException, SQLException, IOException {
		int t_uid = getUID(uname);		
		statusInfo result = getStatus(t_uid, num);
			
		int [] t_sids = result.getSIDs();		
		Random rand = new Random();		
		int picked = rand.nextInt(t_sids.length - 1);											
		return storeReply(uid, t_sids[picked], msg, reqSize);
	}
	
	private static String storeReply(int uid, int sid, String msg, int reqSize) throws SQLException {
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
		return "Success";
	}		
}