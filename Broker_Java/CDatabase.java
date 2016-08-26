import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class CDatabase {

	Connection epConn = null;
	Connection brokerConn = null;
	String epUrl = null;
	
	public void connectEntryPointDatabase(Socket socket){
		
		String url = socket.getInetAddress().toString();
		epUrl = url.substring(1);
		String port = "3306";
		String dbName = "snsdb";
		String id = "root";
		String password = "cclab";
		
		try{
			Class.forName("com.mysql.jdbc.Driver");
			epConn = DriverManager.getConnection("jdbc:mysql:/" + url + ":" + port + "/" + dbName + "?autoReconnect=true&useSSL=false", id, password);
		//	epConn = DriverManager.getConnection("jdbc:mysql:/" + url + ":" + port + "/" + dbName, id, password);
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
			ResultSet rs = stmt.executeQuery("select server_side_traffic, cpu_util from server_side_monitor;");
			connectBrokerDatabase();
			
			while(rs.next()){
				String serverTraffic = rs.getString("server_side_traffic");
				String cpuUtil = rs.getString("cpu_util");
				
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
		//	System.out.println("Database was connected successfully");
		
		}catch(ClassNotFoundException cnfe){
			System.out.println("해당 클래스를 찾을수 없습니다."+cnfe.getMessage());
		}catch(SQLException se){
			System.out.println(se.getMessage());
		}
	}
	
	public void createTable(int ep_num){
	
	//	createDistanceTable(ep_num);
		createNormDistanceTable(ep_num);
	//	createNormSocialLevelTable(ep_num);
		createWeightTable(ep_num);
	}
	
	private void createDistanceTable(int ep_num){
		
		Statement stmt = null;
		String sql = null;
		try {
			// Statement 얻기
		    stmt = brokerConn.createStatement();
		    
		    sql = "CREATE TABLE distance_table ( " +
		    		"user varchar(100)";
		    for(int i=0; i<ep_num; i++){
		    	sql += ", ep" + (i+1) + " int";
		    }
		    sql += ");";
		    
		    stmt.executeUpdate(sql);
		    
		} catch (SQLException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	}
	
	private void createNormDistanceTable(int ep_num){
		
		Statement stmt = null;
		String sql = null;
		try {
		    // Statement 얻기
		    stmt = brokerConn.createStatement();
		    
		    sql = "CREATE TABLE normalized_distance_table ( " +
		    		"user varchar(100)";
		    for(int i=0; i<ep_num; i++){
		    	sql += ", ep" + (i+1) + " double";
		    }
		    sql += ");";
		    
		    stmt.executeUpdate(sql);
		    
		} catch (SQLException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	}
	
	private void createNormSocialLevelTable(int ep_num){
		
	}
	
	private void createWeightTable(int ep_num){
		
		Statement stmt = null;
		String sql = null;
		try {
		    // Statement 얻기
		    stmt = brokerConn.createStatement();
		    
		    sql = "CREATE TABLE weight_table ( " +
		    		"user varchar(100)," +
		    		"user_no int";
		    for(int i=0; i<ep_num; i++){
		    	sql += ", ep" + (i+1) + " double";
		    }
		    sql += ");";
		    
		    stmt.executeUpdate(sql);
		    
		} catch (SQLException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	}
	
	public void insertServerMonitoredResult(String serverTraffic, String cpuUtil){
		
		Statement stmt = null;
		
		try {
			stmt = brokerConn.createStatement();
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
	
	public int getServerTotalTraffic(){
		
		int totalTraffic = 0;
		
		Statement stmt = null;

		try {
			stmt = brokerConn.createStatement();
			ResultSet rs = stmt.executeQuery("select sum(server_side_traffic) from server_table;");

			while(rs.next()){
				totalTraffic = Integer.parseInt(rs.getString("sum(server_side_traffic)"));
			}

			rs.close();
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return totalTraffic;
	}
	
	public ArrayList<String> getServerList(){
		
		ArrayList<String> serverList = new ArrayList<String>();
		Statement stmt = null;

		try {
			stmt = brokerConn.createStatement();
			ResultSet rs = stmt.executeQuery("select ep from server_table;");

			while(rs.next()){
				String epAddr = rs.getString("ep");
				serverList.add(epAddr);
			}

			rs.close();
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return serverList;
	}
	
	public ArrayList<ServerInfo> getServerInfo(){
		
		ArrayList<ServerInfo> alServerData = new ArrayList<ServerInfo>();
		Statement stmt = null;

		try {
			stmt = brokerConn.createStatement();
			ResultSet rs = stmt.executeQuery("select ep from server_table;");	//ep의 ip주소 가져오기

			while(rs.next()){
				ServerInfo serverInfo = new ServerInfo();
				String epAddr = rs.getString("ep");
				
				//EP의 IP주소로 위치(Location) 알아내서, ServerInfo 객체에 저장
				serverInfo.mappingIpAddrToLocation(epAddr);
				//EP의 IP주소로 EP번호 알아내서, ServerInfo 객체에 저장
				serverInfo.mappingIpAddrToEpNo(epAddr);
				
				alServerData.add(serverInfo);
			}

			rs.close();
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return alServerData;
	}
	
	public int getEpNoWithIp(String ip){
		
		int epNo = 0;
		Statement stmt = null;
		
		try {
			stmt = brokerConn.createStatement();
			ResultSet rs = stmt.executeQuery("select ep from locationIP where ip = '" + ip +"';");

			while(rs.next()){
				epNo = Integer.parseInt(rs.getString("ep"));
			}

			rs.close();
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return epNo;
	}
	
	public String getLocationWithIp(String ip){
		
		Statement stmt = null;
		String location = null;
		try {
			stmt = brokerConn.createStatement();
			ResultSet rs = stmt.executeQuery("select state from locationIP where ip = '" + ip +"';");

			while(rs.next()){
				
				location = rs.getString("state");
			}

			rs.close();
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return location;
	}
	
	public CoordValue getLatitudeLongitude(String location){
		
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
				System.out.println("-------------"+location+"에 대한 위도/경도 정보가 없음");

			rs.close();
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return coordValue;
	}
	
	public ArrayList<ClientData> extractClientData(){
		
		ArrayList<ClientData> alClientData = new ArrayList<ClientData>();
		Statement stmt = null;

		try {
			stmt = brokerConn.createStatement();
			ResultSet rs = stmt.executeQuery("select user, location from client_table;");

			while(rs.next()){
				ClientData clientData = new ClientData();
				
				String userId = rs.getString("user");
				clientData.setUserID(userId);
				
				String userLocation = rs.getString("location");
				clientData.setUserLocation(userLocation);
			
				alClientData.add(clientData);
			}

			rs.close();
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return alClientData;
	}

	public ArrayList<ClientData> getUserList(){
		
		ArrayList<ClientData> userList = new ArrayList<ClientData>();
		Statement stmt = null;
		
		try {
			stmt = brokerConn.createStatement();
			ResultSet rs = stmt.executeQuery("select user from client_table;");

			while(rs.next()){
				ClientData userData = new ClientData();
				String sUserId = rs.getString("user");
				userData.setUserID(sUserId);
				userList.add(userData);
			}
			
			rs.close();
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return userList;
	}
	
	public int getUserTraffic(String userId){

		int traffic = 0;
		Statement stmt = null;

		try {
			stmt = brokerConn.createStatement();
			ResultSet rs = stmt.executeQuery("select client_side_traffic from client_table where user = '" + userId + "';");

			while(rs.next()){
				traffic = Integer.parseInt(rs.getString("client_side_traffic"));
			}

			rs.close();
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return traffic;
	}

	public ArrayList<ServerStatus> getServersStatus(HashMap<String , Integer> map, ArrayList<ArrayList<ClientTrafficData>> initUsrsOfClouds){

		ArrayList<ServerStatus> serverStateList = new ArrayList<ServerStatus>();
		ServerStatus eachServerState = null;
		int currTraffic = 0;
		String serverIp = null;
		Statement stmt = null;

		try {
			stmt = brokerConn.createStatement();
			ResultSet rs = stmt.executeQuery("select ep, server_side_traffic from server_table;");

			while(rs.next()){
				eachServerState = new ServerStatus();
				
				serverIp = rs.getString("ep");
				currTraffic = Integer.parseInt(rs.getString("server_side_traffic"));
				
				eachServerState.setServerIp(serverIp);
				eachServerState.setCurrentTraffic(currTraffic);
				eachServerState.setMaximumTraffic(map.get(serverIp));
				//epNo
				int epNo = getEpNoWithIp(serverIp);
				eachServerState.setEpNo(epNo);
				//expectedTraffic
				eachServerState.setExpectedTraffic(epNo, initUsrsOfClouds.get(epNo-1));
				
				serverStateList.add(eachServerState);
			}

			rs.close();
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return serverStateList;
	}
	
	public double getPrevTotalCloudsTraffic(double currTotalTraffic){

		double prevTotalTraffic = 0;
		Statement stmt = null;

		try {
			stmt = brokerConn.createStatement();
			ResultSet rs = stmt.executeQuery("select total_traffic from previous_server_traffic;");

			if(rs.next()){
				prevTotalTraffic = Integer.parseInt(rs.getString("total_traffic"));
			}else{
				prevTotalTraffic = currTotalTraffic;
			}
/*
			while(rs.next()){
				prevTotalTraffic = Integer.parseInt(rs.getString("total_traffic"));
			}
*/
			rs.close();
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return prevTotalTraffic;
	}
	
	public boolean checkPrevTotalTrafficExisting(){
		boolean totalTrafficExisting = false;
		
		Statement stmt = null;

		try {
			stmt = brokerConn.createStatement();
			ResultSet rs = stmt.executeQuery("select total_traffic from previous_server_traffic;");

			if(rs.next()){
				totalTrafficExisting = true;
			}else{
				totalTrafficExisting = false;
			}

			rs.close();
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return totalTrafficExisting;
	}
	
	public double getCurrTotalCloudsTraffic(){

		double currTotalTraffic = 0;
		Statement stmt = null;

		try {
			stmt = brokerConn.createStatement();
			ResultSet rs = stmt.executeQuery("select sum(server_side_traffic) from server_table;");

			while(rs.next()){
				currTotalTraffic = Integer.parseInt(rs.getString("sum(server_side_traffic)"));
			}

			rs.close();
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return currTotalTraffic;
	}
	
	public void updatePrevTotalCloudsTraffic(double currTotalTraffic){

		Statement stmt = null;
		String sql = null;
		try {
			stmt = brokerConn.createStatement();
		    sql = "update previous_server_traffic set total_traffic = " + currTotalTraffic;
		    stmt.executeUpdate(sql);
		} catch (SQLException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
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
	
	public void insertPrevTotalCloudsTraffic(double currTotalTraffic){

		Statement stmt = null;
		
		try {
			stmt = brokerConn.createStatement();
			stmt.executeUpdate("insert into previous_server_traffic values("+currTotalTraffic+");");
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void insertNormDistanceData(String userID, double [] normalizedDistances){

		Statement stmt = null;
		String sql = null;
		try {
			// Statement 얻기
		    stmt = brokerConn.createStatement();
		    
		    sql = "insert into normalized_distance_table values ( " + "'" + userID + "'";
		    for(int i=0; i<normalizedDistances.length; i++){
		    	sql += ", " + normalizedDistances[i];
		    }
		    sql += ");";
		    
		    stmt.executeUpdate(sql);
		    
		} catch (SQLException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	}
	
	public double getNormalizedSocialWeightValue(String userId, int epNo){
		double normalizedValue = 0;
		Statement stmt = null;

		try {
			stmt = brokerConn.createStatement();
			ResultSet rs = stmt.executeQuery("select * from normalized_distance_table where user = '" + userId + "';");

			while(rs.next()){
				normalizedValue = Double.parseDouble(rs.getString("ep"+epNo));
			}

			rs.close();
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return normalizedValue;
	}
	
	public double [] getNormalizedDistanceValues(String userId, int NumOfEp){
	
		double normDistValues [] = new double [NumOfEp];
		Statement stmt = null;
		
		try {
			stmt = brokerConn.createStatement();
			ResultSet rs = stmt.executeQuery("select * from normalized_distance_table where user = '" + userId + "';");

			while(rs.next()){
				for(int i=0; i<NumOfEp; i++){
					normDistValues[i] = Double.parseDouble(rs.getString("ep"+(i+1)));
				}
			}
			
			rs.close();
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return normDistValues;
	}
	
	public double [] getNormalizedSocialWeightValues(String userId, int NumOfEp){
		
		double normDistValues [] = new double [NumOfEp];
		Statement stmt = null;
		
		try {
			stmt = brokerConn.createStatement();
			ResultSet rs = stmt.executeQuery("select * from normalized_social_level_table where user = '" + userId + "';");

			while(rs.next()){
				for(int i=0; i<NumOfEp; i++){
					normDistValues[i] = Double.parseDouble(rs.getString("ep"+(i+1)));
				}
			}
			
			rs.close();
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return normDistValues;
	}
	
	public void updateLocationIpTable(){
		
		Statement stmt = null;
		String sql = null;
		
		try {
			stmt = brokerConn.createStatement();
			ResultSet rs = stmt.executeQuery("select ip from locationIP;");
			
			int seq=1;
			while(rs.next()){
				updateEpNoInLocationIpTable(rs.getString("ip"), seq);
				seq++;
			}
			rs.close();
			stmt.close();

		} catch (SQLException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	}
	
	public void updateEpNoInLocationIpTable(String serverIpAddr, int seq){
		
		Statement stmt = null;
		String sql = null;
		try {
			stmt = brokerConn.createStatement();
		    sql = "update locationIP set ep=" + seq + " where ip='" + serverIpAddr + "'";
		    stmt.executeUpdate(sql);
		} catch (SQLException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	}
	
	public void disconnectBrokerDatabase(){
		
		try {
			brokerConn.close();
		//	System.out.println("Disconnect Broker database session");
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
