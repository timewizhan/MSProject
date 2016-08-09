import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;

public class CDatabase {

	Connection epConn = null;
	Connection brokerConn = null;
	String epUrl = null;
	
	public void connectEntryPointDatabase(Socket socket){
		
		String url = socket.getInetAddress().toString();
		epUrl = url.substring(1);
		String port = "3306";
		String dbName = "sns_db";
		String id = "root";
		String password = "cclab";
		
		try{
			Class.forName("com.mysql.jdbc.Driver");
			epConn = DriverManager.getConnection("jdbc:mysql:/" + url + ":" + port + "/" + dbName + "?autoReconnect=true&useSSL=false", id, password); 
			System.out.println("Entry Point database was connected successfully");

		}catch(ClassNotFoundException cnfe){
			System.out.println("해당 클래스를 찾을수 없습니다."+cnfe.getMessage());

		}catch(SQLException se){
			System.out.println(se.getMessage());

		}
		
	}
	
	public void extractServerMonitoredResult(){
		
		Statement stmt= null;
		
		try {
			stmt = epConn.createStatement();
			ResultSet rs = stmt.executeQuery("select server_side_traffic, cpu_utilization from server_side_monitor;");
			connectBrokerDatabase();
			
			while(rs.next()){
				String serverTraffic = rs.getString("server_side_traffic");
				String cpuUtil = rs.getString("cpu_utilization");
				
				insertServerMonitoredResult(serverTraffic, cpuUtil);
			}
			
			rs.close();
			stmt.close();
			disconnectBrokerDatabase();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void extractClientMonitoredResult(){
		
		Statement stmt= null;
		
		try {
			stmt = epConn.createStatement();
			ResultSet rs = stmt.executeQuery("select user, location, client_side_traffic from client_side_monitor;");
			connectBrokerDatabase();
			
			while(rs.next()){
				String user = rs.getString("user");
				String location = rs.getString("location");
				String cst = rs.getString("client_side_traffic");
				
				insertClientMonitoredResult(user, location, cst);
			}
			
			rs.close();
			stmt.close();
			disconnectBrokerDatabase();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void disconnectEntryPointDatabase(){
		
		try {
			epConn.close();
			System.out.println("Disconnect EntryPoint database session");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void connectBrokerDatabase(){
		
		try{
			Class.forName("com.mysql.jdbc.Driver");  //jdbc 드라이버로 연결하고 
			brokerConn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/broker_table?autoReconnect=true&useSSL=false","root","cclab");
			System.out.println("Database was connected successfully");
		
		}catch(ClassNotFoundException cnfe){
			System.out.println("해당 클래스를 찾을수 없습니다."+cnfe.getMessage());
		}catch(SQLException se){
			System.out.println(se.getMessage());
		}
	}
	
	public void insertServerMonitoredResult(String serverTraffic, String cpuUtil){
		
		Statement stmt = null;
		
		try {
			stmt = brokerConn.createStatement();
			System.out.println("EP URL : " + epUrl);
			stmt.executeUpdate("insert into server_table values('" + epUrl + "'," + Integer.parseInt(serverTraffic) + "," + Integer.parseInt(cpuUtil) + ");");
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void insertClientMonitoredResult(String user, String location, String cst){
		
		Statement stmt = null;
		
		try {
			stmt = brokerConn.createStatement();
			stmt.executeUpdate("insert into client_table values('" + user + "','" + location + "'," + Integer.parseInt(cst) + ");");
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ArrayList<ServerData> extractServerData(){
		
		ArrayList<ServerData> alServerData = new ArrayList<ServerData>();
		Statement stmt = null;

		try {
			stmt = brokerConn.createStatement();
			ResultSet rs = stmt.executeQuery("select ep, server_side_traffic, cpu_utilization from server_table;");

			while(rs.next()){
				ServerData serverData = new ServerData();
				
				String epAddr = rs.getString("ep");
				serverData.setEpAddr(epAddr);
				
				String sServerTraffic = rs.getString("server_side_traffic");
				int iServerTraffic = Integer.parseInt(sServerTraffic);
				serverData.setServerTraffic(iServerTraffic);
			
				alServerData.add(serverData);
				
				//min, max 찾기
				if(rs.isFirst()){
					ServerData.minServerTraffic = iServerTraffic;
					ServerData.maxServerTraffic = iServerTraffic;
					
				} else {
					if(ServerData.minServerTraffic >= iServerTraffic)
						ServerData.minServerTraffic = iServerTraffic;
					
					if(ServerData.maxServerTraffic <= iServerTraffic)
						ServerData.maxServerTraffic = iServerTraffic;
				}
			}

			rs.close();
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return alServerData;
	}
	
	public ArrayList<ServerData> extractLocation(){
		
		ArrayList<ServerData> alServerData = new ArrayList<ServerData>();
		Statement stmt = null;

		try {
			stmt = brokerConn.createStatement();
			ResultSet rs = stmt.executeQuery("select ep, server_side_traffic, cpu_utilization from server_table;");

			while(rs.next()){
				ServerData serverData = new ServerData();
				
				String epAddr = rs.getString("ep");
				serverData.setEpAddr(epAddr);
				
				String sServerTraffic = rs.getString("server_side_traffic");
				int iServerTraffic = Integer.parseInt(sServerTraffic);
				serverData.setServerTraffic(iServerTraffic);
			
				alServerData.add(serverData);
				
				//min, max 찾기
				if(rs.isFirst()){
					ServerData.minServerTraffic = iServerTraffic;
					ServerData.maxServerTraffic = iServerTraffic;
					
				} else {
					if(ServerData.minServerTraffic >= iServerTraffic)
						ServerData.minServerTraffic = iServerTraffic;
					
					if(ServerData.maxServerTraffic <= iServerTraffic)
						ServerData.maxServerTraffic = iServerTraffic;
				}
			}

			rs.close();
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return alServerData;
	}
	
	public void insertNormServerData(String epAddr, double normalizedServerTraffic){
		
		Statement stmt = null;
		
		try {
			stmt = brokerConn.createStatement();
			stmt.executeUpdate("insert into norm_server_table values('" + epAddr + "'," + normalizedServerTraffic + "," + 0 + ");");
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void disconnectBrokerDatabase(){
		
		try {
			brokerConn.close();
			System.out.println("Disconnect Broker database session");
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
