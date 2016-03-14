import java.beans.PropertyVetoException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import Type.opType;
import Type.userType;
import Utility.CoordHandler;
import Utility.MessageHandler;

public class WorkerRunnable implements Runnable {
    private Socket mClientSocket = null;    

    public WorkerRunnable(Socket clientSocket) {
    	mClientSocket = clientSocket;
    }

    public void run() {
        JSONObject request  = MessageHandler.msgParser(mClientSocket);
             
        try {
        	if (Integer.parseInt((String) request.get("TYPE")) < 5) {
	        	int result = operationHandler(request);
				String response = MessageHandler.msgGenerator(result);
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
						mClientSocket.getOutputStream(), "UTF-8"));	
				
				Thread.sleep(CoordHandler.calRTT(ServiceServer.mCoord, (String) request.get("LOC")));
				
				out.write(response);
				out.newLine();
				out.flush();
				mClientSocket.close();
				out.close();
				System.out.println(getTime() + ServiceServer.mCoord.getServerLoc() + " handled a request from " + "[" + request.get("SRC") + "]");
        	} else {
        		commandHanlder(request);
        	} 							
		} catch (PropertyVetoException e) {			
			e.printStackTrace();
		} catch (SQLException e) {			
			e.printStackTrace();
		} catch (IOException e) {			
			e.printStackTrace();
		} catch (InterruptedException e) {			
			e.printStackTrace();
		}
    }
    
    private int operationHandler(JSONObject request) throws PropertyVetoException, SQLException, IOException, InterruptedException {		
		int uid = -1;
		int reqSize = request.toString().length();
		int res = 0;		
						
		int reqType = Integer.parseInt((String) request.get("TYPE"));	
		
		String src = (String) request.get("SRC");		
		String dst = (String) request.get("DST");
		String loc = (String) request.get("LOC");
		String msg = (String) request.get("MSG");
		
		switch (reqType) {                                                                                                                                                                                                      
			case opType.tweet:				
				uid = DBConnection.isThere(src, userType.resident, loc);			
				res = DBConnection.writeStatus(uid, msg, reqSize);			
				break;
			case opType.read:
				uid = DBConnection.isThere(src, userType.visitor, loc);
				res = DBConnection.readStatus(uid, dst, reqSize, opType.num_read);
				break; 
			case opType.reply:
				uid = DBConnection.isThere(src, userType.visitor, loc);			
				res = DBConnection.writeReply(uid, dst, msg, reqSize, opType.num_read);
				break;
			case opType.retweet:
				uid = DBConnection.isThere(src, userType.visitor, loc);
				res = DBConnection.readStatus(uid, dst, reqSize, opType.num_share);
				break;		
			default:
				System.out.println("[ERROR] Invalid Operation Type: " + reqType);			
				break;
		}
		return res;
	}
    
    private void commandHanlder(JSONObject request) throws SQLException {
    	int reqType = Integer.parseInt((String) request.get("TYPE"));
    	
    	switch (reqType) {
	    	case opType.monitor:
	    		storeMonitored();	    		
	    		break;	    	
	    	case opType.moveout:
	    		JSONArray result = DBConnection.getMigrated();	    		
	    		sendMigrated(result);	    		
	    		break;
	    	case opType.movein:
	    		System.out.println("Welcome home! " + request.toString());
	    		break;
	    	case opType.restart:
	    		CpuMonitor.startCpuMonitor();
	    		break;
	    	default:
	    		System.out.println("[ERROR] Invalid Operation Type: " + reqType);
	    		break;
    	}    	
    }
    
    private void storeMonitored() throws SQLException {
    	CpuMonitor.stopScheduler(ServiceServer.mScheduler);
						
		int totalCPU = 0;		
		for(int i = 0; i < ServiceServer.mCPU_Log.size(); i++) {
			totalCPU += ServiceServer.mCPU_Log.get(i);
		}
		
		int avgCPU = totalCPU / ServiceServer.mCPU_Log.size();
		int server_side_traffic = DBConnection.storeClientMonitor();
		DBConnection.storeServerMonitor(avgCPU, server_side_traffic);				
    }
    
    private void sendMigrated(JSONArray migrated) {
    	String dstServerIP = "localhost";
    	int dstServerPort = 7777;
    	
    	try {
			Socket socket = new Socket(dstServerIP, dstServerPort);
			
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream(), "UTF-8"));
			
			String response = MessageHandler.msgGenerator(migrated);
			
			out.write(response);
			out.newLine();
			out.flush();
			
			socket.close();
			out.close();
		} catch (IOException e) {
			System.out.println("[sendMigrated]IOException: " + e.getMessage());
		}
    	
    }
    
    private String getTime() {
    	SimpleDateFormat f = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss.SSS]");
    	return f.format(new Date());
    }
}
