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
import Wrapper.matchInfo;

public class WorkerRunnable implements Runnable {
    private Socket mClientSocket = null;    

    public WorkerRunnable(Socket clientSocket) {
    	mClientSocket = clientSocket;    	
    }

    public void run() {
        JSONObject request  = MessageHandler.msgParser(mClientSocket);             
        int reqType = Integer.parseInt((String) request.get("TYPE"));         
        BufferedWriter out = null;
        String response = null;
        boolean sleepFlag = false;
       
        try {
        	if (reqType < 5) {
        		sleepFlag = true;
	        	int result = operationHandler(request);	        	
	        	System.out.println(getTime() + ServiceServer.mCoord.getServerLoc() + " is handling the request from " + "[" + request.get("SRC") + "]");
	        	response = MessageHandler.msgGenerator(result);	        									
        	} else {
        		System.out.println(getTime() + ServiceServer.mCoord.getServerLoc() + " is handling the command " + reqType);
        		response = commandHanlder(request);        					        		
        	}        	        			
        	out = new BufferedWriter(new OutputStreamWriter(
					mClientSocket.getOutputStream(), "UTF-8"));	
			
        	if(sleepFlag)
        		Thread.sleep(CoordHandler.calRTT(ServiceServer.mCoord, (String) request.get("LOC")));
			
			out.write(response);
			out.newLine();
			out.flush();							        	
		} catch (PropertyVetoException e) {			
			e.printStackTrace();
		} catch (SQLException e) {			
			e.printStackTrace();
		} catch (IOException e) {			
			e.printStackTrace();
		} catch (InterruptedException e) {			
			e.printStackTrace();
		} finally {
			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if (mClientSocket != null)
				try {
					mClientSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
    }
    
    private int operationHandler(JSONObject request) throws PropertyVetoException, SQLException, IOException, InterruptedException {		
		int uid = -1;
		int reqSize = request.toString().length();
		int result = 0;		
						
		int reqType = Integer.parseInt((String) request.get("TYPE"));		
		String src = (String) request.get("SRC");		
		String dst = (String) request.get("DST");
		String loc = (String) request.get("LOC");
		String msg = (String) request.get("MSG");
		
		switch (reqType) {                                                                                                                                                                                                      
			case opType.tweet:				
				uid = DBConnection.isThere(src, userType.resident, loc);			
				result = DBConnection.writeStatus(uid, msg, reqSize);				
				break;
			case opType.read:
				uid = DBConnection.isThere(src, userType.visitor, loc);
				result = DBConnection.readStatus(uid, dst, reqSize, opType.num_read);				
				break; 
			case opType.reply:
				uid = DBConnection.isThere(src, userType.visitor, loc);			
				result = DBConnection.writeReply(uid, dst, msg, reqSize, opType.num_read);				
				break;
			case opType.retweet:
				uid = DBConnection.isThere(src, userType.visitor, loc);
				result = DBConnection.readStatus(uid, dst, reqSize, opType.num_share);				
				break;		
			default:
				System.out.println("[ERROR] Invalid Operation Type: " + reqType);			
				break;
		}		
		return result;
	}
    
    private String commandHanlder(JSONObject request) throws SQLException {
    	int reqType = Integer.parseInt((String) request.get("TYPE"));
    	String result = null;
    	    	
    	switch (reqType) {
	    	case opType.monitor:
	    		CpuMonitor.storeMonitored();
	    		result = MessageHandler.store_complete;	    		
	    		break;	    	
	    	case opType.moveout:
	    		matchInfo[] match = DBConnection.getMatchResult();	    			    		
	    		
	    		for (int i = 0; i < match.length; i++) {
	    			String uname = match[i].getName();
	    			int curr = match[i].getCurr();	    				    				    			
	    			JSONObject migrated = DBConnection.getMigrated(uname);
	    			
	    			DBConnection.deleteMigrated(uname);
	    			
	    			int res = MessageHandler.sendMigrated(curr, migrated);
	    			if (res != 1)
	    				System.out.println(getTime() + ServiceServer.mCoord.getServerLoc() + "has received data replacement failed msg " + "[" + uname +"]");
	    		}
	    		result = MessageHandler.data_replacement_complete;
	    		CpuMonitor.startCpuMonitor();	    		
	    		break;
	    	case opType.movein:   		
	    		JSONObject userItem = (JSONObject) request.get("MIGRATED");	    			    			    		
    			String uname = (String) userItem.get("UNAME");
    			String loc = (String) userItem.get("LOCATION");
    			JSONArray statusList = (JSONArray) userItem.get("STATUS_LIST");
    				    			
    			int uid = DBConnection.isThere(uname, userType.resident, loc);
    			result = MessageHandler.msgGenerator(DBConnection.writeStatus(uid, statusList));    					    			
	    		break;	    	
	    	default:
	    		System.out.println("[ERROR] Invalid Operation Type: " + reqType);
	    		break;
    	}
    	return result;
    }                
    
    private String getTime() {
    	SimpleDateFormat f = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss.SSS]");
    	return f.format(new Date());
    }
}
