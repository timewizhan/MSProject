import java.beans.PropertyVetoException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PropertyConfigurator;

import org.json.simple.JSONObject;

import Type.opType;
import Type.userType;
import Utility.CoordHandler;
import Utility.MessageHandler;

/**
 * The Class WorkerRunnable.
 * Handles the request that service server received
 * 
 */
public class WorkerRunnable implements Runnable {
    
	static Logger log = Logger.getLogger(ServiceServer.class.getName());				//initiate logger
	
    private Socket mClientSocket = null;    

    /**
     * Instantiates a new worker runnable.
     *
     * @param clientSocket the accepted socket
     */
    public WorkerRunnable(Socket clientSocket) {
    	mClientSocket = clientSocket; 	    			
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {    	    	
        JSONObject request  = MessageHandler.msgParser(mClientSocket);        
        int reqType = Integer.parseInt((String) request.get("TYPE"));        
        BufferedWriter out = null;
        String response = null;        
       
        try {
        	if (reqType == opType.mMONITOR) {
        		System.out.println(getTime() + " " + ServiceServer.mCoord.getServerLoc()
									+ " is handling the command "
									+ "[" + opType.getOperationName(reqType) + "]");        		
        		response = commandHanlder(reqType, request);
        	} else {
        		int result = operationHandler(reqType, request);
	        	System.out.println(getTime() + " " + ServiceServer.mCoord.getServerLoc()
    								+ " is handling the request from " 
    								+ "[" + request.get("SRC")
    								+ "(" + opType.getOperationName(reqType) + ")" + "]");
	        	
	        	//유저가 Write operation 하는 거에 대해서 횟수를 카운트 한다
	        	// 1 : tweet, 3 : reply, 4 : retweet
	        	if(reqType == 1) {
	        		Counter.tweet++;
	        	}
	        	else if(reqType == 2) {
	        		Counter.read++;
	        	}
	        	else if(reqType == 3) {
	        		Counter.reply++;
	        	}
	        	else if(reqType == 4) {	 
	        		Counter.retweet++;
	        	}
	        	
	        	double delay = CoordHandler.calRTT(ServiceServer.mCoord, (String) request.get("LOC"));
	        	response = MessageHandler.msgGenerator(result, delay);
        	//	Thread.sleep((long)delay);
        	}
        	
        	out = new BufferedWriter(new OutputStreamWriter(
					mClientSocket.getOutputStream(), "UTF-8"));				        	        	       			
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
    
    /**
     * Handles the user's request
     *
     * @param reqType the request type
     * @param request the request message
     * @return the result of handling request
     * @throws PropertyVetoException the property veto exception
     * @throws SQLException the SQL exception
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws InterruptedException the interrupted exception
     */
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
				result = DBConnection.writeReply(uid, dst, msg, reqSize);				
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
    
    /**
     * Handles the request associated with the simulation
     *
     * @param reqType the request type
     * @param request the request message
     * @return the result of handling request
     * @throws SQLException the SQL exception
     */
    private String commandHanlder(int reqType, JSONObject request) throws SQLException {    	
    	String result = null;
    	
    	switch (reqType) {
	    	case opType.mMONITOR:	    		
	    		DBConnection.deleteMonitorResult();
	    		
	    		int[] server_side_monitor = DBConnection.storeClientMonitor();		
	    		DBConnection.storeServerMonitor(server_side_monitor);
	    		
	    		System.out.println(getTime() + " " + ServiceServer.mCoord.getServerLoc()
	    							+ " stored monitoring result");
	    		result = MessageHandler.mSTORE_COMPLETE;
	    		
	    		
	    		//print counter result using log4j
	    		log.debug("tweet count : " + Counter.tweet);
	    		log.debug("read count : " + Counter.read);
	    		log.debug("reply count : " + Counter.reply);
	    		log.debug("retweet count : " + Counter.retweet);
	    		
	    		double totalCount = Counter.tweet + Counter.reply + Counter.retweet;
	    		log.debug("total write count : " + totalCount + "\n");
	    		
	    		double totalOperationCount = Counter.tweet + Counter.read + Counter.reply + Counter.retweet;
	    		log.debug("total write + read count : " + totalOperationCount + "\n");
	    		
	    		//initialize counter
	    		Counter.initializeCounter();
	    		
	    		
	    		break;
	    	/*
	    	case opType.mMOVEOUT:
	    		matchInfo[] match = DBConnection.getMatchResult();	   			    		
	    		
	    		if (match != null) {
		    		for (int i = 0; i < match.length; i++) {
		    			String uname = match[i].getName();
		    			int curr = match[i].getCurr();	    				 			    			
		    			JSONObject migrated = DBConnection.getMigrated(uname);		    					    		
		    			
		    			int res = MessageHandler.sendMigrated(curr, migrated);	    			
		    			if (res == 0)
		    				System.out.println(getTime() + ServiceServer.mCoord.getServerLoc()
		    									+ "failed sending the data " + "[" + uname +"]");		    						    				    						    						    						    		
		    			else {
		    				System.out.println(getTime() + " " + ServiceServer.mCoord.getServerLoc() 
		    									+ " sent the data "
		    									+ "[" + uname + "]");
		    				DBConnection.deleteMigrated(uname);
		    			}
		    		}
	    		}	    		
	    		result = MessageHandler.mDATA_REPLACEMENT_COMPLETE;	    		
	    		break;
	    	case opType.mMOVEIN:   		
	    		JSONObject userItem = (JSONObject) request.get("MIGRATED");	    			    			    		
    			String uname = (String) userItem.get("UNAME");
    			String loc = (String) userItem.get("LOCATION");
    			//JSONArray statusList = (JSONArray) userItem.get("STATUS_LIST");
    				    			
    			int uid = -1;
    			uid = DBConnection.isThere(uname, userType.resident, loc);
    			//result = MessageHandler.msgGenerator(DBConnection.writeStatus(uid, statusList));
    			if (uid != -1) {
    				result = MessageHandler.msgGenerator(1);
    			
    				System.out.println(getTime() + " " + ServiceServer.mCoord.getServerLoc() 
    									+ " received the data "									
    									+ "[" + uname + "]");
    			}    			
	    		break;	    	
	    	case opType.mRESTART:
	    		result = MessageHandler.mRESTART_CPU_MONITORING;
	    		System.out.println(getTime() + " " + ServiceServer.mCoord.getServerLoc() 
									+ " restarts the service server");
	    		break;
	    	*/    		
	    	default:
	    		System.out.println(getTime() + " ERROR: Invalid Operation " + reqType);
	    		result = opType.getOperationName(reqType);
	    		break;
    	}
    	return result;
    }                
    
    /**
     * Gets the current time
     *
     * @return the time
     */
    private String getTime() {    	
    	SimpleDateFormat f = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss.SSS]");
    	return f.format(new Date());
    }
}