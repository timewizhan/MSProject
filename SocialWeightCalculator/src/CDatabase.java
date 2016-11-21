import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

public class CDatabase {

	static Logger log = Logger.getLogger(SocialWeight.class.getName());
	
	Connection brokerConn = null;
	String epUrl = null;
	
	public void connectBrokerDatabase(){
		
		try{
			Class.forName("com.mysql.jdbc.Driver");  //jdbc 드라이버로 연결하고 
			brokerConn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/broker_table?useSSL=false","root","cclab");
		//	System.out.println("Database was connected successfully");
		//	log.info("# CDatabase : CONNECT BROKER DATABASE SESSION");
		
		}catch(ClassNotFoundException cnfe){
			System.out.println("해당 클래스를 찾을수 없습니다."+cnfe.getMessage());
		}catch(SQLException se){
			System.out.println(se.getMessage());
		}
	}
	
	public CoordValue getLatitudeLongitude(String location){
		connectBrokerDatabase();
		
		CoordValue coordValue = new CoordValue();
		
		Statement stmt = null;
		try {
			stmt = brokerConn.createStatement();
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
		
		disconnectBrokerDatabase();
		return coordValue;
	}
	
	public void disconnectBrokerDatabase(){
		
		try {
			brokerConn.close();
		//	log.info("# CDatabase : DISCONNECT BROKER DATABASE SESSION");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static String toUnicode(String str){
		try{
			byte[] b=str.getBytes("ISO-8859-1");
			return new String(b);
		}
		catch(java.io.UnsupportedEncodingException uee){
			System.out.println(uee.getMessage());
			return null;
		}
	}
}
