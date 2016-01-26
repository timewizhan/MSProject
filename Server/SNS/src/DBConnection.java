import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DBConnection {
	private static DBConnection mDS;
	private ComboPooledDataSource mCDPS;
	
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
			
	public static int isThere(String uname, int ver) throws PropertyVetoException, IOException {
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
				System.out.println("isThere: User already exists");
				uid = rs.getInt("uid");
				utype = rs.getInt("type");
			}
		
			if (uid != -1) {				
				if((utype & ver) == 0) {
					System.out.println("isThere: Call updateUser()");
					updateUser(uid, (utype | ver));
				}
			} else {
				System.out.println("isThere: Call addUser()");
				uid = addUser(uname, ver);
			}
		} catch (SQLException e) {
			System.out.println("[isThere]SQLException: " + e.getMessage());
		} finally {			
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					System.out.println("[isThere]SQLException: " + e.getMessage());
				}
			
			if (prepared != null )
				try {
					prepared.close();
				} catch (SQLException e) {
					System.out.println("[isThere]SQLException: " + e.getMessage());
				}
			
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					System.out.println("[isThere]SQLException: " + e.getMessage());
				}
			
			System.out.println("isThere: Done");
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
			
			prepared.setInt(1, utype);
			prepared.setInt(2, uid);
			prepared.executeUpdate();
		} catch (SQLException e) {
			System.out.println("[updateUser]SQLException: " + e.getMessage());
		} finally {
			if (prepared != null)
				try {
					prepared.close();
				} catch (SQLException e) {
					System.out.println("[updateUser/prepare]SQLException: " + e.getMessage());
				}
			
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					System.out.println("[updateUser/conn]SQLException: " + e.getMessage());
				}						
		}
	}
	
	private static int addUser(String uname, int utype) throws PropertyVetoException, SQLException, IOException {
		Connection conn = null;
		PreparedStatement prepared = null;
		ResultSet rs = null;		
		int uid = -1;		
		
		try {
			conn = DBConnection.getInstance().getConnection();
			prepared = conn.prepareStatement("INSERT INTO users "
					+ "(uname, type) VALUES "
					+ "(?,?)", Statement.RETURN_GENERATED_KEYS);
			
			prepared.setString(1, uname);
			prepared.setInt(2, utype);
			prepared.executeUpdate();
			
			rs = prepared.getGeneratedKeys();
			
			if (rs.next())
				uid = rs.getInt(1);
			
		} catch (SQLException e) {
			System.out.println("[addUser]SQLException: " + e.getMessage());
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					System.out.println("[addUser/rs]SQLException: " + e.getMessage());
				}
			
			if (prepared != null)
				try {
					prepared.close();
				} catch (SQLException e) {
					System.out.println("[addUser/prepared]SQLException: " + e.getMessage());
				}
			
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					System.out.println("[addUser/conn]SQLException: " + e.getMessage());
				}						
			System.out.println("addUser: Done");			
		}
		return uid;
	}
	
	
	public static String writeStatus(int uid, String msg, String loc) throws PropertyVetoException, IOException {
		Connection conn = null;
		PreparedStatement prepared = null;
		
		try {
			conn = DBConnection.getInstance().getConnection();
			prepared = conn.prepareStatement("INSERT INTO status "
					+ "(uid, status, time, location) VALUES "
					+ "(?, ?, ?, ?)");
			
			prepared.setInt(1, uid);							
			prepared.setString(2, msg);				
			prepared.setString(3, getTime());
			prepared.setString(4, loc);							
			prepared.executeUpdate();	
			
		} catch (SQLException e) {
			System.out.println("[writeStatus]SQLException: " + e.getMessage());
		} finally {
			if (prepared != null)
				try {
					prepared.close();
				} catch (SQLException e) {
					System.out.println("[writeStatus/prepared]SQLException: " + e.getMessage());
				}
			
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					System.out.println("[writeStatus/conn]SQLException: " + e.getMessage());
				}						
		}
		
		return "Success";
	}
	
	private static String getTime() {
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return f.format(new Date());
	}
}