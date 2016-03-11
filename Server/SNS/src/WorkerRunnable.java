import java.beans.PropertyVetoException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
				System.out.println(getTime() + ServiceServer.mCoord.getServerLoc() + " handled a request from " + "[" + request.get("SRC") + "]");
        	} else {
        		replacementHanlder(request);
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
    
    private void replacementHanlder(JSONObject request) throws SQLException {
    	int reqType = Integer.parseInt((String) request.get("TYPE"));
    	
    	switch (reqType) {
	    	case opType.monitor:
	    		
	    		// stop cpu util monitoring
				// cal average cpu util
				// store client_side_monitor
				// store server_side_monitor
	    		
	    		CpuMonitor.stopScheduler(ServiceServer.mScheduler);
				
				int server_side_traffic = DBConnection.storeClientMonitor();
				
				CpuMonitor.startCpuMonitor();
	    		break;	    	
	    	case opType.replacement:
	    		break;
	    		
	    	default:
	    		System.out.println("[ERROR] Invalid Operation Type: " + reqType);
    	}    	
    }
    
    private String getTime() {
    	SimpleDateFormat f = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss.SSS]");
    	return f.format(new Date());
    }
}
