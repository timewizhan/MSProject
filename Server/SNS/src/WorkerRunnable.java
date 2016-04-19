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
	        	int result = operationHandler(reqType, request);
	        	System.out.println(getTime() + " " + ServiceServer.mCoord.getServerLoc()
    			+ " is handling the request from " 
				+ "[" + request.get("SRC")
				+ "(" + opType.getOperationName(reqType) + ")" + "]");
	        	response = MessageHandler.msgGenerator(result);	        	
        	} else {        		
        		System.out.println(getTime() + " " + ServiceServer.mCoord.getServerLoc()
				+ " is handling the command "
				+ "[" + opType.getOperationName(reqType) + "]");
        		response = commandHanlder(reqType, request);	
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
    
    private int operationHandler(int reqType, JSONObject request) throws PropertyVetoException, SQLException, IOException, InterruptedException {		
		int uid = -1;
		int reqSize = request.toString().length();
		int result = 0;		
										
		String src = (String) request.get("SRC");		
		String dst = (String) request.get("DST");
		String loc = (String) request.get("LOC");
		String msg = (String) request.get("MSG");
		
		switch (reqType) {                                                                                                                                                                                                      
			case opType.mTWEET:				
				uid = DBConnection.isThere(src, userType.resident, loc);			
				result = DBConnection.writeStatus(uid, msg, reqSize);				
				break;
			case opType.mREAD:
				uid = DBConnection.isThere(src, userType.visitor, loc);
				result = DBConnection.readStatus(uid, dst, reqSize, opType.mNUM_READ);				
				break; 
			case opType.mREPLY:
				uid = DBConnection.isThere(src, userType.visitor, loc);			
				result = DBConnection.writeReply(uid, dst, msg, reqSize, opType.mNUM_SHARE);				
				break;
			case opType.mRETWEET:
				uid = DBConnection.isThere(src, userType.visitor, loc);
				result = DBConnection.readStatus(uid, dst, reqSize, opType.mNUM_SHARE);				
				break;		
			default:
				System.out.println(getTime() + " ERROR: Invalid Operation " + reqType);				
				break;
		}
		return result;
	}
    
    private String commandHanlder(int reqType, JSONObject request) throws SQLException {    	
    	String result = null;
    	
    	switch (reqType) {
	    	case opType.mMONITOR:
	    		CpuMonitor.storeMonitored();
	    		System.out.println(getTime() + " " + ServiceServer.mCoord.getServerLoc()
	    							+ " stored monitoring result");
	    		result = MessageHandler.mSTORE_COMPLETE;	    		
	    		break;	    	
	    	case opType.mMOVEOUT:
	    		matchInfo[] match = DBConnection.getMatchResult();	   			    		
	    		
	    		if (match != null) {
		    		for (int i = 0; i < match.length; i++) {
		    			String uname = match[i].getName();
		    			int curr = match[i].getCurr();	    				    				    			
		    			JSONObject migrated = DBConnection.getMigrated(uname);
		    			
		    			DBConnection.deleteMigrated(uname);
		    			
		    			int res = MessageHandler.sendMigrated(curr, migrated);	    			
		    			if (res == 0)
		    				System.out.println(getTime() + ServiceServer.mCoord.getServerLoc()
		    									+ "failed sending the data " + "[" + uname +"]");
		    			else
		    				System.out.println(getTime() + " " + ServiceServer.mCoord.getServerLoc() 
		    									+ " sent the data "
		    									+ "[" + uname + "]");	    										    										
		    		}
	    		}
	    		
	    		result = MessageHandler.mDATA_REPLACEMENT_COMPLETE;
	    		CpuMonitor.startCpuMonitor();
	    		System.out.println(getTime() + " " + ServiceServer.mCoord.getServerLoc() 
									+ " restarted the cpu monitoring");
	    		break;
	    	case opType.mMOVEIN:   		
	    		JSONObject userItem = (JSONObject) request.get("MIGRATED");	    			    			    		
    			String uname = (String) userItem.get("UNAME");
    			String loc = (String) userItem.get("LOCATION");
    			JSONArray statusList = (JSONArray) userItem.get("STATUS_LIST");
    				    			
    			int uid = DBConnection.isThere(uname, userType.resident, loc);
    			result = MessageHandler.msgGenerator(DBConnection.writeStatus(uid, statusList)); 
    			
    			System.out.println(getTime() + " " + ServiceServer.mCoord.getServerLoc() 
									+ " received the data "									
									+ "[" + uname + "]");									
	    		break;	    	
	    	case opType.mRESTART:
	    		CpuMonitor.startCpuMonitor();
	    		result = MessageHandler.mRESTART_CPU_MONITORING;
	    		System.out.println(getTime() + " " + ServiceServer.mCoord.getServerLoc() 
									+ " restarts cpu monitoring");
	    		break;	    		
	    	default:
	    		System.out.println(getTime() + " ERROR: Invalid Operation " + reqType);
	    		result = opType.getOperationName(reqType);
	    		break;
    	}
    	return result;
    }                
    
    private String getTime() {
    	SimpleDateFormat f = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss.SSS]");
    	return f.format(new Date());
    }
}
